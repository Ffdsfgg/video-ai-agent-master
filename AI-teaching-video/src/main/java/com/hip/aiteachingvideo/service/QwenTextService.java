package com.hip.aiteachingvideo.service;

import com.hip.aiteachingvideo.model.dto.QwenMessage;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface QwenTextService {
    /**
     * 流式输出信息
     */
    Flux<String> streamChat(List<QwenMessage> messages);

    /**
     * 普通输出信息
     */
    String sendChat(List<QwenMessage> messages);


    Flux<String>  streamChatAi(List<QwenMessage> messages);
}
