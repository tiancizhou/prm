package com.prm.module.project.dto;

import lombok.Data;

@Data
public class ProjectMemberVO {
    private Long userId;
    private String nickname;
    private String username;
    private String employeeNo;
    /** 用户的系统角色 code */
    private String role;
    /** 用户的系统角色名称（中文） */
    private String roleName;
}
