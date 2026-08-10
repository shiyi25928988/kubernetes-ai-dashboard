<template>
  <div class="pod-container">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>{{ t('pod.title') }}</span>
          <div class="header-right">
            <el-button type="primary" size="small" @click="loadPods" :loading="loading">
              <el-icon class="mr-1"><Refresh /></el-icon>
              {{ t('common.refresh') }}
            </el-button>
            <el-select v-model="namespace" :placeholder="t('common.selectNamespace')" class="w-48 ml-2" @change="loadPods">
              <el-option :label="t('common.allNamespaces')" value="" />
              <el-option
                v-for="ns in namespaces"
                :key="ns.metadata.name"
                :label="ns.metadata.name"
                :value="ns.metadata.name"
              />
            </el-select>
          </div>
        </div>
      </template>

      <div v-if="pods.length === 0 && !loading" class="empty-state">
        <el-empty :description="t('pod.empty')">
          <el-button type="primary" @click="loadPods">{{ t('common.refreshData') }}</el-button>
        </el-empty>
      </div>
      <el-table v-else :data="pods" v-loading="loading" stripe>
        <el-table-column prop="metadata.name" :label="t('common.name')" />
        <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
        <el-table-column prop="status.phase" :label="t('common.status')">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status.phase)">
              {{ scope.row.status.phase }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status.podIP" :label="t('pod.podIP')" />
        <el-table-column prop="status.hostIP" :label="t('pod.hostIP')" />
        <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
        <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">{{ t('common.details') }}</el-button>
            <el-button type="warning" size="small" @click="viewLogs(scope.row)">{{ t('common.logs') }}</el-button>
            <el-button type="danger" size="small" @click="deletePod(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Pod详情对话框 -->
    <el-dialog v-model="detailVisible" :title="t('pod.detail')" width="800px">
      <el-descriptions :data="selectedPod" border column="1" v-if="selectedPod">
        <el-descriptions-item :label="t('common.name')" prop="metadata.name" />
        <el-descriptions-item :label="t('common.namespace')" prop="metadata.namespace" />
        <el-descriptions-item :label="t('common.status')" prop="status.phase">
          <el-tag :type="getStatusType(selectedPod.status.phase)">
            {{ selectedPod.status.phase }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('pod.podIP')" prop="status.podIP" />
        <el-descriptions-item :label="t('pod.hostIP')" prop="status.hostIP" />
        <el-descriptions-item :label="t('common.createdAt')" prop="metadata.creationTimestamp" />
        <el-descriptions-item :label="t('pod.labels')">
          <el-tag v-for="(value, key) in selectedPod.metadata.labels" :key="key" class="mr-1">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('pod.annotations')">
          <el-tag v-for="(value, key) in selectedPod.metadata.annotations" :key="key" class="mr-1" type="info">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 日志对话框 -->
    <el-dialog v-model="logsVisible" :title="t('pod.logs')" width="1000px">
      <el-card class="log-card">
        <pre class="log-content">{{ logs }}</pre>
      </el-card>
      <template #footer>
        <el-button @click="logsVisible = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { t } = useI18n({ useScope: 'global' })

const namespace = ref('')
const namespaces = ref([])
const pods = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const logsVisible = ref(false)
const selectedPod = ref(null)
const logs = ref('')

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    Running: 'success',
    Pending: 'warning',
    Succeeded: 'info',
    Failed: 'danger',
    Unknown: 'info'
  }
  return statusMap[status] || 'info'
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

// 加载Pod列表
const loadPods = async () => {
  loading.value = true
  try {
    let res
    if (namespace.value) {
      res = await request.get(`/api/v1/k8s/namespaces/${namespace.value}/pods`)
    } else {
      res = await request.get(`/api/v1/k8s/pods`)
    }
    if (res.code === 200) {
      pods.value = res.data
    }
  } catch (error) {
    ElMessage.error(t('pod.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (row) => {
  selectedPod.value = row
  detailVisible.value = true
}

// 查看日志
const viewLogs = async (row) => {
  try {
    const res = await request.get(`/api/v1/k8s/namespaces/${row.metadata.namespace}/pods/${row.metadata.name}/logs`)
    if (res.code === 200) {
      logs.value = res.data
      logsVisible.value = true
    }
  } catch (error) {
    ElMessage.error(t('common.getLogsFailed'))
  }
}

// 删除Pod
const deletePod = (row) => {
  ElMessageBox.confirm(
    t('pod.confirmDelete', { name: row.metadata.name }),
    t('common.tip'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/v1/k8s/namespaces/${row.metadata.namespace}/pods/${row.metadata.name}`)
      if (res.code === 200) {
        ElMessage.success(t('common.deleteSuccess'))
        loadPods()
      }
    } catch (error) {
      ElMessage.error(t('common.deleteFailed'))
    }
  })
}

onMounted(() => {
  loadNamespaces().catch(err => console.error('load namespaces failed:', err))
  loadPods().catch(err => console.error('load pods failed:', err))
})

onActivated(() => {
  loadPods().catch(err => console.error('refresh pods failed:', err))
})
</script>

<style scoped>
.pod-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
}
.log-card {
  background-color: #f5f7fa;
}
.log-content {
  max-height: 500px;
  overflow-y: auto;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
