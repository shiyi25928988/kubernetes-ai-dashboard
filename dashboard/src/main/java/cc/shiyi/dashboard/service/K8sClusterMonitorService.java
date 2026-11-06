package cc.shiyi.dashboard.service;

import cc.shiyi.base.ai.config.ChantClientPool;
import cc.shiyi.message.service.SiteMessageService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * K8s 集群自动巡检：定期健康检查 + 站内告警；
 * 若开启 AI Agent 诊断，发现的问题会异步交给 AI 分析并以站内消息回传诊断结果。
 */
@Slf4j
@Service
@Setter(onMethod_ = @Autowired)
public class K8sClusterMonitorService {

    private KubernetesService kubernetesService;
    private SiteMessageService siteMessageService;
    private ChantClientPool chatClientPool;
    private ClusterInspectionConfigService inspectionConfigService;

    // 告警防抖时间窗口（分钟）
    private static final int ALERT_DEBOUNCE_MINUTES = 10;
    // AI 诊断防抖时间窗口（分钟）：同类告警的 AI 分析一次就够
    private static final int AI_DEBOUNCE_MINUTES = 60;
    // AI 诊断会话 ID：固定会话，巡检历史沉淀在该记忆中，越用越有上下文
    private static final String AI_INSPECTION_CONVERSATION_ID = "cluster-inspection";
    // AI 诊断执行的守护线程池：不阻塞巡检主流程
    private final ExecutorService aiDiagnosisExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "inspection-ai-diagnosis");
        thread.setDaemon(true);
        return thread;
    });

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

        // 开启了 AI Agent 诊断时，异步把该问题提交给 AI 分析
        requestAiDiagnosis(alertTitle, fullContent.toString());
    }

    /**
     * 将巡检发现的问题异步提交给 AI Agent 诊断，结果以站内消息形式广播。
     * 自带防抖：同类告警 1 小时内只诊断一次，避免刷屏。
     */
    private void requestAiDiagnosis(String alertTitle, String alertContent) {
        try {
            if (inspectionConfigService.getConfig().getAiAgentEnabled() == null
                    || inspectionConfigService.getConfig().getAiAgentEnabled() != 1) {
                return;
            }

            String diagnosisTitle = "[AI诊断] " + alertTitle;
            if (siteMessageService.hasRecentSimilarAlert(diagnosisTitle, AI_DEBOUNCE_MINUTES)) {
                log.info("AI diagnosis '{}' debounced (already diagnosed in last {} minutes)",
                        diagnosisTitle, AI_DEBOUNCE_MINUTES);
                return;
            }

            aiDiagnosisExecutor.submit(() -> runAiDiagnosis(diagnosisTitle, alertContent));
        } catch (Exception e) {
            log.error("Failed to schedule AI diagnosis", e);
        }
    }

    /**
     * 调用 AI 分析巡检问题（阻塞调用，运行在守护线程池里，不阻塞巡检）。
     */
    private void runAiDiagnosis(String diagnosisTitle, String alertContent) {
        try {
            log.info("Submitting cluster issue to AI agent for diagnosis: {}", diagnosisTitle);
            String analysis = chatClientPool.get().prompt()
                    .system("""
                            你是 Kubernetes 集群运维专家助手。平台自动巡检发现了以下集群问题。\
                            请输出：1) 问题影响分析；2) 可能的根因；3) 具体的处理建议（含可执行的命令或操作步骤）。\
                            使用中文，Markdown 格式，简洁清晰。
                            """)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, AI_INSPECTION_CONVERSATION_ID))
                    .user(alertContent)
                    .call()
                    .content();

            if (analysis != null && !analysis.isBlank()) {
                siteMessageService.broadcastMessage(diagnosisTitle, analysis, "INFO", 50);
                log.info("AI diagnosis completed for: {}", diagnosisTitle);
            }
        } catch (Exception e) {
            log.error("AI diagnosis failed for: {}", diagnosisTitle, e);
        }
    }
}
