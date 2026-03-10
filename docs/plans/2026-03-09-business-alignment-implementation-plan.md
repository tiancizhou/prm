# PRM Business Alignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align the existing PRM business flow so permissions, state transitions, user-visible actions, and dashboard metrics all match the currently implemented backend capabilities without changing schema.

**Architecture:** This plan is a phase-one convergence pass. It does not add new entities, routes, or database migrations. It standardizes project-domain permissions around project membership, collapses divergent state semantics to one active rule set per module, removes misleading UI actions and fake data, and updates documentation to reflect the true enabled scope.

**Tech Stack:** Spring Boot, MyBatis-Plus, Sa-Token, Vue 3, TypeScript, Element Plus, Vite

---

### Task 1: Add task permission regression tests

**Files:**
- Create: `backend/src/test/java/com/prm/module/task/TaskServicePermissionTests.java`
- Modify: `backend/src/main/java/com/prm/module/task/application/TaskService.java`

**Step 1: Write the failing test**

Add tests covering these behaviors:

- project manager of the target project can create and assign task
- plain member of the target project cannot create or assign task even if system role says `PROJECT_ADMIN`
- plain member can only update status of tasks assigned to self
- non-member cannot read project tasks

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=TaskServicePermissionTests test`

Expected: FAIL because `TaskService` currently uses `SecurityUtil.isManager()` instead of project membership.

**Step 3: Write minimal implementation**

- Introduce project membership lookup in `TaskService`
- Reuse the same permission shape already used in `RequirementService` / `BugService`
- Remove direct reliance on `SecurityUtil.isManager()` for project-domain permissions

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=TaskServicePermissionTests test`

Expected: PASS

---

### Task 2: Add sprint permission regression tests

**Files:**
- Create: `backend/src/test/java/com/prm/module/sprint/SprintServicePermissionTests.java`
- Modify: `backend/src/main/java/com/prm/module/sprint/application/SprintService.java`

**Step 1: Write the failing test**

Cover these behaviors:

- project admin can create/start/close sprint in own project
- project member can read project sprints but cannot create/start/close
- non-member cannot read sprint detail or list project sprints

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=SprintServicePermissionTests test`

Expected: FAIL because `SprintService` currently lacks project-level permission checks.

**Step 3: Write minimal implementation**

- Add membership lookup and permission guards to `page`, `getById`, `create`, `start`, `close`
- Preserve super-admin bypass

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=SprintServicePermissionTests test`

Expected: PASS

---

### Task 3: Add release permission regression tests

**Files:**
- Create: `backend/src/test/java/com/prm/module/release/ReleaseServicePermissionTests.java`
- Modify: `backend/src/main/java/com/prm/module/release/application/ReleaseService.java`

**Step 1: Write the failing test**

Cover these behaviors:

- project admin can create/publish release in own project
- project member can read project releases but cannot create/publish
- non-member cannot read or list releases for that project

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=ReleaseServicePermissionTests test`

Expected: FAIL because `ReleaseService` currently lacks project-level permission checks.

**Step 3: Write minimal implementation**

- Add project membership permission guards to `page`, `getById`, `create`, `publish`
- Preserve super-admin bypass

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=ReleaseServicePermissionTests test`

Expected: PASS

---

### Task 4: Lock bug state semantics to the real backend flow

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`
- Modify: `frontend/src/views/bug/BugDetailPage.vue`
- Modify: `frontend/src/constants/bug.ts`
- Test: `backend/src/test/java/com/prm/module/bug/BugStateMachineTests.java`

**Step 1: Write the failing test**

Add or extend bug state-machine tests to assert the canonical flow is:

- `ACTIVE -> RESOLVED`
- `ACTIVE -> CLOSED`
- `RESOLVED -> CLOSED`
- `RESOLVED -> ACTIVE`
- `CLOSED -> ACTIVE`

Add a failing UI-facing assertion if an existing bug state test pattern exists; otherwise validate UI via code inspection and later frontend type-check.

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=BugStateMachineTests test`

Expected: FAIL if tests still assume old status semantics.

**Step 3: Write minimal implementation**

- Remove `VERIFIED` as a user-visible active state in bug pages
- Stop mapping old compatibility states as if they were current workflow states
- Ensure button labels and lifecycle display only express the real backend state machine

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=BugStateMachineTests test`

Expected: PASS

---

### Task 5: Restrict task UI actions to legal next states

**Files:**
- Modify: `frontend/src/views/task/TaskPage.vue`
- Modify: `frontend/src/constants/task.ts`
- Test: `backend/src/test/java/com/prm/module/task/TaskStateMachineTests.java` (create if absent)

**Step 1: Write the failing test**

Create or extend task state-machine tests covering legal transitions only:

- `TODO -> IN_PROGRESS`
- `IN_PROGRESS -> TODO`
- `IN_PROGRESS -> PENDING_REVIEW`
- `PENDING_REVIEW -> IN_PROGRESS`
- `PENDING_REVIEW -> DONE`
- `DONE -> CLOSED`
- `DONE -> IN_PROGRESS`
- `CLOSED -> TODO`

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=TaskStateMachineTests test`

Expected: FAIL if the new test file is added first and not yet implemented or absent.

**Step 3: Write minimal implementation**

- Replace “show all statuses” task menus with per-status next-step menus
- Expose `CLOSED` only where it is actually reachable
- Keep task columns and labels aligned with the chosen UI representation

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=TaskStateMachineTests test`

Expected: PASS

---

### Task 6: Freeze requirement flow to the current three-state model

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/views/requirement/RequirementDetailPage.vue`
- Modify: `frontend/src/constants/requirement.ts`
- Modify: `docs/plans/2026-02-26-prm-design.md`

**Step 1: Write the failing test**

No dedicated frontend harness exists. Use targeted backend requirement state-machine tests plus code inspection.

If needed, extend `backend/src/test/java/com/prm/module/requirement/RequirementStateMachineTests.java` to assert current first-phase flow is centered on `DRAFT / IN_PROGRESS / DONE` while old statuses remain compatibility-only.

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=RequirementStateMachineTests test`

Expected: FAIL if assertions still reflect the old broader lifecycle as primary behavior.

**Step 3: Write minimal implementation**

- Ensure requirement page and detail page present the three-state flow as the active workflow
- Keep review-related entities/API untouched, but stop implying they are the current default process
- Update the design document to mark review flow as later-stage capability

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=RequirementStateMachineTests test`

Expected: PASS

---

### Task 7: Remove fake project overview trend data

**Files:**
- Modify: `frontend/src/views/project/ProjectOverviewPage.vue`
- Modify: `frontend/src/constants/projectOverview.ts`
- Modify: `frontend/src/api/dashboard.ts`
- Modify: `backend/src/main/java/com/prm/module/dashboard/application/DashboardService.java`

**Step 1: Write the failing test**

There is no frontend test harness. Use backend dashboard tests if they cover overview semantics; otherwise add/extend focused service tests in `backend/src/test/java/com/prm/module/dashboard/DashboardServiceTests.java` for overview scope and bug status counting.

**Step 2: Run test to verify it fails**

Run: `mvn -f backend/pom.xml -Dtest=DashboardServiceTests test`

Expected: FAIL if new expectations reveal mismatch between bug canonical states and dashboard counts.

**Step 3: Write minimal implementation**

- Remove `Math.random()` generated trend lines from project overview
- Keep only real KPI / activity / sprint data, or show an explicit placeholder for unavailable trend capability
- Align bug open-count logic with the canonical bug state model
- Decide and enforce whether `project overview` is project-scope only in phase one

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=DashboardServiceTests test`

Expected: PASS

---

### Task 8: Mark half-built cross-module capabilities as not enabled

**Files:**
- Modify: `docs/plans/2026-02-26-prm-design.md`
- Modify: `docs/plans/2026-03-09-business-alignment-design.md`
- Modify: `backend/src/main/java/com/prm/module/release/application/ReleaseService.java`
- Modify: `backend/src/main/java/com/prm/module/bug/application/BugService.java`

**Step 1: Write the failing test**

No automated test needed for documentation semantics; validate by code inspection and existing regression tests for unchanged service behavior.

**Step 2: Run test to verify it fails**

Not applicable.

**Step 3: Write minimal implementation**

- Update docs to distinguish enabled phase-one capabilities from later-stage target capabilities
- Clarify release is currently a version record only, not a full release closure
- Clarify bug-to-requirement currently creates a requirement record, not a formal many-to-many link workflow

**Step 4: Run test to verify it passes**

Run: `mvn -f backend/pom.xml -Dtest=BugServicePermissionTests,RequirementServicePermissionTests,ProjectServicePermissionTests test`

Expected: PASS to confirm surrounding behavior remains intact.

---

### Task 9: Add operation-log coverage decision

**Files:**
- Modify: `backend/src/main/java/com/prm/module/log/aspect/OperationLogAspect.java`
- Modify: controllers/services that should carry `@OperLog` if phase one chooses to enable it
- Test: add focused tests only if the repository already has a pattern for aspect verification; otherwise document as a follow-up item

**Step 1: Write the failing test**

If aspect verification pattern exists, add a minimal test that proves a state change writes to `pm_operation_log`. If no such pattern exists, document this as deferred and do not force a new testing style into the repo.

**Step 2: Run test to verify it fails**

Run the narrowest available test command if such a test is added.

Expected: FAIL before instrumentation is connected.

**Step 3: Write minimal implementation**

- Either connect the highest-value project-domain state changes to `@OperLog`
- Or explicitly defer operation-log enablement if the current aspect cannot be verified safely within phase one

**Step 4: Run test to verify it passes**

Run the same narrow test command if applicable.

Expected: PASS or explicit documented deferral.

---

### Task 10: Final verification and delivery

**Files:**
- Verify: `backend/src/main/java/com/prm/module/task/application/TaskService.java`
- Verify: `backend/src/main/java/com/prm/module/sprint/application/SprintService.java`
- Verify: `backend/src/main/java/com/prm/module/release/application/ReleaseService.java`
- Verify: `frontend/src/views/bug/BugPage.vue`
- Verify: `frontend/src/views/bug/BugDetailPage.vue`
- Verify: `frontend/src/views/task/TaskPage.vue`
- Verify: `frontend/src/views/requirement/RequirementPage.vue`
- Verify: `frontend/src/views/project/ProjectOverviewPage.vue`

**Step 1: Run targeted backend verification**

Run:

`mvn -f backend/pom.xml -Dtest=TaskServicePermissionTests,SprintServicePermissionTests,ReleaseServicePermissionTests,BugStateMachineTests,TaskStateMachineTests,RequirementStateMachineTests,DashboardServiceTests test`

Expected: PASS

**Step 2: Run frontend type-check/build verification**

Run: `npm run build`

Expected: if unrelated pre-existing frontend type errors remain, record them explicitly and verify no new phase-one errors were introduced in touched files.

**Step 3: Re-read design checklist**

Confirm all phase-one goals are met:

- permissions unified
- state semantics unified
- fake data removed
- misleading actions removed
- docs reflect enabled scope

**Step 4: Prepare delivery summary**

- list changed modules
- list deferred phase-two items
- list any unrelated verification blockers still present in repo
