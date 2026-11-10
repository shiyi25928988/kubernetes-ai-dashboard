package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import cc.shiyi.dashboard.service.DockerRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registry")
@Setter(onMethod_ = @Autowired)
@Tag(name = "Docker Registry管理-DockerRegistryController")
public class DockerRegistryController {

    private DockerRegistryService dockerRegistryService;

    @Operation(summary = "列出所有镜像仓库", description = "registryId 不传时使用默认仓库源")
    @GetMapping("/repositories")
    public ResponseWrapper<List<String>> listRepositories(
            @Parameter(description = "仓库源ID，不传使用默认仓库源")
            @RequestParam(value = "registryId", required = false) Long registryId) {
        return new ResponseWrapper().success(dockerRegistryService.listRepositories(registryId));
    }

    @Operation(summary = "查询指定镜像的所有tags")
    @GetMapping("/repositories/tags")
    public ResponseWrapper<DockerRegistryRepo> listTags(
            @RequestParam String repository,
            @RequestParam(value = "registryId", required = false) Long registryId) {
        return new ResponseWrapper().success(dockerRegistryService.listTags(registryId, repository));
    }

    @Operation(summary = "获取镜像manifest详情")
    @GetMapping("/repositories/manifests")
    public ResponseWrapper<DockerImageManifest> getManifest(
            @RequestParam String repository,
            @RequestParam String tag,
            @RequestParam(value = "registryId", required = false) Long registryId) {
        return new ResponseWrapper().success(dockerRegistryService.getManifest(registryId, repository, tag));
    }

    @Operation(summary = "删除指定镜像tag")
    @DeleteMapping("/repositories/tags")
    public ResponseWrapper<Void> deleteImage(
            @RequestParam String repository,
            @RequestParam String tag,
            @RequestParam(value = "registryId", required = false) Long registryId) {
        dockerRegistryService.deleteImage(registryId, repository, tag);
        return new ResponseWrapper().success(null);
    }
}
