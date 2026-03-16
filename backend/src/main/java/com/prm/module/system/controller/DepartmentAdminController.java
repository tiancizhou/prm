package com.prm.module.system.controller;

import com.prm.common.result.R;
import com.prm.module.system.application.DepartmentService;
import com.prm.module.system.dto.DepartmentDetailDTO;
import com.prm.module.system.dto.DepartmentTreeDTO;
import com.prm.module.system.dto.SaveDepartmentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class DepartmentAdminController {

    private final DepartmentService departmentService;

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public R<List<DepartmentTreeDTO>> tree() {
        return R.ok(departmentService.listTree());
    }

    @Operation(summary = "部门详情")
    @GetMapping("/{id}")
    public R<DepartmentDetailDTO> getById(@PathVariable Long id) {
        return R.ok(departmentService.getById(id));
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public R<DepartmentDetailDTO> create(@Valid @RequestBody SaveDepartmentRequest request) {
        return R.ok(departmentService.create(request));
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public R<DepartmentDetailDTO> update(@PathVariable Long id, @Valid @RequestBody SaveDepartmentRequest request) {
        return R.ok(departmentService.update(id, request));
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return R.ok();
    }
}
