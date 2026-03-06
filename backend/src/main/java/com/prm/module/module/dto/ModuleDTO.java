package com.prm.module.module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModuleDTO {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private List<ModuleDTO> children = new ArrayList<>();
}
