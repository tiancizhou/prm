# User Project Scope Data Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add real project-scope data so the admin users page can show joined-project counts and on-demand project scope details, while the project members page can show the real project join time.

**Architecture:** Extend the existing backend DTO and service chain instead of creating a parallel data model. The user list continues to use `UserDTO`, but now includes a lightweight `joinedProjectCount`; a new dedicated user project-scope endpoint returns the heavy project list only when the frontend opens the detail drawer. The existing project members endpoint is extended by mapping `pm_project_member.created_at` into `ProjectMemberVO.joinedAt`.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, TypeScript, Element Plus, JUnit 5, `vue-tsc`.

---

### Task 1: Extend backend DTOs for project scope data

**Files:**
- Modify: `backend/src/main/java/com/prm/module/system/dto/UserDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/UserProjectScopeDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/UserJoinedProjectDTO.java`
- Modify: `backend/src/main/java/com/prm/module/project/dto/ProjectMemberVO.java`

**Step 1: Write the failing test**

Create `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java` with one test that expects `UserDTO` list results to expose `joinedProjectCount`, and create `backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java` with one test that expects `ProjectMemberVO.joinedAt` to be present in `getMemberVOs()` output.

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests,ProjectServiceMemberViewTests test`

Expected: FAIL because the DTO fields and/or mapping methods do not exist yet.

**Step 3: Write minimal implementation**

- Add `joinedProjectCount` to `UserDTO`
- Add new DTOs for user project scope detail
- Add `joinedAt` to `ProjectMemberVO`

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests,ProjectServiceMemberViewTests test`

Expected: PASS for the DTO-level expectations.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/system/dto/UserDTO.java backend/src/main/java/com/prm/module/system/dto/UserProjectScopeDTO.java backend/src/main/java/com/prm/module/system/dto/UserJoinedProjectDTO.java backend/src/main/java/com/prm/module/project/dto/ProjectMemberVO.java backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java
git commit -m "feat: add project scope dto fields"
```

### Task 2: Add joined-project count to the user list service

**Files:**
- Modify: `backend/src/main/java/com/prm/module/system/application/UserService.java`
- Modify: `backend/src/test/java/com/prm/module/system/UserServiceDepartmentScopeTests.java`
- Modify: `backend/src/test/java/com/prm/module/system/UserServiceDeleteTests.java`
- Modify: `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java`

**Step 1: Write the failing test**

In `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java`, add a test that:

- returns two users from `userMapper.selectPage(...)`
- returns project-member rows for those users
- expects `userService.page(...)` to map real counts into `joinedProjectCount`

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests test`

Expected: FAIL because `UserService.page()` does not yet populate the count.

**Step 3: Write minimal implementation**

- Inject `ProjectMemberMapper` into `UserService`
- Batch query project-member rows for the page user IDs
- Compute counts in memory and set `joinedProjectCount` in `toDTO(...)` or in a post-conversion enrichment step
- Update existing `UserService` tests so constructor wiring still compiles after adding the new dependency

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests,UserServiceDepartmentScopeTests,UserServiceDeleteTests test`

Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/system/application/UserService.java backend/src/test/java/com/prm/module/system/UserServiceDepartmentScopeTests.java backend/src/test/java/com/prm/module/system/UserServiceDeleteTests.java backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java
git commit -m "feat: expose joined project counts for users"
```

### Task 3: Add a dedicated backend endpoint for user project scope detail

**Files:**
- Modify: `backend/src/main/java/com/prm/module/system/application/UserService.java`
- Modify: `backend/src/main/java/com/prm/module/system/controller/UserController.java`
- Modify: `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java`

**Step 1: Write the failing test**

Add a test to `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java` that expects:

- `userService.getProjectScope(userId)` returns the user roles and joined projects
- joined projects include `id`, `name`, and `code`

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests test`

Expected: FAIL because the new service method and controller endpoint do not exist yet.

**Step 3: Write minimal implementation**

- Inject `ProjectMapper` into `UserService`
- Add `getProjectScope(Long id)`
- Create `GET /api/system/users/{id}/project-scope` in `UserController`
- Reuse existing user and role data instead of creating a second identity model

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests test`

Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/system/application/UserService.java backend/src/main/java/com/prm/module/system/controller/UserController.java backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java
git commit -m "feat: add user project scope detail endpoint"
```

### Task 4: Extend project members output with joined time

**Files:**
- Modify: `backend/src/main/java/com/prm/module/project/application/ProjectService.java`
- Modify: `backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java`

**Step 1: Write the failing test**

In `backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java`, add a test that:

- returns one `ProjectMember` with `createdAt`
- calls `projectService.getMemberVOs(projectId)`
- expects the mapped `ProjectMemberVO.joinedAt` to equal the member `createdAt`

**Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ProjectServiceMemberViewTests test`

Expected: FAIL because `joinedAt` is not yet mapped.

**Step 3: Write minimal implementation**

- In `ProjectService#getMemberVOs(...)`, map `member.getCreatedAt()` into `vo.setJoinedAt(...)`

**Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=ProjectServiceMemberViewTests,ProjectServicePermissionTests test`

Expected: PASS.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/project/application/ProjectService.java backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java
git commit -m "feat: expose project member joined time"
```

### Task 5: Consume joined-project count and project scope detail in the admin users page

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Write the failing check**

Run: `Select-String -Path 'frontend/src/views/admin/AdminUsersPage.vue' -Pattern 'joinedProjectCount|project scope|drawer|已加入项目|joined projects'`

Expected: no matches for the new real-data UI yet.

**Step 2: Run type check to establish baseline**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS before changes.

**Step 3: Write minimal implementation**

- Add a real `已加入项目` list column that consumes `row.joinedProjectCount`
- Add a right-side detail drawer that loads `/system/users/{id}/project-scope` on demand
- Show user identity, system roles, and joined project list in the drawer
- Reuse the existing wording model; do not add any project-role field

**Step 4: Run type check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue frontend/src/constants/adminPeople.ts
git commit -m "feat: show user project scope details"
```

### Task 6: Consume joined time in the project members page

**Files:**
- Modify: `frontend/src/api/project.ts`
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/constants/projectMembers.ts`

**Step 1: Write the failing check**

Run: `Select-String -Path 'frontend/src/api/project.ts','frontend/src/views/project/ProjectMembersPage.vue' -Pattern 'joinedAt|加入时间|Joined Time'`

Expected: no matches for the new join-time field yet.

**Step 2: Run type check to establish baseline**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS before changes.

**Step 3: Write minimal implementation**

- Add a typed project member interface in `frontend/src/api/project.ts` that includes `joinedAt`
- Add a `加入时间 / Joined Time` column to the members table
- Format missing values as `—`

**Step 4: Run type check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/api/project.ts frontend/src/views/project/ProjectMembersPage.vue frontend/src/constants/projectMembers.ts
git commit -m "feat: show project member joined time"
```

### Task 7: Final verification and documentation cleanup

**Files:**
- Verify: `backend/src/main/java/com/prm/module/system/dto/UserDTO.java`
- Verify: `backend/src/main/java/com/prm/module/system/application/UserService.java`
- Verify: `backend/src/main/java/com/prm/module/system/controller/UserController.java`
- Verify: `backend/src/main/java/com/prm/module/project/dto/ProjectMemberVO.java`
- Verify: `backend/src/main/java/com/prm/module/project/application/ProjectService.java`
- Verify: `frontend/src/views/admin/AdminUsersPage.vue`
- Verify: `frontend/src/views/project/ProjectMembersPage.vue`

**Step 1: Run backend regression search**

Run: `Select-String -Path 'backend/src/main/java/**/*.java' -Pattern 'joinedProjectCount|project-scope|joinedAt'`

Expected: matches in the intended DTO, service, controller, and project-member files.

**Step 2: Run frontend regression search**

Run: `Select-String -Path 'frontend/src/views/**/*.vue','frontend/src/api/**/*.ts' -Pattern 'joinedProjectCount|project-scope|joinedAt|已加入项目|加入时间'`

Expected: matches in the admin users page, project members page, and project API typing.

**Step 3: Run frontend type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 4: Run backend verification if the environment allows**

Run: `mvn -q -Dtest=UserServiceProjectScopeTests,UserServiceDepartmentScopeTests,UserServiceDeleteTests,ProjectServiceMemberViewTests,ProjectServicePermissionTests test`

Expected: PASS. If the environment still blocks Maven or dependency resolution, record the exact limitation and do not claim backend tests passed.

**Step 5: Commit**

```bash
git add backend/src/main/java/com/prm/module/system/dto/UserDTO.java backend/src/main/java/com/prm/module/system/dto/UserProjectScopeDTO.java backend/src/main/java/com/prm/module/system/dto/UserJoinedProjectDTO.java backend/src/main/java/com/prm/module/system/application/UserService.java backend/src/main/java/com/prm/module/system/controller/UserController.java backend/src/main/java/com/prm/module/project/dto/ProjectMemberVO.java backend/src/main/java/com/prm/module/project/application/ProjectService.java backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java frontend/src/api/project.ts frontend/src/views/admin/AdminUsersPage.vue frontend/src/constants/adminPeople.ts frontend/src/views/project/ProjectMembersPage.vue frontend/src/constants/projectMembers.ts
git commit -m "feat: expose real project scope data in admin and member views"
```
