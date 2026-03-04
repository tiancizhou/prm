package com.prm.module.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.entity.Bug;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.dashboard.application.DashboardService;
import com.prm.module.dashboard.dto.OverviewDTO;
import com.prm.module.dashboard.mapper.DashboardSnapshotMapper;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.requirement.entity.Requirement;
import com.prm.module.requirement.mapper.RequirementMapper;
import com.prm.module.task.entity.Task;
import com.prm.module.task.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private RequirementMapper requirementMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private BugMapper bugMapper;
    @Mock
    private DashboardSnapshotMapper snapshotMapper;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                projectMapper,
                projectMemberMapper,
                requirementMapper,
                taskMapper,
                bugMapper,
                snapshotMapper,
                new ObjectMapper()
        );

        lenient().when(projectMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        lenient().when(requirementMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        lenient().when(taskMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        lenient().when(bugMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
    }

    @Test
    void overviewWithProjectIdShouldScopeProjectCounts() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(true);

            dashboardService.getOverview(1001L);

            verify(projectMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
            verify(requirementMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
            verify(taskMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
            verify(bugMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
            verify(projectMemberMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void nonSuperAdminWithoutAnyProjectMembershipShouldReturnZeroOverview() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of());

            OverviewDTO overview = dashboardService.getOverview(null);

            assertThat(overview.getTotalProjects()).isZero();
            assertThat(overview.getActiveProjects()).isZero();
            assertThat(overview.getOverdueProjects()).isZero();
            assertThat(overview.getTotalRequirements()).isZero();
            assertThat(overview.getDoneRequirements()).isZero();
            assertThat(overview.getTotalTasks()).isZero();
            assertThat(overview.getInProgressTasks()).isZero();
            assertThat(overview.getOverdueTasks()).isZero();
            assertThat(overview.getTotalBugs()).isZero();
            assertThat(overview.getOpenBugs()).isZero();
            assertThat(overview.getCriticalOpenBugs()).isZero();

            verify(projectMapper, never()).selectCount(any(LambdaQueryWrapper.class));
            verify(requirementMapper, never()).selectCount(any(LambdaQueryWrapper.class));
            verify(taskMapper, never()).selectCount(any(LambdaQueryWrapper.class));
            verify(bugMapper, never()).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void memberViewShouldApplyCurrentAssigneeFilter() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(8888L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                    List.of(member(1001L, 8888L, "DEV")),
                    List.of()
            );

            dashboardService.getOverview(null);

            verify(projectMemberMapper, times(2)).selectList(any(LambdaQueryWrapper.class));
            verify(requirementMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
            verify(taskMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
            verify(bugMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void projectAdminViewShouldNotApplyAssigneeFilter() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(8888L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(
                    List.of(member(1001L, 8888L, "PROJECT_ADMIN")),
                    List.of(member(1001L, 8888L, "PROJECT_ADMIN"))
            );

            dashboardService.getOverview(null);

            verify(projectMemberMapper, times(2)).selectList(any(LambdaQueryWrapper.class));
            verify(requirementMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
            verify(taskMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
            verify(bugMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void aggregatePermissionShouldDenyNonAdminUser() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> dashboardService.requireAggregatePermission(1001L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("项目经理");
        }
    }

    @Test
    void aggregatePermissionShouldAllowProjectAdminMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "PROJECT_ADMIN"));

            assertThatCode(() -> dashboardService.requireAggregatePermission(1001L)).doesNotThrowAnyException();
        }
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }
}
