package com.lilei.leiaiagent.constant;

/**
 * API常量
 * <p>
 * 定义API相关的常量，如路径、参数名等
 */
public class ApiConstants {

    // API路径前缀
    public static final String API_PREFIX = "/api";
    
    // 控制器路径
    public static final String AGENT_PATH = "/agent";
    public static final String APP_PATH = "/ai";
    public static final String RAG_PATH = "/rag";
    
    // 智能体API路径
    public static final String AGENT_EXECUTE = "/execute";
    public static final String AGENT_EXECUTE_ADVANCED = "/execute/advanced";
    public static final String AGENT_STREAM = "/stream";
    public static final String AGENT_STREAM_ADVANCED = "/stream/advanced";
    public static final String AGENT_STATUS = "/status";
    public static final String AGENT_RESET = "/reset";
    public static final String AGENT_STREAM_CLOSE = "/stream/close/{sessionId}";
    
    // 应用API路径
    public static final String APP_CHAT = "/chat";
    
    // RAG API路径
    public static final String RAG_QUERY = "/query";
    
    // 参数名称
    public static final String PARAM_PROMPT = "prompt";
    public static final String PARAM_CHAT_ID = "chatId";
    public static final String PARAM_SESSION_ID = "sessionId";
    public static final String PARAM_MAX_STEPS = "maxSteps";
    public static final String PARAM_SYSTEM_PROMPT = "systemPrompt";
    
    // 响应字段名称
    public static final String RESP_STATUS = "status";
    public static final String RESP_RESULT = "result";
    public static final String RESP_MESSAGE = "message";
    public static final String RESP_AGENT_STATE = "agentState";
    public static final String RESP_SUCCESS = "success";
    public static final String RESP_CODE = "code";
    public static final String RESP_ERRORS = "errors";
    
    // 状态值
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_WARNING = "warning";
    
    // SSE事件名称
    public static final String EVENT_START = "start";
    public static final String EVENT_STEP = "step";
    public static final String EVENT_MAX_STEPS = "max_steps";
    public static final String EVENT_COMPLETE = "complete";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_CLOSE = "close";
    
    private ApiConstants() {
        // 私有构造函数，防止实例化
    }
} 