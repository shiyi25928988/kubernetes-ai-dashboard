package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.db.mapper.ClusterConfigMapper;
import cc.shiyi.dashboard.db.table.ClusterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kubernetes Client 动态管理器。
 * 客户端从 cluster_config 表中"激活"的 kubeconfig 构建，支持运行时切换集群：
 * 激活新集群后调用 {@link #reload()}，下次 {@link #getClient()} 按新配置重建。
 * 替代旧的 kubernetes.client.config-url / config-path 静态配置方式。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KubernetesClientManager {

    private final ClusterConfigMapper clusterConfigMapper;

    @Value("${kubernetes.client.trust-certs:true}")
    boolean trustCerts;

    private volatile KubernetesClient client;

    /**
     * 获取当前激活集群的客户端。首次调用或 reload 后按激活的 kubeconfig 惰性构建。
     *
     * @throws IllegalStateException 没有已激活的集群配置，或 kubeconfig 无效
     */
    public KubernetesClient getClient() {
        KubernetesClient c = client;
        if (c == null) {
            synchronized (this) {
                if (client == null) {
                    client = build();
                }
                c = client;
            }
        }
        return c;
    }

    /** 关闭当前客户端并置空，下次获取时按最新激活配置重建 */
    public synchronized void reload() {
        KubernetesClient old = client;
        client = null;
        if (old != null) {
            old.close();
            log.info("Closed previous Kubernetes client, will rebuild from active cluster config");
        }
    }

    private KubernetesClient build() {
        ClusterConfig active = clusterConfigMapper.selectOne(new LambdaQueryWrapper<ClusterConfig>()
                .eq(ClusterConfig::getActive, 1)
                .last("LIMIT 1"));
        if (active == null) {
            throw new IllegalStateException("没有已激活的K8s集群配置，请先在集群配置页面上传并激活kubeconfig");
        }
        Config config = Config.fromKubeconfig(active.getContent());
        config.setTrustCerts(trustCerts);
        log.info("Initialized Kubernetes client for cluster '{}' (id={})", active.getClusterName(), active.getId());
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
