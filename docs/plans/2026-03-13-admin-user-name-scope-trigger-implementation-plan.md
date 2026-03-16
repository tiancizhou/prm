# Admin User Name Scope Trigger Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the primary user name text in the admin users table open the existing project-scope drawer while preserving the current view icon behavior.

**Architecture:** Reuse the existing `openScopeDrawer(row)` action instead of adding a second interaction path. Change only the name cell rendering and related styles so the primary name becomes a semantic button with hover/focus states, while keeping the existing drawer data flow untouched.

**Tech Stack:** Vue 3, TypeScript, Element Plus, `vue-tsc`.

---

### Task 1: Connect the name text to the existing scope drawer

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/admin/AdminUsersPage.vue'; if ($content -match 'user-identity__primary[^>]*@click=\"openScopeDrawer\(row\)\"') { exit 0 } else { Write-Error 'Clickable name trigger missing'; exit 1 }"`

Expected: FAIL because the primary name text is not yet wired to the scope drawer.

**Step 2: Implement the minimal interaction change**

- Convert the primary name element into a semantic button
- Bind it to `openScopeDrawer(row)`
- Add focused/hovered link-like styling consistent with the current admin UI
- Keep the existing view icon unchanged

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/admin/AdminUsersPage.vue'; if ($content -match 'user-identity__primary[^>]*@click=\"openScopeDrawer\(row\)\"') { exit 0 } else { Write-Error 'Clickable name trigger missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue
git commit -m "feat: open user scope drawer from name cell"
```
