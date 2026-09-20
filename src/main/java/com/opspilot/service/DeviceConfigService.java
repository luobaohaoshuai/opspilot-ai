package com.opspilot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.opspilot.entity.DeviceConfig;
import com.opspilot.entity.DeviceConfigRequest;
import com.opspilot.mapper.DeviceConfigMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DeviceConfigService {
    public static final BigDecimal DEFAULT_TEMPERATURE_THRESHOLD = new BigDecimal("35.00");
    public static final BigDecimal DEFAULT_HUMIDITY_MIN_THRESHOLD = new BigDecimal("20.00");
    public static final BigDecimal DEFAULT_HUMIDITY_MAX_THRESHOLD = new BigDecimal("80.00");
    public static final int DEFAULT_SAMPLE_INTERVAL_SECONDS = 10;
    public static final int DEFAULT_INSPECTION_INTERVAL_MINUTES = 30;
    public static final String DEFAULT_DISPLAY_MODE = "NORMAL";

    private final DeviceConfigMapper deviceConfigMapper;

    public DeviceConfigService(DeviceConfigMapper deviceConfigMapper) {
        this.deviceConfigMapper = deviceConfigMapper;
    }

    public DeviceConfig getOrCreateByDeviceId(Long deviceId) {
        DeviceConfig existing = getByDeviceId(deviceId);
        if (existing != null) {
            return existing;
        }

        DeviceConfig config = newDefaultConfig(deviceId);
        deviceConfigMapper.insert(config);
        return config;
    }

    public DeviceConfig updateConfig(Long deviceId, DeviceConfigRequest request) {
        DeviceConfig config = getOrCreateByDeviceId(deviceId);

        if (request.getTemperatureThreshold() != null) {
            config.setTemperatureThreshold(request.getTemperatureThreshold());
        }
        if (request.getHumidityMinThreshold() != null) {
            config.setHumidityMinThreshold(request.getHumidityMinThreshold());
        }
        if (request.getHumidityMaxThreshold() != null) {
            config.setHumidityMaxThreshold(request.getHumidityMaxThreshold());
        }
        if (request.getSampleIntervalSeconds() != null) {
            config.setSampleIntervalSeconds(clamp(request.getSampleIntervalSeconds(), 5, 3600));
        }
        if (request.getInspectionIntervalMinutes() != null) {
            config.setInspectionIntervalMinutes(clamp(request.getInspectionIntervalMinutes(), 1, 1440));
        }
        if (request.getDisplayMode() != null && !request.getDisplayMode().isBlank()) {
            config.setDisplayMode(request.getDisplayMode().trim().toUpperCase());
        }

        config.setUpdateTime(LocalDateTime.now());
        deviceConfigMapper.updateById(config);
        return config;
    }

    public DeviceConfig getByDeviceId(Long deviceId) {
        QueryWrapper<DeviceConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId).last("LIMIT 1");
        return deviceConfigMapper.selectOne(wrapper);
    }

    private DeviceConfig newDefaultConfig(Long deviceId) {
        DeviceConfig config = new DeviceConfig();
        config.setDeviceId(deviceId);
        config.setTemperatureThreshold(DEFAULT_TEMPERATURE_THRESHOLD);
        config.setHumidityMinThreshold(DEFAULT_HUMIDITY_MIN_THRESHOLD);
        config.setHumidityMaxThreshold(DEFAULT_HUMIDITY_MAX_THRESHOLD);
        config.setSampleIntervalSeconds(DEFAULT_SAMPLE_INTERVAL_SECONDS);
        config.setInspectionIntervalMinutes(DEFAULT_INSPECTION_INTERVAL_MINUTES);
        config.setDisplayMode(DEFAULT_DISPLAY_MODE);
        config.setUpdateTime(LocalDateTime.now());
        return config;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
