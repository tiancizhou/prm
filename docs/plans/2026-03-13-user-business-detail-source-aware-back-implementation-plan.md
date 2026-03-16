# User Business Detail Source-Aware Back Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the user business-detail page return to the originating project members page when entered from that page, while preserving the existing organization-team return behavior.

**Architecture:** Keep the existing route and detail component unchanged, but add lightweight query metadata to the project-members entry path. Update the detail page back action to branch on `route.query.from` and `route.query.projectId`, falling back to the current organization-team route when no project-members source is present.

**Tech Stack:** Vue 3, TypeScript, Vue Router, `vue-tsc`.

---

### Task 1: Add source-aware navigation between project members and user business detail

**Files:**
- Modify: `frontend/src/views/project/ProjectMembersPage.vue`
- Modify: `frontend/src/views/organization/OrganizationUserBusinessPage.vue`

**Step 1: Write the failing checks**

Run: `powershell.exe -Command "$members = Get-Content -Raw 'frontend/src/views/project/ProjectMembersPage.vue'; if ($members -match 'from=project-members|query:\s*\{\s*from:\s*[\"\'']project-members[\"\'']') { exit 0 } else { Write-Error 'Project member source query missing'; exit 1 }"`

Expected: FAIL because the jump from project members does not yet carry a source marker.

Run: `powershell.exe -Command "$detail = Get-Content -Raw 'frontend/src/views/organization/OrganizationUserBusinessPage.vue'; if ($detail -match 'project-members' -and $detail -match '/projects/\$\{.*projectId.*\}/members') { exit 0 } else { Write-Error 'Source-aware back route missing'; exit 1 }"`

Expected: FAIL because the detail page back action always returns to `/organization/team`.

**Step 2: Implement the minimal source-aware routing change**

- In `frontend/src/views/project/ProjectMembersPage.vue`, change the existing `router.push` call so it carries query parameters: `from=project-members` and `projectId=<current project id>`
- In `frontend/src/views/organization/OrganizationUserBusinessPage.vue`, update `goBack()` to read `route.query.from` and `route.query.projectId`
- If the source matches project members and the project id is valid, navigate to `/projects/${projectId}/members`
- Otherwise keep the existing `/organization/team` fallback

**Step 3: Run the checks to verify they pass**

Run: `powershell.exe -Command "$members = Get-Content -Raw 'frontend/src/views/project/ProjectMembersPage.vue'; if ($members -match 'project-members') { exit 0 } else { Write-Error 'Project member source query missing'; exit 1 }"`

Expected: PASS.

Run: `powershell.exe -Command "$detail = Get-Content -Raw 'frontend/src/views/organization/OrganizationUserBusinessPage.vue'; if ($detail -match 'project-members' -and $detail -match '/projects/\$\{.*projectId.*\}/members') { exit 0 } else { Write-Error 'Source-aware back route missing'; exit 1 }"`

Expected: PASS.

**Step 4: Run type verification**

Run: `npm.cmd exec -- vue-tsc -b`

Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/views/project/ProjectMembersPage.vue frontend/src/views/organization/OrganizationUserBusinessPage.vue
git commit -m "feat: preserve source when opening user business detail"
```
