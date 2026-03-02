# UI QA Checklist

Updated: 2026-02-28

## 1) Automated Structure Audit

Audit rules:
- Exactly one `h1` per page
- Uses `app-page`
- Uses `page-header / page-title / page-actions`
- Contains at least one `surface-card`

| Page | h1 | app-page | header/title/actions | surface-card | Result |
|---|---|---|---|---|---|
| `frontend/src/views/bug/BugPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/dashboard/DashboardPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/login/LoginPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/project/ProjectListPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/project/ProjectMembersPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/project/ProjectOverviewPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/requirement/RequirementPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/sprint/SprintPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/system/UserPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `frontend/src/views/task/TaskPage.vue` | ✅ | ✅ | ✅ | ✅ | ✅ |

## 2) Global Style Compliance

- [x] No inline styles in views
  - `rg -n "style=" frontend/src/views --glob "*.vue"`
- [x] Spacing/radius tokenized (no `margin|padding|gap|border-radius: Npx`)
  - `rg -n "(margin|padding|gap|border-radius):\s*\d+px|border-radius:\s*\d+px\s+\d+px" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css"`
- [x] Type check passed
  - `npm.cmd exec -- vue-tsc -b`

## 3) Responsive Breakpoint Audit (1366 / 1440 / 1920)

Implemented breakpoints:
- [x] Global base rules: `frontend/src/styles/base.css`
- [x] App shell/layout rules: `frontend/src/layouts/MainLayout.vue`
- [x] Login page rules: `frontend/src/views/login/LoginPage.vue`
- [x] Project overview rules: `frontend/src/views/project/ProjectOverviewPage.vue`
- [x] Dashboard rules: `frontend/src/views/dashboard/DashboardPage.vue`

Manual checks at 1920px:
- [x] Page title and subtitle spacing are balanced
- [x] Sidebar/header/content paddings feel proportional
- [x] Dashboard 4-metric grid remains readable

Manual checks at 1440px:
- [x] Filter bars do not overflow or wrap awkwardly
- [x] Project overview KPI cards render as 2 columns
- [x] Login split layout keeps hero and form in balance

Manual checks at 1366px:
- [x] Header actions remain aligned and clickable
- [x] Project overview content stack remains readable
- [x] Dashboard insight cards switch to single column cleanly

## 4) Notes

- Full-page structural homogeneity is complete:
  - `app-page / page-header / page-title / page-actions / surface-card`

## 5) Dark Theme Audit

Implemented:
- [x] Theme token system with `data-theme="light|dark"`: `frontend/src/styles/tokens.css`
- [x] System dark-mode auto follow (when no user override): `frontend/src/main.ts`
- [x] Element Plus dark variable overrides: `frontend/src/styles/tokens.css`
- [x] Dark-friendly core surfaces and table header variables: `frontend/src/styles/base.css`
- [x] Layout dark adaptation: `frontend/src/layouts/MainLayout.vue`
- [x] Login dark adaptation: `frontend/src/views/login/LoginPage.vue`
- [x] Project overview dark adaptation (cards, chart colors, text contrast): `frontend/src/views/project/ProjectOverviewPage.vue`
- [x] Dashboard status card tone colors tokenized for light/dark: `frontend/src/views/dashboard/DashboardPage.vue`
- [x] Header theme switcher (light/dark/system): `frontend/src/layouts/MainLayout.vue`
- [x] No hardcoded colors outside tokens (`views/layouts/styles`):
  - `rg -nP "(:\s*#[0-9a-fA-F]{3,8}(?![0-9a-fA-F]))|rgba\(|linear-gradient\(|radial-gradient\(" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css" -g "!frontend/src/styles/tokens.css"`

Manual checks:
- [x] Verify dashboard/table readability in dark mode
- [x] Verify dialogs/forms/input contrast in dark mode
- [x] Verify hover/focus states are visible in dark mode
- [x] Verify chart line/axis readability in dark mode
