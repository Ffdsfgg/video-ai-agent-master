package com.lilei.leiaiagent.utils;

import com.lilei.leiaiagent.dao.ChatMessage;
import org.springframework.ai.chat.messages.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MessageConverter {

    /**
     * 将 Message 转换为 ChatMessage
     */
    public static ChatMessage toChatMessage(Message message, String conversationId) {
        return ChatMessage.builder()
                .conversationId(conversationId)
                .messageType(message.getMessageType())
                .content(message.getText())
                .metadata(message.getMetadata())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 将 ChatMessage 转换为 Message
     */
    public static Message toMessage(ChatMessage chatMessage) {
        MessageType messageType = chatMessage.getMessageType();
        String text = chatMessage.getContent();
        Map<String, Object> metadata = chatMessage.getMetadata();
        return switch (messageType) {
            case USER -> new UserMessage(text);
            case ASSISTANT -> new AssistantMessage(text, metadata);
            case SYSTEM -> new SystemMessage(text);
            case TOOL -> new ToolResponseMessage(List.of(), metadata);
        };
    }

}