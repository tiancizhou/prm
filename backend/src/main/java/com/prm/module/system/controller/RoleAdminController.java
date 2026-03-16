package com.prm.module.system.controller;

import com.prm.common.result.R;
import com.prm.module.system.application.RoleAdminService;
import com.prm.module.system.dto.RoleGroupDetailDTO;
import com.prm.module.system.dto.RoleGroupRowDTO;
import com.prm.module.system.dto.SaveRoleGroupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "角色分组管理")
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleAdminService roleAdminService;

    @Operation(summary = "角色分组列表")
    @GetMapping
    public R<List<RoleGroupRowDTO>> list() {
        return R.ok(roleAdminService.listRoleGroups());
    }

    @Operation(summary = "角色分组详情")
    @GetMapping("/{id}")
    public R<RoleGroupDetailDTO> get(@PathVariable Long id) {
        return R.ok(roleAdminService.getRoleGroup(id));
    }

    @Operation(summary = "新增角色分组")
    @PostMapping
    public R<RoleGroupDetailDTO> create(@Valid @RequestBody SaveRoleGroupRequest request) {
        return R.ok(roleAdminService.create(request));
    }

    @Operation(summary = "更新角色分组")
    @PutMapping("/{id}")
    public R<RoleGroupDetailDTO> update(@PathVariable Long id, @Valid @RequestBody SaveRoleGroupRequest request) {
        return R.ok(roleAdminService.update(id, request));
    }

    @Operation(summary = "删除角色分组")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleAdminService.delete(id);
        return R.ok();
    }
}
