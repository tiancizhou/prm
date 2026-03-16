# Filter Text Semantics Unification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Unify the module-name entry points in `BugPage` and `RequirementPage` as filter-semantic text buttons while keeping their current filtering behavior unchanged.

**Architecture:** Add a small shared filter-text utility to the global stylesheet, then migrate the module-name entry points in the two target pages to use that utility. Preserve existing expand-arrow behavior and page-local active-state classes, and keep navigation-style clickable text out of this pass.

**Tech Stack:** Vue 3, TypeScript, CSS variables, `vue-tsc`.

---

### Task 1: Add shared filter-text utility styles

**Files:**
- Modify: `frontend/src/styles/base.css`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/styles/base.css'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'Shared filter-text utility missing'; exit 1 }"`

Expected: FAIL because the filter-text utility does not exist yet.

**Step 2: Implement the minimal shared utility**

- Add a global class for text-button-style filter entries
- Cover default, hover, and focus-visible states
- Keep the visual tone aligned with the current design system, but distinct from navigation-style text

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/styles/base.css'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'Shared filter-text utility missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/styles/base.css
git commit -m "style: add shared filter text utility"
```

### Task 2: Migrate `BugPage` module-name entries to filter semantics

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/bug/BugPage.vue'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'BugPage filter-text class missing'; exit 1 }"`

Expected: FAIL because BugPage module names still use the navigation-style shared class.

**Step 2: Implement the minimal migration**

- Change module-name entries from `workspace-link-text` to the new filter-text utility
- Keep the current `selectModule(...)` behavior unchanged
- Keep expand arrow behavior unchanged
- Remove any leftover local hover rules that duplicate the shared filter utility

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/bug/BugPage.vue'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'BugPage filter-text class missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/bug/BugPage.vue
git commit -m "refactor: unify bug page module filter text"
```

### Task 3: Migrate `RequirementPage` module-name entries to filter semantics

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/requirement/RequirementPage.vue'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'RequirementPage filter-text class missing'; exit 1 }"`

Expected: FAIL because RequirementPage module names still use the navigation-style shared class.

**Step 2: Implement the minimal migration**

- Change module-name entries from `workspace-link-text` to the new filter-text utility
- Keep the current `selectSidebarModule(...)` behavior unchanged
- Keep expand arrow behavior unchanged
- Remove any leftover local hover rules that duplicate the shared filter utility

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/requirement/RequirementPage.vue'; if ($content -match 'workspace-filter-text') { exit 0 } else { Write-Error 'RequirementPage filter-text class missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: unify requirement page module filter text"
```

### Task 4: Final verification

**Files:**
- Verify: `frontend/src/styles/base.css`
- Verify: `frontend/src/views/bug/BugPage.vue`
- Verify: `frontend/src/views/requirement/RequirementPage.vue`

**Step 1: Run regression search**

Run: `powershell.exe -Command "Select-String -Path 'frontend/src/views/bug/BugPage.vue','frontend/src/views/requirement/RequirementPage.vue' -Pattern 'workspace-filter-text|module-name'"`

Expected: module-name entries now use the shared filter-text class.

**Step 2: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 3: Run build if environment allows**

Run: `npm.cmd run build`

Expected: PASS, or the existing known sandbox `spawn EPERM` limitation if unchanged.

**Step 4: Commit**

```bash
git add frontend/src/styles/base.css frontend/src/views/bug/BugPage.vue frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: align filter text semantics in project work pages"
```
