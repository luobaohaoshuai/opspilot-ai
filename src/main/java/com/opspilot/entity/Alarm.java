package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("alarm")
public class Alarm {


    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String alarmType;

    private String alarmValue;

    private String status;

    private LocalDateTime createTime;

}
