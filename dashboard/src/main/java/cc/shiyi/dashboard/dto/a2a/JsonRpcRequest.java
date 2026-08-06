package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * JsonRpcRequest
 * JSON-RPC 2.0 请求体，遵循 A2A 协议的 JSON-RPC 传输规范。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {

    /**
     * 协议版本，固定为 "2.0"
     */
    private String jsonrpc;

    /**
     * 方法名（如 message/send、tasks/get、tasks/cancel）
     */
    private String method;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 请求 ID，用于响应关联
     */
    private Object id;
}
