# PRM Role Model Simplification Design

**Date:** 2026-03-12

## Background

当前系统历史上保留了 6 个系统角色：

- `SUPER_ADMIN`
- `PROJECT_ADMIN`
- `PM`
- `DEV`
- `QA`
- `GUEST`

但经过前面的权限梳理后可以确认：

- `PROJECT_ADMIN` 已经承担了真正的“项目经理 / 项目管理者”职责
- `PM` 没有形成独立权限边界
- `QA / GUEST` 也尚未形成必须保留的独立权限模型

用户明确要求：为了极简开发，当前系统只保留：

- `SUPER_ADMIN`
- `PROJECT_ADMIN`
- `DEV`

未来如果需要更多角色，再通过新增角色组与权限分配扩展。

## Confirmed Decisions

- 当前系统角色模型收敛为 3 个角色：
  - `SUPER_ADMIN`
  - `PROJECT_ADMIN`
  - `DEV`
- 历史角色迁移规则：
  - `PM -> PROJECT_ADMIN`
  - `QA -> DEV`
  - `GUEST -> DEV`
- 清理策略不是“只在 UI 隐藏”，而是：
  - 迁移用户角色关系
  - 迁移角色权限关系
  - 清理旧角色数据
  - 收敛前端角色展示与文案

## Goals

1. 把当前角色模型压缩到真正需要的最小集合
2. 避免历史角色继续污染权限页、用户页和回归检查
3. 为后续“按需新增角色组”保留清晰扩展路径

## Data Migration Strategy

### User Role Mapping

- `sys_user_role`
  - `PM -> PROJECT_ADMIN`
  - `QA -> DEV`
  - `GUEST -> DEV`

采用 `INSERT OR IGNORE` 迁移，再删除旧绑定，避免唯一约束冲突。

### Role Permission Mapping

- `sys_role_permission`
  - `PM` 的权限绑定并入 `PROJECT_ADMIN`
  - `QA / GUEST` 的权限绑定并入 `DEV`

### Old Role Cleanup

- 迁移完绑定关系后，删除旧角色：
  - `PM`
  - `QA`
  - `GUEST`

## Frontend Simplification Scope

### Admin Users

- 角色列表只显示 3 个角色
- 顶部筛选不再使用 `GUEST` 作为“外部人员”判断依据

### Admin Permissions

- 角色分组列表只显示 3 个角色
- 权限分配弹层只针对 3 个核心角色工作

### Project Members / Other Role Labels

- 去掉对 `PM / QA / GUEST` 的展示映射依赖
- 默认围绕 `SUPER_ADMIN / PROJECT_ADMIN / DEV` 展示

## Future Role Expansion

后续若需要新角色，不需要重建模型，只需要：

1. 新增角色组
2. 给该角色组分配模块权限与动作权限
3. 按需要补充默认文案或默认权限模板

也就是说，未来扩展角色的方式就是“新增角色组 + 分配权限”，而不是再改权限框架。

## Expected Outcome

完成后：

- 系统角色模型更简单
- 权限页、用户页、角色展示更一致
- 未来新增角色仍然保留扩展性，但当前开发负担最小
