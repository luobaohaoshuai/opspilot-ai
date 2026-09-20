package com.opspilot.entity;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceConfigRequest {
    @DecimalMin(value = "-50", message = "温度阈值不能低于 -50℃")
    @DecimalMax(value = "150", message = "温度阈值不能高于 150℃")
    private BigDecimal temperatureThreshold;

    @DecimalMin(value = "0", message = "湿度下限不能低于 0%")
    @DecimalMax(value = "100", message = "湿度下限不能高于 100%")
    private BigDecimal humidityMinThreshold;

    @DecimalMin(value = "0", message = "湿度上限不能低于 0%")
    @DecimalMax(value = "100", message = "湿度上限不能高于 100%")
    private BigDecimal humidityMaxThreshold;

    @Min(value = 5, message = "采样间隔不能少于 5 秒")
    @Max(value = 3600, message = "采样间隔不能超过 3600 秒")
    private Integer sampleIntervalSeconds;

    @Min(value = 1, message = "巡检间隔不能少于 1 分钟")
    @Max(value = 1440, message = "巡检间隔不能超过 1440 分钟")
    private Integer inspectionIntervalMinutes;

    @Pattern(regexp = "(?i)NORMAL|ALERT|MAINTENANCE", message = "屏幕模式仅支持 NORMAL、ALERT 或 MAINTENANCE")
    private String displayMode;

    @AssertTrue(message = "湿度下限不能高于湿度上限")
    public boolean isHumidityRangeValid() {
        return humidityMinThreshold == null
                || humidityMaxThreshold == null
                || humidityMinThreshold.compareTo(humidityMaxThreshold) <= 0;
    }
}
