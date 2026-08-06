# A2A 接入说明文档

> 本文档面向需要通过 **A2A (Agent-to-Agent) 协议**调用本平台 MPI 作业管理能力的上游 AI 应用开发者。

---

## 一、概述

本平台作为 A2A Server，将 MPI 并行计算作业的创建、查询、取消等管理能力，通过标准 A2A 协议（JSON-RPC 2.0 over HTTP）暴露给上游 AI 应用调用。

上游 AI 应用只需发送自然语言消息，平台内置的 LLM 会自动识别意图、提取参数，并调度对应工具完成操作（如创建 K8s MPIJob、查询状态等），无需调用方编写复杂的参数构造逻辑。

### 架构

```
上游 AI 应用 (A2A Client)
        │
        │  JSON-RPC 2.0 over HTTP
        ▼
┌─────────────────────────────────────────┐
│  本平台 A2A Server                        │
│  ┌───────────────┐  ┌─────────────────┐ │
│  │ API Key 认证   │→ │ JSON-RPC 分发    │ │
│  └───────────────┘  └────────┬────────┘ │
│                              │          │
│                   ┌──────────▼────────┐ │
│                   │ SolverProvider    │ │  求解器路由
│                   │ (MPI / 未来扩展)   │ │
│                   └──────────┬────────┘ │
│                              │          │
│                   ┌──────────▼────────┐ │
│                   │ ChatClient + LLM  │ │  意图识别 + 工具调度
│                   │ + @Tool 工具集     │ │
│                   └──────────┬────────┘ │
│                              │          │
│                   ┌──────────▼────────┐ │
│                   │ MpiJobService     │ │  作业生命周期管理
│                   │ KubernetesMpiSvc  │ │  K8s MPIJob 操作
│                   └───────────────────┘ │
└─────────────────────────────────────────┘
```

---

## 二、接入信息

| 项目 | 值 |
|------|-----|
| **服务地址** | `http://<host>:8080`（默认端口 8080） |
| **AgentCard 发现** | `GET /a2a/card` 或 `GET /a2a/.well-known/agent-card.json` |
| **JSON-RPC 端点** | `POST /a2a` |
| **协议版本** | A2A `1.0` / JSON-RPC `2.0` |
| **认证方式** | API Key（请求头 `X-API-Key` 或 `Authorization: Bearer <key>`） |
| **默认 API Key** | `1234567890`（可通过环境变量 `A2A_API_KEY` 或 `EXTERNAL_API_KEY` 覆盖） |
| **内容类型** | `application/json; charset=utf-8` |

### 认证说明

所有 `POST /a2a` 请求必须携带 API Key，二选一：

```http
X-API-Key: 1234567890
```

或

```http
Authorization: Bearer 1234567890
```

> `GET /a2a/card` 为发现端点，**免认证**，便于上游应用先探测 Agent 能力。

认证失败返回 `401`：
```json
{"error":"UNAUTHORIZED","message":"缺少 API Key，请在 Authorization 或 X-API-Key 请求头中提供"}
```

---

## 三、AgentCard 发现

上游应用通过 AgentCard 了解本 Agent 的能力与技能，用于路由决策。

### 请求

```bash
curl http://localhost:8080/a2a/card
```

### 响应

```json
{
  "name": "MPI Scheduling Agent",
  "description": "提供 MPI 并行计算作业的创建、查询、取消等管理服务，支持 Kubernetes 集群上的 MPIJob 调度",
  "url": "http://localhost:8080/a2a",
  "version": "1.0.0",
  "protocolVersion": "1.0",
  "capabilities": {
    "streaming": false,
    "pushNotifications": false,
    "stateTransitionHistory": false
  },
  "defaultInputModes": ["text"],
  "defaultOutputModes": ["text", "data"],
  "skills": [
    {
      "id": "mpi_job_create",
      "name": "创建MPI作业",
      "description": "根据用户描述创建 MPI 并行计算作业，支持自定义镜像、命令、Worker 数量、CPU/内存/GPU 资源等",
      "tags": ["mpi", "job", "create"],
      "examples": ["创建一个名为 test 的 MPI 作业，使用镜像 mpi:latest，命令 mpirun -np 4 ./app"]
    },
    {
      "id": "mpi_job_status",
      "name": "查询MPI作业状态",
      "description": "根据作业 ID 查询 MPI 作业的实时运行状态、Worker 进度、错误信息等",
      "tags": ["mpi", "job", "status"]
    },
    {
      "id": "mpi_job_list",
      "name": "列出MPI作业",
      "description": "分页查询 MPI 作业列表，可按状态过滤",
      "tags": ["mpi", "job", "list"]
    },
    {
      "id": "mpi_job_cancel",
      "name": "取消MPI作业",
      "description": "根据作业 ID 取消正在运行或排队的 MPI 作业",
      "tags": ["mpi", "job", "cancel"]
    }
  ]
}
```

---

## 四、JSON-RPC 接口

所有请求遵循 JSON-RPC 2.0 格式：

```json
{
  "jsonrpc": "2.0",
  "id": "<请求ID，用于响应关联>",
  "method": "<方法名>",
  "params": { ... }
}
```

### 支持的方法

| 方法 | 说明 | 是否调用 LLM |
|------|------|:---:|
| `message/send` | 发送自然语言消息，LLM 推理后调度工具完成操作 | ✅ |
| `tasks/get` | 查询任务（作业）状态 | ❌ |
| `tasks/cancel` | 取消任务（作业） | ❌ |
| `tasks/list` | 列出任务（占位，返回空列表） | ❌ |

> `message/send` 会经过 LLM 推理（有延迟，约数秒到数十秒）；`tasks/get`、`tasks/cancel` 直接操作，响应更快。

---

### 4.1 `message/send` — 自然语言操作

通过自然语言完成作业的创建、查询、列表、取消等操作。LLM 会自动识别意图并调用对应工具。

#### 请求参数

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "message/send",
  "params": {
    "message": {
      "role": "user",
      "parts": [
        { "kind": "text", "text": "<自然语言指令>" }
      ]
    },
    "metadata": {
      "solverType": "mpi",
      "contextId": "<可选，会话上下文ID>"
    }
  }
}
```

| 字段 | 必填 | 说明 |
|------|:---:|------|
| `message.role` | 是 | 固定 `user` |
| `message.parts[].kind` | 是 | 固定 `text` |
| `message.parts[].text` | 是 | 自然语言指令内容 |
| `metadata.solverType` | 否 | 求解器类型，默认 `mpi`。多求解器场景下用于路由 |
| `metadata.contextId` | 否 | 会话上下文 ID，未提供则自动生成 |

#### 示例：创建 MPI 作业

```bash
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "X-API-Key: 1234567890" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "message/send",
    "params": {
      "message": {
        "role": "user",
        "parts": [{"kind": "text", "text": "创建一个MPI作业，名称叫a2a-test-job，使用镜像nginx:latest，命令echo，参数hello-from-a2a，1个worker"}]
      }
    }
  }'
```

#### 响应

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "kind": "task",
    "id": "task-03e0cecd",
    "contextId": "ctx-cee436a4",
    "status": {
      "state": "COMPLETED",
      "timestamp": "2026-07-06T14:20:19.8279968"
    },
    "artifacts": [
      {
        "name": "mpi-solver-response",
        "index": 0,
        "parts": [
          {
            "kind": "text",
            "text": "MPI 作业已成功创建！以下是关键信息：\n\n| 项目 | 详情 |\n|------|------|\n| 作业 ID | 1 |\n| 作业名称 | a2a-test-job |\n| Kubernetes 作业名 | mpi-job-1-fb50ca67 |\n| 命名空间 | default |\n| 镜像 | nginx:latest |\n| 当前状态 | PENDING |\n\n你可以随时使用作业 ID 1 查询状态或日志。"
          }
        ]
      }
    ]
  }
}
```

> **说明**：`message/send` 返回的 `task.id` 是 A2A 会话任务 ID（`task-xxx` 格式），与数据库作业 ID 不同。LLM 会在响应文本中告知实际的作业 ID（如上例的 `1`），后续可用该 ID 通过 `tasks/get` 或再次 `message/send` 查询。

#### 更多自然语言示例

| 指令示例 | LLM 调用的工具 |
|---------|---------------|
| "列出当前所有的 MPI 作业" | `listMpiJobs` |
| "查询作业 1 的详细状态和错误信息" | `getMpiJob` / `getMpiJobStatus` |
| "取消作业 1" | `cancelMpiJob` |
| "获取作业 1 的运行日志" | `getMpiJobLogs` |
| "提交一个 8 核 16G 的 MPI 作业运行 fluid_solver" | `createMpiJob` |

---

### 4.2 `tasks/get` — 查询任务状态

直接查询作业的实时运行状态（从 Kubernetes 同步）。

#### 请求

```bash
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -H "X-API-Key: 1234567890" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tasks/get",
    "params": {"id": "1"}
  }'
```

> `params.id` 为作业 ID（数字字符串，如 `"1"`）。

#### 响应

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "kind": "task",
    "id": "1",
    "status": {
      "state": "WORKING",
      "timestamp": "2026-07-06T14:21:00.000000"
    },
    "artifacts": [
      {
        "name": "job-status",
        "index": 0,
        "parts": [
          {
            "kind": "data",
            "data": {
              "jobId": 1,
              "jobStatus": "RUNNING",
              "totalWorkers": 1,
              "errorMessage": ""
            }
          }
        ]
      }
    ]
  }
}
```

#### 作业状态映射

MPI Job 状态与 A2A TaskState 的对应关系：

| MPI Job 状态 | A2A TaskState | 含义 |
|-------------|---------------|------|
| `PENDING` | `SUBMITTED` | 已提交，等待调度 |
| `RUNNING` | `WORKING` | 运行中 |
| `SUCCEEDED` | `COMPLETED` | 已完成 |
| `FAILED` | `FAILED` | 失败 |
| `CANCELLED` | `CANCELED` | 已取消 |

---

### 4.3 `tasks/cancel` — 取消任务

取消指定作业，会删除 K8s 中的 MPIJob 资源并标记为已取消。

#### 请求

```bash
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -H "X-API-Key: 1234567890" \
  -d '{
    "jsonrpc": "2.0",
    "id": "3",
    "method": "tasks/cancel",
    "params": {"id": "1"}
  }'
```

#### 响应

```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "result": {
    "kind": "task",
    "id": "1",
    "status": {
      "state": "CANCELED",
      "timestamp": "2026-07-06T14:21:06.3802611"
    },
    "artifacts": [
      {
        "name": "cancel-result",
        "index": 0,
        "parts": [{"kind": "text", "text": "作业 1 已取消"}]
      }
    ]
  }
}
```

---

### 4.4 `tasks/list` — 列出任务

> 当前为占位实现，返回空列表。作业列表查询请使用 `message/send` 发送"列出所有作业"。

#### 请求

```bash
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -H "X-API-Key: 1234567890" \
  -d '{"jsonrpc":"2.0","id":"4","method":"tasks/list"}'
```

#### 响应

```json
{
  "jsonrpc": "2.0",
  "id": "4",
  "result": {"tasks": []}
}
```

---

## 五、错误处理

### JSON-RPC 错误响应

错误通过 `error` 字段返回（与 `result` 互斥）：

```json
{
  "jsonrpc": "2.0",
  "id": "<请求ID>",
  "error": {
    "code": -32601,
    "message": "Method not found: 不支持的方法 xxx"
  }
}
```

### 错误码

| 错误码 | 含义 | 触发场景 |
|-------|------|---------|
| `-32600` | Invalid Request | 请求格式错误，如 `method` 为空 |
| `-32601` | Method not found | 不支持的 JSON-RPC 方法 |
| `-32602` | Invalid params | 参数缺失或非法，如 `message` 为空 |
| `-32603` | Internal error | 服务端内部异常 |
| `401` (HTTP) | Unauthorized | API Key 缺失或无效 |

### message/send 中的业务错误

`message/send` 即使工具执行失败，HTTP 层面仍返回 `200`，错误信息封装在 Task 的 artifact 文本中，`status.state` 为 `COMPLETED`（LLM 已处理）或 `FAILED`（处理过程异常）：

```json
{
  "result": {
    "status": {"state": "FAILED"},
    "artifacts": [{"parts": [{"kind": "text", "text": "处理失败: ..."}]}]
  }
}
```

---

## 六、完整调用流程示例

以下演示上游 AI 应用从发现到完成作业管理的完整流程：

```bash
# 步骤 1：发现 Agent 能力（免认证）
curl http://localhost:8080/a2a/card

# 步骤 2：通过自然语言创建作业
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json; charset=utf-8" \
  -H "X-API-Key: 1234567890" \
  -d '{"jsonrpc":"2.0","id":"1","method":"message/send","params":{"message":{"role":"user","parts":[{"kind":"text","text":"创建MPI作业test-job，镜像mpi:latest，命令mpirun，参数-np 4 ./app，2个worker，每worker 2核4G"}]}}}'
# → 从响应文本中获取作业 ID（假设为 1）

# 步骤 3：查询作业实时状态
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -H "X-API-Key: 1234567890" \
  -d '{"jsonrpc":"2.0","id":"2","method":"tasks/get","params":{"id":"1"}}'

# 步骤 4：取消作业
curl -X POST http://localhost:8080/a2a \
  -H "Content-Type: application/json" \
  -H "X-API-Key: 1234567890" \
  -d '{"jsonrpc":"2.0","id":"3","method":"tasks/cancel","params":{"id":"1"}}'
```

---

## 七、求解器扩展说明

本平台采用 `SolverProvider` 接口设计，当前已实现 MPI 求解器，后续将扩展更多求解器类型（如 CFD、结构力学、电磁仿真等）。

### 多求解器路由

当存在多个求解器时，通过 `message/send` 的 `metadata.solverType` 指定目标求解器：

```json
{
  "method": "message/send",
  "params": {
    "message": {"role": "user", "parts": [{"kind": "text", "text": "..."}]},
    "metadata": {"solverType": "cfd"}
  }
}
```

未指定 `solverType` 时，默认使用第一个注册的求解器（当前为 `mpi`）。

### 当前支持的求解器

| solverType | 说明 | 技能数 |
|-----------|------|:-----:|
| `mpi` | MPI 并行计算作业管理 | 4 |

> 新增求解器后会自动注册到 AgentCard 的 skills 列表，上游应用可通过 `GET /a2a/card` 动态发现。

---

## 八、注意事项

1. **编码**：请求体请使用 UTF-8 编码（`Content-Type: application/json; charset=utf-8`），尤其是包含中文时。
2. **LLM 延迟**：`message/send` 经过 LLM 推理，建议客户端超时设置 ≥ 60 秒。
3. **作业 ID**：`message/send` 返回的 `task.id` 是会话任务 ID（`task-xxx`），实际作业 ID 在响应文本中；`tasks/get` 和 `tasks/cancel` 的 `params.id` 使用实际作业 ID（数字）。
4. **K8s 依赖**：作业创建/状态查询依赖 Kubernetes 集群可用，集群不可达时作业会标记为 `FAILED`。
5. **并发**：作业创建有分布式锁保护（10 秒过期），高并发场景下请处理锁冲突。

---

## 九、相关文档

- 设计方案：[`# 现有系统增加 A2A Server 改造方案.md`](#%20%E7%8E%B0%E6%9C%89%E7%B3%BB%E7%BB%9F%E5%A2%9E%E5%8A%A0%20A2A%20Server%20%E6%94%B9%E9%80%A0%E6%96%B9%E6%A1%88.md)
- 实施计划：[`plans/a2a-server-implementation.md`](plans/a2a-server-implementation.md)
- A2A 协议规范：[a2aproject/A2A](https://github.com/a2aproject/A2A)
- JSON-RPC 2.0 规范：[jsonrpc.org/specification](https://www.jsonrpc.org/specification)
