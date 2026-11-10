package cc.shiyi.dashboard.service.registry;

import cc.shiyi.dashboard.db.table.RegistryConfig;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Docker Registry V2 协议客户端（docker distribution 及一切兼容 V2 API 的仓库）。
 * 认证方式：Basic（用户名/密码可为空）。
 */
@Slf4j
@Component
public class DockerV2RegistryClient implements ImageRegistryClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DockerV2RegistryClient(WebClient webClient) {
        this.webClient = webClient;
    }

    private WebClient client(RegistryConfig config) {
        WebClient.Builder builder = webClient.mutate();
        String auth = basicAuth(config);
        if (auth != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, auth);
        }
        return builder.build();
    }

    /** Basic 认证头；用户名/密码都为空时返回 null（匿名访问） */
    static String basicAuth(RegistryConfig config) {
        String u = config.getUsername() == null ? "" : config.getUsername();
        String p = config.getPassword() == null ? "" : config.getPassword();
        if (u.isEmpty() && p.isEmpty()) {
            return null;
        }
        return "Basic " + Base64.getEncoder()
                .encodeToString((u + ":" + p).getBytes(StandardCharsets.UTF_8));
    }

    private static final String MANIFEST_ACCEPT =
            "application/vnd.docker.distribution.manifest.v2+json, " +
            "application/vnd.oci.image.manifest.v1+json, " +
            "application/vnd.docker.distribution.manifest.list.v2+json, " +
            "application/vnd.oci.image.index.v1+json";

    @Override
    public List<String> listRepositories(RegistryConfig config) {
        String url = config.getUrl() + "/v2/_catalog";
        JsonNode node = client(config).get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        List<String> repos = new ArrayList<>();
        if (node != null && node.has("repositories")) {
            node.get("repositories").forEach(r -> repos.add(r.asText()));
        }
        return repos;
    }

    @Override
    public DockerRegistryRepo listTags(RegistryConfig config, String repository) {
        String url = config.getUrl() + "/v2/" + repository + "/tags/list";
        JsonNode node = client(config).get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        DockerRegistryRepo repo = new DockerRegistryRepo();
        repo.setName(repository);
        repo.setRegistryHost(config.getUrl().replaceFirst("https?://", ""));
        List<String> tags = new ArrayList<>();
        if (node != null && node.has("tags")) {
            node.get("tags").forEach(t -> tags.add(t.asText()));
        }
        repo.setTags(tags);
        repo.setTagCount(tags.size());
        return repo;
    }

    @Override
    public DockerImageManifest getManifest(RegistryConfig config, String repository, String tag) {
        return doGetManifest(config.getUrl(), basicAuth(config), repository, tag);
    }

    /**
     * 以 Bearer token 认证获取 manifest（供 Harbor 等需要 token 流程的仓库源复用解析逻辑）。
     */
    public DockerImageManifest getManifestWithBearer(RegistryConfig config, String repository, String tag, String bearerToken) {
        return doGetManifest(config.getUrl(), "Bearer " + bearerToken, repository, tag);
    }

    /**
     * 以 Bearer token 认证按 digest 删除镜像（供 Harbor 等需要 token 流程的仓库源复用）。
     */
    public void deleteImageWithBearer(RegistryConfig config, String repository, String tag, String bearerToken) {
        try {
            String authHeader = "Bearer " + bearerToken;
            String digest = webClient.mutate().build().get()
                    .uri(config.getUrl() + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT, MANIFEST_ACCEPT)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getHeaders().getFirst("Docker-Content-Digest"))
                    .block();

            if (digest == null) {
                throw new RuntimeException("无法获取镜像 digest，删除失败");
            }

            webClient.mutate().build().delete()
                    .uri(config.getUrl() + "/v2/" + repository + "/manifests/" + digest)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Deleted image {}:{} (digest: {}) from registry '{}'", repository, tag, digest, config.getName());
        } catch (Exception e) {
            log.error("Failed to delete image {}:{} - {}", repository, tag, e.getMessage());
            throw new RuntimeException("删除镜像失败: " + e.getMessage(), e);
        }
    }

    private DockerImageManifest doGetManifest(String baseUrl, String authHeader, String repository, String tag) {
        try {
            JsonNode manifest = webClient.mutate().build().get()
                    .uri(baseUrl + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT, MANIFEST_ACCEPT)
                    .header(HttpHeaders.AUTHORIZATION, authHeader == null ? "" : authHeader)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String digest = webClient.mutate().build().get()
                    .uri(baseUrl + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT, MANIFEST_ACCEPT)
                    .header(HttpHeaders.AUTHORIZATION, authHeader == null ? "" : authHeader)
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getHeaders().getFirst("Docker-Content-Digest"))
                    .block();

            DockerImageManifest info = new DockerImageManifest();
            info.setName(repository);
            info.setTag(tag);
            info.setDigest(digest);

            if (manifest == null) return info;

            info.setMediaType(manifest.path("mediaType").asText(null));
            info.setSizeBytes(manifest.path("config").path("size").asLong(0));

            List<String> layers = new ArrayList<>();
            if (manifest.has("layers")) {
                manifest.get("layers").forEach(layer -> {
                    String layerDigest = layer.path("digest").asText(null);
                    if (layerDigest != null) layers.add(layerDigest);
                });
            }
            info.setLayers(layers);

            if (manifest.has("config")) {
                JsonNode config = manifest.get("config");
                if (config.has("annotations")) {
                    info.setAnnotations(objectMapper.convertValue(config.get("annotations"), Map.class));
                }
            }

            if (manifest.has("annotations")) {
                Map<String, String> ann = objectMapper.convertValue(manifest.get("annotations"), Map.class);
                info.setArchitecture(ann.get("vnd.docker.reference.architecture"));
                info.setOs(ann.get("vnd.docker.reference.os"));
                info.setCreated(ann.get("org.opencontainers.image.created"));
            }

            // 获取 config blob 中的详细信息
            if (manifest.has("config") && manifest.get("config").has("digest")) {
                String configDigest = manifest.get("config").get("digest").asText();
                try {
                    JsonNode configBlob = webClient.mutate().build().get()
                            .uri(baseUrl + "/v2/" + repository + "/blobs/" + configDigest)
                            .header(HttpHeaders.AUTHORIZATION, authHeader == null ? "" : authHeader)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .block();
                    if (configBlob != null) {
                        if (configBlob.has("architecture")) info.setArchitecture(configBlob.get("architecture").asText());
                        if (configBlob.has("os")) info.setOs(configBlob.get("os").asText());
                        if (configBlob.has("created")) info.setCreated(configBlob.get("created").asText());
                    }
                } catch (Exception ignored) {
                    // config blob 可能不可访问
                }
            }

            return info;
        } catch (Exception e) {
            log.error("Failed to get manifest for {}:{} - {}", repository, tag, e.getMessage());
            throw new RuntimeException("获取镜像详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(RegistryConfig config, String repository, String tag) {
        try {
            String digest = webClient.mutate().build().get()
                    .uri(config.getUrl() + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT, MANIFEST_ACCEPT)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth(config) == null ? "" : basicAuth(config))
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getHeaders().getFirst("Docker-Content-Digest"))
                    .block();

            if (digest == null) {
                throw new RuntimeException("无法获取镜像 digest，删除失败");
            }

            webClient.mutate().build().delete()
                    .uri(config.getUrl() + "/v2/" + repository + "/manifests/" + digest)
                    .header(HttpHeaders.AUTHORIZATION, basicAuth(config) == null ? "" : basicAuth(config))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Deleted image {}:{} (digest: {}) from registry '{}'", repository, tag, digest, config.getName());
        } catch (Exception e) {
            log.error("Failed to delete image {}:{} - {}", repository, tag, e.getMessage());
            throw new RuntimeException("删除镜像失败: " + e.getMessage(), e);
        }
    }
}
