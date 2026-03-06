# Sub-Requirement Assignee Default Design

**Date:** 2026-03-06

## Background

当前 PRM 在“创建子需求”场景下存在两条入口路径：

- 需求列表页中，展开父需求后，通过弹窗创建子需求：`frontend/src/views/requirement/RequirementPage.vue`
- 需求详情页中，通过跳转进入独立创建页创建子需求：`frontend/src/views/requirement/RequirementDetailPage.vue` → `frontend/src/views/requirement/RequirementCreatePage.vue`

当前这两条路径对“负责人默认值”的处理不一致：

- 列表页弹窗路径在 `openSubReqCreate()` 中会预填 `assigneeId = parentReq?.assigneeId ?? null`
- 详情页跳转路径仅带 `parentId` 到创建页，创建页不会进一步读取父需求负责人，因此默认负责人为空

这导致用户在“创建子需求”时体验不一致，也不符合“默认继承父需求负责人，但允许独立调整”的目标。

## Confirmed Product Decision

本次优化目标已经确认：

- 创建子需求时，**默认带出父需求负责人**
- 子需求负责人 **必须允许与父需求分开**
- 默认带出只是初始化行为，不是强绑定规则
- 用户手动修改后，以用户当前选择为准

## Scope

### In Scope

- 统一子需求创建两条入口的负责人默认行为
- 在创建子需求时增加轻量上下文与提示文案
- 明确“默认继承但可修改”的交互认知
- 补齐边界处理：父需求无负责人、父负责人不可用、父需求加载失败

### Out of Scope

- 后端接口、数据库、权限规则调整
- 将模块、迭代、优先级等字段一并默认继承
- 重构整个需求创建表单布局
- 编辑子需求时自动重新同步父需求负责人

## Current Behavior Analysis

### Path 1: Requirement List Dialog

在 `frontend/src/views/requirement/RequirementPage.vue` 中：

- `openSubReqCreate(parentReq)` 会将 `form.parentId` 设为父需求 ID
- 同时将 `form.assigneeId` 设为 `parentReq?.assigneeId ?? null`

这条路径已经具备“默认带出父负责人”的基础行为。

### Path 2: Requirement Detail → Create Page

在 `frontend/src/views/requirement/RequirementDetailPage.vue` 中：

- `goCreateSub()` 跳转到：
  - `/projects/${projectId}/requirements/create?parentId=${reqId}`

在 `frontend/src/views/requirement/RequirementCreatePage.vue` 中：

- 页面只从 query 中读取 `parentId`
- 页面不会进一步 `requirementApi.get(parentId)` 拉取父需求详情
- 因此 `form.assigneeId` 仍初始化为 `null`

这就是当前行为不一致的根因。

## Design Goal

统一两条“创建子需求”入口的默认行为，让用户在任何入口下都得到同样的结果：

1. 系统自动带出父需求负责人
2. 用户可以立刻改成其他负责人
3. 系统明确告知“这是默认值，不是绑定关系”

## Interaction Design

### Default Rule

当创建行为处于“子需求模式”（即 `parentId` 存在）时：

- 若父需求有负责人，则子需求 `assigneeId` 默认取父需求负责人
- 若父需求无负责人，则子需求负责人保持空
- 若父负责人不在当前项目成员列表中，则子需求负责人保持空

### Editing Rule

- 负责人字段始终保持可编辑
- 用户一旦手动修改负责人，系统不再自动覆盖当前选择
- 提交时，以当前表单中的 `assigneeId` 为准

### Parent Context Hint

在“子需求模式”下，增加轻量父需求上下文展示：

- 所属父需求：`#123 登录页体验优化`
- 父负责人：`Bobby`（如果存在）

该信息应放在负责人字段附近，或放在表单上部的轻提示区域，避免用户误以为该负责人来自当前子需求本身。

## UI Copy Strategy

### When Parent Has Assignee

在负责人字段下显示：

- `已默认带出父需求负责人，可单独修改`

### When Parent Has No Assignee

显示：

- `父需求未设置负责人，请为子需求单独选择`

### When User Changes Assignee

若用户将负责人改为与父需求不同的人，显示：

- `当前负责人已独立于父需求`

### When Parent Assignee Is Unavailable

若父负责人不在项目成员列表中，显示：

- `父需求负责人当前不可选，请重新选择子需求负责人`

## Data Flow Design

### Create Sub-Requirement From List Dialog

1. 用户点击父需求下的“新建子需求”
2. 系统保存 `parentId`
3. 系统使用当前父需求行数据初始化默认值：
   - `assigneeId = parentReq.assigneeId ?? null`
4. 系统展示提示文案
5. 用户可修改负责人后提交

### Create Sub-Requirement From Detail Page

1. 用户点击详情页中的“新建子需求”
2. 系统跳转到创建页并带 `parentId`
3. 创建页加载基础数据后，再根据 `parentId` 拉取父需求详情
4. 若父需求负责人可用，则默认写入 `form.assigneeId`
5. 展示相同提示文案
6. 用户可修改负责人后提交

## Edge Cases

### Parent Requirement Load Failure

- 不阻塞创建页继续使用
- 仅跳过默认负责人赋值
- 不展示错误级阻断提示，可保持静默或轻提示

### Parent Has No Assignee

- `form.assigneeId = null`
- 提示用户需要手动选择

### Parent Assignee Not In Member List

- `form.assigneeId = null`
- 给出轻提示，避免用户认为系统出错

### User Already Edited Assignee

- 后续若重新处理父需求数据，不应覆盖用户当前选择

### Edit Existing Child Requirement

- 不触发本轮默认逻辑
- 只在“新建子需求”初始化时生效

## File Touch Points

核心涉及文件：

- `frontend/src/views/requirement/RequirementCreatePage.vue`
- `frontend/src/views/requirement/RequirementDetailPage.vue`
- `frontend/src/views/requirement/RequirementPage.vue`
- `frontend/src/constants/requirement.ts`

其中：

- `RequirementCreatePage.vue` 是本轮主实现落点
- `RequirementDetailPage.vue` 负责子需求创建入口跳转
- `RequirementPage.vue` 负责列表弹窗路径的一致性提示
- `requirement.ts` 负责中英文提示文案沉淀

## Minimal Implementation Strategy

建议按最小范围实现：

1. 不新增后端改动
2. 不重构整套需求表单
3. 只补齐“父负责人默认带出 + 可单独修改 + 明确提示”
4. 保持列表弹窗与独立创建页行为一致

## Risks and Mitigations

### Risk 1: Overwriting User Choice

- **Risk:** 拉取父需求详情后可能覆盖用户已经手动改过的负责人
- **Mitigation:** 增加 `assigneeTouched` 或等价状态，只在未手动修改时才自动带值

### Risk 2: Different Entry Paths Drift Again

- **Risk:** 列表页和独立页后续继续出现逻辑分叉
- **Mitigation:** 提炼一个共享的“父需求默认负责人初始化规则”，让两条路径遵循同一判断标准

### Risk 3: Parent Assignee Is Not Selectable

- **Risk:** 默认回填一个不在成员列表中的用户会导致选择框显示异常
- **Mitigation:** 在成员列表中校验父负责人可用性，不可用则置空并提示

## Expected Outcome

完成本轮优化后，应达到以下结果：

- 从需求列表创建子需求时，默认带出父需求负责人
- 从需求详情创建子需求时，也默认带出父需求负责人
- 两条路径的文案提示一致
- 用户可以随时把子需求负责人改成与父需求不同的人
- 提交后子需求负责人以用户最终选择为准
