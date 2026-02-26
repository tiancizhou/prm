package com.prm.module.sprint.domain;

import com.prm.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 迭代状态机：规划中 -> 进行中 -> 已关闭
 */
@Component
public class SprintStateMachine {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "PLANNING", Set.of("ACTIVE"),
            "ACTIVE",   Set.of("CLOSED")
    );

    public void transit(String from, String to) {
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw BizException.of(String.format("迭代状态不允许从 [%s] 流转到 [%s]", from, to));
        }
    }
}
