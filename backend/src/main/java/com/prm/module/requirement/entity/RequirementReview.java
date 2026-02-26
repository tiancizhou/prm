package com.prm.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_requirement_review")
public class RequirementReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long requirementId;
    private Long reviewerId;
    private String conclusion;
    private String remark;
    private LocalDateTime reviewedAt;
    private Long createdBy;
    private LocalDateTime createdAt;
}
