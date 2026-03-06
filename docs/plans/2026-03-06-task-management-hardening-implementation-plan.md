# Task Management Hardening Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Harden task-management permissions and worklog rules with test-first implementation, then align task page validation.

**Architecture:** Use service-layer enforcement as the source of truth for task permissions and worklog validation. Add isolated unit tests for task permission and worklog flow before touching production code. Keep frontend changes minimal and only for early validation/UX consistency.

**Tech Stack:** Java 21, Spring Boot, MyBatis-Plus, JUnit 5, Mockito, Vue 3, Element Plus, TypeScript.

---

## Implementation Constraints

- Follow @test-driven-development strictly for each behavior change.
- Keep changes scoped to task module and task page.
- No schema changes in this round.

---

### Task 1: Add failing permission tests for task operations

**Files:**
- Create: `backend/src/test/java/com/prm/module/task/TaskServicePermissionTests.java`
- Modify: `backend/src/main/java/com/prm/module/task/application/TaskService.java` (only after RED)

**Step 1: Write the failing tests**

```java
@Test
void nonManagerShouldNotUpdateStatusOfOthersTask() {
    // mock SecurityUtil current user = 2001, non-manager
    // task assignee = 3001
    // expect BizException forbidden
}

@Test
void nonManagerShouldNotAssignTask() {
    // non-manager calling assign should fail
}

@Test
void nonManagerShouldNotLogWorkForOthersTask() {
    // non-manager trying worklog on task not assigned to self should fail
}

@Test
void managerShouldLogWorkForAnyTask() {
    // manager should pass permission check
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskServicePermissionTests test`
Expected: FAIL (new permission tests expose missing logWork permission guard).

**Step 3: Write minimal implementation**

```java
@Transactional
public void logWork(Long taskId, BigDecimal spentHours, String remark) {
    Task task = taskMapper.selectById(taskId);
    if (task == null) throw BizException.notFound("任务");

    Long userId = SecurityUtil.getCurrentUserId();
    if (!SecurityUtil.isManager()) {
        if (!userId.equals(task.getAssigneeId())) {
            throw BizException.forbidden("只能为分配给自己的任务登记工时");
        }
    }

    // existing insert + aggregation logic
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskServicePermissionTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/test/java/com/prm/module/task/TaskServicePermissionTests.java backend/src/main/java/com/prm/module/task/application/TaskService.java
git commit -m "test(task): cover task permission boundaries"
```

### Task 2: Add failing worklog flow tests and enforce input guards

**Files:**
- Create: `backend/src/test/java/com/prm/module/task/TaskWorklogFlowTests.java`
- Modify: `backend/src/main/java/com/prm/module/task/application/TaskService.java`

**Step 1: Write the failing tests**

```java
@Test
void logWorkShouldRejectZeroSpentHours() {
    // spentHours = 0 -> BizException
}

@Test
void logWorkShouldRejectNegativeSpentHours() {
    // spentHours = -1 -> BizException
}

@Test
void calcRemainingShouldFloorAtZero() {
    // estimate 2, consumed 5 => 0
}

@Test
void logWorkShouldUpdateSpentAndRemainingHours() {
    // sumSpent returns 3.5, estimate 5 => remaining 1.5
}
```

**Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskWorklogFlowTests test`
Expected: FAIL (no positive-hours validation yet).

**Step 3: Write minimal implementation**

```java
private void validateSpentHours(BigDecimal spentHours) {
    if (spentHours == null || spentHours.compareTo(BigDecimal.ZERO) <= 0) {
        throw BizException.of("工时必须大于0");
    }
}

@Transactional
public void logWork(Long taskId, BigDecimal spentHours, String remark) {
    validateSpentHours(spentHours);
    // existing logic
    BigDecimal totalSpent = taskMapper.sumSpentHoursByTaskId(taskId);
    task.setSpentHours(totalSpent);
    task.setRemainingHours(calcRemaining(task.getEstimatedHours(), totalSpent));
    taskMapper.updateById(task);
}
```

**Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskWorklogFlowTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/test/java/com/prm/module/task/TaskWorklogFlowTests.java backend/src/main/java/com/prm/module/task/application/TaskService.java
git commit -m "fix(task): enforce worklog hours and remaining-hour floor"
```

### Task 3: Keep controller boundary explicit for worklog params

**Files:**
- Modify: `backend/src/main/java/com/prm/module/task/controller/TaskController.java`

**Step 1: Write a failing expectation in existing service tests (if needed)**

```java
// optional: assert null spentHours path returns business error in service tests
```

**Step 2: Run targeted tests for RED (if added)**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskWorklogFlowTests test`
Expected: FAIL for null-path expectation.

**Step 3: Write minimal implementation**

```java
@PostMapping("/{id}/worklog")
public R<Void> logWork(@PathVariable Long id,
                       @RequestParam BigDecimal spentHours,
                       @RequestParam(required = false) String remark) {
    taskService.logWork(id, spentHours, remark);
    return R.ok();
}
```

Note: Keep controller thin; validation authority remains in `TaskService`.

**Step 4: Run tests to verify GREEN**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskServicePermissionTests,TaskWorklogFlowTests test`
Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/task/controller/TaskController.java backend/src/main/java/com/prm/module/task/application/TaskService.java backend/src/test/java/com/prm/module/task/TaskServicePermissionTests.java backend/src/test/java/com/prm/module/task/TaskWorklogFlowTests.java
git commit -m "refactor(task): keep controller thin and service-validated"
```

### Task 4: Align task page worklog validation and UX safety

**Files:**
- Modify: `frontend/src/views/task/TaskPage.vue`

**Step 1: Add failing expectation (manual RED check)**

```markdown
- Open worklog dialog
- Input spentHours = 0
- Click confirm
- Expected behavior should block submit with warning
```

**Step 2: Verify RED**

Run app and try submit with `0`.
Expected: currently can submit or behavior not explicitly guarded.

**Step 3: Write minimal implementation**

```ts
async function submitWorklog() {
  if (!worklogTask.value) return
  if (worklogForm.spentHours <= 0) {
    ElMessage.warning(taskText.messages.worklogHoursPositive)
    return
  }
  // existing submit logic
}
```

```vue
<el-input-number v-model="worklogForm.spentHours" :min="0.1" :precision="1" />
```

Also add i18n text key in `frontend/src/constants/task.ts` if missing:

```ts
worklogHoursPositive: '登记工时必须大于 0'
```

**Step 4: Verify GREEN**

Run:
- `npm run type-check --prefix frontend`

Manual:
- `spentHours=0` blocked with warning
- `spentHours>0` submit still works

Expected: type-check PASS, validation behavior correct.

**Step 5: Commit**

```bash
git add frontend/src/views/task/TaskPage.vue frontend/src/constants/task.ts
git commit -m "fix(task-ui): validate positive worklog hours before submit"
```

### Task 5: Final verification and delivery notes

**Files:**
- Modify: `docs/plans/2026-03-06-task-management-hardening-design.md` (optional summary updates)

**Step 1: Run backend targeted test suite**

Run: `mvn -q -f backend/pom.xml -Dtest=TaskServicePermissionTests,TaskWorklogFlowTests test`
Expected: PASS.

**Step 2: Run related regression tests**

Run: `mvn -q -f backend/pom.xml -Dtest=BugServicePermissionTests,RequirementServicePermissionTests test`
Expected: PASS (no permission-regression spillover).

**Step 3: Run frontend type check**

Run: `npm run type-check --prefix frontend`
Expected: PASS.

**Step 4: Validate git state**

Run: `git status --short --branch`
Expected: only intended files changed; no accidental artifacts.

**Step 5: Commit final docs update (if any)**

```bash
git add docs/plans/2026-03-06-task-management-hardening-design.md
git commit -m "docs: update task hardening verification notes"
```

---

## Verification Checklist Before Completion

- Task permission tests pass.
- Task worklog validation tests pass.
- Frontend type-check passes.
- Manual worklog edge case check completed.
- No unrelated file modifications included.
