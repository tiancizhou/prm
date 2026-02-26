package com.prm.module.bug.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_bug")
public class Bug {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private String module;
    private String severity;
    private String priority;
    private String status;
    private Long assigneeId;
    private Long reporterId;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String environment;
    private String resolveType;
    private LocalDateTime resolvedAt;
    private LocalDateTime verifiedAt;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
