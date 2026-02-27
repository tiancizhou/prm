package com.prm.module.requirement.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementMapper requirementMapper;
    private final RequirementReviewMapper reviewMapper;
    private final RequirementStateMachine stateMachine;
    private final SysUserMapper userMapper;

    public IPage<RequirementDTO> page(int pageNum, int pageSize, Long projectId, String status, String keyword,
                                       Long assigneeId, Long sprintId, Boolean unscheduled,
                                       LocalDate dueDateFrom, LocalDate dueDateTo) {
        Page<Requirement> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Requirement> wrapper = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getDeleted, 0)
                .orderByDesc(Requirement::getCreatedAt);
        if (projectId != null) wrapper.eq(Requirement::getProjectId, projectId);
        if (StringUtils.hasText(status)) wrapper.eq(Requirement::getStatus, status);
        if (StringUtils.hasText(keyword)) wrapper.like(Requirement::getTitle, keyword);
        if (assigneeId != null) wrapper.eq(Requirement::getAssigneeId, assigneeId);
        if (Boolean.TRUE.equals(unscheduled)) wrapper.and(w -> w.isNull(Requirement::getSprintId));
        else if (sprintId != null) wrapper.eq(Requirement::getSprintId, sprintId);
        if (dueDateFrom != null) wrapper.ge(Requirement::getDueDate, dueDateFrom);
        if (dueDateTo != null)   wrapper.le(Requirement::getDueDate, dueDateTo);
        // 非管理者只能看分配给自己的需求
        if (!SecurityUtil.isManager()) {
            wrapper.eq(Requirement::getAssigneeId, SecurityUtil.getCurrentUserId());
        }
        IPage<Requirement> rPage = requirementMapper.selectPage(pageReq, wrapper);
        return rPage.convert(this::toDTO);
    }

    @Transactional
    public RequirementDTO create(CreateRequirementRequest request) {
        if (!SecurityUtil.isManager()) throw BizException.forbidden("只有项目经理或超级管理员才能创建需求");
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Requirement r = new Requirement();
        r.setProjectId(request.getProjectId());
        r.setSprintId(request.getSprintId());
        r.setTitle(request.getTitle());
        r.setDescription(request.getDescription());
        r.setSource(request.getSource());
        r.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        r.setStatus("DRAFT");
        r.setAssigneeId(request.getAssigneeId());
        r.setEstimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : BigDecimal.ZERO);
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
        // 非管理者只能查看分配给自己的需求
        if (!SecurityUtil.isManager()) {
            if (r.getAssigneeId() == null || !r.getAssigneeId().equals(SecurityUtil.getCurrentUserId())) {
                throw BizException.forbidden("无权查看该需求");
            }
        }
        return toDTO(r);
    }

    @Transactional
    public RequirementDTO update(Long id, UpdateRequirementRequest request) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null || r.getDeleted() == 1) throw BizException.notFound("需求");
        // 管理员可编辑任意需求；负责人只能编辑分配给自己的需求
        if (!SecurityUtil.isManager()) {
            if (r.getAssigneeId() == null || !r.getAssigneeId().equals(SecurityUtil.getCurrentUserId())) {
                throw BizException.forbidden("只能编辑分配给自己的需求");
            }
        }
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
    public RequirementDTO updateStatus(Long id, String newStatus) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw BizException.notFound("需求");
        // 管理员可变更任意需求状态；负责人只能变更分配给自己的需求状态
        if (!SecurityUtil.isManager()) {
            if (r.getAssigneeId() == null || !r.getAssigneeId().equals(SecurityUtil.getCurrentUserId())) {
                throw BizException.forbidden("只能变更分配给自己的需求状态");
            }
        }
        stateMachine.transit(r.getStatus(), newStatus);
        r.setStatus(newStatus);
        r.setUpdatedBy(SecurityUtil.getCurrentUserId());
        requirementMapper.updateById(r);
        return toDTO(r);
    }

    @Transactional
    public void addReview(Long requirementId, String conclusion, String remark) {
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
        dto.setSprintId(r.getSprintId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setSource(r.getSource());
        dto.setPriority(r.getPriority());
        dto.setStatus(r.getStatus());
        dto.setStatusLabel(RequirementStateMachine.STATUS_LABELS.getOrDefault(r.getStatus(), r.getStatus()));
        dto.setAssigneeId(r.getAssigneeId());
        dto.setEstimatedHours(r.getEstimatedHours());
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
}
