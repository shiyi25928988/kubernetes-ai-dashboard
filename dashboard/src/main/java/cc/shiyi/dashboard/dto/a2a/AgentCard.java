package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * AgentCard
 * Agent 在 A2A 网络中的名片，描述 Agent 的元数据与能力。
 * 通过 GET /.well-known/agent-card.json 或 GET /a2a/card 暴露。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentCard {

    /**
     * Agent 名称
     */
    private String name;

    /**
     * Agent 描述
     */
    private String description;

    /**
     * Agent 服务端点 URL（0.3 风格顶层字段，保留以兼容旧客户端）
     */
    private String url;

    /**
     * 支持的协议接口列表（A2A 1.0 风格）。
     * 调用方按顺序选择第一个可用接口，读取其 url/protocolBinding/protocolVersion。
     */
    private List<AgentInterface> supportedInterfaces;

    /**
     * Agent 版本
     */
    private String version;

    /**
     * A2A 协议版本
     */
    private String protocolVersion;

    /**
     * Agent 能力描述
     */
    private AgentCapabilities capabilities;

    /**
     * 默认输入模式（如 text）
     */
    private List<String> defaultInputModes;

    /**
     * 默认输出模式（如 text）
     */
    private List<String> defaultOutputModes;

    /**
     * Agent 提供的技能列表
     */
    private List<AgentSkill> skills;
}
