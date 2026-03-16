# PRM Admin UI Alignment Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align the admin domain pages and shell details with the organization domain visual system while preserving the current admin information architecture and business interactions.

**Architecture:** Reuse the existing admin page structures and data flows, and converge them onto the same `page-header`, `surface-card`, `workspace-*`, helper-banner, and table rhythm already used by `OrganizationTeamPage` and `OrganizationCompanyPage`. Fix shell-level inconsistencies at the same time by making all admin child routes keep the left global-nav highlight and by adding explicit breadcrumb labels for admin child pages.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, existing PRM CSS variables and shared workspace styles, `vue-tsc`, Vite build.

---

### Task 1: Add route-label and active-menu expectations for the admin shell

**Files:**
- Modify: `frontend/src/constants/layout.ts`
- Modify: `frontend/src/layouts/MainLayout.vue`

**Step 1: Write the failing structural expectation**

Run:

```bash
rg -n "AdminDepartments|AdminUsers|AdminPermissions|activeMenu = computed" frontend/src/constants/layout.ts frontend/src/layouts/MainLayout.vue
```

Expected: `AdminDepartments`, `AdminUsers`, and `AdminPermissions` are missing from `routeLabels`, and `activeMenu` still maps directly to `route.path`.

**Step 2: Verify RED**

- Confirm the admin child route labels do not exist yet.
- Confirm the current active-menu logic is too literal to keep `/admin/users` and `/admin/permissions` highlighted under the global `后台` menu item.

**Step 3: Write minimal implementation**

- Add exact `routeLabels` entries for:
  - `AdminDepartments`
  - `AdminUsers`
  - `AdminPermissions`
- Update `activeMenu` so all `/admin/*` routes map to `/admin/departments` and all `/organization/*` routes map to `/organization/team`.

**Step 4: Run type-check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 2: Align the domain top-nav rhythm with the organization shell baseline

**Files:**
- Modify: `frontend/src/layouts/DomainTopNavLayout.vue`

**Step 1: Write the failing structural expectation**

Run:

```bash
rg -n "domain-topbar|domain-topbar-title|domain-tab.is-active|padding: 0 var\(--space-lg\)" frontend/src/layouts/DomainTopNavLayout.vue
```

Expected: the current domain-shell styles still use the older tighter spacing and tab rhythm.

**Step 2: Verify RED**

- Confirm the current top-nav shell has not yet been tuned to match the newer organization/admin page rhythm.

**Step 3: Write minimal implementation**

- Keep the existing title and tab interaction model.
- Adjust topbar spacing, title weight, tab padding, and active-state rhythm so the shell feels consistent with the current organization domain visual language.
- Avoid adding new features or tool areas.

**Step 4: Run type-check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 3: Converge `后台 > 部门` onto the organization-company card language

**Files:**
- Modify: `frontend/src/views/admin/AdminDepartmentPage.vue`

**Step 1: Write the failing structural expectation**

Run:

```bash
rg -n "department-sidebar|workspace-panel-head|workspace-node-card|child-editor-list|company-summary-text" frontend/src/views/admin/AdminDepartmentPage.vue
```

Expected: the page still uses its own side-panel and card presentation details rather than the fully aligned organization-company visual rhythm.

**Step 2: Verify RED**

- Confirm the left tree panel, current-node card, and child-editor card are still visually inconsistent with `OrganizationCompanyPage`.

**Step 3: Write minimal implementation**

- Keep the existing tree, current-node logic, and child-editor workflow unchanged.
- Refine the left panel width, padding, title/action rhythm, and tree spacing.
- Refine the right-side cards so path text, panel titles, helper text, tags, and footer action areas align with the organization-company page language.
- Keep unsaved-change affordances and route-leave protection untouched.

**Step 4: Run type-check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 4: Converge `后台 > 用户` onto the organization-team toolbar and table rhythm

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`

**Step 1: Write the failing structural expectation**

Run:

```bash
rg -n "users-toolbar|users-rule-banner|users-layout|users-table-panel|workspace-action-icons" frontend/src/views/admin/AdminUsersPage.vue
```

Expected: the page still uses the older toolbar, banner, side-panel, and table-panel presentation details.

**Step 2: Verify RED**

- Confirm the users page still looks older than `OrganizationTeamPage` even though the structure is already compatible.

**Step 3: Write minimal implementation**

- Keep filter/search/actions, department tree, table columns, row actions, dialogs, and drawer logic unchanged.
- Align toolbar spacing and control sizing with `OrganizationTeamPage`.
- Restyle the helper banner, side panel, table card, footer, and icon action cluster to match the organization-domain visual system.

**Step 4: Run type-check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 5: Converge `后台 > 权限` onto the organization-team panel language

**Files:**
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`

**Step 1: Write the failing structural expectation**

Run:

```bash
rg -n "permissions-toolbar|permissions-rule-banner|permissions-surface|permission-action-group|permission-assignment-layout" frontend/src/views/admin/AdminPermissionsPage.vue
```

Expected: the page still uses the older admin-specific panel styling rather than the same visual generation as organization pages.

**Step 2: Verify RED**

- Confirm the permission list surface and assignment dialog still feel visually detached from the organization-domain pages.

**Step 3: Write minimal implementation**

- Keep the role-group list, row actions, details dialog, members dialog, edit dialog, and permission-assignment dialog behavior unchanged.
- Align the top action bar, helper banner, table surface, action icons, and assignment-dialog cards with the organization-domain visual system.
- Preserve existing permission gating.

**Step 4: Run type-check to verify it passes**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

### Task 6: Run full verification for shell and page alignment

**Files:**
- Verify: `frontend/src/layouts/MainLayout.vue`
- Verify: `frontend/src/layouts/DomainTopNavLayout.vue`
- Verify: `frontend/src/views/admin/AdminDepartmentPage.vue`
- Verify: `frontend/src/views/admin/AdminUsersPage.vue`
- Verify: `frontend/src/views/admin/AdminPermissionsPage.vue`

**Step 1: Run type-check**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 2: Run production build**

Run: `npm.cmd run build`

Expected: PASS.

**Step 3: Run structural spot checks**

Run:

```bash
rg -n "AdminDepartments|AdminUsers|AdminPermissions|/admin/departments|/admin/users|/admin/permissions|users-rule-banner|permissions-rule-banner|department-sidebar" frontend/src
```

Expected: shell labels, routes, and updated page containers are all present.

**Step 4: Manual verification in the browser**

- Visit `/admin/departments`
- Visit `/admin/users`
- Visit `/admin/permissions`
- Visit `/organization/team`
- Visit `/organization/company`

Check:

- `后台` stays highlighted in the left global nav on all three admin child pages.
- Breadcrumbs show the exact admin child page label.
- The admin toolbar, helper banner, card padding, side-panel style, table panel, and footer rhythm feel aligned with organization pages.
- The admin top domain tabs still behave correctly.

**Step 5: Do not commit automatically**

- Keep the changes uncommitted for user review.
- Only create a git commit if the user explicitly requests it later.
