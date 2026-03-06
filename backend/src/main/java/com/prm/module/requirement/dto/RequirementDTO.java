package com.prm.module.requirement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RequirementDTO {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String parentTitle;
    private Long sprintId;
    private Integer childrenCount;
    private String title;
    private String description;
    private String source;
    private String priority;
    private String status;
    private String statusLabel;
    private Long assigneeId;
    private String assigneeName;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    private String verificationScenario;
    private String verificationSteps;
    private String verificationResult;
    private String acceptanceCriteria;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
