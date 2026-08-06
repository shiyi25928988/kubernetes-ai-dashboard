package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Task
 * A2A 协议的任务对象，是 message/send、tasks/get 等方法的核心返回结构。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {

    /**
     * 对象类型标识，固定为 "task"
     */
    private String kind = "task";

    /**
     * 任务 ID
     */
    private String id;

    /**
     * 会话上下文 ID
     */
    private String contextId;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 任务产物列表
     */
    private List<Artifact> artifacts;

    /**
     * 历史消息（可选）
     */
    private List<Message> history;

    /**
     * 元数据（可选）
     */
    private java.util.Map<String, Object> metadata;
}
