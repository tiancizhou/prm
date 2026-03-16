# PRM Module and Action Permission Assignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Upgrade the existing module permission assignment dialog into a two-level module-and-action permission editor that reads and writes real `MODULE` and `ACTION` permission bindings for a role.

**Architecture:** Keep the existing role-first assignment dialog, but change the backend permission listing to return module groups with action children based on `parent_id`. Update the save path to replace both `MODULE` and `ACTION` bindings while auto-including parent modules for selected actions. On the frontend, replace the flat module checkbox group with grouped cards and weak-linkage behavior.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, SQLite, Vue 3 SFC, TypeScript, Element Plus, JUnit 5, `vue-tsc`.

---

### Task 1: Add failing backend tests for grouped permission assignment

**Files:**
- Modify: `backend/src/test/java/com/prm/module/system/RoleModulePermissionServiceTests.java`

**Step 1: Write the failing tests**

- Assert module list includes grouped child actions
- Assert saving an action code auto-includes its parent module code
- Assert deleting old bindings removes both `MODULE` and `ACTION` assignments only

**Step 2: Run tests to verify RED**

Run:
- `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleModulePermissionServiceTests test` (workdir: `backend`)

Expected: FAIL before implementation, or fail due the known Java 21 blocker.

### Task 2: Link action permissions to modules in the database

**Files:**
- Create: `backend/src/main/resources/db/migration/V22__link_action_permissions_to_modules.sql`

**Step 1: Update `parent_id` for existing action permissions**

- Map requirement actions to `requirement:view`
- Map bug actions to `bug:view`
- Map admin management actions to `admin:view`

### Task 3: Upgrade backend permission grouping and save logic

**Files:**
- Modify: `backend/src/main/java/com/prm/module/system/dto/ModulePermissionDTO.java`
- Modify: `backend/src/main/java/com/prm/module/system/application/RoleModulePermissionService.java`

**Step 1: Return grouped permissions**

- Modules as parents
- Actions as `children`

**Step 2: Save both module and action codes**

- Validate codes against assignable permission set
- Auto-add parent module codes for selected actions

### Task 4: Upgrade frontend assignment dialog

**Files:**
- Modify: `frontend/src/api/adminPermission.ts`
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`

**Step 1: Update permission DTOs to include `children`**

**Step 2: Replace flat checkbox group with grouped cards**

**Step 3: Add weak linkage behavior**

- Selecting action selects module
- Unselecting module clears its actions

### Task 5: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n "children|parent_id|permissionCodes|module-and-action|permission-module-card" backend/src frontend/src`

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=RoleModulePermissionServiceTests test` (workdir: `backend`)

**Step 4:** `npm.cmd run build` (workdir: `frontend`)

Expected:
- frontend type-check passes
- grouped permission wiring is visible end-to-end
- backend test either passes in Java 21 or remains blocked by the known runtime mismatch
