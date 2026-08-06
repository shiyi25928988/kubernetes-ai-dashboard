package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * GetTaskResponse
 * A2A 1.0 GetTask 方法的响应结果体（JSON-RPC result）。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetTaskResponse {

    /**
     * 查询到的任务对象
     */
    private Task task;

    public GetTaskResponse() {
    }

    public GetTaskResponse(Task task) {
        this.task = task;
    }
}
