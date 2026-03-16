package com.prm.module.sprint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.sprint.application.SprintService;
import com.prm.module.sprint.domain.SprintStateMachine;
import com.prm.module.sprint.dto.CreateSprintRequest;
import com.prm.module.sprint.entity.Sprint;
import com.prm.module.sprint.mapper.SprintMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServicePermissionTests {

    @Mock
    private SprintMapper sprintMapper;
    @Mock
    private SprintStateMachine stateMachine;
    @Mock
    private BugMapper bugMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private SprintService sprintService;

    @BeforeEach
    void setUp() {
        lenient().when(sprintMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<Sprint>(1, 20));
        lenient().when(bugMapper.countOpenCriticalInSprint(any(Long.class))).thenReturn(0);
    }

    @Test
    void createShouldDenyProjectMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> sprintService.create(createRequest(1001L, "Sprint-1")))
                    .isInstanceOf(BizException.class);

            verify(sprintMapper, never()).insert(any(Sprint.class));
        }
    }

    @Test
    void createShouldAllowProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);

            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "MEMBER"));

            assertThatCode(() -> sprintService.create(createRequest(1001L, "Sprint-1")))
                    .doesNotThrowAnyException();

            verify(sprintMapper).insert(any(Sprint.class));
        }
    }

    @Test
    void getByIdShouldDenyUserOutsideProject() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(sprintMapper.selectById(9001L)).thenReturn(sprint(9001L, 1001L, "PLANNING"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> sprintService.getById(9001L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Test
    void pageShouldReturnEmptyForNonMemberWithoutQueryingSprints() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThat(sprintService.page(1, 20, 1001L).getRecords()).isEmpty();
            verify(sprintMapper, never()).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void startShouldDenyProjectMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);

            when(sprintMapper.selectById(9001L)).thenReturn(sprint(9001L, 1001L, "PLANNING"));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> sprintService.start(9001L))
                    .isInstanceOf(BizException.class);

            verify(sprintMapper, never()).updateById(any(Sprint.class));
        }
    }

    @Test
    void closeShouldAllowProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);

            when(sprintMapper.selectById(9001L)).thenReturn(sprint(9001L, 1001L, "ACTIVE"));
            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "MEMBER"));

            assertThatCode(() -> sprintService.close(9001L)).doesNotThrowAnyException();

            verify(sprintMapper).updateById(any(Sprint.class));
        }
    }

    private CreateSprintRequest createRequest(Long projectId, String name) {
        CreateSprintRequest request = new CreateSprintRequest();
        request.setProjectId(projectId);
        request.setName(name);
        request.setCapacityHours(BigDecimal.TEN);
        return request;
    }

    private Sprint sprint(Long id, Long projectId, String status) {
        Sprint sprint = new Sprint();
        sprint.setId(id);
        sprint.setProjectId(projectId);
        sprint.setStatus(status);
        sprint.setDeleted(0);
        return sprint;
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }
}
