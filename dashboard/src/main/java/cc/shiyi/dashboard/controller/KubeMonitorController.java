package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * K8S集群监控接口
 */
@Tag(name = "K8S集群监控接口", description = "集群资源统计、健康状态查询等接口")
@RestController
@RequestMapping("/api/v1/k8s/cluster")
@Validated
public class KubeMonitorController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取集群资源统计", description = "查询集群整体资源使用、工作负载统计、健康状态")
    @GetMapping("/stats")
    public ResponseWrapper<Map<String, Object>> getClusterStats() {
        return new ResponseWrapper().success(kubernetesService.getClusterStats());
    }
}