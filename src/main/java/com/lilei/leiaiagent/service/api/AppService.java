package com.lilei.leiaiagent.service.api;

import com.lilei.leiaiagent.pojo.vo.ReportVO;
import reactor.core.publisher.Flux;

public interface AppService {
    // 简单聊天
    String doChat(String message, String chatId);

    // 流式聊天
    Flux<String> doChatByStream(String message, String chatId);

    // 结构化输出
    ReportVO doChatWithStructuredOutput(String message, String chatId);

    // 流式聊天，使用工具
    Flux<String> doChatByStreamWithTool(String message, String chatId);

    // 流式聊天，使用工具和回调
    Flux<String> doChatByStreamWithToolAndCallback(String message, String chatId);
}
