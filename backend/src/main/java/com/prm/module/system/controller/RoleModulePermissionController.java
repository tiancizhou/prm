package com.prm.module.system.controller;

import com.prm.common.result.R;
import com.prm.module.system.application.RoleModulePermissionService;
import com.prm.module.system.dto.ModulePermissionDTO;
import com.prm.module.system.dto.SaveRoleModulePermissionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "模块权限分配")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RoleModulePermissionController {

    private final RoleModulePermissionService roleModulePermissionService;

    @Operation(summary = "模块权限列表")
    @GetMapping("/permissions/modules")
    public R<List<ModulePermissionDTO>> listModules() {
        return R.ok(roleModulePermissionService.listModulePermissions());
    }

    @Operation(summary = "角色已分配的模块权限")
    @GetMapping("/roles/{id}/module-permissions")
    public R<List<String>> getRoleModulePermissions(@PathVariable Long id) {
        return R.ok(roleModulePermissionService.getRoleModulePermissionCodes(id));
    }

    @Operation(summary = "保存角色模块权限")
    @PutMapping("/roles/{id}/module-permissions")
    public R<List<String>> saveRoleModulePermissions(@PathVariable Long id, @RequestBody SaveRoleModulePermissionsRequest request) {
        return R.ok(roleModulePermissionService.saveRoleModulePermissions(id, request));
    }
}
