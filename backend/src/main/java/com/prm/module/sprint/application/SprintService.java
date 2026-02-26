package com.prm.module.sprint.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.mapper.BugMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintMapper sprintMapper;
    private final SprintStateMachine stateMachine;
    private final BugMapper bugMapper;

    public IPage<SprintDTO> page(int pageNum, int pageSize, Long projectId) {
        Page<Sprint> pageReq = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Sprint> wrapper = new LambdaQueryWrapper<Sprint>()
                .eq(Sprint::getDeleted, 0)
                .orderByDesc(Sprint::getCreatedAt);
        if (projectId != null) wrapper.eq(Sprint::getProjectId, projectId);
        return sprintMapper.selectPage(pageReq, wrapper).convert(this::toDTO);
    }

    @Transactional
    public SprintDTO create(CreateSprintRequest request) {
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
        if (sprint == null || sprint.getDeleted() == 1) throw BizException.notFound("迭代");
        return toDTO(sprint);
    }

    @Transactional
    public SprintDTO start(Long id) {
        Sprint sprint = sprintMapper.selectById(id);
        if (sprint == null) throw BizException.notFound("迭代");
        stateMachine.transit(sprint.getStatus(), "ACTIVE");
        sprint.setStatus("ACTIVE");
        sprint.setUpdatedBy(SecurityUtil.getCurrentUserId());
        sprintMapper.updateById(sprint);
        return toDTO(sprint);
    }

    @Transactional
    public SprintDTO close(Long id) {
        Sprint sprint = sprintMapper.selectById(id);
        if (sprint == null) throw BizException.notFound("迭代");
        stateMachine.transit(sprint.getStatus(), "CLOSED");

        int openCritical = bugMapper.countOpenCriticalInSprint(id);
        if (openCritical > 0) {
            throw BizException.of("存在 " + openCritical + " 个未关闭的严重/阻塞缺陷，无法关闭迭代");
        }

        sprint.setStatus("CLOSED");
        sprint.setClosedAt(LocalDateTime.now());
        sprint.setUpdatedBy(SecurityUtil.getCurrentUserId());
        sprintMapper.updateById(sprint);
        return toDTO(sprint);
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
