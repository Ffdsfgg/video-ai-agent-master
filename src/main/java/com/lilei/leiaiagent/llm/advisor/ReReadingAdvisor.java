package com.lilei.leiaiagent.llm.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义阅读 advisor
 * 在用户输入的基础上，重新阅读问题，增加模型推理能力
 * 论据：<a href="https://arxiv.org/pdf/2309.06275">...</a>
 * 该文介绍了一种名为“重读”(Re2)的技术该技术可以提高大型语言模型的推理能力。Re2技术需要像这样增强输入提示:
 */
@Slf4j
public class ReReadingAdvisor implements BaseAdvisor {

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

        Map<String, Object> advisedUserParams = new HashMap<>(chatClientRequest.context());
        advisedUserParams.put("re2_input_query", chatClientRequest.prompt().getUserMessage());

        return ChatClientRequest.builder()
                .context(advisedUserParams)
                .prompt(Prompt.builder()
                        .content("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                        .build())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}