package com.prm.module.bug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBugRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "Bug标题不能为空")
    private String title;

    private String description;
    private String module;
    private String severity = "NORMAL";
    private String priority = "MEDIUM";
    private Long assigneeId;
    private Long sprintId;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String environment;
}
