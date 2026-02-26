package com.prm.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField(select = false)
    private String password;

    private String nickname;
    /** 姓名（与主数据同步） */
    private String realName;
    /** 工号（与主数据同步） */
    private String employeeNo;
    /** 部门（与主数据同步） */
    private String department;
    /** 小组（与主数据同步） */
    private String team;
    /** 主数据唯一标识，同步时用于匹配 */
    private String externalId;
    private String email;
    private String phone;
    private String avatar;
    private String status;

    @TableLogic
    private Integer deleted;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
