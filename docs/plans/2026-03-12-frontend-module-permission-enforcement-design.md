# PRM Frontend Module Permission Enforcement Design

**Date:** 2026-03-12

## Background

系统已经具备真实的模块权限分配能力：

- 模块级权限写入 `sys_permission`
- 角色与权限绑定写入 `sys_role_permission`
- 用户通过角色继承模块权限

但当前前端还没有真正消费这些权限码，因此“分配了权限”和“界面实际生效”之间仍然断开：

- 左侧导航不会根据权限变化
- 路由访问不会根据权限变化
- 项目内二级导航也不会根据权限变化

用户已确认：第一版前端权限生效先做到 **导航 + 路由访问**，并且 `SUPER_ADMIN` 永远全量放行。

## Confirmed Decisions

- 第一版生效范围：
  - 左侧一级导航
  - 顶部域内入口
  - 项目域二级菜单
  - 路由访问守卫
- 第一版不处理按钮级显隐
- `SUPER_ADMIN` 永远拥有全部模块权限，不受分配限制
- 前端采用统一权限中心，而不是在各页面零散判断

## Goals

1. 让模块权限在前端真正可见、可感知
2. 保持权限判断集中、可扩展
3. 为后续动作级权限扩展打基础

## Permission Source

### Backend

- 登录接口返回：
  - `roles`
  - `permissions`
- 当前用户接口返回：
  - `roles`
  - `permissions`

### Frontend

- `auth` store 保存：
  - 当前用户角色码
  - 当前用户权限码
- 暴露统一能力：
  - `isSuperAdmin`
  - `hasPermission(code)`
  - `hasAnyPermission(codes)`

## Permission Mapping

### Global Navigation

- `dashboard:view` → `工作台`
- `projects:view` → `项目集`
- `docs:view` → `文档`
- `organization:view` → `组织`
- `admin:view` → `后台`

### Project Secondary Navigation

- `project.overview:view` → `项目概览`
- `requirement:view` → `需求`
- `bug:view` → `Bug`
- `sprint:view` → `迭代`
- `project.member:view` → `成员`

### Domain Tabs

- `organization:view` → 组织域入口
- `admin:view` → 后台域入口

当前组织域内部的 `团队 / 动态 / 公司` 先统一视为 `organization:view` 范围；后台域内部的 `部门 / 用户 / 权限` 先统一视为 `admin:view` 范围。

## Frontend Enforcement

### Main Layout

- 左侧一级导航仅显示当前用户有权限的入口
- 若用户无 `projects:view`，则不显示 `项目集`

### Project Sidebar

- 进入项目后，仅显示有权限的二级菜单
- 如果某用户有 `projects:view` 但没有对应子模块权限，则只看到有权限的项目子菜单

### Domain Top Navigation

- 仅在拥有对应域权限时显示该域入口与标签

### Router Guard

- 每个受控路由补充模块权限元信息
- 路由守卫中：
  - `SUPER_ADMIN` 直接放行
  - 普通用户若缺权限，则跳转到第一个安全可访问页

## Safe Fallback Strategy

推荐顺序：

1. `dashboard:view`
2. `projects:view`
3. `organization:view`
4. `admin:view`
5. 若都没有，则保留登录态但跳到一个受限提示页（第一版可先回退 `/dashboard` 并提示）

## Expected Outcome

完成后：

- 分配模块权限后，界面入口会真实变化
- 用户无法再通过手工输入 URL 进入无权限模块
- `SUPER_ADMIN` 维持全量访问能力
- 前端权限体系具备继续扩展到动作级权限的结构基础
