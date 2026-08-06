package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Pod;
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
 * Kubernetes Pod 管理接口
 */
@Tag(name = "Kubernetes Pod管理接口", description = "Pod的查询、删除、日志、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubePodController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有Pod列表", description = "查询所有命名空间中所有的Pod")
    @GetMapping("/pods")
    public ResponseWrapper<List<Pod>> listAllPods() {
        return new ResponseWrapper().success(kubernetesService.listAllPods());
    }

    @Operation(summary = "获取指定命名空间下的Pod列表", description = "查询某个命名空间中所有的Pod")
    @GetMapping("/namespaces/{namespace}/pods")
    public ResponseWrapper<List<Pod>> listPods(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listPods(namespace));
    }

    @Operation(summary = "获取单个Pod详情", description = "根据命名空间和名称查询Pod详细信息")
    @GetMapping("/namespaces/{namespace}/pods/{name}")
    public ResponseWrapper<Pod> getPod(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Pod名称", required = true)
            @PathVariable @NotBlank(message = "Pod名称不能为空") String name) {
        Pod pod = kubernetesService.getPod(namespace, name);
        if (pod == null) {
            return new ResponseWrapper().fail("Pod不存在");
        }
        return new ResponseWrapper().success(pod);
    }

    @Operation(summary = "删除Pod", description = "删除指定命名空间下的Pod")
    @DeleteMapping("/namespaces/{namespace}/pods/{name}")
    public ResponseWrapper<Void> deletePod(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Pod名称", required = true)
            @PathVariable @NotBlank(message = "Pod名称不能为空") String name) {
        Pod pod = kubernetesService.getPod(namespace, name);
        if (pod == null) {
            return new ResponseWrapper().fail("Pod不存在");
        }
        kubernetesService.deletePod(pod, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "获取Pod日志", description = "查询指定Pod的日志内容")
    @GetMapping("/namespaces/{namespace}/pods/{name}/logs")
    public ResponseWrapper<String> getPodLogs(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Pod名称", required = true)
            @PathVariable @NotBlank(message = "Pod名称不能为空") String name) {
        String logs = kubernetesService.getPodLogs(namespace, name);
        if (logs == null) {
            return new ResponseWrapper().fail("Pod不存在");
        }
        return new ResponseWrapper().success(logs);
    }

    @Operation(summary = "获取Pod状态", description = "查询Pod的运行状态信息")
    @GetMapping("/namespaces/{namespace}/pods/{name}/status")
    public ResponseWrapper<Map<String, Object>> getPodStatus(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Pod名称", required = true)
            @PathVariable @NotBlank(message = "Pod名称不能为空") String name) {
        Map<String, Object> status = kubernetesService.getPodStatus(namespace, name);
        if (status.isEmpty()) {
            return new ResponseWrapper().fail("Pod不存在");
        }
        return new ResponseWrapper().success(status);
    }

    @Operation(summary = "检查Pod是否存在", description = "查询指定的Pod是否存在")
    @GetMapping("/namespaces/{namespace}/pods/{name}/exists")
    public ResponseWrapper<Boolean> isPodExists(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Pod名称", required = true)
            @PathVariable @NotBlank(message = "Pod名称不能为空") String name) {
        return new ResponseWrapper().success(kubernetesService.isPodExists(namespace, name));
    }
}
