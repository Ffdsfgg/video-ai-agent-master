package com.lilei.leiaiagent.model;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatClientConfigTest {
    @Resource
    ChatClient chatClient;

    @Test
    void chatClient() {
        String call = chatClient.prompt()
                .user("你好，我是李雷")
                .call().content();
    }
}