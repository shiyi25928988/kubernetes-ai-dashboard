package cc.shiyi.dashboard.controller;
import cc.shiyi.coleditor.common.http.ResponseWrapper;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
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
 * Kubernetes资源管理接口
 * 提供Kubernetes各类资源的CRUD、状态查询、操作等功能，供前端调用
 */
//@Tag(name = "Kubernetes资源管理接口", description = "Kubernetes各类资源的CRUD、状态查询、操作接口")
//@RestController
//@RequestMapping("/api/v1/k8s")
//@Validated
public class KubeController{}
//public class KubeController {
//    @Autowired
//    private cc.shiyi.dashboard.service.KubernetesService kubernetesService;
//    // ==================== Namespace 命名空间相关接口 ====================
//    @Operation(summary = "获取所有命名空间列表", description = "查询Kubernetes集群中所有的命名空间")
//    @GetMapping("/namespaces")
//    public ResponseWrapper<NamespaceList> listNamespaces() {
//        return new ResponseWrapper().success(kubernetesService.listNamespaces());
//    }
//    @Operation(summary = "创建命名空间", description = "创建新的Kubernetes命名空间")
//    @PostMapping("/namespaces")
//    public ResponseWrapper<Namespace> createNamespace(
//            @Parameter(description = "命名空间对象", required = true)
//            @RequestBody @Validated Namespace namespace) {
//        Namespace created = kubernetesService.createNamespace(namespace);
//        return new ResponseWrapper().success(created);
//    }
//    @Operation(summary = "删除命名空间", description = "删除指定的Kubernetes命名空间")
//    @DeleteMapping("/namespaces/{namespace}")
//    public ResponseWrapper<Void> deleteNamespace(
//            @Parameter(description = "要删除的命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
//        Namespace ns = new Namespace();
//        ns.getMetadata().setName(namespace);
//        kubernetesService.deleteNamespace(ns);
//        return new ResponseWrapper().success();
//    }
//    // ==================== Deployment 相关接口 ====================
//    @Operation(summary = "获取指定命名空间下的Deployment列表", description = "查询某个命名空间中所有的Deployment")
//    @GetMapping("/namespaces/{namespace}/deployments")
//    public ResponseWrapper<List<Deployment>> listDeployments(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
//        return new ResponseWrapper().success(kubernetesService.listDeployments(namespace));
//    }
//    @Operation(summary = "获取单个Deployment详情", description = "根据命名空间和名称查询Deployment详细信息")
//    @GetMapping("/namespaces/{namespace}/deployments/{name}")
//    public ResponseWrapper<Deployment> getDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
//        Deployment deployment = kubernetesService.getDeployment(namespace, name);
//        if (deployment == null) {
//            return new ResponseWrapper().fail("Deployment不存在");
//        }
//        return new ResponseWrapper().success(deployment);
//    }
//    @Operation(summary = "创建Deployment", description = "在指定命名空间下创建新的Deployment")
//    @PostMapping("/namespaces/{namespace}/deployments")
//    public ResponseWrapper<Deployment> createDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment对象", required = true)
//            @RequestBody @Validated Deployment deployment) {
//        kubernetesService.createDeployment(deployment, namespace);
//        return new ResponseWrapper().success(deployment);
//    }
//    @Operation(summary = "更新Deployment", description = "更新指定命名空间下的Deployment配置")
//    @PutMapping("/namespaces/{namespace}/deployments/{name}")
//    public ResponseWrapper<Deployment> updateDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name,
//            @Parameter(description = "更新后的Deployment对象", required = true)
//            @RequestBody @Validated Deployment deployment) {
//        Deployment updated = kubernetesService.updateDeployment(deployment, namespace);
//        return new ResponseWrapper().success(updated);
//    }
//    @Operation(summary = "删除Deployment", description = "删除指定命名空间下的Deployment")
//    @DeleteMapping("/namespaces/{namespace}/deployments/{name}")
//    public ResponseWrapper<Void> deleteDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
//        Deployment deployment = kubernetesService.getDeployment(namespace, name);
//        if (deployment == null) {
//            return new ResponseWrapper().fail("Deployment不存在");
//        }
//        kubernetesService.deleteDeployment(deployment, namespace);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "扩容/缩容Deployment", description = "调整Deployment的副本数量")
//    @PutMapping("/namespaces/{namespace}/deployments/{name}/scale")
//    public ResponseWrapper<Deployment> scaleDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name,
//            @Parameter(description = "目标副本数", required = true)
//            @RequestParam @NotBlank(message = "副本数不能为空") Integer replicas) {
//        if (replicas < 0) {
//            return new ResponseWrapper().fail("副本数不能小于0");
//        }
//        Deployment deployment = kubernetesService.scaleDeployment(namespace, name, replicas);
//        return new ResponseWrapper().success(deployment);
//    }
//    @Operation(summary = "重启Deployment", description = "触发Deployment的滚动重启操作")
//    @PutMapping("/namespaces/{namespace}/deployments/{name}/restart")
//    public ResponseWrapper<Deployment> restartDeployment(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
//        Deployment deployment = kubernetesService.restartDeployment(namespace, name);
//        return new ResponseWrapper().success(deployment);
//    }
//    @Operation(summary = "获取Deployment状态", description = "查询Deployment的运行状态信息")
//    @GetMapping("/namespaces/{namespace}/deployments/{name}/status")
//    public ResponseWrapper<Map<String, Object>> getDeploymentStatus(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
//        Map<String, Object> status = kubernetesService.getDeploymentStatus(namespace, name);
//        if (status.isEmpty()) {
//            return new ResponseWrapper().fail("Deployment不存在");
//        }
//        return new ResponseWrapper().success(status);
//    }
//    @Operation(summary = "检查Deployment是否存在", description = "查询指定的Deployment是否存在")
//    @GetMapping("/namespaces/{namespace}/deployments/{name}/exists")
//    public ResponseWrapper<Boolean> isDeploymentExists(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Deployment名称", required = true)
//            @PathVariable @NotBlank(message = "Deployment名称不能为空") String name) {
//        return new ResponseWrapper().success(kubernetesService.isDeploymentExists(namespace, name));
//    }
//    // ==================== PVC 持久化存储卷相关接口 ====================
//    @Operation(summary = "获取指定命名空间下的PVC列表", description = "查询某个命名空间中所有的PersistentVolumeClaim")
//    @GetMapping("/namespaces/{namespace}/pvcs")
//    public ResponseWrapper<List<PersistentVolumeClaim>> listPVCs(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
//        return new ResponseWrapper().success(kubernetesService.listPVCs(namespace));
//    }
//    @Operation(summary = "获取单个PVC详情", description = "根据命名空间和名称查询PVC详细信息")
//    @GetMapping("/namespaces/{namespace}/pvcs/{name}")
//    public ResponseWrapper<PersistentVolumeClaim> getPVC(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC名称", required = true)
//            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
//        PersistentVolumeClaim pvc = kubernetesService.getPVC(namespace, name);
//        if (pvc == null) {
//            return new ResponseWrapper().fail("PVC不存在");
//        }
//        return new ResponseWrapper().success(pvc);
//    }
//    @Operation(summary = "创建PVC", description = "在指定命名空间下创建新的PersistentVolumeClaim")
//    @PostMapping("/namespaces/{namespace}/pvcs")
//    public ResponseWrapper<PersistentVolumeClaim> createPVC(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC对象", required = true)
//            @RequestBody @Validated PersistentVolumeClaim pvc) {
//        PersistentVolumeClaim created = kubernetesService.createPVC(pvc, namespace);
//        return new ResponseWrapper().success(created);
//    }
//    @Operation(summary = "更新PVC", description = "更新指定命名空间下的PVC配置")
//    @PutMapping("/namespaces/{namespace}/pvcs/{name}")
//    public ResponseWrapper<PersistentVolumeClaim> updatePVC(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC名称", required = true)
//            @PathVariable @NotBlank(message = "PVC名称不能为空") String name,
//            @Parameter(description = "更新后的PVC对象", required = true)
//            @RequestBody @Validated PersistentVolumeClaim pvc) {
//        PersistentVolumeClaim updated = kubernetesService.updatePVC(pvc, namespace);
//        return new ResponseWrapper().success(updated);
//    }
//    @Operation(summary = "删除PVC", description = "删除指定命名空间下的PVC")
//    @DeleteMapping("/namespaces/{namespace}/pvcs/{name}")
//    public ResponseWrapper<Void> deletePVC(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC名称", required = true)
//            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
//        if (!kubernetesService.isPVCExists(namespace, name)) {
//            return new ResponseWrapper().fail("PVC不存在");
//        }
//        kubernetesService.deletePVC(namespace, name);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "获取PVC状态", description = "查询PVC的状态信息")
//    @GetMapping("/namespaces/{namespace}/pvcs/{name}/status")
//    public ResponseWrapper<Map<String, Object>> getPVCStatus(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC名称", required = true)
//            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
//        Map<String, Object> status = kubernetesService.getPVCStatus(namespace, name);
//        if (status.isEmpty()) {
//            return new ResponseWrapper().fail("PVC不存在");
//        }
//        return new ResponseWrapper().success(status);
//    }
//    @Operation(summary = "检查PVC是否存在", description = "查询指定的PVC是否存在")
//    @GetMapping("/namespaces/{namespace}/pvcs/{name}/exists")
//    public ResponseWrapper<Boolean> isPVCExists(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "PVC名称", required = true)
//            @PathVariable @NotBlank(message = "PVC名称不能为空") String name) {
//        return new ResponseWrapper().success(kubernetesService.isPVCExists(namespace, name));
//    }
//    // ==================== Service 服务相关接口 ====================
//    @Operation(summary = "获取指定命名空间下的Service列表", description = "查询某个命名空间中所有的Service")
//    @GetMapping("/namespaces/{namespace}/services")
//    public ResponseWrapper<List<io.fabric8.kubernetes.api.model.Service>> listServices(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
//        return new ResponseWrapper().success(kubernetesService.listServices(namespace));
//    }
//    @Operation(summary = "获取单个Service详情", description = "根据命名空间和名称查询Service详细信息")
//    @GetMapping("/namespaces/{namespace}/services/{name}")
//    public ResponseWrapper<io.fabric8.kubernetes.api.model.Service> getService(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service名称", required = true)
//            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
//        io.fabric8.kubernetes.api.model.Service service = kubernetesService.getService(namespace, name);
//        if (service == null) {
//            return new ResponseWrapper().fail("Service不存在");
//        }
//        return new ResponseWrapper().success(service);
//    }
//    @Operation(summary = "创建Service", description = "在指定命名空间下创建新的Service")
//    @PostMapping("/namespaces/{namespace}/services")
//    public ResponseWrapper<Void> createService(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service对象", required = true)
//            @RequestBody @Validated io.fabric8.kubernetes.api.model.Service service) {
//        kubernetesService.createService(service, namespace);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "更新Service", description = "更新指定命名空间下的Service配置")
//    @PutMapping("/namespaces/{namespace}/services/{name}")
//    public ResponseWrapper<io.fabric8.kubernetes.api.model.Service> updateService(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service名称", required = true)
//            @PathVariable @NotBlank(message = "Service名称不能为空") String name,
//            @Parameter(description = "更新后的Service对象", required = true)
//            @RequestBody @Validated io.fabric8.kubernetes.api.model.Service service) {
//        io.fabric8.kubernetes.api.model.Service updated = kubernetesService.updateService(service, namespace);
//        return new ResponseWrapper().success(updated);
//    }
//    @Operation(summary = "删除Service", description = "删除指定命名空间下的Service")
//    @DeleteMapping("/namespaces/{namespace}/services/{name}")
//    public ResponseWrapper<Void> deleteService(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service名称", required = true)
//            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
//        io.fabric8.kubernetes.api.model.Service service = kubernetesService.getService(namespace, name);
//        if (service == null) {
//            return new ResponseWrapper().fail("Service不存在");
//        }
//        kubernetesService.deleteService(service, namespace);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "获取Service状态", description = "查询Service的状态信息")
//    @GetMapping("/namespaces/{namespace}/services/{name}/status")
//    public ResponseWrapper<Map<String, Object>> getServiceStatus(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service名称", required = true)
//            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
//        Map<String, Object> status = kubernetesService.getServiceStatus(namespace, name);
//        if (status.isEmpty()) {
//            return new ResponseWrapper().fail("Service不存在");
//        }
//        return new ResponseWrapper().success(status);
//    }
//    @Operation(summary = "检查Service是否存在", description = "查询指定的Service是否存在")
//    @GetMapping("/namespaces/{namespace}/services/{name}/exists")
//    public ResponseWrapper<Boolean> isServiceExists(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Service名称", required = true)
//            @PathVariable @NotBlank(message = "Service名称不能为空") String name) {
//        return new ResponseWrapper().success(kubernetesService.isServiceExists(namespace, name));
//    }
//    // ==================== Ingress 路由相关接口 ====================
//    @Operation(summary = "获取指定命名空间下的Ingress列表", description = "查询某个命名空间中所有的Ingress")
//    @GetMapping("/namespaces/{namespace}/ingresses")
//    public ResponseWrapper<List<Ingress>> listIngresses(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
//        return new ResponseWrapper().success(kubernetesService.listIngresses(namespace));
//    }
//    @Operation(summary = "获取单个Ingress详情", description = "根据命名空间和名称查询Ingress详细信息")
//    @GetMapping("/namespaces/{namespace}/ingresses/{name}")
//    public ResponseWrapper<Ingress> getIngress(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress名称", required = true)
//            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
//        Ingress ingress = kubernetesService.getIngress(namespace, name);
//        if (ingress == null) {
//            return new ResponseWrapper().fail("Ingress不存在");
//        }
//        return new ResponseWrapper().success(ingress);
//    }
//    @Operation(summary = "创建Ingress", description = "在指定命名空间下创建新的Ingress")
//    @PostMapping("/namespaces/{namespace}/ingresses")
//    public ResponseWrapper<Void> createIngress(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress对象", required = true)
//            @RequestBody @Validated Ingress ingress) {
//        kubernetesService.createIngress(ingress, namespace);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "更新Ingress", description = "更新指定命名空间下的Ingress配置")
//    @PutMapping("/namespaces/{namespace}/ingresses/{name}")
//    public ResponseWrapper<Ingress> updateIngress(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress名称", required = true)
//            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name,
//            @Parameter(description = "更新后的Ingress对象", required = true)
//            @RequestBody @Validated Ingress ingress) {
//        Ingress updated = kubernetesService.updateIngress(ingress, namespace);
//        return new ResponseWrapper().success(updated);
//    }
//    @Operation(summary = "删除Ingress", description = "删除指定命名空间下的Ingress")
//    @DeleteMapping("/namespaces/{namespace}/ingresses/{name}")
//    public ResponseWrapper<Void> deleteIngress(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress名称", required = true)
//            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
//        Ingress ingress = kubernetesService.getIngress(namespace, name);
//        if (ingress == null) {
//            return new ResponseWrapper().fail("Ingress不存在");
//        }
//        kubernetesService.deleteIngress(ingress, namespace);
//        return new ResponseWrapper().success();
//    }
//    @Operation(summary = "获取Ingress状态", description = "查询Ingress的状态信息")
//    @GetMapping("/namespaces/{namespace}/ingresses/{name}/status")
//    public ResponseWrapper<Map<String, Object>> getIngressStatus(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress名称", required = true)
//            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
//        Map<String, Object> status = kubernetesService.getIngressStatus(namespace, name);
//        if (status.isEmpty()) {
//            return new ResponseWrapper().fail("Ingress不存在");
//        }
//        return new ResponseWrapper().success(status);
//    }
//    @Operation(summary = "检查Ingress是否存在", description = "查询指定的Ingress是否存在")
//    @GetMapping("/namespaces/{namespace}/ingresses/{name}/exists")
//    public ResponseWrapper<Boolean> isIngressExists(
//            @Parameter(description = "命名空间名称", required = true)
//            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
//            @Parameter(description = "Ingress名称", required = true)
//            @PathVariable @NotBlank(message = "Ingress名称不能为空") String name) {
//        return new ResponseWrapper().success(kubernetesService.isIngressExists(namespace, name));
//    }
//}