package com.opspilot.service;

import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import com.opspilot.entity.DeviceCommandRequest;
import com.opspilot.entity.DeviceConfig;
import com.opspilot.entity.DeviceData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DeviceReportService {
    private final DeviceService deviceService;
    private final DeviceDataService deviceDataService;
    private final AlarmService alarmService;
    private final DeviceConfigService deviceConfigService;
    private final DeviceCommandService deviceCommandService;

    public DeviceReportService(DeviceService deviceService,
                               DeviceDataService deviceDataService,
                               AlarmService alarmService,
                               DeviceConfigService deviceConfigService,
                               DeviceCommandService deviceCommandService) {
        this.deviceService = deviceService;
        this.deviceDataService = deviceDataService;
        this.alarmService = alarmService;
        this.deviceConfigService = deviceConfigService;
        this.deviceCommandService = deviceCommandService;
    }

    @Transactional
    public String handleReport(String deviceCode,
                               BigDecimal temperature,
                               BigDecimal humidity) {
        return handleReport(deviceCode, temperature, humidity, null, null, null);
    }

    @Transactional
    public String handleReport(String deviceCode,
                               BigDecimal temperature,
                               BigDecimal humidity,
                               Integer rssi,
                               Long uptimeSeconds,
                               String firmwareVersion) {
        // 步骤 1：根据 deviceCode 查设备
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            return "设备不存在: " + deviceCode;
        }

        return handleReport(device, temperature, humidity, rssi, uptimeSeconds, firmwareVersion);
    }

    @Transactional
    public String handleReport(Device device,
                               BigDecimal temperature,
                               BigDecimal humidity,
                               Integer rssi,
                               Long uptimeSeconds,
                               String firmwareVersion) {
        DeviceConfig config = deviceConfigService.getOrCreateByDeviceId(device.getId());

        // 步骤 2：存上报数据
        DeviceData data = new DeviceData();
        data.setDeviceId(device.getId());        // 拿到数据库 id
        data.setTemperature(temperature);         // Controller 传进来的
        data.setHumidity(humidity);
        data.setRssi(rssi);
        data.setUptimeSeconds(uptimeSeconds);
        data.setFirmwareVersion(firmwareVersion);
        data.setReportTime(LocalDateTime.now()); // 当前时间
        deviceDataService.addDeviceData(data);

        // 步骤 3：更新在线状态
        device.setOnline(true);
        device.setLastOnlineTime(LocalDateTime.now());
        deviceService.updateDevice(device);


        // 步骤 4：超过动态阈值时自动告警 + 自动下发继电器命令
        if (temperature.compareTo(config.getTemperatureThreshold()) > 0) {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            String highType = "温度过高";
            if (alarmService.hasDuplicateAlarm(device.getId(), highType, fiveMinutesAgo)) {
                alarmService.escalateAlarm(device.getId(), highType, fiveMinutesAgo, temperature + "℃");
            } else {
                Alarm alarm = new Alarm();
                alarm.setDeviceId(device.getId());
                alarm.setAlarmType(highType);
                alarm.setAlarmValue(temperature.toString() + "℃");
                alarm.setStatus("未处理");
                alarm.setCreateTime(LocalDateTime.now());
                alarmService.addAlarm(alarm);

                // 首次高温告警 → 自动下发开风扇命令
                DeviceCommandRequest cmdReq = new DeviceCommandRequest();
                cmdReq.setCommandType("RELAY_ON");
                cmdReq.setPayload("FAN");
                deviceCommandService.createCommand(device, cmdReq, "agent");
            }
        } else {
            // 温度降至阈值以下 → 若 5 分钟前有过高温告警且未关过风扇，自动关风扇
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            if (alarmService.hasDuplicateAlarm(device.getId(), "温度过高", fiveMinutesAgo)
                    && !deviceCommandService.hasRecentCommand(device.getId(), "RELAY_OFF", 300)) {
                DeviceCommandRequest cmdReq = new DeviceCommandRequest();
                cmdReq.setCommandType("RELAY_OFF");
                cmdReq.setPayload("FAN");
                deviceCommandService.createCommand(device, cmdReq, "agent");
            }
        }
        if (humidity.compareTo(config.getHumidityMinThreshold()) < 0
                || humidity.compareTo(config.getHumidityMaxThreshold()) > 0) {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            String alarmType = "湿度异常";
            String alarmValue = humidity + "%";
            if (alarmService.hasDuplicateAlarm(device.getId(), alarmType, fiveMinutesAgo)) {
                alarmService.escalateAlarm(device.getId(), alarmType, fiveMinutesAgo, alarmValue);
            } else {
                Alarm alarm = new Alarm();
                alarm.setDeviceId(device.getId());
                alarm.setAlarmType(alarmType);
                alarm.setAlarmValue(alarmValue);
                alarm.setStatus("未处理");
                alarm.setCreateTime(LocalDateTime.now());
                alarmService.addAlarm(alarm);
            }
        }

        return "上报成功";
    }

}



