package com.prm.module.task.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 任务状态机
 * 待处理 -> 进行中 -> 待验收 -> 已完成 -> 已关闭
 */
@Component
public class TaskStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "TODO",        Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("TODO", "PENDING_REVIEW"),
            "PENDING_REVIEW", Set.of("IN_PROGRESS", "DONE"),
            "DONE",        Set.of("CLOSED", "IN_PROGRESS"),
            "CLOSED",      Set.of("TODO")
    );

    public static final Map<String, String> STATUS_LABELS = Map.of(
            "TODO",           "待处理",
            "IN_PROGRESS",    "进行中",
            "PENDING_REVIEW", "待验收",
            "DONE",           "已完成",
            "CLOSED",         "已关闭"
    );

    public boolean canTransit(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void transit(String from, String to) {
        if (!canTransit(from, to)) {
            throw BizException.of(String.format(
                    "任务状态不允许从 [%s] 流转到 [%s]",
                    STATUS_LABELS.getOrDefault(from, from),
                    STATUS_LABELS.getOrDefault(to, to)));
        }
    }
}
