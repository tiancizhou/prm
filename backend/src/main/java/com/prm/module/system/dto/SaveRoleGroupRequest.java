package com.prm.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveRoleGroupRequest {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    private String code;

    /** 前端标签类型：danger/warning/primary/success/info，可选，默认 info */
    private String tagType;

    private String description;
}
