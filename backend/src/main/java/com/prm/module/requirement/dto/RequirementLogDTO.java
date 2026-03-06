package com.prm.module.requirement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequirementLogDTO {
    private Long id;
    private Long requirementId;
    private Long userId;
    private String username;
    private String logType;
    private String content;
    private LocalDateTime createdAt;
}
