package com.prm.module.bug;

import com.prm.common.exception.BizException;
import com.prm.module.bug.domain.BugStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BugStateMachineTests {

    private BugStateMachine machine;

    @BeforeEach
    void setUp() {
        machine = new BugStateMachine();
    }

    @Test
    void activeBugCanBeResolved() {
        assertThat(machine.canTransit("ACTIVE", "RESOLVED")).isTrue();
    }

    @Test
    void activeBugCanBeClosed() {
        assertThat(machine.canTransit("ACTIVE", "CLOSED")).isTrue();
    }

    @Test
    void resolvedBugCanBeClosed() {
        assertThat(machine.canTransit("RESOLVED", "CLOSED")).isTrue();
    }

    @Test
    void resolvedBugCanReopenToActive() {
        assertThat(machine.canTransit("RESOLVED", "ACTIVE")).isTrue();
    }

    @Test
    void closedBugCanReopenToActive() {
        assertThat(machine.canTransit("CLOSED", "ACTIVE")).isTrue();
    }

    @Test
    void invalidTransitionShouldThrow() {
        assertThatThrownBy(() -> machine.transit("ACTIVE", "ACTIVE"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不允许");
    }
}
