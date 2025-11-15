package com.lilei.leiaiagent.llm.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 违禁词校验顾问 - 检查用户输入是否包含违禁词
 */
@Slf4j
public class ForbiddenWordsAdvisor implements BaseAdvisor {

    // 违禁词列表（实际项目中应从配置中心或数据库加载）
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
            "暴力", "色情", "毒品", "赌博", "诈骗", "敏感词"
    );

    // 预编译违禁词正则表达式
    private static final Pattern FORBIDDEN_PATTERN = buildForbiddenPattern();

    private static Pattern buildForbiddenPattern() {
        StringBuilder patternBuilder = new StringBuilder();
        for (String word : FORBIDDEN_WORDS) {
            if (!patternBuilder.isEmpty()) {
                patternBuilder.append("|");
            }
            patternBuilder.append(Pattern.quote(word));
        }
        return Pattern.compile(patternBuilder.toString());
    }

    private boolean containsForbiddenWords(String text) {
        return FORBIDDEN_PATTERN.matcher(text).find();
    }


    @Override
    public int getOrder() {
        return 0; // 在权限校验之后执行
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();

        if (!StringUtils.hasText(userText)) {
            return chatClientRequest;
        }

        // 检查违禁词
        if (containsForbiddenWords(userText)) {
            throw new IllegalArgumentException("输入包含违禁词，请修改后重试");
        }

        log.info("违禁词校验通过");

        // 添加校验标记到上下文
        chatClientRequest.context().put("forbiddenCheckPassed", true);
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}