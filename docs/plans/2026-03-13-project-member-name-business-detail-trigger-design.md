# Project Member Name Business Detail Trigger Design

**Date:** 2026-03-13

## Background

`项目成员` 页面已经完成“系统角色 + 数据范围 + 加入时间”的信息收敛，但成员姓名仍然只是静态文本。与此同时，组织域已经存在可复用的 `用户业务详情页`，支持从 `团队` 页面点击姓名后进入 `日程 / 需求 / Bug` 三类业务数据视图。

因此，本轮需要把项目成员页的姓名也接到这条已有用户详情链路上，让项目域中的“人”也可以快速进入统一的个人业务视图。

## Confirmed Decisions

- 入口：在 `项目成员页` 点击姓名主文本
- 目标：直接复用现有 `组织域用户业务详情页`
- 交互：采用 `姓名可点 + 保持现有静态辅助信息` 的简化方案
- 不新增第二个用户业务详情页面
- 不改后端，不新增接口

## Recommended Approach

采用**直接复用现有组织域详情页**方案。

### Why this approach

- 当前已经存在成熟的 `OrganizationUserBusinessPage` 组件和路由，复用成本最低
- 组织域 `团队` 页面已经有一套现成的姓名点击模式，可直接对齐
- 不需要额外维护第二份用户业务详情壳层或数据流
- 继续符合当前“从简开发”的方向

## UX Strategy

### 1) Entry Pattern

项目成员表中的姓名主文本改为可点击入口：

- 点击姓名 → 跳转到用户业务详情页
- 用户名/工号等副信息继续保持静态文本
- 系统角色、加入时间、成员维护操作保持现有行为不变

### 2) Navigation Target

直接跳转到现有组织域路由：

- `/organization/team/:userId`

这样可以保持与组织域团队页一致的用户详情访问方式。

### 3) Visual Consistency

为了降低学习成本，项目成员页的姓名点击样式建议复用组织域团队页思路：

- 主色文本链接风格
- hover 时清晰提示可点击
- 不引入额外图标或二级入口

## Scope

### In Scope

- `frontend/src/views/project/ProjectMembersPage.vue` 中姓名主文本交互改造
- 如有需要，微调样式使其具备可点击反馈

### Out of Scope

- 后端改动
- 新增详情页组件
- 用户业务详情页内容改造
- 项目域新增一套别名详情路由

## Reference Pattern

当前组织域团队页已经有现成入口：

- `frontend/src/views/organization/OrganizationTeamPage.vue`
  - `openUserBusiness(row)`
  - `router.push(`/organization/team/${row.id}`)`

项目成员页应尽量复用这套交互认知。

## Success Criteria

- 点击项目成员姓名可跳转到现有用户业务详情页
- 不影响成员移除、加入时间显示、系统角色显示等现有功能
- 不新增第二套用户详情路由或页面
- 前端类型检查通过
