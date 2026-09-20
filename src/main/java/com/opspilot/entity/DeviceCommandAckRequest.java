package com.opspilot.entity;

import lombok.Data;

@Data
public class DeviceCommandAckRequest {
    private Boolean success;

    private String resultMessage;
}
