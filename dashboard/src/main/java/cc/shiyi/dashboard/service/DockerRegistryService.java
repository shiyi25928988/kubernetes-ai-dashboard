package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class DockerRegistryService {

    @Value("${docker.registry.url}")
    private String registryUrl;

    @Value("${docker.registry.username:}")
    private String username;

    @Value("${docker.registry.password:}")
    private String password;

    @Autowired
    private WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient client() {
        WebClient.Builder builder = webClient.mutate();
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            String auth = "Basic " + Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, auth);
        }
        return builder.build();
    }

    /**
     * 列出所有镜像仓库
     */
    public List<String> listRepositories() {
        String url = registryUrl + "/v2/_catalog";
        try {
            JsonNode node = client().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<String> repos = new ArrayList<>();
            if (node != null && node.has("repositories")) {
                node.get("repositories").forEach(r -> repos.add(r.asText()));
            }
            return repos;
        } catch (WebClientResponseException e) {
            log.error("Failed to list repositories: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取仓库列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询指定镜像的所有 tags
     */
    public DockerRegistryRepo listTags(String repository) {
        String url = registryUrl + "/v2/" + repository + "/tags/list";
        try {
            JsonNode node = client().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            DockerRegistryRepo repo = new DockerRegistryRepo();
            repo.setName(repository);
            repo.setRegistryHost(registryUrl.replaceFirst("https?://", ""));
            List<String> tags = new ArrayList<>();
            if (node != null && node.has("tags")) {
                node.get("tags").forEach(t -> tags.add(t.asText()));
            }
            repo.setTags(tags);
            repo.setTagCount(tags.size());
            return repo;
        } catch (WebClientResponseException e) {
            log.error("Failed to list tags for {}: {} {}", repository, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取镜像标签失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取镜像 manifest 详情
     */
    @SuppressWarnings("unchecked")
    public DockerImageManifest getManifest(String repository, String tag) {
        try {
            // 先获取 manifest (JSON 格式), 同时记录 digest
            JsonNode manifest = client().get()
                    .uri(registryUrl + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT,
                            "application/vnd.docker.distribution.manifest.v2+json, " +
                            "application/vnd.oci.image.manifest.v1+json, " +
                            "application/vnd.docker.distribution.manifest.list.v2+json, " +
                            "application/vnd.oci.image.index.v1+json")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String digest = client().get()
                    .uri(registryUrl + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT,
                            "application/vnd.docker.distribution.manifest.v2+json, " +
                            "application/vnd.docker.distribution.manifest.list.v2+json, " +
                            "application/vnd.oci.image.manifest.v1+json, " +
                            "application/vnd.oci.image.index.v1+json")
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

            // 尝试解析 config 中的 annotations
            if (manifest.has("config")) {
                JsonNode config = manifest.get("config");
                if (config.has("annotations")) {
                    info.setAnnotations(objectMapper.convertValue(config.get("annotations"), Map.class));
                }
            }

            // 解析 manifest JSON 本身的 annotations (如架构信息)
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
                    JsonNode configBlob = client().get()
                            .uri(registryUrl + "/v2/" + repository + "/blobs/" + configDigest)
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
        } catch (WebClientResponseException e) {
            log.error("Failed to get manifest for {}:{} - {} {}", repository, tag, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取镜像详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除指定镜像的 tag（通过 digest 删除）
     */
    public void deleteImage(String repository, String tag) {
        try {
            // 先获取 digest
            String digest = client().get()
                    .uri(registryUrl + "/v2/" + repository + "/manifests/" + tag)
                    .header(HttpHeaders.ACCEPT,
                            "application/vnd.docker.distribution.manifest.v2+json, " +
                            "application/vnd.docker.distribution.manifest.list.v2+json, " +
                            "application/vnd.oci.image.manifest.v1+json, " +
                            "application/vnd.oci.image.index.v1+json")
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getHeaders().getFirst("Docker-Content-Digest"))
                    .block();

            if (digest == null) {
                throw new RuntimeException("无法获取镜像 digest，删除失败");
            }

            client().delete()
                    .uri(registryUrl + "/v2/" + repository + "/manifests/" + digest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Deleted image {}:{} (digest: {})", repository, tag, digest);
        } catch (WebClientResponseException e) {
            log.error("Failed to delete image {}:{} - {} {}", repository, tag, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("删除镜像失败: " + e.getMessage(), e);
        }
    }
}
