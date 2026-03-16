package com.prm.module.system.dto;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentDetailDTO {
    private Long id;
    private Long companyId;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private Long userCount;
    private List<DepartmentTreeDTO> children;
}
