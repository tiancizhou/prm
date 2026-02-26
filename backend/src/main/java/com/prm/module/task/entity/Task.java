package com.prm.module.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long requirementId;
    private Long sprintId;
    private Long parentTaskId;
    private String title;
    private String description;
    private String type;
    private String priority;
    private String status;
    private Long assigneeId;
    private BigDecimal estimatedHours;
    private BigDecimal spentHours;
    private BigDecimal remainingHours;
    private LocalDate dueDate;
    private Integer isBlocked;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
