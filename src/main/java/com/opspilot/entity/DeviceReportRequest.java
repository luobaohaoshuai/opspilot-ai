package com.opspilot.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceReportRequest {

    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    @NotNull(message = "温度不能为空")
    @DecimalMin(value = "-50", message = "温度不能低于 -50℃")
    @DecimalMax(value = "150", message = "温度不能高于 150℃")
    private BigDecimal temperature;

    @NotNull(message = "湿度不能为空")
    @DecimalMin(value = "0", message = "湿度不能低于 0%")
    @DecimalMax(value = "100", message = "湿度不能高于 100%")
    private BigDecimal humidity;

    @Min(value = -120, message = "RSSI 不能低于 -120dBm")
    @Max(value = 0, message = "RSSI 不能高于 0dBm")
    private Integer rssi;

    @PositiveOrZero(message = "运行时长不能为负数")
    private Long uptimeSeconds;

    @Size(max = 50, message = "固件版本长度不能超过 50 个字符")
    private String firmwareVersion;
}
