package com.prm.module.bug;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.bug.application.BugService;
import com.prm.module.bug.domain.BugStateMachine;
import com.prm.module.bug.dto.BugDTO;
import com.prm.module.bug.dto.CreateBugRequest;
import com.prm.module.bug.entity.Bug;
import com.prm.module.bug.mapper.BugCommentMapper;
import com.prm.module.bug.mapper.BugMapper;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BugServicePermissionTests {

    @Mock
    private BugMapper bugMapper;
    @Mock
    private BugCommentMapper commentMapper;
    @Mock
    private BugStateMachine stateMachine;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;

    private BugService bugService;

    @BeforeEach
    void setUp() {
        bugService = new BugService(
                bugMapper,
                commentMapper,
                stateMachine,
                userMapper,
                projectMemberMapper
        );

        lenient().when(bugMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(new Page<Bug>(1, 20));
    }

    @Test
    void memberInProjectShouldOnlySeeAssignedBugs() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member(1001L, 3001L, "DEV")));

            bugService.page(1, 20, 1001L, null, null, null);

            verify(projectMemberMapper).selectList(any(LambdaQueryWrapper.class));
            verify(bugMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void projectAdminInProjectShouldSeeAllProjectBugs() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member(1001L, 3001L, "PROJECT_ADMIN")));

            bugService.page(1, 20, 1001L, null, null, null);

            ArgumentCaptor<LambdaQueryWrapper<Bug>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
            verify(bugMapper).selectPage(any(Page.class), captor.capture());
            assertThat(captor.getValue().getParamNameValuePairs().values()).doesNotContain(3001L);
        }
    }

    @Test
    void userNotInProjectShouldGetEmptyResultWithoutQueryingBugs() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(member(1002L, 3001L, "DEV")));

            IPage<BugDTO> page = bugService.page(1, 20, 1001L, null, null, null);

            assertThat(page.getRecords()).isEmpty();
            verify(bugMapper, never()).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void mixedMembershipWithoutProjectIdShouldKeepManagerAndMemberScopes() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                    member(1001L, 3001L, "PROJECT_ADMIN"),
                    member(1002L, 3001L, "DEV")
            ));

            bugService.page(1, 20, null, null, null, null);

            verify(projectMemberMapper).selectList(any(LambdaQueryWrapper.class));
            verify(bugMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void getByIdShouldDenyMemberWhenBugNotAssignedToCurrentUser() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(bugMapper.selectById(9001L)).thenReturn(bug(9001L, 1001L, 4001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "DEV"));

            assertThatThrownBy(() -> bugService.getById(9001L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("分配给自己");
        }
    }

    @Test
    void assignShouldDenyNonProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(bugMapper.selectById(9001L)).thenReturn(bug(9001L, 1001L, 4001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "DEV"));

            assertThatThrownBy(() -> bugService.assign(9001L, 3002L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("项目经理");
        }
    }

    @Test
    void assignShouldAllowProjectManager() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(bugMapper.selectById(9001L)).thenReturn(bug(9001L, 1001L, 4001L));
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member(1001L, 3001L, "PROJECT_ADMIN"));

            assertThatCode(() -> bugService.assign(9001L, 3002L)).doesNotThrowAnyException();
        }
    }

    @Test
    void createShouldDenyUserOutsideProject() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(3001L);
            when(projectMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            CreateBugRequest request = new CreateBugRequest();
            request.setProjectId(1001L);
            request.setTitle("bug");

            assertThatThrownBy(() -> bugService.create(request))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("提交 Bug");
        }
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        return member;
    }

    private Bug bug(Long id, Long projectId, Long assigneeId) {
        Bug bug = new Bug();
        bug.setId(id);
        bug.setProjectId(projectId);
        bug.setAssigneeId(assigneeId);
        bug.setStatus("ASSIGNED");
        bug.setDeleted(0);
        return bug;
    }
}
