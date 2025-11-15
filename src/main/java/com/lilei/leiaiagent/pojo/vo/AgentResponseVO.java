package com.lilei.leiaiagent.pojo.vo;

import com.lilei.leiaiagent.agent.model.AgentState;
import com.lilei.leiaiagent.constant.ApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 智能体响应数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "智能体响应结果")
public class AgentResponseVO {

    /**
     * 响应状态：success-成功，error-错误，warning-警告
     */
    @Schema(description = "响应状态：success-成功，error-错误，warning-警告", example = "success")
    private String status;

    /**
     * 智能体执行结果
     */
    @Schema(description = "智能体执行结果", example = "我为您解析了你的问题，请查看...")
    private String result;

    /**
     * 智能体执行状态
     */
    @Schema(description = "智能体执行状态", example = "FINISHED", 
            allowableValues = {"IDLE", "RUNNING", "FINISHED", "ERROR"})
    private String agentState;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "user123-session456")
    private String sessionId;

    /**
     * 错误信息（如果有）
     */
    @Schema(description = "错误信息（如果有）", example = "执行失败：模型调用超时")
    private String message;

    @Schema(description = "响应时间戳")
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 执行时间（毫秒）
     */
    @Schema(description = "执行时间（毫秒）", example = "1500")
    private Long executionTime;

    /**
     * 创建成功响应
     *
     * @param result 执行结果
     * @param state 智能体状态
     * @param sessionId 会话ID
     * @return 响应对象
     */
    public static AgentResponseVO success(String result, AgentState state, String sessionId) {
        return new AgentResponseVO()
                .setStatus(ApiConstants.STATUS_SUCCESS)
                .setResult(result)
                .setAgentState(state != null ? state.toString() : "UNKNOWN")
                .setSessionId(sessionId);
    }

    /**
     * 创建成功响应（带执行时间）
     *
     * @param result 执行结果
     * @param state 智能体状态
     * @param sessionId 会话ID
     * @param executionTime 执行时间（毫秒）
     * @return 响应对象
     */
    public static AgentResponseVO success(String result, AgentState state, String sessionId, long executionTime) {
        return success(result, state, sessionId)
                .setExecutionTime(executionTime);
    }

    /**
     * 创建错误响应
     *
     * @param message 错误信息
     * @param state 智能体状态
     * @return 响应对象
     */
    public static AgentResponseVO error(String message, AgentState state) {
        return new AgentResponseVO()
                .setStatus(ApiConstants.STATUS_ERROR)
                .setMessage(message)
                .setAgentState(state != null ? state.toString() : "ERROR");
    }

    /**
     * 创建警告响应
     *
     * @param message 警告信息
     * @return 响应对象
     */
    public static AgentResponseVO warning(String message) {
        return new AgentResponseVO()
                .setStatus(ApiConstants.STATUS_WARNING)
                .setMessage(message);
    }
} 