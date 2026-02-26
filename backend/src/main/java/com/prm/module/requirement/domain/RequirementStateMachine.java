package com.prm.module.requirement.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 需求状态机
 * 草稿 -> 评审中 -> 已立项 -> 开发中 -> 已完成 -> 已关闭
 */
@Component
public class RequirementStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "DRAFT",       Set.of("REVIEWING"),
            "REVIEWING",   Set.of("DRAFT", "APPROVED"),
            "APPROVED",    Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("DONE"),
            "DONE",        Set.of("CLOSED", "IN_PROGRESS"),
            "CLOSED",      Set.of("DRAFT")
    );

    public static final Map<String, String> STATUS_LABELS = Map.of(
            "DRAFT",       "草稿",
            "REVIEWING",   "评审中",
            "APPROVED",    "已立项",
            "IN_PROGRESS", "开发中",
            "DONE",        "已完成",
            "CLOSED",      "已关闭"
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
