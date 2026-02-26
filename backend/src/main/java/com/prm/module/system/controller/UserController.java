package com.prm.module.system.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.system.application.UserService;
import com.prm.module.system.dto.CreateUserRequest;
import com.prm.module.system.dto.UpdateUserRequest;
import com.prm.module.system.dto.UserDTO;
import com.prm.module.system.entity.SysRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "角色列表（用于下拉选择）")
    @GetMapping("/roles")
    public R<List<SysRole>> listRoles() {
        return R.ok(userService.listAllRoles());
    }

    @Operation(summary = "用户分页列表")
    @GetMapping
    public R<PageResult<UserDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(userService.page(page, size, keyword)));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public R<UserDTO> create(@Valid @RequestBody CreateUserRequest request) {
        return R.ok(userService.create(request));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public R<UserDTO> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public R<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return R.ok(userService.update(id, request));
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return R.ok();
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles/{roleId}")
    public R<Void> assignRole(@PathVariable Long id, @PathVariable Long roleId) {
        userService.assignRole(id, roleId);
        return R.ok();
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        userService.changeStatus(id, status);
        return R.ok();
    }
}
