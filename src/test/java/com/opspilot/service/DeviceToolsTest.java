package com.opspilot.service;

import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeviceToolsTest {

    private DeviceService deviceService;
    private DeviceDataService deviceDataService;
    private DeviceTools deviceTools;

    @BeforeEach
    void setUp() {
        deviceService = mock(DeviceService.class);
        deviceDataService = mock(DeviceDataService.class);
        deviceTools = new DeviceTools(
                deviceService,
                deviceDataService,
                mock(AlarmService.class),
                mock(DeviceConfigService.class),
                mock(DeviceCommandService.class),
                new ToolCallGuard(),
                new ToolTraceCollector()
        );
    }

    @Test
    void 设备日志应返回趋势和统计信息() {
        Device device = device(1L, "ESP32-001", "3号机房");
        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);
        when(deviceDataService.listRecentByDeviceId(1L, 6)).thenReturn(List.of(
                data(1L, "36.5", "60"),
                data(1L, "35.0", "58"),
                data(1L, "33.5", "55")
        ));

        String result = deviceTools.getDeviceLogs("ESP32-001", 6, "mem");

        assertTrue(result.contains("样本 3 条"));
        assertTrue(result.contains("最高温度 36.5℃"));
        assertTrue(result.contains("持续上升"));
    }

    @Test
    void 多设备对比应提示明显温差() {
        Device device1 = device(1L, "ESP32-001", "A区");
        Device device2 = device(2L, "ESP32-002", "B区");
        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device1);
        when(deviceService.getByDeviceCode("ESP32-002")).thenReturn(device2);
        when(deviceDataService.getLatestData(1L)).thenReturn(data(1L, "28.0", "50"));
        when(deviceDataService.getLatestData(2L)).thenReturn(data(2L, "35.5", "52"));

        String result = deviceTools.compareDevices("ESP32-001,ESP32-002", "mem");

        assertTrue(result.contains("ESP32-001"));
        assertTrue(result.contains("ESP32-002"));
        assertTrue(result.contains("温差 7.5℃"));
    }

    private Device device(Long id, String code, String location) {
        Device device = new Device();
        device.setId(id);
        device.setDeviceCode(code);
        device.setName(code);
        device.setLocation(location);
        device.setOnline(true);
        return device;
    }

    private DeviceData data(Long deviceId, String temperature, String humidity) {
        DeviceData data = new DeviceData();
        data.setDeviceId(deviceId);
        data.setTemperature(new BigDecimal(temperature));
        data.setHumidity(new BigDecimal(humidity));
        data.setReportTime(LocalDateTime.now());
        return data;
    }
}
