package com.opspilot.service;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class DataQueryTools {
    private final AiService aiService;
    private final ToolTraceCollector collector;

    public DataQueryTools(AiService aiService, ToolTraceCollector collector) {
        this.aiService = aiService;
        this.collector = collector;
    }

    @Tool
    public String querDatabase(String question) {
        long start = System.currentTimeMillis();
        String result = aiService.chat(question);
        collector.record("default", "queryDatabase", question, result, System.currentTimeMillis() - start);
        return result;
    }
}

