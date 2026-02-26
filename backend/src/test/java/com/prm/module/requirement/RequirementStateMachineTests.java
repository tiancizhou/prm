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
    void draftShouldTransitToReviewing() {
        assertThat(machine.canTransit("DRAFT", "REVIEWING")).isTrue();
    }

    @Test
    void reviewingShouldTransitToApproved() {
        assertThat(machine.canTransit("REVIEWING", "APPROVED")).isTrue();
    }

    @Test
    void approvedShouldTransitToInProgress() {
        assertThat(machine.canTransit("APPROVED", "IN_PROGRESS")).isTrue();
    }

    @Test
    void closedCanReopenToDraft() {
        assertThat(machine.canTransit("CLOSED", "DRAFT")).isTrue();
    }

    @Test
    void invalidTransitionShouldThrow() {
        assertThatThrownBy(() -> machine.transit("DRAFT", "DONE"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不允许");
    }
}
