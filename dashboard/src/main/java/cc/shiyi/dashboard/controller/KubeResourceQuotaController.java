package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kubernetes ResourceQuota 管理接口
 */
@Tag(name = "Kubernetes ResourceQuota接口", description = "资源配额的CRUD操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeResourceQuotaController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的ResourceQuota列表")
    @GetMapping("/namespaces/{namespace}/resourcequotas")
    public ResponseWrapper<List<ResourceQuota>> listResourceQuotas(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listResourceQuotas(namespace));
    }

    @Operation(summary = "获取单个ResourceQuota详情")
    @GetMapping("/namespaces/{namespace}/resourcequotas/{name}")
    public ResponseWrapper<ResourceQuota> getResourceQuota(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ResourceQuota名称", required = true)
            @PathVariable @NotBlank(message = "ResourceQuota名称不能为空") String name) {
        ResourceQuota quota = kubernetesService.getResourceQuota(namespace, name);
        if (quota == null) {
            return new ResponseWrapper().fail("ResourceQuota不存在");
        }
        return new ResponseWrapper().success(quota);
    }

    @Operation(summary = "创建ResourceQuota")
    @PostMapping("/namespaces/{namespace}/resourcequotas")
    public ResponseWrapper<ResourceQuota> createResourceQuota(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ResourceQuota对象", required = true)
            @RequestBody @Validated ResourceQuota quota) {
        return new ResponseWrapper().success(kubernetesService.createResourceQuota(quota, namespace));
    }

    @Operation(summary = "更新ResourceQuota")
    @PutMapping("/namespaces/{namespace}/resourcequotas/{name}")
    public ResponseWrapper<ResourceQuota> updateResourceQuota(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ResourceQuota名称", required = true)
            @PathVariable @NotBlank(message = "ResourceQuota名称不能为空") String name,
            @Parameter(description = "更新后的ResourceQuota对象", required = true)
            @RequestBody @Validated ResourceQuota quota) {
        return new ResponseWrapper().success(kubernetesService.updateResourceQuota(quota, namespace));
    }

    @Operation(summary = "删除ResourceQuota", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/resourcequotas/{name}")
    public ResponseWrapper<Void> deleteResourceQuota(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ResourceQuota名称", required = true)
            @PathVariable @NotBlank(message = "ResourceQuota名称不能为空") String name) {
        ResourceQuota quota = kubernetesService.getResourceQuota(namespace, name);
        if (quota == null) {
            return new ResponseWrapper().fail("ResourceQuota不存在");
        }
        kubernetesService.deleteResourceQuota(quota, namespace);
        return new ResponseWrapper().success();
    }
}
