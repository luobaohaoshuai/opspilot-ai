package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

        @TableId(type = IdType.AUTO)
        private Long id;


        private String documentName;

        private Integer chunkIndex;

        private String content;

        private String vectorJson;

}
