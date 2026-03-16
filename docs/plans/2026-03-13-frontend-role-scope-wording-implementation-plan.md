# Frontend Role Scope Wording Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align the frontend wording and information structure with the simplified permission model so that system roles define capability and project membership defines data scope.

**Architecture:** Update the role-related UI from the constants layer upward. First normalize shared copy and field labels, then refactor the project members page, admin users page, and admin permissions page so they consistently present `system role` and `project scope` without reintroducing a separate project-role concept. Because the frontend currently has no dedicated unit-test harness, the plan uses narrow pre/post change verification via targeted search checks plus `vue-tsc` instead of adding a new testing stack.

**Tech Stack:** Vue 3, TypeScript, Element Plus, `vue-tsc`, `rg`/PowerShell search.

---

### Task 1: Normalize shared wording constants

**Files:**
- Modify: `frontend/src/constants/projectMembers.ts`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Capture the current conflicting wording**

Run: `Select-String -Path 'frontend/src/constants/projectMembers.ts','frontend/src/constants/adminPeople.ts' -Pattern '项目角色|项目内角色|成员角色|role collaboration|Manager Roles|Developer Role'`

Expected: existing wording still contains mixed role/range language that needs to be removed.

**Step 2: Update the shared copy to the new model**

- In `frontend/src/constants/projectMembers.ts`, change the page subtitle, table labels, add-member helper text, and permission messages so the page clearly says it manages project participation and only shows system roles.
- In `frontend/src/constants/adminPeople.ts`, update users-page and permissions-page copy so the product rule is explicit: `系统角色决定权限，加入项目后才可查看该项目业务数据`.
- Remove or rename any filters/text that imply there are distinct project roles.

**Step 3: Verify the conflicting wording is gone**

Run: `Select-String -Path 'frontend/src/constants/projectMembers.ts','frontend/src/constants/adminPeople.ts' -Pattern '项目角色|项目内角色|成员角色|Manager Roles|Developer Role'`

Expected: no matches, or only intentional historical comments if any remain.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/constants/projectMembers.ts frontend/src/constants/adminPeople.ts
git commit -m "refactor: align role scope wording constants"
```

### Task 2: Refactor the project members page into a pure scope-management view

**Files:**
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/constants/projectMembers.ts`

**Step 1: Capture the current structure that still exposes the old model**

Run: `Select-String -Path 'frontend/src/views/project/ProjectMembersPage.vue' -Pattern 'employeeNo|username|systemRole|addMember|noPermission'`

Expected: current page still has old column composition and lacks the new scope rule banner.

**Step 2: Implement the minimal page structure change**

- Add a visible rule banner or helper block under the page header.
- Replace the current table composition with: name, department, position, system role, joined time, actions.
- Remove duplicated identity fields that no longer help explain project scope.
- Keep the role tag, but ensure it is explicitly labeled and understood as a system-role tag.
- Ensure the add-member dialog only expresses `join current project`, not `assign project role`.

**Step 3: Verify the page structure matches the new model**

Run: `Select-String -Path 'frontend/src/views/project/ProjectMembersPage.vue' -Pattern 'employeeNo|username|项目角色|project role'`

Expected: no matches for removed project-role semantics; only retained fields that are still intentionally used.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectMembersPage.vue frontend/src/constants/projectMembers.ts
git commit -m "refactor: simplify project members scope view"
```

### Task 3: Reshape the admin users page around system role plus project scope

**Files:**
- Modify: `frontend/src/views/admin/AdminUsersPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Capture the current field layout**

Run: `Select-String -Path 'frontend/src/views/admin/AdminUsersPage.vue' -Pattern 'id|username|department|position|lastLogin|visitCount|roles'`

Expected: current page still emphasizes legacy maintenance fields and does not clearly expose project scope.

**Step 2: Implement the minimal layout and wording update**

- Change the list emphasis to: name, department, position, system role, joined-project count, status, actions.
- Remove or demote fields that are not part of the simplified model if they interfere with scan efficiency.
- Add a small helper text block near the table or page header: `系统角色决定权限，加入项目后才可查看该项目业务数据`.
- If the page already has expandable details or a dialog summary area, add a `项目范围` section instead of introducing a new role dimension.

**Step 3: Verify the new page semantics**

Run: `Select-String -Path 'frontend/src/views/admin/AdminUsersPage.vue','frontend/src/constants/adminPeople.ts' -Pattern '项目角色|项目内角色|成员角色|visitCount|Manager Roles|Developer Role'`

Expected: removed wording is gone, and only intended fields remain.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminUsersPage.vue frontend/src/constants/adminPeople.ts
git commit -m "refactor: align admin users with role and scope model"
```

### Task 4: Tighten the permissions page so it only describes system-role permissions

**Files:**
- Modify: `frontend/src/views/admin/AdminPermissionsPage.vue`
- Modify: `frontend/src/constants/adminPeople.ts`

**Step 1: Capture the current permissions-page wording**

Run: `Select-String -Path 'frontend/src/views/admin/AdminPermissionsPage.vue','frontend/src/constants/adminPeople.ts' -Pattern 'members|permissions|角色|project role|项目角色'`

Expected: current page copy is mostly correct structurally, but still lacks the explicit rule that project membership controls data scope rather than permission capability.

**Step 2: Implement the minimal clarification layer**

- Add a top-level helper text or information banner explaining the split between `系统角色权限` and `项目数据范围`.
- Update any role detail, member list, tooltip, or empty-state wording that could imply a second role system exists.
- Keep the current permission-assignment workflow intact; only change wording and explanatory framing.

**Step 3: Verify the page no longer implies dual role systems**

Run: `Select-String -Path 'frontend/src/views/admin/AdminPermissionsPage.vue','frontend/src/constants/adminPeople.ts' -Pattern '项目角色|项目内角色|成员角色'`

Expected: no matches.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/admin/AdminPermissionsPage.vue frontend/src/constants/adminPeople.ts
git commit -m "docs: clarify permissions page role scope model"
```

### Task 5: Final regression verification and cleanup

**Files:**
- Verify: `frontend/src/views/project/ProjectMembersPage.vue`
- Verify: `frontend/src/views/admin/AdminUsersPage.vue`
- Verify: `frontend/src/views/admin/AdminPermissionsPage.vue`
- Verify: `frontend/src/constants/projectMembers.ts`
- Verify: `frontend/src/constants/adminPeople.ts`

**Step 1: Run wording regression search**

Run: `Select-String -Path 'frontend/src/views/**/*.vue','frontend/src/constants/**/*.ts' -Pattern '项目角色|项目内角色|成员角色|project role'`

Expected: no remaining UI wording that reintroduces a second role system.

**Step 2: Run focused structural spot checks**

Run: `Select-String -Path 'frontend/src/views/project/ProjectMembersPage.vue','frontend/src/views/admin/AdminUsersPage.vue','frontend/src/views/admin/AdminPermissionsPage.vue' -Pattern '系统角色|项目范围|加入项目|业务数据'`

Expected: the new rule language appears in the intended pages.

**Step 3: Run full type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 4: Run production build if the environment allows**

Run: `npm.cmd run build`

Expected: PASS. If the sandbox still blocks the build process, record the exact environment error rather than changing unrelated code.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/admin/AdminUsersPage.vue frontend/src/views/admin/AdminPermissionsPage.vue frontend/src/constants/projectMembers.ts frontend/src/constants/adminPeople.ts
git commit -m "refactor: unify frontend role and scope language"
```
