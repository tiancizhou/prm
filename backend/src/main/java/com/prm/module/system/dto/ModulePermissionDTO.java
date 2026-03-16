package com.prm.module.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModulePermissionDTO {
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private List<ModulePermissionDTO> children = new ArrayList<>();
}
