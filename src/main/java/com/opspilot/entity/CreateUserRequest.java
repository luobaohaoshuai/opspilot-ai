package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3 到 50 个字符之间")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "用户名只能包含字母、数字、点、下划线和连字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 72, message = "密码长度必须在 8 到 72 个字符之间")
    private String password;

    @NotBlank(message = "角色不能为空")
    private String role;
}
