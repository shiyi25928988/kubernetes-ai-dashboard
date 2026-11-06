package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.mapper.ClusterConfigMapper;
import cc.shiyi.dashboard.db.table.ClusterConfig;
import cc.shiyi.dashboard.config.KubernetesClientManager;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.fabric8.kubernetes.client.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * K8s 集群配置管理：页面上传 kubeconfig 存 MySQL，多集群并存、单集群激活。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClusterConfigService {

    private final ClusterConfigMapper clusterConfigMapper;

    private final KubernetesClientManager kubernetesClientManager;

    /**
     * 上传 kubeconfig。内容会用 fabric8 解析校验，不合法直接拒绝。
     */
    public ClusterConfig upload(String clusterName, String content, String remark, Long creatorId) {
        if (clusterName == null || clusterName.isBlank()) {
            throw new IllegalArgumentException("集群名称不能为空");
        }
        // 去掉可能的 BOM / 前后空白
        String yaml = content == null ? "" : content.strip();
        if (yaml.isEmpty()) {
            throw new IllegalArgumentException("kubeconfig 内容不能为空");
        }
        try {
            Config.fromKubeconfig(yaml);
        } catch (Exception e) {
            throw new IllegalArgumentException("kubeconfig 文件格式无效: " + e.getMessage());
        }
        Long count = clusterConfigMapper.selectCount(new LambdaQueryWrapper<ClusterConfig>()
                .eq(ClusterConfig::getClusterName, clusterName));
        if (count > 0) {
            throw new IllegalArgumentException("集群名称已存在: " + clusterName);
        }

        ClusterConfig entity = new ClusterConfig();
        entity.setClusterName(clusterName);
        entity.setContent(yaml);
        entity.setActive(0);
        entity.setRemark(remark == null ? "" : remark);
        entity.setCreatorId(creatorId);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        clusterConfigMapper.insert(entity);
        log.info("Uploaded kubeconfig for cluster '{}' (id={}) by user {}", clusterName, entity.getId(), creatorId);
        return mask(entity);
    }

    /**
     * 集群列表（不含 kubeconfig 内容）。
     */
    public List<ClusterConfig> list() {
        List<ClusterConfig> list = clusterConfigMapper.selectList(
                new LambdaQueryWrapper<ClusterConfig>().orderByDesc(ClusterConfig::getActive)
                        .orderByAsc(ClusterConfig::getClusterName));
        list.forEach(this::mask);
        return list;
    }

    /**
     * 获取当前激活集群（不含内容）。
     */
    public ClusterConfig getActive() {
        ClusterConfig active = clusterConfigMapper.selectOne(new LambdaQueryWrapper<ClusterConfig>()
                .eq(ClusterConfig::getActive, 1).last("LIMIT 1"));
        return active == null ? null : mask(active);
    }

    /**
     * 激活指定集群（互斥：先清掉其它激活标记），并重建 Kubernetes 客户端。
     */
    @Transactional
    public ClusterConfig activate(Long id) {
        ClusterConfig target = requireExists(id);
        // 触发一次解析校验，激活前先确认 kubeconfig 可用
        try {
            Config.fromKubeconfig(target.getContent());
        } catch (Exception e) {
            throw new IllegalStateException("该kubeconfig无法解析，激活失败: " + e.getMessage());
        }
        clusterConfigMapper.update(null, new LambdaUpdateWrapper<ClusterConfig>()
                .set(ClusterConfig::getActive, 0));
        clusterConfigMapper.update(null, new LambdaUpdateWrapper<ClusterConfig>()
                .set(ClusterConfig::getActive, 1)
                .eq(ClusterConfig::getId, id));
        kubernetesClientManager.reload();
        log.info("Activated cluster '{}' (id={})", target.getClusterName(), id);
        target.setActive(1);
        return mask(target);
    }

    /**
     * 删除集群配置。当前激活的集群不允许删除，需先切换。
     */
    @Transactional
    public void delete(Long id) {
        ClusterConfig target = requireExists(id);
        if (target.getActive() != null && target.getActive() == 1) {
            throw new IllegalStateException("该集群当前处于激活状态，请先激活其它集群后再删除");
        }
        clusterConfigMapper.deleteById(id);
        log.info("Deleted cluster config '{}' (id={})", target.getClusterName(), id);
    }

    private ClusterConfig requireExists(Long id) {
        ClusterConfig c = clusterConfigMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("集群配置不存在: id=" + id);
        }
        return c;
    }

    /** 不对外返回 kubeconfig 内容 */
    private ClusterConfig mask(ClusterConfig c) {
        c.setContent(null);
        return c;
    }
}
