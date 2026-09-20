package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgentChatRequest {
    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息长度不能超过 2000 个字符")
    private String message;

    @Size(max = 100, message = "会话 ID 长度不能超过 100 个字符")
    private String memoryId = "default";
}
