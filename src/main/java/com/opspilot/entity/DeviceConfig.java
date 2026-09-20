package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_config")
public class DeviceConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private BigDecimal temperatureThreshold;

    private BigDecimal humidityMinThreshold;

    private BigDecimal humidityMaxThreshold;

    private Integer sampleIntervalSeconds;

    private Integer inspectionIntervalMinutes;

    private String displayMode;

    private LocalDateTime updateTime;
}
