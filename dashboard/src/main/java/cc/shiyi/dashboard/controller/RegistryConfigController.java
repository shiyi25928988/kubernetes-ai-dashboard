package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.RegistryConfig;
import cc.shiyi.dashboard.service.RegistryConfigService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 镜像仓库源配置管理接口：多仓库源（Docker Registry V2 / Harbor）增删改查与测试连接。
 */
@RestController
@RequestMapping("/api/v1/registry/configs")
@Setter(onMethod_ = @Autowired)
@Tag(name = "镜像仓库源配置-RegistryConfigController")
public class RegistryConfigController {

    private RegistryConfigService registryConfigService;

    @Operation(summary = "仓库源列表", description = "列出所有镜像仓库源（密码不回传）")
    @GetMapping
    public ResponseWrapper<List<RegistryConfig>> list() {
        return new ResponseWrapper().success(registryConfigService.list());
    }

    @Operation(summary = "新增仓库源")
    @PostMapping
    public ResponseWrapper<RegistryConfig> create(@RequestBody RegistryConfig config) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return new ResponseWrapper().success(registryConfigService.create(config, userId));
        } catch (IllegalArgumentException e) {
            return new ResponseWrapper().fail(e.getMessage());
        } catch (Exception e) {
            return new ResponseWrapper().fail("新增仓库源失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新仓库源", description = "password 传空表示保持原密码不变")
    @PutMapping("/{id}")
    public ResponseWrapper<RegistryConfig> update(
            @Parameter(description = "仓库源ID", required = true)
            @PathVariable Long id,
            @RequestBody RegistryConfig config) {
        try {
            return new ResponseWrapper().success(registryConfigService.update(id, config));
        } catch (IllegalArgumentException e) {
            return new ResponseWrapper().fail(e.getMessage());
        } catch (Exception e) {
            return new ResponseWrapper().fail("更新仓库源失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试仓库源连接")
    @PostMapping("/{id}/test")
    public ResponseWrapper<RegistryConfig> testConnection(
            @Parameter(description = "仓库源ID", required = true)
            @PathVariable Long id) {
        try {
            return new ResponseWrapper().success(registryConfigService.testConnection(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "删除仓库源")
    @DeleteMapping("/{id}")
    public ResponseWrapper<Void> delete(
            @Parameter(description = "仓库源ID", required = true)
            @PathVariable Long id) {
        try {
            registryConfigService.delete(id);
            return new ResponseWrapper().success(null);
        } catch (IllegalArgumentException e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }
}
