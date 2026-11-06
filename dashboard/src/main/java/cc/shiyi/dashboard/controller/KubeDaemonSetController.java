package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kubernetes DaemonSet 管理接口
 */
@Tag(name = "Kubernetes DaemonSet管理接口", description = "DaemonSet的CRUD等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeDaemonSetController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的DaemonSet列表")
    @GetMapping("/namespaces/{namespace}/daemonsets")
    public ResponseWrapper<List<DaemonSet>> listDaemonSets(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listDaemonSets(namespace));
    }

    @Operation(summary = "获取单个DaemonSet详情")
    @GetMapping("/namespaces/{namespace}/daemonsets/{name}")
    public ResponseWrapper<DaemonSet> getDaemonSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "DaemonSet名称", required = true)
            @PathVariable @NotBlank(message = "DaemonSet名称不能为空") String name) {
        DaemonSet daemonSet = kubernetesService.getDaemonSet(namespace, name);
        if (daemonSet == null) {
            return new ResponseWrapper().fail("DaemonSet不存在");
        }
        return new ResponseWrapper().success(daemonSet);
    }

    @Operation(summary = "创建DaemonSet")
    @PostMapping("/namespaces/{namespace}/daemonsets")
    public ResponseWrapper<DaemonSet> createDaemonSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "DaemonSet对象", required = true)
            @RequestBody @Validated DaemonSet daemonSet) {
        kubernetesService.createDaemonSet(daemonSet, namespace);
        return new ResponseWrapper().success(daemonSet);
    }

    @Operation(summary = "更新DaemonSet")
    @PutMapping("/namespaces/{namespace}/daemonsets/{name}")
    public ResponseWrapper<DaemonSet> updateDaemonSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "DaemonSet名称", required = true)
            @PathVariable @NotBlank(message = "DaemonSet名称不能为空") String name,
            @Parameter(description = "更新后的DaemonSet对象", required = true)
            @RequestBody @Validated DaemonSet daemonSet) {
        DaemonSet updated = kubernetesService.updateDaemonSet(daemonSet, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除DaemonSet", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/daemonsets/{name}")
    public ResponseWrapper<Void> deleteDaemonSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "DaemonSet名称", required = true)
            @PathVariable @NotBlank(message = "DaemonSet名称不能为空") String name) {
        DaemonSet daemonSet = kubernetesService.getDaemonSet(namespace, name);
        if (daemonSet == null) {
            return new ResponseWrapper().fail("DaemonSet不存在");
        }
        kubernetesService.deleteDaemonSet(daemonSet, namespace);
        return new ResponseWrapper().success();
    }
}
