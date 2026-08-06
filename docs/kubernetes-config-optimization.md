# Kubernetes Client 配置优化建议

## 当前问题

1. **缺少默认值和空值处理**
2. **未利用集群内部署的 ServiceAccount**
3. **缺少优雅的回退逻辑**
4. **缺少日志输出**
5. **硬编码 trustCerts**

## 优化方案

### 1. 多种部署模式支持（按优先级）

| 优先级 | 配置方式 | 场景 | 说明 |
|-------|---------|------|------|
| 1 | config-url | 集群外/多云管理 | 从 HTTP(S) 下载 kubeconfig |
| 2 | config-path | 本地开发/固定节点 | 使用本地 kubeconfig 文件 |
| 3 | 留空 | 集群内部署 | 自动使用 ServiceAccount（生产推荐） |

### 2. 配置项说明

```yaml
kubernetes:
  client:
    config-url: ""              # 可选：从 URL 下载 kubeconfig
    config-path: ""            # 可选：本地 kubeconfig 路径
    trust-certs: true          # 可选：是否信任证书，默认 true
```

### 3. URL 配置示例

从配置中心或文件服务器下载：

```yaml
kubernetes:
  client:
    config-url: "https://config-server/k8s/kubeconfig.yaml"
    trust-certs: true
```

支持的协议：
- `http://`
- `https://`


### 3. 推荐生产配置

**应用部署在集群内（推荐）**：
```yaml
kubernetes:
  client:
    config-path: ""              # 留空自动使用 ServiceAccount
    trust-certs: true
```

**应用部署在集群外**：
```yaml
kubernetes:
  client:
    config-path: /etc/k8s/kubeconfig
    trust-certs: true
```

### 4. 集群内部署最佳实践

#### 4.1 配置 RBAC

创建 `cluster-role.yaml`：
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: mpi-dashboard
rules:
- apiGroups: ["", "apps", "batch", "kubeflow.org"]
  resources:
  - pods, pods/log, services, configmaps, secrets
  - deployments, jobs, cronjobs
  - namespaces
  - mpijobs
  verbs: ["get", "list", "watch", "create", "update", "delete"]
```

创建 `cluster-role-binding.yaml`：
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: mpi-dashboard
subjects:
- kind: ServiceAccount
  name: default
  namespace: mpi-dashboard
roleRef:
  kind: ClusterRole
  name: mpi-dashboard
  apiGroup: rbac.authorization.k8s.io
```

#### 4.2 应用部署配置

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mpi-dashboard
  namespace: mpi-dashboard
spec:
  template:
    spec:
      serviceAccountName: default
      containers:
      - name: mpi-dashboard
        image: 10.113.16.240/mpi-dashboard-backend:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
        # 不需要配置 KUBECONFIG，自动使用 ServiceAccount
```

### 5. ConfigMap 挂载方式（集群外部署）

如果应用部署在集群外但仍需访问集群：

```yaml
volumes:
- name: kubeconfig
  secret:
    secretName: mpi-dashboard-kubeconfig
volumeMounts:
- name: kubeconfig
  mountPath: /etc/k8s
  readOnly: true
```

配置项：
```yaml
kubernetes:
  client:
    config-path: /etc/k8s/kubeconfig
```

### 6. 本地开发配置

在 `application-dev.yml` 中配置：
```yaml
kubernetes:
  client:
    config-path: ${user.home}/.kube/config
```

### 7. 优化后的优势

| 优势 | 说明 |
|------|------|
| 自动适配 | 无需改配置，集群内/外都能运行 |
| 更安全 | 集群内使用 ServiceAccount，无需管理 kubeconfig |
| 容错性好 | 配置文件不存在时自动回退 |
| 可观测 | 添加日志，便于排查连接问题 |
| 灵活配置 | TrustCerts 可通过配置调整 |
