package com.prm.module.release.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateReleaseRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "版本号不能为空")
    private String version;

    private String description;
    private Long sprintId;
    private LocalDate releaseDate;
}
