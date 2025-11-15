package com.lilei.leiaiagent.rag.vectorstore;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问
 * 向量存储 +文档检索器 + 空上下文增强器
 */
public class RagCustomAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     *
     * @param vectorStore 向量存储
     * @return 自定义的 RAG 检索增强顾问
     */
    public static Advisor createInstance(VectorStore vectorStore, String fileName) {
        // 过滤特定状态的文档
//        Filter.Expression expression = new FilterExpressionBuilder()
//                .eq("filename", fileName)
//                .build();
        // 创建文档检索器
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore) // 向量存储
//                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.5) // 相似度阈值
                .topK(5) // 返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever) // 文档检索器
                .queryAugmenter(ContextualQueryAugmenterFactory.createInstance()) // 空上下文增强器
                .build();
    }
}

