package com.lilei.leiaiagent.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体请求参数VO
 */
@Data
@Accessors(chain = true)
@Schema(description = "智能体请求参数")
public class AgentRequestVO {

    /**
     * 用户提示词
     */
    @NotBlank(message = "提示词不能为空")
    @Schema(description = "用户提示词", example = "帮我视频解析", required = true)
    private String prompt;

    /**
     * 会话ID，用于跟踪对话上下文
     */
    @Schema(description = "会话ID，用于跟踪对话上下文", example = "user123-session456")
    private String sessionId;

    /**
     * 系统提示词，用于设置智能体行为
     */
    @Schema(description = "系统提示词，用于设置智能体行为", example = "你是一个视频解析系统")
    private String systemPrompt;

    /**
     * 最大执行步骤数
     */
    @Positive(message = "最大步骤数必须大于0")
    @Schema(description = "最大执行步骤数", example = "10", minimum = "1")
    private Integer maxSteps;
} 