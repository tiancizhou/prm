# PRM Project Role Removal Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove project-role-based authorization and make project membership only represent data scope, while system roles (`SUPER_ADMIN`, `PROJECT_ADMIN`, `DEV`) become the sole source of project-domain capabilities.

**Architecture:** Introduce a shared `ProjectAccessPolicy`, switch project-domain services from `membership.getRole()` checks to `membership + system role` checks, normalize `pm_project_member.role` values to `MEMBER`, and keep the existing membership table for data scoping only.

**Tech Stack:** Spring Boot 3, SQLite migrations, Vue 3 UI consistency checks, JUnit 5, `vue-tsc`.

---

### Task 1: Add shared project access policy and normalize project member role values

**Files:**
- Create: `backend/src/main/java/com/prm/module/project/domain/ProjectAccessPolicy.java`
- Create: `backend/src/main/resources/db/migration/V24__normalize_project_member_roles.sql`

### Task 2: Replace project-role authorization in core services

**Files:**
- Modify: `backend/src/main/java/com/prm/module/project/application/ProjectService.java`
- Modify: `backend/src/main/java/com/prm/module/requirement/application/RequirementService.java`
- Modify: `backend/src/main/java/com/prm/module/bug/application/BugService.java`
- Modify: `backend/src/main/java/com/prm/module/task/application/TaskService.java`
- Modify: `backend/src/main/java/com/prm/module/module/application/ModuleService.java`
- Modify: `backend/src/main/java/com/prm/module/sprint/application/SprintService.java`
- Modify: `backend/src/main/java/com/prm/module/release/application/ReleaseService.java`
- Modify: `backend/src/main/java/com/prm/module/attachment/application/AttachmentService.java`
- Modify: `backend/src/main/java/com/prm/module/dashboard/application/DashboardService.java`

### Task 3: Keep tests aligned with new semantics

**Files:**
- Modify: representative project-domain permission tests where old project-role assumptions are encoded

### Task 4: Verification

**Step 1:** `rg -n 'getRole\(|PROJECT_ADMIN"\.equalsIgnoreCase\(membership.getRole' backend/src/main/java/com/prm/module`

**Step 2:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 3:** `mvn.cmd --% -Dmaven.repo.local=C:\Users\Bob\.m2\repository -q -Dtest=ProjectServicePermissionTests,RequirementServicePermissionTests,BugServicePermissionTests test` (workdir: `backend`)

Expected:
- no active backend project-domain auth path depends on project-member role anymore
- frontend still type-checks
- backend tests pass in a Java 21 environment or remain blocked by the known runtime mismatch
