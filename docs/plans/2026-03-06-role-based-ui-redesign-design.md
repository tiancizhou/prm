# Role-Based PRM UI Redesign Design

**Date:** 2026-03-06

> **Historical Note:** This document references an older role model (`SUPER_ADMIN / PM / DEV / QA / GUEST`). The current active role model has been simplified to `SUPER_ADMIN / PROJECT_ADMIN / DEV`.

## Background

当前 PRM 前端已具备稳定的 Vue 3 + Element Plus 应用壳层，包含登录、Dashboard、项目列表、项目概览、需求、Bug、模块、迭代、成员与用户管理等页面，但整体仍偏传统后台式信息呈现：

- 全局导航与项目内导航边界还不够清晰
- 首页更像通用统计页，而不是角色工作台
- 项目列表、需求页、Bug 页以表格为主，角色差异和上下文连续性不足
- 视觉层级虽已 token 化，但尚未形成鲜明、统一的产品工作台体验

本轮目标是为当前项目定义一套 **Web 端优先、按角色分层展示、信息架构与视觉体系一起重做** 的 UI 设计方案，并明确后续实施路径。

## Confirmed Product Decisions

本次设计依据已确认的用户输入：

- 信息架构与视觉体系一起重做，而不是只做皮肤替换
- 整体气质选择：**轻量现代的产品工作台**
- 首页和导航需根据不同角色展示不同重点信息
- 第一版覆盖现有角色体系：`SUPER_ADMIN / PM / DEV / QA / GUEST`
- 设备优先级：**Web 端优先**，桌面端为主要设计目标，移动端暂不纳入本轮范围

## Scope

### In Scope

- 全局信息架构重组（首页 / 项目中心 / 系统管理）
- 角色化首页策略与项目内工作区导航策略
- 全局视觉系统（字体、色彩、卡片、列表、状态、留白、动效原则）
- 关键页面模板与首批落地页面定义：
  - `frontend/src/layouts/MainLayout.vue`
  - `frontend/src/router/index.ts`
  - `frontend/src/styles/tokens.css`
  - `frontend/src/styles/base.css`
  - `frontend/src/views/login/LoginPage.vue`
  - `frontend/src/views/dashboard/DashboardPage.vue`
  - `frontend/src/views/project/ProjectListPage.vue`
  - `frontend/src/views/project/ProjectOverviewPage.vue`
  - `frontend/src/views/requirement/RequirementPage.vue`
  - `frontend/src/views/bug/BugPage.vue`

### Out of Scope

- 后端接口、数据库结构、权限模型重构
- 全量移动端适配设计
- 新增复杂前端基础设施（如新状态框架、全新组件库、Storybook 等）
- 本轮未优先页面的彻底重做（如二级编辑页、部分维护页可后续跟进）

## Design Vision

### Core Direction

PRM 的新界面应从“传统企业后台”转向“轻量现代产品工作台”。

设计目标不是追求炫技感，而是让不同角色一登录就知道：

1. 我是谁
2. 我现在最该处理什么
3. 当前有哪些风险或上下文需要注意

因此，新界面会强调：

- 更轻、更清晰的导航结构
- 更强的角色语义和工作流导向
- 更少但更有决策价值的指标信息
- 更像“工作面板”的列表与详情体验

### Memorable Differentiator

这套设计最应该被记住的一点是：

> **同一套 PRM，因角色不同而展示不同的首页重点与工作入口，但整体骨架仍高度统一。**

## Information Architecture

### Global-Level Navigation

全局层重新收敛为 3 个一级入口：

- **角色首页**：当前登录角色的个性化工作台
- **项目中心**：查找、切换、回到具体项目的统一入口
- **系统管理**：仅超级管理员可见，用于用户与权限类系统级操作

### Project-Level Navigation

进入项目后，导航聚焦为项目工作区：

- 项目概览
- 需求管理
- 缺陷管理
- 迭代管理
- 模块管理
- 成员管理

说明：

- 当前任务页保留在现有代码结构内，但本轮视觉策略将优先在首页、项目中心、概览、需求与 Bug 上建立统一模式；后续可按同一模板扩展到任务等页面
- 面包屑的重要性降低，页面标题、项目上下文条、最近访问与项目切换将承担更多定位职责

### Navigation Behavior

- 左侧导航变为“轻全局导航 + 当前项目工作区”
- 顶部区域承担 `项目切换`、`全局搜索`、`快捷新建`、`个人状态/主题` 等高频能力
- 用户在全局与项目内切换时，尽量不丢失最近项目与最近筛选上下文

## Role-Based Homepage Strategy

首页不再是统一统计板，而是统一骨架下的角色工作台。

### Shared Homepage Skeleton

所有角色首页共享 5 区结构：

1. 欢迎区 / 角色身份识别
2. 今天要做什么
3. 风险与提醒
4. 最近项目 / 最近活动
5. 快捷动作

### Role Priorities

#### SUPER_ADMIN

- 系统运行态势
- 用户与权限异常
- 全局项目风险
- 活跃项目排行

#### PM

- 项目推进度
- 需求待决策项
- 当前迭代风险
- 跨项目提醒

#### DEV

- 分配给我的工作
- 今日截止事项
- 阻塞与依赖
- 最近关联需求 / 缺陷

#### QA

- 待验证事项
- 活跃缺陷
- 回归范围
- 临近上线的质量风险

#### GUEST

- 我可见的项目
- 只读进展概览
- 最近动态
- 关键里程碑

## Visual System

### Tone

- 基调：轻量现代、可信、克制、有产品感
- 避免传统后台的厚重边框、满屏密表格、强装饰 KPI 板
- 默认以浅色主题为主舞台，暗色模式保留但不作为主展示方向

### Typography

- 标题：使用更有识别度的中文展示风格，强化产品感与层级
- 正文：使用高可读无衬线，适配长时间浏览任务、表格、表单
- 数字：统一字重与视觉节奏，用于指标、日期、优先级、计数

### Color Strategy

- 主品牌方向：清透蓝为主，辅以青绿色调，减少传统后台的生硬感
- 角色辅助色：
  - PM：偏蓝
  - DEV：偏青
  - QA：偏橙红
  - SUPER_ADMIN：偏深靛
  - GUEST：偏灰蓝
- 状态色整体降低饱和度，仅在风险、阻塞、超期等场景集中强调

### Spatial Principles

- 桌面端优先，重点覆盖 `1366 / 1440 / 1920`
- 页面采用“大留白 + 面板分区 + 局部高密度”的组合
- 卡片承担信息组织作用，但卡片之间应有明确节奏，而非等权平铺

### Component Treatment

- 卡片定义为“工作面板”而非普通白盒
- 列表页从“纯表格”升级为“可浏览、可聚焦、可快速处理”的面板化列表
- 统一搜索、筛选、状态摘要、快捷操作的视觉位置与交互反馈
- 动效仅做轻反馈：进入渐显、hover 抬升、标签与筛选过渡

## Reusable Page Templates

### Role Homepage Template

- 顶部欢迎区：角色身份、今日日期、简短目标说明
- 第一层：今日重点卡 / 风险卡
- 第二层：最近项目 / 最近活动
- 右上：快捷新建、快速跳转、只看我的等高频动作

### Project Overview Template

- 项目头部信息：名称、阶段、负责人、当前迭代、健康状态
- 关键指标条：仅保留最有决策价值的 4-6 个指标
- 主体分区：进度节奏、需求与缺陷摘要、团队与近期动态

### List Page Template

- 标题区
- 状态摘要条
- 搜索 / 筛选 / 视图切换工具条
- 主内容区（表格 / 分组列表 / 紧凑面板）
- 快速详情抽屉或批量操作区

### Detail Page Template

- 头部摘要（状态、优先级、负责人、所属项目/迭代）
- 主内容区（核心描述）
- 时间线 / 评论 / 操作记录
- 关联对象区

## Page-Specific Recommendations

### 1) Login Page

目标：从“登录表单页”升级为“产品欢迎入口页”。

设计要点：

- 左侧品牌区承担产品价值表达，而非纯装饰区
- 右侧表单更轻，主按钮更聚焦
- 增加简短角色入口提示，帮助用户理解登录后会看到什么

目标文件：

- `frontend/src/views/login/LoginPage.vue`
- `frontend/src/constants/login.ts`
- `frontend/src/constants/brand.ts`

### 2) Main Layout

目标：建立清晰的全局层 / 项目层切换骨架。

设计要点：

- 侧栏分离全局导航与项目工作区导航
- 顶栏升级为：项目切换、全局搜索、快捷新建、用户入口
- 当前项目从单一 pill 升级为更强的项目上下文条

目标文件：

- `frontend/src/layouts/MainLayout.vue`
- `frontend/src/router/index.ts`
- `frontend/src/constants/layout.ts`

### 3) Dashboard / Role Home

目标：从通用统计板升级为角色工作台。

设计要点：

- 保留少量组织级/项目级核心指标
- 依据角色切换首页卡片顺序、主 CTA 和风险优先级
- 用“今天要做什么”替代“纯展示型 KPI”

目标文件：

- `frontend/src/views/dashboard/DashboardPage.vue`
- `frontend/src/constants/dashboard.ts`

### 4) Project Center

目标：把当前项目列表页升级为真正的项目入口中心。

设计要点：

- 强化最近访问、我参与项目、风险项目、收藏项目
- 项目卡承载状态、当前迭代、风险、最近动态等关键信息
- 保留项目创建能力，但弱化其对主界面的打断感

目标文件：

- `frontend/src/views/project/ProjectListPage.vue`
- `frontend/src/constants/projectList.ts`

### 5) Project Overview

目标：把项目概览页升级为“项目驾驶舱”。

设计要点：

- 顶部展示项目上下文与健康状态
- 中部展示进度节奏、需求/Bug 摘要、团队动态
- 根据角色强调不同信息权重，但骨架一致

目标文件：

- `frontend/src/views/project/ProjectOverviewPage.vue`
- `frontend/src/constants/projectOverview.ts`

### 6) Requirement Page

目标：从重型表格页升级为工作面板式需求页。

设计要点：

- 页面顶部增加状态摘要条
- 筛选区支持更清晰的“只看我的 / 按模块 / 按状态 / 按优先级”组织
- 列表更强调标题、负责人、优先级、所属迭代和当前状态
- 提供快速详情与批量操作的落点

目标文件：

- `frontend/src/views/requirement/RequirementPage.vue`
- `frontend/src/constants/requirement.ts`

### 7) Bug Page

目标：将 Bug 页改造成更偏 QA / DEV 协作处理的缺陷工作台。

设计要点：

- 优先呈现严重程度、待验证、未解决、我负责等信息
- 视图切换支持“全部 / 未解决 / 待验证 / 高严重度”等聚焦方式
- 保留模块侧栏，但风格需与全局系统统一

目标文件：

- `frontend/src/views/bug/BugPage.vue`
- `frontend/src/constants/bug.ts`

## Rollout Strategy

建议按以下顺序实施，以减少一次性改动风险：

1. 先统一视觉 token 与全局基础样式
2. 再改 `MainLayout` 与路由层级表达
3. 再落地角色首页与项目中心
4. 再落地项目概览、需求页、Bug 页
5. 最后将相同模板扩散到模块、迭代、成员、用户等次级页面

## Risks and Mitigations

### Risk 1: Shell-level changes may affect many pages

- **Risk:** `MainLayout`、全局样式和 token 调整会波及大量现有页面
- **Mitigation:** 先沉淀共享 class 和 token，再分页面迁移；避免一次性替换所有局部样式

### Risk 2: Role-based homepage may need data not perfectly modeled today

- **Risk:** 不同角色首页理想状态下需要更细粒度的个体数据
- **Mitigation:** 第一版先基于现有接口与现有聚合数据做“轻角色化排序与聚焦”，必要时后续再补接口

### Risk 3: Requirement / Bug pages are large and style-divergent

- **Risk:** 这两个页面已有较多局部样式与复杂交互，重构时容易产生 UI 不一致
- **Mitigation:** 先抽象统一模板，再局部替换；优先保证信息层次和可读性，不在第一轮塞入过多新交互

## Figma / MCP Artifacts

本轮设计讨论已生成以下可视化参考：

- `PRM Role-Based IA`
- `PRM Visual System`
- `PRM Page Templates`
- `PRM Key Page Redesign`

这些图用于辅助校对信息架构、视觉系统和页面模板，不替代最终代码实现。

## Expected Outcome

完成本轮 UI 重构后，PRM 应达到以下状态：

- 登录后第一屏能明显体现当前角色关注重点
- 全局与项目层导航关系更清楚
- 项目中心成为高频、可用的真实入口，而不是普通列表页
- 需求与 Bug 页面从“后台表格”升级为“工作面板”
- 整体视觉语言统一为轻量现代产品工作台，并适配 `1366+` 桌面端使用场景
