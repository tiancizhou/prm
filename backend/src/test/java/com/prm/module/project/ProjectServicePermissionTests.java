package com.prm.module.project;

import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.application.ProjectService;
import com.prm.module.project.dto.ProjectDTO;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServicePermissionTests {

    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ProjectMemberMapper memberMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysRoleMapper roleMapper;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectMapper, memberMapper, userMapper, roleMapper);
    }

    @Test
    void projectMemberShouldNotAddMemberEvenWithSystemProjectAdminRole() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);
            when(memberMapper.selectOne(any())).thenReturn(member(1001L, 2001L, "DEV"));

            assertThatThrownBy(() -> projectService.addMember(1001L, 3001L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("编辑权限");

            verify(memberMapper, never()).insert(any(ProjectMember.class));
        }
    }

    @Test
    void projectManagerShouldAddMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            when(memberMapper.selectOne(any())).thenReturn(member(1001L, 2001L, "PROJECT_ADMIN"));
            when(memberMapper.selectCount(any())).thenReturn(0L);

            assertThatCode(() -> projectService.addMember(1001L, 3001L)).doesNotThrowAnyException();

            verify(memberMapper).insert(any(ProjectMember.class));
        }
    }

    @Test
    void getByIdShouldMarkCanEditFalseForNormalMember() {
        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            securityUtil.when(() -> SecurityUtil.hasRole("PROJECT_ADMIN")).thenReturn(true);

            Project project = new Project();
            project.setId(1001L);
            project.setDeleted(0);
            project.setOwnerId(5001L);
            when(projectMapper.selectById(1001L)).thenReturn(project);
            when(memberMapper.selectOne(any())).thenReturn(member(1001L, 2001L, "DEV"));

            SysUser owner = new SysUser();
            owner.setId(5001L);
            owner.setNickname("owner");
            when(userMapper.selectById(5001L)).thenReturn(owner);

            ProjectDTO dto = projectService.getById(1001L);

            assertThat(dto.getCanEdit()).isFalse();
        }
    }

    private ProjectMember member(Long projectId, Long userId, String role) {
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(projectId);
        projectMember.setUserId(userId);
        projectMember.setRole(role);
        return projectMember;
    }
}
