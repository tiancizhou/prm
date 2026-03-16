# PRM Organization Module Design

**Date:** 2026-03-11

## Background

当前 PRM 已有系统级人员管理页面 `frontend/src/views/system/UserPage.vue`，但它仍挂在旧的系统管理语义下，且最左侧 `组织` 一级入口还只是占位按钮，无法承担真正的组织域入口职责。

用户已确认本轮要把“系统整体的人员管理”与“组织模块”接起来，并采用轻量导航枢纽模式，而不是一开始就做重型组织工作台。

## Confirmed Decisions

- 一级导航 `组织` 改为真实入口
- 组织模块采用 `B1`：轻量导航枢纽首页
- 第一批入口卡片为：`人员管理`、`团队/部门`、`角色权限`
- 第一批真正可用能力仅 `人员管理`
- `团队/部门`、`角色权限` 先落占位页面，保证信息架构完整
- 旧地址 `/system/users` 保留兼容，并重定向到新的组织域入口

## Goals

本轮组织模块接线要解决三个问题：

1. 让 `组织` 从占位导航升级为真实一级域
2. 让系统级人员管理拥有清晰的组织语义入口
3. 为后续团队/部门、角色权限扩展预留稳定结构，而不是继续把用户页塞在系统设置里

## Information Architecture

### Level 1 Navigation

- `组织`：进入 `/organization`
- `后台设置`：保留为未来系统配置入口，不再承载人员管理

### Organization Routes

- `/organization` → 组织首页
- `/organization/users` → 人员管理
- `/organization/teams` → 团队/部门（占位）
- `/organization/roles` → 角色权限（占位）
- `/system/users` → 重定向到 `/organization/users`

## Organization Home

### Page Role

组织首页不是大盘，而是轻量的组织管理枢纽：

- 标题：`组织管理`
- 副标题：说明这里统一管理系统人员、组织结构与角色权限
- 三张卡片入口：
  - `人员管理`
  - `团队/部门`
  - `角色权限`

### Card Rules

每张卡片包含：

- 图标
- 标题
- 简短描述
- 状态标签（`已启用` / `即将开放`）
- 主按钮

### First Version Behavior

- `人员管理`：跳转 `/organization/users`
- `团队/部门`：跳转 `/organization/teams`
- `角色权限`：跳转 `/organization/roles`

## Reuse Strategy

### People Management

- 复用现有 `frontend/src/views/system/UserPage.vue`
- 在组织路由下作为 `/organization/users` 使用
- 仅对页头文案做组织语义适配，不重写用户管理表格、创建、编辑和状态操作逻辑

### Placeholder Pages

- 新增统一轻量占位页组件，用于 `/organization/teams` 与 `/organization/roles`
- 样式与主壳层一致，清楚说明“即将开放”，并提供返回组织首页入口

## Shell Integration

### Main Layout

- `MainLayout` 的一级导航中，`组织` 改为可点击入口
- 可见范围与当前系统级人员管理入口保持一致（即超级管理员可见）
- `后台设置` 保留但先不接人员页，避免与组织入口重复

### Header Semantics

- `/organization`：
  - 路径：`组织`
  - 标题：`组织管理`
- `/organization/users`：
  - 路径：`组织`
  - 标题：`人员管理`
- `/organization/teams`：
  - 路径：`组织`
  - 标题：`团队与部门`
- `/organization/roles`：
  - 路径：`组织`
  - 标题：`角色权限`

## Risks and Mitigations

### Risk 1: Duplicate access paths confuse users

- **Risk:** 旧的 `/system/users` 与新的 `/organization/users` 若同时暴露，会导致导航重复
- **Mitigation:** 保留旧路径仅作重定向，不再作为主导航入口

### Risk 2: User page copy still reads as old system settings page

- **Risk:** 复用 `UserPage` 时，页头文案若不调整，会削弱组织域语义
- **Mitigation:** 通过路由名识别，在组织路径下切换为“人员管理”文案

### Risk 3: Placeholder pages feel fake

- **Risk:** 占位页若只是空白，会影响专业感
- **Mitigation:** 给占位页提供明确标题、说明、状态标签和返回入口，让结构完整但不假装功能已完成

## Expected Outcome

完成本轮后，PRM 的组织域应达到以下状态：

- `组织` 成为真实一级导航入口
- 系统级人员管理通过 `/organization/users` 进入
- 组织首页成为清晰的入口枢纽
- `团队/部门`、`角色权限` 虽未完成，但已有正确的产品结构落点

