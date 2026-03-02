# Frontend UI QA Polish Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在不改业务逻辑的前提下，完成 `frontend/UI_QA_CHECKLIST.md` 中所有当前未勾选的响应式与暗色模式检查项。

**Architecture:** 以 token + 全局基础样式为主，页面局部样式为辅。优先在 `base.css` 与 `MainLayout` 收敛通用问题，再在 `Dashboard` / `ProjectOverview` / `Login` 做断点与暗色细修，最后回填 checklist。

**Tech Stack:** Vue 3 SFC、Element Plus、CSS Variables（tokens）、Vite、TypeScript (`vue-tsc`)

---

### Task 1: Set up QA baseline checks

**Files:**
- Modify: `frontend/UI_QA_CHECKLIST.md`

**Step 1: Run baseline checklist-gap check**

```bash
rg -n "^- \[ \]" frontend/UI_QA_CHECKLIST.md
```

**Step 2: Verify baseline is RED**

Run: `rg -n "^- \[ \]" frontend/UI_QA_CHECKLIST.md`
Expected: FAIL state for completion criteria（应命中多条未勾项）。

**Step 3: Record current constraints for this round**

```markdown
- 仅样式/布局改造
- 禁止改业务逻辑
- 验收以 checklist 未勾项清零为准
```

**Step 4: Keep baseline evidence in terminal log**

Run: same command above.
Expected: 结果保留，供收尾对比。

**Step 5: Commit**

```bash
git add frontend/UI_QA_CHECKLIST.md
git commit -m "chore: capture frontend QA polish baseline"
```

### Task 2: Polish responsive shell and action areas (1920/1440/1366)

**Files:**
- Modify: `frontend/src/styles/base.css`
- Modify: `frontend/src/layouts/MainLayout.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "@media \(max-width: 1919px\)|@media \(max-width: 1439px\)|@media \(max-width: 1365px\)" frontend/src/styles/base.css frontend/src/layouts/MainLayout.vue
```

**Step 2: Verify RED for target behavior**

Run: manual viewport checks at 1920/1440/1366.
Expected: 至少存在 header 操作区/筛选条/壳层比例不理想项。

**Step 3: Write minimal implementation**

```css
/* base.css */
.page-actions { flex-wrap: wrap; justify-content: flex-end; }
.page-actions > * { min-height: 36px; }

@media (max-width: 1365px) {
  .filter-bar { grid-template-columns: 1fr; }
}

/* MainLayout.vue */
@media (max-width: 1365px) {
  .header-right { flex-wrap: wrap; justify-content: flex-end; }
}
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- 手工核验 1920/1440/1366 的标题间距、壳层比例、header actions

Expected: 类型检查通过，相关断点项可勾选。

**Step 5: Commit**

```bash
git add frontend/src/styles/base.css frontend/src/layouts/MainLayout.vue
git commit -m "style: polish responsive shell and action alignment"
```

### Task 3: Polish dashboard / overview / login responsive details

**Files:**
- Modify: `frontend/src/views/dashboard/DashboardPage.vue`
- Modify: `frontend/src/views/project/ProjectOverviewPage.vue`
- Modify: `frontend/src/views/login/LoginPage.vue`

**Step 1: Write failing viewport checks**

```markdown
- 1920: dashboard 4-metric 可读性
- 1440: overview KPI 两列稳定
- 1440: login hero/form 比例平衡
- 1366: dashboard insight 单列稳定
- 1366: overview 内容堆叠可读
```

**Step 2: Verify RED**

Run: 逐项手工检查。
Expected: 至少一项不满足。

**Step 3: Write minimal implementation**

```css
/* DashboardPage.vue */
.metrics-grid { grid-template-columns: repeat(4, minmax(220px, 1fr)); }

/* ProjectOverviewPage.vue */
@media (max-width: 1365px) {
  .meta-item { width: 100%; padding: 0; }
}

/* LoginPage.vue */
@media (max-width: 1439px) {
  .login-page { grid-template-columns: 1.1fr minmax(360px, 0.9fr); }
}
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- 手工核验本任务 5 项

Expected: 全部满足。

**Step 5: Commit**

```bash
git add frontend/src/views/dashboard/DashboardPage.vue frontend/src/views/project/ProjectOverviewPage.vue frontend/src/views/login/LoginPage.vue
git commit -m "style: polish responsive details for dashboard overview login"
```

### Task 4: Polish dark-mode readability and interaction visibility

**Files:**
- Modify: `frontend/src/styles/tokens.css`
- Modify: `frontend/src/styles/base.css`
- Modify: `frontend/src/views/project/ProjectOverviewPage.vue`

**Step 1: Write failing dark-mode checks**

```markdown
- Dashboard/table readability
- Dialogs/forms/input contrast
- Hover/focus visibility
- Chart line/axis readability
```

**Step 2: Verify RED**

Run: 手动切换暗黑主题逐项检查。
Expected: 至少一项对比或可见性不足。

**Step 3: Write minimal implementation**

```css
/* tokens.css */
:root[data-theme='dark'] {
  --app-focus-ring: 0 0 0 3px rgba(106, 155, 255, 0.35);
}

/* base.css */
:root[data-theme='dark'] .el-input__wrapper:focus-within { box-shadow: var(--app-focus-ring); }
:root[data-theme='dark'] .el-dialog,
:root[data-theme='dark'] .el-drawer { border: 1px solid var(--app-border-soft); }

/* ProjectOverviewPage.vue */
.trend-svg { color: var(--app-chart-axis); }
```

**Step 4: Verify GREEN**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `rg -nP "(:\s*#[0-9a-fA-F]{3,8}(?![0-9a-fA-F]))|rgba\(|linear-gradient\(|radial-gradient\(" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css" -g "!frontend/src/styles/tokens.css"`
- 手工暗黑模式核验 4 项

Expected: 类型通过、颜色规则符合、暗黑可读性项满足。

**Step 5: Commit**

```bash
git add frontend/src/styles/tokens.css frontend/src/styles/base.css frontend/src/views/project/ProjectOverviewPage.vue
git commit -m "style: improve dark theme readability and focus visibility"
```

### Task 5: Close checklist and final verification

**Files:**
- Modify: `frontend/UI_QA_CHECKLIST.md`

**Step 1: Mark completed manual checks**

```markdown
- 将当前所有 `- [ ]` 改为 `- [x]`
```

**Step 2: Run completion check**

Run: `rg -n "^- \[ \]" frontend/UI_QA_CHECKLIST.md`
Expected: no matches.

**Step 3: Run type check and style compliance checks**

Run:
- `npm.cmd exec -- vue-tsc -b` (workdir: `frontend`)
- `rg -n "style=" frontend/src/views --glob "*.vue"`
- `rg -n "(margin|padding|gap|border-radius):\s*\d+px|border-radius:\s*\d+px\s+\d+px" frontend/src/layouts frontend/src/views frontend/src/styles --glob "*.vue" --glob "*.css"`

**Step 4: Verify final GREEN**

Expected: checklist 未勾项为 0；类型检查通过；无新增违规样式。

**Step 5: Commit**

```bash
git add frontend/UI_QA_CHECKLIST.md
git commit -m "docs: close frontend ui qa checklist"
```
