package com.lilei.leiaiagent.rag.vectorstore;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建 ContextualQueryAugmenter 的工厂类 功能：
 * 如果启用 “允许空上下文"，系统会自动处理空 Prompt 情况，默认表示 “不知道”
 * 如果启用 “不允许空上下文"，可以自定义错误处理逻辑，来运用工厂模式创建一个自定义的 ContextualQueryAugmenter:
 */
public class ContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该基于下面的内容格式进行输出：
                抱歉，我只能回答xx相关的问题，别的没办法帮到您哦，
                有问题可以联系人工客服 联系方式: 11111111111
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false) // 是否允许空上下文
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
