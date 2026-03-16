# PRM Module Permission Assignment Design

**Date:** 2026-03-12

## Background

`后台 > 权限` 的角色分组列表已经切到真实数据，但顶部的 `按模块分配权限` 仍然只是保留入口。用户已确认希望把它做成真正可配置的权限分配能力，并且第一版先按模块级配置，不先下钻到动作级。

## Confirmed Decisions

- 第一版按 **模块级权限** 配置
- 后续再细化到动作级权限
- 交互形态采用：
  - 左侧选择角色
  - 右侧勾选模块
- 入口保留在 `后台 > 权限` 页顶部按钮中
- 第一版优先做成 **大弹层**，不新开独立页面

## Goals

1. 让角色可以真实配置“能访问哪些模块”
2. 复用现有 `sys_permission / sys_role_permission` 结构，不重建权限体系
3. 保持实现范围最小，只处理模块可见性，不处理动作级授权

## Permission Model

### Storage

- `sys_permission`
  - 记录模块级权限项
- `sys_role_permission`
  - 记录角色与模块权限的绑定关系

### First-Version Permission Codes

- `dashboard:view`
- `projects:view`
- `docs:view`
- `organization:view`
- `admin:view`
- `project.overview:view`
- `requirement:view`
- `bug:view`
- `sprint:view`
- `project.member:view`

### Permission Type

- 第一版建议在 `sys_permission.type` 中使用 `MODULE`
- 这样可以和未来的 `ACTION` / `API` 类型分开

## UX Design

### Entry

- `后台 > 权限` 顶部按钮：`按模块分配权限`

### Dialog Layout

- 左侧：角色列表
- 右侧：模块权限面板

### Left Column

- 展示真实角色分组
- 点击切换当前配置角色

### Right Column

- 按模块卡片或分组列表展示可配置项
- 每个模块一个勾选开关
- 保存时整体覆盖该角色当前模块权限绑定

## Backend API Scope

- `GET /api/admin/permissions/modules`
  - 返回所有模块级权限项
- `GET /api/admin/roles/{id}/module-permissions`
  - 返回某角色当前已绑定的模块权限编码
- `PUT /api/admin/roles/{id}/module-permissions`
  - 整体覆盖保存该角色的模块权限绑定

## Frontend Scope

- 新增模块权限 API 封装
- 权限页新增分配弹层
- 左侧加载真实角色列表
- 右侧加载真实模块权限列表和当前勾选状态

## Expected Outcome

完成后，`按模块分配权限` 会变成真实可用的配置入口：

- 能为真实角色分组配置可访问模块
- 配置结果落入真实权限表
- 后续扩展动作级权限时，不需要推翻当前设计
