package com.prm.module.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverviewDTO {

    private long totalProjects;
    private long activeProjects;
    private long overdueProjects;

    private long totalRequirements;
    private long doneRequirements;

    private long totalTasks;
    private long inProgressTasks;
    private long overdueTasks;

    private long totalBugs;
    private long openBugs;
    private long criticalOpenBugs;
}
