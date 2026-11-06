package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kubernetes HPA (HorizontalPodAutoscaler) 管理接口
 */
@Tag(name = "Kubernetes HPA接口", description = "HPA的列表、详情与min/max副本数调整接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeHpaController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的HPA列表")
    @GetMapping("/namespaces/{namespace}/horizontalpodautoscalers")
    public ResponseWrapper<List<HorizontalPodAutoscaler>> listHpas(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listHorizontalPodAutoscalers(namespace));
    }

    @Operation(summary = "获取单个HPA详情")
    @GetMapping("/namespaces/{namespace}/horizontalpodautoscalers/{name}")
    public ResponseWrapper<HorizontalPodAutoscaler> getHpa(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "HPA名称", required = true)
            @PathVariable @NotBlank(message = "HPA名称不能为空") String name) {
        HorizontalPodAutoscaler hpa = kubernetesService.getHorizontalPodAutoscaler(namespace, name);
        if (hpa == null) {
            return new ResponseWrapper().fail("HPA不存在");
        }
        return new ResponseWrapper().success(hpa);
    }

    @Operation(summary = "调整HPA的min/max副本数")
    @PutMapping("/namespaces/{namespace}/horizontalpodautoscalers/{name}/minmax")
    public ResponseWrapper<HorizontalPodAutoscaler> updateHpaMinMax(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "HPA名称", required = true)
            @PathVariable @NotBlank(message = "HPA名称不能为空") String name,
            @Parameter(description = "最小副本数")
            @RequestParam(required = false) Integer minReplicas,
            @Parameter(description = "最大副本数", required = true)
            @RequestParam(required = false) Integer maxReplicas) {
        if ((minReplicas == null && maxReplicas == null)) {
            return new ResponseWrapper().fail("minReplicas与maxReplicas不能同时为空");
        }
        if (maxReplicas != null && maxReplicas <= 0) {
            return new ResponseWrapper().fail("maxReplicas必须大于0");
        }
        if (minReplicas != null && minReplicas < 1) {
            return new ResponseWrapper().fail("minReplicas不能小于1");
        }
        if (minReplicas != null && maxReplicas != null && minReplicas > maxReplicas) {
            return new ResponseWrapper().fail("minReplicas不能大于maxReplicas");
        }
        HorizontalPodAutoscaler updated = kubernetesService.updateHorizontalPodAutoscalerReplicas(
                namespace, name, minReplicas, maxReplicas);
        if (updated == null) {
            return new ResponseWrapper().fail("HPA不存在");
        }
        return new ResponseWrapper().success(updated);
    }
}
