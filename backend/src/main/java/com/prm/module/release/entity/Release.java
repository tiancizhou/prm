package com.prm.module.release.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_release")
public class Release {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long sprintId;
    private String version;
    private String description;
    private String status;
    private LocalDate releaseDate;
    private Long releasedBy;
    private LocalDateTime releasedAt;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
