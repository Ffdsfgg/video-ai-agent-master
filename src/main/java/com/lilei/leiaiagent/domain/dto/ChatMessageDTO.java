package com.lilei.leiaiagent.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 聊天消息数据传输对象
 * <p>
 * 用于在不同层之间传递聊天消息数据
 */
@Data
@Accessors(chain = true)
public class ChatMessageDTO {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 角色：user-用户，assistant-助手，system-系统
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息类型：text-文本，image-图片，file-文件
     */
    private String type;

    /**
     * 消息顺序
     */
    private Integer sequence;

    /**
     * 父消息ID（回复哪条消息）
     */
    private String parentMessageId;

    /**
     * 工具调用信息（JSON格式）
     */
    private String toolCalls;

    /**
     * 工具调用结果（JSON格式）
     */
    private String toolResults;
} 