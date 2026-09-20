package com.opspilot.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolTraceCollector {

    private final Map<String, List<ToolTrace>> traceMap = new ConcurrentHashMap<>();

    public void record(String memoryId, String toolName, String args, String result, long durationMs) {
        // 截断结果，避免日志过长
        String shortResult = result != null && result.length() > 300
                ? result.substring(0, 300) + "..."
                : result;
        traceMap.computeIfAbsent(memoryId, k -> new ArrayList<>())
                .add(new ToolTrace(toolName, args, shortResult, durationMs));
    }

    public List<ToolTrace> collect(String memoryId) {
        List<ToolTrace> traces = traceMap.getOrDefault(memoryId, List.of());
        return new ArrayList<>(traces);
    }

    public void clear(String memoryId) {
        traceMap.remove(memoryId);
    }
}
