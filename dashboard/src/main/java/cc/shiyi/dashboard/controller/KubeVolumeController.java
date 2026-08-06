package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
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
 * Kubernetes 存储卷（PVC）管理接口
 */
@Tag(name = "Kubernetes PVC存储卷管理接口", description = "PersistentVolumeClaim的CRUD、状态查询等操作接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeVolumeController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的PVC列表", description = "查询某个命名空间中所有的PersistentVolumeClaim")
    @GetMapping("/namespaces/{namespace}/pvcs")
    public ResponseWrapper<List<PersistentVolumeClaim>> listPVCs(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listPVCs(namespace));
    }

    @Operation(summary = "获取单个PVC详情", description = "根据命名空间和名称查询PVC详细信息")
    @GetMapping("/namespaces/{namespace}/pvcs/{name}")
    public ResponseWrapper<PersistentVolumeClaim> getPVC(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC名称", required = true)
            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
        PersistentVolumeClaim pvc = kubernetesService.getPVC(namespace, name);
        if (pvc == null) {
            return new ResponseWrapper().fail("PVC不存在");
        }
        return new ResponseWrapper().success(pvc);
    }

    @Operation(summary = "创建PVC", description = "在指定命名空间下创建新的PersistentVolumeClaim")
    @PostMapping("/namespaces/{namespace}/pvcs")
    public ResponseWrapper<PersistentVolumeClaim> createPVC(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC对象", required = true)
            @RequestBody @Validated PersistentVolumeClaim pvc) {
        PersistentVolumeClaim created = kubernetesService.createPVC(pvc, namespace);
        return new ResponseWrapper().success(created);
    }

    @Operation(summary = "更新PVC", description = "更新指定命名空间下的PVC配置")
    @PutMapping("/namespaces/{namespace}/pvcs/{name}")
    public ResponseWrapper<PersistentVolumeClaim> updatePVC(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC名称", required = true)
            @PathVariable @NotBlank(message = "PVC名称不能为空") String name,
            @Parameter(description = "更新后的PVC对象", required = true)
            @RequestBody @Validated PersistentVolumeClaim pvc) {
        PersistentVolumeClaim updated = kubernetesService.updatePVC(pvc, namespace);
        return new ResponseWrapper().success(updated);
    }

    @Operation(summary = "删除PVC", description = "删除指定命名空间下的PVC")
    @DeleteMapping("/namespaces/{namespace}/pvcs/{name}")
    public ResponseWrapper<Void> deletePVC(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC名称", required = true)
            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
        if (!kubernetesService.isPVCExists(namespace, name)) {
            return new ResponseWrapper().fail("PVC不存在");
        }
        kubernetesService.deletePVC(namespace, name);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "获取PVC状态", description = "查询PVC的状态信息")
    @GetMapping("/namespaces/{namespace}/pvcs/{name}/status")
    public ResponseWrapper<Map<String, Object>> getPVCStatus(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC名称", required = true)
            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
        Map<String, Object> status = kubernetesService.getPVCStatus(namespace, name);
        if (status.isEmpty()) {
            return new ResponseWrapper().fail("PVC不存在");
        }
        return new ResponseWrapper().success(status);
    }

    @Operation(summary = "检查PVC是否存在", description = "查询指定的PVC是否存在")
    @GetMapping("/namespaces/{namespace}/pvcs/{name}/exists")
    public ResponseWrapper<Boolean> isPVCExists(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "PVC名称", required = true)
            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
        return new ResponseWrapper().success(kubernetesService.isPVCExists(namespace, name));
    }

    @Operation(summary = "获取集群所有StorageClass列表")
    @GetMapping("/storageclasses")
    public ResponseWrapper<List<StorageClass>> listStorageClasses() {
        return new ResponseWrapper().success(kubernetesService.listStorageClasses());
    }
}

