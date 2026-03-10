package com.prm.module.dashboard.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.entity.Bug;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.dashboard.dto.OverviewDTO;
import com.prm.module.dashboard.entity.DashboardSnapshot;
import com.prm.module.dashboard.mapper.DashboardSnapshotMapper;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final DashboardSnapshotMapper snapshotMapper;
    private final ObjectMapper objectMapper;

    public OverviewDTO getOverview(Long projectId) {
        DashboardScope scope = resolveDashboardScope(projectId);
        return buildOverview(scope);
    }

    public void requireAggregatePermission(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, currentUserId));
        if (membership == null || !"PROJECT_ADMIN".equalsIgnoreCase(membership.getRole())) {
            throw BizException.forbidden("仅项目经理可触发所属项目聚合");
        }
    }

    private DashboardScope resolveDashboardScope(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return new DashboardScope(projectId == null ? null : Set.of(projectId), false, null);
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        Set<Long> memberProjectIds = projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId))
                .stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> managerProjectIds = projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId)
                                .eq(ProjectMember::getRole, "PROJECT_ADMIN"))
                .stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toCollection(HashSet::new));

        if (memberProjectIds.isEmpty()) {
            return new DashboardScope(Set.of(), true, currentUserId);
        }

        if (projectId != null) {
            if (!memberProjectIds.contains(projectId)) {
                return new DashboardScope(Set.of(), true, currentUserId);
            }
            boolean managerView = managerProjectIds.contains(projectId);
            return new DashboardScope(Set.of(projectId), !managerView, managerView ? null : currentUserId);
        }

        Set<Long> scopedProjectIds = memberProjectIds;
        boolean allProjectsManaged = !managerProjectIds.isEmpty() && managerProjectIds.size() == memberProjectIds.size();
        if (allProjectsManaged) {
            return new DashboardScope(managerProjectIds, false, null);
        }
        return new DashboardScope(scopedProjectIds, true, currentUserId);
    }

    private OverviewDTO buildOverview(DashboardScope scope) {
        Set<Long> scopedProjectIds = scope.projectIds();
        if (scopedProjectIds != null && scopedProjectIds.isEmpty()) {
            return OverviewDTO.builder().build();
        }
        boolean personalView = scope.personalView();
        Long currentUserId = scope.currentUserId();

        LambdaQueryWrapper<Project> projectWrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .in(scopedProjectIds != null, Project::getId, scopedProjectIds);
        long totalProjects = projectMapper.selectCount(projectWrapper);
        long activeProjects = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0)
                        .in(scopedProjectIds != null, Project::getId, scopedProjectIds)
                        .eq(Project::getStatus, "ACTIVE"));
        long overdueProjects = projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0)
                        .in(scopedProjectIds != null, Project::getId, scopedProjectIds)
                        .eq(Project::getStatus, "ACTIVE")
                        .lt(Project::getEndDate, LocalDate.now()));

        LambdaQueryWrapper<Requirement> requirementWrapper = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getDeleted, 0)
                .in(scopedProjectIds != null, Requirement::getProjectId, scopedProjectIds)
                .eq(personalView, Requirement::getAssigneeId, currentUserId);
        long totalReqs = requirementMapper.selectCount(requirementWrapper);
        long doneReqs = requirementMapper.selectCount(
                new LambdaQueryWrapper<Requirement>().eq(Requirement::getDeleted, 0)
                        .in(scopedProjectIds != null, Requirement::getProjectId, scopedProjectIds)
                        .eq(personalView, Requirement::getAssigneeId, currentUserId)
                        .in(Requirement::getStatus, List.of("DONE", "CLOSED")));

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getDeleted, 0)
                .in(scopedProjectIds != null, Task::getProjectId, scopedProjectIds)
                .eq(personalView, Task::getAssigneeId, currentUserId);
        long totalTasks = taskMapper.selectCount(taskWrapper);
        long inProgressTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getDeleted, 0)
                        .in(scopedProjectIds != null, Task::getProjectId, scopedProjectIds)
                        .eq(personalView, Task::getAssigneeId, currentUserId)
                        .eq(Task::getStatus, "IN_PROGRESS"));
        long overdueTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getDeleted, 0)
                        .in(scopedProjectIds != null, Task::getProjectId, scopedProjectIds)
                        .eq(personalView, Task::getAssigneeId, currentUserId)
                        .notIn(Task::getStatus, List.of("DONE", "CLOSED"))
                        .lt(Task::getDueDate, LocalDate.now()));

        LambdaQueryWrapper<Bug> bugWrapper = new LambdaQueryWrapper<Bug>()
                .eq(Bug::getDeleted, 0)
                .in(scopedProjectIds != null, Bug::getProjectId, scopedProjectIds)
                .eq(personalView, Bug::getAssigneeId, currentUserId);
        long totalBugs = bugMapper.selectCount(bugWrapper);
        long openBugs = bugMapper.selectCount(
                new LambdaQueryWrapper<Bug>().eq(Bug::getDeleted, 0)
                        .in(scopedProjectIds != null, Bug::getProjectId, scopedProjectIds)
                        .eq(personalView, Bug::getAssigneeId, currentUserId)
                        .notIn(Bug::getStatus, List.of("CLOSED")));
        long criticalOpenBugs = bugMapper.selectCount(
                new LambdaQueryWrapper<Bug>().eq(Bug::getDeleted, 0)
                        .in(scopedProjectIds != null, Bug::getProjectId, scopedProjectIds)
                        .eq(personalView, Bug::getAssigneeId, currentUserId)
                        .notIn(Bug::getStatus, List.of("CLOSED"))
                        .in(Bug::getSeverity, List.of("CRITICAL", "BLOCKER")));

        return OverviewDTO.builder()
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .overdueProjects(overdueProjects)
                .totalRequirements(totalReqs)
                .doneRequirements(doneReqs)
                .totalTasks(totalTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .totalBugs(totalBugs)
                .openBugs(openBugs)
                .criticalOpenBugs(criticalOpenBugs)
                .build();
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void runDailyAggregation() {
        log.info("Start daily dashboard aggregation");
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0)
                        .in(Project::getStatus, List.of("ACTIVE")));
        for (Project project : projects) {
            try {
                aggregateProject(project.getId());
            } catch (Exception exception) {
                log.error("Aggregate dashboard snapshot failed, projectId={}", project.getId(), exception);
            }
        }
        log.info("Finish daily dashboard aggregation, total projects={}", projects.size());
    }

    public void aggregateProject(Long projectId) {
        OverviewDTO overview = buildOverview(new DashboardScope(Set.of(projectId), false, null));
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
        } catch (Exception exception) {
            log.error("Serialize dashboard snapshot failed", exception);
        }
    }

    private record DashboardScope(Set<Long> projectIds, boolean personalView, Long currentUserId) {
    }
}
