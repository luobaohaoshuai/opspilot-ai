package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotBlank(message = "角色不能为空")
    private String role;
}
