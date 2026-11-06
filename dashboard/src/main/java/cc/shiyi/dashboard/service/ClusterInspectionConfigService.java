package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.mapper.ClusterInspectionConfigMapper;
import cc.shiyi.dashboard.db.table.ClusterInspectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 集群自动巡检设置：读取/保存单例行（id=1）。
 * 注意：本服务只负责持久化，保存成功后的定时任务重排由调用方
 * （SiteMessageController）触发 {@link ClusterMonitorScheduleConfig#reschedule()}，
 * 避免两个 Bean 相互注入形成循环依赖。
 */
@Slf4j
@Service
public class ClusterInspectionConfigService {

    /** 巡检间隔上下限（分钟）：1 分钟 ~ 24 小时 */
    public static final int MIN_INTERVAL_MINUTES = 1;
    public static final int MAX_INTERVAL_MINUTES = 1440;

    private final ClusterInspectionConfigMapper configMapper;

    public ClusterInspectionConfigService(ClusterInspectionConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 读取设置（保证单例行存在，不存在则补建默认行）。
     */
    public ClusterInspectionConfig getConfig() {
        ClusterInspectionConfig config = configMapper.selectById(1L);
        if (config == null) {
            config = createDefaultRow();
        }
        return config;
    }

    /**
     * 更新设置（仅持久化；重排定时任务由调用方联动）。
     *
     * @return 保存后的设置；间隔非法抛 IllegalArgumentException
     */
    public synchronized ClusterInspectionConfig updateConfig(ClusterInspectionConfig update, Long userId) {
        int interval = update.getIntervalMinutes() == null ? 0 : update.getIntervalMinutes();
        if (interval < MIN_INTERVAL_MINUTES || interval > MAX_INTERVAL_MINUTES) {
            throw new IllegalArgumentException("巡检间隔必须在 " + MIN_INTERVAL_MINUTES + " ~ "
                    + MAX_INTERVAL_MINUTES + " 分钟之间");
        }

        // 保证行存在
        ClusterInspectionConfig config = configMapper.selectById(1L);
        if (config == null) {
            config = createDefaultRow();
        }

        config.setEnabled(isTrue(update.getEnabled()) ? 1 : 0);
        config.setIntervalMinutes(interval);
        config.setAiAgentEnabled(isTrue(update.getAiAgentEnabled()) ? 1 : 0);
        config.setUpdatedBy(userId);
        config.setUpdatedTime(LocalDateTime.now());
        configMapper.updateById(config);

        log.info("Cluster inspection config updated by user {}: enabled={}, interval={}min, aiAgent={}",
                userId, config.getEnabled(), config.getIntervalMinutes(), config.getAiAgentEnabled());
        return config;
    }

    private boolean isTrue(Integer value) {
        return value != null && value == 1;
    }

    private ClusterInspectionConfig createDefaultRow() {
        ClusterInspectionConfig config = new ClusterInspectionConfig();
        config.setId(1L);
        config.setEnabled(0);
        config.setIntervalMinutes(5);
        config.setAiAgentEnabled(0);
        config.setUpdatedTime(LocalDateTime.now());
        try {
            configMapper.insert(config);
        } catch (Exception e) {
            // 并发插入唯一主键时可能冲突，回读一次
            log.warn("Insert default inspection config failed, fallback to read", e);
        }
        ClusterInspectionConfig existing = configMapper.selectById(1L);
        return existing != null ? existing : config;
    }
}
