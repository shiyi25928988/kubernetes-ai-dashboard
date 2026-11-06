package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.config.KubernetesClientManager;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressList;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class KubernetesService {

    @Autowired
    private KubernetesClientManager kubernetesClientManager;

    private KubernetesClient client() {
        return kubernetesClientManager.getClient();
    }

    @Autowired
    private ResourceOwnershipService ownershipService;

    public Namespace createNamespace(Namespace namespace) {
        ownershipService.addOwnershipLabels(namespace.getMetadata());
        Namespace result = client().resource(namespace).create();
        ownershipService.recordCreation("Namespace", "", result.getMetadata().getName());
        return result;
    }

    public List<StatusDetails> deleteNamespace(Namespace namespace) {
        ownershipService.requirePlatformManaged(namespace.getMetadata(),
                "Namespace " + namespace.getMetadata().getName());
        return client().resource(namespace).delete();
    }

    public NamespaceList listNamespaces() {
        return client().namespaces().list();
    }

    public Namespace getNamespace(String name) {
        return client().namespaces().withName(name).get();
    }

    public void createPod(Pod pod, String namespace) {
        client().resource(pod).inNamespace(namespace).create();
    }

    public void deletePod(Pod pod, String namespace) {
        client().resource(pod).inNamespace(namespace).delete();
    }

    /**
     * 查询单个Pod
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return Pod对象，不存在则返回null
     */
    public Pod getPod(String namespace, String podName) {
        return client().pods()
                .inNamespace(namespace)
                .withName(podName)
                .get();
    }

    /**
     * 列表查询Namespace下的所有Pod
     * @param namespace 命名空间
     * @return Pod列表
     */
    public List<Pod> listPods(String namespace) {
        return client().pods()
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    /**
     * 列表查询所有命名空间下的所有Pod
     * @return Pod列表
     */
    public List<Pod> listAllPods() {
        return client().pods()
                .list()
                .getItems();
    }

    /**
     * 获取Pod日志
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return 日志内容
     */
    public String getPodLogs(String namespace, String podName) {
        Pod pod = getPod(namespace, podName);
        if (pod == null) {
            return null;
        }
        return client().pods()
                .inNamespace(namespace)
                .withName(podName)
                .getLog();
    }

    /**
     * 获取Pod状态
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return 状态信息Map，包含阶段、IP、容器状态等
     */
    public Map<String, Object> getPodStatus(String namespace, String podName) {
        Pod pod = getPod(namespace, podName);
        if (pod == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("name", pod.getMetadata().getName());
        status.put("namespace", pod.getMetadata().getNamespace());
        status.put("phase", pod.getStatus().getPhase());
        status.put("podIP", pod.getStatus().getPodIP());
        status.put("hostIP", pod.getStatus().getHostIP());
        status.put("containerStatuses", pod.getStatus().getContainerStatuses());
        status.put("startTime", pod.getStatus().getStartTime());
        status.put("creationTimestamp", pod.getMetadata().getCreationTimestamp());
        return status;
    }

    /**
     * 检查Pod是否存在
     * @param namespace 命名空间
     * @param podName Pod名称
     * @return 存在返回true，否则返回false
     */
    public boolean isPodExists(String namespace, String podName) {
        return getPod(namespace, podName) != null;
    }

    public void createDeployment(Deployment deployment, String namespace) {
        ownershipService.addOwnershipLabels(deployment.getMetadata());
        client().resource(deployment).inNamespace(namespace).create();
        ownershipService.recordCreation("Deployment", namespace, deployment.getMetadata().getName());
    }

    public void deleteDeployment(Deployment deployment, String namespace) {
        ownershipService.requirePlatformManaged(deployment.getMetadata(),
                "Deployment " + namespace + "/" + deployment.getMetadata().getName());
        client().resource(deployment).inNamespace(namespace).delete();
    }

    public void createService(io.fabric8.kubernetes.api.model.Service service, String namespace) {
        ownershipService.addOwnershipLabels(service.getMetadata());
        client().resource(service).inNamespace(namespace).create();
        ownershipService.recordCreation("Service", namespace, service.getMetadata().getName());
    }

    public void deleteService(io.fabric8.kubernetes.api.model.Service service, String namespace) {
        ownershipService.requirePlatformManaged(service.getMetadata(),
                "Service " + namespace + "/" + service.getMetadata().getName());
        client().resource(service).inNamespace(namespace).delete();
    }

    public void createIngress(Ingress ingress, String namespace) {
        ownershipService.addOwnershipLabels(ingress.getMetadata());
        client().resource(ingress).inNamespace(namespace).create();
        ownershipService.recordCreation("Ingress", namespace, ingress.getMetadata().getName());
    }

    public void deleteIngress(Ingress ingress, String namespace) {
        ownershipService.requirePlatformManaged(ingress.getMetadata(),
                "Ingress " + namespace + "/" + ingress.getMetadata().getName());
        client().resource(ingress).inNamespace(namespace).delete();
    }

    // ==================== StatefulSet ====================
    public List<StatefulSet> listStatefulSets(String namespace) {
        return client().apps().statefulSets().inNamespace(namespace).list().getItems();
    }

    public StatefulSet getStatefulSet(String namespace, String name) {
        return client().apps().statefulSets().inNamespace(namespace).withName(name).get();
    }

    public void createStatefulSet(StatefulSet statefulSet, String namespace) {
        ownershipService.addOwnershipLabels(statefulSet.getMetadata());
        client().resource(statefulSet).inNamespace(namespace).create();
        ownershipService.recordCreation("StatefulSet", namespace, statefulSet.getMetadata().getName());
    }

    public void deleteStatefulSet(StatefulSet statefulSet, String namespace) {
        ownershipService.requirePlatformManaged(statefulSet.getMetadata(),
                "StatefulSet " + namespace + "/" + statefulSet.getMetadata().getName());
        client().resource(statefulSet).inNamespace(namespace).delete();
    }

    public StatefulSet updateStatefulSet(StatefulSet statefulSet, String namespace) {
        return client().apps().statefulSets()
                .inNamespace(namespace)
                .resource(statefulSet)
                .update();
    }

    /** 扩缩容/更新副本数 */
    public StatefulSet scaleStatefulSet(String namespace, String name, Integer replicas) {
        return client().apps().statefulSets()
                .inNamespace(namespace)
                .withName(name)
                .scale(replicas);
    }

    // ==================== DaemonSet ====================
    public List<DaemonSet> listDaemonSets(String namespace) {
        return client().apps().daemonSets().inNamespace(namespace).list().getItems();
    }

    public DaemonSet getDaemonSet(String namespace, String name) {
        return client().apps().daemonSets().inNamespace(namespace).withName(name).get();
    }

    public void createDaemonSet(DaemonSet daemonSet, String namespace) {
        ownershipService.addOwnershipLabels(daemonSet.getMetadata());
        client().resource(daemonSet).inNamespace(namespace).create();
        ownershipService.recordCreation("DaemonSet", namespace, daemonSet.getMetadata().getName());
    }

    public void deleteDaemonSet(DaemonSet daemonSet, String namespace) {
        ownershipService.requirePlatformManaged(daemonSet.getMetadata(),
                "DaemonSet " + namespace + "/" + daemonSet.getMetadata().getName());
        client().resource(daemonSet).inNamespace(namespace).delete();
    }

    public DaemonSet updateDaemonSet(DaemonSet daemonSet, String namespace) {
        return client().apps().daemonSets()
                .inNamespace(namespace)
                .resource(daemonSet)
                .update();
    }

    // ==================== Node 节点（只读） ====================
    public List<Node> listNodes() {
        return client().nodes().list().getItems();
    }

    public Node getNode(String name) {
        return client().nodes().withName(name).get();
    }

    // ==================== Deployment扩展操作 ====================
    /**
     * 查询单个Deployment
     * @param namespace 命名空间
     * @param deploymentName Deployment名称
     * @return Deployment对象，不存在则返回null
     */
    public Deployment getDeployment(String namespace, String deploymentName) {
        return client().apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .get();
    }

    /**
     * 列表查询Namespace下的所有Deployment
     * @param namespace 命名空间
     * @return Deployment列表
     */
    public List<Deployment> listDeployments(String namespace) {
        DeploymentList deploymentList = client().apps().deployments()
                .inNamespace(namespace)
                .list();
        return deploymentList.getItems();
    }

    /**
     * 更新Deployment
     * @param deployment 要更新的Deployment对象
     * @param namespace 命名空间
     * @return 更新后的Deployment
     */
    public Deployment updateDeployment(Deployment deployment, String namespace) {
        return client().apps().deployments()
                .inNamespace(namespace)
                .resource(deployment)
                .update();
    }

    /**
     * Deployment扩容缩容
     * @param namespace 命名空间
     * @param deploymentName Deployment名称
     * @param replicas 目标副本数
     * @return 更新后的Deployment
     */
    public Deployment scaleDeployment(String namespace, String deploymentName, int replicas) {
        return client().apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .scale(replicas);
    }

    /**
     * 重启Deployment（滚动重启）
     * @param namespace 命名空间
     * @param deploymentName Deployment名称
     * @return 重启后的Deployment
     */
    public Deployment restartDeployment(String namespace, String deploymentName) {
        return client().apps().deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .rolling()
                .restart();
    }

    // ==================== PVC相关操作 ====================
    /**
     * 创建PVC
     * @param pvc PVC对象
     * @param namespace 命名空间
     * @return 创建后的PVC
     */
    public PersistentVolumeClaim createPVC(PersistentVolumeClaim pvc, String namespace) {
        ownershipService.addOwnershipLabels(pvc.getMetadata());
        PersistentVolumeClaim result = client().resource(pvc).inNamespace(namespace).create();
        ownershipService.recordCreation("PVC", namespace, result.getMetadata().getName());
        return result;
    }

    /**
     * 查询单个PVC
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return PVC对象，不存在则返回null
     */
    public PersistentVolumeClaim getPVC(String namespace, String pvcName) {
        return client().persistentVolumeClaims()
                .inNamespace(namespace)
                .withName(pvcName)
                .get();
    }

    /**
     * 列表查询Namespace下的所有PVC
     * @param namespace 命名空间
     * @return PVC列表
     */
    public List<PersistentVolumeClaim> listPVCs(String namespace) {
        PersistentVolumeClaimList pvcList = client().persistentVolumeClaims()
                .inNamespace(namespace)
                .list();
        return pvcList.getItems();
    }

    /**
     * 删除PVC
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return 删除结果详情
     */
    public List<StatusDetails> deletePVC(String namespace, String pvcName) {
        PersistentVolumeClaim pvc = getPVC(namespace, pvcName);
        if (pvc != null) {
            ownershipService.requirePlatformManaged(pvc.getMetadata(), "PVC " + namespace + "/" + pvcName);
        }
        return client().persistentVolumeClaims()
                .inNamespace(namespace)
                .withName(pvcName)
                .delete();
    }

    /**
     * 更新PVC
     * @param pvc 要更新的PVC对象
     * @param namespace 命名空间
     * @return 更新后的PVC
     */
    public PersistentVolumeClaim updatePVC(PersistentVolumeClaim pvc, String namespace) {
        return client().persistentVolumeClaims()
                .inNamespace(namespace)
                .resource(pvc)
                .update();
    }

    // ==================== Service扩展操作 ====================
    /**
     * 查询单个Service
     * @param namespace 命名空间
     * @param serviceName Service名称
     * @return Service对象，不存在则返回null
     */
    public io.fabric8.kubernetes.api.model.Service getService(String namespace, String serviceName) {
        return client().services()
                .inNamespace(namespace)
                .withName(serviceName)
                .get();
    }

    /**
     * 列表查询Namespace下的所有Service
     * @param namespace 命名空间
     * @return Service列表
     */
    public List<io.fabric8.kubernetes.api.model.Service> listServices(String namespace) {
        io.fabric8.kubernetes.api.model.ServiceList serviceList = client().services()
                .inNamespace(namespace)
                .list();
        return serviceList.getItems();
    }

    /**
     * 更新Service
     * @param service 要更新的Service对象
     * @param namespace 命名空间
     * @return 更新后的Service
     */
    public io.fabric8.kubernetes.api.model.Service updateService(io.fabric8.kubernetes.api.model.Service service, String namespace) {
        return client().services()
                .inNamespace(namespace)
                .resource(service)
                .update();
    }

    // ==================== Ingress扩展操作 ====================
    /**
     * 查询单个Ingress
     * @param namespace 命名空间
     * @param ingressName Ingress名称
     * @return Ingress对象，不存在则返回null
     */
    public Ingress getIngress(String namespace, String ingressName) {
        return client().network().v1().ingresses()
                .inNamespace(namespace)
                .withName(ingressName)
                .get();
    }

    /**
     * 列表查询Namespace下的所有Ingress
     * @param namespace 命名空间
     * @return Ingress列表
     */
    public List<Ingress> listIngresses(String namespace) {
        IngressList ingressList = client().network().v1().ingresses()
                .inNamespace(namespace)
                .list();
        return ingressList.getItems();
    }

    /**
     * 更新Ingress
     * @param ingress 要更新的Ingress对象
     * @param namespace 命名空间
     * @return 更新后的Ingress
     */
    public Ingress updateIngress(Ingress ingress, String namespace) {
        return client().network().v1().ingresses()
                .inNamespace(namespace)
                .resource(ingress)
                .update();
    }

    // ==================== 通用工具方法 ====================
    /**
     * 检查Deployment是否存在
     * @param namespace 命名空间
     * @param deploymentName Deployment名称
     * @return 存在返回true，否则返回false
     */
    public boolean isDeploymentExists(String namespace, String deploymentName) {
        return getDeployment(namespace, deploymentName) != null;
    }

    /**
     * 检查PVC是否存在
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return 存在返回true，否则返回false
     */
    public boolean isPVCExists(String namespace, String pvcName) {
        return getPVC(namespace, pvcName) != null;
    }

    /**
     * 检查Service是否存在
     * @param namespace 命名空间
     * @param serviceName Service名称
     * @return 存在返回true，否则返回false
     */
    public boolean isServiceExists(String namespace, String serviceName) {
        return getService(namespace, serviceName) != null;
    }

    /**
     * 检查Ingress是否存在
     * @param namespace 命名空间
     * @param ingressName Ingress名称
     * @return 存在返回true，否则返回false
     */
    public boolean isIngressExists(String namespace, String ingressName) {
        return getIngress(namespace, ingressName) != null;
    }

    // ==================== ConfigMap相关操作 ====================
    public List<ConfigMap> listConfigMaps(String namespace) {
        return client().configMaps()
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    public ConfigMap getConfigMap(String namespace, String name) {
        return client().configMaps()
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    public ConfigMap createConfigMap(ConfigMap configMap, String namespace) {
        ownershipService.addOwnershipLabels(configMap.getMetadata());
        ConfigMap result = client().resource(configMap).inNamespace(namespace).create();
        ownershipService.recordCreation("ConfigMap", namespace, result.getMetadata().getName());
        return result;
    }

    public ConfigMap updateConfigMap(ConfigMap configMap, String namespace) {
        return client().resource(configMap).inNamespace(namespace).update();
    }

    public List<StatusDetails> deleteConfigMap(String namespace, String name) {
        ConfigMap cm = getConfigMap(namespace, name);
        if (cm != null) {
            ownershipService.requirePlatformManaged(cm.getMetadata(), "ConfigMap " + namespace + "/" + name);
        }
        return client().configMaps()
                .inNamespace(namespace)
                .withName(name)
                .delete();
    }

    // ==================== Secret相关操作 ====================
    public List<Secret> listSecrets(String namespace) {
        return client().secrets()
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    public Secret getSecret(String namespace, String name) {
        return client().secrets()
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    public Secret createSecret(Secret secret, String namespace) {
        ownershipService.addOwnershipLabels(secret.getMetadata());
        Secret result = client().resource(secret).inNamespace(namespace).create();
        ownershipService.recordCreation("Secret", namespace, result.getMetadata().getName());
        return result;
    }

    public Secret updateSecret(Secret secret, String namespace) {
        return client().resource(secret).inNamespace(namespace).update();
    }

    public List<StatusDetails> deleteSecret(String namespace, String name) {
        Secret secret = getSecret(namespace, name);
        if (secret != null) {
            ownershipService.requirePlatformManaged(secret.getMetadata(), "Secret " + namespace + "/" + name);
        }
        return client().secrets()
                .inNamespace(namespace)
                .withName(name)
                .delete();
    }

    // ==================== Job相关操作 ====================
    public List<Job> listJobs(String namespace) {
        return client().batch().v1().jobs()
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    public Job getJob(String namespace, String name) {
        return client().batch().v1().jobs()
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    public Job createJob(Job job, String namespace) {
        ownershipService.addOwnershipLabels(job.getMetadata());
        Job result = client().resource(job).inNamespace(namespace).create();
        ownershipService.recordCreation("Job", namespace, result.getMetadata().getName());
        return result;
    }

    public void deleteJob(String namespace, String name) {
        Job job = getJob(namespace, name);
        if (job != null) {
            ownershipService.requirePlatformManaged(job.getMetadata(), "Job " + namespace + "/" + name);
        }
        client().batch().v1().jobs()
                .inNamespace(namespace)
                .withName(name)
                .delete();
    }

    public Map<String, String> getJobLogs(String namespace, String jobName) {
        Map<String, String> logs = new HashMap<>();
        List<Pod> pods = client().pods()
                .inNamespace(namespace)
                .withLabel("batch.kubernetes.io/job-name", jobName)
                .list()
                .getItems();
        for (Pod pod : pods) {
            try {
                String log = client().pods()
                        .inNamespace(namespace)
                        .withName(pod.getMetadata().getName())
                        .getLog();
                logs.put(pod.getMetadata().getName(), log);
            } catch (Exception e) {
                logs.put(pod.getMetadata().getName(), "Failed to get logs: " + e.getMessage());
            }
        }
        return logs;
    }

    // ==================== StorageClass相关操作 ====================
    public List<io.fabric8.kubernetes.api.model.storage.StorageClass> listStorageClasses() {
        return client().storage().storageClasses().list().getItems();
    }

    // ==================== CRD相关操作 ====================
    public List<CustomResourceDefinition> listCrds() {
        return client().apiextensions().v1()
                .customResourceDefinitions()
                .list()
                .getItems();
    }

    public CustomResourceDefinition getCrd(String name) {
        return client().apiextensions().v1()
                .customResourceDefinitions()
                .withName(name)
                .get();
    }

    // ==================== Event相关操作 ====================
    /**
     * 列表查询Namespace下的所有Event
     * @param namespace 命名空间
     * @return Event列表
     */
    public List<Event> listEvents(String namespace) {
        return client().v1().events()
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    /**
     * 列表查询所有命名空间下的所有Event
     * @return Event列表
     */
    public List<Event> listAllEvents() {
        return client().v1().events()
                .list()
                .getItems();
    }

    /**
     * 获取集群整体资源统计信息
     * @return 包含节点、资源使用率、工作负载、健康状态的统计数据
     */
    /**
     * Parse a Kubernetes CPU Quantity to core count.
     * CPU values like "4" (4 cores) or "4000m" (4000 millicores = 4 cores).
     */
    private double parseCpuCores(Quantity cpuQty) {
        BigDecimal amount = cpuQty.getNumericalAmount();
        if (amount == null) return 0;
        String format = cpuQty.getFormat();
        if ("m".equals(format)) {
            return amount.doubleValue() / 1000.0;
        }
        return amount.doubleValue();
    }

    public Map<String, Object> getClusterStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 节点状态统计
        NodeList nodeList = client().nodes().list();
        long totalNodes = nodeList.getItems().size();
        long readyNodes = nodeList.getItems().stream()
                .filter(node -> node.getStatus().getConditions().stream()
                        .anyMatch(cond -> "Ready".equals(cond.getType()) && "True".equals(cond.getStatus())))
                .count();
        Map<String, Object> nodeStats = new HashMap<>();
        nodeStats.put("total", totalNodes);
        nodeStats.put("ready", readyNodes);
        nodeStats.put("notReady", totalNodes - readyNodes);
        result.put("nodes", nodeStats);

        // 2. 集群资源使用率统计
        double totalCpu = 0;
        double totalMemory = 0;
        double usedCpu = 0;
        double usedMemory = 0;
        double totalStorage = 0;
        double usedStorage = 0;

        // 统计节点总资源
        for (Node node : nodeList.getItems()) {
            // 解析CPU（转成核数）
            Quantity cpuQty = node.getStatus().getCapacity().get("cpu");
            if (cpuQty != null) totalCpu += parseCpuCores(cpuQty);
            // 解析内存（转成Gi）
            Quantity memQty = node.getStatus().getCapacity().get("memory");
            if (memQty != null) totalMemory += Quantity.getAmountInBytes(memQty).doubleValue() / (1024 * 1024 * 1024);
        }

        // 统计已分配资源（所有Pod的Request总和）
        PodList allPods = client().pods().list();
        for (Pod pod : allPods.getItems()) {
            if (!"Running".equals(pod.getStatus().getPhase())) continue;
            for (Container container : pod.getSpec().getContainers()) {
                if (container.getResources() != null && container.getResources().getRequests() != null) {
                    Quantity cpuReq = container.getResources().getRequests().get("cpu");
                    if (cpuReq != null) usedCpu += parseCpuCores(cpuReq);
                    Quantity memReq = container.getResources().getRequests().get("memory");
                    if (memReq != null) usedMemory += Quantity.getAmountInBytes(memReq).doubleValue() / (1024 * 1024 * 1024);
                }
            }
        }

        // 统计存储资源
        PersistentVolumeList pvList = client().persistentVolumes().list();
        for (PersistentVolume pv : pvList.getItems()) {
            Quantity storageQty = pv.getSpec().getCapacity().get("storage");
            if (storageQty != null) {
                long pvSize = Quantity.getAmountInBytes(storageQty).longValue() / (1024 * 1024 * 1024);
                totalStorage += pvSize;
                if ("Bound".equals(pv.getStatus().getPhase())) usedStorage += pvSize;
            }
        }

        Map<String, Object> resourceStats = new HashMap<>();
        resourceStats.put("cpuTotal", String.format("%.1f核", totalCpu));
        resourceStats.put("cpuUsed", String.format("%.1f核", usedCpu));
        resourceStats.put("cpuUsage", totalCpu > 0 ? Math.round(usedCpu / totalCpu * 100) : 0);
        resourceStats.put("memoryTotal", String.format("%.1fGi", totalMemory));
        resourceStats.put("memoryUsed", String.format("%.1fGi", usedMemory));
        resourceStats.put("memoryUsage", totalMemory > 0 ? Math.round(usedMemory / totalMemory * 100) : 0);
        resourceStats.put("storageTotal", String.format("%.0fGi", totalStorage));
        resourceStats.put("storageUsed", String.format("%.0fGi", usedStorage));
        resourceStats.put("storageUsage", totalStorage > 0 ? Math.round(usedStorage / totalStorage * 100) : 0);
        result.put("resources", resourceStats);

        // 3. 工作负载统计
        NamespaceList nsList = client().namespaces().list();
        DeploymentList deployList = client().apps().deployments().list();
        ServiceList svcList = client().services().list();
        IngressList ingressList = client().network().v1().ingresses().list();
        PersistentVolumeClaimList pvcList = client().persistentVolumeClaims().list();

        long runningPods = allPods.getItems().stream().filter(p -> "Running".equals(p.getStatus().getPhase())).count();
        long abnormalPods = allPods.getItems().stream()
                .filter(p -> !Arrays.asList("Running", "Succeeded", "Pending").contains(p.getStatus().getPhase()))
                .count();

        Map<String, Object> workloadStats = new HashMap<>();
        workloadStats.put("namespaces", nsList.getItems().size());
        workloadStats.put("deployments", deployList.getItems().size());
        workloadStats.put("podsTotal", allPods.getItems().size());
        workloadStats.put("podsRunning", runningPods);
        workloadStats.put("podsAbnormal", abnormalPods);
        workloadStats.put("services", svcList.getItems().size());
        workloadStats.put("ingresses", ingressList.getItems().size());
        workloadStats.put("pvc", pvcList.getItems().size());
        result.put("workloads", workloadStats);

        // 4. 集群健康状态
        Map<String, Object> healthStats = new HashMap<>();
        healthStats.put("controlPlane", readyNodes > 0 ? "normal" : "error");
        healthStats.put("nodeHealth", readyNodes == totalNodes ? "normal" : "warning");
        healthStats.put("network", abnormalPods < 3 ? "normal" : "warning");
        healthStats.put("storage", usedStorage / totalStorage < 0.8 ? "normal" : "warning");
        result.put("health", healthStats);

        return result;
    }

    /**
     * 获取Deployment状态
     * @param namespace 命名空间
     * @param deploymentName Deployment名称
     * @return 状态信息Map，包含可用副本数、就绪副本数、总副本数等
     */
    public Map<String, Object> getDeploymentStatus(String namespace, String deploymentName) {
        Deployment deployment = getDeployment(namespace, deploymentName);
        if (deployment == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("name", deployment.getMetadata().getName());
        status.put("namespace", deployment.getMetadata().getNamespace());
        status.put("replicas", deployment.getSpec().getReplicas());
        status.put("availableReplicas", deployment.getStatus().getAvailableReplicas());
        status.put("readyReplicas", deployment.getStatus().getReadyReplicas());
        status.put("updatedReplicas", deployment.getStatus().getUpdatedReplicas());
        status.put("unavailableReplicas", deployment.getStatus().getUnavailableReplicas());
        status.put("creationTimestamp", deployment.getMetadata().getCreationTimestamp());
        return status;
    }

    /**
     * 获取PVC状态
     * @param namespace 命名空间
     * @param pvcName PVC名称
     * @return 状态信息Map，包含阶段、容量、访问模式等
     */
    public Map<String, Object> getPVCStatus(String namespace, String pvcName) {
        PersistentVolumeClaim pvc = getPVC(namespace, pvcName);
        if (pvc == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("name", pvc.getMetadata().getName());
        status.put("namespace", pvc.getMetadata().getNamespace());
        status.put("phase", pvc.getStatus().getPhase());
        status.put("capacity", pvc.getStatus().getCapacity());
        status.put("accessModes", pvc.getSpec().getAccessModes());
        status.put("storageClassName", pvc.getSpec().getStorageClassName());
        status.put("volumeName", pvc.getSpec().getVolumeName());
        status.put("creationTimestamp", pvc.getMetadata().getCreationTimestamp());
        return status;
    }

    /**
     * 获取Service状态
     * @param namespace 命名空间
     * @param serviceName Service名称
     * @return 状态信息Map，包含类型、集群IP、端口等
     */
    public Map<String, Object> getServiceStatus(String namespace, String serviceName) {
        io.fabric8.kubernetes.api.model.Service service = getService(namespace, serviceName);
        if (service == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("name", service.getMetadata().getName());
        status.put("namespace", service.getMetadata().getNamespace());
        status.put("type", service.getSpec().getType());
        status.put("clusterIP", service.getSpec().getClusterIP());
        status.put("ports", service.getSpec().getPorts());
        status.put("selector", service.getSpec().getSelector());
        status.put("creationTimestamp", service.getMetadata().getCreationTimestamp());
        return status;
    }

    /**
     * 获取Ingress状态
     * @param namespace 命名空间
     * @param ingressName Ingress名称
     * @return 状态信息Map，包含负载均衡地址、规则等
     */
    public Map<String, Object> getIngressStatus(String namespace, String ingressName) {
        Ingress ingress = getIngress(namespace, ingressName);
        if (ingress == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("name", ingress.getMetadata().getName());
        status.put("namespace", ingress.getMetadata().getNamespace());
        status.put("rules", ingress.getSpec().getRules());
        status.put("tls", ingress.getSpec().getTls());
        status.put("loadBalancer", ingress.getStatus().getLoadBalancer());
        status.put("creationTimestamp", ingress.getMetadata().getCreationTimestamp());
        return status;
    }
}
