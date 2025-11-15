package com.lilei.leiaiagent.llm.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;

/**
 * 自定义日志 advisor
 * 打印 info 级别日志，只输出单次用户提示词和AI回复的文本
 */
@Slf4j
public class LoggerAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        log.info("AI Request: {}", chatClientRequest.prompt().getUserMessage().getText());
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
