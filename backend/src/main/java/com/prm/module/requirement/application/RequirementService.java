package com.prm.module.requirement.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.domain.RequirementStateMachine;
import com.prm.module.requirement.dto.CreateRequirementRequest;
import com.prm.module.requirement.dto.RequirementDTO;
import com.prm.module.requirement.dto.UpdateRequirementRequest;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.entity.RequirementReview;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.requirement.mapper.RequirementReviewMapper;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementMapper requirementMapper;
    private final RequirementReviewMapper reviewMapper;
    private final RequirementStateMachine stateMachine;
    private final SysUserMapper userMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public IPage<RequirementDTO> page(int pageNum, int pageSize, Long projectId, String status, String keyword,
                                       Long assigneeId, Long sprintId, Boolean unscheduled,
                                       LocalDate dueDateFrom, LocalDate dueDateTo, Long parentId) {
        Page<Requirement> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getDeleted, 0)
                .orderByDesc(Requirement::getCreatedAt);

        if (!SecurityUtil.isSuperAdmin()) {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            List<ProjectMember> memberships = projectMemberMapper.selectList(
                    new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getUserId, currentUserId));
            if (memberships.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }

            Set<Long> allProjectIds = memberships.stream()
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toCollection(HashSet::new));
            Set<Long> managerProjectIds = memberships.stream()
                    .filter(member -> "PROJECT_ADMIN".equalsIgnoreCase(member.getRole()))
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toCollection(HashSet::new));

            if (projectId != null) {
                if (!allProjectIds.contains(projectId)) {
                    return new Page<>(pageNum, pageSize);
                }
                wrapper.eq(Requirement::getProjectId, projectId);
                if (!managerProjectIds.contains(projectId)) {
                    wrapper.eq(Requirement::getAssigneeId, currentUserId);
                }
            } else {
                if (managerProjectIds.isEmpty()) {
                    wrapper.in(Requirement::getProjectId, allProjectIds)
                            .eq(Requirement::getAssigneeId, currentUserId);
                } else {
                    Set<Long> memberOnlyProjectIds = new HashSet<>(allProjectIds);
                    memberOnlyProjectIds.removeAll(managerProjectIds);

                    if (memberOnlyProjectIds.isEmpty()) {
                        wrapper.in(Requirement::getProjectId, managerProjectIds);
                    } else {
                        wrapper.and(condition -> condition
                                .in(Requirement::getProjectId, managerProjectIds)
                                .or(memberCondition -> memberCondition
                                        .in(Requirement::getProjectId, memberOnlyProjectIds)
                                        .eq(Requirement::getAssigneeId, currentUserId)));
                    }
                }
            }
        } else if (projectId != null) {
            wrapper.eq(Requirement::getProjectId, projectId);
        }

        if (StringUtils.hasText(status)) wrapper.eq(Requirement::getStatus, status);
        if (StringUtils.hasText(keyword)) wrapper.like(Requirement::getTitle, keyword);
        if (assigneeId != null) wrapper.eq(Requirement::getAssigneeId, assigneeId);
        if (Boolean.TRUE.equals(unscheduled)) {
            wrapper.and(w -> w
                    .isNull(Requirement::getSprintId)
                    .isNull(Requirement::getStartDate)
                    .isNull(Requirement::getDueDate));
        }
        else if (sprintId != null) wrapper.eq(Requirement::getSprintId, sprintId);
        if (dueDateFrom != null) wrapper.ge(Requirement::getDueDate, dueDateFrom);
        if (dueDateTo != null)   wrapper.le(Requirement::getDueDate, dueDateTo);
        // 父子需求过滤：指定 parentId 查子需求；不指定则默认只查顶级需求
        if (parentId != null) {
            wrapper.eq(Requirement::getParentId, parentId);
        } else {
            wrapper.isNull(Requirement::getParentId);
        }

        IPage<Requirement> rPage = requirementMapper.selectPage(pageReq, wrapper);
        return rPage.convert(this::toDTO);
    }

    @Transactional
    public RequirementDTO create(CreateRequirementRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!SecurityUtil.isSuperAdmin()) {
            ProjectMember membership = findMembership(request.getProjectId(), currentUserId);
            if (!isProjectManager(membership)) {
                throw BizException.forbidden("只有项目经理或超级管理员才能创建需求");
            }
        }
        Requirement r = new Requirement();
        r.setProjectId(request.getProjectId());
        r.setParentId(request.getParentId());
        r.setSprintId(request.getSprintId());
        r.setTitle(request.getTitle());
        r.setDescription(request.getDescription());
        r.setSource(request.getSource());
        r.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        r.setStatus("DRAFT");
        r.setAssigneeId(request.getAssigneeId());
        r.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : BigDecimal.ZERO);
        r.setActualHours(BigDecimal.ZERO);
        r.setAcceptanceCriteria(request.getAcceptanceCriteria());
        r.setStartDate(request.getStartDate());
        r.setDueDate(request.getDueDate());
        r.setDeleted(0);
        r.setCreatedBy(currentUserId);
        r.setUpdatedBy(currentUserId);
        requirementMapper.insert(r);
        return toDTO(r);
    }

    public RequirementDTO getById(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) throw BizException.notFound("需求");
        ensureRequirementReadable(r);
        return toDTO(r);
    }

    @Transactional
    public RequirementDTO update(Long id, UpdateRequirementRequest request) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) throw BizException.notFound("需求");
        ensureRequirementManageable(r);
        r.setTitle(request.getTitle());
        r.setDescription(request.getDescription());
        r.setSource(request.getSource());
        r.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        r.setAssigneeId(request.getAssigneeId());
        r.setSprintId(request.getSprintId());
        r.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : BigDecimal.ZERO);
        r.setAcceptanceCriteria(request.getAcceptanceCriteria());
        r.setStartDate(request.getStartDate());
        r.setDueDate(request.getDueDate());
        r.setUpdatedBy(SecurityUtil.getCurrentUserId());
        requirementMapper.updateById(r);
        return toDTO(r);
    }

    @Transactional
    public RequirementDTO updateStatus(Long id,
                                       String newStatus,
                                       LocalDateTime actualStartAt,
                                       LocalDateTime actualEndAt,
                                       String verificationScenario,
                                       String verificationSteps,
                                       String verificationResult) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw BizException.notFound("需求");
        ensureRequirementEditable(r);
        stateMachine.transit(r.getStatus(), newStatus);

        if ("DONE".equalsIgnoreCase(newStatus)) {
            BigDecimal resolvedActualHours = resolveDoneActualHours(actualStartAt, actualEndAt);
            String resolvedVerificationScenario = requireDoneVerificationField(verificationScenario, "验证场景");
            String resolvedVerificationSteps = requireDoneVerificationField(verificationSteps, "验证步骤");
            String resolvedVerificationResult = requireDoneVerificationField(verificationResult, "实际结果");

            r.setActualStartAt(actualStartAt);
            r.setActualEndAt(actualEndAt);
            r.setActualHours(resolvedActualHours);
            r.setVerificationScenario(resolvedVerificationScenario);
            r.setVerificationSteps(resolvedVerificationSteps);
            r.setVerificationResult(resolvedVerificationResult);
        } else {
            r.setActualStartAt(null);
            r.setActualEndAt(null);
            r.setActualHours(BigDecimal.ZERO);
        }

        r.setStatus(newStatus);
        r.setUpdatedBy(SecurityUtil.getCurrentUserId());
        requirementMapper.updateById(r);
        return toDTO(r);
    }

    @Transactional
    public void addReview(Long requirementId, String conclusion, String remark) {
        Requirement requirement = requirementMapper.selectById(requirementId);
        if (requirement == null || requirement.getDeleted() == 1) {
            throw BizException.notFound("需求");
        }
        ensureRequirementReadable(requirement);

        Long reviewerId = SecurityUtil.getCurrentUserId();
        RequirementReview review = new RequirementReview();
        review.setRequirementId(requirementId);
        review.setReviewerId(reviewerId);
        review.setConclusion(conclusion);
        review.setRemark(remark);
        review.setReviewedAt(LocalDateTime.now());
        review.setCreatedBy(reviewerId);
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    private RequirementDTO toDTO(Requirement r) {
        RequirementDTO dto = new RequirementDTO();
        dto.setId(r.getId());
        dto.setProjectId(r.getProjectId());
        dto.setParentId(r.getParentId());
        if (r.getParentId() != null) {
            Requirement parent = requirementMapper.selectById(r.getParentId());
            if (parent != null) dto.setParentTitle(parent.getTitle());
        }
        // 子需求数量
        Long childCount = requirementMapper.selectCount(
                new LambdaQueryWrapper<Requirement>()
                        .eq(Requirement::getParentId, r.getId())
                        .eq(Requirement::getDeleted, 0));
        dto.setChildrenCount(childCount != null ? childCount.intValue() : 0);
        dto.setSprintId(r.getSprintId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setSource(r.getSource());
        dto.setPriority(r.getPriority());
        dto.setStatus(r.getStatus());
        dto.setStatusLabel(RequirementStateMachine.STATUS_LABELS.getOrDefault(r.getStatus(), r.getStatus()));
        dto.setAssigneeId(r.getAssigneeId());
        dto.setEstimatedHours(r.getEstimatedHours());
        dto.setActualHours(r.getActualHours());
        dto.setActualStartAt(r.getActualStartAt());
        dto.setActualEndAt(r.getActualEndAt());
        dto.setVerificationScenario(r.getVerificationScenario());
        dto.setVerificationSteps(r.getVerificationSteps());
        dto.setVerificationResult(r.getVerificationResult());
        dto.setAcceptanceCriteria(r.getAcceptanceCriteria());
        dto.setStartDate(r.getStartDate());
        dto.setDueDate(r.getDueDate());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        if (r.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(r.getAssigneeId());
            if (assignee != null) dto.setAssigneeName(assignee.getNickname());
        }
        return dto;
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberMapper.selectOne(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    private boolean isProjectManager(ProjectMember membership) {
        return membership != null && "PROJECT_ADMIN".equalsIgnoreCase(membership.getRole());
    }

    private void ensureRequirementReadable(Requirement requirement) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(requirement.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("无权查看该需求");
        }
        if (isProjectManager(membership) || Objects.equals(currentUserId, requirement.getAssigneeId())) {
            return;
        }
        throw BizException.forbidden("无权查看该需求");
    }

    private void ensureRequirementEditable(Requirement requirement) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(requirement.getProjectId(), currentUserId);
        if (membership == null) {
            throw BizException.forbidden("仅项目经理或需求负责人可编辑");
        }
        if (isProjectManager(membership) || Objects.equals(currentUserId, requirement.getAssigneeId())) {
            return;
        }
        throw BizException.forbidden("仅项目经理或需求负责人可编辑");
    }

    private void ensureRequirementManageable(Requirement requirement) {
        if (SecurityUtil.isSuperAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ProjectMember membership = findMembership(requirement.getProjectId(), currentUserId);
        if (!isProjectManager(membership)) {
            throw BizException.forbidden("仅项目经理可编辑需求详情");
        }
    }

    private BigDecimal resolveDoneActualHours(LocalDateTime actualStartAt, LocalDateTime actualEndAt) {
        if (actualStartAt == null || actualEndAt == null) {
            throw BizException.of("完成需求时必须填写开始时间和结束时间");
        }
        if (!actualEndAt.isAfter(actualStartAt)) {
            throw BizException.of("结束时间必须晚于开始时间");
        }

        long minutes = Duration.between(actualStartAt, actualEndAt).toMinutes();
        if (minutes <= 0) {
            throw BizException.of("工时计算失败，请检查时间范围");
        }

        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String requireDoneVerificationField(String value, String fieldLabel) {
        if (!StringUtils.hasText(value)) {
            throw BizException.of("完成需求时必须填写" + fieldLabel);
        }
        return value.trim();
    }
}
