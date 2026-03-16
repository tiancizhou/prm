# PRM Department Module Design

**Date:** 2026-03-11

## Background

当前后台 `人员管理 > 部门` 页面只是结构壳子。系统里只有 `sys_user.department` 这个文本字段，并没有真正的部门实体、树形结构、CRUD 或用户归属关系，因此不能满足“真正实现部门”的要求。

## Confirmed Decisions

- 本轮实现真实部门模块，而不是继续使用文本字段模拟
- 第一版支持 **树形部门结构**
- 数据建模采用 **邻接表**：通过 `parent_id` 组织父子关系
- 新增独立 `sys_department` 表
- `sys_user` 增加 `department_id`
- 历史 `sys_user.department` 文本字段先保留，用于兼容与数据回填
- 后台页继续保留 `人员管理 > 部门 / 用户 / 权限`

## Goals

本轮要完成以下目标：

1. 建立真正的部门数据模型与树形结构
2. 提供后台部门树、部门详情、部门 CRUD
3. 支持用户与部门的结构化归属
4. 让后台 `部门` 页和 `用户` 页基于真实部门数据联动

## Data Model

### `sys_department`

建议字段：

- `id`
- `name`
- `parent_id`
- `sort_order`
- `status`
- `deleted`
- `created_by`
- `created_at`
- `updated_by`
- `updated_at`

### `sys_user`

新增字段：

- `department_id`

保留旧字段：

- `department`

说明：第一阶段通过 `department_id -> sys_department.name` 建立正式关系，但仍保留 `department` 文本字段作为兼容和兜底。

## Migration Strategy

迁移顺序：

1. 新增 `sys_department` 表
2. 给 `sys_user` 增加 `department_id`
3. 从历史 `sys_user.department` 文本中提取唯一部门名，生成顶级部门记录
4. 根据部门名回填所有用户的 `department_id`

第一版迁移后：

- 所有历史部门默认作为顶级节点
- 后续层级调整通过后台操作完成

## Backend APIs

### Department APIs

- `GET /api/admin/departments/tree`
  - 返回部门树
- `GET /api/admin/departments/{id}`
  - 返回部门详情、子部门、用户数量
- `POST /api/admin/departments`
  - 新增部门
- `PUT /api/admin/departments/{id}`
  - 更新部门
- `DELETE /api/admin/departments/{id}`
  - 删除部门（若有子部门或用户，拒绝删除）
- `GET /api/admin/departments/{id}/users`
  - 查询部门下用户列表

### User API Adjustments

- `GET /api/system/users`
  - 增加可选参数：`departmentId`
- `POST /api/system/users`
  - 支持 `departmentId`
- `PUT /api/system/users/{id}`
  - 支持 `departmentId`

## Frontend Integration

### Admin Department Page

- 左侧：真实部门树
- 右侧：部门详情表单 + 子部门列表
- 支持新增、编辑、删除当前部门

### Admin Users Page

- 左侧部门筛选改为真实部门树/列表
- 用户列表可按 `departmentId` 过滤
- 创建用户时使用真实部门选择器

### Admin Permissions Page

- 暂时保持现状，不依赖部门模型

## Risks and Mitigations

### Risk 1: Legacy department text and new department tree diverge

- **Risk:** 旧字段 `department` 与 `department_id` 可能短期不一致
- **Mitigation:** 服务层写入时同步更新两者，前端显示优先使用结构化部门名称

### Risk 2: Deleting a department may break user ownership

- **Risk:** 若部门下仍有用户或子部门，直接删除会导致数据悬空
- **Mitigation:** 删除前强校验，返回明确错误

### Risk 3: Tree operations may overcomplicate the first release

- **Risk:** 一开始就做拖拽、批量移动等高级能力会把范围做爆
- **Mitigation:** 第一版只做基本树、CRUD、用户归属，不做复杂树交互

## Expected Outcome

完成本轮后，后台 `部门` 页应成为真正可用的组织结构管理页，而不是展示壳子；`用户` 页则基于真实部门数据进行筛选和归属管理。

