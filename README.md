# PRM - 项目管理平台

> 面向中小团队的研发全流程管理平台：项目 → 需求 → 任务 → Bug → 迭代/发布 → 看板

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Java 21, Spring Boot 3.3.x |
| 认证 | Sa-Token（UUID Token + RBAC）|
| ORM | MyBatis-Plus 3.5.9 |
| 数据库 | SQLite（零运维，可平滑迁移至 MySQL）|
| 迁移 | Flyway 10.x |
| 前端 | Vue3 + Vite + Element Plus + Pinia |
| 文档 | SpringDoc OpenAPI（Swagger UI）|

## 快速启动

### 前置要求

- Java 21+
- Maven 3.9+
- Node 18+

### 开发调试（推荐：前后端分开启动，便于调试）

**1. 先启动后端**（默认端口 8080）

```bash
cd backend
mvn spring-boot:run
```

或先打包再运行：

```bash
cd backend
mvn package -DskipTests
java -jar target/prm-backend-1.0.0-SNAPSHOT.jar
```

**2. 再启动前端**（Vite 开发服务器，端口 5173，热更新）

```bash
cd frontend
npm install   # 首次需要
npm run dev
```

**3. 在浏览器访问** [http://localhost:5173](http://localhost:5173) 进行调试。

前端会把 `/api` 请求代理到 `http://localhost:8080`，无需跨域。修改 `frontend/vite.config.ts` 里的 `server.proxy` 可更换后端地址。

### 生产部署

**约定（当前环境）：**

- 前端静态资源目录：**`/opt/prm/front`**
- 后端 JAR 所在目录：**`/opt/prm`**
- 更新后端后重启：**`pm2 restart 0`**（`0` 为 PM2 进程 id，以 `pm2 list` 为准）

**完整步骤、Nginx 示例与验证方式见：[部署文档](docs/deployment.md)。**

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | Admin@123 | 超级管理员 |

## API 文档

- 本地直连后端：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- 经 Nginx 部署：浏览器访问 `http(s)://<你的域名>/swagger-ui.html`（与上文 `location` 一致）

## 核心功能模块

- **认证**：`/api/auth/login` 登录，Bearer Token 鉴权
- **项目管理**：创建/归档/关闭项目，项目成员管理（角色：ADMIN/PM/DEV/QA/GUEST）
- **需求管理（第一阶段）**：当前主流程为 `DRAFT → IN_PROGRESS → DONE`，评审流接口保留但不作为默认流程
- **任务管理**：看板/列表双视图，工时填报，子任务，任务状态流转
- **Bug 管理（第一阶段）**：当前主流程为 `ACTIVE → RESOLVED → CLOSED`，历史旧状态仅兼容展示，支持重开
- **迭代管理**：Sprint 规划，关闭前自动检查严重 Bug
- **版本发布（第一阶段）**：当前提供版本记录与发布标记，未启用完整发布项闭环
- **看板统计**：项目/需求/任务/Bug 多维统计，每日自动聚合快照

## 数据库迁移至 MySQL

1. 导出 SQLite 数据
2. 修改 `application.yml`：更换 datasource 为 MySQL 配置
3. Flyway 会自动在 MySQL 上重新执行迁移脚本
4. 所有 DDL 保持 SQLite/MySQL 双兼容风格

## 项目结构

```
prm/
├── backend/                   # Spring Boot 后端
│   ├── src/main/java/com/prm/
│   │   ├── common/            # 统一响应、异常处理、工具
│   │   ├── config/            # Sa-Token、MyBatis-Plus、CORS、Swagger
│   │   └── module/            # 业务模块（auth/system/project/requirement/task/bug/sprint/release/dashboard/log）
│   └── src/main/resources/
│       └── db/migration/      # Flyway SQL 迁移脚本
├── frontend/                  # Vue3 前端
│   └── src/
│       ├── api/               # Axios 接口封装
│       ├── stores/            # Pinia 状态管理
│       ├── views/             # 页面组件
│       └── layouts/           # 主布局
├── docs/
│   ├── deployment.md          # 生产部署（目录约定、PM2、Nginx）
│   └── plans/                 # 设计文档与实施计划
```
