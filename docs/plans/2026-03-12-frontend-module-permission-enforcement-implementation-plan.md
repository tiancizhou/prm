# PRM Frontend Module Permission Enforcement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make assigned module permissions actually take effect in the frontend by feeding permission codes into the auth state, hiding unauthorized navigation, and blocking unauthorized route access while always allowing `SUPER_ADMIN`.

**Architecture:** Extend the backend auth payloads to include permission codes, store those codes centrally in the frontend auth store, add a small permission utility layer, annotate protected routes with module permission metadata, and have both layouts plus the router guard consume the same permission checks.

**Tech Stack:** Spring Boot 3, Sa-Token, Vue 3, Pinia, Vue Router, TypeScript, `vue-tsc`, JUnit 5.

---

### Task 1: Add failing backend auth tests for permission payloads

**Files:**
- Create: `backend/src/test/java/com/prm/module/auth/AuthServicePermissionTests.java`

**Step 1: Write the failing tests**

- Assert login response includes permission codes
- Assert current-user response includes permission codes

**Step 2: Run tests to verify RED**

Run:
- `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=AuthServicePermissionTests test` (workdir: `backend`)

Expected: FAIL before implementation, or fail due the known Java 21 blocker.

### Task 2: Extend backend auth payloads

**Files:**
- Modify: `backend/src/main/java/com/prm/module/system/mapper/SysUserMapper.java`
- Modify: `backend/src/main/java/com/prm/module/auth/dto/LoginResponse.java`
- Modify: `backend/src/main/java/com/prm/module/auth/application/AuthService.java`

**Step 1: Add permission-code query for a user**

**Step 2: Include permission codes in login and current-user responses**

### Task 3: Extend frontend auth store and permission helpers

**Files:**
- Modify: `frontend/src/api/auth.ts`
- Modify: `frontend/src/stores/auth.ts`
- Create: `frontend/src/utils/permission.ts`

**Step 1: Add permission codes to the frontend auth response type**

**Step 2: Add `isSuperAdmin`, `hasPermission`, and `hasAnyPermission` helpers**

### Task 4: Annotate routes and enforce route access

**Files:**
- Modify: `frontend/src/router/index.ts`

**Step 1: Add `requiredPermission` metadata to protected routes**

**Step 2: Update `beforeEach` to redirect unauthorized users to the first safe route**

### Task 5: Hide unauthorized navigation in layouts

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/layouts/DomainTopNavLayout.vue`

**Step 1: Filter global nav by module permission**

**Step 2: Filter project secondary nav by module permission**

**Step 3: Filter domain tabs by module permission where applicable**

### Task 6: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n "requiredPermission|permissions|hasPermission|isSuperAdmin" backend/src frontend/src`

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=AuthServicePermissionTests test` (workdir: `backend`)

**Step 4:** `npm.cmd run build` (workdir: `frontend`)

Expected:
- frontend type-check passes
- permission propagation is wired end-to-end
- backend test either passes in Java 21 or is blocked by the known Java mismatch
