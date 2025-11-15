package com.lilei.leiaiagent.controller;

import com.lilei.leiaiagent.pojo.vo.ReportVO;
import com.lilei.leiaiagent.service.api.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
//@OpenAPIDefinition(info = @Info(title = "Chat API", version = "1.0", description = "API for chat operations"))
@Tag(name = "AI聊天", description = "集成机器人角色设定，会话存储，聊天记录持久化")
public class AppController {
    @Resource
    private AppService appService;

    @Operation(summary = "对话聊天-同步调用", description = "处理用户消息并返回聊天响应")
    @GetMapping("/chat")
    public ResponseEntity<String> doChat(
            @Parameter(description = "Message to send") @RequestParam String message,
            @Parameter(description = "Chat ID") @RequestParam String chatId
    ) {
        return ResponseEntity.ok(appService.doChat(message, chatId));
    }

    @Operation(summary = "对话聊天-流式调用（方法一）", description = "使用 Flux 实现流式聊天")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatStreamMethod1(String message, String chatId) {
        return appService.doChatByStream(message, chatId);
    }

    @Operation(summary = "对话聊天-流式调用（方法二）", description = "使用 Server-Sent Events (SSE) 实现流式聊天")
    @GetMapping("/chat/stream-sse")
    public Flux<ServerSentEvent<String>> doChatStreamSSE(String message, String chatId) {
        return appService.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @Operation(summary = "对话聊天-流式调用（方法三）", description = "使用 SseEmitter 实现流式聊天")
    @GetMapping("/chat/stream-sse-emitter")
    public SseEmitter doChatStreamSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        appService.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    @Operation(summary = "结构化输出聊天报告", description = "处理用户消息并返回结构化的聊天报告")
    @GetMapping("/chat/report")
    public ResponseEntity<ReportVO> doChatReport(String message, String chatId) {
        ReportVO reportVO = appService.doChatWithStructuredOutput(message, chatId);
        return ResponseEntity.ok(reportVO);
    }

    @Operation(summary = "流式聊天，使用工具", description = "处理用户消息并返回流式聊天结果，支持工具调用")
    @GetMapping(value = "/chat/tools")
    public Flux<ServerSentEvent<String>> doChatWithTools(String message, String chatId) {
        return appService.doChatByStreamWithTool(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @Operation(summary = "流式聊天，使用工具和回调", description = "处理用户消息并返回流式聊天结果，支持工具调用和回调")
    @GetMapping(value = "/chat/tools/callback")
    public Flux<ServerSentEvent<String>> doChatWithToolsAndCallback(String message, String chatId) {
        return appService.doChatByStreamWithToolAndCallback(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
