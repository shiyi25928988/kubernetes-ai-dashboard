package cc.shiyi.dashboard.service.registry;

import cc.shiyi.dashboard.db.table.RegistryConfig;
import cc.shiyi.dashboard.dto.DockerImageManifest;
import cc.shiyi.dashboard.dto.DockerRegistryRepo;

import java.util.List;

/**
 * 镜像仓库源客户端 SPI：不同类型仓库（Docker Registry V2 / Harbor ...）各自实现。
 */
public interface ImageRegistryClient {

    /** 列出所有镜像仓库名 */
    List<String> listRepositories(RegistryConfig config);

    /** 查询指定镜像的所有 tags */
    DockerRegistryRepo listTags(RegistryConfig config, String repository);

    /** 获取镜像 manifest 详情 */
    DockerImageManifest getManifest(RegistryConfig config, String repository, String tag);

    /** 删除指定镜像 tag */
    void deleteImage(RegistryConfig config, String repository, String tag);
}
