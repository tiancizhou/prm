package com.prm.module.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prm.module.project.entity.Project;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.application.UserService;
import com.prm.module.system.dto.UserDTO;
import com.prm.module.system.dto.UserProjectScopeDTO;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.entity.SysUserRole;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceProjectScopeTests {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysDepartmentMapper departmentMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private ProjectMapper projectMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, userRoleMapper, roleMapper, departmentMapper, projectMemberMapper, projectMapper);
    }

    @Test
    void pageShouldPopulateJoinedProjectCount() {
        Page<SysUser> page = new Page<>(1, 20);
        page.setRecords(List.of(user(2001L, "alice"), user(2002L, "bob")));
        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                membership(1001L, 2001L),
                membership(1002L, 2001L),
                membership(1003L, 2002L)
        ));
        when(userMapper.selectRoleCodesByUserId(2001L)).thenReturn(List.of("DEV"));
        when(userMapper.selectRoleCodesByUserId(2002L)).thenReturn(List.of("PROJECT_ADMIN"));
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of());

        Page<UserDTO> result = (Page<UserDTO>) userService.page(1, 20, null, null);

        assertThat(result.getRecords())
                .extracting(UserDTO::getJoinedProjectCount)
                .containsExactly(2L, 1L);
    }

    @Test
    void getProjectScopeShouldReturnJoinedProjects() {
        when(userMapper.selectById(2001L)).thenReturn(user(2001L, "alice"));
        when(userMapper.selectRoleCodesByUserId(2001L)).thenReturn(List.of("PROJECT_ADMIN"));
        when(projectMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                membership(1002L, 2001L),
                membership(1001L, 2001L)
        ));
        when(projectMapper.selectBatchIds(List.of(1002L, 1001L))).thenReturn(List.of(
                project(1001L, "PRM", "项目平台"),
                project(1002L, "OPS", "运维中心")
        ));

        UserProjectScopeDTO result = userService.getProjectScope(2001L);

        assertThat(result.getUserId()).isEqualTo(2001L);
        assertThat(result.getRoles()).containsExactly("PROJECT_ADMIN");
        assertThat(result.getJoinedProjects())
                .extracting(project -> project.getCode())
                .containsExactly("OPS", "PRM");
    }

    private SysUser user(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setRealName(username.toUpperCase());
        user.setDeleted(0);
        return user;
    }

    private ProjectMember membership(Long projectId, Long userId) {
        ProjectMember membership = new ProjectMember();
        membership.setProjectId(projectId);
        membership.setUserId(userId);
        membership.setCreatedAt(LocalDateTime.of(2026, 3, 13, 9, 0));
        return membership;
    }

    private Project project(Long id, String code, String name) {
        Project project = new Project();
        project.setId(id);
        project.setCode(code);
        project.setName(name);
        return project;
    }
}
