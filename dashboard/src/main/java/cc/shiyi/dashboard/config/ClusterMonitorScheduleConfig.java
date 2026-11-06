package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.db.table.ClusterInspectionConfig;
import cc.shiyi.dashboard.service.ClusterInspectionConfigService;
import cc.shiyi.dashboard.service.K8sClusterMonitorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

/**
 * 集群自动巡检定时调度：替代原先硬编码 @Scheduled(fixedRate=5min) 的方案，
 * 改为按 {@link ClusterInspectionConfig} 中的开关与间隔动态注册任务，
 * 设置变更时可通过 {@link #reschedule()} 立即重排，无需重启应用。
 */
@Slf4j
@Component
public class ClusterMonitorScheduleConfig {

    /** 启动后首巡延迟 */
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(60);
    /** 配置变更后的首巡延迟（快速生效，便于验证） */
    private static final Duration RESCHEDULE_DELAY = Duration.ofSeconds(10);

    private final ClusterInspectionConfigService configService;
    private final K8sClusterMonitorService monitorService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private ScheduledFuture<?> scheduledTask;

    public ClusterMonitorScheduleConfig(ClusterInspectionConfigService configService,
                                        K8sClusterMonitorService monitorService) {
        this.configService = configService;
        this.monitorService = monitorService;
        scheduler.setThreadNamePrefix("cluster-inspection-");
        scheduler.setPoolSize(1);
        scheduler.initialize();
    }

    /**
     * 启动时按数据库中的设置装载定时巡检。
     */
    @PostConstruct
    public void init() {
        try {
            schedule(INITIAL_DELAY);
        } catch (Exception e) {
            log.error("Failed to schedule cluster inspection", e);
        }
    }

    /**
     * 按当前设置重排任务：开启则注册新的巡检任务，关闭则取消现有任务。
     *配置变更后首巡延迟 10 秒，便于快速验证设置是否生效。
     */
    public synchronized void reschedule() {
        if (scheduledTask != null) {
            // 说明是设置变更（而非启动装载）
            cancel();
            schedule(RESCHEDULE_DELAY);
            return;
        }
        // 启动装载阶段未注册任何任务时也走统一入口
        cancel();
        schedule(RESCHEDULE_DELAY);
    }

    private synchronized void schedule(Duration initialDelay) {
        cancel();

        ClusterInspectionConfig config = configService.getConfig();
        int intervalMinutes = config.getIntervalMinutes() == null ? 5 : config.getIntervalMinutes();

        if (config.getEnabled() == null || config.getEnabled() != 1) {
            log.info("Cluster auto inspection is disabled.");
            return;
        }

        scheduledTask = scheduler.scheduleAtFixedRate(
                this::runSafely,
                Instant.now().plus(initialDelay),
                Duration.ofMinutes(intervalMinutes));
        log.info("Cluster auto inspection scheduled: interval={}min, first run in {}s",
                intervalMinutes, initialDelay.toSeconds());
    }

    private synchronized void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    private void runSafely() {
        try {
            monitorService.checkAndAlert();
        } catch (Exception e) {
            log.error("Error in scheduled cluster health check", e);
        }
    }
}
