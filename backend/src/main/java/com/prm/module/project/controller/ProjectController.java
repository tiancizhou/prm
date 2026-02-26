package com.prm.module.project.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.project.application.ProjectService;
import com.prm.module.project.dto.CreateProjectRequest;
import com.prm.module.project.dto.ProjectDTO;
import com.prm.module.project.dto.ProjectMemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "项目分页列表")
    @GetMapping
    public R<PageResult<ProjectDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(projectService.page(page, size, keyword, status)));
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public R<ProjectDTO> create(@Valid @RequestBody CreateProjectRequest request) {
        return R.ok(projectService.create(request));
    }

    @Operation(summary = "项目详情")
    @GetMapping("/{id}")
    public R<ProjectDTO> getById(@PathVariable Long id) {
        return R.ok(projectService.getById(id));
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public R<ProjectDTO> update(@PathVariable Long id, @Valid @RequestBody CreateProjectRequest request) {
        return R.ok(projectService.update(id, request));
    }

    @Operation(summary = "归档项目")
    @PostMapping("/{id}/archive")
    public R<Void> archive(@PathVariable Long id) {
        projectService.archive(id);
        return R.ok();
    }

    @Operation(summary = "关闭项目")
    @PostMapping("/{id}/close")
    public R<Void> close(@PathVariable Long id) {
        projectService.close(id);
        return R.ok();
    }

    @Operation(summary = "项目成员列表（含昵称，用于指派等）")
    @GetMapping("/{id}/members")
    public R<List<ProjectMemberVO>> getMembers(@PathVariable Long id) {
        return R.ok(projectService.getMemberVOs(id));
    }

    @Operation(summary = "添加项目成员")
    @PostMapping("/{id}/members")
    public R<Void> addMember(@PathVariable Long id, @RequestParam Long userId) {
        projectService.addMember(id, userId);
        return R.ok();
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{id}/members/{userId}")
    public R<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return R.ok();
    }
}
