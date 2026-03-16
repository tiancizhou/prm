# PRM Admin People Management Design

**Date:** 2026-03-11

## Background

用户要求将 `后台` 域收敛为单一主入口 `人员管理`，并在该工作区内提供 `部门 / 用户 / 权限` 三个页面，页面布局参考提供的后台截图。

## Confirmed Decisions

- 左侧一级导航保留 `后台`
- 进入 `后台` 后，顶部蓝色导航只承载 `后台 > 人员管理`
- 页面内部二级切换为：`部门`、`用户`、`权限`
- `部门` 页面按左右分栏结构实现
- `用户` 页面按后台用户表格结构实现
- `权限` 页面按权限分组列表结构实现

## Route Design

- `/admin` → 重定向到 `/admin/departments`
- `/admin/departments`
- `/admin/users`
- `/admin/permissions`

## Expected Outcome

- 后台域不再展示 `系统 / 动态 / 公司`
- 后台域只保留一个主业务区：`人员管理`
- `部门 / 用户 / 权限` 三页结构与截图风格保持一致

