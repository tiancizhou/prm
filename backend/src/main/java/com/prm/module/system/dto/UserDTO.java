package com.prm.module.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String realName;
    private String employeeNo;
    private Long departmentId;
    private String department;
    /** 部门完整路径，如：一级部门 / 二级部门 */
    private String departmentPath;
    private String team;
    private String externalId;
    private String email;
    private String phone;
    private String avatar;
    private String status;
    private List<String> roles;
    private List<Long> roleIds;
    private Long joinedProjectCount;
    private LocalDateTime createdAt;
}
