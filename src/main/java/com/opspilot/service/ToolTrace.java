package com.opspilot.service;

public class ToolTrace {
    private String toolName;
    private String args;
    private String result;
    private long durationMs;

    public ToolTrace() {}

    public ToolTrace(String toolName, String args, String result, long durationMs) {
        this.toolName = toolName;
        this.args = args;
        this.result = result;
        this.durationMs = durationMs;
    }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getArgs() { return args; }
    public void setArgs(String args) { this.args = args; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
