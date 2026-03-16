package com.prm.module.system;

import com.prm.common.exception.BizException;
import com.prm.common.util.SecurityUtil;
import com.prm.module.project.mapper.ProjectMapper;
import com.prm.module.project.mapper.ProjectMemberMapper;
import com.prm.module.system.application.UserService;
import com.prm.module.system.entity.SysDepartment;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysDepartmentMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceDeleteTests {

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
    void deleteShouldSoftDeleteUserAndCleanupRoles() {
        SysUser user = new SysUser();
        user.setId(1001L);
        user.setDeleted(0);
        when(userMapper.selectById(1001L)).thenReturn(user);

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserIdOrNull).thenReturn(2001L);

            assertThatCode(() -> userService.delete(1001L)).doesNotThrowAnyException();

            verify(userMapper).updateById(any(SysUser.class));
            verify(userRoleMapper).delete(any());
        }
    }

    @Test
    void deleteShouldRejectDeletingCurrentUser() {
        SysUser user = new SysUser();
        user.setId(1001L);
        user.setDeleted(0);
        when(userMapper.selectById(1001L)).thenReturn(user);

        try (MockedStatic<SecurityUtil> securityUtil = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUserIdOrNull).thenReturn(1001L);

            assertThatThrownBy(() -> userService.delete(1001L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Test
    void deleteShouldRejectMissingUser() {
        when(userMapper.selectById(1001L)).thenReturn(null);

        assertThatThrownBy(() -> userService.delete(1001L))
                .isInstanceOf(BizException.class);
    }
}
