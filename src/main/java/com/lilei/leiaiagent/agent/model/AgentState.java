package com.lilei.leiaiagent.agent.model;

/**
 * 代理执行状态的枚举类
 * <p>
 * 此枚举定义了代理可能的执行状态，用于跟踪代理的生命周期。
 * 状态转换通常遵循以下规则：
 * - IDLE → RUNNING: 当代理开始执行任务
 * - RUNNING → FINISHED: 当代理成功完成任务
 * - RUNNING → ERROR: 当代理执行过程中遇到错误
 * - ERROR/FINISHED → IDLE: 当代理被重置以准备执行新的任务
 */
public enum AgentState {

    /**
     * 空闲状态
     * <p>
     * 代理未在执行任务，可以接受新任务。
     * 这是代理的初始状态和重置后的状态。
     */
    IDLE,

    /**
     * 运行中状态
     * <p>
     * 代理正在执行任务，处理用户请求。
     * 在此状态下，代理不应接受新任务。
     */
    RUNNING,

    /**
     * 已完成状态
     * <p>
     * 代理已成功完成任务。
     * 可以通过重置回到空闲状态以接受新任务。
     */
    FINISHED,

    /**
     * 错误状态
     * <p>
     * 代理在执行过程中遇到错误或异常。
     * 需要检查日志了解错误原因，并可能需要重置代理。
     */
    ERROR;
    
    /**
     * 检查当前状态是否允许开始新任务
     * 
     * @return 如果状态为IDLE则返回true，否则返回false
     */
    public boolean canStartNewTask() {
        return this == IDLE;
    }
    
    /**
     * 检查当前状态是否表示代理处于活动状态
     * 
     * @return 如果状态为RUNNING则返回true，否则返回false
     */
    public boolean isActive() {
        return this == RUNNING;
    }
    
    /**
     * 检查当前状态是否表示执行已结束（成功或失败）
     * 
     * @return 如果状态为FINISHED或ERROR则返回true，否则返回false
     */
    public boolean isTerminated() {
        return this == FINISHED || this == ERROR;
    }
    
    /**
     * 检查当前状态是否表示执行成功结束
     * 
     * @return 如果状态为FINISHED则返回true，否则返回false
     */
    public boolean isSuccessful() {
        return this == FINISHED;
    }
}