# PRM Department Module Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a real hierarchical department module to the backend and wire the admin people-management department and users pages to it.

**Architecture:** Introduce a new `sys_department` model using a parent-child adjacency tree, add `department_id` to `sys_user`, backfill from legacy department text, expose admin department APIs, and update the admin frontend to use real department data instead of inferred text lists.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway, SQLite, Vue 3, Element Plus, `vue-tsc`, JUnit 5.

---

### Task 1: Add failing backend verification for migration and tree behavior

**Files:**
- Modify: `backend/src/test/java/com/prm/infra/FlywayMigrationTests.java`
- Create: `backend/src/test/java/com/prm/module/system/DepartmentServiceTests.java`

**Step 1: Write the failing tests**

- Assert `sys_department` exists and `sys_user.department_id` exists
- Assert department tree building groups child departments correctly
- Assert delete rejects when child departments or users exist

**Step 2: Run tests to verify RED**

Run:
- `mvnw.cmd -q -Dtest=FlywayMigrationTests,DepartmentServiceTests test`

Expected: FAIL because the table, column, and service do not exist yet.

### Task 2: Add migration and backend department model

**Files:**
- Create: `backend/src/main/resources/db/migration/V18__add_department_module.sql`
- Create: `backend/src/main/java/com/prm/module/system/entity/SysDepartment.java`
- Create: `backend/src/main/java/com/prm/module/system/mapper/SysDepartmentMapper.java`
- Modify: `backend/src/main/java/com/prm/module/system/entity/SysUser.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/UserDTO.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/CreateUserRequest.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/UpdateUserRequest.java`

### Task 3: Add backend department APIs and user department support

**Files:**
- Create: `backend/src/main/java/com/prm/module/system/dto/DepartmentTreeDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/DepartmentDetailDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/SaveDepartmentRequest.java`
- Create: `backend/src/main/java/com/prm/module/system/application/DepartmentService.java`
- Create: `backend/src/main/java/com/prm/module/system/controller/DepartmentAdminController.java`
- Modify: `backend/src/main/java/com/prm/module/system/application/UserService.java`
- Modify: `backend/src/main/java/com/prm/module/system/controller/UserController.java`

### Task 4: Wire admin department and users pages to real APIs

**Files:**
- Create: `frontend/src/api/adminDepartment.ts`
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`

### Task 5: Run verification

**Step 1:** `backend\mvnw.cmd -q -Dtest=FlywayMigrationTests,DepartmentServiceTests test`
**Step 2:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
**Step 3:** `npm.cmd run build` (workdir: `frontend`) and record sandbox blockers if `spawn EPERM` remains.

