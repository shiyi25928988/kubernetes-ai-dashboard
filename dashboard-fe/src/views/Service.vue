<template>
  <div class="service-container">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>Service管理</span>
          <el-select v-model="namespace" placeholder="选择命名空间" class="w-48" @change="loadServices">
            <el-option
              v-for="ns in namespaces"
              :key="ns.metadata.name"
              :label="ns.metadata.name"
              :value="ns.metadata.name"
            />
          </el-select>
        </div>
      </template>

      <el-table :data="services" v-loading="loading" stripe table-layout="fixed">
        <el-table-column prop="metadata.name" label="名称" width="200" />
        <el-table-column prop="metadata.namespace" label="命名空间" width="150" />
        <el-table-column prop="spec.type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.spec.type)">
              {{ scope.row.spec.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec.clusterIP" label="集群IP" width="150" />
        <el-table-column label="端口" width="200">
          <template #default="scope">
            <div v-for="port in scope.row.spec.ports" :key="port.port" class="port-item">
              <el-tag size="small">{{ port.port }} -> {{ port.targetPort }}/{{ port.protocol }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="metadata.creationTimestamp" label="创建时间" width="200" />
        <el-table-column label="操作" width="200" class-name="sticky-right-col">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="danger" size="small" @click="deleteService(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Service详情对话框 -->
    <el-dialog v-model="detailVisible" title="Service详情" width="800px">
      <el-descriptions :data="selectedService" border column="1" v-if="selectedService">
        <el-descriptions-item label="名称" prop="metadata.name" />
        <el-descriptions-item label="命名空间" prop="metadata.namespace" />
        <el-descriptions-item label="类型" prop="spec.type">
          <el-tag :type="getTypeTag(selectedService.spec.type)">
            {{ selectedService.spec.type }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="集群IP" prop="spec.clusterIP" />
        <el-descriptions-item label="外部IP" prop="spec.externalIPs">
          {{ selectedService.spec.externalIPs?.join(', ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="端口">
          <div v-for="port in selectedService.spec.ports" :key="port.port" class="mb-1">
            <el-tag size="small">{{ port.name || '' }}: {{ port.port }} -> {{ port.targetPort }}/{{ port.protocol }}</el-tag>
            <span v-if="port.nodePort"> (NodePort: {{ port.nodePort }})</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="选择器">
          <el-tag v-for="(value, key) in selectedService.spec.selector" :key="key" class="mr-1">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" prop="metadata.creationTimestamp" />
        <el-descriptions-item label="标签">
          <el-tag v-for="(value, key) in selectedService.metadata.labels" :key="key" class="mr-1">
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
const services = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedService = ref(null)

// 获取类型标签
const getTypeTag = (type) => {
  const typeMap = {
    ClusterIP: 'primary',
    NodePort: 'warning',
    LoadBalancer: 'success',
    ExternalName: 'info'
  }
  return typeMap[type] || 'info'
}

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

// 加载Service列表
const loadServices = async () => {
  if (!namespace.value) return
  loading.value = true
  try {
    const res = await request.get(`/api/v1/k8s/namespaces/${namespace.value}/services`)
    if (res.code === 200) {
      services.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载Service列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (row) => {
  selectedService.value = row
  detailVisible.value = true
}

// 删除Service
const deleteService = (row) => {
  ElMessageBox.confirm(
    `确定要删除Service "${row.metadata.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/v1/k8s/namespaces/${row.metadata.namespace}/services/${row.metadata.name}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadServices()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadNamespaces()
  loadServices()
})
</script>

<style scoped>
.service-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.port-item {
  margin-bottom: 4px;
}
</style>
