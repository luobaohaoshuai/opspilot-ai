package com.opspilot.service;


import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class ToolCallGuard {
    private final Map<String, String> lastCall = new ConcurrentHashMap<>();
    private final Map<String, Integer> repeatCount = new ConcurrentHashMap<>();

    public String check(String memoryId, String toolName, String args) {
        String key = memoryId + ":" + toolName;
        String last = lastCall.get(key);

        if (args.equals(last)) {

            int count = repeatCount.merge(key, 1, Integer::sum);
            if (count >= 3) {
                return "REJECT:重复调用" + toolName;
            }
        } else {

            lastCall.put(key, args);
            repeatCount.remove(key);
        }
        return null;
    }

}


