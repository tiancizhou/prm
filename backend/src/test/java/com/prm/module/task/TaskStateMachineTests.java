package com.prm.module.task;

import com.prm.common.exception.BizException;
import com.prm.module.task.domain.TaskStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskStateMachineTests {

    private TaskStateMachine machine;

    @BeforeEach
    void setUp() {
        machine = new TaskStateMachine();
    }

    @Test
    void todoShouldTransitToInProgress() {
        assertThat(machine.canTransit("TODO", "IN_PROGRESS")).isTrue();
    }

    @Test
    void inProgressShouldTransitBackToTodo() {
        assertThat(machine.canTransit("IN_PROGRESS", "TODO")).isTrue();
    }

    @Test
    void inProgressShouldTransitToPendingReview() {
        assertThat(machine.canTransit("IN_PROGRESS", "PENDING_REVIEW")).isTrue();
    }

    @Test
    void pendingReviewShouldTransitBackToInProgress() {
        assertThat(machine.canTransit("PENDING_REVIEW", "IN_PROGRESS")).isTrue();
    }

    @Test
    void pendingReviewShouldTransitToDone() {
        assertThat(machine.canTransit("PENDING_REVIEW", "DONE")).isTrue();
    }

    @Test
    void doneShouldTransitToClosed() {
        assertThat(machine.canTransit("DONE", "CLOSED")).isTrue();
    }

    @Test
    void doneShouldReopenToInProgress() {
        assertThat(machine.canTransit("DONE", "IN_PROGRESS")).isTrue();
    }

    @Test
    void closedShouldReopenToTodo() {
        assertThat(machine.canTransit("CLOSED", "TODO")).isTrue();
    }

    @Test
    void invalidTransitionShouldThrow() {
        assertThatThrownBy(() -> machine.transit("TODO", "DONE"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不允许");
    }
}
