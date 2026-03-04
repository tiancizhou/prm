package com.prm.module.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.requirement.application.RequirementService;
import com.prm.module.requirement.domain.RequirementStateMachine;
import com.prm.module.requirement.dto.CreateRequirementRequest;
import com.prm.module.requirement.dto.RequirementDTO;
import com.prm.module.requirement.dto.UpdateRequirementRequest;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.requirement.mapper.RequirementReviewMapper;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequirementServicePermissionTests {

    @Mock
    private RequirementMapper requirementMapper;
    @Mock
    private RequirementReviewMapper reviewMapper;
    @Mock
    private RequirementStateMachine stateMachine;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    private RequirementService requirementService;

    @BeforeEach
    void setUp() {
        requirementService = new RequirementService(
                requirementMapper,
                reviewMapper,
                stateMachine,
                userMapper,
                projectMemberMapper
        );
    }

    @Test
    void assigneeShouldNotUpdateRequirementDetail() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(requirementMapper.selectById(9001L)).thenReturn(requirement(9001L, 1001L, 2001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> requirementService.update(9001L, updateRequest("R-Updated")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("项目经理");

            verify(requirementMapper, never()).updateById(any(Requirement.class));
        }
    }

    @Test
    void nonAssigneeMemberShouldNotUpdateRequirementDetail() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2002L);

            when(requirementMapper.selectById(9001L)).thenReturn(requirement(9001L, 1001L, 2001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2002L, "DEV"));

            assertThatThrownBy(() -> requirementService.update(9001L, updateRequest("R-Updated")))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("项目经理");

            verify(requirementMapper, never()).updateById(any(Requirement.class));
        }
    }

    @Test
    void projectManagerShouldUpdateRequirementDetail() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);

            when(requirementMapper.selectById(9001L)).thenReturn(requirement(9001L, 1001L, 2001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> requirementService.update(9001L, updateRequest("R-Updated")))
                    .doesNotThrowAnyException();

            verify(requirementMapper).updateById(any(Requirement.class));
        }
    }

    @Test
    void assigneeShouldUpdateStatus() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            Requirement requirement = requirement(9001L, 1001L, 2001L);
            requirement.setStatus("IN_PROGRESS");
            when(requirementMapper.selectById(9001L)).thenReturn(requirement);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatCode(() -> requirementService.updateStatus(
                    9001L,
                    "DONE",
                    LocalDateTime.of(2026, 3, 1, 9, 0),
                    LocalDateTime.of(2026, 3, 1, 15, 30)))
                    .doesNotThrowAnyException();

            verify(requirementMapper).updateById(any(Requirement.class));
        }
    }

    @Test
    void doneWithoutStartOrEndTimeShouldFail() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            Requirement requirement = requirement(9001L, 1001L, 2001L);
            requirement.setStatus("IN_PROGRESS");
            when(requirementMapper.selectById(9001L)).thenReturn(requirement);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> requirementService.updateStatus(9001L, "DONE", null, null))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("时间");
        }
    }

    @Test
    void doneWithTimeRangeShouldCalculateHoursByMinute() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            Requirement requirement = requirement(9001L, 1001L, 2001L);
            requirement.setStatus("IN_PROGRESS");
            when(requirementMapper.selectById(9001L)).thenReturn(requirement);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            RequirementDTO dto = requirementService.updateStatus(
                    9001L,
                    "DONE",
                    LocalDateTime.of(2026, 3, 2, 9, 15),
                    LocalDateTime.of(2026, 3, 2, 11, 45));

            assertThat(dto.getActualHours()).isEqualByComparingTo("2.50");
        }
    }

    @Test
    void createShouldDenyNonProjectManagerMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            CreateRequirementRequest request = new CreateRequirementRequest();
            request.setProjectId(1001L);
            request.setTitle("R-1");
            request.setEstimatedHours(BigDecimal.ONE);

            assertThatThrownBy(() -> requirementService.create(request))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("项目经理");

            verify(requirementMapper, never()).insert(any(Requirement.class));
        }
    }

    private Requirement requirement(Long id, Long projectId, Long assigneeId) {
        Requirement requirement = new Requirement();
        requirement.setId(id);
        requirement.setProjectId(projectId);
        requirement.setAssigneeId(assigneeId);
        requirement.setDeleted(0);
        requirement.setStatus("DRAFT");
        return requirement;
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }

    private UpdateRequirementRequest updateRequest(String title) {
        UpdateRequirementRequest request = new UpdateRequirementRequest();
        request.setTitle(title);
        request.setPriority("MEDIUM");
        request.setEstimatedHours(BigDecimal.ONE);
        return request;
    }
}
