# PRM Module Permission Assignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add real module-level permission assignment for role groups via the existing `sys_permission` and `sys_role_permission` tables, exposed from the permissions page through a role-first assignment dialog.

**Architecture:** Seed stable `MODULE` permissions, add focused admin permission APIs for listing modules plus reading/writing role-module assignments, then wire `AdminPermissionsPage` to open a large dialog with role selection on the left and module checkboxes on the right. The dialog saves the full module-permission set for one role at a time.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, SQLite, Vue 3 SFC, TypeScript, Element Plus, JUnit 5, `vue-tsc`.

---

### Task 1: Add failing backend tests for module permission assignment

**Files:**
- Create: `backend/src/test/java/com/prm/module/system/RoleModulePermissionServiceTests.java`

**Step 1: Write the failing tests**

- Assert module permissions list is filtered by `type = MODULE`
- Assert saving role module permissions replaces prior module assignments for that role
- Assert non-module assignments are preserved when updating module permissions

**Step 2: Run tests to verify RED**

Run:
- `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleModulePermissionServiceTests test` (workdir: `backend`)

Expected: FAIL before implementation, or fail due the known Java 21 environment blocker.

### Task 2: Add permission entities, mappers, and seed migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V20__seed_module_permissions.sql`
- Create: `backend/src/main/java/com/prm/module/system/entity/SysPermission.java`
- Create: `backend/src/main/java/com/prm/module/system/entity/SysRolePermission.java`
- Create: `backend/src/main/java/com/prm/module/system/mapper/SysPermissionMapper.java`
- Create: `backend/src/main/java/com/prm/module/system/mapper/SysRolePermissionMapper.java`

### Task 3: Add backend module permission admin service and controller

**Files:**
- Create: `backend/src/main/java/com/prm/module/system/dto/ModulePermissionDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/SaveRoleModulePermissionsRequest.java`
- Create: `backend/src/main/java/com/prm/module/system/application/RoleModulePermissionService.java`
- Create: `backend/src/main/java/com/prm/module/system/controller/RoleModulePermissionController.java`

**Step 1: Add list/read/save APIs**

- `GET /api/admin/permissions/modules`
- `GET /api/admin/roles/{id}/module-permissions`
- `PUT /api/admin/roles/{id}/module-permissions`

**Step 2: Implement replace-only-module behavior**

- Remove existing `MODULE` bindings for that role
- Insert the submitted `MODULE` bindings
- Keep any future non-module permission bindings intact

### Task 4: Wire the permissions page dialog

**Files:**
- Create: `frontend/src/api/adminPermission.ts`
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Add module assignment dialog**

- Left: real role list
- Right: real module permission checkboxes

**Step 2: Load current role assignments when the selected role changes**

- Use the backend assignment endpoint

**Step 3: Save the updated role assignments**

- Submit the full selected module permission code list

### Task 5: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n "/api/admin/permissions/modules|module-permissions|RoleModulePermissionService|adminPermission" backend/src frontend/src`

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleModulePermissionServiceTests test` (workdir: `backend`)

**Step 4:** `npm.cmd run build` (workdir: `frontend`)

Expected:
- frontend type-check passes
- module permission paths are wired consistently
- backend test either passes in a Java 21 environment or is blocked by the known Java mismatch
