package com.hip.aiteachingvideo.service;

import com.hip.aiteachingvideo.model.dto.ChatRequest;
import com.hip.aiteachingvideo.model.dto.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    /**
     * 流式输出信息
     */
    Flux<String> streamChat(List<Message> messages);

    /**
     * 普通输出信息
     */
    String sendChat(List<Message> messages);
}
