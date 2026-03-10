package com.prm.module.requirement;

import com.prm.common.exception.BizException;
import com.prm.module.requirement.domain.RequirementStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequirementStateMachineTests {

    private RequirementStateMachine machine;

    @BeforeEach
    void setUp() {
        machine = new RequirementStateMachine();
    }

    @Test
    void draftShouldTransitToInProgress() {
        assertThat(machine.canTransit("DRAFT", "IN_PROGRESS")).isTrue();
    }

    @Test
    void inProgressShouldTransitToDone() {
        assertThat(machine.canTransit("IN_PROGRESS", "DONE")).isTrue();
    }

    @Test
    void doneShouldReopenToInProgress() {
        assertThat(machine.canTransit("DONE", "IN_PROGRESS")).isTrue();
    }

    @Test
    void reviewingShouldTransitToInProgress() {
        assertThat(machine.canTransit("REVIEWING", "IN_PROGRESS")).isTrue();
    }

    @Test
    void approvedShouldTransitToInProgress() {
        assertThat(machine.canTransit("APPROVED", "IN_PROGRESS")).isTrue();
    }

    @Test
    void closedCanReopenToInProgress() {
        assertThat(machine.canTransit("CLOSED", "IN_PROGRESS")).isTrue();
    }

    @Test
    void invalidTransitionShouldThrow() {
        assertThatThrownBy(() -> machine.transit("DRAFT", "DONE"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不允许");
    }

    @Test
    void inProgressShouldNotTransitBackToDraft() {
        assertThat(machine.canTransit("IN_PROGRESS", "DRAFT")).isFalse();
    }
}
