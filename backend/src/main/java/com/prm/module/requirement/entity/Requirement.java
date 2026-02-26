package com.prm.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pm_requirement")
public class Requirement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private String source;
    private String priority;
    private String status;
    private Long assigneeId;
    private BigDecimal estimatedHours;
    private String acceptanceCriteria;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
