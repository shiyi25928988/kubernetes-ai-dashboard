package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kubernetes ConfigMap管理接口")
@RestController
@RequestMapping("/api/v1/k8s")
public class KubeConfigMapController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的ConfigMap列表")
    @GetMapping("/namespaces/{namespace}/configmaps")
    public ResponseWrapper<List<ConfigMap>> listConfigMaps(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace) {
        return new ResponseWrapper().success(kubernetesService.listConfigMaps(namespace));
    }

    @Operation(summary = "获取单个ConfigMap详情")
    @GetMapping("/namespaces/{namespace}/configmaps/{name}")
    public ResponseWrapper<ConfigMap> getConfigMap(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "ConfigMap名称", required = true)
            @PathVariable String name) {
        return new ResponseWrapper().success(kubernetesService.getConfigMap(namespace, name));
    }

    @Operation(summary = "创建ConfigMap")
    @PostMapping("/namespaces/{namespace}/configmaps")
    public ResponseWrapper<ConfigMap> createConfigMap(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @RequestBody ConfigMap configMap) {
        return new ResponseWrapper().success(kubernetesService.createConfigMap(configMap, namespace));
    }

    @Operation(summary = "更新ConfigMap")
    @PutMapping("/namespaces/{namespace}/configmaps/{name}")
    public ResponseWrapper<ConfigMap> updateConfigMap(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "ConfigMap名称", required = true)
            @PathVariable String name,
            @RequestBody ConfigMap configMap) {
        configMap.getMetadata().setName(name);
        configMap.getMetadata().setNamespace(namespace);
        return new ResponseWrapper().success(kubernetesService.updateConfigMap(configMap, namespace));
    }

    @Operation(summary = "删除ConfigMap")
    @DeleteMapping("/namespaces/{namespace}/configmaps/{name}")
    public ResponseWrapper<Void> deleteConfigMap(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "ConfigMap名称", required = true)
            @PathVariable String name) {
        kubernetesService.deleteConfigMap(namespace, name);
        return new ResponseWrapper().success(null);
    }
}
