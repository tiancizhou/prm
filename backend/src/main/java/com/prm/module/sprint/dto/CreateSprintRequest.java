package com.prm.module.sprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSprintRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "迭代名称不能为空")
    private String name;

    private String goal;
    private BigDecimal capacityHours;
    private LocalDate startDate;
    private LocalDate endDate;
}
