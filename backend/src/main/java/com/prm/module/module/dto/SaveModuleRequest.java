package com.prm.module.module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveModuleRequest {

    @NotBlank(message = "模块名称不能为空")
    private String name;

    private Long parentId;

    private Integer sortOrder = 0;
}
