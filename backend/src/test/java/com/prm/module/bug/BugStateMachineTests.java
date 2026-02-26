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
    void newBugCanBeConfirmed() {
        assertThat(machine.canTransit("NEW", "CONFIRMED")).isTrue();
    }

    @Test
    void confirmedCanBeAssigned() {
        assertThat(machine.canTransit("CONFIRMED", "ASSIGNED")).isTrue();
    }

    @Test
    void closedBugCanReopen() {
        assertThat(machine.canTransit("CLOSED", "NEW")).isTrue();
    }

    @Test
    void invalidTransitionShouldThrow() {
        assertThatThrownBy(() -> machine.transit("NEW", "CLOSED_INVALID"))
                .isInstanceOf(BizException.class);
    }
}
