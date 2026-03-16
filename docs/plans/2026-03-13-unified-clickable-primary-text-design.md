# Unified Clickable Primary Text Design

**Date:** 2026-03-13

## Background

当前系统已经在多个页面引入“主文本可点击”的交互入口，例如：

- 后台用户页姓名
- 后台用户页项目范围抽屉中的项目名
- 项目成员页姓名
- 组织团队页姓名
- 项目列表页项目名

但这些入口目前仍然存在两种实现方式：

- 页面内局部 `button + 自定义 --link 样式`
- `el-link type="primary"`

这会导致 hover、focus、字重、颜色反馈和键盘可访问反馈不完全一致。

## Confirmed Decision

- 本轮统一到 `B` 范围：
  - `frontend/src/views/admin/AdminUsersPage.vue`
  - `frontend/src/views/project/ProjectMembersPage.vue`
  - `frontend/src/views/organization/OrganizationTeamPage.vue`
  - `frontend/src/views/project/ProjectListPage.vue`
- 采用一套全局公共样式规则，不新增组件
- 不改既有点击行为，只统一视觉与交互语义

## Recommended Approach

采用**全局样式工具类 + 页面最小替换**方案。

### Why this approach

- 比各页继续复制局部样式更容易长期维护
- 比抽成新组件更轻，适合当前仓库节奏
- 能统一 `button` 和 `link` 两类入口的 hover / focus / cursor / 主色反馈

## Visual Rule

定义一套统一的“主文本可点击”规则：

- 默认：主文本色、600 字重、无边框、无底色
- hover：主色 + 下划线
- focus-visible：统一描边，保证键盘可访问性
- cursor：统一使用 pointer
- secondary text 保持静态辅助信息，不参与点击语义

## Scope

### In Scope

- 在全局样式中新增统一类
- 将 4 个页面中的主文本点击入口迁移到这套类上
- 删除页面内重复的 `--link` 局部样式定义（若已被全局类覆盖）

### Out of Scope

- 全站所有交互文本统一
- 新增前端基础组件
- 改动点击目标或业务流程

## Target Pattern

建议使用类似以下组合：

- `workspace-link-text`
- `workspace-link-text--primary`

页面可以继续自由控制布局，但可点击主文本的视觉和交互应复用这两类。

## Success Criteria

- 4 个指定页面的主文本点击入口视觉一致
- hover / focus / underline / 主色反馈一致
- 不影响当前跳转或打开抽屉行为
- `vue-tsc` 继续通过
