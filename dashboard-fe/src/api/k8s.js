import request from '@/utils/request'

// 命名空间相关
export const getNamespaceList = () => {
  return request({
    url: '/v1/k8s/namespaces',
    method: 'get'
  })
}

export const createNamespace = (data) => {
  return request({
    url: '/v1/k8s/namespaces',
    method: 'post',
    data: data
  })
}

export const deleteNamespace = (name) => {
  return request({
    url: `/v1/k8s/namespaces/${name}`,
    method: 'delete'
  })
}

// Deployment相关
export const getDeploymentList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments`,
    method: 'get'
  })
}

export const createDeployment = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments`,
    method: 'post',
    data: data
  })
}

export const deleteDeployment = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}`,
    method: 'delete'
  })
}

export const scaleDeployment = (namespace, name, replicas) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}/scale`,
    method: 'put',
    params: { replicas }
  })
}

export const restartDeployment = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}/restart`,
    method: 'put'
  })
}

export const getDeploymentStatus = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}/status`,
    method: 'get'
  })
}

export const getDeployment = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}`,
    method: 'get'
  })
}

export const updateDeployment = (namespace, name, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/deployments/${name}`,
    method: 'put',
    data: data
  })
}

// Job相关
export const getJobList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/jobs`,
    method: 'get'
  })
}

export const getJob = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/jobs/${name}`,
    method: 'get'
  })
}

export const createJob = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/jobs`,
    method: 'post',
    data
  })
}

export const deleteJob = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/jobs/${name}`,
    method: 'delete'
  })
}

export const getJobLogs = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/jobs/${name}/logs`,
    method: 'get'
  })
}

// Pod相关
export const getPodList = (namespace) => {
  let url = '/v1/k8s/pods'
  if (namespace) {
    url = `/v1/k8s/namespaces/${namespace}/pods`
  }
  return request({
    url: url,
    method: 'get'
  })
}

export const getPod = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pods/${name}`,
    method: 'get'
  })
}

export const deletePod = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pods/${name}`,
    method: 'delete'
  })
}

export const getPodLogs = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pods/${name}/logs`,
    method: 'get'
  })
}

// Service相关
export const getServiceList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services`,
    method: 'get'
  })
}

export const createService = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services`,
    method: 'post',
    data: data
  })
}

export const deleteService = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services/${name}`,
    method: 'delete'
  })
}

export const getService = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services/${name}`,
    method: 'get'
  })
}

export const updateService = (namespace, name, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services/${name}`,
    method: 'put',
    data: data
  })
}

export const getServiceStatus = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/services/${name}/status`,
    method: 'get'
  })
}

// Ingress相关
export const getIngressList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/ingresses`,
    method: 'get'
  })
}

export const createIngress = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/ingresses`,
    method: 'post',
    data: data
  })
}

export const deleteIngress = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/ingresses/${name}`,
    method: 'delete'
  })
}

export const getIngressStatus = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/ingresses/${name}/status`,
    method: 'get'
  })
}

// PVC相关
export const getPvcList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pvcs`,
    method: 'get'
  })
}

export const createPvc = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pvcs`,
    method: 'post',
    data: data
  })
}

export const deletePvc = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pvcs/${name}`,
    method: 'delete'
  })
}

export const getPvcStatus = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/pvcs/${name}/status`,
    method: 'get'
  })
}

export const getStorageClassList = () => {
  return request({
    url: '/v1/k8s/storageclasses',
    method: 'get'
  })
}

// Event相关
export const getEventList = (namespace) => {
  let url = '/v1/k8s/events'
  if (namespace) {
    url = `/v1/k8s/namespaces/${namespace}/events`
  }
  return request({ url, method: 'get' })
}

// ConfigMap相关
export const getConfigMapList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/configmaps`,
    method: 'get'
  })
}

export const getConfigMap = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/configmaps/${name}`,
    method: 'get'
  })
}

export const createConfigMap = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/configmaps`,
    method: 'post',
    data
  })
}

export const updateConfigMap = (namespace, name, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/configmaps/${name}`,
    method: 'put',
    data
  })
}

export const deleteConfigMap = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/configmaps/${name}`,
    method: 'delete'
  })
}

// Secret相关
export const getSecretList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/secrets`,
    method: 'get'
  })
}

export const getSecret = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/secrets/${name}`,
    method: 'get'
  })
}

export const createSecret = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/secrets`,
    method: 'post',
    data
  })
}

export const updateSecret = (namespace, name, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/secrets/${name}`,
    method: 'put',
    data
  })
}

export const deleteSecret = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/secrets/${name}`,
    method: 'delete'
  })
}

// CRD相关
export const getCrdList = () => {
  return request({
    url: '/v1/k8s/crds',
    method: 'get'
  })
}

export const getCrd = (name) => {
  return request({
    url: `/v1/k8s/crds/${name}`,
    method: 'get'
  })
}

// 集群监控相关
export const getClusterStats = () => {
  return request({
    url: `/v1/k8s/cluster/stats`,
    method: 'get'
  })
}

// 资源归属查询
export const getOwnershipRecords = (resourceType, namespace) => {
  return request({
    url: '/v1/resources/ownership',
    method: 'get',
    params: { resourceType, namespace }
  })
}

// 集群配置（kubeconfig 上传/激活/删除，存MySQL）
export const uploadClusterConfig = (formData) => {
  return request({
    url: '/v1/k8s/cluster-config/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getClusterConfigList = () => {
  return request({
    url: '/v1/k8s/cluster-config/list',
    method: 'get'
  })
}

export const getActiveClusterConfig = () => {
  return request({
    url: '/v1/k8s/cluster-config/active',
    method: 'get'
  })
}

export const activateClusterConfig = (id) => {
  return request({
    url: `/v1/k8s/cluster-config/${id}/activate`,
    method: 'post'
  })
}

export const deleteClusterConfig = (id) => {
  return request({
    url: `/v1/k8s/cluster-config/${id}`,
    method: 'delete'
  })
}

// 测试集群连接（临时构建客户端探测版本，不改变激活状态）
export const testClusterConfig = (id) => {
  return request({
    url: `/v1/k8s/cluster-config/${id}/test`,
    method: 'post',
    timeout: 30000
  })
}

// StatefulSet相关
export const getStatefulSetList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/statefulsets`,
    method: 'get'
  })
}

export const getStatefulSet = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/statefulsets/${name}`,
    method: 'get'
  })
}

export const scaleStatefulSet = (namespace, name, replicas) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/statefulsets/${name}/scale?replicas=${replicas}`,
    method: 'put'
  })
}

export const deleteStatefulSet = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/statefulsets/${name}`,
    method: 'delete'
  })
}

// DaemonSet相关
export const getDaemonSetList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/daemonsets`,
    method: 'get'
  })
}

export const getDaemonSet = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/daemonsets/${name}`,
    method: 'get'
  })
}

export const deleteDaemonSet = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/daemonsets/${name}`,
    method: 'delete'
  })
}

// Node节点相关（只读）
export const getNodeList = () => {
  return request({
    url: '/v1/k8s/nodes',
    method: 'get'
  })
}

export const getNode = (name) => {
  return request({
    url: `/v1/k8s/nodes/${name}`,
    method: 'get'
  })
}

// PersistentVolume相关（只读）
export const getPvList = () => {
  return request({
    url: '/v1/k8s/persistentvolumes',
    method: 'get'
  })
}

export const getPv = (name) => {
  return request({
    url: `/v1/k8s/persistentvolumes/${name}`,
    method: 'get'
  })
}

// HPA相关
export const getHpaList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/horizontalpodautoscalers`,
    method: 'get'
  })
}

export const getHpa = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/horizontalpodautoscalers/${name}`,
    method: 'get'
  })
}

export const updateHpaMinMax = (namespace, name, minReplicas, maxReplicas) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/horizontalpodautoscalers/${name}/minmax`,
    method: 'put',
    params: { minReplicas, maxReplicas }
  })
}

// NetworkPolicy相关（只读）
export const getNetworkPolicyList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/networkpolicies`,
    method: 'get'
  })
}

export const getNetworkPolicy = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/networkpolicies/${name}`,
    method: 'get'
  })
}

// ResourceQuota相关
export const getResourceQuotaList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/resourcequotas`,
    method: 'get'
  })
}

export const getResourceQuota = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/resourcequotas/${name}`,
    method: 'get'
  })
}

export const createResourceQuota = (namespace, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/resourcequotas`,
    method: 'post',
    data: data
  })
}

export const updateResourceQuota = (namespace, name, data) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/resourcequotas/${name}`,
    method: 'put',
    data: data
  })
}

export const deleteResourceQuota = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/resourcequotas/${name}`,
    method: 'delete'
  })
}

// ServiceAccount相关
export const getServiceAccountList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/serviceaccounts`,
    method: 'get'
  })
}

export const getServiceAccount = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/serviceaccounts/${name}`,
    method: 'get'
  })
}

export const deleteServiceAccount = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/serviceaccounts/${name}`,
    method: 'delete'
  })
}

// Role相关
export const getRoleList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/roles`,
    method: 'get'
  })
}

export const getRole = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/roles/${name}`,
    method: 'get'
  })
}

export const deleteRole = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/roles/${name}`,
    method: 'delete'
  })
}

// ClusterRole相关
export const getClusterRoleList = () => {
  return request({
    url: '/v1/k8s/clusterroles',
    method: 'get'
  })
}

export const getClusterRole = (name) => {
  return request({
    url: `/v1/k8s/clusterroles/${name}`,
    method: 'get'
  })
}

export const deleteClusterRole = (name) => {
  return request({
    url: `/v1/k8s/clusterroles/${name}`,
    method: 'delete'
  })
}

// RoleBinding相关
export const getRoleBindingList = (namespace) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/rolebindings`,
    method: 'get'
  })
}

export const getRoleBinding = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/rolebindings/${name}`,
    method: 'get'
  })
}

export const deleteRoleBinding = (namespace, name) => {
  return request({
    url: `/v1/k8s/namespaces/${namespace}/rolebindings/${name}`,
    method: 'delete'
  })
}

// ClusterRoleBinding相关
export const getClusterRoleBindingList = () => {
  return request({
    url: '/v1/k8s/clusterrolebindings',
    method: 'get'
  })
}

export const getClusterRoleBinding = (name) => {
  return request({
    url: `/v1/k8s/clusterrolebindings/${name}`,
    method: 'get'
  })
}

export const deleteClusterRoleBinding = (name) => {
  return request({
    url: `/v1/k8s/clusterrolebindings/${name}`,
    method: 'delete'
  })
}
