package com.prm.module.requirement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RequirementDTO {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private String source;
    private String priority;
    private String status;
    private String statusLabel;
    private Long assigneeId;
    private String assigneeName;
    private BigDecimal estimatedHours;
    private String acceptanceCriteria;
    private LocalDateTime createdAt;
}
