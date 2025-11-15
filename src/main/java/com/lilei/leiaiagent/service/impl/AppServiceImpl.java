package com.lilei.leiaiagent.service.impl;

import com.lilei.leiaiagent.pojo.vo.ReportVO;
import com.lilei.leiaiagent.service.api.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@Service
@Slf4j
public class AppServiceImpl implements AppService {
    @Resource
    private ChatClient chatClient;

    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    @Override
    public String doChat(String message, String chatId) {
        String answer = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID , chatId))
                .call()
                .content();
        log.info("Chat response: {}", answer);
        return answer;
    }

    @Override
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @Override
    public ReportVO doChatWithStructuredOutput(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .entity(ReportVO.class);
    }

    @Override
    public Flux<String> doChatByStreamWithTool(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .stream()
                .content();
    }

    @Override
    public Flux<String> doChatByStreamWithToolAndCallback(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .stream()
                .content()
                .doOnNext(content -> {
                    log.info("Received content: {}", content);
                    // 这里可以添加对工具调用结果的处理逻辑
                });
    }



}
