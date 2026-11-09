package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.controller.A2AController;
import cc.shiyi.dashboard.dto.a2a.JsonRpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * A2AExceptionHandler
 * 仅作用于 {@link A2AController} 的异常处理，把请求体反序列化失败
 * （{@link HttpMessageNotReadableException}，发生在请求到达控制器方法之前）
 * 转换为 JSON-RPC 2.0 规范的 -32700 Parse error 响应。
 *
 * <p>不加该处理器时，畸形 JSON 会走全局兜底异常处理返回 HTTP 500，
 * 上游严格遵循 JSON-RPC 的调用方无法把它识别为解析错误。</p>
 *
 * <p>注意：A2AController 的两个 POST 端点（/a2a 与 /a2a/）的 @RequestBody
 * 反序列化异常都会归属到该控制器，因此两个路径均被覆盖。</p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = A2AController.class)
public class A2AExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonRpcResponse> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("A2A 请求体解析失败: {}", e.getMessage());
        // JSON-RPC 请求体本身损坏时无法获知请求 id，规范要求 id 为 null
        return ResponseEntity.ok(
                JsonRpcResponse.error(null, -32700, "Parse error: 请求体不是合法的JSON或缺少必需字段"));
    }
}
