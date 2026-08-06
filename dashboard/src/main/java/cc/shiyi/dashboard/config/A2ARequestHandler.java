package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.advisor.SolverProvider;
import cc.shiyi.dashboard.dto.a2a.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A2ARequestHandler
 * A2A JSON-RPC 请求处理器，负责将 JSON-RPC 方法分发到对应的 SolverProvider。
 *
 * <p>支持的 JSON-RPC 方法（遵循 A2A 协议规范）：</p>
 * <ul>
 *   <li><b>message/send</b>：发送消息，执行求解逻辑，返回 Task</li>
 *   <li><b>tasks/get</b>：查询任务状态</li>
 *   <li><b>tasks/cancel</b>：取消任务</li>
 *   <li><b>tasks/list</b>：列出任务（返回空列表占位，实际作业查询通过 message/send 调用 listMpiJobs 工具）</li>
 * </ul>
 *
 * <p>方法名兼容：除 A2A 规范 v0.2+ 的斜杠命名（message/send）外，同时接受 camelCase（sendMessage）
 * 与早期 Google A2A SDK v0.1 的 PascalCase（SendMessage）别名，查找时大小写无关。
 * 见 {@link #METHOD_ALIASES}。</p>
 *
 * <p>求解器路由策略：</p>
 * <ol>
 *   <li>优先根据 message.metadata.solverType 匹配 SolverProvider</li>
 *   <li>未指定时使用默认（第一个）SolverProvider</li>
 * </ol>
 */
@Slf4j
@Component
@Setter(onMethod_ = @Autowired)
public class A2ARequestHandler {

    List<SolverProvider> solverProviders;
    ObjectMapper objectMapper;

    /**
     * JSON-RPC 方法名别名表（key 已统一转为小写）。
     * <p>兼容三种命名风格，查找时大小写无关：</p>
     * <ul>
     *   <li>A2A 规范 v0.2+ 斜杠命名：{@code message/send}、{@code tasks/get} …</li>
     *   <li>camelCase 别名：{@code sendMessage}、{@code getTask} …</li>
     *   <li>早期 Google A2A SDK v0.1 PascalCase：{@code SendMessage}、{@code GetTask} …</li>
     * </ul>
     */
    private static final Map<String, String> METHOD_ALIASES;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("message/send", "message/send");
        m.put("sendmessage", "message/send");
        m.put("tasks/get", "tasks/get");
        m.put("gettask", "tasks/get");
        m.put("tasks/cancel", "tasks/cancel");
        m.put("canceltask", "tasks/cancel");
        m.put("tasks/list", "tasks/list");
        m.put("listtasks", "tasks/list");
        METHOD_ALIASES = Collections.unmodifiableMap(m);
    }

    /**
     * 处理 JSON-RPC 请求
     *
     * @param request JSON-RPC 请求
     * @return JSON-RPC 响应
     */
    public JsonRpcResponse handle(JsonRpcRequest request) {
        if (request == null || request.getMethod() == null) {
            return JsonRpcResponse.error(null, -32600, "Invalid Request: method 为空");
        }

        String method = request.getMethod().trim();
        String canonical = METHOD_ALIASES.get(method.toLowerCase(Locale.ROOT));
        if (canonical == null) {
            return JsonRpcResponse.error(request.getId(), -32601,
                    "Method not found: 不支持的方法 " + method);
        }

        try {
            log.info("A2A 处理 JSON-RPC 请求: method={}, canonical={}, id={}", method, canonical, request.getId());
            if (log.isDebugEnabled()) {
                log.debug("A2A 请求原始 params: {}", request.getParams());
            }

            return switch (canonical) {
                case "message/send" -> handleMessageSend(request);
                case "tasks/get" -> handleTasksGet(request);
                case "tasks/cancel" -> handleTasksCancel(request);
                case "tasks/list" -> handleTasksList(request);
                default -> JsonRpcResponse.error(request.getId(), -32601,
                        "Method not found: 不支持的方法 " + method);
            };
        } catch (Exception e) {
            log.error("A2A 处理请求异常: method={}", method, e);
            return JsonRpcResponse.error(request.getId(), -32603,
                    "Internal error: " + e.getMessage());
        }
    }

    /**
     * 处理 message/send 方法
     */
    private JsonRpcResponse handleMessageSend(JsonRpcRequest request) {
        MessageSendParams params = parseParams(request.getParams(), MessageSendParams.class);
        if (params == null || params.getMessage() == null) {
            return JsonRpcResponse.error(request.getId(), -32602,
                    "Invalid params: message 不能为空");
        }

        // 从 metadata 中提取会话上下文 ID
        String contextId = extractContextId(params.getMetadata(), params.getMessage());

        // 路由到合适的 SolverProvider
        SolverProvider provider = routeSolverProvider(params.getMetadata());
        if (provider == null) {
            return JsonRpcResponse.error(request.getId(), -32603,
                    "No solver provider available: 无可用求解器");
        }

        Task task = provider.processMessage(params.getMessage(), contextId);
        // A2A 1.0：SendMessage 响应 result 为 SendMessageResponse（包装在 task 字段下）
        return JsonRpcResponse.success(request.getId(), new SendMessageResponse(task));
    }

    /**
     * 处理 tasks/get 方法
     */
    private JsonRpcResponse handleTasksGet(JsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        String taskId = params != null ? (String) params.get("id") : null;
        if (taskId == null) {
            return JsonRpcResponse.error(request.getId(), -32602,
                    "Invalid params: id 不能为空");
        }

        SolverProvider provider = routeByTaskId(taskId);
        if (provider == null) {
            return JsonRpcResponse.error(request.getId(), -32603,
                    "No solver provider available");
        }

        Task task = provider.getTask(taskId);
        // A2A 1.0：GetTask 响应 result 为 GetTaskResponse（包装在 task 字段下）
        return JsonRpcResponse.success(request.getId(), new GetTaskResponse(task));
    }

    /**
     * 处理 tasks/cancel 方法
     */
    private JsonRpcResponse handleTasksCancel(JsonRpcRequest request) {
        Map<String, Object> params = request.getParams();
        String taskId = params != null ? (String) params.get("id") : null;
        if (taskId == null) {
            return JsonRpcResponse.error(request.getId(), -32602,
                    "Invalid params: id 不能为空");
        }

        SolverProvider provider = routeByTaskId(taskId);
        if (provider == null) {
            return JsonRpcResponse.error(request.getId(), -32603,
                    "No solver provider available");
        }

        Task task = provider.cancelTask(taskId);
        // A2A 1.0：CancelTask 响应 result 直接为 Task（规范方法表明确，无 CancelTaskResponse）
        return JsonRpcResponse.success(request.getId(), task);
    }

    /**
     * 处理 tasks/list 方法
     * 返回空列表占位；实际作业查询通过 message/send 调用 listMpiJobs 工具完成。
     */
    private JsonRpcResponse handleTasksList(JsonRpcRequest request) {
        // A2A 1.0：ListTasks 响应 result 为 ListTasksResponse
        return JsonRpcResponse.success(request.getId(),
                new ListTasksResponse(Collections.emptyList(), 0, 0, ""));
    }

    /**
     * 路由到合适的 SolverProvider
     * 优先根据 metadata.solverType 匹配，未指定时使用默认（第一个）
     */
    private SolverProvider routeSolverProvider(Map<String, Object> metadata) {
        if (solverProviders == null || solverProviders.isEmpty()) {
            return null;
        }

        // 优先按 solverType 匹配
        if (metadata != null) {
            Object solverType = metadata.get("solverType");
            if (solverType != null) {
                String type = solverType.toString();
                for (SolverProvider provider : solverProviders) {
                    if (type.equalsIgnoreCase(provider.getSolverType())) {
                        return provider;
                    }
                }
                log.warn("未找到 solverType={} 的 SolverProvider，使用默认", type);
            }
        }

        // 默认使用第一个
        return solverProviders.get(0);
    }

    /**
     * 根据 taskId 格式路由到合适的 SolverProvider（用于 tasks/get、tasks/cancel）
     * <p>不同求解器的 taskId 格式不同：</p>
     * <ul>
     *   <li>纯数字 → MPI 求解器（taskId 即 Job ID）</li>
     *   <li>solver- 前缀 → Aerosim 求解器（taskId 即 Solver ID）</li>
     *   <li>其他 → 默认第一个</li>
     * </ul>
     */
    private SolverProvider routeByTaskId(String taskId) {
        if (solverProviders == null || solverProviders.isEmpty()) {
            return null;
        }
        if (taskId != null) {
            if (taskId.startsWith("mystran-")) {
                return findProviderByType("mystran");
            }
            if (taskId.startsWith("solver-")) {
                return findProviderByType("aerosim");
            }
            if (taskId.matches("\\d+")) {
                return findProviderByType("mpi");
            }
        }
        return solverProviders.get(0);
    }

    /**
     * 按 solverType 查找 SolverProvider，未找到则返回第一个
     */
    private SolverProvider findProviderByType(String solverType) {
        for (SolverProvider provider : solverProviders) {
            if (solverType.equalsIgnoreCase(provider.getSolverType())) {
                return provider;
            }
        }
        return solverProviders.get(0);
    }

    /**
     * 从 metadata 或 message 中提取会话上下文 ID
     */
    private String extractContextId(Map<String, Object> metadata, Message message) {
        if (metadata != null && metadata.get("contextId") != null) {
            return metadata.get("contextId").toString();
        }
        if (message.getContextId() != null) {
            return message.getContextId();
        }
        return "ctx-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 解析 JSON-RPC params 为指定类型
     */
    private <T> T parseParams(Map<String, Object> params, Class<T> clazz) {
        if (params == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(params, clazz);
        } catch (IllegalArgumentException e) {
            log.error("解析 params 失败: {}", e.getMessage());
            return null;
        }
    }
}
