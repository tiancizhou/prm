# PRM Organization and Admin Domain Shell Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a reusable blue top-navigation domain shell for the organization and admin sections while preserving the current workbench shell for dashboard and project flows.

**Architecture:** Keep `MainLayout` as the global left-navigation shell, then nest `/organization/*` and `/admin/*` under a new `DomainTopNavLayout`. Reuse the existing people-management page for organization users, keep lightweight structured placeholder pages for other submodules, and hide the workbench header when a domain shell is active.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, CSS Variables, `vue-tsc`.

---

### Task 1: Add the domain top-nav layout

**Files:**
- Create: `frontend/src/layouts/DomainTopNavLayout.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "DomainTopNavLayout|domain-topbar|domain-tab" frontend/src/layouts --glob "*.vue"
```

**Step 2: Verify RED**

Expected: no matches.

**Step 3: Write minimal implementation**

Create a reusable layout with:
- blue top bar
- domain title
- horizontal tabs
- right-side tools
- nested `router-view`

**Step 4: Verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

### Task 2: Restructure organization routes under the domain shell

**Files:**
- Modify: `frontend/src/router/index.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "domainShell|OrganizationTeams|OrganizationUsers|OrganizationRoles" frontend/src/router/index.ts
```

**Step 2: Verify RED**

Expected: organization routes are not nested under a domain shell yet.

**Step 3: Write minimal implementation**

Nest organization routes under `/organization` with default redirect to `/organization/teams`.

**Step 4: Verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

### Task 3: Add admin routes and placeholder pages

**Files:**
- Create: `frontend/src/constants/admin.ts`
- Create: `frontend/src/views/admin/AdminPlaceholderPage.vue`
- Modify: `frontend/src/router/index.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "AdminSystem|AdminActivity|AdminCompany|/admin/" frontend/src/router/index.ts frontend/src/views frontend/src/constants --glob "*.vue" --glob "*.ts"
```

**Step 2: Verify RED**

Expected: no admin domain pages or routes exist.

**Step 3: Write minimal implementation**

Add `/admin/system`, `/admin/activity`, `/admin/company`, each under the new domain shell.

**Step 4: Verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

### Task 4: Rewire the global left navigation

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "globalNavLabels\.admin|/admin/system|usesDomainShell|is-domain-shell" frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Expected: admin is not yet a first-class global nav entry and the workbench header still renders for all pages.

**Step 3: Write minimal implementation**

Add a real `后台` entry and hide the workbench header when `domainShell` routes are active.

**Step 4: Verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

### Task 5: Final verification

**Step 1:** `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
**Step 2:** `rg -n "DomainTopNavLayout|domainShell|AdminSystem|AdminActivity|AdminCompany|/admin/" frontend/src/router/index.ts frontend/src/layouts frontend/src/views frontend/src/constants --glob "*.vue" --glob "*.ts"`
**Step 3:** `npm.cmd run build` (workdir: `frontend`) and record sandbox blockers if `spawn EPERM` remains.
