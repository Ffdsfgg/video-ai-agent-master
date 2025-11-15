package com.lilei.leiaiagent.controller;

import com.lilei.leiaiagent.constant.ApiConstants;
import com.lilei.leiaiagent.pojo.vo.AgentRequestVO;
import com.lilei.leiaiagent.pojo.vo.AgentResponseVO;
import com.lilei.leiaiagent.service.api.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能体控制器
 * <p>
 * 提供与AI智能体交互的REST API接口
 * 支持同步和流式响应模式
 */
@RestController
@RequestMapping(ApiConstants.AGENT_PATH)
@Tag(name = "智能体 API", description = "提供各种AI智能体的交互接口，包括同步和流式响应")
@Slf4j
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 使用LiManus智能体处理用户请求（同步模式）
     *
     * @param prompt 用户请求内容
     * @return 智能体处理结果
     */
    @Operation(summary = "执行智能体任务（同步模式）",
            description = "使用VideoAnalysis智能体处理用户请求，同步返回执行结果")
    @PostMapping(ApiConstants.AGENT_EXECUTE)
    public ResponseEntity<AgentResponseVO> executeTask(
            @Parameter(description = "用户请求内容", required = true)
            @RequestParam @NotBlank String prompt) {

        log.info("接收到同步智能体请求：{}", prompt);

        try {
            String result = agentService.executeTask(prompt);
            return ResponseEntity.ok(new AgentResponseVO()
                    .setStatus(ApiConstants.STATUS_SUCCESS)
                    .setResult(result));
        } catch (Exception e) {
            log.error("智能体任务执行失败", e);
            return ResponseEntity.internalServerError().body(
                    AgentResponseVO.error("执行失败：" + e.getMessage(), null)
            );
        }
    }

    /**
     * 使用LiManus智能体处理用户请求（高级同步模式）
     *
     * @param request 智能体请求对象
     * @return 智能体处理结果
     */
    @Operation(summary = "执行智能体任务（高级同步模式）",
            description = "使用VideoAnalysis智能体处理复杂请求，支持会话、系统提示和步骤控制")
    @PostMapping(ApiConstants.AGENT_EXECUTE_ADVANCED)
    public ResponseEntity<AgentResponseVO> executeAdvancedTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "智能体请求参数",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AgentRequestVO.class)
                    )
            )
            @Valid @RequestBody AgentRequestVO request) {

        log.info("接收到高级同步智能体请求：{}", request);

        try {
            AgentResponseVO response = agentService.executeAdvancedTask(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("高级智能体任务执行失败", e);
            return ResponseEntity.internalServerError().body(
                    AgentResponseVO.error("执行失败：" + e.getMessage(), null)
            );
        }
    }

    /**
     * 使用VideoAnalysis智能体处理用户请求（流式模式）
     *
     * @param prompt 用户请求内容
     * @return SSE事件流
     */
    @Operation(summary = "执行智能体任务（流式模式）",
            description = "使用VideoAnalysis智能体处理用户请求，通过SSE流式返回执行过程和结果")
    @GetMapping(value = ApiConstants.AGENT_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeTaskStream(
            @Parameter(description = "用户请求内容", required = true)
            @RequestParam @NotBlank String prompt) {

        log.info("接收到流式智能体请求：{}", prompt);

        return agentService.executeTaskStream(prompt);
    }

    /**
     * 使用VideoAnalysis智能体处理用户请求（流式模式）
     *
     * @param prompt 用户请求内容
     * @return SSE事件流
     */
    @Operation(summary = "执行智能体任务（流式模式）",
            description = "使用VideoAnalysis智能体处理用户请求，通过SSE流式返回执行过程和结果")
    @PostMapping(value = ApiConstants.AGENT_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeTaskStreams(
            @Parameter(description = "用户请求内容", required = true)
            @RequestParam @NotBlank String prompt) {

        log.info("接收到流式智能体请求：{}", prompt);

        return agentService.executeTaskStream(prompt);
    }
    /**
     * 使用LiManus智能体处理用户请求（高级流式模式）
     *
     * @param request 智能体请求对象
     * @return SSE事件流
     */
    @Operation(summary = "执行智能体任务（高级流式模式）",
            description = "使用LiManus智能体处理复杂请求，支持会话、系统提示和步骤控制，通过SSE流式返回执行过程和结果")
    @PostMapping(value = ApiConstants.AGENT_STREAM_ADVANCED)
    public SseEmitter executeAdvancedTaskStream(@Valid @RequestBody AgentRequestVO request) {

        log.info("接收到高级流式智能体请求：{}", request);

        return agentService.executeAdvancedTaskStream(request);
    }

    /**
     * 获取智能体当前状态
     *
     * @return 智能体状态信息
     */
    @Operation(summary = "获取智能体状态",
            description = "获取VideoAnalysis智能体的当前状态和配置信息")
    @GetMapping(ApiConstants.AGENT_STATUS)
    public ResponseEntity<AgentResponseVO> getAgentStatus() {
        return ResponseEntity.ok(agentService.getAgentStatus());
    }

    /**
     * 重置智能体状态
     *
     * @return 操作结果
     */
    @Operation(summary = "重置智能体",
            description = "将VideoAnalysis智能体重置到初始状态，清除所有当前上下文")
    @PostMapping(ApiConstants.AGENT_RESET)
    public ResponseEntity<AgentResponseVO> resetAgent() {
        return ResponseEntity.ok(agentService.resetAgent());
    }

    /**
     * 关闭特定会话的流式连接
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Operation(summary = "关闭流式连接",
            description = "手动关闭指定会话ID的SSE流式连接")
    @PostMapping(ApiConstants.AGENT_STREAM_CLOSE)
    public ResponseEntity<AgentResponseVO> closeStream(
            @Parameter(description = "会话ID", required = true)
            @PathVariable String sessionId) {

        return ResponseEntity.ok(agentService.closeStream(sessionId));
    }
}
