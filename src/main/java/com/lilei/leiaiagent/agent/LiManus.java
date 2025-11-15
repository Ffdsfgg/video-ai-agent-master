package com.lilei.leiaiagent.agent;

import com.lilei.leiaiagent.llm.advisor.LoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * LiManus - 全能AI助手
 * <p>
 * 一个强大的AI智能体，具备自主规划能力，可以解决用户提出的各种任务
 * 通过整合多种工具和自动选择合适的工具来解决复杂问题
 */
@Component
@Slf4j
public class LiManus extends ToolCallAgent {

    /**
     * 系统提示词 - 定义AI助手的核心能力和行为
     */
    private static final String SYSTEM_PROMPT = """
            You are VideoAnalysis, an all-capable AI assistant, aimed at solving any task presented by the user.
            You have various tools at your disposal that you can call upon to efficiently complete complex requests.
            
            Core capabilities:
            1. Understanding user needs and breaking them down into manageable steps
            2. Selecting and using the most appropriate tools for each task
            3. Providing clear explanations of your actions and results
            4. Maintaining context throughout multi-step interactions
            
            Always prioritize accuracy and helpfulness in your responses.
            """;

    /**
     * 下一步提示词 - 指导AI助手如何处理后续步骤
     */
    private static final String NEXT_STEP_PROMPT = """
            Based on user needs and our previous conversation, please:
            
            1. Analyze the current state of the task
            2. Proactively select the most appropriate tool or combination of tools
            3. For complex tasks, break down the problem into manageable steps
            4. After using each tool, explain the results and suggest next steps clearly
            5. If the task is complete or you want to stop the interaction, use the `doTerminate` function call
            
            Focus on finding the most efficient solution path.
            """;

    /**
     * 构造函数 - 初始化VideoAnalysis智能体
     * 
     * @param allTools 可用的所有工具
     * @param dashscopeChatModel 大语言模型
     */
    public LiManus(
            @Qualifier("allTools") ToolCallback[] allTools, 
            ChatModel dashscopeChatModel) {
        super(allTools);
        
        try {
            // 设置基本属性
            this.setName("liManus");
            this.setSystemPrompt(SYSTEM_PROMPT);
            this.setNextStepPrompt(NEXT_STEP_PROMPT);
            this.setMaxSteps(20);
            
            log.info("Initializing VideoAnalysis agent with {} available tools",
                    allTools != null ? allTools.length : 0);
            
            // 初始化 AI 对话客户端
            ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                    .defaultAdvisors(new LoggerAdvisor())
                    .build();
            this.setChatClient(chatClient);
            
            log.info("VideoAnalysis agent successfully initialized");
        } catch (Exception e) {
            log.error("Failed to initialize VideoAnalysis agent", e);
            throw new RuntimeException("Failed to initialize VideoAnalysis agent: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重置智能体状态，保持特定配置
     */
    @Override
    public void reset() {
        super.reset();
        // 如果有任何特定于VideoAnalysis的重置逻辑，可以在这里添加
        log.debug("VideoAnalysis agent reset completed, ready for new tasks");
    }
}
