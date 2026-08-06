package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.advisor.SolverProvider;
import cc.shiyi.dashboard.dto.a2a.AgentCapabilities;
import cc.shiyi.dashboard.dto.a2a.AgentCard;
import cc.shiyi.dashboard.dto.a2a.AgentInterface;
import cc.shiyi.dashboard.dto.a2a.AgentSkill;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * A2A Server 配置
 * 创建 AgentCard Bean，聚合所有 SolverProvider 的技能，作为 A2A 网络中的 Agent 名片。
 *
 * <p>当 a2a.enabled=true 时生效。AgentCard 通过 GET /a2a/card 暴露给上游 AI 应用发现。</p>
 *
 * <p>扩展机制：新增求解器只需实现 {@link SolverProvider} 并注册为 Spring Bean，
 * 本配置会自动发现并聚合其技能到 AgentCard，无需修改此处代码。</p>
 */
@Slf4j
@Configuration
@Setter(onMethod_ = @Autowired)
@ConditionalOnProperty(prefix = "a2a", name = "enabled", havingValue = "true", matchIfMissing = true)
public class A2AServerConfig {

    A2AProperties a2aProperties;

    /**
     * 所有求解器提供者（Spring 自动注入所有实现；无实现时注入空列表）
     */
    @Setter(AccessLevel.NONE)
    @Autowired(required = false)
    List<SolverProvider> solverProviders;

    @Setter(AccessLevel.NONE)
    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * AgentCard Bean —— Agent 在 A2A 网络中的名片。
     * 聚合所有 SolverProvider 的技能，供上游 AI 应用发现与路由。
     */
    @Bean
    public AgentCard agentCard() {
        A2AProperties.AgentConfig agentConfig = a2aProperties.getAgent();

        // 聚合所有 SolverProvider 的技能
        List<AgentSkill> allSkills = new ArrayList<>();
        if (solverProviders != null) {
            for (SolverProvider provider : solverProviders) {
                List<AgentSkill> skills = provider.getSkills();
                if (skills != null) {
                    allSkills.addAll(skills);
                }
                log.info("A2A 注册求解器: type={}, skills={}", provider.getSolverType(),
                        skills != null ? skills.size() : 0);
            }
        }

        AgentCard card = new AgentCard();
        card.setName(agentConfig.getName());
        card.setDescription(agentConfig.getDescription());

        // A2A 1.0：通过 supportedInterfaces 声明协议接口；同时保留顶层 url 兼容 0.3 客户端
        String endpointUrl = buildEndpointUrl();
        card.setUrl(endpointUrl);
        AgentInterface iface = new AgentInterface();
        iface.setUrl(endpointUrl);
        iface.setProtocolBinding("JSONRPC");
        iface.setProtocolVersion(agentConfig.getProtocolVersion());
        card.setSupportedInterfaces(List.of(iface));

        card.setVersion(agentConfig.getVersion());
        card.setProtocolVersion(agentConfig.getProtocolVersion());

        AgentCapabilities capabilities = new AgentCapabilities();
        capabilities.setStreaming(false);
        capabilities.setPushNotifications(false);
        capabilities.setStateTransitionHistory(false);
        card.setCapabilities(capabilities);

        // A2A 1.0：输入输出模式用媒体类型表示
        card.setDefaultInputModes(List.of("text/plain"));
        card.setDefaultOutputModes(List.of("text/plain", "application/json"));
        card.setSkills(allSkills);

        log.info("A2A AgentCard 初始化完成: name={}, skills总数={}", card.getName(), allSkills.size());
        return card;
    }

    /**
     * 构建 AgentCard.url（外部可达的服务端点）。
     * <p>优先使用 a2a.base-url（外部可达地址）；未配置时回退到 localhost + server.port，
     * 仅适用于本地联调，并打告警日志提醒部署方显式配置，否则外部调用方拿到名片后无法回调。</p>
     */
    private String buildEndpointUrl() {
        String baseUrl = a2aProperties.getBaseUrl();
        if (baseUrl != null && !baseUrl.isBlank()) {
            // 去掉末尾斜杠，避免与 contextPath 拼接出双斜杠
            return baseUrl.replaceAll("/+$", "") + a2aProperties.getContextPath();
        }
        log.warn("a2a.base-url 未配置，AgentCard.url 回退为 localhost，外部调用将失败。" +
                "请通过 A2A_BASE_URL 环境变量配置外部可达地址");
        return "http://localhost:" + serverPort + a2aProperties.getContextPath();
    }
}
