# Admin User Name Scope Trigger Design

**Date:** 2026-03-13

## Background

后台用户页已经支持通过右侧查看图标打开“项目范围”抽屉，但用户主名文本仍然只是静态展示。

## Confirmed Decision

- 采用 `姓名可点 + 保留查看图标` 方案
- 姓名主文本作为主入口
- 右侧查看图标继续保留，行为保持一致
- 不改接口、不改抽屉内容、不改其他操作按钮

## UX Rule

- 点击姓名主文本时，直接调用现有 `openScopeDrawer(row)`
- 姓名主文本需要具备按钮语义与可聚焦能力
- Hover / focus 状态需要明显但克制，保持现有后台风格一致

## Scope

### In Scope

- `frontend/src/views/admin/AdminUsersPage.vue` 中姓名主文本交互改造
- 补充对应的按钮样式

### Out of Scope

- 抽屉内容改造
- API 改动
- 其他页面联动改造

## Success Criteria

- 点击姓名即可打开项目范围抽屉
- 查看图标仍然可用
- 前端类型检查通过
