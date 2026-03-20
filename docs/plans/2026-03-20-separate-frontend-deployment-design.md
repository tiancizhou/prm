# 前后端分离部署（设计摘要）

**日期：** 2026-03-20  
**目标：** 前端构建产物不再写入 `backend/src/main/resources/static`，由 Nginx 托管静态资源并将 `/api` 反代到 Spring Boot。

## 决策

1. **Vite `build.outDir`**：`frontend/dist`。
2. **API 路径**：前端固定 `axios` `baseURL` 为 `/api`；生产与开发均依赖同源或反向代理（Nginx / Vite dev proxy），不在仓库内做跨域配置说明。

## 运维提示

- Nginx：`root` 指向前端构建目录；`try_files` 回退 `index.html`；`/api/` 反代到后端 `/api/`；可按需反代 `/v3/api-docs`、`/swagger-ui.html`、`/swagger-ui/`。
- 生产步骤与 Nginx 示例见 `docs/deployment.md`。
