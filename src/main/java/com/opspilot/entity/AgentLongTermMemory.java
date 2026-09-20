package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_long_term_memory")
public class AgentLongTermMemory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String memoryType;
    private String content;
    private String source;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
