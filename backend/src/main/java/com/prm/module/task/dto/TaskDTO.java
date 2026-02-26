package com.prm.module.task.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    private Long projectId;
    private Long requirementId;
    private Long sprintId;
    private Long parentTaskId;
    private String title;
    private String description;
    private String type;
    private String priority;
    private String status;
    private String statusLabel;
    private Long assigneeId;
    private String assigneeName;
    private String requirementTitle;
    private BigDecimal estimatedHours;
    private BigDecimal spentHours;
    private BigDecimal remainingHours;
    private LocalDate dueDate;
    private Boolean isBlocked;
    private LocalDateTime createdAt;
}
