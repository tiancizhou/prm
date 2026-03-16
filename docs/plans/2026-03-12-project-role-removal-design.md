# PRM Project Role Removal Design

**Date:** 2026-03-12

## Background

当前系统同时存在两套角色语义：

- 系统角色：`SUPER_ADMIN / PROJECT_ADMIN / DEV`
- 项目成员角色：`pm_project_member.role`

用户已明确要求：项目角色没有存在必要，项目内权限应直接复用系统角色；项目成员关系只承担“是否属于该项目”的数据范围语义。

## Confirmed Decisions

- 保留 `pm_project_member`，但它只表示“项目成员关系”
- 不再使用 `pm_project_member.role` 作为权限判断来源
- 项目内权限统一规则：
  - 可读：项目成员即可（`SUPER_ADMIN` 仍保留全局兜底）
  - 可管理：项目成员 + 系统角色 `PROJECT_ADMIN`

## Goals

1. 去掉“双套角色”概念
2. 统一项目域权限心智：成员决定数据范围，系统角色决定能力边界
3. 保持改动尽量集中、可回归

## Strategy

### Stage 1: Logic Removal

- 后端不再依赖 `pm_project_member.role == PROJECT_ADMIN`
- 全部改为：
  - `ProjectAccessPolicy.canRead(membership)`
  - `ProjectAccessPolicy.canManage(membership)`

### Stage 2: Data Normalization

- 现有 `pm_project_member.role` 全部归一为 `MEMBER`
- 新增成员时也默认写入 `MEMBER`

### Stage 3: UI Consistency

- 项目成员页继续展示系统角色，不再强调项目角色

## Affected Services

- `ProjectService`
- `RequirementService`
- `BugService`
- `TaskService`
- `ModuleService`
- `SprintService`
- `ReleaseService`
- `AttachmentService`
- `DashboardService`

## Expected Outcome

完成后：

- 项目内权限来源只剩系统角色
- 项目成员表只承担数据范围语义
- 未来不需要再维护“项目角色”和“系统角色”两套概念
