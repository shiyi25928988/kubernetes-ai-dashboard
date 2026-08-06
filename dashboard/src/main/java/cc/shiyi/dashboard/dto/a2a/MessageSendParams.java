package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * MessageSendParams
 * A2A message/send 与 message/stream 方法的参数体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageSendParams {

    /**
     * 消息对象
     */
    private Message message;

    /**
     * 配置项（可选）
     */
    private java.util.Map<String, Object> configuration;

    /**
     * 元数据（可选）
     */
    private java.util.Map<String, Object> metadata;
}
