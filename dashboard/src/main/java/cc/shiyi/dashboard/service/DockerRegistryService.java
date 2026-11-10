package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.table.RegistryConfig;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import cc.shiyi.dashboard.service.registry.DockerV2RegistryClient;
import cc.shiyi.dashboard.service.registry.HarborRegistryClient;
import cc.shiyi.dashboard.service.registry.ImageRegistryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 镜像仓库操作门面：按仓库源类型分发到对应客户端。
 * 不传 registryId 时使用默认仓库源（id 最小），供 MCP 工具与旧接口兼容。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DockerRegistryService {

    private final RegistryConfigService registryConfigService;
    private final WebClient webClient;
    private final DockerV2RegistryClient v2Client;
    private final HarborRegistryClient harborClient;

    // ---- 带仓库源的接口 ----

    public List<String> listRepositories(Long registryId) {
        RegistryConfig config = resolve(registryId);
        return clientFor(config).listRepositories(config);
    }

    public DockerRegistryRepo listTags(Long registryId, String repository) {
        RegistryConfig config = resolve(registryId);
        return clientFor(config).listTags(config, repository);
    }

    public DockerImageManifest getManifest(Long registryId, String repository, String tag) {
        RegistryConfig config = resolve(registryId);
        return clientFor(config).getManifest(config, repository, tag);
    }

    public void deleteImage(Long registryId, String repository, String tag) {
        RegistryConfig config = resolve(registryId);
        clientFor(config).deleteImage(config, repository, tag);
    }

    // ---- 兼容旧签名（默认仓库源，MCP 工具使用） ----

    public List<String> listRepositories() {
        return listRepositories(null);
    }

    public DockerRegistryRepo listTags(String repository) {
        return listTags(null, repository);
    }

    public DockerImageManifest getManifest(String repository, String tag) {
        return getManifest(null, repository, tag);
    }

    public void deleteImage(String repository, String tag) {
        deleteImage(null, repository, tag);
    }

    private RegistryConfig resolve(Long registryId) {
        return registryConfigService.requireRegistry(registryId);
    }

    private ImageRegistryClient clientFor(RegistryConfig config) {
        if (RegistryConfigService.TYPE_HARBOR.equals(config.getType())) {
            return harborClient;
        }
        return v2Client;
    }
}
