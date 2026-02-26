package com.prm.module.bug.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BugDTO {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private String module;
    private String severity;
    private String priority;
    private String status;
    private String statusLabel;
    private Long assigneeId;
    private String assigneeName;
    private Long reporterId;
    private String reporterName;
    private String steps;
    private String expectedResult;
    private String actualResult;
    private String environment;
    private String resolveType;
    private LocalDateTime resolvedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
