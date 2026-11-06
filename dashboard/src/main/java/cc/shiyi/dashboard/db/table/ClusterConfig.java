package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * K8s 集群 kubeconfig 配置（页面上传，存 MySQL）。
 * 可上传多个集群，但同时只有一个激活（active = 1）。
 */
@Data
@TableName("cluster_config")
public class ClusterConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 集群显示名，唯一 */
    private String clusterName;

    /** kubeconfig YAML 内容 */
    private String content;

    /** 是否为当前激活集群：1 = 激活 */
    private Integer active;

    /** 备注说明 */
    private String remark;

    /** 上传用户 ID */
    private Long creatorId;

    /** 上传时间 */
    private LocalDateTime createdTime;

    /** 最后更新时间 */
    private LocalDateTime updatedTime;
}
