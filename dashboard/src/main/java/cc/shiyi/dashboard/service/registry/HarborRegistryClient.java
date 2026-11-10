package cc.shiyi.dashboard.service.registry;

import cc.shiyi.dashboard.db.table.RegistryConfig;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Harbor 仓库客户端。
 * 实测兼容 Harbor 1.x：
 *  - 项目/仓库/标签列表走 Harbor 管理 API（/api/projects、/api/repositories，Basic 认证）
 *  - Manifest/删除走 Docker V2 协议（先向 /service/token 换 Bearer token）
 * Harbor 1.x 的管理 API 不在 /api/v2.0 前缀下；V2 /v2/_catalog 仅管理员可用，故列表不用 _catalog。
 */
@Slf4j
@Component
public class HarborRegistryClient implements ImageRegistryClient {

    private static final int PAGE_SIZE = 100;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WebClient webClient;
    private final DockerV2RegistryClient v2Client;

    public HarborRegistryClient(WebClient webClient, DockerV2RegistryClient v2Client) {
        this.webClient = webClient;
        this.v2Client = v2Client;
    }

    private String getJson(RegistryConfig config, String path) {
        return webClient.mutate().build().get()
                .uri(config.getUrl() + path)
                .header("Authorization", DockerV2RegistryClient.basicAuth(config) == null
                        ? "" : DockerV2RegistryClient.basicAuth(config))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Override
    public List<String> listRepositories(RegistryConfig config) {
        List<String> repos = new ArrayList<>();
        int page = 1;
        while (true) {
            String body = getJson(config, "/api/projects?page=" + page + "&page_size=" + PAGE_SIZE);
            List<JsonNode> projects = parseList(body);
            for (JsonNode p : projects) {
                long projectId = p.path("project_id").asLong(0);
                repos.addAll(listProjectRepositories(config, projectId));
            }
            if (projects.size() < PAGE_SIZE) break;
            page++;
        }
        return repos;
    }

    private List<String> listProjectRepositories(RegistryConfig config, long projectId) {
        List<String> repos = new ArrayList<>();
        int page = 1;
        while (true) {
            String body = getJson(config, "/api/repositories?project_id=" + projectId
                    + "&page=" + page + "&page_size=" + PAGE_SIZE);
            List<JsonNode> list = parseList(body);
            for (JsonNode r : list) {
                String name = r.path("name").asText("");
                if (!name.isEmpty()) repos.add(name);
            }
            if (list.size() < PAGE_SIZE) break;
            page++;
        }
        return repos;
    }

    @Override
    public DockerRegistryRepo listTags(RegistryConfig config, String repository) {
        DockerRegistryRepo repo = new DockerRegistryRepo();
        repo.setName(repository);
        repo.setRegistryHost(config.getUrl().replaceFirst("https?://", ""));
        // Harbor 1.x 标签接口返回含 digest/size/architecture/os/created 的对象数组
        String body = getJson(config, "/api/repositories/" + repository + "/tags");
        List<String> tags = new ArrayList<>();
        if (body != null) {
            try {
                JsonNode node = MAPPER.readTree(body);
                if (node.isArray()) {
                    node.forEach(t -> {
                        String name = t.path("name").asText(null);
                        if (name != null) tags.add(name);
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException("解析Harbor标签列表失败: " + e.getMessage(), e);
            }
        }
        repo.setTags(tags);
        repo.setTagCount(tags.size());
        return repo;
    }

    @Override
    public DockerImageManifest getManifest(RegistryConfig config, String repository, String tag) {
        String token = requestToken(config, "repository:" + repository + ":pull");
        return v2Client.getManifestWithBearer(config, repository, tag, token);
    }

    @Override
    public void deleteImage(RegistryConfig config, String repository, String tag) {
        // Harbor 1.x 支持直接按 tag 删除：DELETE /api/repositories/{repo}/tags/{tag}
        try {
            webClient.mutate().build().delete()
                    .uri(config.getUrl() + "/api/repositories/" + repository + "/tags/" + tag)
                    .header("Authorization", DockerV2RegistryClient.basicAuth(config) == null
                            ? "" : DockerV2RegistryClient.basicAuth(config))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Deleted Harbor image tag {}:{}", repository, tag);
        } catch (WebClientResponseException e) {
            // 404/405 等说明该版本不支持按 tag 删除，回退到 V2 digest 删除
            if (e.getStatusCode().is4xxClientError()) {
                log.info("Harbor tag delete failed ({}), falling back to V2 digest delete", e.getStatusCode());
                String token = requestToken(config, "repository:" + repository + ":pull,push");
                v2Client.deleteImageWithBearer(config, repository, tag, token);
            } else {
                throw new RuntimeException("删除镜像失败: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("删除镜像失败: " + e.getMessage(), e);
        }
    }

    /** 向 Harbor token 服务申请指定 scope 的 Bearer token（docker login 同款流程） */
    private String requestToken(RegistryConfig config, String scope) {
        String auth = DockerV2RegistryClient.basicAuth(config);
        try {
            String body = webClient.mutate().build().get()
                    .uri(config.getUrl() + "/service/token?service=harbor-registry&scope=" + scope)
                    .header("Authorization", auth == null ? "" : auth)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JsonNode node = MAPPER.readTree(body);
            String token = node.path("token").asText(null);
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Harbor token 服务未返回 token，请检查账号权限");
            }
            return token;
        } catch (Exception e) {
            throw new RuntimeException("获取Harbor访问令牌失败: " + e.getMessage(), e);
        }
    }

    private List<JsonNode> parseList(String body) {
        try {
            JsonNode node = MAPPER.readTree(body == null ? "[]" : body);
            List<JsonNode> result = new ArrayList<>();
            if (node.isArray()) {
                node.forEach(result::add);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析Harbor接口响应失败: " + e.getMessage(), e);
        }
    }
}
