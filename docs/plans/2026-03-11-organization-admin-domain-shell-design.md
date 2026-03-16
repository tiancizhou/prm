# PRM Organization and Admin Domain Shell Design

**Date:** 2026-03-11

## Background

用户要求保留左侧一级导航中的 `工作台 / 项目集 / 组织 / 后台`，但希望 `组织` 与 `后台` 两个域的内部页面切换为更接近历史管理系统截图的蓝色横向导航模式，而不是继续沿用当前的工作台 Header + 卡片式组织首页。

## Confirmed Decisions

- `工作台 / 项目集` 继续使用当前工作台壳层
- `组织 / 后台` 改用共用的蓝色横向导航子壳层
- `组织` 域子菜单：`团队/部门`、`人员`、`权限`
- `后台` 域子菜单：`系统`、`动态`、`公司`
- `/organization` 默认跳到 `/organization/teams`
- `/admin` 默认跳到 `/admin/system`

## Architecture

- 保留 `MainLayout` 作为全局一级导航壳层
- 新增 `DomainTopNavLayout` 作为组织/后台共用子壳层
- 域壳层负责：
  - 顶部蓝色横向导航
  - 右侧图标工具区
  - 下方内容页容器

## Route Strategy

- `/organization/*` 挂到 `DomainTopNavLayout`
- `/admin/*` 挂到 `DomainTopNavLayout`
- 旧的 `/system/users` 继续重定向到 `/organization/users`

## Expected Outcome

- 左侧仍可从一级入口切换产品域
- 进入组织或后台后，视觉上切换为蓝色横向导航管理域
- 组织与后台的内部导航与页面结构更接近用户提供的参考图

