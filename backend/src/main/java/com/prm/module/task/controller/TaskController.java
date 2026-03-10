package com.prm.module.task.controller;

import com.prm.common.result.PageResult;
import com.prm.common.result.R;
import com.prm.module.log.annotation.OperLog;
import com.prm.module.task.application.TaskService;
import com.prm.module.task.dto.CreateTaskRequest;
import com.prm.module.task.dto.TaskDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "任务管理")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "任务分页列表")
    @GetMapping
    public R<PageResult<TaskDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long requirementId) {
        return R.ok(PageResult.of(taskService.page(page, size, projectId, status, assigneeId, keyword, requirementId)));
    }

    @Operation(summary = "创建任务")
    @OperLog(module = "TASK", action = "CREATE", bizType = "TASK")
    @PostMapping
    public R<TaskDTO> create(@Valid @RequestBody CreateTaskRequest request) {
        return R.ok(taskService.create(request));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public R<TaskDTO> getById(@PathVariable Long id) {
        return R.ok(taskService.getById(id));
    }

    @Operation(summary = "任务状态流转")
    @OperLog(module = "TASK", action = "UPDATE_STATUS", bizType = "TASK")
    @PutMapping("/{id}/status")
    public R<TaskDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return R.ok(taskService.updateStatus(id, status));
    }

    @Operation(summary = "指派任务")
    @OperLog(module = "TASK", action = "ASSIGN", bizType = "TASK")
    @PutMapping("/{id}/assign")
    public R<TaskDTO> assign(@PathVariable Long id, @RequestParam(required = false) Long assigneeId) {
        return R.ok(taskService.assign(id, assigneeId));
    }

    @Operation(summary = "填报工时")
    @OperLog(module = "TASK", action = "LOG_WORK", bizType = "TASK")
    @PostMapping("/{id}/worklog")
    public R<Void> logWork(@PathVariable Long id,
                            @RequestParam BigDecimal spentHours,
                            @RequestParam(required = false) String remark) {
        taskService.logWork(id, spentHours, remark);
        return R.ok();
    }
}
