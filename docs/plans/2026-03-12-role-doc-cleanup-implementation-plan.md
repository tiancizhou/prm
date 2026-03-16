# PRM Role Documentation Cleanup Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Clean active reference docs to the current three-role model while marking older role-based design documents as historical references tied to the retired PM/QA/GUEST role set.

**Architecture:** Update the current system-design doc directly, then add lightweight historical notes to older role-based design and implementation documents so they stop misleading ongoing work without erasing their historical context.

**Tech Stack:** Markdown docs, `rg` verification.

---

### Task 1: Update current role model doc

**Files:**
- Modify: `docs/plans/2026-02-26-prm-design.md`

**Step 1: Replace the old six-role description with the current three-role model**

### Task 2: Add historical notes to older role-based docs

**Files:**
- Modify: `docs/plans/2026-03-06-role-based-ui-redesign-design.md`
- Modify: `docs/plans/2026-03-06-role-based-ui-redesign-implementation-plan.md`

**Step 1: Add a short note near the top saying these docs reference an older role model**

### Task 3: Verification

**Step 1:** `rg -n "系统角色：|旧角色模型|当前有效角色模型" docs/plans`

Expected:
- current design doc shows only `SUPER_ADMIN / PROJECT_ADMIN / DEV`
- historical docs clearly state they refer to the old role model
