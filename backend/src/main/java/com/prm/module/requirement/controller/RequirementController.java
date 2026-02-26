package com.prm.module.requirement.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.requirement.application.RequirementService;
import com.prm.module.requirement.dto.CreateRequirementRequest;
import com.prm.module.requirement.dto.RequirementDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "需求管理")
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @Operation(summary = "需求分页列表")
    @GetMapping
    public R<PageResult<RequirementDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(PageResult.of(requirementService.page(page, size, projectId, status, keyword)));
    }

    @Operation(summary = "创建需求")
    @PostMapping
    public R<RequirementDTO> create(@Valid @RequestBody CreateRequirementRequest request) {
        return R.ok(requirementService.create(request));
    }

    @Operation(summary = "需求详情")
    @GetMapping("/{id}")
    public R<RequirementDTO> getById(@PathVariable Long id) {
        return R.ok(requirementService.getById(id));
    }

    @Operation(summary = "需求状态流转")
    @PutMapping("/{id}/status")
    public R<RequirementDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return R.ok(requirementService.updateStatus(id, status));
    }

    @Operation(summary = "添加需求评审记录")
    @PostMapping("/{id}/review")
    public R<Void> addReview(@PathVariable Long id,
                              @RequestParam String conclusion,
                              @RequestParam(required = false) String remark) {
        requirementService.addReview(id, conclusion, remark);
        return R.ok();
    }
}
