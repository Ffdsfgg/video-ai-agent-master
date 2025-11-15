package com.lilei.leiaiagent.rag.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文本切片器
 * 1.最佳文档切片策略是 结合智能分块算法和人工二次校验。智能分块算法基于分句标识符先划分为段落再根据语义相关性动态选择切片点，
 * 避免固定长度切分导致的语义断裂。在实际应用中，应尽量让文本切片包含完整信息，同时避免包含过多干扰信息。
 * 2.在编程实现上，可以通过 Spring Al 的 ETL Pipeline 提供的 DocumentTransformer 来调整切分规则，代码如下
 */
@Component
public class SelfTokenTextSplitter {
    // 默认构造函数使用默认的 TokenTextSplitter
    public List<Document> splitDocuments(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    // 自定义切分规则
    public List<Document> splitCustomized(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 10, 5000, true);
        return splitter.apply(documents);
    }
}
