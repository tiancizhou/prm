package com.prm.module.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoleGroupDetailDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String tagType;
    private List<RoleGroupMemberDTO> members = new ArrayList<>();
}
