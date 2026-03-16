# Second-Batch Clickable Entry Unification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Unify the remaining high-frequency clickable primary-text entries in `BugPage` and `RequirementPage` to the shared `workspace-link-text` visual rule without changing behavior.

**Architecture:** Reuse the existing global clickable-text utility from `frontend/src/styles/base.css` and migrate only the title and module-name entry points in the two target pages. Preserve all current click handlers and active-state logic, while removing page-local hover/underline rules that duplicate the global utility.

**Tech Stack:** Vue 3, TypeScript, CSS variables, `vue-tsc`.

---

### Task 1: Migrate `BugPage` clickable entries to the shared rule

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/bug/BugPage.vue'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text class missing in BugPage'; exit 1 }"`

Expected: FAIL because `BugPage` still uses local `title-link` / `module-name` styling.

**Step 2: Implement the minimal migration**

- Convert the clickable Bug title to use `workspace-link-text workspace-link-text--primary`
- Convert the clickable module-name text to use the same shared class
- Keep the existing click handlers unchanged
- Remove duplicate local `title-link` and module hover styling if fully replaced by the global rule

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/bug/BugPage.vue'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text class missing in BugPage'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/bug/BugPage.vue
git commit -m "refactor: unify bug page clickable entry styling"
```

### Task 2: Migrate `RequirementPage` clickable entries to the shared rule

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/requirement/RequirementPage.vue'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text class missing in RequirementPage'; exit 1 }"`

Expected: FAIL because `RequirementPage` still uses local `title-link` / `module-name` styling.

**Step 2: Implement the minimal migration**

- Convert the clickable requirement title to use `workspace-link-text workspace-link-text--primary`
- Convert the clickable module-name text to use the same shared class
- Keep all current click handlers unchanged
- Remove duplicate local `title-link` and module hover styling if fully replaced by the global rule

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/requirement/RequirementPage.vue'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text class missing in RequirementPage'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: unify requirement page clickable entry styling"
```

### Task 3: Final regression verification

**Files:**
- Verify: `frontend/src/views/bug/BugPage.vue`
- Verify: `frontend/src/views/requirement/RequirementPage.vue`
- Verify: `frontend/src/styles/base.css`

**Step 1: Run regression search**

Run: `powershell.exe -Command "$files = 'frontend/src/views/bug/BugPage.vue','frontend/src/views/requirement/RequirementPage.vue'; foreach ($file in $files) { Write-Output ('--- ' + $file); Select-String -Path $file -Pattern 'workspace-link-text|title-link|module-name' }"`

Expected: shared class usage is present, and any remaining `title-link` / `module-name` references are only those still intentionally used for layout/state hooks.

**Step 2: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 3: Run build if environment allows**

Run: `npm.cmd run build`

Expected: PASS, or the existing known sandbox `spawn EPERM` limitation if unchanged.

**Step 4: Commit**

```bash
git add frontend/src/views/bug/BugPage.vue frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: finish second clickable entry unification pass"
```
