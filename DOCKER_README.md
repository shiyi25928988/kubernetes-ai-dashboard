# MPI Scheduling Service Docker 部署指南

## 文件说明

| 文件 | 说明 |
|------|------|
| `dashboard/Dockerfile` | 后端 Docker 镜像构建文件 |
| `dashboard-fe/Dockerfile` | 前端 Docker 镜像构建文件 |
| `docker-compose.dashboard.yml` | **全栈部署**（含 MySQL、Redis、MinIO） |
| `docker-compose.dashboard-only.yml` | **仅部署应用**（中间件外部已部署） |
| `.env.example` | 环境变量配置模板 |

---

## 1. 全栈部署（推荐本地开发使用）

包含完整的中间件（MySQL 8、Redis、MinIO）和应用服务：

```bash
# 复制到项目根目录使用
cp .env.example .env

# 启动所有服务
docker-compose -f docker-compose.dashboard.yml up -d

# 查看日志
docker-compose -f docker-compose.dashboard.yml logs -f

# 停止服务
docker-compose -f docker-compose.dashboard.yml down
```

访问地址：
- 前端: http://localhost:3000
- 后端 API: http://localhost:8080
- MinIO 控制台: http://localhost:9001 (minioadmin/minioadmin)

---

## 2. 仅部署应用（生产环境使用）

假设 MySQL、Redis、MinIO 等中间件已在集群中部署：

```bash
# 1. 复制环境变量模板
cp .env.example .env

# 2. 修改 .env 文件中的中间件地址
# 编辑 .env，替换 mysql-host, redis-host, minio-host 等

# 3. 启动
docker-compose -f docker-compose.dashboard-only.yml up -d

# 4. 查看状态
docker-compose -f docker-compose.dashboard-only.yml logs -f
```

---

## 3. 使用打包脚本构建镜像

提供了跨平台打包脚本：

### Windows (使用 .bat)
```cmd
# 完整打包（前端+后端）
build-docker.bat v1.0.0

# 仅打包后端
build-docker.bat --backend-only v1.0.0

# 仅打包前端
build-docker.bat --frontend-only v1.0.0

# 打包并推送到仓库
build-docker.bat --push v1.0.0
```

### Linux/Mac (使用 .sh)
```bash
# 赋予执行权限
chmod +x build-docker.sh

# 完整打包（前端+后端）
./build-docker.sh v1.0.0

# 仅打包后端
./build-docker.sh --backend-only v1.0.0

# 仅打包前端
./build-docker.sh --frontend-only v1.0.0

# 打包并推送到仓库
./build-docker.sh --push v1.0.0
```

## 4. 单独构建镜像（原始方式）

```bash
# 构建后端镜像
docker build -f dashboard/Dockerfile -t <REGISTRY>/mpi-dashboard-backend:latest .

# 构建前端镜像
docker build -f dashboard-fe/Dockerfile -t <REGISTRY>/mpi-dashboard-frontend:latest ./dashboard-fe
```

---

## 环境变量说明（完整）

### 后端配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `SERVER_PORT` | 8080 | 后端服务端口 |
| `SPRING_PROFILES_ACTIVE` | dev | 激活的配置文件 |
| `SPRING_DATASOURCE_URL` | | MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | | 数据库密码 |
| `REDIS_HOST` | | Redis 主机 |
| `REDIS_PORT` | | Redis 端口 |
| `REDIS_DATABASE` | 0 | Redis DB 索引 |
| `REDIS_PASSWORD` | | Redis 密码（无则留空） |
| `MINIO_HOST` | | MinIO 主机 |
| `MINIO_PORT` | | MinIO 端口 |
| `MINIO_BUCKET` | mpi | MinIO bucket |
| `MINIO_USERNAME` | minioadmin | MinIO 用户名 |
| `MINIO_PASSWORD` | minioadmin | MinIO 密码 |
| `SPRING_AI_OPENAI_BASE_URL` | | AI API 地址 |
| `SPRING_AI_OPENAI_API_KEY` | | AI API Key |

### 前端配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `BACKEND_HOST` | backend | 后端服务地址（docker-compose 中为服务名） |
| `BACKEND_PORT` | 8080 | 后端服务端口 |

---

## 网络连接说明

当使用 `docker-compose.dashboard.yml` 全栈部署时，服务间通过 Docker 网络相互访问：

- 前端容器访问后端: `http://backend:8080`
- 后端容器访问 MySQL: `http://mysql:3306`
- 后端容器访问 Redis: `http://redis:6379`
- 后端容器访问 MinIO: `http://minio:9000`

当使用外部中间件时，请确保容器网络能够访问外部中间件地址。
