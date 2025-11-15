package com.lilei.leiaiagent.rag.vectorstore;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * 配置 PgVectorStore (使用本地的 PostgreSQL 数据库作为向量存储)
 * 默认情况下可以不用配置，使用时直接注解引入即可，但是，这种方式不适合特殊情况! 因为 Vectorstore 依赖 EmbeddingModel 对象，
 * 如果同时引入了 Ollama 或 阿里云 Dashscope 等多个多个模型，有两个 EmbeddingModel的 Bean话，Spring 不知道注入哪个，就会报错误
 * 下面方式更灵活
 */
@Configuration
public class PgVectorVectorStoreConfig {
    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 必填项，向量维度
                .distanceType(COSINE_DISTANCE)       // 余弦距离
                .indexType(HNSW)                     // HNSW 索引类型
                .initializeSchema(true)              // 是否初始化 schema
                .schemaName("public")                // PostgreSQL schema 名称
                .vectorTableName("vector_store")     // 向量表名称
                .maxDocumentBatchSize(10000)         // 批量文档处理的最大数量
                .build();
    }
}
