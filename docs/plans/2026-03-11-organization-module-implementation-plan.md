# PRM Organization Module Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a real organization module entry to the PRM shell, create a lightweight organization hub page, route people management through the organization domain, and preserve `/system/users` as a compatibility redirect.

**Architecture:** Keep the existing user-management implementation, but reframe it under a new `/organization` route group. Add one organization home page, one lightweight shared placeholder page for future submodules, and update shell navigation plus route metadata so the organization domain feels native to the new triple-level layout.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, CSS Variables, `vue-tsc`.

---

### Task 1: Add organization route metadata and compatibility redirect

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "OrganizationHome|OrganizationUsers|OrganizationTeams|OrganizationRoles|/organization" frontend/src/router/index.ts frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no organization routes or route labels exist yet.

**Step 3: Write minimal implementation**

Add:

- `/organization`
- `/organization/users`
- `/organization/teams`
- `/organization/roles`
- redirect `/system/users` -> `/organization/users`

Also add route label keys for the new pages.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: PASS.

### Task 2: Create the organization home hub page

**Files:**
- Create: `frontend/src/views/organization/OrganizationHomePage.vue`
- Create: `frontend/src/constants/organization.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "organization-hub|organization-card|OrganizationHomePage" frontend/src/views frontend/src/constants
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches.

**Step 3: Write minimal implementation**

Create a hub page with:

- title + subtitle
- cards for people, teams, and roles
- active badge on people card
- coming soon badge on teams and roles

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: PASS.

### Task 3: Add organization placeholder subpages

**Files:**
- Create: `frontend/src/views/organization/OrganizationPlaceholderPage.vue`
- Modify: `frontend/src/constants/organization.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "OrganizationPlaceholderPage|placeholder-badge|backToOrganization" frontend/src/views/organization frontend/src/constants/organization.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches.

**Step 3: Write minimal implementation**

Create a reusable placeholder page for:

- `/organization/teams`
- `/organization/roles`

Include title, description, status badge, and a button back to `/organization`.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: PASS.

### Task 4: Rewire shell navigation to the organization domain

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "organization.*disabled|/organization|settings.*disabled" frontend/src/layouts/MainLayout.vue
```

**Step 2: Verify RED**

Run the command above.
Expected: the organization item is still disabled or not wired correctly.

**Step 3: Write minimal implementation**

Update global nav so:

- `organization` points to `/organization`
- `organization` is visible for the same audience as the current system user-management entry
- `settings` no longer routes to people management

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: PASS.

### Task 5: Adapt people-management page copy for organization routing

**Files:**
- Modify: `frontend/src/views/system/UserPage.vue`
- Modify: `frontend/src/constants/organization.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "OrganizationUsers|人员管理|People Management" frontend/src/views/system/UserPage.vue frontend/src/constants/organization.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no organization-aware copy path exists yet.

**Step 3: Write minimal implementation**

Detect the organization route in `UserPage.vue` and switch the header title/subtitle to organization-aware copy while keeping the existing table logic unchanged.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: PASS.

### Task 6: Final verification

**Files:**
- Modify: `docs/plans/2026-03-11-organization-module-design.md` (only if minor notes change)

**Step 1: Run type-check**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
Expected: PASS.

**Step 2: Run structural checks**

Run:
- `rg -n "OrganizationHome|OrganizationUsers|OrganizationTeams|OrganizationRoles|/organization" frontend/src/router/index.ts frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts frontend/src/views --glob "*.vue" --glob "*.ts"`

Expected: all organization routes and pages exist.

**Step 3: Attempt production build**

Run: `npm.cmd run build` (workdir: `frontend`)
Expected: if the sandbox still blocks Vite with `spawn EPERM`, record the environment blocker explicitly.

---

## Verification Checklist Before Completion

- `组织` is a real global navigation entry.
- `/organization` exists as a lightweight hub page.
- `/organization/users` reuses the people-management page.
- `/organization/teams` and `/organization/roles` exist as structured placeholders.
- `/system/users` redirects to `/organization/users`.
- `vue-tsc` passes.
