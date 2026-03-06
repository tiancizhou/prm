# Task Management Hardening Design

**Date:** 2026-03-06
**Scope:** `task` module quality hardening for Beta readiness
**Status:** Approved

---

## 1. Context

Current task management already supports the main workflow:
- list/board views
- create task
- assign/unassign
- status transition
- worklog submission and spent/remaining hour update

The largest delivery risk is not missing features, but weak guardrails and thin automated coverage in task-specific flows. This round focuses on hardening behavior consistency and preventing regressions.

---

## 2. Goals and Non-Goals

### 2.1 Goals

1. Make task core operations behaviorally safe:
- status transition
- assignment
- worklog recording
2. Add explicit automated tests for task domain rules and permission boundaries.
3. Keep frontend and backend behavior aligned for operation feedback and basic validation.

### 2.2 Non-Goals

1. No new large features (no subtask tree UI, no dependency graph, no batch operations).
2. No cross-module architecture rewrite.
3. No database schema redesign unless absolutely required by tests.

---

## 3. Approaches Considered

### Approach A: Full Feature Expansion

- Add subtask/dependency/bulk actions first.
- Pros: visible feature growth.
- Cons: compounds existing test debt and risk.

### Approach B: Frontend-Only UX Polish

- Improve task page interactions but leave backend rules mostly as-is.
- Pros: fast visual impact.
- Cons: hidden backend inconsistencies remain.

### Approach C: Task Hardening First (Selected)

- Build tests first, then patch backend/frontend behavior only where tests expose gaps.
- Pros: highest reliability gain per effort, supports Beta stabilization.
- Cons: less visible net-new functionality.

---

## 4. Target Design

### 4.1 Backend Hardening

Rules to enforce:
1. Worklog amount must be positive.
2. Non-manager users can only record worklog for their own assigned tasks.
3. Existing manager/non-manager permission boundaries for status and assignment remain explicit and test-covered.
4. Remaining hours must never be negative after worklog aggregation.

Primary touch points:
- `backend/src/main/java/com/prm/module/task/application/TaskService.java`
- `backend/src/main/java/com/prm/module/task/controller/TaskController.java`
- task module tests under `backend/src/test/java/com/prm/module/task/`

### 4.2 Frontend Alignment

1. Validate worklog hours as `> 0` before submit.
2. Keep operation feedback consistent (success/failure reload path already exists).
3. Keep role-based operation visibility unchanged.

Primary touch point:
- `frontend/src/views/task/TaskPage.vue`

---

## 5. Data Flow and Error Handling

### 5.1 Worklog Flow

1. User submits worklog in task page.
2. Frontend performs minimal input guard (`spentHours > 0`).
3. Backend re-validates and enforces permission.
4. Backend writes worklog record and recalculates:
- `spent_hours = SUM(worklog.spent_hours)`
- `remaining_hours = max(estimated_hours - spent_hours, 0)`
5. Frontend refreshes list.

### 5.2 Error Handling Strategy

1. Frontend validation blocks obvious invalid input early.
2. Backend remains source of truth for all security/rule checks.
3. On backend rejection, frontend keeps current fallback behavior (reload state) to avoid stale UI.

---

## 6. Testing Strategy

TDD for every behavior change:
1. Add failing task-module tests for:
- permission boundaries (status/assign/worklog)
- worklog validation and remaining-hour floor
2. Run targeted task tests to confirm RED.
3. Implement minimal backend/frontend changes to pass.
4. Re-run targeted tests to confirm GREEN.

Planned test files:
- `backend/src/test/java/com/prm/module/task/TaskServicePermissionTests.java` (new)
- `backend/src/test/java/com/prm/module/task/TaskWorklogFlowTests.java` (new)

---

## 7. Acceptance Criteria

1. Non-manager cannot write worklog for unassigned task.
2. Any non-positive worklog is rejected.
3. Remaining hours never drops below zero after any worklog sequence.
4. Existing status transition and assignment permission behavior is regression-safe through tests.
5. Task page blocks zero/negative worklog input and keeps current UX behavior for successful operations.
