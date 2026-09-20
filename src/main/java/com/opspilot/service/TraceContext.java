package com.opspilot.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TraceContext {

    private final Map<String, String> traceMap = new ConcurrentHashMap<>();

    public String startTrace(String memoryId) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        traceMap.put(memoryId, traceId);
        return traceId;
    }

    public String getTraceId(String memoryId) {
        return traceMap.getOrDefault(memoryId, "unknown");
    }

    public void clear(String memoryId) {
        traceMap.remove(memoryId);
    }
}
