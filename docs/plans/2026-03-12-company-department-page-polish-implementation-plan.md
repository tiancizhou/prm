# PRM Company and Department Page Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refine `组织 > 公司` and `后台 > 部门` into a unified mixed-mode workflow where the top panel edits the current node and the bottom panel quickly maintains direct child departments.

**Architecture:** Reuse the existing company and department APIs, keep company editing owned by `OrganizationCompanyPage`, keep structure editing owned by `AdminDepartmentPage`, and align both pages around the same right-side two-card pattern. Add lightweight unsaved-change state, direct-child batch editing, and clear path context without changing the global shell.

**Tech Stack:** Vue 3 SFC, TypeScript, Element Plus, existing PRM API wrappers, shared workspace CSS, `vue-tsc`.

---

### Task 1: Finalize shared interaction copy and page structure

**Files:**
- Modify: `frontend/src/views/organization/OrganizationCompanyPage.vue`
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`
- Modify: `frontend/src/constants/organization.ts`

**Step 1: Document the final panel labels and helper text in code**

- Add exact copy for:
  - current node panel title
  - direct child panel title
  - company-only guidance
  - unsaved changes hint

**Step 2: Run type-check to confirm the copy changes are wired**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 2: Refactor `组织 > 公司` into mixed mode

**Files:**
- Modify: `frontend/src/views/organization/OrganizationCompanyPage.vue`
- Modify: `frontend/src/api/adminDepartment.ts`
- Modify: `frontend/src/api/organizationCompany.ts`

**Step 1: Add a current-company card**

- Keep the existing company profile form
- Add top-level summary state and explicit save affordance

**Step 2: Add a direct-department quick editor below the company form**

- Load root departments from the company-rooted tree
- Show existing departments as editable rows
- Keep several empty rows ready for quick add

**Step 3: Add delete confirmation for clearing existing rows**

- Track rows that map to real department IDs
- Confirm before deleting saved rows when the user clears them

**Step 4: Verify the page still type-checks**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 3: Refactor `后台 > 部门` top panel into current-node editor

**Files:**
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`

**Step 1: Replace the breadcrumb-only header with a real current-node card**

- For `COMPANY` nodes: show summary + guidance to edit in `组织 > 公司`
- For `DEPARTMENT` nodes: show editable fields for name, parent, status, sort

**Step 2: Keep the direct-child quick editor as the lower panel**

- Preserve the current batch-edit speed
- Make the parent target obvious from the current-node card

**Step 3: Add unsaved-change tracking**

- Detect edits in the top card or child rows
- Warn before discarding by switching tree nodes

**Step 4: Verify the page still type-checks**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 4: Align the two pages visually

**Files:**
- Modify: `frontend/src/views/organization/OrganizationCompanyPage.vue`
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Modify: `frontend/src/styles/base.css`

**Step 1: Introduce or reuse shared two-card spacing styles**

- Keep one visual language for current-node card and direct-child card
- Avoid page-specific one-off spacing where shared classes work

**Step 2: Add path and helper-text hierarchy**

- Make current path secondary
- Make primary object title and action area stronger

**Step 3: Re-run type-check after style/template changes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 5: Structural verification

**Files:**
- Modify: `docs/plans/2026-03-12-company-department-page-polish-design.md` (only if wording changes during implementation)

**Step 1: Run type-check**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 2: Run route/API wiring checks**

Run:
- `rg -n "OrganizationCompanyPage|OrganizationCompany|/organization/company|/api/organization/company|companyId|nodeType" frontend/src backend/src`

Expected: all new page and tree semantics remain connected.

**Step 3: Run production build if sandbox allows**

Run: `npm.cmd run build`

Expected: either PASS or the existing `spawn EPERM` sandbox blocker; record the exact result.

**Step 4: Do not commit automatically**

- Keep the branch/worktree cleanly modified for user review
- Only commit if the user explicitly requests it later
