# PRM（项目管理平台）设计文档

**日期：** 2026-02-26  
**目标：** 一次性快速上线可用的中小团队项目管理平台，覆盖项目→需求→任务→Bug→迭代/发布→看板闭环。  
**约束：** 长期保持单体架构，不做微服务拆分。

---

## 阶段说明（2026-03-09）

以下内容主要描述 PRM 的目标态设计，不完全等同于当前第一阶段已启用能力。

当前第一阶段实现口径以 `docs/plans/2026-03-09-business-alignment-design.md` 为准，特别是：

- Bug 主流程按 `ACTIVE -> RESOLVED -> CLOSED` 收敛，旧状态仅兼容展示
- Requirement 当前主流程按 `DRAFT -> IN_PROGRESS -> DONE` 收敛，评审流保留为后续阶段能力
- Release 当前仅视为版本记录能力，未启用 `pm_release_item` 发布闭环
- `pm_sprint_item`、`pm_requirement_link` 仍属于未启用的跨模块关联能力
- `pm_operation_log` 当前已覆盖核心写操作的基础审计接线，但字段级 before/after 差异审计仍属后续增强项

---

## 1. 总体架构与模块边界

### 1.1 架构形态
- 采用模块化单体架构，统一进程、统一数据库、统一部署。
- 模块之间通过应用服务编排，不进行跨模块 Mapper 直接访问。

### 1.2 分层规则
- `Controller`：REST API 入参校验、鉴权入口。
- `Application Service`：用例编排、事务边界控制。
- `Domain Service`：状态机、领域规则、流转校验。
- `Repository/Mapper`：数据访问。
- `Infra`：缓存、消息、对象存储、日志、调度等基础设施。

### 1.3 技术栈
- 后端：`Spring Boot 3.x`、`Spring Web`、`Spring Validation`
- 安全：`Spring Security + JWT`
- ORM：`MyBatis-Plus`
- 数据库：`SQLite`（首发）
- 缓存：`Redis`
- 消息：`RabbitMQ`
- 调度：`Spring Scheduler`
- 对象存储：`MinIO`
- 文档：`SpringDoc OpenAPI`

### 1.4 业务模块（MVP 全覆盖）
- `auth`：登录、刷新、注销、鉴权
- `org`：用户、角色、团队
- `project`：项目生命周期、项目成员
- `requirement`：需求与评审
- `task`：任务与工时
- `bug`：缺陷生命周期
- `sprint`：迭代规划与关闭
- `release`：版本发布记录
- `dashboard`：统计看板与聚合
- `notify`：异步通知
- `audit`：操作日志与审计

---

## 2. 核心数据模型与状态机

### 2.1 主键与删除策略（已锁定）
- 所有业务表使用递增主键 ID。
- `SQLite`：`INTEGER PRIMARY KEY AUTOINCREMENT`，严格不复用旧 ID。
- `MySQL`：`BIGINT UNSIGNED AUTO_INCREMENT`。
- 全业务采用逻辑删除，禁止物理删除与 `TRUNCATE`。

### 2.2 核心表（24 张）
- `sys_user`
- `sys_role`
- `sys_permission`
- `sys_role_permission`
- `sys_user_role`
- `sys_team`
- `sys_team_member`
- `pm_project`
- `pm_project_member`
- `pm_requirement`
- `pm_requirement_review`
- `pm_requirement_link`
- `pm_task`
- `pm_task_dependency`
- `pm_task_worklog`
- `pm_bug`
- `pm_bug_comment`
- `pm_sprint`
- `pm_sprint_item`
- `pm_release`
- `pm_release_item`
- `pm_attachment`
- `pm_operation_log`
- `pm_dashboard_snapshot`

### 2.3 通用字段规范
- `id`、`status`、`deleted`
- `created_by`、`created_at`、`updated_by`、`updated_at`
- 涉及项目隔离的表统一含 `project_id`
- 时间统一使用 UTC 存储

### 2.4 关键关系
- 需求 → 任务：一对多
- 需求 ↔ Bug：多对多（`pm_requirement_link`，目标态设计，第一阶段未启用正式闭环）
- Sprint ↔ 需求/任务：多对多（`pm_sprint_item`，目标态设计，第一阶段未启用）
- Release ↔ 需求/任务/Bug：多对多（`pm_release_item`，目标态设计，第一阶段未启用）
- 附件通过 `biz_type + biz_id` 关联业务实体

### 2.5 状态机
- 第一阶段当前启用：
  - 需求：`DRAFT -> IN_PROGRESS -> DONE`
  - 任务：`TODO -> IN_PROGRESS -> PENDING_REVIEW -> DONE -> CLOSED`
  - Bug：`ACTIVE -> RESOLVED -> CLOSED`（支持重开）
  - Sprint：`PLANNING -> ACTIVE -> CLOSED`
- 目标态扩展：
  - 需求评审相关状态（如 `REVIEWING / APPROVED / CLOSED`）保留为后续阶段能力或兼容态，不作为第一阶段主流程
  - Bug 历史状态（如 `NEW / CONFIRMED / ASSIGNED / VERIFIED`）仅用于兼容历史数据展示，不再作为第一阶段主流程状态
- 每次流转必须落 `pm_operation_log`

---

## 3. API 与权限模型

### 3.1 API 分组
- 认证：`/api/auth/login`、`/api/auth/refresh`、`/api/auth/logout`
- 项目：`/api/projects`、`/api/projects/{id}/members`
- 需求（第一阶段主用）：`/api/requirements`、`/api/requirements/{id}/status`、`/api/requirements/{id}/logs`
- 需求评审（保留接口，非第一阶段默认流程）：`/api/requirements/{id}/review`
- 任务：`/api/tasks`、`/api/tasks/{id}/assign`、`/api/tasks/{id}/worklog`、`/api/tasks/{id}/status`
- Bug（第一阶段主用）：`/api/bugs`、`/api/bugs/{id}`、`/api/bugs/{id}/status`、`/api/bugs/{id}/assign`、`/api/bugs/{id}/comments`、`/api/bugs/{id}/convert-to-requirement`
- 迭代/发布：`/api/sprints`、`/api/sprints/{id}/plan`、`/api/sprints/{id}/close`、`/api/releases`
- 看板（第一阶段主用）：`/api/dashboard/overview`、`/api/dashboard/aggregate`
- 看板趋势类接口（如 `bug-trend / burndown / team-efficiency`）属于目标态设计，第一阶段未启用

### 3.2 权限模型（基础 RBAC）
- 系统角色：`SUPER_ADMIN`、`PROJECT_ADMIN`、`DEV`
- 接口权限：`@PreAuthorize` + JWT
- 数据范围：首版仅校验“是否项目成员”，不做字段级权限

---

## 4. 看板统计口径

### 4.1 项目总览
- 进行中项目数
- 延期项目数
- 完成率

### 4.2 需求看板
- 新增需求数
- 需求完成率
- 平均流转周期

### 4.3 任务看板
- 燃尽趋势
- 逾期任务数
- 成员负载

### 4.4 Bug 看板
- 新增/关闭趋势
- 未关闭按严重度分布
- 平均修复时长

### 4.5 聚合策略
- 每日定时聚合写入 `pm_dashboard_snapshot`
- 看板优先读快照，必要时回源

---

## 5. 非功能与上线约束

- 所有状态流转与操作日志同事务提交。
- 列表接口分页默认 20，最大 100。
- 统一错误码与统一响应结构。
- 关键动作幂等（关闭 Sprint、发布等）。
- 密码哈希使用 `BCrypt`，登录失败限流。
- 文件上传类型与大小白名单。
- 全链路 `traceId`。

---

## 6. SQLite 到 MySQL 迁移策略

- 从首版开始使用 `Flyway` 管理 schema。
- DDL 保持 SQLite/MySQL 可兼容风格，避免厂商专有能力依赖。
- 迁移步骤：全量导出 -> MySQL 导入 -> 校验 -> 切换。
- 切换时显式设置 `AUTO_INCREMENT` 起点为历史最大 ID 之后。
- 保证主键不复用与外键完整性。

