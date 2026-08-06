package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * SendMessageResponse
 * A2A 1.0 SendMessage 方法的响应结果体（JSON-RPC result）。
 * 包含 task 或 message 之一。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageResponse {

    /**
     * 返回的任务对象
     */
    private Task task;

    /**
     * 返回的消息对象（可选，用于直接消息交互场景）
     */
    private Message message;

    public SendMessageResponse() {
    }

    public SendMessageResponse(Task task) {
        this.task = task;
    }
}
