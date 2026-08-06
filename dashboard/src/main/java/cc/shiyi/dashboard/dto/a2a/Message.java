package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Message
 * A2A 协议中的消息对象，包含角色与消息内容片段。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    /**
     * 消息角色：user / agent
     */
    private String role;

    /**
     * 消息内容片段列表
     */
    private List<Part> parts;

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 会话上下文 ID
     */
    private String contextId;

    /**
     * 关联的任务 ID
     */
    private String taskId;
}
