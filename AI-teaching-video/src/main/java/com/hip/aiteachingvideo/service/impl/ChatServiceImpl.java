package com.hip.aiteachingvideo.service.impl;

import cn.hutool.json.JSONUtil;
import com.hip.aiteachingvideo.model.dto.ChatRequest;
import com.hip.aiteachingvideo.model.dto.Message;
import com.hip.aiteachingvideo.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private WebClient webClient;

    @Value("${hip.aliyun.quen-vl.url}")
    private String url;
    @Value("${hip.aliyun.quen-vl.model}")
    private String model;

    public Flux<String> streamChat(List<Message> messages) {
        ChatRequest chatRequest = ChatRequest.builder()
                .model(model)
                .stream(true)
                .messages(messages)
                .build();
        log.info("流式输出请求日志：{}", JSONUtil.toJsonStr(chatRequest));
        return webClient.post()
                .uri(url)
                .bodyValue(JSONUtil.toJsonStr(chatRequest))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> System.out.println("Received chunk: " + chunk)) // 添加这行用于调试
                .filter(chunk -> chunk.contains("\"content\""))
                .map(chunk -> {
                    int start = chunk.indexOf("\"content\":\"") + 11;
                    int end = chunk.indexOf("\"", start);
                    return chunk.substring(start, end);
                })
                .onErrorResume(e -> Flux.just("【错误: " + e.getMessage() + "】"));
    }

    public String sendChat(List<Message> messages) {
        ChatRequest chatRequest = ChatRequest.builder()
                .model(model)
                .stream(false)
                .messages(messages)
                .build();
        System.out.println(JSONUtil.toJsonStr(chatRequest));
        return webClient.post()
                .uri(url)
                .bodyValue(JSONUtil.toJsonStr(chatRequest))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}