package com.prm.module.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRequirementRequest {

    @NotBlank(message = "需求标题不能为空")
    private String title;

    private String description;
    private String source;
    private String priority = "MEDIUM";
    private Long assigneeId;
    private Long sprintId;
    private BigDecimal estimatedHours;
    private String acceptanceCriteria;
    private LocalDate startDate;
    private LocalDate dueDate;
}
