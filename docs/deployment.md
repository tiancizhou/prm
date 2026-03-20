# PRM 生产环境部署说明

本文说明当前约定下的服务器目录、构建发布步骤与 Nginx / PM2 用法，供同事查阅。

## 服务器目录约定

| 用途 | 路径 |
|------|------|
| 前端静态资源（`npm run build` 产物） | `/opt/prm/front` |
| 后端可执行 JAR | `/opt/prm`（JAR 文件放在此目录下，与运维约定的 PM2 启动命令一致） |

> 部署前请确保目录已创建且 Nginx 用户对工作目录有读权限，运行 JAR 的系统用户对 `/opt/prm` 及数据/上传目录有读写权限。

## 环境要求（服务器）

- **Java 21+**（运行 Spring Boot JAR）
- **Nginx**（托管前端、反代 API 与 Swagger）
- **PM2**（托管后端 JAR 进程；当前使用 `pm2 restart 0` 表示重启 **PM2 列表中 id 为 0** 的应用）

构建可在本机或 CI 完成，服务器上可不安装 Node，仅需替换构建产物与 JAR。

## 发布流程概览

1. 本地或流水线构建前端 → 将 `dist` 同步到 `/opt/prm/front`
2. 构建后端 JAR → 上传到 `/opt/prm`
3. 重启后端进程：`pm2 restart 0`
4. reload Nginx（若仅改动了 Nginx 配置）：`sudo nginx -t && sudo nginx -s reload`

## 1. 构建前端

```bash
cd frontend
npm install    # 首次或依赖变更时
npm run build
```

产物目录：`frontend/dist/`。将其中**所有文件**部署到服务器 **`/opt/prm/front`**（覆盖更新即可）。

示例（在构建机执行，按实际用户与主机修改）：

```bash
rsync -av --delete ./dist/ user@your-server:/opt/prm/front/
# 或使用 scp、SFTP 等工具上传 dist 内容
```

## 2. 构建后端

```bash
cd backend
mvn package -DskipTests
```

生成的 JAR 默认路径：`backend/target/prm-backend-1.0.0-SNAPSHOT.jar`。将该文件上传到服务器 **`/opt/prm/`**（可与旧 JAR 同名覆盖，以当前 PM2 启动脚本为准）。

上传后执行：

```bash
pm2 restart 0
```

> **说明：** `0` 为 PM2 进程 **id**。若不确定，在服务器执行 `pm2 list` 查看；若团队改用进程名称管理，可改为 `pm2 restart <应用名>`，并同步更新本文档。

## 3. Nginx 配置示例

前端 `root` 指向 `/opt/prm/front`，`/api` 与 Swagger 反代到本机后端（默认 `127.0.0.1:8080`）。域名与路径请按实际修改。

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /opt/prm/front;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
        proxy_set_header Host $host;
    }

    location /swagger-ui.html {
        proxy_pass http://127.0.0.1:8080/swagger-ui.html;
        proxy_set_header Host $host;
    }

    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
        proxy_set_header Host $host;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|svg|ico|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800";
        access_log off;
    }
}
```

若 Swagger 页面资源 404，可补充：

```nginx
location /webjars/ {
    proxy_pass http://127.0.0.1:8080/webjars/;
    proxy_set_header Host $host;
}
```

## 4. PM2 与后端启动

- 后端由 **PM2** 管理；更新 JAR 后执行 **`pm2 restart 0`** 使新版本生效。
- PM2 的**首次**配置（`pm2 start …`、开机自启等）由运维或负责人单独维护，不在此重复；请向管理员索取当前使用的启动参数与环境变量（如 `application-prod.yml`、JVM 参数、上传目录等）。

## 5. 验证

- 打开站点首页，登录与主要业务流程走通。
- API 文档：`http(s)://<域名>/swagger-ui.html`
- 服务器上：`pm2 logs 0`（或对应应用名）查看后端日志是否有报错。

## 相关文档

- 仓库根目录 [README.md](../README.md)：技术栈、本地开发、默认账号等。
