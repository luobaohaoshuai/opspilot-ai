package com.opspilot.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DeviceSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void 设备Token允许写入但禁止出现在接口响应中() throws Exception {
        Device device = mapper.readValue(
                "{\"deviceCode\":\"ESP32-001\",\"name\":\"传感器\",\"deviceToken\":\"secret-token\"}",
                Device.class
        );

        assertEquals("secret-token", device.getDeviceToken());
        assertFalse(mapper.writeValueAsString(device).contains("secret-token"));
        assertFalse(mapper.writeValueAsString(device).contains("deviceToken"));
    }
}
