package com.prm.module.sprint.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SprintDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String goal;
    private String status;
    private BigDecimal capacityHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private Integer taskCount;
    private Integer completedTaskCount;
}
