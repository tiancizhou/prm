# PRM Company Hierarchy Design

**Date:** 2026-03-12

## Background

当前系统已经具备真实的部门树与人员管理能力，但“公司”仍然只是组织域中的占位入口。现在需要把“公司”从展示概念升级为真实的顶层组织节点：

- `组织 > 公司` 负责维护公司信息
- 公司作为部门树的最顶层
- `后台 > 人员管理 > 部门` 左侧树要先显示公司，再显示部门层级
- `后台 > 人员管理 > 用户` 的部门树也要兼容公司根节点

## Confirmed Decision

- 采用 **C 方案**：这次先按“单公司”体验落地，但底层模型预留“多公司”扩展能力。
- 当前 UI 只维护一个“当前公司”，后续如果要支持多公司，不需要推翻数据模型。

## Goals

1. 新增真实公司模型，支持维护公司名称与基础资料
2. 让公司成为部门树的顶层根节点
3. 保持 `组织` 与 `后台` 页面内容层 UI 风格一致
4. 不破坏现有用户、部门、权限页面的可用性

## Data Model

### `sys_company`

新增公司表，第一版保留以下字段：

- `id`
- `name`
- `short_name`
- `contact_name`
- `phone`
- `email`
- `address`
- `description`
- `status`
- `deleted`
- `created_by`
- `created_at`
- `updated_by`
- `updated_at`

### `sys_department`

新增：

- `company_id`

说明：

- 顶级部门通过 `company_id + parent_id = null` 归属到公司
- 子部门继续通过 `parent_id` 建模
- 这样既满足“公司 > 部门 > 子部门”的树结构，也兼容未来多公司场景

## Tree Strategy

部门树接口继续返回树形结构，但节点扩展为两类：

- `COMPANY`
- `DEPARTMENT`

树节点需要补充：

- `nodeKey`：前端树的稳定唯一键，例如 `company-1`、`department-12`
- `nodeType`
- `companyId`

这样可以避免公司 ID 与部门 ID 冲突，同时让前端清楚识别当前选中的是公司还是部门。

## Backend APIs

### Company API

- `GET /api/organization/company`
  - 获取当前公司资料
- `PUT /api/organization/company`
  - 更新当前公司资料

### Department API Adjustments

- `GET /api/admin/departments/tree`
  - 返回“公司根节点 + 部门树”
- `POST /api/admin/departments`
  - 支持 `companyId`
- `PUT /api/admin/departments/{id}`
  - 支持 `companyId`

## Frontend Integration

### `组织 > 公司`

- 左侧显示公司作为顶层的组织预览
- 右侧提供公司资料表单
- 至少支持维护公司名称，并预留简称、联系人、电话、邮箱、地址、简介字段

### `后台 > 人员管理 > 部门`

- 左侧树根节点改为公司
- 选中公司时，右侧直接维护直属部门列表
- 选中部门时，右侧维护该部门的子部门列表
- 公司节点不在后台部门页中编辑/删除，公司信息统一回到 `组织 > 公司`

### `后台 > 人员管理 > 用户`

- 左侧树兼容公司根节点
- 选中公司时展示全部用户
- 用户创建/编辑时，部门下拉只展示真实部门，不展示公司根节点

## Migration Strategy

新增一条新的 Flyway migration，而不是改写旧的 `V18`：

1. 创建 `sys_company`
2. 插入一个默认公司
3. 为 `sys_department` 增加 `company_id`
4. 将现有部门全部回填到默认公司
5. 调整部门唯一索引为 `company_id + name + parent_id + deleted`

## Risks and Mitigations

### Risk 1: 前端仍把所有树节点当成部门

- **Mitigation:** 给树节点增加 `nodeType` 和 `nodeKey`，前端统一改用这两个字段驱动行为。

### Risk 2: 顶级部门缺少 `companyId`

- **Mitigation:** 服务层在创建/更新时做归属校验；若未显式传入，回退到当前公司。

### Risk 3: 公司入口与后台部门页职责重叠

- **Mitigation:** 明确职责边界：公司资料只在 `组织 > 公司` 维护；后台部门页只维护部门。

## Expected Outcome

完成后，系统组织结构会变成稳定的一致模型：

- `公司` 是最顶层
- `部门` 在公司下面维护
- `组织 > 公司` 维护公司资料
- `后台 > 人员管理` 继续负责部门、用户、权限，但树结构与公司顶层保持一致
