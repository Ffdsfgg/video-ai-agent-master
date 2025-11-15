package com.lilei.leiaiagent.service.impl;

import com.lilei.leiaiagent.rag.retriever.QueryRewriter;
import com.lilei.leiaiagent.service.api.RagService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class RagServiceImpl implements RagService {
    @Resource
    private ChatClient chatClient;

    @Resource
    private QueryRewriter queryRewriter;
//
//    @Resource
//    @Qualifier("inMemoryVectorStore") // Specify the desired bean
//    private VectorStore inMemoryVectorStore;
//
//    @Resource
//    @Qualifier("pgVectorVectorStore") // Specify the desired bean
//    private VectorStore pgVectorVectorStore;

    @Resource
    private Advisor ragCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;


    @Override
    public Flux<String> doChatWithRagQuery(String message, String chatId) {
        // 使用查询改写器对用户输入的消息进行改写
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
        return chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewriteMessage)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答（基于内存的向量存储）
//                .advisors(QuestionAnswerAdvisor.builder(inMemoryVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(ragCloudAdvisor)
                // 应用 RAG 检索增强服务（基于本地 PgVector 向量存储）
                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).searchRequest(SearchRequest.builder().similarityThreshold(0.5).topK(3).build()).build())
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        RagCustomAdvisorFactory.createInstance(
//                                pgVectorVectorStore, "旅游指南"
//                        )
//                )
                .stream()
                .content();
    }

}
