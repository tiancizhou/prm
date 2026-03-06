package com.prm.module.bug.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Bug 状态机
 * 已激活 -> 已解决 -> 已关闭
 * 支持重开：已关闭/已解决 -> 已激活
 */
@Component
public class BugStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "ACTIVE",   Set.of("RESOLVED", "CLOSED"),
            "RESOLVED", Set.of("CLOSED", "ACTIVE"),
            "CLOSED",   Set.of("ACTIVE")
    );

    public static final Map<String, String> STATUS_LABELS = Map.of(
            "ACTIVE",   "已激活",
            "RESOLVED", "已解决",
            "CLOSED",   "已关闭"
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
