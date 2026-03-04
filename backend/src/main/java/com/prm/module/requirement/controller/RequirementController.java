package com.prm.module.requirement.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.common.exception.BizException;
import com.prm.module.requirement.application.RequirementService;
import com.prm.module.requirement.dto.CreateRequirementRequest;
import com.prm.module.requirement.dto.RequirementDTO;
import com.prm.module.requirement.dto.UpdateRequirementRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

@Tag(name = "需求管理")
@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;
    private static final Pattern OFFSET_OR_ZONE_SUFFIX = Pattern.compile(".*(?:[zZ]|[+-]\\d{2}:\\d{2})$");
    private static final List<DateTimeFormatter> FLEXIBLE_DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    @Operation(summary = "需求分页列表")
    @GetMapping
    public R<PageResult<RequirementDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(required = false) Boolean unscheduled,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo) {
        return R.ok(PageResult.of(requirementService.page(
                page, size, projectId, status, keyword,
                assigneeId, sprintId, unscheduled, dueDateFrom, dueDateTo)));
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

    @Operation(summary = "更新需求")
    @PutMapping("/{id}")
    public R<RequirementDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateRequirementRequest request) {
        return R.ok(requirementService.update(id, request));
    }

    @Operation(summary = "需求状态流转")
    @PutMapping("/{id}/status")
    public R<RequirementDTO> updateStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String actualStartAt,
                                          @RequestParam(required = false) String actualEndAt,
                                          @RequestParam(required = false) String verificationScenario,
                                          @RequestParam(required = false) String verificationSteps,
                                          @RequestParam(required = false) String verificationResult) {
        LocalDateTime resolvedActualStartAt = parseDateTimeParameter(actualStartAt, "actualStartAt");
        LocalDateTime resolvedActualEndAt = parseDateTimeParameter(actualEndAt, "actualEndAt");

        return R.ok(requirementService.updateStatus(
                id,
                status,
                resolvedActualStartAt,
                resolvedActualEndAt,
                verificationScenario,
                verificationSteps,
                verificationResult));
    }

    private LocalDateTime parseDateTimeParameter(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String value = text.trim();
        if (OFFSET_OR_ZONE_SUFFIX.matcher(value).matches()) {
            throw BizException.of(fieldName + " 不支持时区偏移，请使用本地时间 yyyy-MM-ddTHH:mm");
        }

        for (DateTimeFormatter formatter : FLEXIBLE_DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw BizException.of(fieldName + " 时间格式不正确，请使用本地时间 yyyy-MM-ddTHH:mm");
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
