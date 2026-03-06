# Sub-Requirement Assignee Default Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make sub-requirement creation default to the parent requirement's assignee across all entry paths while keeping the child assignee independently editable.

**Architecture:** Keep backend contracts unchanged and solve the behavior entirely in the frontend. Unify both creation entry paths around the same parent-assignee initialization rule: preload from parent requirement when `parentId` is present, then treat the selected child assignee as independent form state once the user edits it.

**Tech Stack:** Vue 3 SFC, TypeScript, Element Plus, Vue Router, existing `requirementApi`, existing i18n constants in `frontend/src/constants/requirement.ts`, `vue-tsc`.

---

## Implementation Constraints

- No backend API changes.
- No database or permission changes.
- Keep scope limited to sub-requirement assignee defaulting and explanatory copy.
- Do not redesign the whole requirement form.
- Follow @test-driven-development in spirit with structural RED checks, targeted manual RED/GREEN checks, and `vue-tsc` verification.

---

### Task 1: Add i18n copy for sub-requirement assignee hints

**Files:**
- Modify: `frontend/src/constants/requirement.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "subRequirementAssignee|parentAssigneeUnavailable|independentFromParent|defaultFromParent" frontend/src/constants/requirement.ts
```

**Step 2: Run the check to verify RED**

Run: `rg -n "subRequirementAssignee|parentAssigneeUnavailable|independentFromParent|defaultFromParent" frontend/src/constants/requirement.ts`
Expected: no matches.

**Step 3: Write minimal implementation**

Add the following keys in both `zh-CN` and `en-US` blocks under a suitable section such as `messages` or a new `subRequirementHints` section:

```ts
subRequirementHints: {
  defaultFromParent: '已默认带出父需求负责人，可单独修改',
  parentHasNoAssignee: '父需求未设置负责人，请为子需求单独选择',
  independentFromParent: '当前负责人已独立于父需求',
  parentAssigneeUnavailable: '父需求负责人当前不可选，请重新选择子需求负责人'
}
```

**Step 4: Run verification to verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b`
Workdir: `frontend`
Expected: PASS.

**Step 5: Commit**

```bash
git add frontend/src/constants/requirement.ts
git commit -m "feat(requirement): add sub-requirement assignee hint copy"
```

### Task 2: Load parent requirement in the standalone create page and default assignee

**Files:**
- Modify: `frontend/src/views/requirement/RequirementCreatePage.vue`

**Step 1: Write the failing structural expectation**

```bash
rg -n "parentRequirement|loadParentRequirement|initializeParentAssignee|assigneeTouched" frontend/src/views/requirement/RequirementCreatePage.vue
```

**Step 2: Run the check to verify RED**

Run: `rg -n "parentRequirement|loadParentRequirement|initializeParentAssignee|assigneeTouched" frontend/src/views/requirement/RequirementCreatePage.vue`
Expected: no matches.

**Step 3: Write minimal implementation**

Add parent requirement state and initialization logic:

```ts
const parentRequirement = ref<any | null>(null)
const assigneeTouched = ref(false)

function parentAssigneeAvailable(parentAssigneeId: number | null | undefined) {
  if (!parentAssigneeId) return false
  return members.value.some((member: any) => member.userId === parentAssigneeId)
}

function initializeAssigneeFromParent() {
  if (!parentIdFromQuery || assigneeTouched.value || !parentRequirement.value) return
  const parentAssigneeId = parentRequirement.value.assigneeId ?? null
  form.assigneeId = parentAssigneeAvailable(parentAssigneeId) ? parentAssigneeId : null
}

async function loadParentRequirement() {
  if (!parentIdFromQuery) return
  try {
    const res = await requirementApi.get(parentIdFromQuery)
    parentRequirement.value = (res as any).data ?? null
    initializeAssigneeFromParent()
  } catch {
    parentRequirement.value = null
  }
}
```

Call `loadParentRequirement()` after members and requirements are loaded.

**Step 4: Run verification to verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b`
Workdir: `frontend`
Expected: PASS.

Manual GREEN check:
- Open `RequirementDetailPage`
- Click `新建子需求`
- Confirm the create page opens with the parent assignee preselected when available

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementCreatePage.vue
git commit -m "feat(requirement): default child assignee from parent on create page"
```

### Task 3: Preserve user edits and surface assignee hint state in the create page

**Files:**
- Modify: `frontend/src/views/requirement/RequirementCreatePage.vue`
- Modify: `frontend/src/constants/requirement.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "sub-assignee-hint|assigneeHintText|handleAssigneeChange|independentFromParent" frontend/src/views/requirement/RequirementCreatePage.vue frontend/src/constants/requirement.ts
```

**Step 2: Run the check to verify RED**

Run: `rg -n "sub-assignee-hint|assigneeHintText|handleAssigneeChange|independentFromParent" frontend/src/views/requirement/RequirementCreatePage.vue frontend/src/constants/requirement.ts`
Expected: no matches in the view file for the new UI state.

**Step 3: Write minimal implementation**

Add a computed hint state and track manual edits:

```ts
function handleAssigneeChange(value: number | null) {
  assigneeTouched.value = true
  form.assigneeId = value
}

const assigneeHintText = computed(() => {
  if (!parentIdFromQuery || !parentRequirement.value) return ''
  const parentAssigneeId = parentRequirement.value.assigneeId ?? null
  if (!parentAssigneeId) return requirementText.subRequirementHints.parentHasNoAssignee
  if (!parentAssigneeAvailable(parentAssigneeId)) return requirementText.subRequirementHints.parentAssigneeUnavailable
  if (assigneeTouched.value && form.assigneeId !== parentAssigneeId) {
    return requirementText.subRequirementHints.independentFromParent
  }
  return requirementText.subRequirementHints.defaultFromParent
})
```

Render it under the assignee field:

```vue
<el-form-item :label="requirementText.formLabels.assignee">
  <el-select :model-value="form.assigneeId" @change="handleAssigneeChange" ... />
  <div v-if="assigneeHintText" class="sub-assignee-hint">{{ assigneeHintText }}</div>
</el-form-item>
```

Optionally render lightweight parent context above or below the assignee field:

```vue
<div v-if="parentRequirement" class="sub-parent-context">
  所属父需求：#{{ parentRequirement.id }} {{ parentRequirement.title }}
</div>
```

**Step 4: Run verification to verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b`
Workdir: `frontend`
Expected: PASS.

Manual GREEN check:
- Confirm hint shows “默认带出父需求负责人，可单独修改” when parent assignee exists
- Change assignee to another user
- Confirm hint changes to “当前负责人已独立于父需求”

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementCreatePage.vue frontend/src/constants/requirement.ts
git commit -m "feat(requirement): show editable parent-assignee hint for child creation"
```

### Task 4: Align the requirement list dialog with the same copy and parent context

**Files:**
- Modify: `frontend/src/views/requirement/RequirementPage.vue`
- Modify: `frontend/src/constants/requirement.ts`

**Step 1: Write the failing structural expectation**

```bash
rg -n "subRequirementParentContext|subAssigneeHint|defaultFromParent|independentFromParent" frontend/src/views/requirement/RequirementPage.vue frontend/src/constants/requirement.ts
```

**Step 2: Run the check to verify RED**

Run: `rg -n "subRequirementParentContext|subAssigneeHint|defaultFromParent|independentFromParent" frontend/src/views/requirement/RequirementPage.vue frontend/src/constants/requirement.ts`
Expected: no view-side hint state yet.

**Step 3: Write minimal implementation**

Preserve current prefill behavior from `openSubReqCreate(parentReq)` and add lightweight context + hint rendering:

```ts
const subRequirementParentContext = ref<any | null>(null)

function openSubReqCreate(parentReq: any) {
  subRequirementParentContext.value = parentReq ?? null
  Object.assign(form, {
    assigneeId: parentReq?.assigneeId ?? null,
    sprintId: parentReq?.sprintId ?? null,
    parentId: parentReq?.id ?? null,
    ...
  })
  showCreate.value = true
}
```

Render under the assignee field:

```vue
<div v-if="form.parentId && subRequirementParentContext" class="sub-assignee-hint">
  {{ requirementText.subRequirementHints.defaultFromParent }}
</div>
```

Reset `subRequirementParentContext.value = null` when the dialog closes.

**Step 4: Run verification to verify GREEN**

Run: `npm.cmd exec -- vue-tsc -b`
Workdir: `frontend`
Expected: PASS.

Manual GREEN check:
- Open `RequirementPage`
- Expand a parent requirement and click `新建子需求`
- Confirm the assignee remains prefilled and the same hint copy is shown

**Step 5: Commit**

```bash
git add frontend/src/views/requirement/RequirementPage.vue frontend/src/constants/requirement.ts
git commit -m "style(requirement): align sub-requirement dialog hint copy"
```

### Task 5: Final verification for both child-creation entry paths

**Files:**
- Modify: `docs/plans/2026-03-06-sub-requirement-assignee-default-design.md` (optional note updates only)

**Step 1: Run frontend type-check**

Run: `npm.cmd exec -- vue-tsc -b`
Workdir: `frontend`
Expected: PASS.

**Step 2: Run production build**

Run: `npm.cmd run build`
Workdir: `frontend`
Expected: PASS.

**Step 3: Run structural spot checks**

Run:
- `rg -n "subRequirementHints|defaultFromParent|independentFromParent|parentAssigneeUnavailable" frontend/src/constants/requirement.ts`
- `rg -n "parentRequirement|assigneeTouched|assigneeHintText|sub-assignee-hint" frontend/src/views/requirement/RequirementCreatePage.vue frontend/src/views/requirement/RequirementPage.vue`

Expected: matches appear in the intended files.

**Step 4: Perform manual verification**

Manual checklist:
- From `RequirementPage`, create a child requirement and confirm the parent assignee is prefilled
- From `RequirementDetailPage`, create a child requirement and confirm the same defaulting behavior
- Change the child assignee in both paths and confirm the new assignee is submitted
- Verify the parent requirement assignee is unchanged after child creation
- Verify the “parent has no assignee” path leaves child assignee empty

Expected: both entry paths behave consistently and the child assignee remains independently editable.

**Step 5: Validate git state**

Run: `git status --short --branch`
Expected: only intended frontend and docs files changed.

**Step 6: Commit final docs update (optional)**

```bash
git add docs/plans/2026-03-06-sub-requirement-assignee-default-design.md docs/plans/2026-03-06-sub-requirement-assignee-default-implementation-plan.md
git commit -m "docs: capture sub-requirement assignee default plan"
```

---

## Verification Checklist Before Completion

- Requirement list dialog and standalone create page both default child assignee from parent assignee.
- Child assignee remains fully editable.
- Hint copy clearly explains the defaulting rule.
- Parent-assignee edge cases are handled gracefully.
- `vue-tsc` and production build both pass.
