package cc.shiyi.dashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A2AProperties
 * A2A Server 配置属性，对应 application.yml 中的 a2a 配置段。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "a2a")
public class A2AProperties {

    /**
     * 是否启用 A2A Server
     */
    private boolean enabled = true;

    /**
     * A2A 端点的基础路径
     */
    private String contextPath = "/a2a";

    /**
     * AgentCard 对外暴露的服务端点 Origin（scheme + host[:port]，不含路径）。
     * <p>该地址会被写入 AgentCard.url 供上游 AI 应用回调，必须为外部可达地址，
     * 不能用 localhost，否则外部调用方拿到名片后会去连自己的本机。</p>
     * <p>留空时回退为 http://localhost:{server.port}（仅适用于本地联调）。</p>
     */
    private String baseUrl = "";

    /**
     * A2A 认证 API Key（复用 external.api-key，也可单独配置）
     */
    private String apiKey = "";

    /**
     * 是否启用 API Key 认证
     */
    private boolean authEnabled = true;

    /**
     * A2A 系统调用使用的用户 ID（用于建立 Sa-Token 登录上下文）
     */
    private Long systemUserId = 0L;

    /**
     * Agent 卡片配置
     */
    private AgentConfig agent = new AgentConfig();

    /**
     * Agent 卡片配置
     */
    @Data
    public static class AgentConfig {
        /**
         * Agent 名称
         */
        private String name = "MPI Scheduling Agent";

        /**
         * Agent 描述
         */
        private String description = "提供 MPI 并行计算作业的创建、查询、取消等管理服务，支持 Kubernetes 集群上的 MPIJob 调度";

        /**
         * Agent 版本
         */
        private String version = "1.0.0";

        /**
         * A2A 协议版本
         */
        private String protocolVersion = "1.0";
    }
}
