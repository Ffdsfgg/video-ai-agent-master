package com.lilei.leiaiagent.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话数据传输对象
 * <p>
 * 用于在不同层之间传递聊天会话数据
 */
@Data
@Accessors(chain = true)
public class ChatSessionDTO {

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
    
    /**
     * 会话消息列表
     */
    private List<ChatMessageDTO> messages;
} 