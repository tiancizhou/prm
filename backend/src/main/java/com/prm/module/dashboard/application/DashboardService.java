package com.prm.module.dashboard.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prm.module.bug.entity.Bug;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.dashboard.dto.OverviewDTO;
import com.prm.module.dashboard.entity.DashboardSnapshot;
import com.prm.module.dashboard.mapper.DashboardSnapshotMapper;
import com.prm.module.project.entity.Project;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.task.entity.Task;
import com.prm.module.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectMapper projectMapper;
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final DashboardSnapshotMapper snapshotMapper;
    private final ObjectMapper objectMapper;

    public OverviewDTO getOverview(Long projectId) {
        LambdaQueryWrapper<Project> pWrapper = new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0);
        if (projectId != null) pWrapper.eq(Project::getId, projectId);
        long totalProjects = projectMapper.selectCount(pWrapper);
        long activeProjects = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0).eq(Project::getStatus, "ACTIVE"));
        long overdueProjects = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0)
                        .eq(Project::getStatus, "ACTIVE").lt(Project::getEndDate, LocalDate.now()));

        LambdaQueryWrapper<Requirement> rWrapper = new LambdaQueryWrapper<Requirement>().eq(Requirement::getDeleted, 0);
        if (projectId != null) rWrapper.eq(Requirement::getProjectId, projectId);
        long totalReqs = requirementMapper.selectCount(rWrapper);
        long doneReqs = requirementMapper.selectCount(
                new LambdaQueryWrapper<Requirement>().eq(Requirement::getDeleted, 0)
                        .eq(projectId != null, Requirement::getProjectId, projectId)
                        .in(Requirement::getStatus, List.of("DONE", "CLOSED")));

        LambdaQueryWrapper<Task> tWrapper = new LambdaQueryWrapper<Task>().eq(Task::getDeleted, 0);
        if (projectId != null) tWrapper.eq(Task::getProjectId, projectId);
        long totalTasks = taskMapper.selectCount(tWrapper);
        long inProgressTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getDeleted, 0)
                        .eq(projectId != null, Task::getProjectId, projectId)
                        .eq(Task::getStatus, "IN_PROGRESS"));
        long overdueTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getDeleted, 0)
                        .eq(projectId != null, Task::getProjectId, projectId)
                        .notIn(Task::getStatus, List.of("DONE", "CLOSED"))
                        .lt(Task::getDueDate, LocalDate.now()));

        LambdaQueryWrapper<Bug> bWrapper = new LambdaQueryWrapper<Bug>().eq(Bug::getDeleted, 0);
        if (projectId != null) bWrapper.eq(Bug::getProjectId, projectId);
        long totalBugs = bugMapper.selectCount(bWrapper);
        long openBugs = bugMapper.selectCount(
                new LambdaQueryWrapper<Bug>().eq(Bug::getDeleted, 0)
                        .eq(projectId != null, Bug::getProjectId, projectId)
                        .notIn(Bug::getStatus, List.of("CLOSED", "VERIFIED")));
        long criticalOpenBugs = bugMapper.selectCount(
                new LambdaQueryWrapper<Bug>().eq(Bug::getDeleted, 0)
                        .eq(projectId != null, Bug::getProjectId, projectId)
                        .notIn(Bug::getStatus, List.of("CLOSED", "VERIFIED"))
                        .in(Bug::getSeverity, List.of("CRITICAL", "BLOCKER")));

        return OverviewDTO.builder()
                .totalProjects(totalProjects).activeProjects(activeProjects).overdueProjects(overdueProjects)
                .totalRequirements(totalReqs).doneRequirements(doneReqs)
                .totalTasks(totalTasks).inProgressTasks(inProgressTasks).overdueTasks(overdueTasks)
                .totalBugs(totalBugs).openBugs(openBugs).criticalOpenBugs(criticalOpenBugs)
                .build();
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void runDailyAggregation() {
        log.info("开始执行每日看板快照聚合...");
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0)
                        .in(Project::getStatus, List.of("ACTIVE")));
        for (Project project : projects) {
            try {
                aggregateProject(project.getId());
            } catch (Exception e) {
                log.error("项目[{}]快照聚合失败", project.getId(), e);
            }
        }
        log.info("每日看板快照聚合完成，共处理 {} 个项目", projects.size());
    }

    public void aggregateProject(Long projectId) {
        OverviewDTO overview = getOverview(projectId);
        LocalDate today = LocalDate.now();
        try {
            String json = objectMapper.writeValueAsString(overview);
            DashboardSnapshot existing = snapshotMapper.findByProjectAndDate(projectId, today);
            if (existing != null) {
                existing.setMetricsJson(json);
                snapshotMapper.updateById(existing);
            } else {
                DashboardSnapshot snapshot = new DashboardSnapshot();
                snapshot.setProjectId(projectId);
                snapshot.setSnapDate(today);
                snapshot.setMetricsJson(json);
                snapshot.setCreatedAt(LocalDateTime.now());
                snapshotMapper.insert(snapshot);
            }
        } catch (Exception e) {
            log.error("序列化快照数据失败", e);
        }
    }
}
