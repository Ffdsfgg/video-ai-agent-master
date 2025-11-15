package com.lilei.leiaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 * <p>
 * 这种模式遵循"思考-行动"的循环，让代理先分析当前情况，
 * 然后决定是否需要执行具体行动。如果不需要行动，可以直接返回结果。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     * 这个方法实现了ReAct模式的核心逻辑：先思考后行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 记录开始执行步骤
            log.debug("[Agent: {}] Starting step execution - thinking phase", getName());
            
            // 先思考，决定是否需要行动
            boolean shouldAct = think();
            
            if (!shouldAct) {
                log.debug("[Agent: {}] Thinking completed - no action needed", getName());
                return "思考完成 - 无需行动";
            }
            
            // 需要行动，执行行动
            log.debug("[Agent: {}] Thinking completed - action needed, executing action", getName());
            String actionResult = act();
            log.debug("[Agent: {}] Action completed with result: {}", getName(), actionResult);
            
            return actionResult;
        } catch (Exception e) {
            // 记录异常日志
            log.error("[Agent: {}] Error during step execution", getName(), e);
            return "步骤执行失败：" + e.getMessage();
        }
    }
    
    /**
     * 重置代理状态
     * 扩展基类方法实现ReAct特定的重置逻辑
     */
    @Override
    public void reset() {
        super.reset(); // 调用基类的重置方法
        log.debug("[Agent: {}] ReAct agent reset completed", getName());
    }
}
