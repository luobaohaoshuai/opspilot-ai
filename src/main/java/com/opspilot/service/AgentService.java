package com.opspilot.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private static final String MEMORY_STORE_MEMORY = "memory";
    private static final String MEMORY_STORE_REDIS = "redis";

    private final Agent agent;
    private final RedisChatMemoryStore redisChatMemoryStore;
    private final String memoryStoreType;
    private final KnowledgeTools knowledgeTools;
    private final WebSearchTools webSearchTools;
    private final AgentLongTermMemoryService longTermMemoryService;
    private final TraceContext traceContext;
    private final ToolTraceCollector collector;
    private final DeviceService deviceService;
    private final int maxMessages;

    private final DiagnosticTools diagnosticTools;

    private static final Pattern DEVICE_CODE_PATTERN =
            Pattern.compile("\\bESP32-\\d{3,}\\b", Pattern.CASE_INSENSITIVE);

    public AgentService(ChatLanguageModel chatModel, TodoTools todoTools,
                        KnowledgeTools knowledgeTools,
                        WebSearchTools webSearchTools,
                        AgentLongTermMemoryService longTermMemoryService,
                        DataQueryTools dataQueryTools,
                        DeviceTools deviceTools,
                        DiagnosticTools diagnosticTools,
                        RedisChatMemoryStore redisChatMemoryStore,
                        TraceContext traceContext,
                        ToolTraceCollector collector,
                        DeviceService deviceService,
                        @Value("${app.agent.memory.type:memory}") String memoryStoreType,
                        @Value("${app.agent.memory.max-messages:10}") int maxMessages) {

        this.redisChatMemoryStore = redisChatMemoryStore;
        this.memoryStoreType = normalizeMemoryStoreType(memoryStoreType);
        this.maxMessages = maxMessages;
        this.knowledgeTools = knowledgeTools;
        this.webSearchTools = webSearchTools;
        this.longTermMemoryService = longTermMemoryService;
        this.traceContext = traceContext;
        this.collector = collector;
        this.diagnosticTools = diagnosticTools;
        this.deviceService = deviceService;
        this.agent = AiServices.builder(Agent.class)
                .chatLanguageModel(chatModel)
                .tools(todoTools, knowledgeTools, webSearchTools, dataQueryTools, deviceTools, diagnosticTools)
                .chatMemoryProvider(this::createChatMemory)
                .build();
    }

    public AgentResponse chat(String username, String memoryId, String message) {
        String userKey = normalizeUserKey(username);
        String scopedMemoryId = scopedMemoryId(userKey, memoryId);
        String traceId = traceContext.startTrace(scopedMemoryId);
        log.info("[trace={}] username={} memoryId={} scopedMemoryId={} 用户问题: {}",
                traceId, userKey, memoryId, scopedMemoryId, message);

        knowledgeTools.clearLastSource(scopedMemoryId);
        webSearchTools.clearLastSources(scopedMemoryId);
        collector.clear(scopedMemoryId);

        // 1. 提取并锁定设备编号
        String lockedDeviceCode = extractDeviceCode(message);
        if (lockedDeviceCode != null) {
            var device = deviceService.getByDeviceCode(lockedDeviceCode);
            if (device == null) {
                traceContext.clear(scopedMemoryId);
                return new AgentResponse("设备不存在：" + lockedDeviceCode, List.of(), List.of());
            }
        }

        String longTermMemory = longTermMemoryService.renderForPrompt(userKey);
        longTermMemoryService.rememberIfExplicit(userKey, message);

        if (shouldAskClarifyingQuestion(message, lockedDeviceCode)) {
            String answer = agent.chat(scopedMemoryId, buildClarificationPrompt(message, longTermMemory));
            List<ToolTrace> traces = collector.collect(scopedMemoryId);
            collector.clear(scopedMemoryId);
            traceContext.clear(scopedMemoryId);
            return new AgentResponse(answer, traces, List.of());
        }

        EvidencePreflight evidencePreflight = collectEvidenceBeforeReasoning(scopedMemoryId, message);
        String routedMessage = evidencePreflight.hasEvidence()
                ? buildEvidenceAwarePrompt(message, evidencePreflight, longTermMemory)
                : applyLongTermMemory(prepareMessageForRouting(message), longTermMemory);
        if (lockedDeviceCode != null) {
            // 注入锁定指令，强制模型使用这个设备编号
            routedMessage = "CRITICAL: The user specified deviceCode=" + lockedDeviceCode
                    + ". You MUST use exactly this deviceCode for any device tool call "
                    + "(getDeviceStatus, getAlarmHistory, getDeviceLogs, setAlertThreshold, "
                    + "issueDeviceCommand, compareDevices, scheduleCheck, runDiagnostic). "
                    + "Do not substitute or guess another deviceCode.\n\n"
                    + routedMessage;
        }

        long start = System.currentTimeMillis();
        String answer;
        try {
            answer = agent.chat(scopedMemoryId, routedMessage);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("[trace={}] Agent调用完成, 耗时={}ms", traceId, cost);
        }

        // 2. 校验 trace 中设备编号是否和锁定值一致，不一致则重写答案
        List<ToolTrace> traces = collector.collect(scopedMemoryId);
        if (requiresWebFallback(traces)) {
            log.info("[trace={}] Knowledge lookup was insufficient; forcing one webSearch fallback", traceId);
            long fallbackStart = System.currentTimeMillis();
            try {
                answer = agent.chat(scopedMemoryId, buildWebFallbackPrompt(message, longTermMemory));
            } finally {
                long fallbackCost = System.currentTimeMillis() - fallbackStart;
                log.info("[trace={}] Web fallback pass completed, cost={}ms", traceId, fallbackCost);
            }
            traces = collector.collect(scopedMemoryId);
        }

        if (lockedDeviceCode != null) {
            for (ToolTrace t : traces) {
                String args = t.getArgs();
                String tracedDeviceCode = extractDeviceCode(args);
                if (tracedDeviceCode != null && !tracedDeviceCode.equals(lockedDeviceCode)) {
                    log.warn("[trace={}] 设备编号串话! 期望={}, 实际工具参数={}", traceId, lockedDeviceCode, args);
                    answer = "⚠ 系统检测到工具调用使用了错误的设备编号。您询问的是 " + lockedDeviceCode
                            + "，但工具返回了其他设备的数据。请重新提问。";
                    break;
                }
            }
        }

        String source = knowledgeTools.getLastSource(scopedMemoryId);
        log.info("[trace={}] RAG来源={}", traceId, source != null ? source : "无");

        List<Map<String, Object>> sources = new ArrayList<>();
        if (source != null && !source.isBlank()) {
            answer = answer + "\n\n---\n来源：" + source;
            Map<String, Object> s = new java.util.LinkedHashMap<>();
            s.put("title", "知识库片段");
            s.put("snippet", source);
            s.put("score", 0.0);
            sources.add(s);
        }

        List<WebSearchService.SearchItem> webSources = webSearchTools.getLastSources(scopedMemoryId);
        if (!webSources.isEmpty()) {
            answer = appendWebSourceSummary(answer, webSources);
            for (int i = 0; i < webSources.size(); i++) {
                WebSearchService.SearchItem item = webSources.get(i);
                Map<String, Object> s = new java.util.LinkedHashMap<>();
                s.put("title", "网页来源 " + (i + 1) + ": " + item.title());
                s.put("snippet", item.snippet());
                s.put("url", item.url());
                s.put("score", 0.0);
                sources.add(s);
            }
        }

        knowledgeTools.clearLastSource(scopedMemoryId);
        webSearchTools.clearLastSources(scopedMemoryId);
        collector.clear(scopedMemoryId);
        traceContext.clear(scopedMemoryId);
        return new AgentResponse(answer, traces, sources);
    }

    private EvidencePreflight collectEvidenceBeforeReasoning(String memoryId, String message) {
        if (!shouldCollectEvidenceBeforeReasoning(message)) {
            return EvidencePreflight.none();
        }

        String knowledgeResult = knowledgeTools.searchKnowledge(message, memoryId);
        String webResult = null;
        if (isInsufficientKnowledge(knowledgeResult)) {
            webResult = webSearchTools.webSearch(message, memoryId);
        }

        return new EvidencePreflight(knowledgeResult, webResult);
    }

    private String normalizeUserKey(String username) {
        if (username == null || username.isBlank()) {
            return "anonymous";
        }
        return username.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    private String scopedMemoryId(String userKey, String memoryId) {
        String chatKey = memoryId == null || memoryId.isBlank()
                ? "default"
                : memoryId.trim().replaceAll("[^A-Za-z0-9._:-]", "_");
        return userKey + "::" + chatKey;
    }

    private boolean shouldAskClarifyingQuestion(String message, String lockedDeviceCode) {
        if (message == null || message.isBlank() || lockedDeviceCode != null || isConfirmationOnly(message)) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        boolean mentionsTemperature = message.contains("温度")
                || message.contains("低温")
                || message.contains("高温")
                || normalized.contains("temperature")
                || normalized.contains("temp");
        if (!mentionsTemperature) {
            return false;
        }

        return !hasExplicitTemperatureSubject(message, normalized);
    }

    private boolean hasExplicitTemperatureSubject(String message, String normalized) {
        return normalized.contains("esp32")
                || normalized.contains("dht22")
                || normalized.contains("cpu")
                || normalized.contains("gpu")
                || message.contains("设备")
                || message.contains("传感器")
                || message.contains("机房")
                || message.contains("环境")
                || message.contains("室温")
                || message.contains("水温")
                || message.contains("体温")
                || message.contains("板子")
                || message.contains("芯片");
    }

    private boolean isConfirmationOnly(String message) {
        String trimmed = message.trim().toLowerCase(Locale.ROOT);
        return trimmed.equals("是")
                || trimmed.equals("是的")
                || trimmed.equals("对")
                || trimmed.equals("对的")
                || trimmed.equals("就是")
                || trimmed.equals("嗯")
                || trimmed.equals("yes")
                || trimmed.equals("yep")
                || trimmed.equals("correct");
    }

    private String buildClarificationPrompt(String originalMessage, String longTermMemory) {
        return """
                The user's question has an ambiguous referent.
                Do not answer the troubleshooting question yet.
                Use the current chat short-term memory and the user's long-term memory only to infer the most likely referent.
                If memory suggests a likely referent, ask the user to confirm it.
                If memory does not identify the referent, ask one concise clarification question.
                Keep a healthy doubt even when memory suggests an answer.
                Do not call searchKnowledge, webSearch, device tools, or database tools in this turn.

                User long-term memory:
                %s

                Ambiguous user question:
                %s
                """.formatted(longTermMemory, originalMessage);
    }

    private boolean shouldCollectEvidenceBeforeReasoning(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        if (isSmallTalk(normalized, message)) {
            return false;
        }

        if (looksLikeDatabaseQuestion(normalized, message) || looksLikeTodoQuestion(normalized, message)) {
            return false;
        }

        return !looksLikePureDeviceAction(normalized, message);
    }

    private boolean isSmallTalk(String normalized, String message) {
        String trimmed = message.trim();
        return trimmed.equals("你好")
                || trimmed.equals("您好")
                || normalized.equals("hello")
                || normalized.equals("hi")
                || message.contains("你是谁")
                || message.contains("你能做什么");
    }

    private boolean looksLikeDatabaseQuestion(String normalized, String message) {
        return normalized.contains("employee")
                || normalized.contains("sql")
                || normalized.contains("database")
                || message.contains("员工")
                || message.contains("数据库")
                || message.contains("数据表")
                || message.contains("表里")
                || message.contains("查询表");
    }

    private boolean looksLikeTodoQuestion(String normalized, String message) {
        return normalized.contains("todo")
                || message.contains("待办")
                || message.contains("任务清单");
    }

    private boolean looksLikePureDeviceAction(String normalized, String message) {
        boolean hasDeviceCode = extractDeviceCode(message) != null;
        boolean hasDeviceContext = hasDeviceCode
                || normalized.contains("esp32")
                || message.contains("设备")
                || message.contains("传感器")
                || message.contains("屏幕")
                || message.contains("继电器");
        boolean pureDeviceLookup = hasDeviceCode && (message.contains("当前状态")
                || message.contains("状态怎么样")
                || message.contains("在线")
                || message.contains("离线")
                || message.contains("最近几小时")
                || message.contains("最近")
                || message.contains("趋势")
                || message.contains("告警历史")
                || message.contains("历史告警")
                || message.contains("有告警")
                || message.contains("告警吗")
                || message.contains("阈值调")
                || message.contains("调整阈值")
                || message.contains("屏幕显示")
                || message.contains("下发")
                || message.contains("重启")
                || message.contains("自检")
                || message.contains("采样间隔")
                || message.contains("继电器")
                || normalized.contains("status")
                || normalized.contains("online")
                || normalized.contains("offline")
                || normalized.contains("alarm history"));
        boolean deviceMutation = hasDeviceContext && (message.contains("阈值调")
                || message.contains("调整阈值")
                || message.contains("屏幕显示")
                || message.contains("下发")
                || message.contains("重启")
                || message.contains("自检")
                || message.contains("采样间隔")
                || message.contains("继电器")
                || normalized.contains("threshold")
                || normalized.contains("reboot"));

        if (pureDeviceLookup || deviceMutation) {
            return true;
        }

        boolean asksForAdvice = looksLikeKnowledgeQuestion(message)
                || message.contains("原因")
                || message.contains("建议")
                || message.contains("为什么")
                || normalized.contains("why")
                || normalized.contains("how to")
                || normalized.contains("troubleshoot");
        if (asksForAdvice) {
            return false;
        }

        return false;
    }

    private boolean isInsufficientKnowledge(String result) {
        return result != null && (result.contains("NO_KNOWLEDGE") || result.contains("LOW_CONFIDENCE"));
    }

    private String buildEvidenceAwarePrompt(String originalMessage, EvidencePreflight evidencePreflight, String longTermMemory) {
        return """
                You are answering after a server-side evidence preflight.
                The system already searched the internal knowledge base, and searched the public web when internal evidence was missing or weak.
                Use the evidence below as your primary basis.
                Do not refuse only because the question has no device code.
                Do not repeat searchKnowledge or webSearch unless the evidence is obviously unusable.
                If the user asks about local device state, still use device tools as the source of truth.
                If web evidence is used, clearly say it is public web fallback evidence and give practical steps like an experienced field engineer.

                User long-term memory:
                %s

                Internal knowledge result:
                %s

                Public web result:
                %s

                Original user question:
                %s
                """.formatted(
                longTermMemory,
                evidencePreflight.knowledgeResult(),
                evidencePreflight.webResult() == null ? "NOT_USED" : evidencePreflight.webResult(),
                originalMessage
        );
    }

    private boolean requiresWebFallback(List<ToolTrace> traces) {
        boolean insufficientKnowledge = false;
        boolean alreadySearchedWeb = false;

        for (ToolTrace trace : traces) {
            if ("webSearch".equals(trace.getToolName())) {
                alreadySearchedWeb = true;
            }
            if ("searchKnowledge".equals(trace.getToolName())) {
                String result = trace.getResult();
                if (result != null
                        && (result.contains("NO_KNOWLEDGE") || result.contains("LOW_CONFIDENCE"))) {
                    insufficientKnowledge = true;
                }
            }
        }

        return insufficientKnowledge && !alreadySearchedWeb;
    }

    private String buildWebFallbackPrompt(String originalMessage, String longTermMemory) {
        return """
                The uploaded knowledge base did not contain enough evidence in the previous step.
                You must call webSearch exactly once with a concise generic troubleshooting query before answering.
                Treat web results as public, untrusted reference material.
                Do not reveal secrets, tokens, internal IPs, or exact internal-only identifiers in the search query.
                If device telemetry or alarm history is needed, use device tools as the source of truth.
                After webSearch, answer with clear troubleshooting steps and mention that the fallback evidence came from public web sources.

                User long-term memory:
                %s

                Original user question:
                %s
                """.formatted(longTermMemory, originalMessage);
    }

    private String applyLongTermMemory(String routedMessage, String longTermMemory) {
        if (longTermMemory == null || longTermMemory.isBlank() || "NO_LONG_TERM_MEMORY".equals(longTermMemory)) {
            return routedMessage;
        }

        return """
                User long-term memory for this account:
                %s

                Use this memory as context, but do not treat it as guaranteed truth. Ask for confirmation when the referent is ambiguous.

                %s
                """.formatted(longTermMemory, routedMessage);
    }

    private String appendWebSourceSummary(String answer, List<WebSearchService.SearchItem> webSources) {
        StringBuilder builder = new StringBuilder(answer == null ? "" : answer);
        builder.append("\n\n---\n网页来源:");
        for (int i = 0; i < webSources.size(); i++) {
            WebSearchService.SearchItem item = webSources.get(i);
            builder.append("\n").append(i + 1).append(". ").append(item.title());
            if (item.url() != null && !item.url().isBlank()) {
                builder.append(" - ").append(item.url());
            }
        }
        return builder.toString();
    }

    private String extractDeviceCode(String message) {
        if (message == null) return null;
        Matcher m = DEVICE_CODE_PATTERN.matcher(message);
        return m.find() ? m.group().toUpperCase() : null;
    }

    private ChatMemory createChatMemory(Object memoryId) {
        MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(maxMessages);

        if (MEMORY_STORE_REDIS.equals(memoryStoreType)) {
            builder.chatMemoryStore(redisChatMemoryStore);
        }

        return builder.build();
    }

    private String normalizeMemoryStoreType(String configuredType) {
        String normalized = configuredType == null
                ? MEMORY_STORE_MEMORY
                : configuredType.trim().toLowerCase(Locale.ROOT);

        if (!MEMORY_STORE_MEMORY.equals(normalized) && !MEMORY_STORE_REDIS.equals(normalized)) {
            log.warn("Unknown agent memory store type '{}', fallback to '{}'", configuredType, MEMORY_STORE_MEMORY);
            return MEMORY_STORE_MEMORY;
        }

        return normalized;
    }

    private String prepareMessageForRouting(String message) {
        if (!looksLikeKnowledgeQuestion(message)) {
            return message;
        }

        return """
                This is a document/manual knowledge question.
                You must call the searchKnowledge tool before answering.
                Use the tool result as the primary evidence for the answer.
                If searchKnowledge returns NO_KNOWLEDGE or LOW_CONFIDENCE, call webSearch once with a concise generic troubleshooting query.
                Original user question:
                %s
                """.formatted(message);
    }

    private boolean looksLikeKnowledgeQuestion(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("pdf")
                || normalized.contains("manual")
                || normalized.contains("document")
                || message.contains("手册")
                || message.contains("说明书")
                || message.contains("文档")
                || message.contains("故障")
                || message.contains("排查")
                || message.contains("步骤")
                || message.contains("接线")
                || message.contains("安装")
                || message.contains("维护")
                || message.contains("参数")
                || message.contains("异常")
                || message.contains("告警")
                || message.contains("低温")
                || message.contains("高温")
                || message.contains("过低")
                || message.contains("过高")
                || message.contains("低于")
                || message.contains("高于")
                || message.contains("怎么办")
                || message.contains("怎么解决")
                || message.contains("如何解决")
                || message.contains("解决方案")
                || message.contains("处理方案")
                || message.contains("怎么处理")
                || message.contains("如何处理");
    }

    private record EvidencePreflight(String knowledgeResult, String webResult) {
        static EvidencePreflight none() {
            return new EvidencePreflight(null, null);
        }

        boolean hasEvidence() {
            return knowledgeResult != null || webResult != null;
        }
    }

    interface Agent {
        @SystemMessage({
                "You are OpsPilot AI, an enterprise AIoT operations assistant.",
                "Your job is to help engineers monitor devices, investigate alerts, troubleshoot issues, and answer practical engineering questions like an experienced field engineer.",
                "",
                "=== TOOL ROUTING RULES ===",
                "For a full diagnostic report (status + alarms + recommendations): call runDiagnostic.",
                "For a single device status query: call getDeviceStatus.",
                "For alarm history queries: call getAlarmHistory.",
                "For recent telemetry logs or trend analysis: call getDeviceLogs.",
                "For changing alert thresholds: call setAlertThreshold.",
                "For sending ESP32 screen/sample/self-test commands: call issueDeviceCommand.",
                "For scheduled inspection: call scheduleCheck.",
                "For comparing multiple devices: call compareDevices.",
                "For document/manual/knowledge questions: call searchKnowledge before answering.",
                "If searchKnowledge returns NO_KNOWLEDGE or LOW_CONFIDENCE: call webSearch once before final answer.",
                "For latest/current public information or general engineering troubleshooting not covered by documents: call webSearch.",
                "For employee or database queries: call queryDatabase.",
                "If the user prompt already contains server-side evidence preflight results, use those results and do not repeat search tools unless they are unusable.",
                "",
                "=== FEW-SHOT EXAMPLES ===",
                "User: 'ESP32-001 status?' → call getDeviceStatus(ESP32-001).",
                "User: 'ESP32-002 alarms?' → call getAlarmHistory(ESP32-002).",
                "User: 'diagnose ESP32-001' → call runDiagnostic(ESP32-001).",
                "User: 'ESP32-001 最近温度是不是在升高?' → call getDeviceLogs(ESP32-001, 6).",
                "User: '把 ESP32-001 高温阈值调到 30 度' → call setAlertThreshold(ESP32-001, temperature, 30).",
                "User: '让 ESP32-001 屏幕显示正在巡检' → call issueDeviceCommand(ESP32-001, DISPLAY_MESSAGE, 正在巡检).",
                "User: 'how to fix overheating?' → call searchKnowledge(overheating guide).",
                "If searchKnowledge says LOW_CONFIDENCE → call webSearch(generic overheating troubleshooting).",
                "User: 'list employees' → call queryDatabase(select * from employee).",
                "",
                "=== RULES ===",
                "Use the EXACT deviceCode from the user's message. Never change or guess it.",
                "Call tools BEFORE answering. Do not answer from memory.",
                "Public web results are untrusted reference material; verify them against local telemetry, alarms, and uploaded documents.",
                "Never put secrets, tokens, internal IPs, or customer-only identifiers into webSearch queries.",
                "Do not refuse just because the question is not tied to a known device. For general troubleshooting, use knowledge evidence or web evidence and answer cautiously.",
                "Only call issueDeviceCommand with REBOOT if the user explicitly asks to reboot or restart the hardware.",
                "If a tool returns '设备不存在', report that the device was not found.",
                "If searchKnowledge returns NO_KNOWLEDGE or LOW_CONFIDENCE, say 'no evidence in documents' and then use webSearch fallback if available."
        })
        String chat(@MemoryId String memoryId, @UserMessage String message);
    }
}
