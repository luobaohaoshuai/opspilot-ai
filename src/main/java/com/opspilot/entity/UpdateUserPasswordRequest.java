package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserPasswordRequest {
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须在 8 到 72 个字符之间")
    private String password;
}
