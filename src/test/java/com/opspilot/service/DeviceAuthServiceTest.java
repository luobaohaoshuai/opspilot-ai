package com.opspilot.service;

import com.opspilot.entity.Device;
import com.opspilot.common.security.DeviceTokenHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceAuthServiceTest {

    private final DeviceAuthService deviceAuthService = new DeviceAuthService();

    @Test
    void 设备Token正确才允许设备侧访问() {
        Device device = new Device();
        device.setDeviceCode("ESP32-001");
        device.setDeviceToken(DeviceTokenHasher.hash("demo-device-token"));

        assertTrue(deviceAuthService.hasValidDeviceToken(device, "demo-device-token"));
        assertFalse(deviceAuthService.hasValidDeviceToken(device, "wrong-token"));
        assertFalse(deviceAuthService.hasValidDeviceToken(device, null));
    }
}
