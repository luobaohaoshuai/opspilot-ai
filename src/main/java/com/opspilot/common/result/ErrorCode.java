package com.opspilot.common.result;

public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMITED(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    DEVICE_NOT_FOUND(10001, "设备不存在"),
    EMPLOYEE_NOT_FOUND(10002, "员工不存在"),
    VALIDATION_ERROR(10003, "参数校验失败"),
    AI_SERVICE_UNAVAILABLE(10004, "AI 服务不可用"),
    KNOWLEDGE_EMPTY(10005, "知识库为空");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
