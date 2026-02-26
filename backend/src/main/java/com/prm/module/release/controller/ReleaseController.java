package com.prm.module.release.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.release.application.ReleaseService;
import com.prm.module.release.dto.CreateReleaseRequest;
import com.prm.module.release.dto.ReleaseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "版本发布")
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @Operation(summary = "版本分页列表")
    @GetMapping
    public R<PageResult<ReleaseDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId) {
        return R.ok(PageResult.of(releaseService.page(page, size, projectId)));
    }

    @Operation(summary = "创建版本")
    @PostMapping
    public R<ReleaseDTO> create(@Valid @RequestBody CreateReleaseRequest request) {
        return R.ok(releaseService.create(request));
    }

    @Operation(summary = "版本详情")
    @GetMapping("/{id}")
    public R<ReleaseDTO> getById(@PathVariable Long id) {
        return R.ok(releaseService.getById(id));
    }

    @Operation(summary = "发布版本")
    @PostMapping("/{id}/publish")
    public R<ReleaseDTO> publish(@PathVariable Long id) {
        return R.ok(releaseService.publish(id));
    }
}
