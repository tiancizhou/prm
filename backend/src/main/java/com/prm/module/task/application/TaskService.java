package com.prm.module.task.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.domain.ProjectAccessPolicy;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.task.domain.TaskStateMachine;
import com.prm.module.task.dto.CreateTaskRequest;
import com.prm.module.task.dto.TaskDTO;
import com.prm.module.task.entity.Task;
import com.prm.module.task.entity.TaskWorklog;
import com.prm.module.task.mapper.TaskMapper;
import com.prm.module.task.mapper.TaskWorklogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskWorklogMapper worklogMapper;
    private final TaskStateMachine stateMachine;
    private final SysUserMapper userMapper;
    private final RequirementMapper requirementMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public IPage<TaskDTO> page(int pageNum, int pageSize, Long projectId, String status, Long assigneeId, String keyword, Long requirementId) {
        Page<Task> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>()
                .eq(Task::getDeleted, 0)
                .orderByDesc(Task::getCreatedAt);

        if (!SecurityUtil.isSuperAdmin()) {
            Long currentUserId = SecurityUtil.getCurrentUserId();

            if (projectId != null) {
                ProjectMember membership = findMembership(projectId, currentUserId);
                if (membership == null) {
                    return new Page<>(pageNum, pageSize);
                }
                if (!isProjectManager(membership)) {
                    wrapper.eq(Task::getAssigneeId, currentUserId);
                } else if (assigneeId != null) {
                    wrapper.eq(Task::getAssigneeId, assigneeId);
                }
            } else {
                List<ProjectMember> memberships = projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId));
                if (memberships.isEmpty()) {
                    return new Page<>(pageNum, pageSize);
                }

                Set<Long> allProjectIds = memberships.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toCollection(HashSet::new));
                Set<Long> managerProjectIds = ProjectAccessPolicy.hasProjectManagerSystemRole()
                        ? new HashSet<>(allProjectIds)
                        : new HashSet<>();

                if (managerProjectIds.isEmpty()) {
                    wrapper.in(Task::getProjectId, allProjectIds)
                            .eq(Task::getAssigneeId, currentUserId);
                } else {
                    Set<Long> memberOnlyProjectIds = new HashSet<>(allProjectIds);
                    memberOnlyProjectIds.removeAll(managerProjectIds);

                    if (memberOnlyProjectIds.isEmpty()) {
                        wrapper.in(Task::getProjectId, managerProjectIds);
                        if (assigneeId != null) {
                            wrapper.eq(Task::getAssigneeId, assigneeId);
                        }
                    } else if (assigneeId != null) {
                        wrapper.and(condition -> condition
                                .and(managerCondition -> managerCondition
                                        .in(Task::getProjectId, managerProjectIds)
                                        .eq(Task::getAssigneeId, assigneeId))
                                .or(memberCondition -> memberCondition
                                        .in(Task::getProjectId, memberOnlyProjectIds)
                                        .eq(Task::getAssigneeId, currentUserId)));
                    } else {
                        wrapper.and(condition -> condition
                                .in(Task::getProjectId, managerProjectIds)
                                .or(memberCondition -> memberCondition
                                        .in(Task::getProjectId, memberOnlyProjectIds)
                                        .eq(Task::getAssigneeId, currentUserId)));
                    }
                }
            }
        }

        if (projectId != null) wrapper.eq(Task::getProjectId, projectId);
        if (StringUtils.hasText(status)) wrapper.eq(Task::getStatus, status);
        if (StringUtils.hasText(keyword)) wrapper.like(Task::getTitle, keyword);
        if (requirementId != null) wrapper.eq(Task::getRequirementId, requirementId);
        if (assigneeId != null) wrapper.eq(Task::getAssigneeId, assigneeId);
        pageReq.setOptimizeCountSql(false);
        return taskMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public TaskDTO create(CreateTaskRequest request) {
        ensureProjectManager(request.getProjectId());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Task task = new Task();
        task.setProjectId(request.getProjectId());
        task.setRequirementId(request.getRequirementId());
        task.setSprintId(request.getSprintId());
        task.setParentTaskId(request.getParentTaskId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType() != null ? request.getType() : "TASK");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setStatus("TODO");
        task.setAssigneeId(request.getAssigneeId());
        task.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : BigDecimal.ZERO);
        task.setSpentHours(BigDecimal.ZERO);
        task.setRemainingHours(task.getEstimatedHours());
        task.setDueDate(request.getDueDate());
        task.setIsBlocked(0);
        task.setDeleted(0);
        task.setCreatedBy(currentUserId);
        task.setUpdatedBy(currentUserId);
        taskMapper.insert(task);
        return toDTO(task);
    }

    public TaskDTO getById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1) throw BizException.notFound("任务");
        ensureReadable(task);
        return toDTO(task);
    }

    @Transactional
    public TaskDTO updateStatus(Long id, String newStatus) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Task task = taskMapper.selectById(id);
        if (task == null) throw BizException.notFound("任务");
        ensureOperable(task);
        stateMachine.transit(task.getStatus(), newStatus);
        task.setStatus(newStatus);
        task.setUpdatedBy(currentUserId);
        taskMapper.updateById(task);
        return toDTO(task);
    }

    @Transactional
    public TaskDTO assign(Long id, Long assigneeId) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw BizException.notFound("任务");
        ensureProjectManager(task.getProjectId());
        task.setAssigneeId(assigneeId);
        task.setUpdatedBy(SecurityUtil.getCurrentUserId());
        taskMapper.updateById(task);
        return toDTO(task);
    }

    @Transactional
    public void logWork(Long taskId, BigDecimal spentHours, String remark) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) throw BizException.notFound("任务");
        Long userId = SecurityUtil.getCurrentUserId();
        TaskWorklog log = new TaskWorklog();
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setSpentHours(spentHours);
        log.setLogDate(LocalDate.now());
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        worklogMapper.insert(log);

        BigDecimal totalSpent = taskMapper.sumSpentHoursByTaskId(taskId);
        task.setSpentHours(totalSpent);
        BigDecimal remaining = task.getEstimatedHours().subtract(totalSpent);
        task.setRemainingHours(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining);
        taskMapper.updateById(task);
    }

    public BigDecimal calcRemaining(BigDecimal estimate, BigDecimal consumed) {
        if (estimate == null) return BigDecimal.ZERO;
        if (consumed == null) return estimate;
        BigDecimal remaining = estimate.subtract(consumed);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    private void ensureReadable(Task task) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(task.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("无项目查看权限");
        }
        if (isProjectManager(membership) || Objects.equals(currentUserId, task.getAssigneeId())) {
            return;
        }
        throw BizException.forbidden("仅项目负责人或任务指派人可查看");
    }

    private void ensureOperable(Task task) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(task.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("无项目编辑权限");
        }
        if (isProjectManager(membership) || Objects.equals(currentUserId, task.getAssigneeId())) {
            return;
        }
        throw BizException.forbidden("仅项目负责人或任务指派人可操作");
    }

    private void ensureProjectManager(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(projectId, currentUserId);
        if (!isProjectManager(membership)) {
            throw BizException.forbidden("仅项目经理可操作");
        }
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private boolean isProjectManager(ProjectMember membership) {
        return ProjectAccessPolicy.canManage(membership);
    }

    private TaskDTO toDTO(Task t) {
        TaskDTO dto = new TaskDTO();
        dto.setId(t.getId());
        dto.setProjectId(t.getProjectId());
        dto.setRequirementId(t.getRequirementId());
        dto.setSprintId(t.getSprintId());
        dto.setParentTaskId(t.getParentTaskId());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        dto.setType(t.getType());
        dto.setPriority(t.getPriority());
        dto.setStatus(t.getStatus());
        dto.setStatusLabel(TaskStateMachine.STATUS_LABELS.getOrDefault(t.getStatus(), t.getStatus()));
        dto.setAssigneeId(t.getAssigneeId());
        dto.setEstimatedHours(t.getEstimatedHours());
        dto.setSpentHours(t.getSpentHours());
        dto.setRemainingHours(t.getRemainingHours());
        dto.setDueDate(t.getDueDate());
        dto.setIsBlocked(t.getIsBlocked() != null && t.getIsBlocked() == 1);
        dto.setCreatedAt(t.getCreatedAt());
        if (t.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(t.getAssigneeId());
            if (assignee != null) {
                dto.setAssigneeName(StringUtils.hasText(assignee.getRealName()) ? assignee.getRealName() : assignee.getNickname());
            }
        }
        if (t.getRequirementId() != null) {
            Requirement req = requirementMapper.selectById(t.getRequirementId());
            if (req != null) dto.setRequirementTitle(req.getTitle());
        }
        return dto;
    }
}


