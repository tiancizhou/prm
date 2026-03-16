# PRM Triple-Level Layout Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Refactor the current PRM frontend shell into a Jira-like triple-level layout with a global navigation rail, project workspace sidebar, compact header, and a paper-like project overview page without changing backend contracts.

**Architecture:** Keep the existing Vue 3 + Element Plus app shell and route paths, then layer the redesign in three steps: first add route metadata and shared layout tokens, then rebuild `MainLayout` around the new navigation model, and finally update `ProjectOverviewPage` to use the new 7:3 workbench pattern. Project switching should be driven by route semantics rather than hard-coded page conditionals.

**Tech Stack:** Vue 3 SFC, TypeScript, Vue Router, Element Plus, Pinia, CSS Variables (`tokens.css` / `base.css`), Vite, `vue-tsc`.

---

## Implementation Constraints

- Keep existing backend APIs and route paths intact.
- Prefer adding route `meta` over renaming routes.
- Do not introduce a new charting dependency in the first pass.
- Use structural checks, `vue-tsc`, and production build as the main verification stack.
- If the execution session disallows git commits, treat commit steps as checkpoints rather than mandatory actions.

---

### Task 1: Add route metadata for the triple-level shell

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "navLevel1|projectSection|projectSwitchMode|pageTitle|pagePathLabel" frontend/src/router/index.ts frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new shell metadata fields.

**Step 3: Write minimal implementation**

```ts
// frontend/src/router/index.ts
{
  path: 'projects/:id/bugs',
  name: 'Bugs',
  component: () => import('@/views/bug/BugPage.vue'),
  meta: {
    navLevel1: 'projects',
    projectSection: 'bugs',
    projectSwitchMode: 'preserve-section',
    pageTitle: 'Bug',
    pagePathLabel: '项目集'
  }
}
```

Implementation note: add equivalent metadata to all shell-relevant routes, including fallback behavior for detail/edit pages.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Expected: type-check PASS.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/router/index.ts frontend/src/constants/layout.ts
git commit -m "feat(ui): add route metadata for triple-level layout"
```

### Task 2: Split the app shell into Level 1 rail and Level 2 project sidebar

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "global-rail|project-sidebar|project-switcher|shell-header-meta|shell-header-tools" frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new shell sections.

**Step 3: Write minimal implementation**

```vue
<aside class="global-rail">...</aside>
<aside v-if="showProjectSidebar" class="project-sidebar">
  <section class="project-switcher">...</section>
</aside>
<header class="shell-header">
  <div class="shell-header-meta">...</div>
  <div class="shell-header-tools">...</div>
</header>
```

Implementation note: remove project navigation items from the global menu and render them only in the Level 2 sidebar.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Login and confirm `/dashboard` shows only the global rail
- Open `/projects/:id/overview` and confirm the project sidebar appears
- Confirm non-admin accounts do not see the system entry

Expected: the two-level left navigation is visually and semantically separated.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts
git commit -m "feat(ui): split app shell into global and project navigation"
```

### Task 3: Implement project switching that preserves the current section

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/router/index.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "preserve-section|fallback-list|resolveProjectSwitchTarget|handleProjectSwitch" frontend/src/layouts/MainLayout.vue frontend/src/router/index.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the switching helper behavior.

**Step 3: Write minimal implementation**

```ts
function resolveProjectSwitchTarget(targetProjectId: number | string) {
  if (route.meta.projectSwitchMode === 'preserve-section' && route.meta.projectSection) {
    return `/projects/${targetProjectId}/${route.meta.projectSection}`
  }
  return `/projects/${targetProjectId}/overview`
}
```

Implementation note: detail, create, and edit routes should explicitly resolve to their parent list route when switching projects.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)

Manual:
- From `/projects/12/bugs`, switch to another project and confirm landing on `/projects/<new>/bugs`
- From a requirement detail page, switch and confirm landing on the target project’s requirement list

Expected: project switching preserves the functional context when safe and falls back cleanly otherwise.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/layouts/MainLayout.vue frontend/src/router/index.ts
git commit -m "feat(ui): preserve section when switching projects"
```

### Task 4: Compress the header and replace the full breadcrumb row

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/constants/layout.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "pageTitle|pagePathLabel|global-search|quick-create|notification-trigger" frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: the compact header affordances do not all exist yet.

**Step 3: Write minimal implementation**

```vue
<div class="shell-header-meta">
  <span class="page-path-label">{{ pagePathLabel }}</span>
  <h1 class="page-title">{{ pageTitle }}</h1>
</div>
<div class="shell-header-tools">
  <div class="global-search">...</div>
  <button class="quick-create">...</button>
  <button class="notification-trigger">...</button>
</div>
```

Implementation note: keep the search and quick-create as lightweight first-pass controls if backend search is not ready.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm the header height is visually reduced versus the current shell
- Confirm page titles are more prominent than path labels

Expected: the header reads like a workbench toolbar rather than a traditional breadcrumb row.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/layouts/MainLayout.vue frontend/src/constants/layout.ts
git commit -m "style(ui): compress header and elevate page titles"
```

### Task 5: Add paper-like layout tokens and shared surface styles

**Files:**
- Modify: `frontend/src/styles/tokens.css`
- Modify: `frontend/src/styles/base.css`

**Step 1: Write the failing structural expectation**

```bash
rg -n "paper-surface|overview-grid|metric-card|activity-panel|subtle-shadow" frontend/src/styles/tokens.css frontend/src/styles/base.css
```

**Step 2: Verify RED**

Run the command above.
Expected: the new workbench surface primitives are absent.

**Step 3: Write minimal implementation**

```css
:root {
  --app-page-bg: #f5f7fa;
  --app-paper-border: #e6eaf0;
  --app-subtle-shadow: 0 1px 2px rgba(16, 24, 40, 0.04), 0 8px 24px rgba(16, 24, 40, 0.06);
}

.paper-surface { background: #fff; border: 1px solid var(--app-paper-border); box-shadow: var(--app-subtle-shadow); }
.overview-grid { display: grid; grid-template-columns: minmax(0, 7fr) minmax(280px, 3fr); gap: var(--space-lg); }
.metric-card { display: grid; gap: var(--space-sm); }
.activity-panel { display: grid; gap: var(--space-md); }
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Expected: shared styling primitives compile and build cleanly.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/styles/tokens.css frontend/src/styles/base.css
git commit -m "style(ui): add paper-like workbench surface tokens"
```

### Task 6: Rebuild the project overview page around a 7:3 workbench layout

**Files:**
- Modify: `frontend/src/views/project/ProjectOverviewPage.vue`
- Modify: `frontend/src/constants/projectOverview.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "overview-grid|metric-card|trend-chip|activity-timeline|risk-panel|my-work-panel" frontend/src/views/project/ProjectOverviewPage.vue frontend/src/constants/projectOverview.ts
```

**Step 2: Verify RED**

Run the command above.
Expected: no matches for the new workbench sections.

**Step 3: Write minimal implementation**

```vue
<section class="overview-grid">
  <div class="overview-main-column">
    <section class="metric-card">...</section>
    <section class="risk-panel">...</section>
    <section class="my-work-panel">...</section>
  </div>
  <aside class="activity-panel">
    <section class="activity-timeline">...</section>
  </aside>
</section>
```

Implementation note: use trend chips, progress bars, and micro status affordances before adding heavyweight charts.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Manual:
- Confirm the first row contains upgraded metric cards
- Confirm the right column reads as dynamic collaboration rather than leftover content

Expected: the overview page feels like a project cockpit instead of a static summary page.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/views/project/ProjectOverviewPage.vue frontend/src/constants/projectOverview.ts
git commit -m "feat(ui): redesign project overview as a 7-3 workbench"
```

### Task 7: Align requirement and bug pages to the new shell semantics

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/views/bug/BugPage.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "page-title|paper-surface|status-strip|command-bar|panel-grid" frontend/src/views/requirement/RequirementPage.vue frontend/src/views/bug/BugPage.vue
```

**Step 2: Verify RED**

Run the command above.
Expected: some or all of the new shell-aligned classes are missing.

**Step 3: Write minimal implementation**

```vue
<header class="page-header">...</header>
<section class="status-strip paper-surface">...</section>
<div class="command-bar">...</div>
<section class="panel-grid">...</section>
```

Implementation note: do not over-redesign list mechanics in this pass; align hierarchy, shell rhythm, and page headers first.

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `npm.cmd run build` (workdir: `frontend`)

Expected: requirement and bug pages visually align with the new shell pattern.

**Step 5: Checkpoint**

Optional if commits are allowed:

```bash
git add frontend/src/views/requirement/RequirementPage.vue frontend/src/views/bug/BugPage.vue
git commit -m "style(ui): align requirement and bug pages with new shell"
```

### Task 8: Final verification

**Files:**
- Modify: `docs/plans/2026-03-11-triple-level-layout-design.md` (only if minor implementation notes need to be updated)

**Step 1: Run type-check**

Run: `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
Expected: PASS.

**Step 2: Run production build**

Run: `npm.cmd run build` (workdir: `frontend`)
Expected: PASS.

**Step 3: Run structural spot checks**

Run:
- `rg -n "global-rail|project-sidebar|project-switcher|shell-header-meta|overview-grid|metric-card|activity-panel" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css"`
- `rg -n "navLevel1|projectSection|projectSwitchMode|pageTitle|pagePathLabel" frontend/src/router frontend/src/constants --glob "*.ts"`

Expected: the new shell primitives and route semantics are present where intended.

**Step 4: Perform manual UX verification**

Manual checklist:
- Verify desktop layouts at `1366 / 1440 / 1920`
- Verify `/dashboard` vs `/projects/:id/*` shell differences
- Verify project switching from overview, bugs, and requirements
- Verify compact header readability and action density
- Verify overview page’s `7:3` rhythm and activity timeline hierarchy

Expected: the triple-level shell is coherent and practical for project work.

**Step 5: Check git state**

Run: `git status --short --branch`
Expected: only intended layout redesign files changed.

---

## Verification Checklist Before Completion

- Global rail and project sidebar are structurally separated.
- Project switching preserves the current functional section where safe.
- The header is shorter and emphasizes the page title over breadcrumb text.
- Shared tokens create a clear paper-like content hierarchy.
- `ProjectOverviewPage` uses a `7:3` workbench layout with activity on the right.
- Requirement and bug pages follow the same shell semantics.
- `vue-tsc` and production build both pass.
