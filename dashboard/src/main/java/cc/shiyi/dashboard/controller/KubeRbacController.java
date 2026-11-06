package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.rbac.ClusterRole;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kubernetes RBAC 管理接口（ServiceAccount / Role / ClusterRole / RoleBinding / ClusterRoleBinding）
 */
@Tag(name = "Kubernetes RBAC接口", description = "RBAC相关资源的查询与管理接口")
@RestController
@RequestMapping("/api/v1/k8s")
@Validated
public class KubeRbacController {

    @Autowired
    private KubernetesService kubernetesService;

    // ==================== ServiceAccount ====================

    @Operation(summary = "获取指定命名空间下的ServiceAccount列表")
    @GetMapping("/namespaces/{namespace}/serviceaccounts")
    public ResponseWrapper<List<ServiceAccount>> listServiceAccounts(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listServiceAccounts(namespace));
    }

    @Operation(summary = "获取单个ServiceAccount详情")
    @GetMapping("/namespaces/{namespace}/serviceaccounts/{name}")
    public ResponseWrapper<ServiceAccount> getServiceAccount(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ServiceAccount名称", required = true)
            @PathVariable @NotBlank(message = "ServiceAccount名称不能为空") String name) {
        ServiceAccount sa = kubernetesService.getServiceAccount(namespace, name);
        if (sa == null) {
            return new ResponseWrapper().fail("ServiceAccount不存在");
        }
        return new ResponseWrapper().success(sa);
    }

    @Operation(summary = "删除ServiceAccount", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/serviceaccounts/{name}")
    public ResponseWrapper<Void> deleteServiceAccount(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "ServiceAccount名称", required = true)
            @PathVariable @NotBlank(message = "ServiceAccount名称不能为空") String name) {
        ServiceAccount sa = kubernetesService.getServiceAccount(namespace, name);
        if (sa == null) {
            return new ResponseWrapper().fail("ServiceAccount不存在");
        }
        kubernetesService.deleteServiceAccount(sa, namespace);
        return new ResponseWrapper().success();
    }

    // ==================== Role ====================

    @Operation(summary = "获取指定命名空间下的Role列表")
    @GetMapping("/namespaces/{namespace}/roles")
    public ResponseWrapper<List<Role>> listRoles(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listRoles(namespace));
    }

    @Operation(summary = "获取单个Role详情")
    @GetMapping("/namespaces/{namespace}/roles/{name}")
    public ResponseWrapper<Role> getRole(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Role名称", required = true)
            @PathVariable @NotBlank(message = "Role名称不能为空") String name) {
        Role role = kubernetesService.getRole(namespace, name);
        if (role == null) {
            return new ResponseWrapper().fail("Role不存在");
        }
        return new ResponseWrapper().success(role);
    }

    @Operation(summary = "删除Role", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/roles/{name}")
    public ResponseWrapper<Void> deleteRole(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "Role名称", required = true)
            @PathVariable @NotBlank(message = "Role名称不能为空") String name) {
        Role role = kubernetesService.getRole(namespace, name);
        if (role == null) {
            return new ResponseWrapper().fail("Role不存在");
        }
        kubernetesService.deleteRole(role, namespace);
        return new ResponseWrapper().success();
    }

    // ==================== ClusterRole ====================

    @Operation(summary = "获取所有ClusterRole列表")
    @GetMapping("/clusterroles")
    public ResponseWrapper<List<ClusterRole>> listClusterRoles() {
        return new ResponseWrapper().success(kubernetesService.listClusterRoles());
    }

    @Operation(summary = "获取单个ClusterRole详情")
    @GetMapping("/clusterroles/{name}")
    public ResponseWrapper<ClusterRole> getClusterRole(
            @Parameter(description = "ClusterRole名称", required = true)
            @PathVariable @NotBlank(message = "ClusterRole名称不能为空") String name) {
        ClusterRole role = kubernetesService.getClusterRole(name);
        if (role == null) {
            return new ResponseWrapper().fail("ClusterRole不存在");
        }
        return new ResponseWrapper().success(role);
    }

    @Operation(summary = "删除ClusterRole", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/clusterroles/{name}")
    public ResponseWrapper<Void> deleteClusterRole(
            @Parameter(description = "ClusterRole名称", required = true)
            @PathVariable @NotBlank(message = "ClusterRole名称不能为空") String name) {
        ClusterRole role = kubernetesService.getClusterRole(name);
        if (role == null) {
            return new ResponseWrapper().fail("ClusterRole不存在");
        }
        kubernetesService.deleteClusterRole(role);
        return new ResponseWrapper().success();
    }

    // ==================== RoleBinding ====================

    @Operation(summary = "获取指定命名空间下的RoleBinding列表")
    @GetMapping("/namespaces/{namespace}/rolebindings")
    public ResponseWrapper<List<RoleBinding>> listRoleBindings(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace) {
        return new ResponseWrapper().success(kubernetesService.listRoleBindings(namespace));
    }

    @Operation(summary = "获取单个RoleBinding详情")
    @GetMapping("/namespaces/{namespace}/rolebindings/{name}")
    public ResponseWrapper<RoleBinding> getRoleBinding(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "RoleBinding名称", required = true)
            @PathVariable @NotBlank(message = "RoleBinding名称不能为空") String name) {
        RoleBinding binding = kubernetesService.getRoleBinding(namespace, name);
        if (binding == null) {
            return new ResponseWrapper().fail("RoleBinding不存在");
        }
        return new ResponseWrapper().success(binding);
    }

    @Operation(summary = "删除RoleBinding", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/namespaces/{namespace}/rolebindings/{name}")
    public ResponseWrapper<Void> deleteRoleBinding(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable @NotBlank(message = "命名空间名称不能为空") String namespace,
            @Parameter(description = "RoleBinding名称", required = true)
            @PathVariable @NotBlank(message = "RoleBinding名称不能为空") String name) {
        RoleBinding binding = kubernetesService.getRoleBinding(namespace, name);
        if (binding == null) {
            return new ResponseWrapper().fail("RoleBinding不存在");
        }
        kubernetesService.deleteRoleBinding(binding, namespace);
        return new ResponseWrapper().success();
    }

    // ==================== ClusterRoleBinding ====================

    @Operation(summary = "获取所有ClusterRoleBinding列表")
    @GetMapping("/clusterrolebindings")
    public ResponseWrapper<List<ClusterRoleBinding>> listClusterRoleBindings() {
        return new ResponseWrapper().success(kubernetesService.listClusterRoleBindings());
    }

    @Operation(summary = "获取单个ClusterRoleBinding详情")
    @GetMapping("/clusterrolebindings/{name}")
    public ResponseWrapper<ClusterRoleBinding> getClusterRoleBinding(
            @Parameter(description = "ClusterRoleBinding名称", required = true)
            @PathVariable @NotBlank(message = "ClusterRoleBinding名称不能为空") String name) {
        ClusterRoleBinding binding = kubernetesService.getClusterRoleBinding(name);
        if (binding == null) {
            return new ResponseWrapper().fail("ClusterRoleBinding不存在");
        }
        return new ResponseWrapper().success(binding);
    }

    @Operation(summary = "删除ClusterRoleBinding", description = "平台创建的资源只能由创建者删除")
    @DeleteMapping("/clusterrolebindings/{name}")
    public ResponseWrapper<Void> deleteClusterRoleBinding(
            @Parameter(description = "ClusterRoleBinding名称", required = true)
            @PathVariable @NotBlank(message = "ClusterRoleBinding名称不能为空") String name) {
        ClusterRoleBinding binding = kubernetesService.getClusterRoleBinding(name);
        if (binding == null) {
            return new ResponseWrapper().fail("ClusterRoleBinding不存在");
        }
        kubernetesService.deleteClusterRoleBinding(binding);
        return new ResponseWrapper().success();
    }
}
