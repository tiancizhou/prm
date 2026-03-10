package com.prm.module.sprint.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.sprint.domain.SprintStateMachine;
import com.prm.module.sprint.dto.CreateSprintRequest;
import com.prm.module.sprint.dto.SprintDTO;
import com.prm.module.sprint.entity.Sprint;
import com.prm.module.sprint.mapper.SprintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintMapper sprintMapper;
    private final SprintStateMachine stateMachine;
    private final BugMapper bugMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public IPage<SprintDTO> page(int pageNum, int pageSize, Long projectId) {
        Page<Sprint> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Sprint> wrapper = new LambdaQueryWrapper<Sprint>()
                .eq(Sprint::getDeleted, 0)
                .orderByDesc(Sprint::getCreatedAt);

        if (!SecurityUtil.isSuperAdmin()) {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            if (projectId != null) {
                ProjectMember membership = findMembership(projectId, currentUserId);
                if (membership == null) {
                    return new Page<>(pageNum, pageSize);
                }
            } else {
                List<ProjectMember> memberships = projectMemberMapper.selectList(
                        new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId));
                if (memberships.isEmpty()) {
                    return new Page<>(pageNum, pageSize);
                }
                Set<Long> projectIds = memberships.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toSet());
                wrapper.in(Sprint::getProjectId, projectIds);
            }
        }

        if (projectId != null) wrapper.eq(Sprint::getProjectId, projectId);
        return sprintMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public SprintDTO create(CreateSprintRequest request) {
        ensureProjectManager(request.getProjectId());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Sprint sprint = new Sprint();
        sprint.setProjectId(request.getProjectId());
        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStatus("PLANNING");
        sprint.setCapacityHours(request.getCapacityHours() != null ? request.getCapacityHours() : BigDecimal.ZERO);
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setDeleted(0);
        sprint.setCreatedBy(currentUserId);
        sprint.setUpdatedBy(currentUserId);
        sprintMapper.insert(sprint);
        return toDTO(sprint);
    }

    public SprintDTO getById(Long id) {
        Sprint sprint = sprintMapper.selectById(id);
        if (sprint == null || sprint.getDeleted() == 1) throw BizException.notFound("杩唬");
        ensureReadable(sprint);
        return toDTO(sprint);
    }

    @Transactional
    public SprintDTO start(Long id) {
        Sprint sprint = sprintMapper.selectById(id);
        if (sprint == null) throw BizException.notFound("杩唬");
        ensureProjectManager(sprint.getProjectId());
        stateMachine.transit(sprint.getStatus(), "ACTIVE");
        sprint.setStatus("ACTIVE");
        sprint.setUpdatedBy(SecurityUtil.getCurrentUserId());
        sprintMapper.updateById(sprint);
        return toDTO(sprint);
    }

    @Transactional
    public SprintDTO close(Long id) {
        Sprint sprint = sprintMapper.selectById(id);
        if (sprint == null) throw BizException.notFound("杩唬");
        ensureProjectManager(sprint.getProjectId());
        stateMachine.transit(sprint.getStatus(), "CLOSED");

        int openCritical = bugMapper.countOpenCriticalInSprint(id);
        if (openCritical > 0) {
            throw BizException.of("瀛樺湪 " + openCritical + " 涓湭鍏抽棴鐨勪弗閲?闃诲缂洪櫡锛屾棤娉曞叧闂凯浠?");
        }

        sprint.setStatus("CLOSED");
        sprint.setClosedAt(LocalDateTime.now());
        sprint.setUpdatedBy(SecurityUtil.getCurrentUserId());
        sprintMapper.updateById(sprint);
        return toDTO(sprint);
    }

    private void ensureReadable(Sprint sprint) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(sprint.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("鏃犳潈鏌ョ湅璇ヨ凯浠?");
        }
    }

    private void ensureProjectManager(Long projectId) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(projectId, currentUserId);
        if (!isProjectManager(membership)) {
            throw BizException.forbidden("浠呴」鐩粡鐞嗗彲鎿嶄綔杩唬");
        }
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private boolean isProjectManager(ProjectMember membership) {
        return membership != null && "PROJECT_ADMIN".equalsIgnoreCase(membership.getRole());
    }

    private SprintDTO toDTO(Sprint s) {
        SprintDTO dto = new SprintDTO();
        dto.setId(s.getId());
        dto.setProjectId(s.getProjectId());
        dto.setName(s.getName());
        dto.setGoal(s.getGoal());
        dto.setStatus(s.getStatus());
        dto.setCapacityHours(s.getCapacityHours());
        dto.setStartDate(s.getStartDate());
        dto.setEndDate(s.getEndDate());
        dto.setClosedAt(s.getClosedAt());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
