package com.prm.module.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pm_dashboard_snapshot")
public class DashboardSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;
    private LocalDate snapDate;
    private String metricsJson;
    private LocalDateTime createdAt;
}
