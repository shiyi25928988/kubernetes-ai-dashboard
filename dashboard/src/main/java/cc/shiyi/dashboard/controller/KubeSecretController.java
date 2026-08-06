package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Secret;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kubernetes Secret管理接口")
@RestController
@RequestMapping("/api/v1/k8s")
public class KubeSecretController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的Secret列表")
    @GetMapping("/namespaces/{namespace}/secrets")
    public ResponseWrapper<List<Secret>> listSecrets(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace) {
        return new ResponseWrapper().success(kubernetesService.listSecrets(namespace));
    }

    @Operation(summary = "获取单个Secret详情")
    @GetMapping("/namespaces/{namespace}/secrets/{name}")
    public ResponseWrapper<Secret> getSecret(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Secret名称", required = true)
            @PathVariable String name) {
        return new ResponseWrapper().success(kubernetesService.getSecret(namespace, name));
    }

    @Operation(summary = "创建Secret")
    @PostMapping("/namespaces/{namespace}/secrets")
    public ResponseWrapper<Secret> createSecret(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @RequestBody Secret secret) {
        return new ResponseWrapper().success(kubernetesService.createSecret(secret, namespace));
    }

    @Operation(summary = "更新Secret")
    @PutMapping("/namespaces/{namespace}/secrets/{name}")
    public ResponseWrapper<Secret> updateSecret(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Secret名称", required = true)
            @PathVariable String name,
            @RequestBody Secret secret) {
        secret.getMetadata().setName(name);
        secret.getMetadata().setNamespace(namespace);
        return new ResponseWrapper().success(kubernetesService.updateSecret(secret, namespace));
    }

    @Operation(summary = "删除Secret")
    @DeleteMapping("/namespaces/{namespace}/secrets/{name}")
    public ResponseWrapper<Void> deleteSecret(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Secret名称", required = true)
            @PathVariable String name) {
        kubernetesService.deleteSecret(namespace, name);
        return new ResponseWrapper().success(null);
    }
}
