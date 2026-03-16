package com.prm.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveDepartmentRequest {

    @NotBlank(message = "部门名称不能为空")
    private String name;
    private Long companyId;
    private Long parentId;
    private Integer sortOrder;
    private String status;
}
