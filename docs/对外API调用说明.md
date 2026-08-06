# 对外 API 调用说明文档

## 概述

本文档描述了供外部系统/Agent 调用的标准化 REST API 接口。所有接口统一使用 `/api/external/v1` 前缀。

**基础 URL**: `http://<服务器地址>:<端口>/api/external/v1`

**Content-Type**: `application/json` (文件上传除外)

---

## 统一响应格式

所有 API 返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 响应码，200 表示成功 |
| `message` | string | 响应消息 |
| `data` | object | 响应数据，具体内容依接口而定 |
| `timestamp` | string | 响应时间戳 |
| `traceId` | string | 请求追踪 ID，用于问题排查 |

### 错误响应示例

```json
{
  "code": 500,
  "message": "错误信息描述",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 1. 健康检查接口

### GET /health

检查 API 服务是否可用，用于心跳检测。

**请求示例**:
```bash
curl http://localhost:8080/api/external/v1/health
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "API 服务正常运行",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 2. 求解任务接口（推荐）

### 2.1 生成 Solver ID

#### POST /solver/generate

初始化求解任务，生成唯一的 solverId。

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/external/v1/solver/generate
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Solver ID 生成成功",
  "data": "solver-abc123def456",
  "timestamp": "2026-06-23T10:30:00",
  "traceId": "abc12345def6"
}
```

**说明**:
- 返回的 solverId 用于后续创建和查询求解任务
- 系统会自动初始化任务记录并设置状态为 PENDING

---

### 2.2 创建求解任务

#### POST /solver/{solverId}/task

基于 solverId 创建 Aerosim 仿真求解任务。

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `solverId` | string | 是 | 求解器 ID（由 /solver/generate 接口返回） |

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `yamlUrl` | string | 是 | YAML 配置文件 URL 地址 |
| `namespace` | string | 否 | K8S 命名空间，默认: `default` |
| `netgenThreads` | int | 否 | 网格生成线程数，默认: 4 |
| `logLevel` | string | 否 | 日志级别，默认: `INFO` |
| `minioEndpoint` | string | 否 | MinIO Endpoint，默认: `http://10.113.16.247:9000` |
| `minioAccessKey` | string | 否 | MinIO Access Key，默认: `minioadmin` |
| `minioSecretKey` | string | 否 | MinIO Secret Key，默认: `minioadmin` |
| `minioBucket` | string | 否 | MinIO Bucket，默认: `test` |

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/external/v1/solver/solver-abc123def456/task \
  -H "Content-Type: application/json" \
  -d '{
    "yamlUrl": "http://10.113.16.247:9000/result/cabin_simulation.yaml",
    "minioEndpoint": "http://10.113.16.247:9000",
    "minioAccessKey": "minioadmin",
    "minioSecretKey": "minioadmin",
    "minioBucket": "test",
    "netgenThreads": 4,
    "logLevel": "INFO"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "任务创建成功，计算结果将会上传至 [test/solving-results/session-xyz123]",
  "data": {
    "solverId": "solver-abc123def456",
    "benchmarkId": "session-xyz123",
    "jobName": "aerosim-job-abc12345",
    "namespace": "default",
    "yamlUrl": "http://10.113.16.247:9000/result/cabin_simulation.yaml",
    "minioBucket": "test",
    "minioFolder": "solving-results/session-xyz123",
    "solvingStatus": "PENDING",
    "activePods": 0,
    "succeededPods": 0,
    "failedPods": 0,
    "readyPods": 0,
    "startTime": "2026-06-23T10:30:00",
    "createdTime": "2026-06-23T10:30:00"
  },
  "timestamp": "2026-06-23T10:30:00",
  "traceId": "abc12345def6"
}
```

---

### 2.3 查询求解任务状态

#### GET /solver/{solverId}/status

根据 solverId 查询求解任务的详细状态信息。

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `solverId` | string | 是 | 求解器 ID |

**请求示例**:
```bash
curl http://localhost:8080/api/external/v1/solver/solver-abc123def456/status
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "solverId": "solver-abc123def456",
    "benchmarkId": "session-xyz123",
    "jobName": "aerosim-job-abc12345",
    "namespace": "default",
    "yamlUrl": "http://10.113.16.247:9000/result/cabin_simulation.yaml",
    "minioBucket": "test",
    "minioFolder": "solving-results/session-xyz123",
    "solvingStatus": "RUNNING",
    "activePods": 1,
    "succeededPods": 0,
    "failedPods": 0,
    "readyPods": 1,
    "startTime": "2026-06-23T10:30:00",
    "endTime": null,
    "createdTime": "2026-06-23T10:30:00"
  },
  "timestamp": "2026-06-23T10:35:00",
  "traceId": "abc12345def6"
}
```

**状态说明**:
- `PENDING`: 任务已创建，等待执行
- `RUNNING`: 任务正在执行中
- `SUCCEEDED`: 任务执行成功
- `FAILED`: 任务执行失败
- `CANCELLED`: 任务已取消

**说明**:
- 系统每 5 秒自动轮询未完成任务的 K8S Job 状态并更新数据库

---

### 2.4 取消求解任务

#### DELETE /solver/{solverId}/task

取消指定 solverId 的求解任务。

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `solverId` | string | 是 | 求解器 ID |

**请求示例**:
```bash
curl -X DELETE http://localhost:8080/api/external/v1/solver/solver-abc123def456/task
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "任务已取消",
  "timestamp": "2026-06-23T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 3. Aerosim 仿真任务接口（旧版）

### 2.1 创建 Aerosim 仿真任务

#### POST /aerosim/jobs

在 K8S 集群中创建 Aerosim 仿真 Job。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID，用于标识求解会话并作为 MinIO 结果文件夹路径 |
| `yamlUrl` | string | 是 | YAML 配置文件 URL 地址 |
| `namespace` | string | 否 | K8S 命名空间，默认: `default` |
| `netgenThreads` | int | 否 | 网格生成线程数，默认: 4 |
| `logLevel` | string | 否 | 日志级别，默认: `INFO` |
| `minioEndpoint` | string | 否 | MinIO Endpoint，默认: `http://10.113.16.247:9000` |
| `minioAccessKey` | string | 否 | MinIO Access Key，默认: `minioadmin` |
| `minioSecretKey` | string | 否 | MinIO Secret Key，默认: `minioadmin` |
| `minioBucket` | string | 否 | MinIO Bucket，默认: `test` |

**请求示例**:

```bash
curl -X POST http://localhost:8080/api/external/v1/aerosim/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "benchmarkId": "solving-session-001",
    "yamlUrl": "http://10.113.16.247:9000/result/cabin_simulation.yaml",
    "minioEndpoint": "http://10.113.16.247:9000",
    "minioAccessKey": "minioadmin",
    "minioSecretKey": "minioadmin",
    "minioBucket": "test",
    "netgenThreads": 4,
    "logLevel": "INFO"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "Job 创建成功，计算结果将会上传至 [test/solving-results/solving-session-001]",
  "data": {
    "jobName": "aerosim-job-abc12345",
    "benchmarkId": "solving-session-001",
    "namespace": "default",
    "image": "10.113.16.240/aerosim:latest",
    "minioFolder": "solving-results/solving-session-001",
    "createdAt": "2026-06-23T10:30:00",
    "status": "Created"
  },
  "timestamp": "2026-06-23T10:30:00",
  "traceId": "abc12345def6"
}
```

**说明**:
- 镜像名称固定为 `10.113.16.240/aerosim:latest`，不允许外部自定义
- 仿真结果会自动上传到 MinIO 的 `solving-results/{benchmarkId}/` 文件夹
- 参考 Docker 运行命令：
  ```bash
  docker run --rm \
    -e MINIO_ENDPOINT=http://10.113.16.247:9000 \
    -e MINIO_ACCESS_KEY=minioadmin \
    -e MINIO_SECRET_KEY=minioadmin \
    -e MINIO_BUCKET=test \
    10.113.16.240/aerosim:latest run http://10.113.16.247:9000/result/cabin_simulation.yaml \
    --minio-folder solving-results/{benchmarkId}
  ```

---

### 2.2 列出所有 Aerosim Jobs

#### GET /aerosim/jobs

获取指定命名空间下的所有 Aerosim Job 列表。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `namespace` | string | 否 | K8S 命名空间，默认: `default` |

**请求示例**:

```bash
# 获取默认命名空间的 Job
curl http://localhost:8080/api/external/v1/aerosim/jobs

# 指定命名空间
curl http://localhost:8080/api/external/v1/aerosim/jobs?namespace=default
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    "aerosim-job-abc12345",
    "aerosim-job-def67890"
  ],
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

### 2.3 获取 Aerosim Job 状态

#### GET /aerosim/jobs/{jobName}/status

获取指定 Job 的运行状态信息。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jobName` | string | 是 | Job 名称（路径参数） |
| `namespace` | string | 否 | K8S 命名空间，默认: `default` |

**请求示例**:

```bash
curl http://localhost:8080/api/external/v1/aerosim/jobs/aerosim-job-abc12345/status
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "Active: 1, Succeeded: 0, Failed: 0, Ready: 1",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

**状态说明**:
- `Active`: 正在运行的 Pod 数量
- `Succeeded`: 成功完成的 Pod 数量
- `Failed`: 失败的 Pod 数量
- `Ready`: 准备就绪的 Pod 数量

---

### 2.4 删除 Aerosim Job

#### DELETE /aerosim/jobs/{jobName}

删除指定的 Aerosim Job。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jobName` | string | 是 | Job 名称（路径参数） |
| `namespace` | string | 否 | K8S 命名空间，默认: `default` |

**请求示例**:

```bash
curl -X DELETE http://localhost:8080/api/external/v1/aerosim/jobs/aerosim-job-abc12345
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "Job 删除成功: aerosim-job-abc12345",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 3. AI 聊天接口

### 3.1 AI 聊天（非流式）

#### POST /chat

发送消息给 AI 并获取完整响应。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID |
| `conversationId` | string | 是 | 对话 ID，用于保持上下文 |
| `message` | string | 是 | 用户消息内容 |
| `configUrl` | string | 否 | 配置 URL，用于加载特定配置 |
| `systemPrompt` | string | 否 | 系统提示词，用于定制 AI 行为 |
| `saveToMinio` | boolean | 否 | 是否保存聊天记录到 MinIO，默认: `false` |

**请求示例**:

```bash
curl -X POST http://localhost:8080/api/external/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "benchmarkId": "solving-session-001",
    "conversationId": "conv-12345",
    "message": "请帮我分析仿真结果数据",
    "systemPrompt": "你是一个专业的求解助手，擅长分析仿真数据",
    "saveToMinio": true
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "根据您提供的仿真结果数据，我分析如下：...",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

### 3.2 AI 流式聊天（SSE）

#### POST /chat/stream

Server-Sent Events 流式响应，实时获取 AI 输出。

**请求参数与 `/chat` 相同**

**请求示例**:

```bash
curl -N http://localhost:8080/api/external/v1/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "benchmarkId": "solving-session-001",
    "conversationId": "conv-12345",
    "message": "请详细描述仿真过程",
    "systemPrompt": "你是一个专业的求解助手"
  }'
```

**响应**: 流式输出，每次返回一段文本内容

---

## 4. 求解会话管理接口

### 4.1 创建求解会话

#### POST /sessions

初始化求解会话，在 MinIO 中创建文件夹并保存会话信息。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID |
| `configUrl` | string | 否 | 配置 URL |
| `description` | string | 否 | 会话描述 |

**请求示例**:

```bash
curl -X POST http://localhost:8080/api/external/v1/sessions \
  -H "Content-Type: application/json" \
  -d '{
    "benchmarkId": "solving-session-001",
    "configUrl": "http://config-service/solver-config.json",
    "description": "第一轮求解任务"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "http://10.35.79.185:9000/aerosim-results/solving-results/solving-session-001/session-info.json",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

**说明**:
- `data` 字段返回会话信息文件的 MinIO URL

---

### 4.2 列出所有求解会话

#### GET /sessions

获取所有求解会话的 benchmarkId 列表。

**请求示例**:

```bash
curl http://localhost:8080/api/external/v1/sessions
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    "solving-session-001",
    "solving-session-002",
    "solving-session-003"
  ],
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 5. 求解结果存储接口

### 5.1 保存求解结果（文本/JSON）

#### POST /results/text

将文本/JSON 格式的求解结果保存到 MinIO。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID |
| `fileName` | string | 是 | 文件名称 |
| `content` | string | 否 | 文件内容 |
| `contentType` | string | 否 | 内容类型 |
| `description` | string | 否 | 结果描述 |

**请求示例**:

```bash
curl -X POST http://localhost:8080/api/external/v1/results/text \
  -H "Content-Type: application/json" \
  -d '{
    "benchmarkId": "solving-session-001",
    "fileName": "result-round1.json",
    "content": "{\"result\": \"success\", \"data\": {\"iterations\": 100, \"convergence\": 0.001}}",
    "contentType": "application/json",
    "description": "第一轮求解结果"
  }'
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "http://10.35.79.185:9000/aerosim-results/solving-results/solving-session-001/result-round1.json",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

**说明**:
- `data` 字段返回文件的 MinIO 访问 URL

---

### 5.2 保存求解结果（文件上传）

#### POST /results/file

上传文件到 MinIO 存储（支持任意类型文件）。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID |
| `file` | file | 是 | 上传的文件（multipart/form-data） |
| `fileName` | string | 否 | 自定义文件名，不指定则使用原始文件名 |

**请求示例**:

```bash
curl -X POST http://localhost:8080/api/external/v1/results/file \
  -F "benchmarkId=solving-session-001" \
  -F "file=@/path/to/result.vtk" \
  -F "fileName=custom-name.vtk"
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": "http://10.35.79.185:9000/aerosim-results/solving-results/solving-session-001/custom-name.vtk",
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

### 5.3 列出求解结果文件

#### GET /results/{benchmarkId}

获取指定 benchmarkId 会话下的所有文件列表。

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `benchmarkId` | string | 是 | 全局会话 ID（路径参数） |

**请求示例**:

```bash
curl http://localhost:8080/api/external/v1/results/solving-session-001
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "fileName": "session-info.json",
      "bucket": "aerosim-results",
      "folderName": "solving-results/solving-session-001",
      "downloadUrl": "http://10.35.79.185:9000/aerosim-results/solving-results/solving-session-001/session-info.json",
      "isFolder": false,
      "size": 256,
      "filePath": "solving-results/solving-session-001/session-info.json"
    },
    {
      "fileName": "result-round1.json",
      "bucket": "aerosim-results",
      "folderName": "solving-results/solving-session-001",
      "downloadUrl": "http://10.35.79.185:9000/aerosim-results/solving-results/solving-session-001/result-round1.json",
      "isFolder": false,
      "size": 1024,
      "filePath": "solving-results/solving-session-001/result-round1.json"
    }
  ],
  "timestamp": "2026-06-15T10:30:00",
  "traceId": "abc12345def6"
}
```

---

## 6. Python 调用示例

```python
import requests

BASE_URL = "http://localhost:8080/api/external/v1"

# 1. 健康检查
response = requests.get(f"{BASE_URL}/health")
print(response.json())

# 2. 生成 Solver ID
response = requests.post(f"{BASE_URL}/solver/generate")
solver_id = response.json()["data"]
print(f"Solver ID: {solver_id}")

# 3. 创建求解任务
job_data = {
    "yamlUrl": "http://10.113.16.247:9000/result/cabin_simulation.yaml",
    "minioEndpoint": "http://10.113.16.247:9000",
    "minioBucket": "test"
}
response = requests.post(f"{BASE_URL}/solver/{solver_id}/task", json=job_data)
print(response.json())

# 4. 查询任务状态
response = requests.get(f"{BASE_URL}/solver/{solver_id}/status")
task_status = response.json()["data"]
print(f"任务状态: {task_status['solvingStatus']}")

# 3. 保存 JSON 结果
result_data = {
    "benchmarkId": "solving-session-001",
    "fileName": "result.json",
    "content": '{"result": "success"}',
    "contentType": "application/json"
}
response = requests.post(f"{BASE_URL}/results/text", json=result_data)
print(response.json())

# 4. 上传文件
files = {'file': open('result.vtk', 'rb')}
data = {'benchmarkId': 'solving-session-001'}
response = requests.post(f"{BASE_URL}/results/file", files=files, data=data)
print(response.json())

# 5. AI 聊天
chat_data = {
    "benchmarkId": "solving-session-001",
    "conversationId": "conv-12345",
    "message": "请帮我分析仿真结果"
}
response = requests.post(f"{BASE_URL}/chat", json=chat_data)
print(response.json())
```

---

## 7. Java 调用示例

```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/external/v1";
    private RestTemplate restTemplate = new RestTemplate();
    
    // 健康检查
    public void healthCheck() {
        String response = restTemplate.getForObject(BASE_URL + "/health", String.class);
        System.out.println(response);
    }
    
    // 创建 Aerosim Job
    public void createJob() {
        Map<String, Object> data = new HashMap<>();
        data.put("benchmarkId", "solving-session-001");
        data.put("yamlUrl", "http://10.113.16.247:9000/result/cabin_simulation.yaml");
        data.put("minioEndpoint", "http://10.113.16.247:9000");
        data.put("minioAccessKey", "minioadmin");
        data.put("minioSecretKey", "minioadmin");
        data.put("minioBucket", "test");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            BASE_URL + "/aerosim/jobs", request, String.class);
        System.out.println(response.getBody());
    }
}
```

---

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

---

## 9. 注意事项

1. **请求超时**: 建议设置合理的超时时间（建议 30-60 秒）
2. **并发限制**: 请避免同时发起过多请求，建议控制在 10 并发以内
3. **文件大小**: 文件上传建议控制在 100MB 以内，大文件建议分片上传
4. **任务状态轮询**: 创建 Job 后建议间隔 5-10 秒轮询一次状态
5. **TraceId**: 遇到问题时请提供 `traceId` 以便快速定位问题

---

## 10. 完整接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| POST | `/solver/generate` | 生成 Solver ID |
| POST | `/solver/{solverId}/task` | 创建求解任务 |
| GET | `/solver/{solverId}/status` | 查询求解任务状态 |
| DELETE | `/solver/{solverId}/task` | 取消求解任务 |
| POST | `/aerosim/jobs` | 创建 Aerosim 仿真任务（旧版） |
| GET | `/aerosim/jobs` | 列出所有 Aerosim Jobs |
| GET | `/aerosim/jobs/{jobName}/status` | 获取 Job 状态 |
| DELETE | `/aerosim/jobs/{jobName}` | 删除 Aerosim Job |
| POST | `/chat` | AI 聊天（非流式） |
| POST | `/chat/stream` | AI 流式聊天 |
| POST | `/sessions` | 创建求解会话 |
| GET | `/sessions` | 列出所有求解会话 |
| POST | `/results/text` | 保存文本结果 |
| POST | `/results/file` | 上传文件结果 |
| GET | `/results/{benchmarkId}` | 列出会话下的所有文件 |

---

**文档版本**: v1.2  
**最后更新**: 2026-06-23
