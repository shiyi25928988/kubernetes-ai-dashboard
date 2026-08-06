<template>
  <div class="pod-container">
    <el-card shadow="hover" class="mb-4">
      <template #header>
        <div class="card-header">
          <span>Pod管理</span>
          <div class="header-right">
            <el-button type="primary" size="small" @click="loadPods" :loading="loading">
              <el-icon class="mr-1"><Refresh /></el-icon>
              刷新
            </el-button>
            <el-select v-model="namespace" placeholder="选择命名空间" class="w-48 ml-2" @change="loadPods">
              <el-option label="所有命名空间" value="" />
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
        <el-empty description="暂无Pod数据">
          <el-button type="primary" @click="loadPods">刷新数据</el-button>
        </el-empty>
      </div>
      <el-table v-else :data="pods" v-loading="loading" stripe>
        <el-table-column prop="metadata.name" label="名称" />
        <el-table-column prop="metadata.namespace" label="命名空间" />
        <el-table-column prop="status.phase" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status.phase)">
              {{ scope.row.status.phase }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status.podIP" label="Pod IP" />
        <el-table-column prop="status.hostIP" label="主机IP" />
        <el-table-column prop="metadata.creationTimestamp" label="创建时间" />
        <el-table-column label="操作" class-name="sticky-right-col">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="warning" size="small" @click="viewLogs(scope.row)">日志</el-button>
            <el-button type="danger" size="small" @click="deletePod(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Pod详情对话框 -->
    <el-dialog v-model="detailVisible" title="Pod详情" width="800px">
      <el-descriptions :data="selectedPod" border column="1" v-if="selectedPod">
        <el-descriptions-item label="名称" prop="metadata.name" />
        <el-descriptions-item label="命名空间" prop="metadata.namespace" />
        <el-descriptions-item label="状态" prop="status.phase">
          <el-tag :type="getStatusType(selectedPod.status.phase)">
            {{ selectedPod.status.phase }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Pod IP" prop="status.podIP" />
        <el-descriptions-item label="主机IP" prop="status.hostIP" />
        <el-descriptions-item label="创建时间" prop="metadata.creationTimestamp" />
        <el-descriptions-item label="标签">
          <el-tag v-for="(value, key) in selectedPod.metadata.labels" :key="key" class="mr-1">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注解">
          <el-tag v-for="(value, key) in selectedPod.metadata.annotations" :key="key" class="mr-1" type="info">
            {{ key }}: {{ value }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 日志对话框 -->
    <el-dialog v-model="logsVisible" title="Pod日志" width="1000px">
      <el-card class="log-card">
        <pre class="log-content">{{ logs }}</pre>
      </el-card>
      <template #footer>
        <el-button @click="logsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

console.log('Pod.vue 组件加载')

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
    ElMessage.error('加载命名空间失败')
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
    ElMessage.error('加载Pod列表失败')
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
    ElMessage.error('获取日志失败')
  }
}

// 删除Pod
const deletePod = (row) => {
  ElMessageBox.confirm(
    `确定要删除Pod "${row.metadata.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await request.delete(`/api/v1/k8s/namespaces/${row.metadata.namespace}/pods/${row.metadata.name}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadPods()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  console.log('Pod页面加载，开始调用接口')
  loadNamespaces().catch(err => console.error('加载命名空间失败:', err))
  loadPods().catch(err => console.error('加载Pod失败:', err))
})

onActivated(() => {
  console.log('Pod页面激活，刷新数据')
  loadPods().catch(err => console.error('刷新Pod失败:', err))
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
