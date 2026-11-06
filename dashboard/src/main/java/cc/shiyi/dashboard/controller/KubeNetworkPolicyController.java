package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kubernetes NetworkPolicy 查询接口（只读）
 */
@Tag(name = "Kubernetes NetworkPolicy接口", description = "网络策略的查询接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeNetworkPolicyController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的NetworkPolicy列表")
    @GetMapping("/namespaces/{namespace}/networkpolicies")
    public ResponseWrapper<List<NetworkPolicy>> listNetworkPolicies(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listNetworkPolicies(namespace));
    }

    @Operation(summary = "获取单个NetworkPolicy详情")
    @GetMapping("/namespaces/{namespace}/networkpolicies/{name}")
    public ResponseWrapper<NetworkPolicy> getNetworkPolicy(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "NetworkPolicy名称", required = true)
            @PathVariable @NotBlank(message = "NetworkPolicy名称不能为空") String name) {
        NetworkPolicy np = kubernetesService.getNetworkPolicy(namespace, name);
        if (np == null) {
            return new ResponseWrapper().fail("NetworkPolicy不存在");
        }
        return new ResponseWrapper().success(np);
    }
}
