# A2A `SendMessage` Method Not Found 排查报告

## 1. 问题概述

前端通过当前协调 Agent 调用远程 `solver` Agent 时失败，后端收到的错误为：

```text
A2A solver send-message failed (invalid-argument, JSONRPC 1.0, http://10.35.79.185:8080/a2a): Method not found: 不支持的方法 SendMessage
```

本次调用输入中同时包含了业务消息和 `metadata`：

```json
{
  "message": "这是一个测试调用，请创建一个简单的MPI Hello World作业用于验证A2A通信是否正常。",
  "metadata": {
    "priority": "normal",
    "projectId": "test-project-a2a-2024",
    "includeHistory": false
  }
}
```

## 2. 结论

该错误的高概率原因是：

> 被调用方实际支持的 A2A JSON-RPC 方法与其 Agent Card 声明的协议版本不一致。

当前调用方根据 Agent Card 选择了 `JSONRPC 1.0`，因此发送了 A2A 1.0 要求的 `SendMessage`。目标服务已经返回了 JSON-RPC 层的“方法不存在”，说明 HTTP 请求已经到达目标接口，但目标服务的 JSON-RPC 方法分派器没有注册或不支持 `SendMessage`。

这不是 `metadata` 校验错误。错误发生在目标服务查找 RPC 方法的阶段，还没有进入 `SendMessage` 参数解析或业务处理阶段。

## 3. 协议依据

A2A 官方协议对不同版本的 JSON-RPC 方法名不同：

| A2A 版本 | 发送消息方法 | 方法命名形式 |
| --- | --- | --- |
| 1.0 | `SendMessage` | PascalCase |
| 0.3 | `message/send` | `category/action` |

A2A 1.0 规范要求 JSON-RPC 请求使用 `SendMessage`，并通过 `A2A-Version: 1.0` 表示协议版本。A2A 0.3 规范使用 `message/send`。

- [A2A 最新规范与 JSON-RPC 方法映射](https://github.com/a2aproject/A2A/blob/main/docs/specification.md)
- [A2A 0.3 规范](https://a2a-protocol.org/v0.3.0/specification/)
- [A2A 官方 JavaScript SDK](https://github.com/a2aproject/a2a-js)

## 4. 当前项目的调用行为

当前项目依赖的 SDK 版本为：

```json
{
  "@a2a-js/sdk": "1.0.0-beta.0"
}
```

### 4.1 Agent Card 接口选择

项目会从 Agent Card 的 `supportedInterfaces` 中按顺序选择第一个可用的 HTTP 接口，并读取其：

- `url`
- `protocolBinding`
- `protocolVersion`

相关实现位于 [agent-card-resolver.ts](/home/ogawamuko/workbench/ai-assistant/backend/src/a2a/agent-card-resolver.ts:282)。如果目标 Card 声明如下：

```json
{
  "supportedInterfaces": [
    {
      "url": "http://10.35.79.185:8080/a2a",
      "protocolBinding": "JSONRPC",
      "protocolVersion": "1.0"
    }
  ]
}
```

调用方就会按 A2A 1.0 JSON-RPC 方式调用该 URL。

### 4.2 SendMessage 请求

项目通过 SDK 的 `client.sendMessage(...)` 发起调用，相关代码位于 [manager.ts](/home/ogawamuko/workbench/ai-assistant/backend/src/a2a/manager.ts:86)。在 A2A 1.0 JSON-RPC 模式下，发送的方法是：

```json
{
  "jsonrpc": "2.0",
  "id": "request-id",
  "method": "SendMessage",
  "params": {
    "message": {
      "messageId": "message-id",
      "role": "ROLE_USER",
      "parts": [
        {
          "text": "...",
          "mediaType": "text/plain"
        }
      ]
    },
    "configuration": {
      "acceptedOutputModes": ["text/plain"]
    },
    "metadata": {
      "priority": "normal",
      "projectId": "test-project-a2a-2024",
      "includeHistory": false
    }
  }
}
```

`metadata` 的写入逻辑位于 [manager.ts](/home/ogawamuko/workbench/ai-assistant/backend/src/a2a/manager.ts:347)。因此，当前错误不能证明 `metadata` 没有发送，也不能归因于 `metadata` 字段类型错误。

## 5. 责任边界判断

### 5.1 最可能：目标服务或目标 Agent Card

如果调用日志中的 Agent Card 来源为 `discovery`，并且 Card 声明 `JSONRPC 1.0`，则问题主要在被调用方，常见情况包括：

1. 目标服务实际是 A2A 0.3，只支持 `message/send`，但 Card 错误声明成了 1.0。
2. 目标 Card 返回的是新版本 Card，但 `supportedInterfaces[].url` 仍然指向旧版服务或旧版路由。
3. 目标服务已升级 Card，但服务端 JSON-RPC handler 没有注册 `SendMessage`。
4. 目标服务使用的 AWS 或其他 SDK 版本仍然实现 A2A 0.3 方法映射。
5. 反向代理将请求转发到了一个不支持 A2A 1.0 方法的旧服务。

### 5.2 可能：本地 fallback 配置

如果日志显示 Agent Card 来源为 `fallback`，则需要检查本地配置是否手工声明了错误的协议版本。例如，fallback Card 声明了：

```json
{
  "protocolBinding": "JSONRPC",
  "protocolVersion": "1.0"
}
```

但目标服务实际上只接受 `message/send`。这种情况下属于调用方配置错误，而不是目标服务实现错误。

### 5.3 当前客户端的限制

如果 Agent Card 同时声明多个接口，当前项目选择第一个可用接口。当前运行时调用失败后不会自动切换到后续接口。相关选择逻辑位于 [agent-card-resolver.ts](/home/ogawamuko/workbench/ai-assistant/backend/src/a2a/agent-card-resolver.ts:288)。

不过，按照 A2A 协议，Agent Card 声明的接口都应当提供相应能力，因此这属于客户端容错能力不足，不是本次 `SendMessage` 方法不存在的首要原因。

## 6. 为什么日志显示 `invalid-argument`

当前日志中的：

```text
(invalid-argument, JSONRPC 1.0, ...)
```

是本项目对 SDK 异常类型进行的本地分类，不一定等于远程服务返回的真实错误类别。

当前分类器会把部分 `RequestMalformedError` 或 `InvalidParamsError` 归类为 `invalid-argument`，实现位于 [diagnostics.ts](/home/ogawamuko/workbench/ai-assistant/backend/src/a2a/diagnostics.ts:90)。某些 SDK 会将远端 JSON-RPC 错误统一包装为参数类异常，因此外层分类可能显示为 `invalid-argument`。

本次真正有诊断价值的错误原因是：

```text
Method not found: 不支持的方法 SendMessage
```

更准确的分类应当是“协议方法不兼容”或“目标服务端未实现声明的方法”。

## 7. 建议的验证步骤

### 7.1 查看实际获取到的 Agent Card

从运行协调后端的同一环境执行：

```bash
curl --fail-with-body --connect-timeout 5 --max-time 10 \
  -H 'Accept: application/json' \
  http://10.35.79.185:8080/.well-known/agent-card.json
```

重点检查：

```text
supportedInterfaces[].url
supportedInterfaces[].protocolBinding
supportedInterfaces[].protocolVersion
```

并确认日志中的 Card 来源：

```text
source: discovery
```

还是：

```text
source: fallback
```

### 7.2 确认 A2A 1.0 方法

在测试环境向目标服务发送最小化的 A2A 1.0 JSON-RPC 请求：

```bash
curl --fail-with-body --connect-timeout 5 --max-time 30 \
  -X POST http://10.35.79.185:8080/a2a \
  -H 'Content-Type: application/json' \
  -H 'A2A-Version: 1.0' \
  --data '{
    "jsonrpc": "2.0",
    "id": "compatibility-probe-v1",
    "method": "SendMessage",
    "params": {
      "message": {
        "messageId": "compatibility-probe-message-v1",
        "role": "ROLE_USER",
        "parts": [{"text": "A2A compatibility probe", "mediaType": "text/plain"}]
      },
      "configuration": {"acceptedOutputModes": ["text/plain"]}
    }
  }'
```

如果返回 `Method not found`，则目标服务没有提供与 Card 一致的 A2A 1.0 JSON-RPC 方法。

### 7.3 确认 A2A 0.3 方法

仅在测试环境执行，并根据对方 SDK 要求调整消息结构：

```bash
curl --fail-with-body --connect-timeout 5 --max-time 30 \
  -X POST http://10.35.79.185:8080/a2a \
  -H 'Content-Type: application/json' \
  -H 'A2A-Version: 0.3' \
  --data '{
    "jsonrpc": "2.0",
    "id": "compatibility-probe-v03",
    "method": "message/send",
    "params": {
      "message": {
        "messageId": "compatibility-probe-message-v03",
        "role": "user",
        "parts": [{"kind": "text", "text": "A2A compatibility probe"}],
        "kind": "message"
      }
    }
  }'
```

如果 `message/send` 成功，而 `SendMessage` 失败，则可以确认目标服务是 A2A 0.3 实现或兼容模式，Agent Card 的协议声明需要修正。

## 8. 修复建议

### 8.1 推荐修复：修正目标服务的 Agent Card

如果目标服务只支持 A2A 0.3，应让 SDK 生成或返回正确的 0.3 Agent Card，并声明其真实的 JSON-RPC 接口和版本。

如果目标服务要声明 A2A 1.0，则必须同时确保：

- `supportedInterfaces[].protocolBinding` 为 `JSONRPC`。
- `supportedInterfaces[].protocolVersion` 为 `1.0`。
- 对应 URL 接受 JSON-RPC 方法 `SendMessage`。
- 请求使用 `A2A-Version: 1.0`。
- 服务端参数解析器支持 A2A 1.0 的消息结构和 `metadata`。

### 8.2 临时方案：强制使用 A2A 0.3

如果已确认目标服务只支持 `message/send`，可以暂时绕过自动发现，使用完整的 0.3 fallback Card，确保协议版本、接口地址和服务端能力一致。

该方案只适合目标服务明确支持 0.3 的情况。不能把一个实际只支持 0.3 的服务继续声明为 1.0，否则后续任务查询、流式调用等方法仍可能失败。

### 8.3 客户端后续改进

当前项目可以进一步改进：

1. 将 JSON-RPC `-32601` 或 `Method not found` 归类为 `protocol`，而不是 `invalid-argument`。
2. 在 Agent Card 解析成功日志中明确记录最终选中的接口和版本。
3. 当第一个接口返回方法不存在或版本不支持时，尝试 Agent Card 中后续兼容接口。
4. 在错误消息中同时输出远端声明版本、实际发送方法和 `A2A-Version`，减少跨语言 SDK 排查成本。

## 9. 当前环境限制

本次从当前 WSL 环境尝试访问：

```text
http://10.35.79.185:8080/.well-known/agent-card.json
```

连接失败，未能直接读取目标 Agent Card。该地址属于内网地址，可能只在实际部署网络、VPN、宿主机网络或特定 Docker 网络中可访问。因此最终确认仍需在运行协调后端的实际网络环境中执行第 7 节的验证命令。

## 10. 最终判断

基于当前错误内容，可以确定：

- 请求已经到达目标 A2A HTTP 接口。
- 失败发生在 JSON-RPC 方法分派阶段。
- `metadata` 不是本次错误的直接原因。
- 调用方按 Agent Card 选择 `JSONRPC 1.0` 并发送 `SendMessage` 的行为符合 A2A 1.0 规范。
- 最需要优先检查的是目标服务的 Agent Card、SDK版本、JSON-RPC handler 注册情况以及代理转发的实际后端版本。

如果目标服务仅支持 `message/send`，则应修正目标 Agent Card 为 0.3 或升级目标服务以实现 A2A 1.0 的 `SendMessage`。
