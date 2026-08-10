<template>
  <div class="service-container">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>{{ t('service.title') }}</span>
          <el-select v-model="namespace" :placeholder="t('common.selectNamespace')" class="w-48" @change="loadServices">
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
        <el-table-column prop="metadata.name" :label="t('common.name')" width="200" />
        <el-table-column prop="metadata.namespace" :label="t('common.namespace')" width="150" />
        <el-table-column prop="spec.type" :label="t('common.type')" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.spec.type)">
              {{ scope.row.spec.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec.clusterIP" :label="t('service.clusterIP')" width="150" />
        <el-table-column :label="t('service.ports')" width="200">
          <template #default="scope">
            <div v-for="port in scope.row.spec.ports" :key="port.port" class="port-item">
              <el-tag size="small">{{ port.port }} -> {{ port.targetPort }}/{{ port.protocol }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="200" />
        <el-table-column :label="t('common.actions')" width="200" class-name="sticky-right-col">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">{{ t('common.details') }}</el-button>
            <el-button type="danger" size="small" @click="deleteService(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Service详情对话框 -->
    <el-dialog v-model="detailVisible" :title="t('service.detail')" width="800px">
      <el-descriptions :data="selectedService" border column="1" v-if="selectedService">
        <el-descriptions-item :label="t('common.name')" prop="metadata.name" />
        <el-descriptions-item :label="t('common.namespace')" prop="metadata.namespace" />
        <el-descriptions-item :label="t('common.type')" prop="spec.type">
          <el-tag :type="getTypeTag(selectedService.spec.type)">
            {{ selectedService.spec.type }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('service.clusterIP')" prop="spec.clusterIP" />
        <el-descriptions-item :label="t('service.externalIP')" prop="spec.externalIPs">
          {{ selectedService.spec.externalIPs?.join(', ') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('service.ports')">
          <div v-for="port in selectedService.spec.ports" :key="port.port" class="mb-1">
            <el-tag size="small">{{ port.name || '' }}: {{ port.port }} -> {{ port.targetPort }}/{{ port.protocol }}</el-tag>
            <span v-if="port.nodePort"> (NodePort: {{ port.nodePort }})</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="t('service.selector')">
          <el-tag v-for="(value, key) in selectedService.spec.selector" :key="key" class="mr-1">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('common.createdAt')" prop="metadata.creationTimestamp" />
        <el-descriptions-item :label="t('common.labels')">
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
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'

const { t } = useI18n({ useScope: 'global' })

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
    ElMessage.error(t('common.loadNamespacesFailed'))
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
    ElMessage.error(t('service.loadFailed'))
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
    t('service.confirmDelete', { name: row.metadata.name }),
    t('common.tip'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/v1/k8s/namespaces/${row.metadata.namespace}/services/${row.metadata.name}`)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        loadServices()
      }
    } catch (error) {
      ElMessage.error(t('common.deleteFailed'))
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
