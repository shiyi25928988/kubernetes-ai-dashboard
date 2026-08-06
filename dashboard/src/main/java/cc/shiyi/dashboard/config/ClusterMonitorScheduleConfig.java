package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.service.K8sClusterMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
public class ClusterMonitorScheduleConfig {

    @Autowired
    private K8sClusterMonitorService clusterMonitorService;

    /**
     * 定时执行集群健康检查
     * 每隔5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void scheduledClusterCheck() {
        log.info("Scheduled K8s cluster health check triggered...");
        try {
            clusterMonitorService.checkAndAlert();
        } catch (Exception e) {
            log.error("Error in scheduled cluster health check", e);
        }
    }
}
