# Second-Batch Clickable Entry Unification Design

**Date:** 2026-03-13

## Background

第一批“主文本可点击”统一已经覆盖：

- 后台用户页姓名
- 后台用户页项目范围抽屉中的项目名
- 项目成员页姓名
- 组织团队页姓名
- 项目列表页项目名

目前仍有两组高频工作台页面保留着旧的局部入口样式：

- `frontend/src/views/bug/BugPage.vue`
- `frontend/src/views/requirement/RequirementPage.vue`

它们仍然通过本地 `title-link` / `module-name` 样式来表达可点击主文本，与已经建立的 `workspace-link-text` 规则不一致。

## Confirmed Decision

- 本轮范围只收敛两页：
  - `BugPage`
  - `RequirementPage`
- 统一的入口只包含两类：
  - 标题主文本 `title-link`
  - 左侧模块名 `module-name`
- `ModulePage` 暂不纳入本轮，因为它更像列表项选择器，而不是标准“主文本跳转入口”

## Recommended Approach

采用**复用现有 `workspace-link-text workspace-link-text--primary` 公共类**的最小收敛方案。

### Why this approach

- 第一批已经验证这套公共样式能稳定工作
- `BugPage` 和 `RequirementPage` 主要问题是入口视觉不一致，不是交互目标错误
- 因此只需要替换入口元素和本地重复样式，不需要新组件或新数据流

## UX Strategy

### 1) Title entry

列表中的标题主文本是典型“进入详情页”入口：

- `BugPage`：点击 Bug 标题进入 Bug 详情
- `RequirementPage`：点击需求标题进入需求详情

这类入口应统一为：

- 使用共享点击主文本样式
- hover 为主色 + 下划线
- focus-visible 具备统一描边

### 2) Module-name entry

左侧模块名虽然不是跳页，而是筛选/切换上下文，但用户感知上仍然是“主文本点击入口”。

因此也建议统一使用同一套视觉反馈：

- 模块名文本可点击
- 激活态继续保留当前页面自己的状态高亮逻辑
- 共享基础 hover / focus 规则，减少各页重复样式差异

## Scope

### In Scope

- `frontend/src/views/bug/BugPage.vue`
- `frontend/src/views/requirement/RequirementPage.vue`
- 将上述两页的 `title-link` / `module-name` 收敛到共享规则
- 删除与共享规则重复的局部入口样式

### Out of Scope

- `ModulePage`
- 页面布局调整
- 跳转逻辑、筛选逻辑、数据逻辑调整
- 全站剩余其它交互点巡检整改

## Success Criteria

- `BugPage` 标题和模块名入口视觉统一到共享规则
- `RequirementPage` 标题和模块名入口视觉统一到共享规则
- 不改变现有点击行为
- `vue-tsc` 通过
