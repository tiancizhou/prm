package com.prm.module.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;
    private String type = "TASK";
    private String priority = "MEDIUM";
    private Long requirementId;
    private Long sprintId;
    private Long parentTaskId;
    private Long assigneeId;
    private BigDecimal estimatedHours;
    private LocalDate dueDate;
}
