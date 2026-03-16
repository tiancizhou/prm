# PRM User Business Detail Design

**Date:** 2026-03-11

## Background

组织域的 `团队` 页面已经重构为人员列表视图。用户要求点击姓名后进入一个业务数据详情页，以用户为中心查看 `日程 / 需求 / Bug` 三类业务数据。

## Confirmed Decisions

- 入口：在组织域 `团队` 页面点击人员姓名进入详情页
- 首版只实现 3 个业务标签：`日程`、`需求`、`Bug`
- `日程` 基于该用户被指派任务的时间信息聚合展示
- `需求` 使用现有需求分页接口按 `assigneeId` 过滤
- `Bug` 使用现有 Bug 接口拉取后在前端按 `assigneeId` 过滤

