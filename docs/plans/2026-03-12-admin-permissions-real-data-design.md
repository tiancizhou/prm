# PRM Admin Permissions Real Data Design

**Date:** 2026-03-12

## Background

当前 `后台 > 权限` 页面已经不再是纯静态壳子，但它仍然是“半真实”状态：

- 角色来源于真实的 `sys_role`
- 用户来源于真实的 `sys_user + sys_user_role`
- 但页面的数据映射仍然依赖前端写死的描述回退逻辑
- 操作列全部是空壳按钮，没有形成真正可用的角色分组管理页

用户希望这个页面参考给定截图，按“分组列表”的形式展示，并且里面的数据要是真实数据。

## Confirmed Decisions

- 页面按 **角色分组列表** 实现，不做权限矩阵
- 列结构对齐参考图：
  - `ID`
  - `分组名称`
  - `分组描述`
  - `用户列表`
  - `操作`
- 数据范围采用 **C1**：
  - 严格使用当前数据库中已有的真实角色数据
  - 不为了贴近截图而额外伪造角色分组
- 操作策略：
  - 本轮做真实：`查看 / 成员 / 编辑 / 删除`
  - 先保留入口：`权限 / 复制`

## Goals

1. 让权限页从“示意页”变成“真实角色分组页”
2. 让表格数据全部来自真实角色和真实用户角色关系
3. 在不一次扩成完整权限中心的前提下，提供基础可用的角色管理能力

## Data Mapping

### Group Name

- 来源：`sys_role.name`

### Group Description

- 优先来源：`sys_role.description`
- 若为空：回退 `sys_role.name`

### User List

- 来源：`sys_user_role` 关联到 `sys_user`
- 展示规则：
  - 优先显示 `realName`
  - 为空时回退 `username`

### ID

- 来源：`sys_role.id`

## Page Structure

### Toolbar

- 保留：`按模块分配权限`
- 保留：`新增分组`

其中：

- `新增分组` 本轮做真实创建
- `按模块分配权限` 本轮先保留入口，不做完整权限矩阵

### Table

- 结构与参考图保持一致
- `用户列表` 允许长文本，但优先保持单行截断或受控换行，避免表格失衡

### Action Column

#### Real in this round

- `查看`
  - 查看角色基础信息与当前成员
- `成员`
  - 查看该角色绑定的真实用户
- `编辑`
  - 编辑角色名与描述
- `删除`
  - 仅在该角色无绑定用户时允许删除

#### Reserved in this round

- `权限`
  - 保留入口，可先用轻量提示或占位弹层
- `复制`
  - 保留入口，后续补齐

## Backend Scope

### Existing reusable data

- `sys_role`
- `sys_user`
- `sys_user_role`
- 已有角色查询能力：`/api/system/users/roles`

### New minimal backend needed

为了让页面真正可用，需要新增后台角色管理接口，至少包括：

- 角色列表
- 角色详情（含成员）
- 新建角色
- 编辑角色
- 删除角色（带成员绑定校验）

## Frontend Scope

- 新增角色 API 封装
- 将 `AdminPermissionsPage` 从前端拼装占位页升级为真实角色分组列表页
- 增加最小弹层交互：
  - 查看
  - 成员
  - 编辑 / 新增

## Expected Outcome

完成后，`后台 > 权限` 会变成一张真实的角色分组表：

- 数据来自真实角色与真实成员关系
- 页面结构接近参考图
- 至少具备查看、编辑、新增、删除这四个真实管理动作
- 为后续再补“按模块分配权限”打下稳定基础
