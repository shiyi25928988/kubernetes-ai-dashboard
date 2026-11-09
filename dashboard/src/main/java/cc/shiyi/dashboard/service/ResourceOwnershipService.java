package cc.shiyi.dashboard.service;

import cc.shiyi.coleditor.user.mapper.UserMapper;
import cc.shiyi.dashboard.db.mapper.PlatformResourceMapper;
import cc.shiyi.dashboard.db.table.PlatformResource;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@lombok.extern.slf4j.Slf4j
public class ResourceOwnershipService {

    public static final String LABEL_MANAGED_BY = "dssp.io/managed-by";
    public static final String LABEL_CREATED_BY = "dssp.io/created-by";
    public static final String MANAGED_BY_VALUE = "dssp";

    @Autowired
    private PlatformResourceMapper platformResourceMapper;

    @Autowired
    private UserMapper userMapper;

    // ---- LABEL INJECTION ----

    /**
     * Add ownership labels to a K8S resource's metadata before creation.
     * If the current context has no logged-in user (e.g., MCP/AI agent),
     * the created-by label is set to "system".
     */
    public void addOwnershipLabels(ObjectMeta metadata) {
        String creator;
        try {
            if (StpUtil.isLogin()) {
                creator = StpUtil.getLoginIdAsString();
            } else {
                creator = "system";
            }
        } catch (Exception e) {
            creator = "system";
        }
        Map<String, String> labels = metadata.getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            metadata.setLabels(labels);
        }
        labels.put(LABEL_MANAGED_BY, MANAGED_BY_VALUE);
        labels.put(LABEL_CREATED_BY, creator);
    }

    // ---- DELETE GATE ----

    /**
     * Returns true if the resource has the platform managed-by label.
     */
    public boolean isPlatformManaged(ObjectMeta metadata) {
        if (metadata == null || metadata.getLabels() == null) {
            return false;
        }
        return MANAGED_BY_VALUE.equals(metadata.getLabels().get(LABEL_MANAGED_BY));
    }

    /**
     * Throws ResourceNotManagedException if the resource is not platform-managed.
     */
    public void requirePlatformManaged(ObjectMeta metadata, String resourceDesc) {
        if (!isPlatformManaged(metadata)) {
            throw new ResourceNotManagedException(
                    resourceDesc + " 并非通过平台创建，无法在此删除");
        }
    }

    // ---- DB AUDIT ----

    /**
     * Record a resource creation in the audit table.
     * 同名的历史记录（例如资源曾被平台删除后又被外部重建又删掉，或重复创建场景）会残留，
     * 此处先清掉同 key 记录再插入，避免同名资源再次创建时撞唯一键 uk_resource 导致创建失败。
     */
    public void recordCreation(String resourceType, String namespace, String name) {
        Long creatorId;
        try {
            creatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : 0L;
        } catch (Exception e) {
            creatorId = 0L;
        }
        String ns = namespace != null ? namespace : "";
        long removed = deleteRecord(resourceType, ns, name);
        if (removed > 0) {
            log.info("归属记录残留清理: {} {}/{} 共{}条", resourceType, ns, name, removed);
        }
        PlatformResource record = new PlatformResource();
        record.setResourceType(resourceType);
        record.setResourceName(name);
        record.setNamespace(ns);
        record.setCreatorId(creatorId);
        record.setCreatedTime(LocalDateTime.now());
        platformResourceMapper.insert(record);
    }

    /**
     * Remove the ownership record after a resource is deleted through the platform.
     * 幂等：记录不存在时为 no-op。
     */
    public void recordDeletion(String resourceType, String namespace, String name) {
        try {
            long removed = deleteRecord(resourceType, namespace, name);
            if (removed > 0) {
                log.info("归属记录删除: {} {}/{} 共{}条", resourceType, namespace != null ? namespace : "", name, removed);
            }
        } catch (Exception e) {
            // 记录清理失败不应影响删除本身
            log.warn("归属记录删除失败: {} {}/{}: {}", resourceType, namespace, name, e.getMessage());
        }
    }

    private long deleteRecord(String resourceType, String namespace, String name) {
        QueryWrapper<PlatformResource> qw = new QueryWrapper<>();
        qw.eq("resource_type", resourceType);
        qw.eq("resource_name", name);
        qw.eq("namespace", namespace != null ? namespace : "");
        return platformResourceMapper.delete(qw);
    }

    // ---- OWNERSHIP QUERY ----

    public List<Map<String, Object>> listOwnershipRecords(String resourceType, String namespace) {
        QueryWrapper<PlatformResource> qw = new QueryWrapper<>();
        if (resourceType != null && !resourceType.isEmpty()) {
            qw.eq("resource_type", resourceType);
        }
        if (namespace != null && !namespace.isEmpty()) {
            qw.eq("namespace", namespace);
        }
        qw.orderByDesc("created_time");
        List<PlatformResource> records = platformResourceMapper.selectList(qw);

        Set<Long> creatorIds = records.stream()
                .map(PlatformResource::getCreatorId)
                .filter(id -> id > 0)
                .collect(Collectors.toSet());
        Map<Long, String> usernameMap = new HashMap<>();
        if (!creatorIds.isEmpty()) {
            userMapper.selectBatchIds(creatorIds)
                    .forEach(u -> usernameMap.put(u.getId(), u.getUsername()));
        }

        return records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("resourceType", r.getResourceType());
            item.put("resourceName", r.getResourceName());
            item.put("namespace", r.getNamespace());
            item.put("creatorId", r.getCreatorId());
            item.put("creatorName", r.getCreatorId() == 0 ? "system" :
                    usernameMap.getOrDefault(r.getCreatorId(), "unknown"));
            item.put("createdTime", r.getCreatedTime() != null ? r.getCreatedTime().toString() : null);
            return item;
        }).collect(Collectors.toList());
    }
}
