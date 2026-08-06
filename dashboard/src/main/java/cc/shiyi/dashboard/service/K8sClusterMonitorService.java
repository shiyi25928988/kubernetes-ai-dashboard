package cc.shiyi.dashboard.service;

import cc.shiyi.message.service.SiteMessageService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@Setter(onMethod_ = @Autowired)
public class K8sClusterMonitorService {

    private KubernetesService kubernetesService;
    private SiteMessageService siteMessageService;

    // 告警防抖时间窗口（分钟）
    private static final int ALERT_DEBOUNCE_MINUTES = 10;

    /**
     * 执行集群健康检查并发送告警
     */
    public void checkAndAlert() {
        log.info("Starting K8s cluster health check...");

        try {
            Map<String, Object> clusterStats = kubernetesService.getClusterStats();

            // 检查节点状态
            checkNodeHealth(clusterStats);

            // 检查Pod状态
            checkPodHealth(clusterStats);

            // 检查资源使用率
            checkResourceUsage(clusterStats);

            // 检查存储空间
            checkStorageUsage(clusterStats);

            log.info("K8s cluster health check completed.");
        } catch (Exception e) {
            log.error("Error during K8s cluster health check", e);
            sendAlert("集群巡检异常", "K8S",
                    "巡检任务执行失败：" + e.getMessage(), "ERROR", 100);
        }
    }

    /**
     * 检查节点健康状态
     */
    @SuppressWarnings("unchecked")
    private void checkNodeHealth(Map<String, Object> clusterStats) {
        Map<String, Object> nodeStats = (Map<String, Object>) clusterStats.get("nodes");
        long totalNodes = (Long) nodeStats.get("total");
        long readyNodes = (Long) nodeStats.get("ready");

        if (readyNodes == 0) {
            sendAlert("严重：集群无可用节点", "K8S_NODE",
                    String.format("当前集群没有任何可用节点！总节点数：%d", totalNodes),
                    "ERROR", 100);
        } else if (readyNodes < totalNodes) {
            sendAlert("警告：集群节点异常", "K8S_NODE",
                    String.format("集群节点异常！总节点数：%d，就绪节点：%d，异常节点：%d",
                            totalNodes, readyNodes, totalNodes - readyNodes),
                    "WARNING", 80);
        }
    }

    /**
     * 检查Pod健康状态
     */
    @SuppressWarnings("unchecked")
    private void checkPodHealth(Map<String, Object> clusterStats) {
        Map<String, Object> workloadStats = (Map<String, Object>) clusterStats.get("workloads");
        long abnormalPods = (Long) workloadStats.get("podsAbnormal");

        if (abnormalPods > 5) {
            sendAlert("警告：集群异常Pod数量较多", "K8S_POD",
                    String.format("当前集群有 %d 个Pod处于异常状态（非Running/Succeeded/Pending）",
                            abnormalPods),
                    "WARNING", 60);
        } else if (abnormalPods > 0) {
            log.info("There are {} abnormal pods in the cluster", abnormalPods);
        }
    }

    /**
     * 检查资源使用率
     */
    @SuppressWarnings("unchecked")
    private void checkResourceUsage(Map<String, Object> clusterStats) {
        Map<String, Object> resourceStats = (Map<String, Object>) clusterStats.get("resources");
        Integer cpuUsage = getIntegerValue(resourceStats.get("cpuUsage"));
        Integer memoryUsage = getIntegerValue(resourceStats.get("memoryUsage"));

        if (cpuUsage != null && cpuUsage > 90) {
            sendAlert("严重：集群CPU使用率过高", "K8S_RESOURCE",
                    String.format("集群CPU使用率已达 %d%%，请及时扩容或优化工作负载！", cpuUsage),
                    "ERROR", 90);
        } else if (cpuUsage != null && cpuUsage > 80) {
            sendAlert("警告：集群CPU使用率过高", "K8S_RESOURCE",
                    String.format("集群CPU使用率已达 %d%%，请注意关注！", cpuUsage),
                    "WARNING", 70);
        }

        if (memoryUsage != null && memoryUsage > 90) {
            sendAlert("严重：集群内存使用率过高", "K8S_RESOURCE",
                    String.format("集群内存使用率已达 %d%%，请及时扩容或优化工作负载！", memoryUsage),
                    "ERROR", 90);
        } else if (memoryUsage != null && memoryUsage > 80) {
            sendAlert("警告：集群内存使用率过高", "K8S_RESOURCE",
                    String.format("集群内存使用率已达 %d%%，请注意关注！", memoryUsage),
                    "WARNING", 70);
        }
    }

    /**
     * 检查存储使用率
     */
    @SuppressWarnings("unchecked")
    private void checkStorageUsage(Map<String, Object> clusterStats) {
        Map<String, Object> resourceStats = (Map<String, Object>) clusterStats.get("resources");
        Integer storageUsage = getIntegerValue(resourceStats.get("storageUsage"));

        if (storageUsage != null && storageUsage > 90) {
            sendAlert("严重：集群存储使用率过高", "K8S_STORAGE",
                    String.format("集群存储使用率已达 %d%%，请及时清理或扩容存储！", storageUsage),
                    "ERROR", 90);
        } else if (storageUsage != null && storageUsage > 80) {
            sendAlert("警告：集群存储使用率较高", "K8S_STORAGE",
                    String.format("集群存储使用率已达 %d%%，请注意关注！", storageUsage),
                    "WARNING", 70);
        }
    }

    /**
     * 安全获取整数值（兼容 Long 和 Integer）
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 发送告警消息（含防抖机制）
     */
    private void sendAlert(String title, String category, String content, String type, int priority) {
        String alertTitle = "[" + category + "] " + title;

        // 防抖检查：避免短期内重复发送相同告警
        if (siteMessageService.hasRecentSimilarAlert(alertTitle, ALERT_DEBOUNCE_MINUTES)) {
            log.info("Alert '{}' debounced (already sent in last {} minutes)",
                    alertTitle, ALERT_DEBOUNCE_MINUTES);
            return;
        }

        // 构建完整告警内容
        StringBuilder fullContent = new StringBuilder();
        fullContent.append(content).append("\n\n");
        fullContent.append("告警级别：").append(type).append("\n");
        fullContent.append("优先级：").append(priority).append("\n");
        fullContent.append("检测时间：").append(java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        siteMessageService.broadcastMessage(alertTitle, fullContent.toString(), type, priority);
    }
}
