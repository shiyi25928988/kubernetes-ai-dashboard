package cc.shiyi.dashboard.controller;

import cc.shiyi.dashboard.config.A2AProperties;
import cc.shiyi.dashboard.config.A2ARequestHandler;
import cc.shiyi.dashboard.dto.a2a.AgentCard;
import cc.shiyi.dashboard.dto.a2a.JsonRpcRequest;
import cc.shiyi.dashboard.dto.a2a.JsonRpcResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A2A Server 控制器
 * 暴露 A2A 协议端点，供上游 AI 应用通过 JSON-RPC 调用。
 *
 * <p>端点：</p>
 * <ul>
 *   <li>GET  /a2a/card                      → 返回 AgentCard（Agent 名片）</li>
 *   <li>GET  /a2a/.well-known/agent-card.json → AgentCard（标准发现路径）</li>
 *   <li>POST /a2a                            → JSON-RPC 2.0 端点（message/send、tasks/get 等）</li>
 * </ul>
 *
 * <p>认证：通过 A2A 认证过滤器校验 API Key，复用 external.api-key 配置。</p>
 */
@Slf4j
@RestController
@Tag(name = "A2A Server", description = "Agent-to-Agent 协议端点，供上游 AI 应用调用")
@ConditionalOnProperty(prefix = "a2a", name = "enabled", havingValue = "true", matchIfMissing = true)
@Setter(onMethod_ = @Autowired)
public class A2AController {

    AgentCard agentCard;
    A2ARequestHandler requestHandler;
    A2AProperties a2aProperties;

    /**
     * 获取 AgentCard（便捷路径）
     */
    @Operation(summary = "获取 AgentCard", description = "返回 Agent 在 A2A 网络中的名片，包含能力与技能描述")
    @GetMapping("/a2a/card")
    public ResponseEntity<AgentCard> getAgentCard() {
        log.info("A2A 获取 AgentCard");
        return ResponseEntity.ok(agentCard);
    }

    /**
     * 获取 AgentCard（A2A 标准发现路径，根路径）
     * <p>A2A 规范的标准 well-known 路径为 {@code /.well-known/agent-card.json}（服务器根），
     * 供不支持 contextPath 前缀的调用方发现。同时保留 {@code /a2a/.well-known/agent-card.json}。</p>
     */
    @Operation(summary = "获取 AgentCard（标准发现路径·根）", description = "A2A 协议标准发现路径（服务器根）")
    @GetMapping("/.well-known/agent-card.json")
    public ResponseEntity<AgentCard> getAgentCardWellKnownRoot() {
        return ResponseEntity.ok(agentCard);
    }

    /**
     * 获取 AgentCard（A2A 标准发现路径，带 contextPath 前缀）
     */
    @Operation(summary = "获取 AgentCard（标准发现路径）", description = "A2A 协议标准发现路径")
    @GetMapping("/a2a/.well-known/agent-card.json")
    public ResponseEntity<AgentCard> getAgentCardWellKnown() {
        return ResponseEntity.ok(agentCard);
    }

    /**
     * A2A JSON-RPC 端点
     * 接收 JSON-RPC 2.0 请求，支持 message/send、tasks/get、tasks/cancel、tasks/list 方法。
     *
     * <p>请求示例：</p>
     * <pre>{@code
     * POST /a2a
     * {
     *   "jsonrpc": "2.0",
     *   "id": "1",
     *   "method": "message/send",
     *   "params": {
     *     "message": {
     *       "role": "user",
     *       "parts": [{"kind": "text", "text": "创建一个MPI作业..."}]
     *     }
     *   }
     * }
     * }</pre>
     */
    @Operation(summary = "A2A JSON-RPC 端点", description = "接收 JSON-RPC 2.0 请求，分发到对应求解器")
    @PostMapping("/a2a")
    public ResponseEntity<JsonRpcResponse> handleJsonRpc(@RequestBody JsonRpcRequest request) {
        log.info("A2A 收到 JSON-RPC 请求: method={}", request.getMethod());
        JsonRpcResponse response = requestHandler.handle(request);
        return ResponseEntity.ok(response);
    }

    /**
     * A2A 根路径端点（兼容 POST /a2a/ 带尾斜杠）
     */
    @PostMapping("/a2a/")
    public ResponseEntity<JsonRpcResponse> handleJsonRpcTrailingSlash(@RequestBody JsonRpcRequest request) {
        return handleJsonRpc(request);
    }
}
