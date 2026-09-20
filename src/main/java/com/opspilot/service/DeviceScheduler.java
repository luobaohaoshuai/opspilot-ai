package com.opspilot.service;

import com.opspilot.entity.Alarm;
import com.opspilot.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class DeviceScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeviceScheduler.class);
    private static final int OFFLINE_SECONDS = 120;
    private static final int COMMAND_TIMEOUT_SECONDS = 30;

    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private final DeviceCommandService deviceCommandService;

    public DeviceScheduler(DeviceService deviceService,
                           AlarmService alarmService,
                           DeviceCommandService deviceCommandService) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.deviceCommandService = deviceCommandService;
    }

    // 每 60 秒检查一次设备离线
    @Scheduled(fixedDelay = 60000)
    public void detectOfflineDevices() {
        List<Device> devices = deviceService.listDevices();
        LocalDateTime deadline = LocalDateTime.now().minusSeconds(OFFLINE_SECONDS);
        for (Device device : devices) {
            if (device.getLastOnlineTime() == null
                    || device.getLastOnlineTime().isBefore(deadline)) {
                if (Boolean.TRUE.equals(device.getOnline())) {
                    device.setOnline(false);
                    deviceService.updateDevice(device);

                    Alarm alarm = new Alarm();
                    alarm.setDeviceId(device.getId());
                    alarm.setAlarmType("设备离线");
                    alarm.setAlarmValue("超过 " + OFFLINE_SECONDS + " 秒未上报");
                    alarm.setStatus("未处理");
                    alarm.setCreateTime(LocalDateTime.now());
                    alarmService.addAlarm(alarm);

                    log.warn("设备离线: {}", device.getDeviceCode());
                }
            }
        }
    }

    // 每 30 秒清理超时命令
    @Scheduled(fixedDelay = 30000)
    public void expireStaleCommands() {
        int count = deviceCommandService.expireStaleCommands(COMMAND_TIMEOUT_SECONDS);
        if (count > 0) {
            log.warn("清理了 {} 条超时命令", count);
        }
    }
}
