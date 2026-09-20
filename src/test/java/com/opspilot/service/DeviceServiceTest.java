package com.opspilot.service;

import com.opspilot.common.security.DeviceTokenHasher;
import com.opspilot.entity.Device;
import com.opspilot.mapper.DeviceMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DeviceServiceTest {

    @Test
    void 注册设备只返回一次原始Token并在数据库保存摘要() {
        DeviceMapper mapper = mock(DeviceMapper.class);
        DeviceService service = new DeviceService(mapper);
        Device device = new Device();
        device.setDeviceCode("ESP32-009");
        device.setDeviceToken("raw-device-token");

        String returnedToken = service.addDevice(device);

        assertEquals("raw-device-token", returnedToken);
        assertNotEquals(returnedToken, device.getDeviceToken());
        assertEquals(DeviceTokenHasher.hash(returnedToken), device.getDeviceToken());
        verify(mapper).insert(device);
    }
}
