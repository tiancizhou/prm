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
    private String realName;
    private String employeeNo;
    private Long departmentId;
    private String department;
    private String team;
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
