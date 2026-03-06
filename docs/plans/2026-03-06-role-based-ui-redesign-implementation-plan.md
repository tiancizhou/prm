# Role-Based PRM UI Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebuild the current PRM frontend into a role-based, web-first product workbench by redesigning the app shell, role homepage, project center, and the first batch of high-frequency pages without changing backend contracts.

**Architecture:** Keep the existing Vue 3 + Element Plus application skeleton and route tree, but reorganize the UI into a clearer global layer and project layer. Land the redesign in shared tokens and base styles first, then refactor `MainLayout`, then rebuild the role homepage and project-facing pages in descending usage order.

**Tech Stack:** Vue 3 SFC, TypeScript, Element Plus, Pinia, Vue Router, CSS Variables (`tokens.css`), Vite, `vue-tsc`.

---

## Implementation Constraints

- Web-first only; mobile-specific redesign is out of scope for this round.
- Do not change backend APIs, database schema, or permission model.
- Prefer reusing existing route paths and stores.
- Follow RED → GREEN verification where possible.
- Because this frontend has no dedicated UI test harness, use structural expectation checks (`rg`), `vue-tsc`, `vite build`, and manual page verification as the validation stack.

---

### Task 1: Establish redesign tokens and shared page-shell utilities

**Files:**
- Modify: `frontend/src/styles/tokens.css`
- Modify: `frontend/src/styles/base.css`

**Step 1: Write the failing structural expectation**

```bash
rg -n "role-home|status-strip|panel-grid|command-bar|project-context-bar" frontend/src/styles/tokens.css frontend/src/styles/base.css
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches, proving the new redesign primitives do not exist yet.

**Step 3: Write minimal implementation**

```css
/* tokens.css */
:root {
  --role-admin-accent: #415fcf;
  --role-pm-accent: #2870d8;
  --role-dev-accent: #1d9a8a;
  --role-qa-accent: #d97745;
  --role-guest-accent: #6f86a8;
  --app-panel-bg: color-mix(in srgb, var(--app-bg-surface) 88%, white);
}

/* base.css */
.status-strip { display: grid; gap: var(--space-md); }
.panel-grid { display: grid; gap: var(--space-lg); }
.command-bar { display: flex; align-items: center; justify-content: space-between; }
.project-context-bar { display: flex; align-items: center; gap: var(--space-md); }
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Expected: type-check and build both PASS.

**Step 5: Commit**

```bash
git add frontend/src/styles/tokens.css frontend/src/styles/base.css
git commit -m "style: add role-based ui redesign tokens and shell primitives"
```

### Task 2: Rebuild app shell navigation and top bar around global/project layers

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "project-switcher|global-search|quick-create|role-home" frontend/src/layouts/MainLayout.vue frontend/src/router/index.ts frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new shell affordances.

**Step 3: Write minimal implementation**

```ts
// router/index.ts
{
  path: 'dashboard',
  name: 'Dashboard',
  component: () => import('@/views/dashboard/DashboardPage.vue'),
  meta: { navGroup: 'home' }
}
```

```vue
<!-- MainLayout.vue -->
<div class="topbar-tools">
  <ProjectSwitcher class="project-switcher" />
  <GlobalSearch class="global-search" />
  <QuickCreateMenu class="quick-create" />
</div>
```

Implementation note: if introducing dedicated child components is too much for this round, keep these as inline sections inside `MainLayout.vue` first, then extract later.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Login and open the app shell
- Confirm global navigation and current-project navigation are visually distinct
- Confirm `系统管理` remains hidden for non-admin users

Expected: shell compiles and the new navigation model is clear.

**Step 5: Commit**

```bash
git add frontend/src/layouts/MainLayout.vue frontend/src/router/index.ts frontend/src/constants/layout.ts
git commit -m "feat(ui): refactor app shell for role and project navigation"
```

### Task 3: Replace generic dashboard with a role-based workbench homepage

**Files:**
- Modify: `frontend/src/views/dashboard/DashboardPage.vue`
- Modify: `frontend/src/constants/dashboard.ts`
- Modify: `frontend/src/stores/auth.ts` (only if a tiny helper is needed for role priority)

**Step 1: Write the failing structural expectation**

```bash
rg -n "today-focus|risk-panel|recent-projects|quick-actions|role-accent" frontend/src/views/dashboard/DashboardPage.vue frontend/src/constants/dashboard.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches, confirming the page is still a generic metric board.

**Step 3: Write minimal implementation**

```ts
const currentRole = computed(() => resolvePrimaryRole(authStore.user?.roles ?? []))

const homeSections = computed(() => {
  switch (currentRole.value) {
    case 'PM':
      return ['today-focus', 'project-risk', 'recent-projects', 'quick-actions']
    case 'DEV':
      return ['my-work', 'blockers', 'recent-context', 'quick-actions']
    default:
      return ['system-overview', 'risk-panel', 'recent-projects', 'quick-actions']
  }
})
```

```vue
<section class="status-strip role-home-status-strip">...</section>
<section class="panel-grid role-home-grid">...</section>
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Log in as at least two different roles if possible
- Confirm homepage card order and primary CTA differ by role
- Confirm old raw KPI-only layout is gone

Expected: page builds and clearly behaves like a role workbench.

**Step 5: Commit**

```bash
git add frontend/src/views/dashboard/DashboardPage.vue frontend/src/constants/dashboard.ts frontend/src/stores/auth.ts
git commit -m "feat(ui): redesign dashboard into role-based workbench"
```

### Task 4: Turn the project list into a project center

**Files:**
- Modify: `frontend/src/views/project/ProjectListPage.vue`
- Modify: `frontend/src/constants/projectList.ts`
- Modify: `frontend/src/stores/project.ts` (only if recent-project persistence is needed)

**Step 1: Write the failing structural expectation**

```bash
rg -n "recent-projects|risk-projects|project-center|project-card|favorite-project" frontend/src/views/project/ProjectListPage.vue frontend/src/constants/projectList.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches, confirming the page is still table-first.

**Step 3: Write minimal implementation**

```vue
<section class="status-strip project-center-strip">...</section>
<section class="panel-grid project-center-grid">
  <RecentProjectsPanel />
  <MyProjectsPanel />
  <RiskProjectsPanel />
</section>
<el-card class="surface-card">existing filters + table remain as lower-priority content</el-card>
```

Implementation note: first iteration can keep the existing table and dialog, but move them below the new project-center summary and card area.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm the page now opens with high-level project context before the table
- Confirm create-project flow still works
- Confirm entering a project still routes to `/projects/:id/overview`

Expected: project page becomes a workbench entry point without breaking existing operations.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectListPage.vue frontend/src/constants/projectList.ts frontend/src/stores/project.ts
git commit -m "feat(ui): redesign project list into project center"
```

### Task 5: Upgrade project overview into a project cockpit

**Files:**
- Modify: `frontend/src/views/project/ProjectOverviewPage.vue`
- Modify: `frontend/src/constants/projectOverview.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "health-badge|progress-rhythm|risk-summary|team-activity|project-cockpit" frontend/src/views/project/ProjectOverviewPage.vue frontend/src/constants/projectOverview.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new cockpit sections.

**Step 3: Write minimal implementation**

```vue
<section class="project-context-bar">...</section>
<section class="status-strip project-overview-strip">...</section>
<section class="panel-grid project-overview-grid">
  <ProgressRhythmPanel />
  <RiskSummaryPanel />
  <TeamActivityPanel />
</section>
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm the page now starts with project identity and health, not only raw cards/charts
- Confirm desktop layouts remain stable at 1366 / 1440 / 1920

Expected: overview feels like a cockpit, not a generic stats page.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectOverviewPage.vue frontend/src/constants/projectOverview.ts
git commit -m "feat(ui): redesign project overview into cockpit layout"
```

### Task 6: Redesign the requirement page as a panelized work page

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/constants/requirement.ts`
- Modify: `frontend/src/styles/base.css` (only for shared list utilities)

**Step 1: Write the failing structural expectation**

```bash
rg -n "status-strip|view-switcher|quick-detail|panel-list|only-mine" frontend/src/views/requirement/RequirementPage.vue frontend/src/constants/requirement.ts frontend/src/styles/base.css
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches; current page remains mostly legacy dense layout.

**Step 3: Write minimal implementation**

```vue
<section class="status-strip requirement-strip">...</section>
<div class="command-bar requirement-command-bar">
  <RequirementFilters />
  <RequirementViewSwitcher />
</div>
<section class="panel-grid requirement-main-grid">
  <RequirementListPanel />
  <RequirementQuickDetail />
</section>
```

First-pass note: keep existing data loading and actions; focus on layout and interaction hierarchy before deeper component extraction.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm “只看我的 / 模块 / 状态 / 优先级” controls are clearer
- Confirm selected requirement can be reviewed without losing list context
- Confirm create/edit/detail routes still work

Expected: requirement page is visibly more scannable and action-oriented.

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementPage.vue frontend/src/constants/requirement.ts frontend/src/styles/base.css
git commit -m "feat(ui): redesign requirement page as panelized work view"
```

### Task 7: Redesign the bug page as a QA/DEV defect workbench

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`
- Modify: `frontend/src/constants/bug.ts`
- Modify: `frontend/src/styles/base.css` (only for shared list utilities)

**Step 1: Write the failing structural expectation**

```bash
rg -n "unresolved|awaiting-verification|severity-strip|bug-workbench|quick-detail" frontend/src/views/bug/BugPage.vue frontend/src/constants/bug.ts frontend/src/styles/base.css
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new defect-workbench structure.

**Step 3: Write minimal implementation**

```vue
<section class="status-strip bug-strip">...</section>
<div class="command-bar bug-command-bar">
  <BugQuickTabs />
  <BugFilters />
</div>
<section class="panel-grid bug-main-grid">
  <BugListPanel />
  <BugQuickDetail />
</section>
```

Implementation note: keep the existing module sidebar concept, but restyle it to match the new shell and reduce visual divergence from the rest of the app.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm “全部 / 未解决 / 待验证 / 高严重度” is easier to access
- Confirm title, severity, assignee, status remain easy to scan
- Confirm create/detail navigation still works

Expected: bug page feels like a real defect triage surface.

**Step 5: Commit**

```bash
git add frontend/src/views/bug/BugPage.vue frontend/src/constants/bug.ts frontend/src/styles/base.css
git commit -m "feat(ui): redesign bug page as defect workbench"
```

### Task 8: Refresh the login page to match the new product identity

**Files:**
- Modify: `frontend/src/views/login/LoginPage.vue`
- Modify: `frontend/src/constants/login.ts`
- Modify: `frontend/src/constants/brand.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "role-entry|welcome-panel|brand-story|role-preview" frontend/src/views/login/LoginPage.vue frontend/src/constants/login.ts frontend/src/constants/brand.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches; login is still a simpler split layout.

**Step 3: Write minimal implementation**

```vue
<section class="brand-story">...</section>
<section class="login-form-panel">...</section>
<div class="role-preview">PM / DEV / QA / GUEST 登录后关注点预览</div>
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm the page communicates product value and role expectations clearly
- Confirm keyboard focus, input, submit, and error states still work

Expected: login becomes a branded product entry page without losing clarity.

**Step 5: Commit**

```bash
git add frontend/src/views/login/LoginPage.vue frontend/src/constants/login.ts frontend/src/constants/brand.ts
git commit -m "feat(ui): refresh login page for role-based workbench"
```

### Task 9: Harmonize secondary pages with the new shared shell pattern

**Files:**
- Modify: `frontend/src/views/module/ModulePage.vue`
- Modify: `frontend/src/views/sprint/SprintPage.vue`
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/views/system/UserPage.vue`
- Modify: `frontend/src/views/task/TaskPage.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "status-strip|command-bar|panel-grid" frontend/src/views/module/ModulePage.vue frontend/src/views/sprint/SprintPage.vue frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/system/UserPage.vue frontend/src/views/task/TaskPage.vue
```

**Step 2: Verify RED**

Run the command above.
Expected: some or all pages still use older page-shell patterns.

**Step 3: Write minimal implementation**

```vue
<header class="page-header">...</header>
<section class="status-strip">...</section>
<div class="command-bar">...</div>
<section class="panel-grid">...</section>
```

Implementation note: do not over-redesign these pages in the first pass; just align them to the new shell and spacing rhythm.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm page headers, spacing, filters, and card rhythm feel consistent across secondary pages

Expected: secondary pages no longer feel like they belong to a different UI generation.

**Step 5: Commit**

```bash
git add frontend/src/views/module/ModulePage.vue frontend/src/views/sprint/SprintPage.vue frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/system/UserPage.vue frontend/src/views/task/TaskPage.vue
git commit -m "style(ui): harmonize secondary pages with redesigned shell"
```

### Task 10: Final verification and delivery checklist

**Files:**
- Modify: `docs/plans/2026-03-06-role-based-ui-redesign-design.md` (only if minor notes need updating)

**Step 1: Run global frontend type-check**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
Expected: PASS.

**Step 2: Run production build**

Run: `npm.cmd run build` (workdir: `frontend`)
Expected: PASS.

**Step 3: Run structural spot checks**

Run:
- `rg -n "role-home|project-center|status-strip|panel-grid|command-bar" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css"`
- `rg -n "project-switcher|global-search|quick-create" frontend/src/layouts frontend/src/router frontend/src/constants --glob "*.vue" --glob "*.ts" --glob "*.css"`

Expected: the new redesign primitives are present in the intended files.

**Step 4: Perform manual UX verification**

Manual checklist:
- Verify desktop layouts at `1366 / 1440 / 1920`
- Verify role homepage differences for available roles
- Verify login, project switch, requirement list, bug list, and overview flows
- Verify light theme primary experience and dark theme readability

Expected: the redesigned shell and first-batch pages are usable and visually coherent.

**Step 5: Validate git state**

Run: `git status --short --branch`
Expected: only intended redesign files changed.

**Step 6: Commit final verification notes (optional)**

```bash
git add docs/plans/2026-03-06-role-based-ui-redesign-design.md docs/plans/2026-03-06-role-based-ui-redesign-implementation-plan.md
git commit -m "docs: capture role-based ui redesign plan"
```

---

## Verification Checklist Before Completion

- Shared tokens and base shell utilities are in place.
- `MainLayout` clearly separates global navigation and project navigation.
- `DashboardPage` behaves like a role-based workbench.
- `ProjectListPage` behaves like a project center.
- `ProjectOverviewPage`, `RequirementPage`, and `BugPage` follow the new page templates.
- `LoginPage` matches the new product identity.
- Secondary pages no longer visually diverge from the redesigned shell.
- `vue-tsc` and production build both pass.
