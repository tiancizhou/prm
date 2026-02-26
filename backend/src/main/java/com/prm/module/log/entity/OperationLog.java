package com.prm.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String module;
    private String action;
    private String bizType;
    private Long bizId;
    private String beforeData;
    private String afterData;
    private String ip;
    private String userAgent;
    private Long durationMs;
    private LocalDateTime createdAt;
}
