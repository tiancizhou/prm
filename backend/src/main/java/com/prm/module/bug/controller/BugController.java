package com.prm.module.bug.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.bug.application.BugService;
import com.prm.module.bug.dto.BugDTO;
import com.prm.module.bug.dto.CreateBugRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bug管理")
@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @Operation(summary = "Bug分页列表")
    @GetMapping
    public R<PageResult<BugDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(bugService.page(page, size, projectId, status, severity, keyword)));
    }

    @Operation(summary = "提交Bug")
    @PostMapping
    public R<BugDTO> create(@Valid @RequestBody CreateBugRequest request) {
        return R.ok(bugService.create(request));
    }

    @Operation(summary = "Bug详情")
    @GetMapping("/{id}")
    public R<BugDTO> getById(@PathVariable Long id) {
        return R.ok(bugService.getById(id));
    }

    @Operation(summary = "Bug状态流转")
    @PutMapping("/{id}/status")
    public R<BugDTO> updateStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   @RequestParam(required = false) String resolveType) {
        return R.ok(bugService.updateStatus(id, status, resolveType));
    }

    @Operation(summary = "指派Bug")
    @PutMapping("/{id}/assign")
    public R<BugDTO> assign(@PathVariable Long id, @RequestParam Long assigneeId) {
        return R.ok(bugService.assign(id, assigneeId));
    }

    @Operation(summary = "添加Bug评论")
    @PostMapping("/{id}/comments")
    public R<Void> addComment(@PathVariable Long id, @RequestParam String content) {
        bugService.addComment(id, content);
        return R.ok();
    }
}
