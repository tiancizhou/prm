package com.prm.module.requirement.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 需求状态机（小团队/轻流程：三态）
 * 待办 -> 进行中 -> 已完成；已完成可重新打开为进行中
 */
@Component
public class RequirementStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "DRAFT",       Set.of("IN_PROGRESS"),   // 待办 -> 开始
            "IN_PROGRESS", Set.of("DONE"),          // 进行中 -> 完成
            "DONE",        Set.of("IN_PROGRESS"),   // 已完成 -> 重新打开
            "REVIEWING",   Set.of("IN_PROGRESS"),   // 兼容旧状态
            "APPROVED",    Set.of("IN_PROGRESS"),
            "CLOSED",     Set.of("IN_PROGRESS")
    );

    /** 三态展示；兼容迁移前的旧状态展示 */
    public static final Map<String, String> STATUS_LABELS = Map.of(
            "DRAFT",       "待办",
            "IN_PROGRESS", "进行中",
            "DONE",        "已完成",
            "REVIEWING",   "待办",
            "APPROVED",    "待办",
            "CLOSED",      "已完成"
    );

    public boolean canTransit(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void transit(String from, String to) {
        if (!canTransit(from, to)) {
            throw BizException.of(String.format(
                    "需求状态不允许从 [%s] 流转到 [%s]",
                    STATUS_LABELS.getOrDefault(from, from),
                    STATUS_LABELS.getOrDefault(to, to)));
        }
    }
}
