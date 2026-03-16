# PRM Role Model Simplification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Shrink the active system role model to `SUPER_ADMIN`, `PROJECT_ADMIN`, and `DEV`, while safely migrating existing `PM`, `QA`, and `GUEST` assignments into the retained roles.

**Architecture:** Add a migration that rewrites `sys_user_role` and `sys_role_permission` bindings from deprecated roles to the retained roles, then removes the old roles. Keep the runtime permission system intact and simplify only the role dataset and UI presentation. Update active frontend displays that still assume `GUEST` or other removed roles.

**Tech Stack:** SQLite migrations, Spring Boot, MyBatis-Plus, Vue 3 SFC, TypeScript, `vue-tsc`, JUnit 5.

---

### Task 1: Add failing migration verification

**Files:**
- Modify: `backend/src/test/java/com/prm/infra/FlywayMigrationTests.java`

**Step 1: Write the failing test**

- Assert only `SUPER_ADMIN`, `PROJECT_ADMIN`, and `DEV` remain active in `sys_role`
- Assert `PM`, `QA`, and `GUEST` no longer remain active

**Step 2: Run test to verify RED**

Run:
- `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=FlywayMigrationTests test` (workdir: `backend`)

Expected: FAIL before migration, or fail due the known Java 21 blocker.

### Task 2: Add role-convergence migration

**Files:**
- Create: `backend/src/main/resources/db/migration/V23__shrink_roles_to_core.sql`

**Step 1: Migrate user-role bindings**

- `PM -> PROJECT_ADMIN`
- `QA -> DEV`
- `GUEST -> DEV`

**Step 2: Migrate role-permission bindings**

- Same mapping as above

**Step 3: Remove deprecated roles**

- `PM`
- `QA`
- `GUEST`

### Task 3: Simplify active frontend role assumptions

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `backend/src/test/java/com/prm/module/system/RoleAdminServiceTests.java`

**Step 1: Remove `GUEST`-based external filter assumption**

**Step 2: Update visible role labels/tags to align with the 3-role model**

**Step 3: Update any tests that still use removed role codes as active examples**

### Task 4: Verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 2:** `rg -n --glob '!backend/src/main/resources/static/**' "\bPM\b|QA|GUEST" backend/src frontend/src`

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=FlywayMigrationTests,RoleAdminServiceTests test` (workdir: `backend`)

Expected:
- frontend type-check passes
- active source no longer depends on the deprecated roles, aside from historical design docs if retained
- backend test either passes in Java 21 or remains blocked by the known environment mismatch
