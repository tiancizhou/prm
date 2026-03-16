package com.prm.module.auth.application;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.auth.dto.LoginRequest;
import com.prm.module.auth.dto.LoginResponse;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsernameWithPassword(request.getUsername());

        if (user == null) {
            throw new BizException(401, "用户名或密码错误");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException(403, "账号已被禁用");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        List<String> roles = sysUserMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(user.getId());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenName(StpUtil.getTokenName())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public void logout() {
        StpUtil.logout();
    }

    public LoginResponse currentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户");
        }
        List<String> roles = sysUserMapper.selectRoleCodesByUserId(userId);
        List<String> permissions = sysUserMapper.selectPermissionCodesByUserId(userId);
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
