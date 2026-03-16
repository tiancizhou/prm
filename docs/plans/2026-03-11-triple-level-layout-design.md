# PRM Triple-Level Linked Layout Design

**Date:** 2026-03-11

## Background

当前 PRM 已具备稳定的 `Vue 3 + Element Plus` 应用壳层与项目级页面体系，但现有布局仍保留传统后台的典型问题：

- 左侧菜单同时承载全局导航与项目内导航，层级边界不清晰
- 进入具体项目后，左侧菜单被项目菜单替换，跨项目切换路径过深
- 顶部面包屑单独占一行，但定位价值有限，挤占垂直空间
- 页面背景与卡片层级过于接近，Dashboard 缺少“纸张感”和重点引导
- 项目概览页的信息组织偏静态展示，不足以支撑高频项目管理操作

本轮目标是在不改后端接口、不重写前端技术栈的前提下，为现有 PRM 设计一套更适合项目管理工作流的 **三级联动现代化布局方案**，并优先适配当前的路由与页面结构。

## Confirmed Decisions

本次方案基于已确认输入：

- 产品气质选择：**偏 Jira 的专业、高密度、效率优先**
- 交付方向选择：**A2，可落地到当前 `Vue 3 + Element Plus` 系统**
- 设备优先级：**Web 桌面端优先**
- 本轮重点：重构壳层、项目切换、Header、项目概览页与共享视觉规则
- 约束条件：尽量复用现有路由结构 `projects/:id/*` 与现有页面组件

## Design Goals

本轮布局重构需要同时解决以下三个问题：

1. **降低跨项目切换成本**
   - 用户在任何项目功能页中，都能直接切换到另一个项目，而不是先回到项目列表页。

2. **强化页面层级与工作感**
   - 用清晰的全局层 / 项目层 / 页面层结构，替代当前相对混合的导航体验。

3. **提升概览页的视觉引导与协同感**
   - 用更轻但更明确的“纸张感”卡片、趋势表达和动态侧栏，提升 Dashboard/Overview 的可读性与决策效率。

## In Scope

- `frontend/src/layouts/MainLayout.vue`
- `frontend/src/router/index.ts`
- `frontend/src/constants/layout.ts`
- `frontend/src/styles/tokens.css`
- `frontend/src/styles/base.css`
- `frontend/src/views/project/ProjectOverviewPage.vue`
- 与壳层联动的项目页标题、导航、上下文结构

## Out of Scope

- 后端 API、数据库结构、权限模型调整
- 新增复杂图表基础设施
- 移动端专门适配
- 文档、组织模块的完整业务页落地（本轮先预留入口）

## Layout Architecture

### Core Structure

推荐采用 **双侧栏 + 压缩头部 + 双栏内容区** 的三级联动结构：

1. `Level 1`：最左全局窄侧边导航
2. `Level 2`：次左项目内侧边菜单 + Project Selector
3. `Header`：单行整合标题、弱化路径、搜索、通知、用户入口
4. `Main Content`：浅灰背景上的白色工作卡片，项目概览页以 `7:3` 双栏为主模板

### Why This Structure

该结构最适合当前 PRM 的原因：

- 它能彻底分离“系统级入口”和“项目级工作区”
- 它与当前 `projects/:id/*` 路由天然契合，改造成本可控
- 它允许 Project Selector 成为稳定的高频切换入口
- 它保持 Jira 风格的信息效率，同时能引入更现代的视觉层次

## Level 1: Global Navigation Rail

### Purpose

`Level 1` 只解决“我当前在哪个产品域”这个问题，不承载项目内工作流导航。

### Layout Rules

- 宽度：`64px`
- 表达方式：仅图标 + Tooltip
- 背景：深色或中性深灰，形成与工作区的第一层分隔
- 底部固定保留收起/展开按钮

### Navigation Items

- 工作台
- 项目集
- 文档
- 组织
- 后台设置

### Behavior

- 点击 `工作台`：进入 `/dashboard`
- 点击 `项目集`：进入 `/projects` 或保持当前项目域
- 点击 `后台设置`：进入 `/system/users`（按权限显示）
- `文档`、`组织` 在第一轮先作为预留入口，不必立即落地完整页面

### Implementation Notes

- 当前 `MainLayout` 中的全局菜单需要从项目菜单中剥离
- `Level 1` 必须始终稳定存在，不因是否进入项目而改变结构

## Level 2: Project Workspace Sidebar

### Purpose

`Level 2` 只解决“我在当前项目里要做什么”的问题，是项目工作区的固定导航面板。

### Visibility

- 当路由命中 `projects/:id/*` 时显示
- 非项目域页面（如 `/dashboard`、`/projects`、`/system/users`）默认隐藏

### Layout Rules

- 宽度：`240px`
- 顶部固定：`Project Selector`
- 下方为项目内功能导航列表
- 二级栏保持独立滚动，不与主内容滚动混在一起

### Menu Items

与现有页面保持一致：

- 概览 → `/projects/:id/overview`
- 需求 → `/projects/:id/requirements`
- Bug → `/projects/:id/bugs`
- 迭代 → `/projects/:id/sprints`
- 模块 → `/projects/:id/modules`
- 成员 → `/projects/:id/members`

当前 `任务` 页面暂不作为本轮二级导航主入口，避免菜单过长；后续可根据使用频率补入。

## Project Selector Design

### Core Requirement

用户必须能够在不离开当前功能页的情况下快速切换项目。

### Placement

- 固定在 `Level 2` 顶部
- 不随主内容滚动
- 视觉上比普通菜单项更强，但不应像大标题一样占据过多高度

### Selector Content

建议显示：

- 当前项目名称
- 项目简称或编码
- 状态标识（进行中 / 风险 / 已归档）
- 下拉箭头

### Dropdown Structure

建议下拉面板包含：

- 最近访问
- 我参与的项目
- 已收藏项目
- 全部项目（带搜索）

### Search Support

支持通过以下方式过滤：

- 项目名称
- 项目编码
- 拼音首字母（如果已有相关能力）

### Switching Rules

这是本轮方案中最重要的交互约定：

- 从 `/projects/12/bugs` 切换到项目 `38` 时，优先跳到 `/projects/38/bugs`
- 从 `/projects/12/requirements` 切换到项目 `38` 时，优先跳到 `/projects/38/requirements`
- 如果目标项目无对应权限或模块不可用，则降级到 `/projects/38/overview`
- 如果当前页为详情或编辑页，如 `/projects/12/requirements/99`，切换项目后回退到对应列表页 `/projects/38/requirements`

### Route Semantics

建议在路由 `meta` 中增加以下字段，供 `MainLayout` 使用：

- `navLevel1`: 当前所属一级域，例如 `dashboard` / `projects` / `system`
- `projectSection`: 当前项目域功能，例如 `overview` / `requirements` / `bugs`
- `projectSwitchMode`: `preserve-section` 或 `fallback-list`
- `pageTitle`: Header 主标题来源
- `pagePathLabel`: Header 弱路径来源

## Header Redesign

### Goal

将当前“独立面包屑行 + 项目 pill + 用户菜单”的头部，改造成 **一条真正承担定位与操作的工作条**。

### Layout Rules

- 高度：`56px`
- 单行布局
- 左侧：弱路径 + 强标题
- 中部：全局搜索
- 右侧：快捷新建、通知、用户入口

### Left Area

建议采用双层文本：

- 上层：弱路径，如 `项目集 / 云学堂`
- 下层：强标题，如 `项目概览`

视觉上，标题应显著强于路径文本，避免面包屑喧宾夺主。

### Breadcrumb Strategy

不建议完全移除定位信息，但要弱化传统 `el-breadcrumb` 的存在感：

- 不再独占一整行
- 不再成为头部主视觉
- 仅作为标题上方或侧旁的辅助路径文本

### Center Area: Global Search

- 建议宽度：`320px - 420px`
- 占位文案：`搜索项目、需求、Bug`
- 第一阶段即使只做壳层占位，也应预留稳定位置

### Right Area

推荐顺序：

- 快捷新建
- 通知
- 用户菜单

主题切换若保留，建议收入用户菜单而不是常驻主位。

### Title Hierarchy

- 路径文本：`12px - 13px`，次级灰色
- 页面标题：`24px`，`600` 字重
- 整体节奏应更像工作台，而不是传统 ERP 顶栏

## Main Content Design

### Paper-Like Workspace

内容区需要显著强化“工作纸张感”：

- 页面背景：`#F5F7FA`
- 卡片背景：`#FFFFFF`
- 卡片边框：`1px solid #E6EAF0`
- 阴影：轻量低扩散，如 `0 1px 2px rgba(16,24,40,0.04), 0 8px 24px rgba(16,24,40,0.06)`

目标不是形成夸张悬浮，而是让白色卡片从浅灰底中自然抬起。

### Spacing Rules

- 页面内容内边距：`24px`
- 卡片之间间距：`16px`
- 区块之间间距：`24px`
- 桌面端优先使用更宽的横向空间，不做过窄内容区限制

## Dashboard / Project Overview Card Strategy

### Summary Cards

当前纯数字卡片需要升级为更有“势能”的状态卡。建议采用三段式结构：

1. 指标名称 + 趋势标签
2. 主数字
3. 微型趋势或进度表达

### Suggested Card Types

- 进行中：折线趋势或轻量走势提示
- 延期：风险色 + 变化标签
- 已完成：进度条 / 完成率
- 待处理：小型分布条或积压说明

### Visual Rules

- 卡片主体保持白底，避免每张卡都改成高饱和背景块
- 用小图标、状态点、趋势色形成差异
- 风险色只用于风险指标，避免全页彩色过载

## Project Overview 7:3 Layout

### Left Column (7)

左侧承载核心看板，建议分三层：

1. 关键指标卡
2. 项目健康 / 进度 / 风险摘要
3. 我的待办 / 重点事项

阅读顺序应当是：

- 先看状态
- 再看原因
- 再看行动

### Right Column (3)

右侧承载协作与时序信息，建议包括：

- 动态 / 操作历史
- 风险提醒 / 即将到期
- 我关注的更新（可选）

### Activity Panel

建议使用时间线，而不是表格：

- 操作人
- 动作
- 对象
- 时间

示例：`王敏 更新了需求 RQ-103 的优先级 · 10 分钟前`

这样能显著增强“团队正在协同工作”的感受。

## Alignment with Current Codebase

### Existing Route Fit

当前 `frontend/src/router/index.ts` 已具备稳定的项目级路由模式：

- `/projects/:id/overview`
- `/projects/:id/requirements`
- `/projects/:id/modules`
- `/projects/:id/bugs`
- `/projects/:id/sprints`
- `/projects/:id/members`

因此本轮推荐 **保留路径结构，仅补充路由元信息与壳层解释逻辑**。

### MainLayout Refactor Direction

当前 `frontend/src/layouts/MainLayout.vue` 将全局导航与当前项目菜单混放在同一个左侧栏中。改造目标应是：

- 左一：全局窄导航
- 左二：项目工作区侧栏（条件显示）
- 顶部：压缩 Header
- 中间：`router-view` 内容区域

这会是本轮壳层升级的主战场。

## Rollout Strategy

建议按以下顺序落地：

1. 先补 `router meta` 与 `layout constants`
2. 重构 `MainLayout` 为双侧栏结构
3. 加入 `Project Selector` 与跨项目同功能页切换逻辑
4. 压缩 Header，并引入全局搜索 / 快捷新建占位
5. 统一 `tokens.css` / `base.css` 的纸张感与卡片层级
6. 首批改造 `ProjectOverviewPage.vue`
7. 再将同样的页面壳层模式扩展到 `RequirementPage.vue` 与 `BugPage.vue`

## Risks and Mitigations

### Risk 1: Shell changes affect many pages

- **Risk:** `MainLayout` 改造将影响所有受保护页面
- **Mitigation:** 优先保持路由路径不变，先完成壳层与样式 token，再迁移页面内部结构

### Risk 2: Project switch behavior may be inconsistent across detail routes

- **Risk:** 详情页、创建页、编辑页切换项目时容易出现错误跳转
- **Mitigation:** 用 `projectSwitchMode` 显式区分“保留功能页”与“回退列表页”

### Risk 3: Overview redesign may outpace current data availability

- **Risk:** 某些理想态的趋势信息当前可能没有现成接口
- **Mitigation:** 第一版优先用状态卡、进度条、轻量趋势标签建立结构，再逐步补图表深度

## MCP Artifact

本轮方案已生成一份简单的布局草图，用于辅助团队理解三级联动结构：

- `PRM Triple-Level Layout Draft`
- Link: `https://www.figma.com/online-whiteboard/create-diagram/d3932531-3993-4700-b9f3-bfea7aaee533?utm_source=other&utm_content=edit_in_figjam&oai_id=&request_id=7a870a4f-410c-41da-8f96-b3e4523eccf8`

## Expected Outcome

完成本轮布局升级后，PRM 应达到以下效果：

- 用户在任何项目功能页中都可直接切换项目
- 全局导航与项目工作区导航职责清晰分离
- Header 高度压缩，但页面标题更突出、操作更集中
- 项目概览页更具“驾驶舱”与协作感，而不是静态数字板
- 整体界面从传统后台，升级为更适合项目管理的高密度现代工作台
