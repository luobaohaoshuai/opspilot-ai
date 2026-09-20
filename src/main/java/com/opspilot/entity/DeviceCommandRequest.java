package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceCommandRequest {
    @NotBlank(message = "命令类型不能为空")
    private String commandType;

    private String payload;
}
