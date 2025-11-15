package com.lilei.leiaiagent.service.impl;

import com.lilei.leiaiagent.agent.LiManus;
import com.lilei.leiaiagent.agent.model.AgentState;
import com.lilei.leiaiagent.constant.ApiConstants;
import com.lilei.leiaiagent.constant.ErrorCode;
import com.lilei.leiaiagent.domain.dto.ChatMessageDTO;
import com.lilei.leiaiagent.pojo.vo.AgentRequestVO;
import com.lilei.leiaiagent.pojo.vo.AgentResponseVO;
import com.lilei.leiaiagent.service.api.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能体服务实现类
 */
@Service
@Slf4j
public class AgentServiceImpl implements AgentService {

    @Autowired
    private LiManus liManus;
    
    // 存储活跃的SSE连接，用于可能的手动关闭
    private final Map<String, SseEmitter> activeEmitters = new ConcurrentHashMap<>();

    @Override
    public String executeTask(String prompt) {
        log.info("执行智能体任务（同步模式）：{}", prompt);
        
        try {
            // 确保智能体处于可用状态
            liManus.reset();
            
            // 执行智能体任务
            String result = liManus.run(prompt);
            
            log.info("智能体任务执行完成，状态：{}", liManus.getState());
            
            return result;
        } catch (Exception e) {
            log.error("智能体任务执行失败", e);
            throw new RuntimeException("执行失败：" + e.getMessage(), e);
        }
    }

    @Override
    public AgentResponseVO executeAdvancedTask(AgentRequestVO request) {
        log.info("执行智能体任务（高级同步模式）：{}", request);
        
        try {
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 确保智能体处于可用状态
            liManus.reset();
            
            // 应用自定义参数（如果有）
            applyCustomParameters(request);
            
            // 执行智能体任务
            String result = liManus.run(request.getPrompt());
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 保存历史记录
            saveExecutionHistory(request, result);
            
            // 准备标准响应
            AgentResponseVO response = AgentResponseVO.success(
                    result, 
                    liManus.getState(), 
                    request.getSessionId() != null ? request.getSessionId() : "none",
                    executionTime
            );
            
            log.info("高级智能体任务执行完成，状态：{}，耗时：{}ms", liManus.getState(), executionTime);
            
            return response;
        } catch (Exception e) {
            log.error("高级智能体任务执行失败", e);
            return AgentResponseVO.error("执行失败：" + e.getMessage(), liManus.getState());
        }
    }

    @Override
    public SseEmitter executeTaskStream(String prompt) {
        log.info("执行智能体任务（流式模式）：{}", prompt);
        
        // 创建会话ID
        String sessionId = UUID.randomUUID().toString();
        
        try {
            // 确保智能体处于可用状态
            liManus.reset();
            
            // 执行流式任务
            SseEmitter emitter = liManus.runStream(prompt);
            
            // 保存emitter以便可能的手动关闭
            activeEmitters.put(sessionId, emitter);
            
            // 设置完成后的回调以移除引用
            emitter.onCompletion(() -> {
                activeEmitters.remove(sessionId);
                log.debug("SSE会话已完成并从活跃列表中移除: {}", sessionId);
            });
            
            // 设置超时回调以移除引用
            emitter.onTimeout(() -> {
                activeEmitters.remove(sessionId);
                log.warn("SSE会话超时并从活跃列表中移除: {}", sessionId);
            });
            
            // 设置错误回调
            emitter.onError(e -> {
                activeEmitters.remove(sessionId);
                log.error("SSE会话发生错误并从活跃列表中移除: {}", sessionId, e);
            });
            
            return emitter;
        } catch (Exception e) {
            log.error("流式智能体任务初始化失败", e);
            SseEmitter emitter = new SseEmitter(5000L);
            try {
                emitter.send(SseEmitter.event().name(ApiConstants.EVENT_ERROR).data("初始化失败: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                log.error("无法发送错误消息", ex);
            }
            return emitter;
        }
    }

    @Override
    public SseEmitter executeAdvancedTaskStream(AgentRequestVO request) {
        log.info("执行智能体任务（高级流式模式）：{}", request);

        // 使用提供的会话ID或生成新ID
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : UUID.randomUUID().toString();

        try {
            // 确保智能体处于可用状态
            liManus.reset();

            // 应用自定义参数（如果有）
            applyCustomParameters(request);

            // 执行流式任务
            SseEmitter emitter = liManus.runStream(request.getPrompt());

            // 保存emitter以便可能的手动关闭
            activeEmitters.put(sessionId, emitter);

            // 设置完成后的回调以移除引用
            emitter.onCompletion(() -> {
                activeEmitters.remove(sessionId);
                log.debug("高级SSE会话已完成并从活跃列表中移除: {}", sessionId);
            });

            // 设置超时回调以移除引用
            emitter.onTimeout(() -> {
                activeEmitters.remove(sessionId);
                log.warn("高级SSE会话超时并从活跃列表中移除: {}", sessionId);
            });

            // 设置错误回调
            emitter.onError(e -> {
                activeEmitters.remove(sessionId);
                log.error("高级SSE会话发生错误并从活跃列表中移除: {}", sessionId, e);
            });

            return emitter;
        } catch (Exception e) {
            log.error("高级流式智能体任务初始化失败", e);
            SseEmitter emitter = new SseEmitter(5000L);
            try {
                emitter.send(SseEmitter.event().name(ApiConstants.EVENT_ERROR).data("初始化失败: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                log.error("无法发送错误消息", ex);
            }
            return emitter;
        }
    }

    @Override
    public AgentResponseVO getAgentStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("name", liManus.getName());
        status.put("state", liManus.getState().toString());
        status.put("currentStep", liManus.getCurrentStep());
        status.put("maxSteps", liManus.getMaxSteps());
        status.put("availableTools", liManus.getAvailableToolsList().size());
        status.put("activeConnections", activeEmitters.size());
        
        AgentResponseVO response = new AgentResponseVO()
                .setStatus(ApiConstants.STATUS_SUCCESS)
                .setResult(status.toString())
                .setAgentState(liManus.getState().toString());
        
        return response;
    }

    @Override
    public AgentResponseVO resetAgent() {
        try {
            liManus.reset();
            return AgentResponseVO.success(
                    "智能体已重置", 
                    liManus.getState(), 
                    "none"
            );
        } catch (Exception e) {
            log.error("重置智能体失败", e);
            return AgentResponseVO.error("重置失败: " + e.getMessage(), liManus.getState());
        }
    }

    @Override
    public AgentResponseVO closeStream(String sessionId) {
        if (activeEmitters.containsKey(sessionId)) {
            SseEmitter emitter = activeEmitters.get(sessionId);
            try {
                emitter.send(SseEmitter.event().name(ApiConstants.EVENT_CLOSE).data("手动关闭连接"));
                emitter.complete();
                activeEmitters.remove(sessionId);
                
                return AgentResponseVO.success(
                        "流式连接已关闭", 
                        AgentState.IDLE, 
                        sessionId
                );
            } catch (IOException e) {
                log.error("关闭流式连接失败", e);
                return AgentResponseVO.error("关闭连接失败: " + e.getMessage(), liManus.getState());
            }
        } else {
            return AgentResponseVO.warning("指定的会话ID不存在或已关闭");
        }
    }
    
    @Override
    public boolean saveAgentHistory(String sessionId, ChatMessageDTO message) {
        // 这里可以实现保存历史记录的逻辑，例如存储到数据库
        // 当前为简化实现，仅记录日志
        log.info("保存智能体执行历史：sessionId={}, message={}", sessionId, message);
        return true;
    }
    
    /**
     * 应用自定义参数到智能体
     * 
     * @param request 智能体请求对象
     */
    private void applyCustomParameters(AgentRequestVO request) {
        // 设置最大步骤数（如果提供）
        if (request.getMaxSteps() != null && request.getMaxSteps() > 0) {
            liManus.setMaxSteps(request.getMaxSteps());
        }
        
        // 设置自定义系统提示（如果提供）
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            liManus.setSystemPrompt(request.getSystemPrompt());
        }
    }
    
    /**
     * 保存执行历史记录
     * 
     * @param request 请求对象
     * @param result 执行结果
     */
    private void saveExecutionHistory(AgentRequestVO request, String result) {
        // 创建消息DTO
        ChatMessageDTO messageDTO = new ChatMessageDTO()
                .setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString())
                .setRole("assistant")
                .setContent(result)
                .setCreateTime(LocalDateTime.now())
                .setType("text");
        
        // 保存历史记录
        saveAgentHistory(messageDTO.getSessionId(), messageDTO);
    }
} 