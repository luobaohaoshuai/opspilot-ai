package com.opspilot.service;

import java.util.List;
import java.util.Map;

public class AgentResponse {
    private String answer;
    private List<ToolTrace> trace;
    private List<Map<String, Object>> sources;

    public AgentResponse() {}

    public AgentResponse(String answer, List<ToolTrace> trace, List<Map<String, Object>> sources) {
        this.answer = answer;
        this.trace = trace;
        this.sources = sources;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<ToolTrace> getTrace() { return trace; }
    public void setTrace(List<ToolTrace> trace) { this.trace = trace; }
    public List<Map<String, Object>> getSources() { return sources; }
    public void setSources(List<Map<String, Object>> sources) { this.sources = sources; }
}
