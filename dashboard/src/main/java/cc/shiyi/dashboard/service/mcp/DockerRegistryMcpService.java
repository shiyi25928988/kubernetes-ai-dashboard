package cc.shiyi.dashboard.service.mcp;

import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;
import cc.shiyi.dashboard.service.DockerRegistryService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Setter(onMethod_ = @Autowired)
@Slf4j
public class DockerRegistryMcpService {

    DockerRegistryService dockerRegistryService;

    @Tool(description = "列出Docker Registry中的所有镜像仓库")
    public List<String> listRepositories() {
        return dockerRegistryService.listRepositories();
    }

    @Tool(description = "查询指定镜像仓库的所有tags")
    public DockerRegistryRepo listTags(
            @ToolParam(description = "镜像仓库名称") String repository) {
        return dockerRegistryService.listTags(repository);
    }

    @Tool(description = "获取指定镜像的manifest详情，包括digest、架构、OS、创建时间、层信息等")
    public DockerImageManifest getManifest(
            @ToolParam(description = "镜像仓库名称") String repository,
            @ToolParam(description = "镜像tag") String tag) {
        return dockerRegistryService.getManifest(repository, tag);
    }

    @Tool(description = "删除指定镜像的tag")
    public void deleteImage(
            @ToolParam(description = "镜像仓库名称") String repository,
            @ToolParam(description = "要删除的镜像tag") String tag) {
        dockerRegistryService.deleteImage(repository, tag);
    }
}
