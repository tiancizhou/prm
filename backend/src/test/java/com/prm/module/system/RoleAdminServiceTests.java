package com.prm.module.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.common.exception.BizException;
import com.prm.module.system.application.RoleAdminService;
import com.prm.module.system.dto.RoleGroupDetailDTO;
import com.prm.module.system.dto.RoleGroupRowDTO;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysUser;
import com.prm.module.system.entity.SysUserRole;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysUserMapper;
import com.prm.module.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAdminServiceTests {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    private RoleAdminService roleAdminService;

    @BeforeEach
    void setUp() {
        roleAdminService = new RoleAdminService(roleMapper, userMapper, userRoleMapper);
    }

    @Test
    void listRoleGroupsShouldFallbackDescriptionAndResolveMembers() {
        when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(role(1L, "研发", "DEV", null)));
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userRole(101L, 1L), userRole(102L, 1L)));
        when(userMapper.selectBatchIds(List.of(101L, 102L))).thenReturn(List.of(
                user(101L, "dev01", "张三"),
                user(102L, "dev02", null)
        ));

        List<RoleGroupRowDTO> rows = roleAdminService.listRoleGroups();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getDescription()).isEqualTo("研发");
        assertThat(rows.get(0).getUsers()).contains("张三", "dev02");
        assertThat(rows.get(0).getMemberCount()).isEqualTo(2L);
    }

    @Test
    void getRoleGroupShouldReturnRealMembers() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "管理员", "SUPER_ADMIN", "系统管理员"));
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(userRole(201L, 1L)));
        when(userMapper.selectBatchIds(List.of(201L))).thenReturn(List.of(user(201L, "admin", "管理员")));

        RoleGroupDetailDTO detail = roleAdminService.getRoleGroup(1L);

        assertThat(detail.getName()).isEqualTo("管理员");
        assertThat(detail.getMembers()).hasSize(1);
        assertThat(detail.getMembers().get(0).getDisplayName()).isEqualTo("管理员");
    }

    @Test
    void deleteShouldRejectWhenRoleHasBoundUsers() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "管理员", "SUPER_ADMIN", "系统管理员"));
        when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> roleAdminService.delete(1L))
                .isInstanceOf(BizException.class);
    }

    @Test
    void deleteShouldDeleteRoleWhenUnbound() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L, "开发", "DEV", "开发"));
        when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        roleAdminService.delete(1L);

        verify(roleMapper).deleteById(1L);
    }

    private SysRole role(Long id, String name, String code, String description) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        role.setDeleted(0);
        return role;
    }

    private SysUserRole userRole(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }

    private SysUser user(Long id, String username, String realName) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setDeleted(0);
        return user;
    }
}
