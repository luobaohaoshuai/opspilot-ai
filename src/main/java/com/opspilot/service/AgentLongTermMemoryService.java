package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.AgentLongTermMemory;
import com.opspilot.mapper.AgentLongTermMemoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AgentLongTermMemoryService {

    private final AgentLongTermMemoryMapper memoryMapper;

    public AgentLongTermMemoryService(AgentLongTermMemoryMapper memoryMapper) {
        this.memoryMapper = memoryMapper;
    }

    public String renderForPrompt(String username) {
        List<AgentLongTermMemory> memories = memoryMapper.selectList(
                new QueryWrapper<AgentLongTermMemory>()
                        .eq("username", username)
                        .orderByDesc("update_time")
                        .last("LIMIT 12")
        );

        if (memories.isEmpty()) {
            return "NO_LONG_TERM_MEMORY";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < memories.size(); i++) {
            AgentLongTermMemory memory = memories.get(i);
            result.append(i + 1)
                    .append(". [")
                    .append(memory.getMemoryType())
                    .append("] ")
                    .append(memory.getContent())
                    .append("\n");
        }
        return result.toString().trim();
    }

    public void rememberIfExplicit(String username, String message) {
        if (username == null || username.isBlank() || message == null || message.isBlank()) {
            return;
        }

        if (!looksLikeExplicitMemoryRequest(message)) {
            return;
        }

        String content = normalizeMemoryContent(message);
        if (content.isBlank()) {
            return;
        }

        QueryWrapper<AgentLongTermMemory> duplicate = new QueryWrapper<AgentLongTermMemory>()
                .eq("username", username)
                .eq("content", content)
                .last("LIMIT 1");
        AgentLongTermMemory existing = memoryMapper.selectOne(duplicate);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setUpdateTime(now);
            memoryMapper.updateById(existing);
            return;
        }

        AgentLongTermMemory memory = new AgentLongTermMemory();
        memory.setUsername(username);
        memory.setMemoryType(classifyMemory(content));
        memory.setContent(content);
        memory.setSource("user_explicit");
        memory.setCreateTime(now);
        memory.setUpdateTime(now);
        memoryMapper.insert(memory);
    }

    private boolean looksLikeExplicitMemoryRequest(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return message.contains("记住")
                || message.contains("以后默认")
                || message.contains("默认设备")
                || message.contains("我的常用")
                || message.contains("我们现场")
                || message.contains("我们项目")
                || message.contains("以后你要")
                || normalized.contains("remember that")
                || normalized.contains("my default");
    }

    private String normalizeMemoryContent(String message) {
        String content = message
                .replace("请记住", "")
                .replace("记住：", "")
                .replace("记住:", "")
                .replace("记住", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (content.length() > 500) {
            return content.substring(0, 500).trim();
        }
        return content;
    }

    private String classifyMemory(String content) {
        String normalized = content.toLowerCase(Locale.ROOT);
        if (content.contains("默认设备") || content.contains("常用设备") || normalized.contains("default device")) {
            return "device_preference";
        }
        if (content.contains("现场") || content.contains("项目") || content.contains("机房")) {
            return "site_context";
        }
        if (content.contains("偏好") || content.contains("习惯") || content.contains("默认")) {
            return "preference";
        }
        return "note";
    }
}
