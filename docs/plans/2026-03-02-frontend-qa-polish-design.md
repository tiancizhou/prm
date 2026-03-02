# Frontend UI QA Polish Design

**Date:** 2026-03-02

## Background

当前前端已完成结构统一与主题 token 化，但 `frontend/UI_QA_CHECKLIST.md` 中仍有以下未完成项：

- 1920 / 1440 / 1366 三档手工响应式检查
- 暗色模式下表格/表单/弹窗/交互态/图表可读性检查

本轮目标是仅通过样式与布局调整完成上述检查项，不修改任何业务逻辑、接口调用或数据结构。

## Scope

### In Scope

- 全局布局比例与间距微调（侧栏/头部/内容区）
- 页面级断点收口（Dashboard / ProjectOverview / Login）
- 全局筛选条与 Header 操作区在 1366/1440 的排版稳定性
- 暗色模式下：表格、表单输入、弹窗、hover/focus、图表轴线与网格对比优化
- 更新 `frontend/UI_QA_CHECKLIST.md` 勾选状态

### Out of Scope

- 业务逻辑、状态管理、接口参数/返回结构调整
- 新功能（新页面、新组件能力、可视化重构）
- 引入新测试框架或新增复杂自动化基建

## Design Decisions

## 1) Responsive polish by breakpoint

- 在 `frontend/src/layouts/MainLayout.vue` 调整 1920/1440/1365 下 header 与 content 的内边距比例，保证整体视觉层级在大屏和中屏下更平衡。
- 在 `frontend/src/styles/base.css` 增强 `.page-header` / `.page-actions` / `.filter-bar` 的弹性换行与最小可点击尺寸，降低窄宽下挤压导致的错位。
- 在 `frontend/src/views/dashboard/DashboardPage.vue`：
  - 1920 下约束 4 指标卡最小宽度与内部排版，避免“过宽但不聚焦”。
  - 1365 下将 insight 区域保持单列稳定。
- 在 `frontend/src/views/project/ProjectOverviewPage.vue`：
  - 1440 保持 KPI 双列。
  - 1365 强化头部 meta 信息换行/分组可读性与图例换行。
- 在 `frontend/src/views/login/LoginPage.vue`：
  - 1440 调整 hero:form 比例，避免表单侧压缩感与视觉失衡。

## 2) Dark theme readability polish

- 在 `frontend/src/styles/tokens.css` 增补暗色模式下图表/控件可读性 token（边框、输入、焦点环、弹窗遮罩层次）。
- 在 `frontend/src/styles/base.css` 增补暗色模式下 Element Plus 常用控件统一规则：
  - `el-input` / `el-select` / `el-textarea` 对比
  - `el-dialog` / `el-drawer` 标题与正文可读性
  - `hover` 与 `focus-visible` 可见性
  - 表格行 hover 与表头层次
- 在 `frontend/src/views/project/ProjectOverviewPage.vue` 细调图表轴线/网格文本对比，保证暗色下趋势可辨识。

## 3) Verification strategy

- 静态核验：沿用现有 QA 命令（inline style、token 化、type-check）
- 手工核验：按 checklist 的 1920/1440/1366 与暗色模式项逐条确认
- 文档回填：完成后将 `frontend/UI_QA_CHECKLIST.md` 未勾项全部置为 `[x]`

## Risk and mitigation

- 风险：全局规则改动可能影响部分页面按钮/筛选条密度。
  - 缓解：仅在断点条件下覆盖，优先最小差异补丁。
- 风险：暗色规则过强导致局部组件对比过高。
  - 缓解：优先使用 token 并维持与既有色阶一致，不引入突兀新色。

## Expected outcome

- `frontend/UI_QA_CHECKLIST.md` 当前全部未勾项完成
- 页面在 1920/1440/1366 下布局更稳，header actions 与 filter bar 可点且不乱
- 暗色下表格、表单、弹窗、hover/focus、图表可读性通过手工检查
