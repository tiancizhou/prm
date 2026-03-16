package com.prm.module.project;

import com.prm.common.util.SecurityUtil;
import com.prm.module.project.application.ProjectService;
import com.prm.module.project.dto.ProjectMemberVO;
import com.prm.module.project.entity.ProjectMember;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.entity.SysRole;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceMemberViewTests {

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
    void getMemberVOsShouldExposeJoinedAt() {
        LocalDateTime joinedAt = LocalDateTime.of(2026, 3, 13, 10, 30);

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isSuperAdmin).thenReturn(false);
            securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2001L);
            when(memberMapper.selectOne(any())).thenReturn(requesterMembership(1001L, 2001L));
            when(memberMapper.selectList(any())).thenReturn(List.of(projectMembership(1001L, 3001L, joinedAt)));
            when(userMapper.selectBatchIds(List.of(3001L))).thenReturn(List.of(user(3001L, "alice")));
            when(userMapper.selectRoleCodesByUserId(3001L)).thenReturn(List.of("DEV"));
            when(roleMapper.selectList(any())).thenReturn(List.of(role("DEV", "开发")));

            List<ProjectMemberVO> result = projectService.getMemberVOs(1001L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getJoinedAt()).isEqualTo(joinedAt);
        }
    }

    private ProjectMember requesterMembership(Long projectId, Long userId) {
        ProjectMember membership = new ProjectMember();
        membership.setProjectId(projectId);
        membership.setUserId(userId);
        membership.setRole("MEMBER");
        return membership;
    }

    private ProjectMember projectMembership(Long projectId, Long userId, LocalDateTime joinedAt) {
        ProjectMember membership = new ProjectMember();
        membership.setProjectId(projectId);
        membership.setUserId(userId);
        membership.setRole("MEMBER");
        membership.setCreatedAt(joinedAt);
        return membership;
    }

    private SysUser user(Long id, String username) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(username);
        user.setRealName("Alice");
        user.setEmployeeNo("E-001");
        return user;
    }

    private SysRole role(String code, String name) {
        SysRole role = new SysRole();
        role.setCode(code);
        role.setName(name);
        role.setDeleted(0);
        return role;
    }
}
