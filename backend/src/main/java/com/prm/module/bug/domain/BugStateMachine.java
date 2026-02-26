package com.prm.module.bug.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Bug 状态机
 * 新建 -> 已确认 -> 已指派 -> 已解决 -> 已验证 -> 已关闭
 * 支持重开：已关闭 -> 新建
 */
@Component
public class BugStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "NEW",       Set.of("CONFIRMED", "CLOSED"),
            "CONFIRMED", Set.of("ASSIGNED", "CLOSED"),
            "ASSIGNED",  Set.of("RESOLVED"),
            "RESOLVED",  Set.of("VERIFIED", "ASSIGNED"),
            "VERIFIED",  Set.of("CLOSED"),
            "CLOSED",    Set.of("NEW")
    );

    public static final Map<String, String> STATUS_LABELS = Map.of(
            "NEW",       "新建",
            "CONFIRMED", "已确认",
            "ASSIGNED",  "已指派",
            "RESOLVED",  "已解决",
            "VERIFIED",  "已验证",
            "CLOSED",    "已关闭"
    );

    public boolean canTransit(String from, String to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void transit(String from, String to) {
        if (!canTransit(from, to)) {
            throw BizException.of(String.format(
                    "Bug 状态不允许从 [%s] 流转到 [%s]",
                    STATUS_LABELS.getOrDefault(from, from),
                    STATUS_LABELS.getOrDefault(to, to)));
        }
    }
}
