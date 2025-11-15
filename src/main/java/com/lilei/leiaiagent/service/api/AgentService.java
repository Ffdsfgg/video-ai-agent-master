package com.lilei.leiaiagent.service.api;

import com.lilei.leiaiagent.domain.dto.ChatMessageDTO;
import com.lilei.leiaiagent.pojo.vo.AgentRequestVO;
import com.lilei.leiaiagent.pojo.vo.AgentResponseVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能体服务接口
 * <p>
 * 提供智能体相关的服务功能
 */
public interface AgentService {

    /**
     * 执行智能体任务（同步模式）
     *
     * @param prompt 用户提示词
     * @return 执行结果
     */
    String executeTask(String prompt);

    /**
     * 执行智能体任务（高级同步模式）
     *
     * @param request 智能体请求对象
     * @return 执行结果
     */
    AgentResponseVO executeAdvancedTask(AgentRequestVO request);

    /**
     * 执行智能体任务（流式模式）
     *
     * @param prompt 用户提示词
     * @return SSE事件发射器
     */
    SseEmitter executeTaskStream(String prompt);

    /**
     * 执行智能体任务（高级流式模式）
     *
     * @param request 智能体请求对象
     * @return SSE事件发射器
     */
    SseEmitter executeAdvancedTaskStream(AgentRequestVO request);

    /**
     * 获取智能体状态
     *
     * @return 状态信息
     */
    AgentResponseVO getAgentStatus();

    /**
     * 重置智能体状态
     *
     * @return 操作结果
     */
    AgentResponseVO resetAgent();

    /**
     * 关闭流式连接
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    AgentResponseVO closeStream(String sessionId);
    
    /**
     * 保存智能体执行历史
     *
     * @param sessionId 会话ID
     * @param message 消息内容
     * @return 是否保存成功
     */
    boolean saveAgentHistory(String sessionId, ChatMessageDTO message);
} 