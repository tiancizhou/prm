# Filter Text Semantics Unification Design

**Date:** 2026-03-13

## Background

当前 `BugPage` 与 `RequirementPage` 的左侧模块列表已经完成第一轮视觉统一，但模块名入口仍然混合着旧结构：

- 模块名本身承载筛选行为
- 展开箭头承载展开/收起行为
- 入口视觉已经部分统一，但语义层面仍然不够清楚

由于这些模块名并不是页面导航，而是“当前页内筛选器”，它们不应该继续沿用导航入口语义，也不适合混入“主文本跳详情”的统一规则中。

## Confirmed Decision

- 本轮只处理：
  - `frontend/src/views/bug/BugPage.vue`
  - `frontend/src/views/requirement/RequirementPage.vue`
- 模块名统一为：`文本按钮型筛选器`
- 不改为链接，不改为整行点击，不改为菜单组件

## Recommended Approach

采用**共享筛选型文本按钮样式 + 页面保留各自 active 逻辑**方案。

### Why this approach

- 模块名本质上是“筛选器切换”，不是导航
- 文本按钮型语义最贴近当前用户心智
- 能和导航型 `workspace-link-text` 保持同一设计系统下的区分关系

## UX Rule

### 1) Semantic split

- 页面导航主文本：继续使用导航型规则
- 当前页筛选主文本：使用筛选型规则

两者需要统一在系统设计语言下，但不能混用同一语义。

### 2) Filter-text behavior

模块名按钮应具备：

- hover：主色反馈
- focus-visible：统一描边
- active：由页面自身 active 状态控制
- 文本超长：继续省略号截断

### 3) Interaction boundaries

- 展开箭头继续只负责展开/收起
- 模块名继续只负责切换筛选
- 不把整个列表项都变成一个大按钮，避免与箭头点击冲突

## Scope

### In Scope

- 在全局样式中新增筛选型文本按钮公共类
- 迁移 `BugPage` 和 `RequirementPage` 的模块名入口

### Out of Scope

- `ModulePage`
- 导航型入口样式调整
- 左侧树/列表结构大改

## Success Criteria

- `BugPage` 和 `RequirementPage` 的模块名入口结构一致
- 两页模块名都以筛选器语义呈现，而不是导航语义
- 激活态逻辑不变
- `vue-tsc` 通过
