package com.opspilot.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final Logger log = LoggerFactory.getLogger(RedisChatMemoryStore.class);

    private final StringRedisTemplate redis;
    private final String redisKeyPrefix;

    public RedisChatMemoryStore(
            StringRedisTemplate redis,
            @Value("${app.agent.memory.redis-key-prefix:agent:chat-memory:}") String redisKeyPrefix
    ) {
        this.redis = redis;
        this.redisKeyPrefix = redisKeyPrefix;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redis.opsForValue().get(key(memoryId));
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return new ArrayList<>(ChatMessageDeserializer.messagesFromJson(json));
        } catch (RuntimeException e) {
            log.warn("Failed to deserialize chat memory from Redis, key={}", key(memoryId), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        redis.opsForValue().set(key(memoryId), ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.delete(key(memoryId));
    }

    private String key(Object memoryId) {
        return redisKeyPrefix + String.valueOf(memoryId);
    }
}
