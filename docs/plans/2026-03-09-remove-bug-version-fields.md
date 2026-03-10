# Remove Bug Version Fields Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove the placeholder "resolved version" and "affected version" fields from Bug resolve/reopen flows so the UI matches current backend capabilities.

**Architecture:** This is a frontend-only cleanup. Update the two Bug views to remove the unused form controls, delete the related local state and validation, and keep the remaining resolve/reopen actions unchanged.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vite

---

### Task 1: Remove fields from list page dialogs

**Files:**
- Modify: `frontend/src/views/bug/BugPage.vue`

**Step 1: Write the failing test**

No dedicated frontend test harness exists in this repository. Use build validation as the narrowest available safety check for this UI-only change.

**Step 2: Run test to verify it fails**

Not applicable because there is no frontend test runner or existing frontend spec pattern.

**Step 3: Write minimal implementation**

- Remove the resolve dialog "解决版本" form item
- Remove the reopen dialog "影响版本" form item
- Remove `resolvedVersion` and `affectedVersion` from component state
- Remove `reopenRules` validation tied to `affectedVersion`

**Step 4: Run test to verify it passes**

Run: `npm run build`

Expected: frontend build succeeds.

### Task 2: Remove fields from detail page dialogs

**Files:**
- Modify: `frontend/src/views/bug/BugDetailPage.vue`

**Step 1: Write the failing test**

No dedicated frontend test harness exists in this repository. Use build validation as the narrowest available safety check for this UI-only change.

**Step 2: Run test to verify it fails**

Not applicable because there is no frontend test runner or existing frontend spec pattern.

**Step 3: Write minimal implementation**

- Remove the resolve dialog "解决版本" form item
- Remove the reopen dialog "影响版本" form item
- Remove `resolvedVersion` and `affectedVersion` from component state
- Remove `reopenRules` validation tied to `affectedVersion`
- Stop writing these fields into reopen comments

**Step 4: Run test to verify it passes**

Run: `npm run build`

Expected: frontend build succeeds.

### Task 3: Validate end state

**Files:**
- Verify: `frontend/src/views/bug/BugPage.vue`
- Verify: `frontend/src/views/bug/BugDetailPage.vue`

**Step 1: Write the failing test**

Not applicable; validate via build and code inspection.

**Step 2: Run test to verify it fails**

Not applicable.

**Step 3: Write minimal implementation**

- Ensure resolve flow still submits `resolveType`
- Ensure reopen flow still submits status update and optional reassignment/comment
- Ensure no dead state or rules remain

**Step 4: Run test to verify it passes**

Run: `npm run build`

Expected: frontend build succeeds without TypeScript errors caused by removed fields.
