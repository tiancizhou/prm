package com.prm.module.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.prm.module.auth.application.AuthService;
import com.prm.module.auth.dto.LoginRequest;
import com.prm.module.auth.dto.LoginResponse;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePermissionTests {

    @Mock
    private SysUserMapper sysUserMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(sysUserMapper);
    }

    @Test
    void loginShouldIncludePermissionCodes() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("Admin");
        user.setStatus("ACTIVE");
        user.setPassword(BCrypt.hashpw("Admin@123"));

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        when(sysUserMapper.selectByUsernameWithPassword("admin")).thenReturn(user);
        when(sysUserMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("SUPER_ADMIN"));
        when(sysUserMapper.selectPermissionCodesByUserId(1L)).thenReturn(List.of("dashboard:view", "admin:view"));

        try (MockedStatic<StpUtil> stpUtil = Mockito.mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getTokenName()).thenReturn("Authorization");
            stpUtil.when(() -> StpUtil.getTokenValue()).thenReturn("token-1");

            LoginResponse response = authService.login(request);

            assertThat(response.getPermissions()).containsExactly("dashboard:view", "admin:view");
        }
    }

    @Test
    void currentUserShouldIncludePermissionCodes() {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("dev01");
        user.setNickname("Dev");

        when(sysUserMapper.selectById(2L)).thenReturn(user);
        when(sysUserMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("DEV"));
        when(sysUserMapper.selectPermissionCodesByUserId(2L)).thenReturn(List.of("projects:view", "requirement:view"));

        try (MockedStatic<StpUtil> stpUtil = Mockito.mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(2L);

            LoginResponse response = authService.currentUser();

            assertThat(response.getPermissions()).containsExactly("projects:view", "requirement:view");
        }
    }
}
