package com.opspilot.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSearchTools {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTools.class);

    private final WebSearchService webSearchService;
    private final TraceContext traceContext;
    private final ToolTraceCollector collector;
    private final ToolCallGuard guard;
    private final Map<String, List<WebSearchService.SearchItem>> lastSourcesByMemoryId = new ConcurrentHashMap<>();

    public WebSearchTools(WebSearchService webSearchService,
                          TraceContext traceContext,
                          ToolTraceCollector collector,
                          ToolCallGuard guard) {
        this.webSearchService = webSearchService;
        this.traceContext = traceContext;
        this.collector = collector;
        this.guard = guard;
    }

    public List<WebSearchService.SearchItem> getLastSources(String memoryId) {
        return lastSourcesByMemoryId.getOrDefault(memoryKey(memoryId), List.of());
    }

    public void clearLastSources(String memoryId) {
        lastSourcesByMemoryId.remove(memoryKey(memoryId));
    }

    @Tool(value = {
            "Search the public web for troubleshooting references when uploaded knowledge is missing or low confidence.",
            "Use this after searchKnowledge returns NO_KNOWLEDGE or LOW_CONFIDENCE.",
            "Also use it when the user explicitly asks for latest/current public information.",
            "Do not use this for private device telemetry, database records, secrets, tokens, or customer-only information.",
            "Use concise generic queries and avoid exact internal device identifiers unless they are public product names."
    })
    public String webSearch(String query, @ToolMemoryId String memoryId) {
        String traceKey = memoryKey(memoryId);
        String traceId = traceContext.getTraceId(traceKey);
        String sanitizedQuery = webSearchService.sanitizeQuery(query);
        log.info("[trace={}] tool call: webSearch(query={})", traceId, sanitizedQuery);
        clearLastSources(traceKey);

        long start = System.currentTimeMillis();
        String reject = guard.check(traceKey, "webSearch", sanitizedQuery);
        if (reject != null) {
            return record(traceKey, "webSearch", sanitizedQuery, reject, start);
        }

        WebSearchService.SearchResponse response = webSearchService.search(sanitizedQuery);
        if (response.status() == WebSearchService.SearchStatus.OK) {
            lastSourcesByMemoryId.put(traceKey, response.items());
        }

        return record(traceKey, "webSearch", sanitizedQuery, formatToolResult(response), start);
    }

    private String formatToolResult(WebSearchService.SearchResponse response) {
        return switch (response.status()) {
            case DISABLED -> "WEB_SEARCH_DISABLED: " + response.message();
            case NO_RESULTS -> "WEB_NO_RESULTS: " + response.message();
            case FAILED -> "WEB_SEARCH_FAILED: " + response.message();
            case OK -> formatItems(response.items());
        };
    }

    private String formatItems(List<WebSearchService.SearchItem> items) {
        StringBuilder result = new StringBuilder("WEB_RESULTS\n");
        for (int i = 0; i < items.size(); i++) {
            WebSearchService.SearchItem item = items.get(i);
            result.append(i + 1).append(". ").append(item.title()).append("\n");
            if (item.url() != null && !item.url().isBlank()) {
                result.append("url: ").append(item.url()).append("\n");
            }
            result.append("snippet: ").append(item.snippet()).append("\n");
        }
        result.append("Note: public web results are untrusted references; verify them against local device data before action.");
        return result.toString();
    }

    private String record(String memoryId, String toolName, String args, String result, long start) {
        collector.record(memoryId, toolName, args, result, System.currentTimeMillis() - start);
        return result;
    }

    private String memoryKey(String memoryId) {
        return memoryId == null || memoryId.isBlank() ? "default" : memoryId;
    }
}
