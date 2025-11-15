package com.lilei.leiaiagent.config;
 
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * * 用于配置 RedisTemplate，指定 key 和 value 的序列化方式
 * * 这里使用了自定义的 MessageRedisSerializer 来序列化 Message 对象
 */
@Configuration
public class RedisConfig {
 
    @Bean
    public RedisTemplate<String, Message> messageRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Message> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
 
        // 使用String序列化器作为key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义的Message序列化器作为value的序列化方式
        template.setValueSerializer(new MessageRedisSerializer());
 
        // 设置hash类型的key和value序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new MessageRedisSerializer());
 
        template.afterPropertiesSet();
        return template;
    }
}