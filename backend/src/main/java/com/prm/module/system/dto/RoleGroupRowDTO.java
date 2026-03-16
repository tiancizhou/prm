package com.prm.module.system.dto;

import lombok.Data;

@Data
public class RoleGroupRowDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String tagType;
    private String users;
    private Long memberCount;
}
