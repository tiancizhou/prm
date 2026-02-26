package com.prm.module.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称不超过128字符")
    private String name;

    @NotBlank(message = "项目代号不能为空")
    @Size(max = 64, message = "项目代号不超过64字符")
    private String code;

    private String description;
    private String visibility = "PRIVATE";
    private LocalDate startDate;
    private LocalDate endDate;
}
