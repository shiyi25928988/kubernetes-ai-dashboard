package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.apps.Deployment;
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
 * Kubernetes Deployment 管理接口
 */
@Tag(name = "Kubernetes Deployment管理接口", description = "Deployment的CRUD、扩缩容、重启、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeDeploymentController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的Deployment列表", description = "查询某个命名空间中所有的Deployment")
    @GetMapping("/namespaces/{namespace}/deployments")
    public ResponseWrapper<List<Deployment>> listDeployments(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listDeployments(namespace));
    }

    @Operation(summary = "获取单个Deployment详情", description = "根据命名空间和名称查询Deployment详细信息")
    @GetMapping("/namespaces/{namespace}/deployments/{name}")
    public ResponseWrapper<Deployment> getDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
        Deployment deployment = kubernetesService.getDeployment(namespace, name);
        if (deployment == null) {
            return new ResponseWrapper().fail("Deployment不存在");
        }
        return new ResponseWrapper().success(deployment);
    }

    @Operation(summary = "创建Deployment", description = "在指定命名空间下创建新的Deployment")
    @PostMapping("/namespaces/{namespace}/deployments")
    public ResponseWrapper<Deployment> createDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment对象", required = true)
            @RequestBody @Validated Deployment deployment) {
        kubernetesService.createDeployment(deployment, namespace);
        return new ResponseWrapper().success(deployment);
    }

    @Operation(summary = "更新Deployment", description = "更新指定命名空间下的Deployment配置")
    @PutMapping("/namespaces/{namespace}/deployments/{name}")
    public ResponseWrapper<Deployment> updateDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name,
            @Parameter(description = "更新后的Deployment对象", required = true)
            @RequestBody @Validated Deployment deployment) {
        Deployment updated = kubernetesService.updateDeployment(deployment, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除Deployment", description = "删除指定命名空间下的Deployment")
    @DeleteMapping("/namespaces/{namespace}/deployments/{name}")
    public ResponseWrapper<Void> deleteDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
        Deployment deployment = kubernetesService.getDeployment(namespace, name);
        if (deployment == null) {
            return new ResponseWrapper().fail("Deployment不存在");
        }
        kubernetesService.deleteDeployment(deployment, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "扩容/缩容Deployment", description = "调整Deployment的副本数量")
    @PutMapping("/namespaces/{namespace}/deployments/{name}/scale")
    public ResponseWrapper<Deployment> scaleDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name,
            @Parameter(description = "目标副本数", required = true)
            @RequestParam @NotBlank(message = "副本数不能为空") Integer replicas) {
        if (replicas < 0) {
            return new ResponseWrapper().fail("副本数不能小于0");
        }
        Deployment deployment = kubernetesService.scaleDeployment(namespace, name, replicas);
        return new ResponseWrapper().success(deployment);
    }

    @Operation(summary = "重启Deployment", description = "触发Deployment的滚动重启操作")
    @PutMapping("/namespaces/{namespace}/deployments/{name}/restart")
    public ResponseWrapper<Deployment> restartDeployment(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
        Deployment deployment = kubernetesService.restartDeployment(namespace, name);
        return new ResponseWrapper().success(deployment);
    }

    @Operation(summary = "获取Deployment状态", description = "查询Deployment的运行状态信息")
    @GetMapping("/namespaces/{namespace}/deployments/{name}/status")
    public ResponseWrapper<Map<String, Object>> getDeploymentStatus(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
        Map<String, Object> status = kubernetesService.getDeploymentStatus(namespace, name);
        if (status.isEmpty()) {
            return new ResponseWrapper().fail("Deployment不存在");
        }
        return new ResponseWrapper().success(status);
    }

    @Operation(summary = "检查Deployment是否存在", description = "查询指定的Deployment是否存在")
    @GetMapping("/namespaces/{namespace}/deployments/{name}/exists")
    public ResponseWrapper<Boolean> isDeploymentExists(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Deployment名称", required = true)
            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
        return new ResponseWrapper().success(kubernetesService.isDeploymentExists(namespace, name));
    }
}

