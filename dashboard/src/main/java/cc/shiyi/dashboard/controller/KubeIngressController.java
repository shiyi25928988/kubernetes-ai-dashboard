package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kubernetes Ingress 路由管理接口
 */
@Tag(name = "Kubernetes Ingress管理接口", description = "Ingress的CRUD、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeIngressController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的Ingress列表", description = "查询某个命名空间中所有的Ingress")
    @GetMapping("/namespaces/{namespace}/ingresses")
    public ResponseWrapper<List<Ingress>> listIngresses(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listIngresses(namespace));
    }

    @Operation(summary = "获取单个Ingress详情", description = "根据命名空间和名称查询Ingress详细信息")
    @GetMapping("/namespaces/{namespace}/ingresses/{name}")
    public ResponseWrapper<Ingress> getIngress(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress名称", required = true)
            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
        Ingress ingress = kubernetesService.getIngress(namespace, name);
        if (ingress == null) {
            return new ResponseWrapper().fail("Ingress不存在");
        }
        return new ResponseWrapper().success(ingress);
    }

    @Operation(summary = "创建Ingress", description = "在指定命名空间下创建新的Ingress")
    @PostMapping("/namespaces/{namespace}/ingresses")
    public ResponseWrapper<Void> createIngress(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress对象", required = true)
            @RequestBody @Validated Ingress ingress) {
        kubernetesService.createIngress(ingress, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "更新Ingress", description = "更新指定命名空间下的Ingress配置")
    @PutMapping("/namespaces/{namespace}/ingresses/{name}")
    public ResponseWrapper<Ingress> updateIngress(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress名称", required = true)
            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name,
            @Parameter(description = "更新后的Ingress对象", required = true)
            @RequestBody @Validated Ingress ingress) {
        Ingress updated = kubernetesService.updateIngress(ingress, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除Ingress", description = "删除指定命名空间下的Ingress")
    @DeleteMapping("/namespaces/{namespace}/ingresses/{name}")
    public ResponseWrapper<Void> deleteIngress(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress名称", required = true)
            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
        Ingress ingress = kubernetesService.getIngress(namespace, name);
        if (ingress == null) {
            return new ResponseWrapper().fail("Ingress不存在");
        }
        kubernetesService.deleteIngress(ingress, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "获取Ingress状态", description = "查询Ingress的状态信息")
    @GetMapping("/namespaces/{namespace}/ingresses/{name}/status")
    public ResponseWrapper<Map<String, Object>> getIngressStatus(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress名称", required = true)
            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
        Map<String, Object> status = kubernetesService.getIngressStatus(namespace, name);
        if (status.isEmpty()) {
            return new ResponseWrapper().fail("Ingress不存在");
        }
        return new ResponseWrapper().success(status);
    }

    @Operation(summary = "检查Ingress是否存在", description = "查询指定的Ingress是否存在")
    @GetMapping("/namespaces/{namespace}/ingresses/{name}/exists")
    public ResponseWrapper<Boolean> isIngressExists(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Ingress名称", required = true)
            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
        return new ResponseWrapper().success(kubernetesService.isIngressExists(namespace, name));
    }
}
