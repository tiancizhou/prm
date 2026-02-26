package com.prm.module.bug.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.domain.BugStateMachine;
import com.prm.module.bug.dto.BugDTO;
import com.prm.module.bug.dto.CreateBugRequest;
import com.prm.module.bug.entity.Bug;
import com.prm.module.bug.entity.BugComment;
import com.prm.module.bug.mapper.BugCommentMapper;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BugService {

    private final BugMapper bugMapper;
    private final BugCommentMapper commentMapper;
    private final BugStateMachine stateMachine;
    private final SysUserMapper userMapper;

    public IPage<BugDTO> page(int pageNum, int pageSize, Long projectId, String status, String severity, String keyword) {
        Page<Bug> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Bug> wrapper = new LambdaQueryWrapper<Bug>()
                .eq(Bug::getDeleted, 0)
                .orderByDesc(Bug::getCreatedAt);
        if (projectId != null) wrapper.eq(Bug::getProjectId, projectId);
        if (StringUtils.hasText(status)) wrapper.eq(Bug::getStatus, status);
        if (StringUtils.hasText(severity)) wrapper.eq(Bug::getSeverity, severity);
        if (StringUtils.hasText(keyword)) wrapper.like(Bug::getTitle, keyword);
        return bugMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public BugDTO create(CreateBugRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Bug bug = new Bug();
        bug.setProjectId(request.getProjectId());
        bug.setSprintId(request.getSprintId());
        bug.setTitle(request.getTitle());
        bug.setDescription(request.getDescription());
        bug.setModule(request.getModule());
        bug.setSeverity(request.getSeverity() != null ? request.getSeverity() : "NORMAL");
        bug.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        bug.setStatus("NEW");
        bug.setAssigneeId(request.getAssigneeId());
        bug.setReporterId(currentUserId);
        bug.setSteps(request.getSteps());
        bug.setExpectedResult(request.getExpectedResult());
        bug.setActualResult(request.getActualResult());
        bug.setEnvironment(request.getEnvironment());
        bug.setDeleted(0);
        bug.setCreatedBy(currentUserId);
        bug.setUpdatedBy(currentUserId);
        bugMapper.insert(bug);
        return toDTO(bug);
    }

    public BugDTO getById(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null || bug.getDeleted() == 1) throw BizException.notFound("Bug");
        return toDTO(bug);
    }

    @Transactional
    public BugDTO updateStatus(Long id, String newStatus, String resolveType) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw BizException.notFound("Bug");
        stateMachine.transit(bug.getStatus(), newStatus);
        bug.setStatus(newStatus);
        if ("RESOLVED".equals(newStatus)) {
            bug.setResolveType(resolveType);
            bug.setResolvedAt(LocalDateTime.now());
        } else if ("VERIFIED".equals(newStatus)) {
            bug.setVerifiedAt(LocalDateTime.now());
        }
        bug.setUpdatedBy(SecurityUtil.getCurrentUserId());
        bugMapper.updateById(bug);
        return toDTO(bug);
    }

    @Transactional
    public BugDTO assign(Long id, Long assigneeId) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw BizException.notFound("Bug");
        bug.setAssigneeId(assigneeId);
        bug.setStatus("ASSIGNED");
        bug.setUpdatedBy(SecurityUtil.getCurrentUserId());
        bugMapper.updateById(bug);
        return toDTO(bug);
    }

    @Transactional
    public void addComment(Long bugId, String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        BugComment comment = new BugComment();
        comment.setBugId(bugId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setDeleted(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
    }

    private BugDTO toDTO(Bug b) {
        BugDTO dto = new BugDTO();
        dto.setId(b.getId());
        dto.setProjectId(b.getProjectId());
        dto.setSprintId(b.getSprintId());
        dto.setTitle(b.getTitle());
        dto.setDescription(b.getDescription());
        dto.setModule(b.getModule());
        dto.setSeverity(b.getSeverity());
        dto.setPriority(b.getPriority());
        dto.setStatus(b.getStatus());
        dto.setStatusLabel(BugStateMachine.STATUS_LABELS.getOrDefault(b.getStatus(), b.getStatus()));
        dto.setAssigneeId(b.getAssigneeId());
        dto.setReporterId(b.getReporterId());
        dto.setSteps(b.getSteps());
        dto.setExpectedResult(b.getExpectedResult());
        dto.setActualResult(b.getActualResult());
        dto.setEnvironment(b.getEnvironment());
        dto.setResolveType(b.getResolveType());
        dto.setResolvedAt(b.getResolvedAt());
        dto.setVerifiedAt(b.getVerifiedAt());
        dto.setCreatedAt(b.getCreatedAt());
        if (b.getAssigneeId() != null) {
            SysUser u = userMapper.selectById(b.getAssigneeId());
            if (u != null) dto.setAssigneeName(u.getNickname());
        }
        if (b.getReporterId() != null) {
            SysUser u = userMapper.selectById(b.getReporterId());
            if (u != null) dto.setReporterName(u.getNickname());
        }
        return dto;
    }
}
