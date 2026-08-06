# A2A 接口入参规范说明文档

## 概述

本文档描述 MPI 调度平台 A2A（Agent-to-Agent）Server 的接口入参规范，供上游 AI 应用 / Agent 通过 A2A 协议调用本平台的求解器能力（MPI 作业、Aerosim 仿真、MYSTRAN 有限元）。

A2A Server 基于 JSON-RPC 2.0 传输协议实现，所有调用统一通过 `POST /a2a` 端点完成，方法名通过 `method` 字段区分。

> **协议版本**：本服务统一实现 **A2A 1.0**（JSONRPC binding）。方法名使用 PascalCase（`SendMessage`/`GetTask`/`CancelTask`/`ListTasks`），Part 无 `kind` 判别字段，TaskState 线名为 `TASK_STATE_*`，`SendMessage`/`GetTask` 响应 result 包装在 `{"task":{...}}` 下。输入侧兼容 0.3 的 `message/send` 等斜杠方法名与 `{kind:...}` Part 写法，但输出严格 1.0。

**基础 URL**: `http://<服务器地址>:<端口>/a2a`

**Content-Type**: `application/a2a+json`（1.0 推荐，`application/json` 亦接受）

**协议版本**: A2A `protocolVersion` = `1.0`，JSON-RPC `2.0`，请求头建议携带 `A2A-Version: 1.0`

---

## 1. 端点清单

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/.well-known/agent-card.json` | 免认证 | 获取 AgentCard（A2A 标准发现路径·根） |
| GET | `/a2a/.well-known/agent-card.json` | 免认证 | 获取 AgentCard（A2A 标准发现路径·带前缀） |
| GET | `/a2a/card` | 免认证 | 获取 AgentCard（便捷路径） |
| POST | `/a2a` | 需认证 | JSON-RPC 2.0 端点（所有方法调用） |
| POST | `/a2a/` | 需认证 | 兼容带尾斜杠的调用 |

> GET 发现端点用于上游应用获取本 Agent 的能力名片（含 supportedInterfaces 与技能列表），免认证可直接访问。

---

## 2. 认证

### 2.1 请求头规范

对 `POST /a2a` 调用，需在请求头中携带 API Key，支持以下两种方式（任选其一）：

| 请求头 | 格式 | 示例 |
|--------|------|------|
| `X-API-Key` | 直接填写 API Key | `X-API-Key: 1234567890` |
| `Authorization` | `Bearer <API Key>` | `Authorization: Bearer 1234567890` |

API Key 配置项为 `a2a.api-key`，默认复用 `external.api-key`（默认值 `1234567890`），支持通过环境变量 `A2A_API_KEY` / `EXTERNAL_API_KEY` 覆盖。

### 2.2 当前实现状态

> ⚠️ **备注**：当前版本 `A2AAuthFilter` 中的 API Key 校验逻辑被注释停用（`auth-enabled` 默认为 `true`，但实际未执行 Key 比对）。请求到达后仅建立系统登录上下文（`systemUserId`，默认 `0`）即放行，供下游依赖 Sa-Token 的服务获取创建者 ID。后续启用 Key 校验后，缺失或错误的 API Key 将返回 `401 Unauthorized`。建议调用方按上述规范提前携带请求头，以保证向前兼容。

认证失败响应：

```json
{
  "error": "UNAUTHORIZED",
  "message": "缺少 API Key，请在 Authorization 或 X-API-Key 请求头中提供"
}
```

---

## 3. 通用请求结构（JsonRpcRequest）

所有 `POST /a2a` 调用统一使用 JSON-RPC 2.0 请求体：

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "message/send",
  "params": { ... }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `jsonrpc` | string | 是 | 协议版本，固定为 `"2.0"` |
| `id` | string \| number \| null | 是 | 请求 ID，用于响应关联；响应中原样回传 |
| `method` | string | 是 | 方法名，见 [第 4 节方法清单](#4-方法清单) |
| `params` | object | 视方法而定 | 方法参数对象，结构由具体 `method` 决定 |

> `params` 在服务端以 `Map<String, Object>` 接收，再按方法反序列化为对应 DTO。字段命名采用驼峰（camelCase）。

---

## 4. 方法清单

> 方法名查找大小写无关，下表「主方法名」为 A2A 1.0 PascalCase，「兼容别名」为 0.3 斜杠命名与 camelCase，均可使用。

| 主方法名（1.0） | 兼容别名 | 说明 | 响应 result |
|----------------|----------|------|------------|
| `SendMessage` | `message/send`、`sendMessage` | 发送消息，执行求解逻辑 | `SendMessageResponse`（`{"task":{...}}`） |
| `GetTask` | `tasks/get`、`getTask` | 查询任务状态 | `GetTaskResponse`（`{"task":{...}}`） |
| `CancelTask` | `tasks/cancel`、`cancelTask` | 取消任务 | 裸 `Task`（**无包装**，规范方法表明确） |
| `ListTasks` | `tasks/list`、`listTasks` | 列出任务（当前返回空列表占位） | `ListTasksResponse`（`{"tasks":[...],"totalSize":0,"pageSize":0,"nextPageToken":""}`） |

> 未知方法返回 JSON-RPC 错误码 `-32601 Method not found`。

---

## 5. 各方法入参详解

### 5.1 SendMessage（兼容别名 message/send）

发送用户消息，由求解器（LLM + 工具）推理后执行创建/查询/取消等操作，返回 `SendMessageResponse`（`result.task`）。

**params 结构（MessageSendParams）**：

```json
{
  "message": {
    "role": "ROLE_USER",
    "messageId": "msg-001",
    "parts": [
      { "text": "创建一个名为 test 的 MPI 作业，使用镜像 mpi:latest，命令 mpirun -np 4 ./app", "mediaType": "text/plain" }
    ],
    "contextId": "ctx-abcdef12"
  },
  "configuration": { "acceptedOutputModes": ["text/plain", "application/json"] },
  "metadata": {
    "solverType": "mpi",
    "contextId": "ctx-abcdef12"
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | object | 是 | 消息对象，见下表；为空返回 `-32602` |
| `message.role` | string | 是 | 消息角色：A2A 1.0 为 `ROLE_USER` / `ROLE_AGENT`（兼容 `user`/`agent`） |
| `message.parts` | array | 是 | 消息内容片段列表，见 [Part 结构](#52-part-结构) |
| `message.messageId` | string | 否 | 消息 ID |
| `message.contextId` | string | 否 | 会话上下文 ID |
| `message.taskId` | string | 否 | 关联的任务 ID |
| `configuration` | object | 否 | 配置项，如 `acceptedOutputModes`（当前未强制校验） |
| `metadata` | object | 否 | 元数据，用于求解器路由与上下文传递，见 [5.3](#53-metadata-与求解器路由) |

**校验规则**：
- `message` 为空 → 返回 `-32602 Invalid params: message 不能为空`
- `message.parts` 中实际被求解器消费的是含有 `text` 字段的片段（A2A 1.0 TextPart，成员存在即判别，无需 `kind`）；`data` / 文件类型片段当前不被求解器读取。服务端输入兼容 0.3 的 `{"kind":"text","text":...}` 写法。

#### 5.2 Part 结构

A2A 1.0 **移除 `kind` 判别字段**，Part 类型由成员字段存在性判别。`mediaType` 对所有 Part 类型可选。

| 类型 | 使用字段 | 示例 |
|------|----------|------|
| TextPart | `text` | `{ "text": "查询作业 123 的状态", "mediaType": "text/plain" }` |
| DataPart | `data` | `{ "data": { "jobId": 123 }, "mediaType": "application/json" }` |
| FilePart | `raw` 或 `url` + `filename` | `{ "raw": "<base64>", "filename": "cfg.yaml", "mediaType": "application/octet-stream" }` |

> 输入兼容 0.3 的 `{"kind":"text","text":...}` / `{"kind":"data","data":...}` 写法；响应中的 Part 严格 1.0（无 `kind`）。

#### 5.3 metadata 与求解器路由

`metadata` 中以下字段被服务端识别：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `solverType` | string | 否 | 求解器类型，见下表；未指定时使用默认求解器（注册顺序第一个，即 `mpi`） |
| `contextId` | string | 否 | 会话上下文 ID；优先取 `metadata.contextId`，其次 `message.contextId`，均无则服务端自动生成 `ctx-xxxxxxxx` |

**`solverType` 取值**（不区分大小写）：

| `solverType` | 求解器 | 能力 | 示例输入 |
|--------------|--------|------|----------|
| `mpi` | MPI 作业求解器（默认） | 创建/查询/列出/取消/日志 MPI 作业 | "创建一个 8 核 16G 的 MPI 作业运行 fluid_solver" |
| `aerosim` | Aerosim 仿真求解器 | 生成 solverId / 创建 / 查询 / 取消 / 列出仿真任务 | "为 benchmark-001 创建仿真求解任务，配置文件 http://.../cabin_simulation.yaml" |
| `mystran` | MYSTRAN 有限元求解器 | 生成 solverId / 创建 / 查询 / 取消 / 列出有限元任务 | "为 benchmark-001 创建 MYSTRAN 求解任务，BDF 文件 http://.../model.bdf" |

> 路由逻辑：优先按 `metadata.solverType` 匹配；未匹配到时记告警并回退到默认求解器（`mpi`）。

### 5.4 GetTask（兼容别名 tasks/get）

根据任务 ID 查询任务状态，返回 `GetTaskResponse`（`result.task`）。

**params 结构**：

```json
{
  "id": "123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 任务 ID；为空返回 `-32602` |

**`id` 路由规则**（服务端按 `id` 前缀/格式路由到对应求解器）：

| `id` 格式 | 路由到 | 含义 |
|-----------|--------|------|
| 纯数字（如 `"123"`） | `mpi` | MPI 作业 Job ID |
| `solver-` 前缀（如 `"solver-c0ce0ee0-2d0"`） | `aerosim` | Aerosim solverId |
| `mystran-` 前缀 | `mystran` | Mystran 任务 ID |
| 其他 | 默认（`mpi`） | — |

> 💡 **建议**：`GetTask` 与 `CancelTask` 的 `id` 直接使用 `SendMessage` 响应中返回的 `Task.id`，可避免内部路由歧义。各求解器 `SendMessage` 返回的 `Task.id` 前缀：MPI 为 `task-`，Aerosim 为 `aerosim-task-`，Mystran 为 `mystran-task-`。

### 5.5 CancelTask（兼容别名 tasks/cancel）

根据任务 ID 取消任务，`params` 结构与 `GetTask` 完全一致，响应 `result` 为裸 `Task`（无包装）：

```json
{
  "id": "123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | string | 是 | 任务 ID；为空返回 `-32602`；路由规则同 `GetTask` |

### 5.6 ListTasks（兼容别名 tasks/list）

列出任务。当前为占位实现，**始终返回空列表**，无需传 `params`。响应 `result` 为 `ListTasksResponse`（`{"tasks":[],"totalSize":0,"pageSize":0,"nextPageToken":""}`）。

实际作业查询请通过 `SendMessage` 调用各求解器的列出工具（如 `listMpiJobs` / `listSolverTasks` / `listMystranTasks`）完成。

---

## 6. 通用响应结构（JsonRpcResponse）

### 6.1 成功响应

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `jsonrpc` | string | 固定 `"2.0"` |
| `id` | string \| number \| null | 与请求 `id` 对应 |
| `result` | object | 成功结果（与 `error` 互斥）；`SendMessage` 返回 `SendMessageResponse`、`GetTask` 返回 `GetTaskResponse`（均为 `{"task":{...}}`），`CancelTask` 返回裸 `Task`，`ListTasks` 返回 `{"tasks":[],"totalSize":0,"pageSize":0,"nextPageToken":""}` |

### 6.2 错误响应

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "error": {
    "code": -32601,
    "message": "Method not found: 不支持的方法 unknown/method"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `error.code` | int | JSON-RPC 错误码，见下表 |
| `error.message` | string | 错误消息 |
| `error.data` | any | 错误附加数据（可选） |

**JSON-RPC 错误码**：

| 错误码 | 含义 | 触发场景 |
|--------|------|----------|
| `-32600` | Invalid Request | `method` 为空 |
| `-32601` | Method not found | 不支持的方法名 |
| `-32602` | Invalid params | `message` 或 `id` 等必填参数为空 |
| `-32603` | Internal error | 处理异常 / 无可用求解器 |

---

## 7. Task 返回结构

`SendMessage`（`result.task`）/ `GetTask`（`result.task`）/ `CancelTask`（`result` 裸）成功时返回 `Task` 对象：

```json
{
  "kind": "task",
  "id": "task-a1b2c3d4",
  "contextId": "ctx-abcdef12",
  "status": {
    "state": "TASK_STATE_COMPLETED",
    "timestamp": "2026-07-20T10:30:00"
  },
  "artifacts": [
    {
      "name": "mpi-solver-response",
      "index": 0,
      "parts": [
        { "text": "已创建 MPI 作业，作业 ID 为 123 ..." }
      ]
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `kind` | string | 固定 `"task"` |
| `id` | string | 任务 ID（建议用作后续 `GetTask` / `CancelTask` 的 `id`） |
| `contextId` | string | 会话上下文 ID |
| `status` | object | 任务状态，见 `TaskStatus` |
| `status.state` | string | 任务状态枚举（A2A 1.0 线名 `TASK_STATE_*`），见下表 |
| `status.timestamp` | string | 状态更新时间戳（ISO-8601） |
| `status.message` | object | 关联消息（可选） |
| `artifacts` | array | 任务产物列表，每个产物含 `name` / `description` / `parts` / `index` / `append` |
| `history` | array | 历史消息（可选） |
| `metadata` | object | 元数据（可选） |

**`status.state`（TaskState，A2A 1.0 线名）取值**：

| 状态 | 含义 |
|------|------|
| `TASK_STATE_SUBMITTED` | 任务已提交（对应作业 PENDING） |
| `TASK_STATE_WORKING` | 任务执行中（对应作业 RUNNING） |
| `TASK_STATE_RUNNING` | 任务运行中（1.0 细化状态） |
| `TASK_STATE_INPUT_REQUIRED` | 需要额外输入 |
| `TASK_STATE_COMPLETED` | 任务已完成（对应作业 SUCCEEDED） |
| `TASK_STATE_CANCELED` | 任务已取消（对应作业 CANCELLED / CANCELED） |
| `TASK_STATE_FAILED` | 任务失败（对应作业 FAILED） |
| `TASK_STATE_REJECTED` | 任务被拒绝 |
| `TASK_STATE_AUTH_REQUIRED` | 需要认证 |
| `TASK_STATE_UNKNOWN` | 未知状态 |

> `SendMessage` 返回的 `Task`：成功时 `state=TASK_STATE_COMPLETED`，产物为 LLM 响应文本；异常时 `state=TASK_STATE_FAILED`，产物为错误信息。
> `GetTask` 返回的 `Task`：`state` 由底层作业状态映射而来，产物为 DataPart（`{"data":{...},"mediaType":"application/json"}`）状态详情。
> `CancelTask` 返回的 `Task`：成功时 `state=TASK_STATE_CANCELED`。

---

## 8. AgentCard 发现端点

`GET /.well-known/agent-card.json`（根）、`GET /a2a/.well-known/agent-card.json` 或 `GET /a2a/card` 返回 Agent 名片：

```json
{
  "name": "MPI Scheduling Agent",
  "description": "提供 MPI 并行计算作业的创建、查询、取消等管理服务...",
  "url": "http://<服务器地址>:8080/a2a",
  "supportedInterfaces": [
    {
      "url": "http://<服务器地址>:8080/a2a",
      "protocolBinding": "JSONRPC",
      "protocolVersion": "1.0"
    }
  ],
  "version": "1.0.0",
  "protocolVersion": "1.0",
  "capabilities": {
    "streaming": false,
    "pushNotifications": false,
    "stateTransitionHistory": false
  },
  "defaultInputModes": ["text/plain"],
  "defaultOutputModes": ["text/plain", "application/json"],
  "skills": [ ... ]
}
```

> `url` 由 `a2a.base-url` 配置（env `A2A_BASE_URL`），未配置时回退 `localhost`（仅本地联调）。调用方应从 `supportedInterfaces` 读取接口与版本（A2A 1.0）。

`skills` 聚合了所有求解器暴露的技能（当前共 10 项：4 项 MPI + 3 项 Aerosim + 3 项 Mystran），每个技能包含 `id` / `name` / `description` / `tags` / `examples` / `inputModes` / `outputModes`，供上游应用发现与路由。

---

## 9. 完整调用示例

### 9.1 curl 示例

```bash
# 创建 MPI 作业（默认求解器，A2A 1.0）
curl -X POST http://<服务器地址>:8080/a2a \
  -H "Content-Type: application/a2a+json" \
  -H "X-API-Key: 1234567890" \
  -H "A2A-Version: 1.0" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "SendMessage",
    "params": {
      "message": {
        "role": "ROLE_USER",
        "parts": [{"text": "创建一个名为 test 的 MPI 作业，镜像 mpi:latest，命令 mpirun -np 4 ./app", "mediaType": "text/plain"}]
      },
      "metadata": { "solverType": "mpi" }
    }
  }'

# 创建 Aerosim 仿真求解任务
curl -X POST http://<服务器地址>:8080/a2a \
  -H "Content-Type: application/a2a+json" \
  -H "Authorization: Bearer 1234567890" \
  -H "A2A-Version: 1.0" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "SendMessage",
    "params": {
      "message": {
        "role": "ROLE_USER",
        "parts": [{"text": "为 benchmark-001 创建仿真求解任务，配置文件 http://10.113.16.247:9000/result/cabin_simulation.yaml，MinIO 桶 test", "mediaType": "text/plain"}]
      },
      "metadata": { "solverType": "aerosim" }
    }
  }'

# 查询任务状态（MPI 作业，传数字 Job ID）-> result.task
curl -X POST http://<服务器地址>:8080/a2a \
  -H "Content-Type: application/a2a+json" \
  -H "X-API-Key: 1234567890" \
  -H "A2A-Version: 1.0" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "GetTask",
    "params": { "id": "123" }
  }'

# 取消 Aerosim 求解任务（传 solverId）-> result 为裸 Task
curl -X POST http://<服务器地址>:8080/a2a \
  -H "Content-Type: application/a2a+json" \
  -H "X-API-Key: 1234567890" \
  -H "A2A-Version: 1.0" \
  -d '{
    "jsonrpc": "2.0",
    "id": "4",
    "method": "CancelTask",
    "params": { "id": "solver-c0ce0ee0-2d0" }
  }'

# 获取 AgentCard（免认证，根路径标准发现端点）
curl http://<服务器地址>:8080/.well-known/agent-card.json
```

### 9.2 Python 示例

```python
import requests

BASE_URL = "http://<服务器地址>:8080/a2a"
HEADERS = {
    "Content-Type": "application/a2a+json",
    "X-API-Key": "1234567890",
    "A2A-Version": "1.0"
}

def call(method, params, req_id="1"):
    payload = {"jsonrpc": "2.0", "id": req_id, "method": method, "params": params}
    resp = requests.post(BASE_URL, headers=HEADERS, json=payload, timeout=120)
    return resp.json()

# 1. 创建 MPI 作业（result.task 为返回的 Task）
result = call("SendMessage", {
    "message": {
        "role": "ROLE_USER",
        "parts": [{"text": "提交一个 8 核 16G 的 MPI 作业运行 fluid_solver", "mediaType": "text/plain"}]
    },
    "metadata": {"solverType": "mpi"}
}, req_id="1")
print(result["result"]["task"]["artifacts"][0]["parts"][0]["text"])

# 2. 查询任务状态（GetTask -> result.task）
status = call("GetTask", {"id": "123"}, req_id="2")
print(status["result"]["task"]["status"]["state"])  # TASK_STATE_*

# 3. 取消任务（CancelTask -> result 为裸 Task）
cancel = call("CancelTask", {"id": "123"}, req_id="3")
print(cancel["result"]["status"]["state"])
```

---

## 10. 注意事项

1. **求解器路由优先级**：`SendMessage` 通过 `metadata.solverType` 显式指定求解器；未指定时默认使用 `mpi`。建议上游应用根据意图显式传 `solverType`，避免误路由。
2. **任务 ID 复用**：`GetTask` 与 `CancelTask` 的 `id` 推荐直接使用 `SendMessage` 返回的 `Task.id`；若使用底层业务 ID，MPI 需传数字 Job ID，Aerosim 传 `solver-` 前缀的 solverId。
3. **`ListTasks` 占位**：当前返回空列表，列出作业请通过 `SendMessage` 让 LLM 调用列出工具完成。
4. **消息内容**：求解器仅消费 `parts` 中含有 `text` 字段的片段（A2A 1.0 TextPart，无需 `kind`），复杂结构化参数请在文本中自然语言描述，由 LLM 解析后调用对应工具。
5. **超时设置**：`SendMessage` 内部会调用 LLM 推理与 K8s 操作，建议客户端超时设置为 60–120 秒。
6. **响应模式**：当前 `capabilities.streaming=false`，不支持流式响应与推送通知，所有调用均为同步返回完整 `Task`。
7. **API Key 兼容**：见 [2.2 当前实现状态](#22-当前实现状态)，建议始终携带认证请求头。

---

## 11. 配置参考

A2A Server 相关配置（`application.yml` 的 `a2a` 段，均支持环境变量覆盖）：

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| `a2a.enabled` | `A2A_ENABLED` | `true` | 是否启用 A2A Server |
| `a2a.context-path` | `A2A_CONTEXT_PATH` | `/a2a` | 端点基础路径 |
| `a2a.base-url` | `A2A_BASE_URL` | 空（回退 localhost） | AgentCard.url / supportedInterfaces.url 的对外可达 Origin，**生产环境必须配置** |
| `a2a.auth-enabled` | `A2A_AUTH_ENABLED` | `true` | 是否启用 API Key 认证 |
| `a2a.api-key` | `A2A_API_KEY` | 复用 `EXTERNAL_API_KEY`（`1234567890`） | 认证 API Key |
| `a2a.system-user-id` | `A2A_SYSTEM_USER_ID` | `0` | 系统调用使用的用户 ID |
| `a2a.agent.name` | `A2A_AGENT_NAME` | `MPI Scheduling Agent` | Agent 名称 |
| `a2a.agent.description` | `A2A_AGENT_DESCRIPTION` | — | Agent 描述 |
| `a2a.agent.version` | — | `1.0.0` | Agent 版本 |
| `a2a.agent.protocolVersion` | — | `1.0` | A2A 协议版本 |

---

---

## 12. 第三方应用接入配置（metadataSchema 约束）

当第三方应用接入 A2A 接口，且 `metadata` 需遵循固定 schema（不含 `solverType`、`additionalProperties: false`）时，使用本节配置。完整机器可读配置见 [`a2a-client-config.json`](./a2a-client-config.json)。

### 12.1 metadataSchema

```json
{
  "type": "object",
  "properties": {
    "projectId": { "type": "string", "description": "当前项目编号" },
    "priority": { "type": "string", "enum": ["low", "normal", "high"], "description": "任务优先级" },
    "includeHistory": { "type": "boolean", "description": "是否包含历史上下文" }
  },
  "required": ["projectId"],
  "additionalProperties": false
}
```

对应的 `metadata` 取值示例：

```json
{
  "projectId": "PROJ-001",
  "priority": "normal",
  "includeHistory": false
}
```

### 12.2 约束影响（重要）

- **求解器路由**：该 schema 不含 `solverType` 且 `additionalProperties: false` 禁止额外字段，第三方无法通过 `metadata` 指定求解器，**所有请求将路由到默认求解器 `mpi`**。
- **文本不能切换求解器**：求解器路由发生在 LLM 推理之前，仅依据 `metadata.solverType`。在 `message.parts` 文本中写入 "aerosim" / "mystran" 等关键词**不会**触发对应求解器路由，仍由 `mpi` 求解器的 LLM 在 MPI 工具集内处理。如需调用 aerosim / mystran，须放宽 schema 允许 `solverType`，或由服务端基于 `projectId` 映射求解器（当前未实现）。
- **预留字段**：`projectId` / `priority` / `includeHistory` 当前被服务端接收但**未实现处理逻辑**，不影响作业调度与上下文行为；按 schema 传递即可向前兼容，待服务端后续版本支持。
- **认证**：与第 2 节一致，`POST /a2a` 建议携带 `X-API-Key` 或 `Authorization: Bearer` 请求头。

### 12.3 最小调用示例

```bash
curl -X POST http://<服务器地址>:8080/a2a \
  -H "Content-Type: application/a2a+json" \
  -H "X-API-Key: 1234567890" \
  -H "A2A-Version: 1.0" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "SendMessage",
    "params": {
      "message": {
        "role": "ROLE_USER",
        "parts": [{"text": "创建一个 8 核 16G 的 MPI 作业运行 fluid_solver", "mediaType": "text/plain"}]
      },
      "metadata": {
        "projectId": "PROJ-001",
        "priority": "normal",
        "includeHistory": false
      }
    }
  }'
```

---

**文档版本**: v1.2（A2A 1.0）
**最后更新**: 2026-07-20
