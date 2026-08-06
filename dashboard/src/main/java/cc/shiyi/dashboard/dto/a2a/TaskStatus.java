package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * TaskStatus
 * A2A 任务的状态信息，包含状态枚举、时间戳与关联消息。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatus {

    /**
     * 任务状态
     */
    private TaskState state;

    /**
     * 状态更新时间戳（ISO-8601）
     */
    private String timestamp;

    /**
     * 关联消息（可选）
     */
    private Message message;
}
