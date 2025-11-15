package com.lilei.leiaiagent.llm.chatmemory;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RedisChatMemory 实现了 ChatMemory 接口，用于将聊天消息持久化存储到 Redis 中。 与DataBaseChatMemory之间可选一使用
 *  1、新建一个类命名为RedisChatMemory 实现ChatMemory接口，并添加@Component注解将它注册成一个服务。引入RedisTemplate依赖并实现ChatMemory定义的方法。
 *  2、可以看到这里的RedisTemplate定义的模版类型约束是Message，只用于Message的数据交互，那么就意味着在配置RedisTemplate时就要指定属性类型，其次Message是接口，
 *  在反序列化时需要明确指定其具体实现类，所以我们要根据Message来自定义一个序列化器。
 *  3、在配置类中配置RedisTemplate，指定key的序列化方式为StringRedisSerializer，value的序列化方式为自定义的MessageRedisSerializer。
 *  4、在完成上述步骤后，需要将我们的RedisChatMemory通过Spring AI的Advisor添加到聊天大模型中
 */
@Component
@RequiredArgsConstructor
public class RedisChatMemory implements ChatMemory {
    private static final String REDIS_KEY_PREFIX = "chatmemory:";
    private final RedisTemplate<String, Message> redisTemplate;

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = REDIS_KEY_PREFIX + conversationId;
        // 存储到 Redis
        redisTemplate.opsForList().rightPushAll(key, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = REDIS_KEY_PREFIX + conversationId;
        // 从 Redis 获取最新的 lastN 条消息
        List<Message> serializedMessages = redisTemplate.opsForList().range(key, -10, -1);
        if (serializedMessages != null) {
            return serializedMessages;
        }
        return List.of();
    }


    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }

}