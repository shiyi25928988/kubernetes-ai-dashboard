package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Node;
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
 * Kubernetes Node 节点管理接口（只读：列表与详情）
 */
@Tag(name = "Kubernetes Node节点管理接口", description = "节点列表、节点详情查询")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeNodeController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有节点列表", description = "查询Kubernetes集群中所有节点（含状态、角色、版本等自动信息）")
    @GetMapping("/nodes")
    public ResponseWrapper<List<Node>> listNodes() {
        return new ResponseWrapper().success(kubernetesService.listNodes());
    }

    @Operation(summary = "获取单个节点详情")
    @GetMapping("/nodes/{name}")
    public ResponseWrapper<Node> getNode(
            @Parameter(description = "节点名称", required = true)
            @PathVariable @NotBlank(message = "节点名称不能为空") String name) {
        Node node = kubernetesService.getNode(name);
        if (node == null) {
            return new ResponseWrapper().fail("节点不存在: " + name);
        }
        return new ResponseWrapper().success(node);
    }
}
