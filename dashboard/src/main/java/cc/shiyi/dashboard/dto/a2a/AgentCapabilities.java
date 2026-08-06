package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * AgentCapabilities
 * 描述 Agent 支持的能力，遵循 A2A 协议规范。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentCapabilities {

    /**
     * 是否支持流式响应
     */
    private Boolean streaming;

    /**
     * 是否支持推送通知
     */
    private Boolean pushNotifications;

    /**
     * 是否支持状态转换历史
     */
    private Boolean stateTransitionHistory;
}
