# PRM Role Documentation Cleanup Design

**Date:** 2026-03-12

## Background

系统当前有效角色模型已收敛为：

- `SUPER_ADMIN`
- `PROJECT_ADMIN`
- `DEV`

但 `docs/plans` 中仍有部分文档保留旧角色模型描述：`PM / QA / GUEST`。

## Confirmed Decisions

- 只清理仍有参考价值的文档
- 当前有效文档直接改成 3 角色现状
- 历史设计稿不重写正文，只补“旧角色模型说明”

## Goals

1. 避免旧角色模型继续误导后续开发
2. 保留历史设计上下文，不篡改阶段性事实
3. 让当前有效文档与现行实现一致

## File Strategy

### Update to Current State

- `docs/plans/2026-02-26-prm-design.md`

### Add Historical Note

- `docs/plans/2026-03-06-role-based-ui-redesign-design.md`
- `docs/plans/2026-03-06-role-based-ui-redesign-implementation-plan.md`

## Expected Outcome

完成后，当前有效文档会清楚表达 3 角色模型，而历史设计稿也会明确说明它们引用的是旧角色体系。
