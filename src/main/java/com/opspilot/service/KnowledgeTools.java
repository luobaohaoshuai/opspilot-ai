package com.opspilot.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KnowledgeTools {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeTools.class);

    private final PdfService pdfService;
    private final TraceContext traceContext;
    private final ToolTraceCollector collector;
    private final Map<String, String> lastSourceByMemoryId = new ConcurrentHashMap<>();

    public KnowledgeTools(PdfService pdfService,
                          TraceContext traceContext,
                          ToolTraceCollector collector) {
        this.pdfService = pdfService;
        this.traceContext = traceContext;
        this.collector = collector;
    }

    public String getLastSource(String memoryId) {
        return lastSourceByMemoryId.get(memoryId);
    }

    public void clearLastSource(String memoryId) {
        lastSourceByMemoryId.remove(memoryId);
    }

    @Tool(value = {
            "Search the uploaded PDF or manual knowledge base.",
            "Use this tool for questions about documents, manuals, troubleshooting, fault analysis, operation steps, installation, wiring, maintenance, specifications, or any answer that should come from the uploaded document.",
            "If the tool returns NO_KNOWLEDGE or LOW_CONFIDENCE, the uploaded document does not provide enough evidence and you should not answer from general knowledge."
    })
    public String searchKnowledge(String question, @ToolMemoryId String memoryId) {
        String traceId = traceContext.getTraceId(memoryId);
        log.info("[trace={}] 工具调用: searchKnowledge(question={})", traceId, question);
        clearLastSource(memoryId);
        long start = System.currentTimeMillis();

        PdfService.SearchResult result = pdfService.searchResult(question);
        String r;
        if (result.status() == PdfService.SearchStatus.NO_KNOWLEDGE) {
            log.info("[trace={}] 工具返回: NO_KNOWLEDGE（知识库为空）", traceId);
            r = "NO_KNOWLEDGE: no uploaded PDF/manual knowledge base is available.";
        } else if (result.status() == PdfService.SearchStatus.LOW_CONFIDENCE) {
            log.info("[trace={}] 工具返回: LOW_CONFIDENCE（相似度过低）", traceId);
            r = "LOW_CONFIDENCE: no confident evidence was found in the uploaded document.";
        } else {
            log.info("[trace={}] 工具返回: 命中, score={}", traceId, String.format("%.2f", result.score()));
            lastSourceByMemoryId.put(memoryId, result.source());
            r = "DOCUMENT_MATCH\n"
                    + "source: " + result.source() + "\n"
                    + "score: " + String.format("%.2f", result.score()) + "\n"
                    + "content: " + result.content();
        }
        collector.record(memoryId, "searchKnowledge", question, r, System.currentTimeMillis() - start);
        return r;
    }
}
