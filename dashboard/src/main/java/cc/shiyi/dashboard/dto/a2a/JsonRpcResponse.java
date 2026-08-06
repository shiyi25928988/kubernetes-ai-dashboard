package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * JsonRpcResponse
 * JSON-RPC 2.0 响应体，包含 result 或 error 之一。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {

    /**
     * 协议版本，固定为 "2.0"
     */
    private String jsonrpc = "2.0";

    /**
     * 请求 ID，与请求中的 id 对应
     */
    private Object id;

    /**
     * 成功结果（与 error 互斥）
     */
    private Object result;

    /**
     * 错误信息（与 result 互斥）
     */
    private JsonRpcError error;

    public JsonRpcResponse() {
    }

    /**
     * 构造成功响应
     */
    public static JsonRpcResponse success(Object id, Object result) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(id);
        response.setResult(result);
        return response;
    }

    /**
     * 构造错误响应
     */
    public static JsonRpcResponse error(Object id, int code, String message) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(id);
        response.setError(new JsonRpcError(code, message));
        return response;
    }

    /**
     * 构造错误响应（带附加数据）
     */
    public static JsonRpcResponse error(Object id, int code, String message, Object data) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(id);
        response.setError(new JsonRpcError(code, message, data));
        return response;
    }
}
