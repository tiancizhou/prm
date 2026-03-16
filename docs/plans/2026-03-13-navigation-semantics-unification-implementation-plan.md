# Navigation Semantics Unification Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace button-based page-navigation primary text with navigation-semantic links in the selected frontend pages while keeping the shared clickable-text visual style intact.

**Architecture:** Keep the shared `workspace-link-text` styling as the visual layer, but change each page-navigation entry from button-style navigation to a navigation-semantic implementation. Reuse existing route targets and handlers where possible, and keep non-navigation click targets such as filters or module selectors out of this pass.

**Tech Stack:** Vue 3, TypeScript, Vue Router, Element Plus, `vue-tsc`.

---

### Task 1: Convert user and project name navigation entries

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/views/organization/OrganizationTeamPage.vue`
- Modify: `frontend/src/views/project/ProjectListPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$files = 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/organization/OrganizationTeamPage.vue','frontend/src/views/project/ProjectListPage.vue'; $found = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -match 'type=\"button\" class=\"workspace-link-text') { $found += $file } }; if ($found.Count -eq 0) { exit 0 } else { Write-Error ('Button-based navigation still present in: ' + ($found -join ', ')); exit 1 }"`

Expected: FAIL because these pages still use button semantics for text-based navigation.

**Step 2: Implement the minimal semantics change**

- Replace the navigation buttons with router-semantic navigation entry points
- Keep `workspace-link-text workspace-link-text--primary`
- Preserve current destinations:
  - user scope project name -> `/projects/:id`
  - project member name -> `/organization/team/:userId?...`
  - organization team name -> `/organization/team/:userId`
  - project list name -> `/projects/:id/overview`

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$files = 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/organization/OrganizationTeamPage.vue','frontend/src/views/project/ProjectListPage.vue'; $found = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -match 'type=\"button\" class=\"workspace-link-text') { $found += $file } }; if ($found.Count -eq 0) { exit 0 } else { Write-Error ('Button-based navigation still present in: ' + ($found -join ', ')); exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/organization/OrganizationTeamPage.vue frontend/src/views/project/ProjectListPage.vue
git commit -m "refactor: use navigation semantics for primary text links"
```

### Task 2: Convert Bug and Requirement title navigation entries

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`
- Modify: `frontend/src/views/requirement/RequirementPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$files = 'frontend/src/views/bug/BugPage.vue','frontend/src/views/requirement/RequirementPage.vue'; $found = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -match 'type=\"button\" class=\".*workspace-link-text.*router.push') { $found += $file } }; if ($found.Count -eq 0) { exit 0 } else { Write-Error ('Button-based detail navigation still present in: ' + ($found -join ', ')); exit 1 }"`

Expected: FAIL because Bug and Requirement title entries still navigate via button semantics.

**Step 2: Implement the minimal semantics change**

- Convert Bug title navigation to a link-semantic implementation
- Convert Requirement title navigation to a link-semantic implementation
- Keep current route targets unchanged
- Keep `workspace-link-text workspace-link-text--primary`

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$files = 'frontend/src/views/bug/BugPage.vue','frontend/src/views/requirement/RequirementPage.vue'; $found = @(); foreach ($file in $files) { $content = Get-Content -Raw $file; if ($content -match 'type=\"button\" class=\".*workspace-link-text.*router.push') { $found += $file } }; if ($found.Count -eq 0) { exit 0 } else { Write-Error ('Button-based detail navigation still present in: ' + ($found -join ', ')); exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/bug/BugPage.vue frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: use navigation semantics for bug and requirement titles"
```

### Task 3: Final verification

**Files:**
- Verify: `frontend/src/views/admin/AdminUsersPage.vue`
- Verify: `frontend/src/views/project/ProjectMembersPage.vue`
- Verify: `frontend/src/views/organization/OrganizationTeamPage.vue`
- Verify: `frontend/src/views/project/ProjectListPage.vue`
- Verify: `frontend/src/views/bug/BugPage.vue`
- Verify: `frontend/src/views/requirement/RequirementPage.vue`

**Step 1: Run navigation-entry audit**

Run: `powershell.exe -Command "$files = 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/organization/OrganizationTeamPage.vue','frontend/src/views/project/ProjectListPage.vue','frontend/src/views/bug/BugPage.vue','frontend/src/views/requirement/RequirementPage.vue'; foreach ($file in $files) { Write-Output ('--- ' + $file); Select-String -Path $file -Pattern 'workspace-link-text|router.push\(`/projects|router.push\(`/organization|<router-link' }"`

Expected: all intended navigation text entries are present and still bound to the correct routes.

**Step 2: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 3: Run build if environment allows**

Run: `npm.cmd run build`

Expected: PASS, or the existing known sandbox `spawn EPERM` limitation if unchanged.

**Step 4: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/organization/OrganizationTeamPage.vue frontend/src/views/project/ProjectListPage.vue frontend/src/views/bug/BugPage.vue frontend/src/views/requirement/RequirementPage.vue
git commit -m "refactor: align navigation semantics for primary text entries"
```
