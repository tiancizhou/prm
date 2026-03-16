# Unified Clickable Primary Text Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Unify the visual treatment of clickable primary text across selected frontend pages without changing any existing click behavior.

**Architecture:** Add a small reusable global utility style to `frontend/src/styles/base.css`, then migrate the selected pages to use that shared class instead of page-specific hover/focus link rules or `el-link` defaults. Keep layout styles local, but move the interactive primary-text appearance into one global rule.

**Tech Stack:** Vue 3, TypeScript, Element Plus, CSS variables, `vue-tsc`.

---

### Task 1: Add shared clickable-primary-text utility styles

**Files:**
- Modify: `frontend/src/styles/base.css`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/styles/base.css'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text utility missing'; exit 1 }"`

Expected: FAIL because the global utility does not exist yet.

**Step 2: Implement the minimal shared utility**

- Add a reusable global class for clickable primary text
- Cover default, hover, and focus-visible states
- Ensure it works for both `button` and `el-link`-style elements

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/styles/base.css'; if ($content -match 'workspace-link-text') { exit 0 } else { Write-Error 'Shared clickable-text utility missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/styles/base.css
git commit -m "style: add shared clickable primary text utility"
```

### Task 2: Migrate the selected pages to the shared utility

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/views/organization/OrganizationTeamPage.vue`
- Modify: `frontend/src/views/project/ProjectListPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$files = 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/organization/OrganizationTeamPage.vue','frontend/src/views/project/ProjectListPage.vue'; $missing = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -notmatch 'workspace-link-text') { $missing += $file } }; if ($missing.Count -eq 0) { exit 0 } else { Write-Error ('Missing shared clickable-text class in: ' + ($missing -join ', ')); exit 1 }"`

Expected: FAIL because the pages still use mixed local styles or `el-link` defaults.

**Step 2: Implement the minimal migration**

- Replace per-page `--link` styling with the shared class usage
- Convert `el-link`-based primary text to the same visual treatment if needed
- Keep all click handlers unchanged
- Remove duplicate local hover/focus styling that is fully replaced by the shared utility

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$files = 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/organization/OrganizationTeamPage.vue','frontend/src/views/project/ProjectListPage.vue'; $missing = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -notmatch 'workspace-link-text') { $missing += $file } }; if ($missing.Count -eq 0) { exit 0 } else { Write-Error ('Missing shared clickable-text class in: ' + ($missing -join ', ')); exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/organization/OrganizationTeamPage.vue frontend/src/views/project/ProjectListPage.vue
git commit -m "refactor: unify clickable primary text styling"
```
