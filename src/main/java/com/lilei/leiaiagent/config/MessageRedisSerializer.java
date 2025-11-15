package com.lilei.leiaiagent.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

/**
 * 自定义 Redis 序列化器，用于将 Message 对象序列化为 JSON 字符串存储到 Redis 中
 */
public class MessageRedisSerializer implements RedisSerializer<Message> {

    private final ObjectMapper objectMapper;

    public MessageRedisSerializer() {
        this.objectMapper = new ObjectMapper();
        configureObjectMapper();
    }

    private void configureObjectMapper() {
        // 1. 注册自定义模块
        SimpleModule module = new SimpleModule();
        module.addSerializer(Message.class, new MessageSerializer());
        module.addDeserializer(Message.class, new MessageDeserializer());
        objectMapper.registerModule(module);

        // 2. 配置忽略未知字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public byte[] serialize(Message message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public Message deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, Message.class);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    /**
     * 自定义消息序列化器
     */
    private static class MessageSerializer extends JsonSerializer<Message> {
        @Override
        public void serialize(Message message, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {

            gen.writeStartObject();
            gen.writeStringField("messageType", message.getMessageType().getValue().toUpperCase());
            gen.writeStringField("text", message.getText());
            gen.writeEndObject();
        }
    }

    /**
     * 自定义消息反序列化器
     */
    private static class MessageDeserializer extends JsonDeserializer<Message> {
        @Override
        public Message deserialize(JsonParser jp, DeserializationContext ctx)
                throws IOException {

            JsonNode root = jp.readValueAsTree();

            // 安全获取消息类型
            JsonNode typeNode = root.get("messageType");
            if (typeNode == null) {
                throw new JsonParseException(jp, "Missing 'messageType' field");
            }

            String type = typeNode.asText();
            JsonNode textNode = root.get("text");

            if (textNode == null) {
                throw new JsonParseException(jp, "Missing 'text' field");
            }
            String text = textNode.asText();

            // 根据类型创建不同消息对象
            return switch (type) {
                case "USER" -> new UserMessage(text);
                case "ASSISTANT" -> new AssistantMessage(text);
                case "SYSTEM" -> new SystemMessage(text);
                default -> throw new JsonParseException(jp, "Unknown message type: " + type);
            };
        }
    }
}