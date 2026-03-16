# User Business Detail Source-Aware Back Design

**Date:** 2026-03-13

## Background

当前 `用户业务详情页` 已经可以从两个入口进入：

- `组织团队页`
- `项目成员页`

但返回按钮始终固定回到 `/organization/team`。这会导致从项目成员页进入详情后，点击返回不能回到原来的项目成员列表，影响连续操作体验。

## Confirmed Decision

- 采用显式来源参数方案
- 从 `项目成员页` 进入详情时，带上来源 query 参数
- 详情页 `goBack()` 根据来源参数判断返回目标：
  - 来自项目成员页 → 返回对应 `/projects/:id/members`
  - 否则 → 返回 `/organization/team`

## Recommended Approach

采用**来源参数 + 稳定路由返回**方案。

### Why this approach

- 不依赖浏览器历史，结果稳定可预期
- 不需要新增路由或页面
- 只改两个前端页面，改动面最小
- 与当前 `从简开发` 方向一致

## UX Strategy

### Entry from project members

项目成员页跳转到用户业务详情时附带：

- `from=project-members`
- `projectId=<当前项目 id>`

### Back behavior on detail page

详情页点击“返回”时：

- 如果 `from === 'project-members'` 且存在 `projectId`
  - 返回 `/projects/${projectId}/members`
- 否则
  - 返回 `/organization/team`

## Scope

### In Scope

- `frontend/src/views/project/ProjectMembersPage.vue`
- `frontend/src/views/organization/OrganizationUserBusinessPage.vue`

### Out of Scope

- 后端改动
- 新增来源类型
- 详情页内容改造
- 浏览器历史兜底逻辑增强

## Success Criteria

- 从项目成员页进入详情后，返回按钮回到对应项目成员页
- 从组织团队页进入详情后，返回按钮仍回组织团队页
- 前端类型检查通过
