package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kubernetes StatefulSet 管理接口
 */
@Tag(name = "Kubernetes StatefulSet管理接口", description = "StatefulSet的CRUD、扩缩容等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeStatefulSetController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的StatefulSet列表")
    @GetMapping("/namespaces/{namespace}/statefulsets")
    public ResponseWrapper<List<StatefulSet>> listStatefulSets(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listStatefulSets(namespace));
    }

    @Operation(summary = "获取单个StatefulSet详情")
    @GetMapping("/namespaces/{namespace}/statefulsets/{name}")
    public ResponseWrapper<StatefulSet> getStatefulSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "StatefulSet名称", required = true)
            @PathVariable @NotBlank(message = "StatefulSet名称不能为空") String name) {
        StatefulSet statefulSet = kubernetesService.getStatefulSet(namespace, name);
        if (statefulSet == null) {
            return new ResponseWrapper().fail("StatefulSet不存在");
        }
        return new ResponseWrapper().success(statefulSet);
    }

    @Operation(summary = "创建StatefulSet")
    @PostMapping("/namespaces/{namespace}/statefulsets")
    public ResponseWrapper<StatefulSet> createStatefulSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "StatefulSet对象", required = true)
            @RequestBody @Validated StatefulSet statefulSet) {
        kubernetesService.createStatefulSet(statefulSet, namespace);
        return new ResponseWrapper().success(statefulSet);
    }

    @Operation(summary = "更新StatefulSet")
    @PutMapping("/namespaces/{namespace}/statefulsets/{name}")
    public ResponseWrapper<StatefulSet> updateStatefulSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "StatefulSet名称", required = true)
            @PathVariable @NotBlank(message = "StatefulSet名称不能为空") String name,
            @Parameter(description = "更新后的StatefulSet对象", required = true)
            @RequestBody @Validated StatefulSet statefulSet) {
        StatefulSet updated = kubernetesService.updateStatefulSet(statefulSet, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除StatefulSet", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/statefulsets/{name}")
    public ResponseWrapper<Void> deleteStatefulSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "StatefulSet名称", required = true)
            @PathVariable @NotBlank(message = "StatefulSet名称不能为空") String name) {
        StatefulSet statefulSet = kubernetesService.getStatefulSet(namespace, name);
        if (statefulSet == null) {
            return new ResponseWrapper().fail("StatefulSet不存在");
        }
        kubernetesService.deleteStatefulSet(statefulSet, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "扩容/缩容StatefulSet", description = "调整StatefulSet的副本数量")
    @PutMapping("/namespaces/{namespace}/statefulsets/{name}/scale")
    public ResponseWrapper<StatefulSet> scaleStatefulSet(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "StatefulSet名称", required = true)
            @PathVariable @NotBlank(message = "StatefulSet名称不能为空") String name,
            @Parameter(description = "目标副本数", required = true)
            @RequestParam @NotBlank(message = "副本数不能为空") Integer replicas) {
        if (replicas < 0) {
            return new ResponseWrapper().fail("副本数不能小于0");
        }
        return new ResponseWrapper().success(kubernetesService.scaleStatefulSet(namespace, name, replicas));
    }
}
