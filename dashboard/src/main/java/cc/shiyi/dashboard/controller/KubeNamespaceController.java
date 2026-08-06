package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Kubernetes Namespace 命名空间管理接口
 */
@Tag(name = "Kubernetes Namespace管理接口", description = "命名空间的CRUD、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeNamespaceController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有命名空间列表", description = "查询Kubernetes集群中所有的命名空间")
    @GetMapping("/namespaces")
    public ResponseWrapper<NamespaceList> listNamespaces() {
        return new ResponseWrapper().success(kubernetesService.listNamespaces());
    }

    @Operation(summary = "创建命名空间", description = "创建新的Kubernetes命名空间")
    @PostMapping("/namespaces")
    public ResponseWrapper<Namespace> createNamespace(
            @Parameter(description = "命名空间对象", required = true)
            @RequestBody @Validated Namespace namespace) {
        Namespace created = kubernetesService.createNamespace(namespace);
        return new ResponseWrapper().success(created);
    }

    @Operation(summary = "删除命名空间", description = "删除指定的Kubernetes命名空间")
    @DeleteMapping("/namespaces/{namespace}")
    public ResponseWrapper<Void> deleteNamespace(
            @Parameter(description = "要删除的命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        Namespace ns = kubernetesService.getNamespace(namespace);
        if (ns != null) {
            kubernetesService.deleteNamespace(ns);
        }
        return new ResponseWrapper().success();
    }
}

