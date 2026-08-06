package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * JsonRpcError
 * JSON-RPC 2.0 错误对象。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误附加数据
     */
    private Object data;

    public JsonRpcError() {
    }

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
