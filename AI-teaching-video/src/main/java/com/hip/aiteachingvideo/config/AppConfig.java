package com.hip.aiteachingvideo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 配置AI接口的公开地址和API Key
 */
@Configuration
public class AppConfig {
    @Value("${hip.aliyun.bash-url}")
    private String bashUrl;

    @Value("${hip.aliyun.key}")
    private String key;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(bashUrl)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public String bashUrl() {
        return bashUrl;
    }
}