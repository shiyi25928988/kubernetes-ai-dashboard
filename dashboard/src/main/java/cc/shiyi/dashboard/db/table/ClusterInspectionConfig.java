package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集群自动巡检设置（单例行 id=1）。
 * 控制定时巡检是否开启、巡检间隔、以及发现问题后是否交给 AI Agent 诊断。
 */
@Data
@TableName("cluster_inspection_config")
public class ClusterInspectionConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 自动巡检是否开启：1 = 开启 */
    private Integer enabled;

    /** 巡检间隔（分钟），取值 1-1440 */
    private Integer intervalMinutes;

    /** 发现问题后是否提交给 AI Agent 诊断：1 = 开启 */
    private Integer aiAgentEnabled;

    /** 最后更新用户 ID */
    private Long updatedBy;

    /** 最后更新时间 */
    private LocalDateTime updatedTime;
}
