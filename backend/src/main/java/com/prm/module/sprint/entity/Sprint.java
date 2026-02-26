package com.prm.module.sprint.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_sprint")
public class Sprint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private String name;
    private String goal;
    private String status;
    private BigDecimal capacityHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime closedAt;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
