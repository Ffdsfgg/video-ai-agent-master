package com.lilei.leiaiagent.dao;

import lombok.Data;

@Data
public class AdvancedChatRequest {
    private String prompt;        // 用户问题
    private String sessionId;     // 会话唯一标识
    private String systemPrompt;  // 系统级提示
    private Integer maxSteps;     // 最大工具调用步数
}
