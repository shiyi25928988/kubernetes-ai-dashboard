<template>
  <div class="ingress-container">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>Ingress管理</span>
          <el-select v-model="namespace" placeholder="选择命名空间" class="w-48" @change="loadIngresses">
            <el-option
              v-for="ns in namespaces"
              :key="ns.metadata.name"
              :label="ns.metadata.name"
              :value="ns.metadata.name"
            />
          </el-select>
        </div>
      </template>

      <el-table :data="ingresses" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="metadata.name" label="名称" width="200" />
        <el-table-column prop="metadata.namespace" label="命名空间" width="150" />
        <el-table-column label="域名" width="200">
          <template #default="scope">
            <div v-for="rule in scope.row.spec.rules" :key="rule.host" class="host-item">
              <el-tag type="primary" size="small">{{ rule.host }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="路径" width="250">
          <template #default="scope">
            <div v-for="rule in scope.row.spec.rules" :key="rule.host">
              <div v-for="path in rule.http.paths" :key="path.path" class="path-item">
                <span class="path-text">{{ path.path }}</span>
                <span class="service-text"> -> {{ path.backend.service.name }}:{{ path.backend.service.port.number }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="metadata.creationTimestamp" label="创建时间" width="200" />
        <el-table-column label="操作" width="200" class-name="sticky-right-col">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="danger" size="small" @click="deleteIngress(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Ingress详情对话框 -->
    <el-dialog v-model="detailVisible" title="Ingress详情" width="900px">
      <el-descriptions :data="selectedIngress" border column="1" v-if="selectedIngress">
        <el-descriptions-item label="名称" prop="metadata.name" />
        <el-descriptions-item label="命名空间" prop="metadata.namespace" />
        <el-descriptions-item label="入口类" prop="spec.ingressClassName">
          {{ selectedIngress.spec.ingressClassName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="TLS配置" v-if="selectedIngress.spec.tls">
          <div v-for="tls in selectedIngress.spec.tls" :key="tls.secretName" class="mb-2">
            <el-tag type="warning" size="small">证书: {{ tls.secretName }}</el-tag>
            <div class="mt-1">
              域名: <span v-for="host in tls.hosts" :key="host" class="mr-2">{{ host }}</span>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="路由规则">
          <div v-for="rule in selectedIngress.spec.rules" :key="rule.host" class="rule-item mb-3">
            <el-tag type="primary" class="mb-2">Host: {{ rule.host }}</el-tag>
            <div v-for="path in rule.http.paths" :key="path.path" class="pl-4">
              <div class="path-info">
                <span class="path-label">路径:</span> <code>{{ path.path }}</code>
                <span class="path-type">({{ path.pathType }})</span>
              </div>
              <div class="service-info pl-4">
                <span class="service-label">后端服务:</span>
                <span>{{ path.backend.service.name }}:{{ path.backend.service.port.number }}</span>
              </div>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="负载均衡地址">
          <div v-for="ingress in selectedIngress.status.loadBalancer.ingress" :key="ingress.ip || ingress.hostname">
            <el-tag type="success" size="small">{{ ingress.ip || ingress.hostname }}</el-tag>
          </div>
          <span v-if="!selectedIngress.status.loadBalancer.ingress?.length">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" prop="metadata.creationTimestamp" />
        <el-descriptions-item label="标签">
          <el-tag v-for="(value, key) in selectedIngress.metadata.labels" :key="key" class="mr-1">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注解">
          <el-tag v-for="(value, key) in selectedIngress.metadata.annotations" :key="key" class="mr-1" type="info">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const namespace = ref('default')
const namespaces = ref([])
const ingresses = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedIngress = ref(null)

// 加载命名空间列表
const loadNamespaces = async () => {
  try {
    const res = await request.get('/api/v1/k8s/namespaces')
    if (res.code === 200) {
      namespaces.value = res.data.items
    }
  } catch (error) {
    ElMessage.error('加载命名空间失败')
  }
}

// 加载Ingress列表
const loadIngresses = async () => {
  if (!namespace.value) return
  loading.value = true
  try {
    const res = await request.get(`/api/v1/k8s/namespaces/${namespace.value}/ingresses`)
    if (res.code === 200) {
      ingresses.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载Ingress列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (row) => {
  selectedIngress.value = row
  detailVisible.value = true
}

// 删除Ingress
const deleteIngress = (row) => {
  ElMessageBox.confirm(
    `确定要删除Ingress "${row.metadata.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/v1/k8s/namespaces/${row.metadata.namespace}/ingresses/${row.metadata.name}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadIngresses()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadNamespaces()
  loadIngresses()
})
</script>

<style scoped>
.ingress-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.host-item {
  margin-bottom: 4px;
}
.path-item {
  margin-bottom: 4px;
  font-size: 12px;
}
.path-text {
  color: #409eff;
  font-weight: bold;
}
.service-text {
  color: #67c23a;
}
.rule-item {
  border-left: 2px solid #409eff;
  padding-left: 12px;
}
.path-info {
  margin-bottom: 4px;
}
.path-label {
  font-weight: bold;
  margin-right: 8px;
}
.path-type {
  color: #909399;
  margin-left: 8px;
}
.service-info {
  margin-bottom: 8px;
}
.service-label {
  font-weight: bold;
  margin-right: 8px;
  color: #67c23a;
}
code {
  background-color: #f5f7fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}
</style>
