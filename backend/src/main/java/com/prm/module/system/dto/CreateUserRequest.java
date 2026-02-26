package com.prm.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度3-64字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度6-64字符")
    private String password;

    private String realName;
    private String employeeNo;
    private String department;
    private String team;
    private String externalId;
    private String email;
    private String phone;
    private List<Long> roleIds;
}
