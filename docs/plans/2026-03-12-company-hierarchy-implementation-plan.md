# PRM Company Hierarchy Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a real company model, make company the top-level root above departments, and wire the organization company page plus admin people-management pages to the new hierarchy.

**Architecture:** Introduce `sys_company` and `sys_department.company_id`, return company-rooted tree nodes from the admin department tree API, expose a focused organization company profile API, and update the organization/admin Vue pages to understand `COMPANY` vs `DEPARTMENT` nodes without changing the overall shell layout.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway, SQLite, Vue 3 SFC, TypeScript, Element Plus, JUnit 5, `vue-tsc`.

---

### Task 1: Add failing verification for company hierarchy

**Files:**
- Modify: `backend/src/test/java/com/prm/infra/FlywayMigrationTests.java`
- Modify: `backend/src/test/java/com/prm/module/system/DepartmentServiceTests.java`

**Step 1: Write the failing tests**

- Assert `sys_company` exists
- Assert `sys_department.company_id` exists
- Assert department tree roots are company nodes

**Step 2: Run tests to verify RED**

Run:
- `mvn -q -Dtest=FlywayMigrationTests,DepartmentServiceTests test` (workdir: `backend`)

Expected: FAIL before implementation, or fail due local Maven dependency constraints if the environment cannot resolve dependencies.

### Task 2: Add company migration and backend model

**Files:**
- Create: `backend/src/main/resources/db/migration/V19__add_company_hierarchy.sql`
- Create: `backend/src/main/java/com/prm/module/system/entity/SysCompany.java`
- Create: `backend/src/main/java/com/prm/module/system/mapper/SysCompanyMapper.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/CompanyProfileDTO.java`
- Create: `backend/src/main/java/com/prm/module/system/dto/SaveCompanyRequest.java`

### Task 3: Extend services and controllers for company-rooted trees

**Files:**
- Create: `backend/src/main/java/com/prm/module/system/application/CompanyService.java`
- Create: `backend/src/main/java/com/prm/module/system/controller/OrganizationCompanyController.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/DepartmentTreeDTO.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/DepartmentDetailDTO.java`
- Modify: `backend/src/main/java/com/prm/module/system/dto/SaveDepartmentRequest.java`
- Modify: `backend/src/main/java/com/prm/module/system/entity/SysDepartment.java`
- Modify: `backend/src/main/java/com/prm/module/system/application/DepartmentService.java`

### Task 4: Build the organization company maintenance page

**Files:**
- Create: `frontend/src/api/organizationCompany.ts`
- Create: `frontend/src/views/organization/OrganizationCompanyPage.vue`
- Modify: `frontend/src/router/index.ts`

### Task 5: Update admin department and user pages for company nodes

**Files:**
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/api/adminDepartment.ts` (types only if needed)

### Task 6: Run verification

**Step 1:** `mvn -q -Dtest=FlywayMigrationTests,DepartmentServiceTests test` (workdir: `backend`)

**Step 2:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

**Step 3:** `rg -n "OrganizationCompanyPage|/api/organization/company|nodeType|companyId" backend/src frontend/src`

Expected:
- backend paths are wired consistently
- frontend type-check passes
- if Maven remains blocked locally, record the exact blocker instead of claiming backend verification passed
