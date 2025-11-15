package com.lilei.leiaiagent.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 * <p>
 * 表示用户与系统的一次完整对话会话
 */
@Data
@Accessors(chain = true)
public class ChatSession {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 会话状态：active-活跃，archived-归档，deleted-删除
     */
    private String status;

    /**
     * 会话类型：chat-普通聊天，agent-智能体会话，rag-知识库增强
     */
    private String type;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 消息数量
     */
    private Integer messageCount;
} 