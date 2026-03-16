# PRM Action Permission Enforcement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Enforce first-batch action permissions on admin management pages and core requirement/bug flows so users with module access only see the buttons they are actually allowed to use.

**Architecture:** Seed action permission codes into `sys_permission`, keep permission evaluation centralized in the existing auth store, and add lightweight `canAccess(actionCode)` checks to the approved pages. Reuse route metadata for create/edit entry pages where direct URL access should also be blocked.

**Tech Stack:** SQLite migrations, Vue 3 SFC, Pinia, Vue Router, TypeScript, `vue-tsc`.

---

### Task 1: Seed action permission codes

**Files:**
- Create: `backend/src/main/resources/db/migration/V21__seed_action_permissions.sql`
- Modify: `frontend/src/utils/permission.ts`

**Step 1: Add the first-batch action permission codes**

**Step 2: Keep a central frontend mapping for the same codes**

### Task 2: Enforce admin page actions

**Files:**
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`

**Step 1: Hide unauthorized admin action buttons**

**Step 2: Guard action handlers against direct invocation**

### Task 3: Enforce requirement and bug actions

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/views/bug/BugPage.vue`
- Modify: `frontend/src/views/bug/BugDetailPage.vue`

**Step 1: Hide unauthorized create/edit/assign/delete actions**

**Step 2: Guard critical handlers against direct invocation**

### Task 4: Block direct route access for action pages

**Files:**
- Modify: `frontend/src/router/index.ts`

**Step 1: Add action permission metadata to create/edit pages**

### Task 5: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n "ACTION_PERMISSION_MAP|requiredPermissions|department:create|user:update|permission:assign|requirement:create|bug:assign" frontend/src backend/src/main/resources/db/migration`

**Step 3:** `npm.cmd run build` (workdir: `frontend`)

Expected:
- frontend type-check passes
- action permission wiring is visible across the targeted pages
- build either passes or still hits the known sandbox `spawn EPERM` blocker
