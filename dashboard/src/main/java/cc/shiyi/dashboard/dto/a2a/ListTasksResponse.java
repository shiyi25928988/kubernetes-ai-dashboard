package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * ListTasksResponse
 * A2A 1.0 ListTasks 方法的响应结果体（JSON-RPC result）。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListTasksResponse {

    /**
     * 任务列表
     */
    private List<Task> tasks;

    /**
     * 总数
     */
    private Integer totalSize;

    /**
     * 当前页大小
     */
    private Integer pageSize;

    /**
     * 下一页游标（无更多数据时为空串）
     */
    private String nextPageToken;

    public ListTasksResponse() {
    }

    public ListTasksResponse(List<Task> tasks, Integer totalSize, Integer pageSize, String nextPageToken) {
        this.tasks = tasks;
        this.totalSize = totalSize;
        this.pageSize = pageSize;
        this.nextPageToken = nextPageToken;
    }
}
