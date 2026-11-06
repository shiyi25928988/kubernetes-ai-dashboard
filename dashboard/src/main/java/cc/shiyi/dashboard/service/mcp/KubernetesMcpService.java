package cc.shiyi.dashboard.service.mcp;

import cc.shiyi.dashboard.config.KubernetesClientManager;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Kubernetes操作MCP服务
 * 提供K8S集群资源的查询和管理功能，包含命名空间、Pod、Deployment、Service、Ingress、PVC等常用资源操作
 */
@Slf4j
@org.springframework.stereotype.Service
@Setter(onMethod_ = @Autowired)
public class KubernetesMcpService {

    KubernetesService kubernetesService;
    KubernetesClientManager kubernetesClientManager;

    // ==================== 命名空间相关操作 ====================
    @Tool(description = "获取当前Kubernetes集群的所有命名空间列表")
    public NamespaceList listNamespaces() {
        log.info("MCP调用-获取所有命名空间");
        return kubernetesService.listNamespaces();
    }

    @Tool(description = "创建新的Kubernetes命名空间")
    public Namespace createNamespace(
            @ToolParam(description = "要创建的命名空间名称，例如: test-namespace") String namespaceName) {
        log.info("MCP调用-创建命名空间: {}", namespaceName);
        Namespace namespace = new Namespace();
        namespace.getMetadata().setName(namespaceName);
        return kubernetesService.createNamespace(namespace);
    }

    @Tool(description = "删除指定的Kubernetes命名空间（危险操作，请确认后使用）")
    public String deleteNamespace(
            @ToolParam(description = "要删除的命名空间名称") String namespaceName) {
        log.info("MCP调用-删除命名空间: {}", namespaceName);
        Namespace namespace = new Namespace();
        namespace.getMetadata().setName(namespaceName);
        kubernetesService.deleteNamespace(namespace);
        return "命名空间删除操作已提交: " + namespaceName;
    }

    // ==================== Pod相关操作 ====================
    @Tool(description = "查询Pod列表，指定命名空间则查询该命名空间下的Pod，不指定则查询所有命名空间的Pod")
    public List<Pod> listPods(
            @ToolParam(description = "命名空间名称，可选，不填则查询所有命名空间") String namespace) {
        log.info("MCP调用-查询Pod列表, 命名空间: {}", namespace != null ? namespace : "所有");
        if (namespace != null && !namespace.trim().isEmpty()) {
            return kubernetesService.listPods(namespace);
        } else {
            return kubernetesService.listAllPods();
        }
    }

    @Tool(description = "查询单个Pod的详细信息")
    public Pod getPod(
            @ToolParam(description = "Pod所在的命名空间") String namespace,
            @ToolParam(description = "Pod的名称") String podName) {
        log.info("MCP调用-查询Pod详情: {}/{}", namespace, podName);
        return kubernetesService.getPod(namespace, podName);
    }

    @Tool(description = "获取Pod的日志内容")
    public String getPodLogs(
            @ToolParam(description = "Pod所在的命名空间") String namespace,
            @ToolParam(description = "Pod的名称") String podName) {
        log.info("MCP调用-获取Pod日志: {}/{}", namespace, podName);
        return kubernetesService.getPodLogs(namespace, podName);
    }

    @Tool(description = "获取Pod的运行状态信息，包含阶段、IP、容器状态、启动时间等")
    public Map<String, Object> getPodStatus(
            @ToolParam(description = "Pod所在的命名空间") String namespace,
            @ToolParam(description = "Pod的名称") String podName) {
        log.info("MCP调用-获取Pod状态: {}/{}", namespace, podName);
        return kubernetesService.getPodStatus(namespace, podName);
    }

    // ==================== Deployment相关操作 ====================
    @Tool(description = "查询指定命名空间下的所有Deployment列表")
    public List<Deployment> listDeployments(
            @ToolParam(description = "命名空间名称，必填") String namespace) {
        log.info("MCP调用-查询Deployment列表, 命名空间: {}", namespace);
        return kubernetesService.listDeployments(namespace);
    }

    @Tool(description = "查询单个Deployment的详细信息")
    public Deployment getDeployment(
            @ToolParam(description = "Deployment所在的命名空间") String namespace,
            @ToolParam(description = "Deployment的名称") String deploymentName) {
        log.info("MCP调用-查询Deployment详情: {}/{}", namespace, deploymentName);
        return kubernetesService.getDeployment(namespace, deploymentName);
    }

    @Tool(description = "Deployment扩缩容操作，调整副本数量")
    public Deployment scaleDeployment(
            @ToolParam(description = "Deployment所在的命名空间") String namespace,
            @ToolParam(description = "Deployment的名称") String deploymentName,
            @ToolParam(description = "目标副本数量，必须大于等于0") Integer replicas) {
        log.info("MCP调用-Deployment扩缩容: {}/{} 副本数: {}", namespace, deploymentName, replicas);
        if (replicas < 0) {
            throw new IllegalArgumentException("副本数量不能小于0");
        }
        return kubernetesService.scaleDeployment(namespace, deploymentName, replicas);
    }

    @Tool(description = "滚动重启Deployment，不中断服务")
    public Deployment restartDeployment(
            @ToolParam(description = "Deployment所在的命名空间") String namespace,
            @ToolParam(description = "Deployment的名称") String deploymentName) {
        log.info("MCP调用-重启Deployment: {}/{}", namespace, deploymentName);
        return kubernetesService.restartDeployment(namespace, deploymentName);
    }

    // ==================== Service相关操作 ====================
    @Tool(description = "查询指定命名空间下的所有Service列表")
    public List<Service> listServices(
            @ToolParam(description = "命名空间名称，必填") String namespace) {
        log.info("MCP调用-查询Service列表, 命名空间: {}", namespace);
        return kubernetesService.listServices(namespace);
    }

    @Tool(description = "查询单个Service的详细信息")
    public Service getService(
            @ToolParam(description = "Service所在的命名空间") String namespace,
            @ToolParam(description = "Service的名称") String serviceName) {
        log.info("MCP调用-查询Service详情: {}/{}", namespace, serviceName);
        return kubernetesService.getService(namespace, serviceName);
    }

    // ==================== Ingress相关操作 ====================
    @Tool(description = "查询指定命名空间下的所有Ingress列表")
    public List<Ingress> listIngresses(
            @ToolParam(description = "命名空间名称，必填") String namespace) {
        log.info("MCP调用-查询Ingress列表, 命名空间: {}", namespace);
        return kubernetesService.listIngresses(namespace);
    }

    @Tool(description = "查询单个Ingress的详细信息")
    public Ingress getIngress(
            @ToolParam(description = "Ingress所在的命名空间") String namespace,
            @ToolParam(description = "Ingress的名称") String ingressName) {
        log.info("MCP调用-查询Ingress详情: {}/{}", namespace, ingressName);
        return kubernetesService.getIngress(namespace, ingressName);
    }

    // ==================== PVC相关操作 ====================
    @Tool(description = "查询指定命名空间下的所有PVC（持久卷声明）列表")
    public List<PersistentVolumeClaim> listPVCs(
            @ToolParam(description = "命名空间名称，必填") String namespace) {
        log.info("MCP调用-查询PVC列表, 命名空间: {}", namespace);
        return kubernetesService.listPVCs(namespace);
    }

    @Tool(description = "查询单个PVC的详细信息")
    public PersistentVolumeClaim getPVC(
            @ToolParam(description = "PVC所在的命名空间") String namespace,
            @ToolParam(description = "PVC的名称") String pvcName) {
        log.info("MCP调用-查询PVC详情: {}/{}", namespace, pvcName);
        return kubernetesService.getPVC(namespace, pvcName);
    }

    @Tool(description = "删除指定的PVC（持久卷声明）")
    public String deletePVC(
            @ToolParam(description = "PVC所在的命名空间") String namespace,
            @ToolParam(description = "PVC的名称") String pvcName) {
        log.info("MCP调用-删除PVC: {}/{}", namespace, pvcName);
        kubernetesService.deletePVC(namespace, pvcName);
        return "PVC删除操作已提交: " + pvcName;
    }

    // ==================== 集群统计相关操作 ====================
    @Tool(description = "获取Kubernetes集群整体统计信息，包含节点状态、资源使用率、工作负载统计等")
    public Map<String, Object> getClusterStats() {
        log.info("MCP调用-获取集群统计信息");
        return kubernetesService.getClusterStats();
    }

    @Tool(description = "获取Kubernetes集群所有节点信息列表")
    public List<io.fabric8.kubernetes.api.model.Node> listNodes() {
        log.info("MCP调用-获取集群节点列表");
        return kubernetesClientManager.getClient().nodes().list().getItems();
    }

    @Tool(description = "查询指定资源是否存在，支持Deployment、Service、Ingress、PVC类型")
    public Boolean checkResourceExists(
            @ToolParam(description = "资源类型，可选值: Deployment、Service、Ingress、PVC") String resourceType,
            @ToolParam(description = "资源所在的命名空间") String namespace,
            @ToolParam(description = "资源名称") String resourceName) {
        log.info("MCP调用-检查资源是否存在: {} {}/{}", resourceType, namespace, resourceName);
        return switch (resourceType.toLowerCase()) {
            case "deployment" -> kubernetesService.isDeploymentExists(namespace, resourceName);
            case "service" -> kubernetesService.isServiceExists(namespace, resourceName);
            case "ingress" -> kubernetesService.isIngressExists(namespace, resourceName);
            case "pvc" -> kubernetesService.isPVCExists(namespace, resourceName);
            default -> throw new IllegalArgumentException("不支持的资源类型: " + resourceType + "，支持的类型: Deployment、Service、Ingress、PVC");
        };
    }

    // ==================== Event相关操作 ====================
    @Tool(description = "查询Kubernetes集群中的Event列表，指定命名空间则查询该命名空间下的Event，不指定则查询所有命名空间的Event")
    public List<io.fabric8.kubernetes.api.model.Event> listEvents(
            @ToolParam(description = "命名空间名称，可为空表示查询所有命名空间") String namespace) {
        log.info("MCP调用-查询Event列表, namespace: {}", namespace);
        if (namespace == null || namespace.isEmpty()) {
            return kubernetesService.listAllEvents();
        }
        return kubernetesService.listEvents(namespace);
    }

    // ==================== ConfigMap相关操作 ====================
    @Tool(description = "查询指定命名空间下的ConfigMap列表")
    public List<io.fabric8.kubernetes.api.model.ConfigMap> listConfigMaps(
            @ToolParam(description = "命名空间名称") String namespace) {
        log.info("MCP调用-查询ConfigMap列表, namespace: {}", namespace);
        return kubernetesService.listConfigMaps(namespace);
    }

    @Tool(description = "获取单个ConfigMap详情")
    public io.fabric8.kubernetes.api.model.ConfigMap getConfigMap(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "ConfigMap名称") String name) {
        log.info("MCP调用-获取ConfigMap: {}/{}", namespace, name);
        return kubernetesService.getConfigMap(namespace, name);
    }

    @Tool(description = "创建ConfigMap，需要提供JSON格式的ConfigMap对象")
    public io.fabric8.kubernetes.api.model.ConfigMap createConfigMap(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "ConfigMap名称") String name,
            @ToolParam(description = "ConfigMap的data数据，JSON键值对格式") java.util.Map<String, String> data) {
        log.info("MCP调用-创建ConfigMap: {}/{}", namespace, name);
        io.fabric8.kubernetes.api.model.ConfigMap cm = new io.fabric8.kubernetes.api.model.ConfigMap();
        cm.getMetadata().setName(name);
        cm.getMetadata().setNamespace(namespace);
        cm.setData(data);
        return kubernetesService.createConfigMap(cm, namespace);
    }

    @Tool(description = "删除指定命名空间下的ConfigMap")
    public String deleteConfigMap(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "ConfigMap名称") String name) {
        log.info("MCP调用-删除ConfigMap: {}/{}", namespace, name);
        kubernetesService.deleteConfigMap(namespace, name);
        return "ConfigMap删除操作已提交: " + namespace + "/" + name;
    }

    // ==================== Secret相关操作 ====================
    @Tool(description = "查询指定命名空间下的Secret列表")
    public List<io.fabric8.kubernetes.api.model.Secret> listSecrets(
            @ToolParam(description = "命名空间名称") String namespace) {
        log.info("MCP调用-查询Secret列表, namespace: {}", namespace);
        return kubernetesService.listSecrets(namespace);
    }

    @Tool(description = "获取单个Secret详情")
    public io.fabric8.kubernetes.api.model.Secret getSecret(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Secret名称") String name) {
        log.info("MCP调用-获取Secret: {}/{}", namespace, name);
        return kubernetesService.getSecret(namespace, name);
    }

    @Tool(description = "创建Secret，需要提供键值对数据")
    public io.fabric8.kubernetes.api.model.Secret createSecret(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Secret名称") String name,
            @ToolParam(description = "Secret的data数据，键值对格式，值需要base64编码") java.util.Map<String, String> data) {
        log.info("MCP调用-创建Secret: {}/{}", namespace, name);
        io.fabric8.kubernetes.api.model.Secret secret = new io.fabric8.kubernetes.api.model.Secret();
        secret.getMetadata().setName(name);
        secret.getMetadata().setNamespace(namespace);
        secret.setData(data);
        return kubernetesService.createSecret(secret, namespace);
    }

    @Tool(description = "删除指定命名空间下的Secret")
    public String deleteSecret(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Secret名称") String name) {
        log.info("MCP调用-删除Secret: {}/{}", namespace, name);
        kubernetesService.deleteSecret(namespace, name);
        return "Secret删除操作已提交: " + namespace + "/" + name;
    }

    // ==================== Job相关操作 ====================
    @Tool(description = "查询指定命名空间下的Job列表")
    public List<io.fabric8.kubernetes.api.model.batch.v1.Job> listJobs(
            @ToolParam(description = "命名空间名称") String namespace) {
        log.info("MCP调用-查询Job列表, namespace: {}", namespace);
        return kubernetesService.listJobs(namespace);
    }

    @Tool(description = "获取单个Job详情")
    public io.fabric8.kubernetes.api.model.batch.v1.Job getJob(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Job名称") String name) {
        log.info("MCP调用-获取Job: {}/{}", namespace, name);
        return kubernetesService.getJob(namespace, name);
    }

    @Tool(description = "删除指定命名空间下的Job")
    public String deleteJob(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Job名称") String name) {
        log.info("MCP调用-删除Job: {}/{}", namespace, name);
        kubernetesService.deleteJob(namespace, name);
        return "Job删除操作已提交: " + namespace + "/" + name;
    }

    @Tool(description = "获取Job的Pod日志，返回Pod名称到日志内容的映射")
    public Map<String, String> getJobLogs(
            @ToolParam(description = "命名空间名称") String namespace,
            @ToolParam(description = "Job名称") String name) {
        log.info("MCP调用-获取Job日志: {}/{}", namespace, name);
        return kubernetesService.getJobLogs(namespace, name);
    }

    // ==================== StorageClass相关操作 ====================
    @Tool(description = "查询集群中所有StorageClass列表")
    public List<io.fabric8.kubernetes.api.model.storage.StorageClass> listStorageClasses() {
        log.info("MCP调用-查询StorageClass列表");
        return kubernetesService.listStorageClasses();
    }

    // ==================== CRD相关操作 ====================
    @Tool(description = "查询集群中所有CustomResourceDefinition列表")
    public List<io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition> listCrds() {
        log.info("MCP调用-查询CRD列表");
        return kubernetesService.listCrds();
    }

    @Tool(description = "获取单个CRD详情")
    public io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition getCrd(
            @ToolParam(description = "CRD名称") String name) {
        log.info("MCP调用-获取CRD: {}", name);
        return kubernetesService.getCrd(name);
    }
}
