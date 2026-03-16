package com.prm.module.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProjectScopeDTO {
    private Long userId;
    private String displayName;
    private String username;
    private String employeeNo;
    private List<String> roles;
    private List<UserJoinedProjectDTO> joinedProjects;
}
