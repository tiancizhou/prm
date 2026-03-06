package com.prm.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_requirement_log")
public class RequirementLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long requirementId;
    private Long userId;
    private String username;
    /** AUTO = 系统自动记录；COMMENT = 用户手动备注 */
    private String logType;
    private String content;
    private LocalDateTime createdAt;
}
