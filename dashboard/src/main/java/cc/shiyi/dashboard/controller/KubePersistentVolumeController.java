package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.PersistentVolume;
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
 * Kubernetes PersistentVolume 查询接口（只读）
 * （StorageClass 列表已在 KubeVolumeController 提供 /storageclasses）
 */
@Tag(name = "Kubernetes PersistentVolume接口", description = "PersistentVolume的查询接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubePersistentVolumeController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有PersistentVolume列表")
    @GetMapping("/persistentvolumes")
    public ResponseWrapper<List<PersistentVolume>> listPVs() {
        return new ResponseWrapper().success(kubernetesService.listPersistentVolumes());
    }

    @Operation(summary = "获取单个PersistentVolume详情")
    @GetMapping("/persistentvolumes/{name}")
    public ResponseWrapper<PersistentVolume> getPV(
            @Parameter(description = "PersistentVolume名称", required = true)
            @PathVariable @NotBlank(message = "名称不能为空") String name) {
        PersistentVolume pv = kubernetesService.getPersistentVolume(name);
        if (pv == null) {
            return new ResponseWrapper().fail("PersistentVolume不存在: " + name);
        }
        return new ResponseWrapper().success(pv);
    }
}
