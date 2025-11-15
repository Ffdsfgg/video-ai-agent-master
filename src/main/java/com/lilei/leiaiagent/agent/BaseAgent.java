package com.lilei.leiaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.lilei.leiaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     * @throws IllegalArgumentException 如果提示词为空或代理状态不正确
     * @throws RuntimeException 如果执行过程中出现错误
     */
    public String run(String userPrompt) {
        validateBeforeRun(userPrompt);

        // 设置状态并记录消息上下文
        this.state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("[Agent: {}] Executing step {}/{}", name, stepNumber, maxSteps);
                
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
                
                // 允许短暂暂停以避免可能的速率限制
                TimeUnit.MILLISECONDS.sleep(100);
            }
            
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
                log.warn("[Agent: {}] Execution terminated due to reaching max steps ({})", name, maxSteps);
            }
            
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("[Agent: {}] Error executing agent", name, e);
            return "执行错误: " + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return SSE事件发射器
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                // 验证参数
                validateBeforeRun(userPrompt);
                
                // 设置状态并记录消息上下文
                this.state = AgentState.RUNNING;
                messageList.add(new UserMessage(userPrompt));
                
                // 开始任务通知
                sseEmitter.send("Agent '" + name + "' starting execution...");
                
                // 执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("[Agent: {}] Executing step {}/{}", name, stepNumber, maxSteps);
                    
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    
                    // 输出当前每一步的结果到 SSE
                    sseEmitter.send(result);
                    
                    // 允许短暂暂停以避免可能的速率限制
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                    state = AgentState.FINISHED;
                    String message = "执行结束：达到最大步骤（" + maxSteps + "）";
                    log.warn("[Agent: {}] {}", name, message);
                    sseEmitter.send(message);
                }
                
                // 发送完成事件并正常完成
                sseEmitter.send("Agent '" + name + "' completed successfully.");
                sseEmitter.complete();
                
            } catch (Exception e) {
                state = AgentState.ERROR;
                String errorMessage = "执行错误：" + e.getMessage();
                log.error("[Agent: {}] {}", name, errorMessage, e);
                
                try {
                    sseEmitter.send(errorMessage);
                    sseEmitter.complete();
                } catch (IOException ex) {
                    log.error("[Agent: {}] Error while sending error message via SSE", name, ex);
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
        });

        // 设置回调处理
        configureEmitterCallbacks(sseEmitter);
        
        return sseEmitter;
    }

    /**
     * 配置SSE发射器的回调处理
     * 
     * @param sseEmitter SSE发射器
     */
    private void configureEmitterCallbacks(SseEmitter sseEmitter) {
        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            log.warn("[Agent: {}] SSE connection timeout", name);
            this.cleanup();
        });
        
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            log.info("[Agent: {}] SSE connection completed", name);
            this.cleanup();
        });
        
        // 设置错误回调
        sseEmitter.onError((ex) -> {
            this.state = AgentState.ERROR;
            log.error("[Agent: {}] SSE connection error", name, ex);
            this.cleanup();
        });
    }

    /**
     * 验证运行前的参数和状态
     *
     * @param userPrompt 用户提示词
     * @throws IllegalArgumentException 如果验证失败
     */
    private void validateBeforeRun(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            String error = "Cannot run agent from state: " + this.state;
            log.error("[Agent: {}] {}", name, error);
            throw new IllegalArgumentException(error);
        }
        
        if (StrUtil.isBlank(userPrompt)) {
            String error = "Cannot run agent with empty user prompt";
            log.error("[Agent: {}] {}", name, error);
            throw new IllegalArgumentException(error);
        }
        
        if (chatClient == null) {
            String error = "Chat client is not initialized";
            log.error("[Agent: {}] {}", name, error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * 定义单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 获取消息历史列表（不可修改版本，安全）
     * 
     * @return 不可修改的消息列表
     */
    public List<Message> getMessageHistory() {
        return Collections.unmodifiableList(messageList);
    }

    /**
     * 重置代理状态，准备新的执行
     */
    public void reset() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.messageList.clear();
        log.info("[Agent: {}] Reset to idle state", name);
    }

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 基类提供基本清理，子类可以扩展
        log.debug("[Agent: {}] Cleaning up resources", name);
    }
}
