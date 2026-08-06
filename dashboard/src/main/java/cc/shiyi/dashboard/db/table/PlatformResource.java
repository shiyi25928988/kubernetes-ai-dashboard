package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("platform_resource")
public class PlatformResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Resource type: Namespace, Deployment, Service, Ingress, PVC, ConfigMap, Secret, Job, MPIJob */
    private String resourceType;

    /** Resource name in K8S */
    private String resourceName;

    /** K8S namespace (empty string for cluster-scoped resources) */
    private String namespace;

    /** Creator user ID from system_user table */
    private Long creatorId;

    /** Record creation time */
    private LocalDateTime createdTime;
}
