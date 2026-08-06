package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Service;
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
 * Kubernetes Service 服务管理接口
 */
@Tag(name = "Kubernetes Service管理接口", description = "Service的CRUD、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeServiceController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的Service列表", description = "查询某个命名空间中所有的Service")
    @GetMapping("/namespaces/{namespace}/services")
    public ResponseWrapper<List<Service>> listServices(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listServices(namespace));
    }

    @Operation(summary = "获取单个Service详情", description = "根据命名空间和名称查询Service详细信息")
    @GetMapping("/namespaces/{namespace}/services/{name}")
    public ResponseWrapper<Service> getService(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service名称", required = true)
            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
        Service service = kubernetesService.getService(namespace, name);
        if (service == null) {
            return new ResponseWrapper().fail("Service不存在");
        }
        return new ResponseWrapper().success(service);
    }

    @Operation(summary = "创建Service", description = "在指定命名空间下创建新的Service")
    @PostMapping("/namespaces/{namespace}/services")
    public ResponseWrapper<Void> createService(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service对象", required = true)
            @RequestBody @Validated Service service) {
        kubernetesService.createService(service, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "更新Service", description = "更新指定命名空间下的Service配置")
    @PutMapping("/namespaces/{namespace}/services/{name}")
    public ResponseWrapper<Service> updateService(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service名称", required = true)
            @PathVariable @NotBlank(message = "Service名称不能为空") String name,
            @Parameter(description = "更新后的Service对象", required = true)
            @RequestBody @Validated Service service) {
        Service updated = kubernetesService.updateService(service, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除Service", description = "删除指定命名空间下的Service")
    @DeleteMapping("/namespaces/{namespace}/services/{name}")
    public ResponseWrapper<Void> deleteService(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service名称", required = true)
            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
        Service service = kubernetesService.getService(namespace, name);
        if (service == null) {
            return new ResponseWrapper().fail("Service不存在");
        }
        kubernetesService.deleteService(service, namespace);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "获取Service状态", description = "查询Service的状态信息")
    @GetMapping("/namespaces/{namespace}/services/{name}/status")
    public ResponseWrapper<Map<String, Object>> getServiceStatus(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service名称", required = true)
            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
        Map<String, Object> status = kubernetesService.getServiceStatus(namespace, name);
        if (status.isEmpty()) {
            return new ResponseWrapper().fail("Service不存在");
        }
        return new ResponseWrapper().success(status);
    }

    @Operation(summary = "检查Service是否存在", description = "查询指定的Service是否存在")
    @GetMapping("/namespaces/{namespace}/services/{name}/exists")
    public ResponseWrapper<Boolean> isServiceExists(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Service名称", required = true)
            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
        return new ResponseWrapper().success(kubernetesService.isServiceExists(namespace, name));
    }
}
