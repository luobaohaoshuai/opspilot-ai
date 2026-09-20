package com.opspilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("employee")
public class Employee implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "姓名不能为空")
    private String name;
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    private Integer age;
    @NotBlank(message = "部门不能为空")
    private String department;
}
