package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import cc.shiyi.dashboard.service.DockerRegistryService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "列出所有镜像仓库")
    @GetMapping("/repositories")
    public ResponseWrapper<List<String>> listRepositories() {
        return new ResponseWrapper().success(dockerRegistryService.listRepositories());
    }

    @Operation(summary = "查询指定镜像的所有tags")
    @GetMapping("/repositories/tags")
    public ResponseWrapper<DockerRegistryRepo> listTags(@RequestParam String repository) {
        return new ResponseWrapper().success(dockerRegistryService.listTags(repository));
    }

    @Operation(summary = "获取镜像manifest详情")
    @GetMapping("/repositories/manifests")
    public ResponseWrapper<DockerImageManifest> getManifest(
            @RequestParam String repository,
            @RequestParam String tag) {
        return new ResponseWrapper().success(dockerRegistryService.getManifest(repository, tag));
    }

    @Operation(summary = "删除指定镜像tag")
    @DeleteMapping("/repositories/tags")
    public ResponseWrapper<Void> deleteImage(
            @RequestParam String repository,
            @RequestParam String tag) {
        dockerRegistryService.deleteImage(repository, tag);
        return new ResponseWrapper().success(null);
    }
}
