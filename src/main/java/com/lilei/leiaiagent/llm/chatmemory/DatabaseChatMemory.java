package com.lilei.leiaiagent.llm.chatmemory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.lilei.leiaiagent.dao.ChatMessage;
import com.lilei.leiaiagent.mapper.ChatMessageRepository;
import com.lilei.leiaiagent.utils.MessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DatabaseChatMemory 实现了 ChatMemory 接口，用于将聊天消息持久化存储到数据库中。与RedisChatMemory之间可选一使用
 * 这里使用的postgres数据库，使用MyBatis-Plus作为ORM框架。
 */
@Component
@RequiredArgsConstructor
public class DatabaseChatMemory implements ChatMemory {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessages = messages.stream()
                .map(message -> MessageConverter.toChatMessage(message, conversationId))
                .collect(Collectors.toList());
        
        chatMessageRepository.saveBatch(chatMessages, chatMessages.size());
    }

    @Override
    public List<Message> get(String conversationId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        // 查询最近的 lastN 条消息
        queryWrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByDesc(ChatMessage::getCreateTime);

        List<ChatMessage> chatMessages = chatMessageRepository.list(queryWrapper);

        // 按照时间顺序返回
        if (!chatMessages.isEmpty()) {
            Collections.reverse(chatMessages);
        }

        return chatMessages
                .stream()
                .map(MessageConverter::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getConversationId, conversationId);
        chatMessageRepository.remove(queryWrapper);
    }

}
