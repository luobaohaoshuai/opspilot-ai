package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;                    // BIGINT → Long

    @NotBlank(message = "设备编号不能为空")
    @Size(max = 50, message = "设备编号长度不能超过 50 个字符")
    private String deviceCode;          // VARCHAR → String

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过 100 个字符")
    private String name;

    @Size(max = 50, message = "设备类型长度不能超过 50 个字符")
    private String type;

    @Size(max = 100, message = "安装位置长度不能超过 100 个字符")
    private String location;

    private Boolean online;             // TINYINT(1) → Boolean

    private LocalDateTime lastOnlineTime; // DATETIME → LocalDateTime

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String deviceToken;
}
