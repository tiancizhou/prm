# Requirement Done Verification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Require structured verification results when a requirement is moved to `DONE`, with local datetime rules and role-based edit permissions.

**Architecture:** Extend requirement domain storage with verification fields, enforce `DONE` transition validation in backend service, and update Requirement page done-status dialog to collect template-based verification data. Keep existing manager/assignee permission model and preserve verification fields when status is reopened.

**Tech Stack:** Spring Boot + MyBatis-Plus + Flyway + Vue 3 + Element Plus + Vitest/JUnit.

---

### Task 1: Database and Domain Fields

**Files:**
- Create: `backend/src/main/resources/db/migration/V12__requirement_add_done_verification_fields.sql`
- Modify: `backend/src/main/java/com/prm/module/requirement/entity/Requirement.java`
- Modify: `backend/src/main/java/com/prm/module/requirement/dto/RequirementDTO.java`

**Step 1: Write failing test expectations**
- Extend requirement service tests to assert verification fields are persisted for `DONE` updates.

**Step 2: Add migration columns**
- Add nullable columns for `verification_scenario`, `verification_steps`, `verification_result`, `verification_conclusion`, `verification_method`.

**Step 3: Update entity/DTO**
- Add matching Java fields and DTO mapping support.

### Task 2: Service and Controller Contract

**Files:**
- Modify: `backend/src/main/java/com/prm/module/requirement/controller/RequirementController.java`
- Modify: `backend/src/main/java/com/prm/module/requirement/application/RequirementService.java`
- Modify: `backend/src/test/java/com/prm/module/requirement/RequirementControllerDateTimeParsingTests.java`
- Modify: `backend/src/test/java/com/prm/module/requirement/RequirementServicePermissionTests.java`

**Step 1: Extend status API input**
- Accept verification template fields in `updateStatus` request params.

**Step 2: Enforce validation on DONE**
- Require start/end datetime plus 4 template fields when target status is `DONE`.
- Keep local datetime-only parsing and reject timezone suffixes.

**Step 3: Preserve on reopen**
- Do not clear verification fields when moving away from `DONE`.

**Step 4: Add/adjust tests**
- Add failing tests for missing verification fields on `DONE`.
- Add passing tests for complete payload and persistence capture.

### Task 3: Frontend Done Dialog and Payload

**Files:**
- Modify: `frontend/src/api/requirement.ts`
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/constants/requirement.ts`

**Step 1: Extend API helper**
- Add optional verification fields to `updateStatus` params.

**Step 2: Update done dialog UI**
- Add template form fields: scenario, steps, result, conclusion, optional method.

**Step 3: Client-side validation**
- Require all mandatory template fields and start/end datetime before submit.

**Step 4: Detail display**
- Show verification fields in requirement detail panel.

### Task 4: Verification and Delivery

**Files:**
- Modify (if needed): `backend/src/test/java/com/prm/module/requirement/*`

**Step 1: Run targeted tests**
- `backend\mvnw.cmd -Dtest=RequirementControllerDateTimeParsingTests,RequirementServicePermissionTests test`

**Step 2: Sanity-check frontend build scope**
- Run minimal frontend type/build check if available.

**Step 3: Commit and push**
- Commit only code/test/plan changes (exclude local DB artifacts).

