# PRM Admin Permissions Real Data Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Turn `后台 > 权限` into a real role-group management page backed by `sys_role`, `sys_user`, and `sys_user_role`, matching the approved reference-table structure.

**Architecture:** Add a focused admin role management backend (`list/detail/create/update/delete`) on top of the existing role tables, then refactor `AdminPermissionsPage` to load real roles plus real members and expose minimal real actions (`查看 / 成员 / 编辑 / 删除`) while keeping `权限 / 复制` as reserved placeholders.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3 SFC, TypeScript, Element Plus, JUnit 5, `vue-tsc`.

---

### Task 1: Add failing backend tests for role-group management

**Files:**
- Create: `backend/src/test/java/com/prm/module/system/RoleAdminServiceTests.java`

**Step 1: Write the failing tests**

- Assert role rows can be listed with description fallback
- Assert deleting a role with bound users is rejected
- Assert deleting a role without users succeeds

**Step 2: Run tests to verify RED**

Run:
- `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleAdminServiceTests test` (workdir: `backend`)

Expected: FAIL before implementation, or fail due the current Java 21 environment blocker.

### Task 2: Add backend role admin DTOs, service, and controller

**Files:**
- Create: `backend/src/main/java/com/prm/module/system/dto/RoleGroupRowDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/RoleGroupDetailDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/SaveRoleGroupRequest.java`
- Create: `backend/src/main/java/com/prm/module/system/application/RoleAdminService.java`
- Create: `backend/src/main/java/com/prm/module/system/controller/RoleAdminController.java`
- Modify: `backend/src/main/java/com/prm/module/system/mapper/SysUserMapper.java`

**Step 1: Write the minimal backend shape**

- `GET /api/admin/roles`
- `GET /api/admin/roles/{id}`
- `POST /api/admin/roles`
- `PUT /api/admin/roles/{id}`
- `DELETE /api/admin/roles/{id}`

**Step 2: Implement deletion guard**

- Refuse delete when any `sys_user_role` points to the role

**Step 3: Use real role description and real member names**

- Description fallback: `description ?? name`
- Member names: `realName`, fallback `username`

### Task 3: Add frontend role API wrapper and real table mapping

**Files:**
- Create: `frontend/src/api/adminRole.ts`
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`

**Step 1: Replace the current derived rows with real admin-role rows**

- Load the new backend list endpoint
- Keep the approved table columns:
  - `ID`
  - `分组名称`
  - `分组描述`
  - `用户列表`
  - `操作`

**Step 2: Keep `权限 / 复制` as reserved actions**

- Show existing buttons
- Use a lightweight placeholder message instead of fake behavior

### Task 4: Add minimal real dialogs and actions

**Files:**
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Add view dialog**

- Show role name, code, description, and member list

**Step 2: Add members dialog**

- Show only real users bound to the role

**Step 3: Add create/edit dialog**

- Support name and description
- Keep code editable on create; lock or preserve it on edit based on existing backend behavior

**Step 4: Add delete flow**

- Confirm before delete
- Surface backend refusal message when users are still bound

### Task 5: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n "/api/admin/roles|RoleAdminController|AdminPermissionsPage|adminRole" backend/src frontend/src`

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleAdminServiceTests test` (workdir: `backend`)

Expected:
- frontend type-check passes
- role management paths are wired consistently
- backend test either passes in a Java 21 environment or is blocked by the known Java 17 vs Java 21 compiler mismatch

**Step 4:** `npm.cmd run build` (workdir: `frontend`)

Expected:
- PASS, or the existing sandbox `spawn EPERM` blocker is recorded explicitly
