package com.lilei.leiaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.lilei.leiaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 处理工具调用的代理类，实现了ReAct模式中的think和act方法
 * <p>
 * 该类管理工具调用流程：
 * 1. 思考阶段识别需要使用的工具
 * 2. 行动阶段执行工具调用并处理结果
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具列表
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 聊天选项配置
    private final ChatOptions chatOptions;
    
    // 用于跟踪工具调用状态
    private boolean lastToolCallSuccess = true;
    private int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    /**
     * 构造函数
     * 
     * @param availableTools 可用的工具数组
     */
    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
        
        log.info("[Agent: {}] Initialized with {} available tools", getName(), 
                availableTools != null ? availableTools.length : 0);
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 检查是否有太多连续失败
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            log.warn("[Agent: {}] Too many consecutive tool call failures ({}). Stopping execution.", 
                    getName(), consecutiveFailures);
            setState(AgentState.ERROR);
            return false;
        }
        
        try {
            // 添加下一步提示词（如果有）
            addNextStepPromptIfNeeded();
            
            // 获取消息上下文并构建提示
            List<Message> messageList = getMessageList();
            if (messageList.isEmpty()) {
                log.warn("[Agent: {}] Message list is empty, cannot think effectively", getName());
                return false;
            }
            
            Prompt prompt = new Prompt(messageList, this.chatOptions);
            log.debug("[Agent: {}] Thinking with {} messages in context", getName(), messageList.size());
            
            // 调用LLM获取响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            
            // 保存响应结果
            this.toolCallChatResponse = chatResponse;
            
            // 提取助手消息和工具调用
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            
            // 记录思考结果
            String thoughtResult = assistantMessage.getText();
            logThinkingResults(thoughtResult, toolCallList);
            
            // 决定是否需要执行工具调用
            if (toolCallList.isEmpty()) {
                // 没有工具调用，添加助手消息到历史
                getMessageList().add(assistantMessage);
                return false;
            }
            
            // 有工具需要调用
            return true;
        } catch (Exception e) {
            handleThinkingError(e);
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            log.warn("[Agent: {}] No tool calls to execute", getName());
            return "没有工具需要调用";
        }
        
        try {
            log.debug("[Agent: {}] Executing tool calls", getName());
            
            // 调用工具
            Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
            
            // 更新消息上下文
            setMessageList(toolExecutionResult.conversationHistory());
            
            // 获取工具响应消息
            Optional<ToolResponseMessage> toolResponseOpt = getLastToolResponseMessage(toolExecutionResult);
            if (toolResponseOpt.isEmpty()) {
                lastToolCallSuccess = false;
                consecutiveFailures++;
                return "工具调用失败：没有获取到工具响应消息";
            }
            
            ToolResponseMessage toolResponseMessage = toolResponseOpt.get();
            
            // 检查是否调用了终止工具
            boolean terminateToolCalled = isTerminateToolCalled(toolResponseMessage);
            if (terminateToolCalled) {
                setState(AgentState.FINISHED);
                log.info("[Agent: {}] Terminate tool called, finishing execution", getName());
            }
            
            // 重置失败计数器
            lastToolCallSuccess = true;
            consecutiveFailures = 0;
            
            // 格式化工具执行结果
            String results = formatToolResults(toolResponseMessage);
            log.info("[Agent: {}] Tool execution results: {}", getName(), results);
            
            return results;
        } catch (Exception e) {
            lastToolCallSuccess = false;
            consecutiveFailures++;
            log.error("[Agent: {}] Error executing tool calls", getName(), e);
            return "工具调用执行错误：" + e.getMessage();
        }
    }
    
    /**
     * 添加下一步提示词（如果有）
     */
    private void addNextStepPromptIfNeeded() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
            log.debug("[Agent: {}] Added next step prompt to messages", getName());
        }
    }
    
    /**
     * 记录思考结果
     * 
     * @param thoughtResult 思考结果文本
     * @param toolCallList 工具调用列表
     */
    private void logThinkingResults(String thoughtResult, List<AssistantMessage.ToolCall> toolCallList) {
        log.info("[Agent: {}] Thinking result: {}", getName(), thoughtResult);
        log.info("[Agent: {}] Selected {} tool(s) to use", getName(), toolCallList.size());
        
        if (!toolCallList.isEmpty()) {
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.debug("[Agent: {}] Tool call details:\n{}", getName(), toolCallInfo);
        }
    }
    
    /**
     * 处理思考阶段的错误
     * 
     * @param e 异常
     */
    private void handleThinkingError(Exception e) {
        log.error("[Agent: {}] Error during thinking phase", getName(), e);
        getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
        lastToolCallSuccess = false;
        consecutiveFailures++;
    }
    
    /**
     * 获取最后一个工具响应消息
     * 
     * @param toolExecutionResult 工具执行结果
     * @return 工具响应消息（可选）
     */
    private Optional<ToolResponseMessage> getLastToolResponseMessage(ToolExecutionResult toolExecutionResult) {
        if (toolExecutionResult == null || 
            toolExecutionResult.conversationHistory() == null || 
            toolExecutionResult.conversationHistory().isEmpty()) {
            return Optional.empty();
        }
        
        Message lastMessage = CollUtil.getLast(toolExecutionResult.conversationHistory());
        if (lastMessage instanceof ToolResponseMessage) {
            return Optional.of((ToolResponseMessage) lastMessage);
        }
        
        return Optional.empty();
    }
    
    /**
     * 检查是否调用了终止工具
     * 
     * @param toolResponseMessage 工具响应消息
     * @return 是否调用了终止工具
     */
    private boolean isTerminateToolCalled(ToolResponseMessage toolResponseMessage) {
        return toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
    }
    
    /**
     * 格式化工具执行结果
     * 
     * @param toolResponseMessage 工具响应消息
     * @return 格式化的结果
     */
    private String formatToolResults(ToolResponseMessage toolResponseMessage) {
        return toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 获取可用工具列表（不可修改）
     * 
     * @return 不可修改的工具列表
     */
    public List<ToolCallback> getAvailableToolsList() {
        return availableTools == null ? 
               Collections.emptyList() : 
               List.of(availableTools);
    }
    
    /**
     * 清理资源
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        this.toolCallChatResponse = null;
        this.lastToolCallSuccess = true;
        this.consecutiveFailures = 0;
        log.debug("[Agent: {}] Tool call agent resources cleaned up", getName());
    }
}
