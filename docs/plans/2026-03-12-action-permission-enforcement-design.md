# PRM Action Permission Enforcement Design

**Date:** 2026-03-12

## Background

模块级权限已经在前端真正生效：

- 控制导航显示
- 控制路由访问

但用户还希望更细一层：进入页面后，按钮和主要操作也要按权限显隐。第一批范围已经确认：

- 后台管理页：`新增 / 编辑 / 删除 / 分配权限`
- 项目页：`创建 / 编辑 / 删除 / 指派`

## Confirmed Decisions

- 第一批优先页面：
  - `后台 > 部门`
  - `后台 > 用户`
  - `后台 > 权限`
  - `需求`
  - `Bug`
- 第一批实现方式：
  - 继续复用现有 `permissions`
  - 页面按钮统一走 `authStore.canAccess(actionCode)`
- 第一版先做按钮级显隐，不做复杂 disabled/tooltip 降级

## Action Permission Codes

### Admin

- `department:create`
- `department:update`
- `department:delete`
- `user:create`
- `user:update`
- `user:delete`
- `role-group:create`
- `role-group:update`
- `role-group:delete`
- `permission:assign`

### Project

- `requirement:create`
- `requirement:update`
- `requirement:delete`
- `requirement:assign`
- `bug:create`
- `bug:update`
- `bug:delete`
- `bug:assign`

## Enforcement Rules

- 没有模块 `view` 权限：页面入口和路由都不可见
- 有模块 `view` 权限但没有动作权限：页面可进，但相关按钮不显示
- `SUPER_ADMIN`：所有动作直接放行

## First Batch Scope

### Admin Department

- 控制：
  - 新增一行
  - 保存当前部门
  - 删除当前部门
  - 子部门批量保存（内部按 create/update/delete 分别校验）

### Admin Users

- 控制：
  - 添加用户
  - 编辑用户
  - 启用/禁用
  - 删除用户

### Admin Permissions

- 控制：
  - 新增分组
  - 编辑分组
  - 删除分组
  - 按模块分配权限

### Requirement

- 控制：
  - 新建需求
  - 新建子需求
  - 编辑需求
  - 批量编辑
  - 批量指派

### Bug

- 控制：
  - 报 Bug
  - 指派 Bug
  - 编辑 Bug
  - 删除 Bug

## Expected Outcome

完成后，权限体系会形成两层稳定生效：

- 模块级权限：控制页面能不能进入
- 动作级权限：控制进入后能做什么

这样既符合后台权限管理的心智，也为后续真正细化到动作分配打下基础。
