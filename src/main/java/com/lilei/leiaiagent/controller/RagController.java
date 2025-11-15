package com.lilei.leiaiagent.controller;

import com.lilei.leiaiagent.service.api.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/rag")
@Tag(name = "知识库问答", description = "集成知识库问答功能，支持文档加载和查询")
public class RagController {
    @Resource
    private RagService ragService;

    @Operation(summary = "流式回答知识库问答", description = "使用RAG模型进行知识库问答，支持流式响应")
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> doChatWithRagQuery(String message, String chatId) {
        return ragService.doChatWithRagQuery(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
