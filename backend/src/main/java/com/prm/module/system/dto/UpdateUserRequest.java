package com.prm.module.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
    private String realName;
    private String employeeNo;
    private Long departmentId;
    private String department;
    private String team;
    private String externalId;
    private String email;
    private String phone;
    private List<Long> roleIds;
}
