package com.hip.aiteachingvideo.controller;

import com.hip.aiteachingvideo.model.dto.Message;
import com.hip.aiteachingvideo.service.ChatService;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@RestController
@Tag(name = "ai请求服务")
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 流式输出
     *
     * @param messages 信息
     * @return 流式
     */
    @Operation(summary = "AI流式输出")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<byte[]> streamChat(@RequestBody List<Message> messages) {
        return chatService.streamChat(messages)
                .map(content -> content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 普通输出
     *
     * @param messages 信息
     * @return 字符串
     */
    @Operation(summary = "AI普通输出")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Result chat(@RequestBody List<Message> messages) {
        return Result.ok(chatService.sendChat(messages));
    }
}