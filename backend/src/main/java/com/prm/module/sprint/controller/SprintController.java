package com.prm.module.sprint.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.sprint.application.SprintService;
import com.prm.module.sprint.dto.CreateSprintRequest;
import com.prm.module.sprint.dto.SprintDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "迭代管理")
@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @Operation(summary = "迭代分页列表")
    @GetMapping
    public R<PageResult<SprintDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId) {
        return R.ok(PageResult.of(sprintService.page(page, size, projectId)));
    }

    @Operation(summary = "创建迭代")
    @PostMapping
    public R<SprintDTO> create(@Valid @RequestBody CreateSprintRequest request) {
        return R.ok(sprintService.create(request));
    }

    @Operation(summary = "迭代详情")
    @GetMapping("/{id}")
    public R<SprintDTO> getById(@PathVariable Long id) {
        return R.ok(sprintService.getById(id));
    }

    @Operation(summary = "开始迭代")
    @PostMapping("/{id}/start")
    public R<SprintDTO> start(@PathVariable Long id) {
        return R.ok(sprintService.start(id));
    }

    @Operation(summary = "关闭迭代")
    @PostMapping("/{id}/close")
    public R<SprintDTO> close(@PathVariable Long id) {
        return R.ok(sprintService.close(id));
    }
}
