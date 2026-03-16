package com.prm.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveCompanyRequest {

    @NotBlank(message = "公司名称不能为空")
    private String name;
    private String shortName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String description;
}
