package com.prm.module.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prm.module.system.application.RoleModulePermissionService;
import com.prm.module.system.dto.ModulePermissionDTO;
import com.prm.module.system.dto.SaveRoleModulePermissionsRequest;
import com.prm.module.system.entity.SysPermission;
import com.prm.module.system.entity.SysRole;
import com.prm.module.system.entity.SysRolePermission;
import com.prm.module.system.mapper.SysPermissionMapper;
import com.prm.module.system.mapper.SysRoleMapper;
import com.prm.module.system.mapper.SysRolePermissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleModulePermissionServiceTests {

    @Mock
    private SysPermissionMapper permissionMapper;
    @Mock
    private SysRolePermissionMapper rolePermissionMapper;
    @Mock
    private SysRoleMapper roleMapper;

    private RoleModulePermissionService service;

    @BeforeEach
    void setUp() {
        service = new RoleModulePermissionService(permissionMapper, rolePermissionMapper, roleMapper);
    }

    @Test
    void listModulePermissionsShouldIncludeGroupedActionChildren() {
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                permission(10L, "Requirements", "requirement:view", "MODULE", 0L, 10),
                permission(11L, "Create Requirement", "requirement:create", "ACTION", 10L, 11),
                permission(12L, "Update Requirement", "requirement:update", "ACTION", 10L, 12)
        ));

        List<ModulePermissionDTO> permissions = service.listModulePermissions();

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getCode()).isEqualTo("requirement:view");
        assertThat(permissions.get(0).getChildren()).hasSize(2);
        assertThat(permissions.get(0).getChildren()).extracting(ModulePermissionDTO::getCode)
                .containsExactly("requirement:create", "requirement:update");
    }

    @Test
    void saveRoleModulePermissionsShouldAutoIncludeParentModuleWhenActionSelected() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L));
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                permission(10L, "Requirements", "requirement:view", "MODULE", 0L, 10),
                permission(11L, "Create Requirement", "requirement:create", "ACTION", 10L, 11)
        ));

        SaveRoleModulePermissionsRequest request = new SaveRoleModulePermissionsRequest();
        request.setPermissionCodes(List.of("requirement:create"));

        List<String> savedCodes = service.saveRoleModulePermissions(1L, request);

        verify(rolePermissionMapper).delete(any(LambdaQueryWrapper.class));
        verify(rolePermissionMapper, times(2)).insert(any(SysRolePermission.class));
        assertThat(savedCodes).containsExactly("requirement:view", "requirement:create");
    }

    @Test
    void saveRoleModulePermissionsShouldDeleteOnlyModuleAndActionAssignments() {
        when(roleMapper.selectById(1L)).thenReturn(role(1L));
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                permission(1L, "Dashboard", "dashboard:view", "MODULE", 0L, 10),
                permission(2L, "Create User", "user:create", "ACTION", 1L, 11)
        ));

        SaveRoleModulePermissionsRequest request = new SaveRoleModulePermissionsRequest();
        request.setPermissionCodes(List.of());

        service.saveRoleModulePermissions(1L, request);

        ArgumentCaptor<LambdaQueryWrapper<SysRolePermission>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(rolePermissionMapper).delete(captor.capture());
        assertThat(captor.getValue().getParamNameValuePairs().values()).contains(1L, 2L);
    }

    private SysRole role(Long id) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setDeleted(0);
        return role;
    }

    private SysPermission permission(Long id, String name, String code, String type, Long parentId, Integer sort) {
        SysPermission permission = new SysPermission();
        permission.setId(id);
        permission.setName(name);
        permission.setCode(code);
        permission.setType(type);
        permission.setParentId(parentId);
        permission.setSort(sort);
        permission.setDeleted(0);
        return permission;
    }
}
