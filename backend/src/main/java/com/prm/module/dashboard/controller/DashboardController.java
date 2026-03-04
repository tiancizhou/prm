package com.prm.module.dashboard.controller;

import com.prm.common.exception.BizException;
import com.prm.common.result.R;
import com.prm.common.util.SecurityUtil;
import com.prm.module.dashboard.application.DashboardService;
import com.prm.module.dashboard.dto.OverviewDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据看板")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "项目总览统计")
    @GetMapping("/overview")
    public R<OverviewDTO> overview(@RequestParam(required = false) Long projectId) {
        return R.ok(dashboardService.getOverview(projectId));
    }

    @Operation(summary = "手动触发快照聚合（管理员）")
    @PostMapping("/aggregate")
    public R<Void> aggregate(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            dashboardService.requireAggregatePermission(projectId);
            dashboardService.aggregateProject(projectId);
        } else {
            if (!SecurityUtil.isSuperAdmin()) {
                throw BizException.forbidden("仅超级管理员可执行全量聚合");
            }
            dashboardService.runDailyAggregation();
        }
        return R.ok();
    }
}
