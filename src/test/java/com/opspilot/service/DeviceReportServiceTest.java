package com.opspilot.service;

import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.entity.DeviceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceReportServiceTest {

    @Mock
    private DeviceService deviceService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private AlarmService alarmService;
    @Mock
    private DeviceConfigService deviceConfigService;
    @Mock
    private DeviceCommandService deviceCommandService;

    @InjectMocks
    private DeviceReportService deviceReportService;

    @BeforeEach
    void setUp() {
        lenient().when(deviceConfigService.getOrCreateByDeviceId(anyLong())).thenReturn(defaultConfig("35"));
    }

    @Test
    void 设备不存在应返回错误() {
        when(deviceService.getByDeviceCode("NOT-EXIST")).thenReturn(null);

        String result = deviceReportService.handleReport("NOT-EXIST",
                new BigDecimal("25"), new BigDecimal("60"));

        assertEquals("设备不存在: NOT-EXIST", result);
        verify(deviceDataService, never()).addDeviceData(any());
        verify(alarmService, never()).addAlarm(any());
    }

    @Test
    void 正常温度不生成告警() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");
        device.setName("温湿度传感器");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);

        String result = deviceReportService.handleReport("ESP32-001",
                new BigDecimal("25"), new BigDecimal("60"));

        assertEquals("上报成功", result);
        verify(deviceDataService).addDeviceData(any());
        verify(deviceService).updateDevice(any());
        verify(alarmService, never()).addAlarm(any());
    }

    @Test
    void 温度超过35度自动生成告警() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);

        deviceReportService.handleReport("ESP32-001",
                new BigDecimal("38.5"), new BigDecimal("55"));

        // 验证告警被创建且类型正确
        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmService).addAlarm(captor.capture());
        Alarm alarm = captor.getValue();
        assertEquals("温度过高", alarm.getAlarmType());
        assertEquals("38.5℃", alarm.getAlarmValue());
        assertEquals("未处理", alarm.getStatus());
        assertEquals(1L, alarm.getDeviceId());
    }

    @Test
    void 自定义高温阈值生效() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);
        when(deviceConfigService.getOrCreateByDeviceId(1L)).thenReturn(defaultConfig("30"));

        deviceReportService.handleReport("ESP32-001",
                new BigDecimal("31.0"), new BigDecimal("55"));

        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmService).addAlarm(captor.capture());
        assertEquals("温度过高", captor.getValue().getAlarmType());
        assertEquals("31.0℃", captor.getValue().getAlarmValue());
    }

    @Test
    void 温度恰好35度不生成告警() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);

        deviceReportService.handleReport("ESP32-001",
                new BigDecimal("35"), new BigDecimal("60"));

        verify(alarmService, never()).addAlarm(any());
    }

    @Test
    void 上报后设备在线状态应更新为在线() {
        Device device = new Device();
        device.setId(2L);
        device.setDeviceCode("ESP32-002");

        when(deviceService.getByDeviceCode("ESP32-002")).thenReturn(device);

        deviceReportService.handleReport("ESP32-002",
                new BigDecimal("26"), new BigDecimal("50"));

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceService).updateDevice(captor.capture());
        assertTrue(captor.getValue().getOnline());
    }

    @Test
    void 首次高温自动创建开风扇命令() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);

        deviceReportService.handleReport("ESP32-001",
                new BigDecimal("38.5"), new BigDecimal("55"));

        ArgumentCaptor<DeviceCommandRequest> captor = ArgumentCaptor.forClass(DeviceCommandRequest.class);
        verify(deviceCommandService).createCommand(eq(device), captor.capture(), eq("agent"));
        assertEquals("RELAY_ON", captor.getValue().getCommandType());
        assertEquals("FAN", captor.getValue().getPayload());
    }

    @Test
    void 高温告警重复时不开重复风扇命令() {
        Device device = new Device();
        device.setId(1L);
        device.setDeviceCode("ESP32-001");

        when(deviceService.getByDeviceCode("ESP32-001")).thenReturn(device);
        when(alarmService.hasDuplicateAlarm(eq(1L), eq("温度过高"), any(java.time.LocalDateTime.class))).thenReturn(true);

        deviceReportService.handleReport("ESP32-001",
                new BigDecimal("38.5"), new BigDecimal("55"));

        verify(deviceCommandService, never()).createCommand(eq(device), any(), any());
    }

    private DeviceConfig defaultConfig(String temperatureThreshold) {
        DeviceConfig config = new DeviceConfig();
        config.setTemperatureThreshold(new BigDecimal(temperatureThreshold));
        config.setHumidityMinThreshold(new BigDecimal("20"));
        config.setHumidityMaxThreshold(new BigDecimal("80"));
        config.setSampleIntervalSeconds(10);
        config.setInspectionIntervalMinutes(30);
        config.setDisplayMode("NORMAL");
        return config;
    }
}
