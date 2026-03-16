# User Project Scope Data Design

**Date:** 2026-03-13

## Background

当前系统已经确认采用单一权限模型：

- `系统角色` 决定功能权限
- `项目成员关系` 决定业务数据范围

前端上一轮已经完成文案与结构收敛，但仍缺少两类真实业务数据：

1. 后台用户页的 `已加入项目数`
2. 用户详情中的 `项目范围（已加入项目列表）`
3. 项目成员页中成员 `加入当前项目的时间`

因此，本轮需要补齐后端数据返回与前端消费链路，让“系统角色 + 项目范围”模型不仅文案一致，而且数据也真实可见。

## Confirmed Decisions

本次设计依据已确认的用户输入：

- 后台用户页需要同时支持：
  - 列表显示 `已加入项目数`
  - 右侧详情区/抽屉显示 `完整项目范围`
- 项目成员页需要显示 `加入时间`
- 继续坚持简化模型：
  - 不新增项目角色概念
  - 不改变现有权限判断来源
- 本轮采用按需加载策略：
  - 列表接口只返回数量
  - 详情区再单独请求完整项目范围

## Recommended Approach

采用**列表轻量返回 + 详情按需加载**方案。

### Why this approach

- 后台用户列表只需要扫描能力，展示 `joinedProjectCount` 即可
- 完整项目列表只在查看某个用户时需要，应该按需加载
- 项目成员页的 `joinedAt` 属于项目成员上下文，应直接并入 `ProjectMemberVO`
- 这样可以避免把 `/system/users` 变成重接口，同时保持前端交互清晰

## Data Model Design

### 1) User List DTO

在现有 `UserDTO` 基础上新增：

- `joinedProjectCount: number`

用途：后台用户列表展示“未加入项目 / 已加入 N 个项目”。

### 2) User Project Scope Detail DTO

新增用于右侧详情区的专用 DTO，例如：

- `UserProjectScopeDTO`
  - `userId`
  - `displayName`
  - `roles`
  - `joinedProjects`

- `UserJoinedProjectDTO`
  - `id`
  - `name`
  - `code`

用途：后台用户页点击某个用户后，显示其完整项目范围。

### 3) Project Member DTO

在现有 `ProjectMemberVO` 基础上新增：

- `joinedAt: LocalDateTime`

用途：项目成员页展示“加入当前项目的时间”。

## API Design

### Existing API extension

#### `GET /api/system/users`

保持分页接口不变，但每个 `UserDTO` 新增 `joinedProjectCount`。

#### `GET /api/projects/{id}/members`

保持接口路径不变，但 `ProjectMemberVO` 新增 `joinedAt`。

### New API

#### `GET /api/system/users/{id}/project-scope`

返回当前用户的项目范围详情。

返回体职责：

- 基础身份概览（用于详情标题区）
- 系统角色
- 已加入项目列表

该接口不承担权限配置，不返回任何项目角色字段。

## Backend Design

### UserService changes

`UserService` 当前只依赖系统用户、部门、角色相关 mapper。为了支持项目范围数据，需要额外接入：

- `ProjectMemberMapper`
- `ProjectMapper`

职责拆分建议：

- 在 `page()` 中批量统计用户加入项目数量，填充 `joinedProjectCount`
- 新增 `getProjectScope(Long id)`，按用户返回完整项目范围 DTO

### ProjectService changes

`ProjectService#getMemberVOs()` 当前已经负责把成员表转成前端展示 VO，本轮直接在这里补：

- `joinedAt = member.getCreatedAt()`

这样可以复用现有接口，不新增成员页专用接口。

## Frontend Design

### 1) Admin Users Page

目标：让后台用户页同时表达“系统角色”和“项目范围”。

设计要点：

- 列表新增 `已加入项目` 列
  - `0 -> 未加入项目`
  - `N -> 已加入 N 个项目`
- 点击用户行或查看按钮时，打开右侧详情抽屉
- 详情抽屉展示：
  - 用户基础信息
  - 系统角色
  - 项目范围（完整项目列表）

说明：为减少主布局改动，本轮优先采用 `右侧抽屉`，而不是重做固定三栏布局。

### 2) Project Members Page

目标：让成员表展示真实的项目加入时间。

设计要点：

- 保持当前“成员 + 系统角色”的结构不变
- 新增一列 `加入时间`
- 语义明确为“加入当前项目的时间”，而不是入职时间或系统创建时间

## Error Handling

- 用户详情抽屉加载项目范围失败时：
  - 不影响用户列表显示
  - 抽屉内显示失败提示和重试入口或空状态提示
- 若用户未加入任何项目：
  - 列表显示 `未加入项目`
  - 抽屉显示 `当前未加入任何项目，因此暂时看不到项目业务数据`
- 项目成员 `joinedAt` 为空时：
  - 前端显示 `—`
  - 不阻塞成员页渲染

## Testing Strategy

### Backend

- 为 `UserService` 新增项目范围与项目数量的单测
- 为 `ProjectService#getMemberVOs()` 新增 `joinedAt` 映射单测
- 同步修正因 `UserService` 构造函数变更而受影响的现有测试

### Frontend

- 不新增测试框架
- 使用 `vue-tsc` 做类型回归验证
- 用定向检索确认相关页面已消费新字段并保留统一文案语义

## Target Files

### Backend

- `backend/src/main/java/com/prm/module/system/dto/UserDTO.java`
- `backend/src/main/java/com/prm/module/system/dto/UserProjectScopeDTO.java`
- `backend/src/main/java/com/prm/module/system/dto/UserJoinedProjectDTO.java`
- `backend/src/main/java/com/prm/module/system/application/UserService.java`
- `backend/src/main/java/com/prm/module/system/controller/UserController.java`
- `backend/src/main/java/com/prm/module/project/dto/ProjectMemberVO.java`
- `backend/src/main/java/com/prm/module/project/application/ProjectService.java`

### Backend Tests

- `backend/src/test/java/com/prm/module/system/UserServiceDepartmentScopeTests.java`
- `backend/src/test/java/com/prm/module/system/UserServiceDeleteTests.java`
- `backend/src/test/java/com/prm/module/system/UserServiceProjectScopeTests.java`
- `backend/src/test/java/com/prm/module/project/ProjectServiceMemberViewTests.java`

### Frontend

- `frontend/src/api/project.ts`
- `frontend/src/views/admin/AdminUsersPage.vue`
- `frontend/src/constants/adminPeople.ts`
- `frontend/src/views/project/ProjectMembersPage.vue`
- `frontend/src/constants/projectMembers.ts`

## Success Criteria

- 后台用户列表真实显示 `已加入项目数`
- 后台用户详情抽屉真实显示完整项目范围
- 项目成员页真实显示成员加入当前项目的时间
- 整体系统仍只表达一套角色模型：
  - `系统角色 = 权限能力`
  - `项目成员 = 数据范围`
- 前端类型检查通过
- 后端至少完成本地单测或静态验证；若受环境限制，需明确记录限制原因
