package com.prm.module.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DepartmentTreeDTO {
    private String nodeKey;
    private String nodeType;
    private Long id;
    private Long companyId;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private List<DepartmentTreeDTO> children = new ArrayList<>();
}
