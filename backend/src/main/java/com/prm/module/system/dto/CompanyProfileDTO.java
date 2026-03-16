package com.prm.module.system.dto;

import lombok.Data;

@Data
public class CompanyProfileDTO {
    private Long id;
    private String name;
    private String shortName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String description;
    private String status;
}
