package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_command")
public class DeviceCommand {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String commandType;

    private String payload;

    private String status;

    private String issuedBy;

    private String resultMessage;

    private LocalDateTime createTime;

    private LocalDateTime sentTime;

    private LocalDateTime doneTime;
}
