# Navigation Semantics Unification Design

**Date:** 2026-03-13

## Background

当前系统已经把高频“主文本可点击入口”的视觉样式统一到了 `workspace-link-text workspace-link-text--primary`。但其中一批入口仍然使用 `<button>` 来做页面导航。

这些入口虽然在视觉上已经一致，但从交互语义上仍属于“导航”，更适合收敛到链接语义，而不是按钮语义。

## Confirmed Decision

- 本轮范围只覆盖会“跳页面”的主文本入口
- 采用最小收敛策略：
  - 保留已有视觉类
  - 将 `<button>` 导航入口改为导航语义实现
- 本轮纳入页面：
  - `frontend/src/views/admin/AdminUsersPage.vue`
  - `frontend/src/views/project/ProjectMembersPage.vue`
  - `frontend/src/views/organization/OrganizationTeamPage.vue`
  - `frontend/src/views/project/ProjectListPage.vue`
  - `frontend/src/views/bug/BugPage.vue`
  - `frontend/src/views/requirement/RequirementPage.vue`

## Recommended Approach

采用**导航语义收敛 + 保持共享视觉类**方案。

### Why this approach

- 这些入口的实际行为都是“进入另一个页面”
- 继续保留 `workspace-link-text` 可以避免重新做视觉规则
- 仅替换入口元素与绑定方式，不影响既有路由目标和页面交互

## UX Rule

### 1) What counts as navigation text

以下都属于导航语义入口：

- 打开项目概览的项目名
- 打开用户业务详情的姓名
- 打开 Bug / 需求详情的标题

这类入口应使用导航语义，而不是普通按钮语义。

### 2) What remains out of scope

以下仍不纳入本轮：

- 左侧模块筛选名（更接近上下文切换器，不是页面导航）
- 工具按钮、操作按钮、创建按钮、关闭按钮
- `ModulePage` 里的模块项选择器

## Scope

### In Scope

- 将上述 6 个页面中“跳页面”的主文本入口统一为导航语义
- 继续复用 `workspace-link-text workspace-link-text--primary`

### Out of Scope

- 非导航类文本入口
- 模块树/筛选器结构重构
- 新增公共组件

## Success Criteria

- 6 个页面中的主文本导航入口不再使用按钮语义实现页面跳转
- 视觉样式保持当前统一状态
- 既有跳转行为不变
- `vue-tsc` 通过
