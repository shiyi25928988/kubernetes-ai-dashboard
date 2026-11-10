package cc.shiyi.dashboard.service;

import cc.shiyi.coleditor.user.mapper.UserMapper;
import cc.shiyi.dashboard.db.mapper.RegistryConfigMapper;
import cc.shiyi.dashboard.db.table.RegistryConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * 镜像仓库源配置管理：页面增删改查，多仓库源并存。
 * 镜像浏览接口按 registryId 选择仓库源；不传时取 id 最小的（默认）仓库源。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryConfigService {

    public static final String TYPE_DOCKER = "docker";
    public static final String TYPE_HARBOR = "harbor";
    private static final Set<String> SUPPORTED_TYPES = Set.of(TYPE_DOCKER, TYPE_HARBOR);

    private final RegistryConfigMapper registryConfigMapper;
    private final UserMapper userMapper;
    private final WebClient webClient;

    /**
     * 仓库源列表（密码不回传，仅返回是否已设置）。
     */
    public List<RegistryConfig> list() {
        List<RegistryConfig> list = registryConfigMapper.selectList(
                new LambdaQueryWrapper<RegistryConfig>().orderByAsc(RegistryConfig::getId));
        list.forEach(this::mask);
        return list;
    }

    /**
     * 新增仓库源。
     */
    public RegistryConfig create(RegistryConfig input, Long creatorId) {
        validate(input);
        Long count = registryConfigMapper.selectCount(new LambdaQueryWrapper<RegistryConfig>()
                .eq(RegistryConfig::getName, input.getName()));
        if (count > 0) {
            throw new IllegalArgumentException("仓库源名称已存在: " + input.getName());
        }
        RegistryConfig entity = new RegistryConfig();
        entity.setName(input.getName().trim());
        entity.setType(input.getType().trim());
        entity.setUrl(input.getUrl().trim().replaceAll("/+$", ""));
        entity.setUsername(input.getUsername() == null ? "" : input.getUsername().trim());
        entity.setPassword(input.getPassword() == null ? "" : input.getPassword());
        entity.setRemark(input.getRemark() == null ? "" : input.getRemark());
        entity.setCreatorId(creatorId);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        registryConfigMapper.insert(entity);
        log.info("Created registry config '{}' (type={}, id={})", entity.getName(), entity.getType(), entity.getId());
        return mask(entity);
    }

    /**
     * 更新仓库源。password 传空表示保持原密码不变。
     */
    public RegistryConfig update(Long id, RegistryConfig input) {
        RegistryConfig entity = requireExists(id);
        validate(input);
        if (!entity.getName().equals(input.getName().trim())) {
            Long count = registryConfigMapper.selectCount(new LambdaQueryWrapper<RegistryConfig>()
                    .eq(RegistryConfig::getName, input.getName().trim())
                    .ne(RegistryConfig::getId, id));
            if (count > 0) {
                throw new IllegalArgumentException("仓库源名称已存在: " + input.getName());
            }
        }
        entity.setName(input.getName().trim());
        entity.setType(input.getType().trim());
        entity.setUrl(input.getUrl().trim().replaceAll("/+$", ""));
        entity.setUsername(input.getUsername() == null ? "" : input.getUsername().trim());
        entity.setRemark(input.getRemark() == null ? "" : input.getRemark());
        if (input.getPassword() != null && !input.getPassword().isEmpty()) {
            entity.setPassword(input.getPassword());
        }
        entity.setUpdatedTime(LocalDateTime.now());
        registryConfigMapper.updateById(entity);
        log.info("Updated registry config '{}' (id={})", entity.getName(), id);
        return mask(entity);
    }

    /**
     * 删除仓库源。
     */
    @Transactional
    public void delete(Long id) {
        RegistryConfig target = requireExists(id);
        registryConfigMapper.deleteById(id);
        log.info("Deleted registry config '{}' (id={})", target.getName(), id);
    }

    /**
     * 测试连接：docker 源探测 /v2/，harbor 源探测管理 API 当前用户。
     */
    public RegistryConfig testConnection(Long id) {
        RegistryConfig target = requireExists(id);
        String probe = TYPE_HARBOR.equals(target.getType())
                ? target.getUrl() + "/api/users/current"
                : target.getUrl() + "/v2/";
        try {
            WebClient client = webClient.mutate().build();
            if (TYPE_HARBOR.equals(target.getType())) {
                client.get()
                        .uri(probe)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth(target))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                log.info("Registry '{}' connection test OK (harbor /api/users/current)", target.getName());
                return mask(target);
            } else {
                // docker 源：/v2/ 返回 200（匿名或认证通过）即视为可达
                client.get()
                        .uri(probe)
                        .header(HttpHeaders.AUTHORIZATION, basicAuth(target))
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                log.info("Registry '{}' connection test OK (docker /v2/)", target.getName());
                return mask(target);
            }
        } catch (WebClientResponseException e) {
            throw new IllegalStateException("连接失败: HTTP " + e.getStatusCode() + (e.getResponseBodyAsString().isEmpty()
                    ? "" : " " + truncate(e.getResponseBodyAsString())));
        } catch (Exception e) {
            throw new IllegalStateException("连接失败: " + rootMessage(e));
        }
    }

    /**
     * 获取默认仓库源（id 最小）；未配置任何仓库源时返回 null。
     */
    public RegistryConfig getDefault() {
        RegistryConfig first = registryConfigMapper.selectOne(
                new LambdaQueryWrapper<RegistryConfig>().orderByAsc(RegistryConfig::getId).last("LIMIT 1"));
        return first == null ? null : mask(first);
    }

    /**
     * 按 id 获取仓库源（已脱敏）；id 为空时取默认仓库源。密码保留在返回值中供客户端使用（不会过控制器）。
     */
    public RegistryConfig requireRegistry(Long id) {
        RegistryConfig config;
        if (id == null) {
            config = getDefault();
            if (config == null) {
                throw new IllegalStateException("没有配置镜像仓库源，请先在镜像仓库页面添加");
            }
            // getDefault 返回的是脱敏对象，重新查全量（含密码）供客户端使用
            config = registryConfigMapper.selectById(config.getId());
        } else {
            config = registryConfigMapper.selectById(id);
            if (config == null) {
                throw new IllegalArgumentException("镜像仓库源不存在: id=" + id);
            }
        }
        return config;
    }

    private void validate(RegistryConfig input) {
        if (input.getName() == null || input.getName().isBlank()) {
            throw new IllegalArgumentException("仓库源名称不能为空");
        }
        if (input.getType() == null || !SUPPORTED_TYPES.contains(input.getType())) {
            throw new IllegalArgumentException("仓库类型无效，仅支持: docker / harbor");
        }
        if (input.getUrl() == null || input.getUrl().isBlank()) {
            throw new IllegalArgumentException("仓库地址不能为空");
        }
        String url = input.getUrl().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("仓库地址必须以 http:// 或 https:// 开头");
        }
    }

    private RegistryConfig requireExists(Long id) {
        RegistryConfig c = registryConfigMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("镜像仓库源不存在: id=" + id);
        }
        return c;
    }

    /** 密码不回传，改为 hasPassword 标记 */
    private RegistryConfig mask(RegistryConfig c) {
        c.setHasPassword(c.getPassword() != null && !c.getPassword().isEmpty());
        c.setPassword(null);
        return c;
    }

    static String basicAuth(RegistryConfig config) {
        String u = config.getUsername() == null ? "" : config.getUsername();
        String p = config.getPassword() == null ? "" : config.getPassword();
        if (u.isEmpty() && p.isEmpty()) return "";
        return "Basic " + Base64.getEncoder()
                .encodeToString((u + ":" + p).getBytes(StandardCharsets.UTF_8));
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() == null ? t.getMessage() : cur.getMessage();
    }

    private String truncate(String s) {
        return s.length() > 120 ? s.substring(0, 120) + "..." : s;
    }
}
