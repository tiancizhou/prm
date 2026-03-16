# Project Member Name Business Detail Trigger Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the primary member name in the project members page navigate to the existing organization user-business detail page.

**Architecture:** Reuse the existing organization-domain user business-detail route instead of creating a new page or alias route. Update only the project members table cell so the primary name becomes a semantic click target, and keep all existing member-management logic unchanged.

**Tech Stack:** Vue 3, TypeScript, Vue Router, Element Plus, `vue-tsc`.

---

### Task 1: Wire project member names to the existing business-detail route

**Files:**
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Reference: `frontend/src/views/organization/OrganizationTeamPage.vue`
- Verify: `frontend/src/router/index.ts`

**Step 1: Write the failing check**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/project/ProjectMembersPage.vue'; if ($content -match 'openUserBusiness|router.push\(`/organization/team/\$\{row\.(userId|id)\}\`\)|@click=\"openUserBusiness\(row\)\"') { exit 0 } else { Write-Error 'Member name business-detail trigger missing'; exit 1 }"`

Expected: FAIL because the project member name is still static text.

**Step 2: Implement the minimal route-trigger change**

- Import `useRouter()` in `frontend/src/views/project/ProjectMembersPage.vue`
- Add an `openUserBusiness(row)` helper that navigates to `/organization/team/${row.userId}`
- Convert the primary member name into a clickable link/button bound to `openUserBusiness(row)`
- Keep username/employee number as static secondary text
- Keep remove-member action and joined-time column unchanged

**Step 3: Run the check to verify it passes**

Run: `powershell.exe -Command "$content = Get-Content -Raw 'frontend/src/views/project/ProjectMembersPage.vue'; if ($content -match 'openUserBusiness|router.push\(`/organization/team/\$\{row\.(userId|id)\}\`\)|@click=\"openUserBusiness\(row\)\"') { exit 0 } else { Write-Error 'Member name business-detail trigger missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectMembersPage.vue
git commit -m "feat: open user business detail from project member name"
```
