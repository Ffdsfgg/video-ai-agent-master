package com.hip.aiteachingvideo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器
        template.setValueSerializer(new StringRedisSerializer());
        // 设置 Hash 键的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置 Hash 值的序列化器
        template.setHashValueSerializer(new StringRedisSerializer());
        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}