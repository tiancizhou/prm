package com.prm.module.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectMemberVO {
    private Long userId;
    private String nickname;
    private String username;
    private String employeeNo;
    private String role;
    private String roleName;
    private LocalDateTime joinedAt;
}
