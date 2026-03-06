package com.prm.module.module.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_module")
public class PrmModule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private Long parentId;
    private String name;
    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
