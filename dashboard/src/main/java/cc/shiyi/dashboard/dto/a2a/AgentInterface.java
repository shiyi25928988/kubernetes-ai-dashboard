package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * AgentInterface
 * A2A 1.0 AgentCard 中声明的单个协议接口。
 * 调用方按 supportedInterfaces 数组顺序选择第一个可用接口。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentInterface {

    /**
     * 接口端点 URL
     */
    private String url;

    /**
     * 协议绑定：JSONRPC / GRPC / HTTP+JSON 等
     */
    private String protocolBinding;

    /**
     * 该接口暴露的 A2A 协议版本，如 "1.0"、"0.3"
     */
    private String protocolVersion;

    /**
     * 租户 ID（可选）
     */
    private String tenant;
}
