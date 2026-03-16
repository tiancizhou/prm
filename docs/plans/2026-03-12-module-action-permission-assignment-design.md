# PRM Module and Action Permission Assignment Design

**Date:** 2026-03-12

## Background

系统已经具备：

- 模块级权限分配
- 模块级导航和路由生效
- 动作级权限码与页面按钮显隐

但当前权限分配弹层仍然只能配置模块级权限，动作级权限还不能在同一处完成配置。这导致：

- `SUPER_ADMIN` 之外的角色无法真正获得动作级按钮
- 权限体系在“分配”与“生效”之间仍不闭环

用户已经确认：

- 继续沿用现有 `按模块分配权限` 弹层
- 右侧升级成 `模块 + 动作` 两层结构
- 联动规则采用“弱联动 + 自动兜底”

## Confirmed Decisions

- 左侧继续选择角色分组
- 右侧每个模块块展示：
  - 模块勾选
  - 模块编码
  - 该模块下的动作勾选项
- 第一版联动规则：
  - 勾选动作 → 自动勾选所属模块
  - 取消模块 → 自动取消其下所有动作
  - 取消动作 → 不影响模块本身

## Goals

1. 让模块级与动作级权限在同一入口中完成配置
2. 让动作权限真正可分配、可生效
3. 保持当前权限弹层的信息层级清晰，不引入更重的树形组件

## Data Model

### Existing Tables Reused

- `sys_permission`
- `sys_role_permission`

### Permission Types

- `MODULE`
- `ACTION`

### Parent Relationship

- 动作权限通过 `sys_permission.parent_id` 归属到某个模块权限

示例：

- `requirement:view`
  - `requirement:create`
  - `requirement:update`
  - `requirement:delete`
  - `requirement:assign`

- `bug:view`
  - `bug:create`
  - `bug:update`
  - `bug:delete`
  - `bug:assign`

- `admin:view`
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

## Backend Behavior

### List Assignable Permissions

- 返回模块列表
- 每个模块附带其动作子项

### Read Role Assignments

- 返回某角色当前已绑定的所有相关权限码
- 包含模块与动作

### Save Role Assignments

- 接收完整权限码集合
- 自动补齐：若包含动作，则确保父模块一并保存
- 保存前删除该角色已有的 `MODULE + ACTION` 绑定
- 保留未来其他类型权限绑定不受影响

## Frontend UX

### Left Column

- 真实角色分组列表，保持不变

### Right Column

- 每个模块一张卡片
- 模块标题行：
  - 模块勾选框
  - 模块名称
  - 模块编码
- 动作区：
  - 多个动作勾选框

### Interaction Rules

- 勾选动作时自动确保父模块已选
- 取消模块时清空其动作
- 保存时提交完整 `permissionCodes`

## Expected Outcome

完成后：

- 一个角色能在一个弹层内同时配置模块和动作权限
- 已有动作按钮显隐能力将真正可配置地生效
- 权限管理从“模块入口控制”升级为“模块 + 操作能力”闭环
