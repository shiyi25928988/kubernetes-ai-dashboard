package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kubernetes CRD管理接口")
@RestController
@RequestMapping("/api/v1/k8s")
public class KubeCrdController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有CRD列表")
    @GetMapping("/crds")
    public ResponseWrapper<List<CustomResourceDefinition>> listCrds() {
        return new ResponseWrapper().success(kubernetesService.listCrds());
    }

    @Operation(summary = "获取单个CRD详情")
    @GetMapping("/crds/{name}")
    public ResponseWrapper<CustomResourceDefinition> getCrd(
            @Parameter(description = "CRD名称", required = true)
            @PathVariable String name) {
        return new ResponseWrapper().success(kubernetesService.getCrd(name));
    }
}
