package com.lilei.leiaiagent.rag.vectorstore;

import com.lilei.leiaiagent.rag.document.KeywordEnricher;
import com.lilei.leiaiagent.rag.document.MarkdownReader;
import com.lilei.leiaiagent.rag.document.SelfTokenTextSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 配置 InMemoryVectorStore (使用内存作为向量存储)
 * 使用 Spring Al内置的、基于内存读写的向量数据库 SimpleVectorStore 来保存文档。
 * SimpleVectorStore 实现了 VectorStore 接口，而 VectorStore 接口集成了 DocumentWriter，所以具备文档写入能力。
 */
@Configuration
public class InMemoryVectorStoreConfig {

    @Resource
    private MarkdownReader markdownReader;

    @Resource
    private SelfTokenTextSplitter selfTokenTextSplitter;

    @Resource
    private KeywordEnricher keywordEnricher;

    /**
     * 使用 Spring Al 内置的 SimpleVectorStore 来保存文档。
     * 可以不用配置使用时直接注解引入，但是，这种方式不适合特殊情况! 因为 Vectorstore 依赖 EmbeddingModel 对象，
     */
//    @Bean
    VectorStore inMemoryVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 创建一个内存向量存储实例
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 加载文档
        List<Document> documentList = markdownReader.loadMarkdowns();
        // 自主切分文档
//        List<Document> splitDocuments = selfTokenTextSplitter.splitCustomized(documentList);
        // 自动补充关键词元信息
        List<Document> enrichedDocuments = keywordEnricher.enrichDocuments(documentList);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
