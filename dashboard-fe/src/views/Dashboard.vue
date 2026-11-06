<template>
  <div class="dashboard-container">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <h2>{{ headerTitle }}</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" size="small" @click="showCreateResourceDialog = true" style="margin-right: 20px;">
          <el-icon><plus /></el-icon> {{ t('header.createResource') }}
        </el-button>
        <LanguageSwitcher />
        <!-- 消息图标 -->
        <div class="message-icon-wrapper" @click="openInboxDialog" style="margin-right: 20px; cursor: pointer; position: relative;">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
            <el-icon size="22"><Bell /></el-icon>
          </el-badge>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-avatar :size="32" :src="userInfo.avatar || 'https://cube.elemencdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
            <span class="username">{{ userInfo.username || userInfo.account }}</span>
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="inbox">
                <el-icon><Message /></el-icon>
                {{ t('header.inbox') }} <span v-if="unreadCount > 0" style="color: #f56c6c; margin-left: 5px;">({{ unreadCount }})</span>
              </el-dropdown-item>
              <el-dropdown-item command="check-cluster">
                <el-icon><Refresh /></el-icon>
                {{ t('header.manualInspect') }}
              </el-dropdown-item>
              <el-dropdown-item command="profile">{{ t('header.profile') }}</el-dropdown-item>
              <el-dropdown-item command="logout">{{ t('header.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="dashboard-main">
      <!-- 侧边栏 -->
      <aside class="dashboard-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="overview">
            <el-icon><monitor /></el-icon>
            <span>{{ t('menu.overview') }}</span>
          </el-menu-item>
          <el-sub-menu index="k8s">
            <template #title>
              <el-icon><box /></el-icon>
              <span>{{ t('menu.k8s') }}</span>
            </template>
            <el-menu-item index="namespace">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ t('menu.namespace') }}</span>
            </el-menu-item>
            <el-menu-item index="pod">
              <el-icon><Cpu /></el-icon>
              <span>{{ t('menu.pod') }}</span>
            </el-menu-item>
            <el-menu-item index="deployment">
              <el-icon><Monitor /></el-icon>
              <span>{{ t('menu.deployment') }}</span>
            </el-menu-item>
            <el-menu-item index="statefulset">
              <el-icon><Files /></el-icon>
              <span>{{ t('menu.statefulset') }}</span>
            </el-menu-item>
            <el-menu-item index="daemonset">
              <el-icon><Files /></el-icon>
              <span>{{ t('menu.daemonset') }}</span>
            </el-menu-item>
            <el-menu-item index="job">
              <el-icon><Operation /></el-icon>
              <span>{{ t('menu.job') }}</span>
            </el-menu-item>
            <el-menu-item index="service">
              <el-icon><Service /></el-icon>
              <span>{{ t('menu.service') }}</span>
            </el-menu-item>
            <el-menu-item index="ingress">
              <el-icon><Link /></el-icon>
              <span>{{ t('menu.ingress') }}</span>
            </el-menu-item>
            <el-menu-item index="pvc">
              <el-icon><Files /></el-icon>
              <span>{{ t('menu.pvc') }}</span>
            </el-menu-item>
            <el-menu-item index="configmap">
              <el-icon><Document /></el-icon>
              <span>{{ t('menu.configmap') }}</span>
            </el-menu-item>
            <el-menu-item index="secret">
              <el-icon><Key /></el-icon>
              <span>{{ t('menu.secret') }}</span>
            </el-menu-item>
            <el-menu-item index="crd">
              <el-icon><Grid /></el-icon>
              <span>{{ t('menu.crd') }}</span>
            </el-menu-item>
            <el-menu-item index="event">
              <el-icon><WarningFilled /></el-icon>
              <span>{{ t('menu.event') }}</span>
            </el-menu-item>
            <el-menu-item index="node">
              <el-icon><Cpu /></el-icon>
              <span>{{ t('menu.node') }}</span>
            </el-menu-item>
            <el-menu-item index="pv">
              <el-icon><Files /></el-icon>
              <span>{{ t('menu.pv') }}</span>
            </el-menu-item>
            <el-menu-item index="storageclass">
              <el-icon><Document /></el-icon>
              <span>{{ t('menu.storageclass') }}</span>
            </el-menu-item>
            <el-menu-item index="hpa">
              <el-icon><Operation /></el-icon>
              <span>{{ t('menu.hpa') }}</span>
            </el-menu-item>
            <el-menu-item index="networkpolicy">
              <el-icon><Link /></el-icon>
              <span>{{ t('menu.networkpolicy') }}</span>
            </el-menu-item>
            <el-menu-item index="rbac">
              <el-icon><Key /></el-icon>
              <span>{{ t('menu.rbac') }}</span>
            </el-menu-item>
            <el-menu-item index="quota">
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ t('menu.quota') }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="cluster">
            <el-icon><Connection /></el-icon>
            <span>{{ t('menu.cluster') }}</span>
          </el-menu-item>
          <el-menu-item index="oss">
            <el-icon><FolderOpened /></el-icon>
            <span>{{ t('menu.oss') }}</span>
          </el-menu-item>
          <el-menu-item index="registry">
            <el-icon><Box /></el-icon>
            <span>{{ t('menu.registry') }}</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 主内容区 -->
      <main class="dashboard-content">
        <div class="content-wrapper">
          <!-- 概览页面 -->
          <div v-if="activeMenu === 'overview'" class="overview-page">
            <!-- 第一行：核心资源统计 -->
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon blue">
                      <el-icon><service /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.namespaceCount }}</div>
                      <div class="stat-label">{{ t('overview.stat.namespace') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon green">
                      <el-icon><box /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.podRunningCount }}/{{ k8sStats.podTotalCount }}</div>
                      <div class="stat-label">{{ t('overview.stat.runningPods') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon orange">
                      <el-icon><service /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.serviceCount }}</div>
                      <div class="stat-label">Service</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon purple">
                      <el-icon><data-analysis /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.pvcCount }}</div>
                      <div class="stat-label">{{ t('overview.stat.pvc') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 第二行：基础设施统计 -->
            <el-row :gutter="20" class="mt-20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon blue">
                      <el-icon><Monitor /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.readyNodeCount }}/{{ k8sStats.nodeCount }}</div>
                      <div class="stat-label">{{ t('overview.stat.readyNodes') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon green">
                      <el-icon><Box /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.deploymentCount }}</div>
                      <div class="stat-label">Deployment</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon orange">
                      <el-icon><Box /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.storageUsed }}/{{ k8sStats.storageTotal }}</div>
                      <div class="stat-label">{{ t('overview.stat.storageUsage') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon purple">
                      <el-icon><service /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-value">{{ k8sStats.ingressCount }}</div>
                      <div class="stat-label">{{ t('overview.stat.ingress') }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 第三行：健康状态面板 -->
            <el-row :gutter="20" class="mt-20">
              <el-col :span="6">
                <el-card class="health-card">
                  <div class="health-item">
                    <div class="health-icon" :class="k8sStats.controlPlaneHealth">
                      <el-icon>
                        <CircleCheck v-if="k8sStats.controlPlaneHealth === 'normal'" />
                        <WarningFilled v-else-if="k8sStats.controlPlaneHealth === 'warning'" />
                        <CircleClose v-else />
                      </el-icon>
                    </div>
                    <div class="health-info">
                      <div class="health-title">{{ t('overview.health.controlPlane') }}</div>
                      <div class="health-status" :class="k8sStats.controlPlaneHealth">
                        {{ k8sStats.controlPlaneHealth === 'normal' ? t('overview.health.healthy') : k8sStats.controlPlaneHealth === 'warning' ? t('overview.health.warning') : t('overview.health.abnormal') }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="health-card">
                  <div class="health-item">
                    <div class="health-icon" :class="k8sStats.nodeHealth">
                      <el-icon>
                        <CircleCheck v-if="k8sStats.nodeHealth === 'normal'" />
                        <WarningFilled v-else-if="k8sStats.nodeHealth === 'warning'" />
                        <CircleClose v-else />
                      </el-icon>
                    </div>
                    <div class="health-info">
                      <div class="health-title">{{ t('overview.health.nodeHealth') }}</div>
                      <div class="health-status" :class="k8sStats.nodeHealth">
                        {{ k8sStats.nodeHealth === 'normal' ? t('overview.health.healthy') : k8sStats.nodeHealth === 'warning' ? t('overview.health.warning') : t('overview.health.abnormal') }}
                        <span v-if="k8sStats.abnormalNodeCount > 0" class="abnormal-count">{{ t('overview.health.abnormalTip', { x: k8sStats.abnormalNodeCount }) }}</span>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="health-card">
                  <div class="health-item">
                    <div class="health-icon" :class="k8sStats.networkHealth">
                      <el-icon>
                        <CircleCheck v-if="k8sStats.networkHealth === 'normal'" />
                        <WarningFilled v-else-if="k8sStats.networkHealth === 'warning'" />
                        <CircleClose v-else />
                      </el-icon>
                    </div>
                    <div class="health-info">
                      <div class="health-title">{{ t('overview.health.network') }}</div>
                      <div class="health-status" :class="k8sStats.networkHealth">
                        {{ k8sStats.networkHealth === 'normal' ? t('overview.health.normal') : k8sStats.networkHealth === 'warning' ? t('overview.health.warning') : t('overview.health.abnormal') }}
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="health-card">
                  <div class="health-item">
                    <div class="health-icon" :class="k8sStats.storageHealth">
                      <el-icon>
                        <CircleCheck v-if="k8sStats.storageHealth === 'normal'" />
                        <WarningFilled v-else-if="k8sStats.storageHealth === 'warning'" />
                        <CircleClose v-else />
                      </el-icon>
                    </div>
                    <div class="health-info">
                      <div class="health-title">{{ t('overview.health.storage') }}</div>
                      <div class="health-status" :class="k8sStats.storageHealth">
                        {{ k8sStats.storageHealth === 'normal' ? t('overview.health.normal') : k8sStats.storageHealth === 'warning' ? t('overview.health.warning') : t('overview.health.abnormal') }}
                        <span v-if="k8sStats.abnormalVolumeCount > 0" class="abnormal-count">{{ t('overview.health.abnormalTip', { x: k8sStats.abnormalVolumeCount }) }}</span>
                      </div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 第四行：资源利用率 -->
            <el-card class="mt-20">
              <template #header>
                <span>{{ t('overview.usage.title') }}</span>
              </template>
              <el-row :gutter="40" style="padding: 20px 0;">
                <el-col :span="6" style="text-align: center;">
                  <div class="usage-title">{{ t('overview.usage.cpu') }}<br/><small>{{ k8sStats.cpuUsed }}/{{ k8sStats.cpuTotal }}</small></div>
                  <el-progress type="circle" :percentage="k8sStats.cpuUsage" :width="120" color="#67C23A" />
                </el-col>
                <el-col :span="6" style="text-align: center;">
                  <div class="usage-title">{{ t('overview.usage.memory') }}<br/><small>{{ k8sStats.memoryUsed }}/{{ k8sStats.memoryTotal }}</small></div>
                  <el-progress type="circle" :percentage="k8sStats.memoryUsage" :width="120" color="#409EFF" />
                </el-col>
                <el-col :span="6" style="text-align: center;">
                  <div class="usage-title">{{ t('overview.usage.storage') }}<br/><small>{{ k8sStats.storageUsed }}/{{ k8sStats.storageTotal }}</small></div>
                  <el-progress type="circle" :percentage="k8sStats.storageUsage" :width="120" color="#E6A23C" />
                </el-col>
                <el-col :span="6" style="text-align: center;">
                  <div class="usage-title">{{ t('overview.usage.abnormalPods') }}</div>
                  <div style="font-size: 32px; font-weight: 700; margin: 16px 0; color: #F56C6C;">{{ k8sStats.abnormalPodCount }}</div>
                </el-col>
              </el-row>
            </el-card>

          </div>

          <!-- K8S命名空间页面 -->
          <div v-if="activeMenu === 'namespace'" class="namespace-page">
            <div class="page-header">
              <h3>{{ t('namespace.title') }}</h3>
              <el-button type="primary" @click="showCreateNamespaceDialog = true">{{ t('namespace.create') }}</el-button>
            </div>
            <el-table :data="namespaceList" border stripe>
              <el-table-column prop="name" :label="t('common.name')" />
              <el-table-column prop="status" :label="t('common.status')" />
              <el-table-column :label="t('common.creator')">
                <template #default="{ row }">
                  <span>{{ getOwnerName('Namespace', '', row.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')">
                <template #default="{ row }">
                  <el-button type="danger" size="small" @click="handleDeleteNamespace(row.name)" v-if="row.labels?.['dssp.io/managed-by'] === 'dssp' && row.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- K8S Pod管理页面 -->
          <div v-if="activeMenu === 'pod'" class="pod-page">
            <div class="page-header">
              <h3>{{ t('pod.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadPodList" :loading="podLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="podNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  class="w-60 ml-2 pod-namespace-select"
                  @change="loadPodList"
                >
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                  <!-- 自定义显示模板，强制显示选中内容 -->
                  <template #prefix v-if="podNamespaceFilter">
                    <span>{{ podNamespaceFilter === 'all' ? t('common.allNamespaces') : podNamespaceFilter }}</span>
                  </template>
                </el-select>
              </div>
            </div>

            <div v-if="podList.length === 0 && !podLoading" class="empty-state">
              <el-empty :description="t('pod.empty')">
                <el-button type="primary" @click="loadPodList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>

            <el-table v-else :data="podList" v-loading="podLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column prop="status.phase" :label="t('common.status')">
                <template #default="scope">
                  <el-tag :type="getPodStatusType(scope.row.status.phase)">
                    {{ scope.row.status.phase }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status.podIP" label="Pod IP" />
              <el-table-column prop="status.hostIP" :label="t('pod.hostIP')" />
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewPodDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="warning" size="small" @click="viewPodLogs(scope.row)">{{ t('common.logs') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeletePod(scope.row)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Deployment管理页面 -->
          <div v-if="activeMenu === 'deployment'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('deployment.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadDeploymentList" :loading="deploymentLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="deploymentNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  class="w-60 ml-2"
                  @change="loadDeploymentList"
                >
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                  <template #prefix v-if="deploymentNamespaceFilter">
                    <span>{{ deploymentNamespaceFilter === 'all' ? t('common.allNamespaces') : deploymentNamespaceFilter }}</span>
                  </template>
                </el-select>
                <el-button type="success" size="small" @click="showCreateResourceDialog = true; currentResourceType = 'deployment'" class="ml-2">
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('deployment.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="deploymentList.length === 0 && !deploymentLoading" class="empty-state">
              <el-empty :description="t('deployment.empty')">
                <el-button type="primary" @click="loadDeploymentList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>

            <el-table v-else :data="deploymentList" v-loading="deploymentLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('deployment.replicaStatus')">
                <template #default="scope">
                  <el-tag :type="getDeploymentStatusType(scope.row)">
                    {{ scope.row.status?.availableReplicas || 0 }}/{{ scope.row.spec?.replicas || 0 }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('deployment.available')" prop="status.availableReplicas" />
              <el-table-column :label="t('deployment.desired')" prop="spec.replicas" />
              <el-table-column :label="t('deployment.updatedVersion')" prop="metadata.labels.app" />
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('Deployment', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewDeploymentDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="warning" size="small" @click="handleScaleDeployment(scope.row)">{{ t('deployment.scale') }}</el-button>
                  <el-button type="info" size="small" @click="handleRestartDeployment(scope.row)">{{ t('deployment.restart') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteDeployment(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- StatefulSet管理页面 -->
          <div v-if="activeMenu === 'statefulset'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('sts.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadStsList" :loading="stsLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select v-model="stsNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadStsList">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>

            <div v-if="stsList.length === 0 && !stsLoading" class="empty-state">
              <el-empty :description="t('sts.empty')">
                <el-button type="primary" @click="loadStsList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="stsList" v-loading="stsLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('deployment.replicaStatus')">
                <template #default="scope">
                  <el-tag :type="getStsStatusType(scope.row)">
                    {{ scope.row.status?.readyReplicas || 0 }}/{{ scope.row.spec?.replicas || 0 }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('deployment.desired')" prop="spec.replicas" />
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('StatefulSet', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewStsDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="warning" size="small" @click="handleStsScale(scope.row)">{{ t('deployment.scale') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteSts(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- DaemonSet管理页面 -->
          <div v-if="activeMenu === 'daemonset'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('ds.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadDsList" :loading="dsLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select v-model="dsNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadDsList">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>

            <div v-if="dsList.length === 0 && !dsLoading" class="empty-state">
              <el-empty :description="t('ds.empty')">
                <el-button type="primary" @click="loadDsList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="dsList" v-loading="dsLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('ds.readyNode')">
                <template #default="scope">
                  <el-tag :type="getDsStatusType(scope.row)">
                    {{ scope.row.status?.numberReady || 0 }}/{{ scope.row.status?.desiredNumberScheduled || 0 }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('ds.desiredNode')" prop="status.desiredNumberScheduled" />
              <el-table-column :label="t('ds.currentNumber')" prop="status.currentNumberScheduled" />
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewDsDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteDs(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Job管理页面 -->
          <div v-if="activeMenu === 'job'" class="configmap-page">
            <div class="page-header">
              <h3>{{ t('job.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadJobList" :loading="jobLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="jobNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  size="small"
                  style="width: 180px"
                  @change="loadJobList"
                >
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                </el-select>
                <el-button
                  type="success"
                  size="small"
                  @click="showCreateResourceDialog = true; currentResourceType = 'job'"
                >
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('job.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="jobList.length === 0 && !jobLoading" class="empty-state">
              <el-empty :description="t('job.empty')" />
            </div>

            <el-table v-else :data="jobList" v-loading="jobLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" show-overflow-tooltip />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column label="Completions">
                <template #default="scope">
                  {{ scope.row.spec?.completions || '-' }}
                </template>
              </el-table-column>
              <el-table-column :label="t('job.completions')">
                <template #default="scope">
                  <span :class="scope.row.status?.succeeded === scope.row.spec?.completions ? 'text-success' : ''">
                    {{ scope.row.status?.succeeded ?? 0 }} / {{ scope.row.spec?.completions ?? '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.status')">
                <template #default="scope">
                  <el-tag
                    v-if="scope.row.status?.active > 0"
                    type="primary"
                    size="small"
                  >{{ t('job.running') }}</el-tag>
                  <el-tag
                    v-else-if="scope.row.status?.succeeded >= (scope.row.spec?.completions || 1)"
                    type="success"
                    size="small"
                  >{{ t('job.completed') }}</el-tag>
                  <el-tag
                    v-else-if="scope.row.status?.failed > 0"
                    type="danger"
                    size="small"
                  >{{ t('job.failed') }}</el-tag>
                  <el-tag v-else type="info" size="small">{{ t('job.unknown') }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('Job', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewJobDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="success" size="small" @click="viewJobLogs(scope.row)">{{ t('common.logs') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteJob(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Job日志弹窗 -->
          <el-dialog v-model="showJobLogsDialog" :title="t('job.logs')" width="1000px">
            <div v-if="Object.keys(jobLogs).length > 0">
              <el-tabs v-model="activeJobLogTab" type="border-card">
                <el-tab-pane v-for="(logs, podName) in jobLogs" :key="podName" :label="podName">
                  <el-card class="log-card">
                    <pre class="log-content">{{ logs }}</pre>
                  </el-card>
                </el-tab-pane>
              </el-tabs>
            </div>
            <div v-else class="text-center py-8">
              <el-empty :description="t('job.noLog')" />
            </div>
            <template #footer>
              <el-button @click="showJobLogsDialog = false">{{ t('common.close') }}</el-button>
              <el-button type="primary" @click="viewJobLogs(selectedJob)">{{ t('common.refresh') }}</el-button>
            </template>
          </el-dialog>

          <!-- Job详情弹窗 -->
          <el-dialog v-model="showJobDetailDialog" :title="t('job.detail')" width="1000px">
            <div v-if="jobDetail" class="detail-container">
              <el-tabs v-model="activeJobTab" type="border-card">
                <el-tab-pane :label="t('common.basicInfo')" name="jobBase">
                  <el-descriptions border column="2" size="small">
                    <el-descriptions-item :label="t('common.name')">{{ jobDetail.metadata?.name || '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('common.namespace')">{{ jobDetail.metadata?.namespace || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="UID">{{ jobDetail.metadata?.uid || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="ResourceVersion">{{ jobDetail.metadata?.resourceVersion || '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('common.createdAt')">{{ formatTimestamp(jobDetail.metadata?.creationTimestamp) }}</el-descriptions-item>
                    <el-descriptions-item label="Owner">
                      <template v-if="jobDetail.metadata?.ownerReferences?.[0]">
                        {{ jobDetail.metadata.ownerReferences[0].kind }}/{{ jobDetail.metadata.ownerReferences[0].name }}
                      </template>
                      <span v-else>-</span>
                    </el-descriptions-item>
                    <el-descriptions-item :label="t('common.labels')" span="2">
                      <el-tag v-for="(value, key) in jobDetail.metadata?.labels" :key="key" size="small" class="mr-1 mb-1">
                        {{ key }}={{ value }}
                      </el-tag>
                      <span v-if="!jobDetail.metadata?.labels || Object.keys(jobDetail.metadata.labels).length === 0">-</span>
                    </el-descriptions-item>
                  </el-descriptions>
                </el-tab-pane>

                <el-tab-pane :label="t('job.runningPolicy')" name="jobPolicy">
                  <el-descriptions border column="2" size="small">
                    <el-descriptions-item label="Completions">{{ jobDetail.spec?.completions ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="Parallelism">{{ jobDetail.spec?.parallelism ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="CompletionMode">{{ jobDetail.spec?.completionMode || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="BackoffLimit">{{ jobDetail.spec?.backoffLimit ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('common.suspend')">
                      <el-tag :type="jobDetail.spec?.suspend ? 'warning' : 'success'" size="small">
                        {{ jobDetail.spec?.suspend ? t('common.yes') : t('common.no') }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item :label="t('job.ttl')">{{ jobDetail.spec?.ttlSecondsAfterFinished ?? '-' }}</el-descriptions-item>
                  </el-descriptions>
                  <div v-if="jobDetail.spec?.selector?.matchLabels" class="mt-4">
                    <h4 class="mb-2">Selector</h4>
                    <el-tag v-for="(value, key) in jobDetail.spec.selector.matchLabels" :key="key" size="small" class="mr-1 mb-1">
                      {{ key }}={{ value }}
                    </el-tag>
                  </div>
                </el-tab-pane>

                <el-tab-pane :label="t('common.podTemplate')" name="jobPodTemplate">
                  <template v-if="jobDetail.spec?.template?.spec?.containers?.[0]">
                    <h4 class="mb-2">{{ t('job.containerConfig') }}</h4>
                    <el-descriptions border column="2" size="small">
                      <el-descriptions-item :label="t('job.containerName')">{{ jobDetail.spec.template.spec.containers[0].name || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.image')">{{ jobDetail.spec.template.spec.containers[0].image || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('pod.imagePullPolicy')">{{ jobDetail.spec.template.spec.containers[0].imagePullPolicy || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('pod.restartPolicy')">{{ jobDetail.spec.template.spec.restartPolicy || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.cpuLimit')">{{ jobDetail.spec.template.spec.containers[0].resources?.limits?.cpu || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.memoryLimit')">{{ jobDetail.spec.template.spec.containers[0].resources?.limits?.memory || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.cpuRequest')">{{ jobDetail.spec.template.spec.containers[0].resources?.requests?.cpu || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.memoryRequest')">{{ jobDetail.spec.template.spec.containers[0].resources?.requests?.memory || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.dnsPolicy')">{{ jobDetail.spec.template.spec.dnsPolicy || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.scheduler')">{{ jobDetail.spec.template.spec.dashboardName || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="Hostname">{{ jobDetail.spec.template.spec.hostname || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="Subdomain">{{ jobDetail.spec.template.spec.subdomain || '-' }}</el-descriptions-item>
                      <el-descriptions-item :label="t('job.terminationGrace')">{{ jobDetail.spec.template.spec.terminationGracePeriodSeconds ?? '-' }}</el-descriptions-item>
                    </el-descriptions>

                    <div v-if="jobDetail.spec.template.spec.containers[0].command?.length > 0" class="mt-4">
                      <h4 class="mb-2">{{ t('job.startCommand') }}</h4>
                      <div class="command-display">
                        <code>{{ jobDetail.spec.template.spec.containers[0].command.join(' ') }}</code>
                      </div>
                    </div>

                    <div v-if="jobDetail.spec.template.spec.containers[0].env?.length > 0" class="mt-4">
                      <h4 class="mb-2">{{ t('job.envVars') }}</h4>
                      <el-table :data="jobDetail.spec.template.spec.containers[0].env" size="small" border stripe max-height="250">
                        <el-table-column prop="name" :label="t('common.name')" />
                        <el-table-column prop="value" :label="t('common.value')">
                          <template #default="scope">
                            {{ scope.row.value || t('common.notSet') }}
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>

                    <div v-if="jobDetail.spec.template.spec.containers[0].volumeMounts?.length > 0" class="mt-4">
                      <h4 class="mb-2">{{ t('pod.volumeMounts') }}</h4>
                      <el-table :data="jobDetail.spec.template.spec.containers[0].volumeMounts" size="small" border stripe>
                        <el-table-column prop="name" :label="t('job.volumeName')" />
                        <el-table-column prop="mountPath" :label="t('job.mountPath')" />
                      </el-table>
                    </div>
                  </template>
                  <el-empty v-else :description="t('job.noPodTemplate')" />
                </el-tab-pane>

                <el-tab-pane :label="t('common.volumes')" name="jobVolumes">
                  <div v-if="jobDetail.spec?.template?.spec?.volumes?.length > 0">
                    <div v-for="(vol, idx) in jobDetail.spec.template.spec.volumes" :key="idx" class="mb-4">
                      <h4 class="mb-2">{{ vol.name }}</h4>
                      <el-descriptions border column="2" size="small">
                        <el-descriptions-item v-if="vol.secret" :label="t('common.type')">
                          <el-tag type="warning" size="small">Secret</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item v-if="vol.secret" :label="t('job.secretName')">{{ vol.secret.secretName }}</el-descriptions-item>
                        <el-descriptions-item v-if="vol.secret" :label="t('job.defaultMode')">{{ vol.secret.defaultMode ?? '-' }}</el-descriptions-item>
                        <el-descriptions-item v-if="vol.secret?.items" label="Items" span="2">
                          <div v-for="(item, i) in vol.secret.items" :key="i" class="mb-1">
                            <el-tag size="small">{{ item.key }} → {{ item.path }}</el-tag>
                          </div>
                        </el-descriptions-item>
                        <el-descriptions-item v-if="vol.configMap" :label="t('common.type')">
                          <el-tag type="info" size="small">ConfigMap</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item v-if="vol.configMap" :label="t('job.configmapName')">{{ vol.configMap.name }}</el-descriptions-item>
                        <el-descriptions-item v-if="vol.configMap" :label="t('job.defaultMode')">{{ vol.configMap.defaultMode ?? '-' }}</el-descriptions-item>
                        <el-descriptions-item v-if="vol.configMap?.items" label="Items" span="2">
                          <div v-for="(item, i) in vol.configMap.items" :key="i" class="mb-1">
                            <el-tag size="small">{{ item.key }} → {{ item.path }} (mode: {{ item.mode ?? '-' }})</el-tag>
                          </div>
                        </el-descriptions-item>
                      </el-descriptions>
                    </div>
                  </div>
                  <el-empty v-else :description="t('job.noVolume')" />
                </el-tab-pane>

                <el-tab-pane :label="t('common.status')" name="jobStatus">
                  <el-descriptions border column="2" size="small">
                    <el-descriptions-item label="Active">{{ jobDetail.status?.active ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="Ready">{{ jobDetail.status?.ready ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="Succeeded">{{ jobDetail.status?.succeeded ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="Failed">{{ jobDetail.status?.failed ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item :label="t('job.startedAt')">{{ formatTimestamp(jobDetail.status?.startTime) }}</el-descriptions-item>
                    <el-descriptions-item :label="t('job.completedAt')">{{ formatTimestamp(jobDetail.status?.completionTime) }}</el-descriptions-item>
                  </el-descriptions>

                  <div v-if="jobDetail.status?.conditions?.length > 0" class="mt-4">
                    <h4 class="mb-2">{{ t('job.statusHistory') }}</h4>
                    <el-timeline>
                      <el-timeline-item
                        v-for="(condition, index) in jobDetail.status.conditions"
                        :key="index"
                        :type="condition.type === 'Complete' ? 'success' : condition.type === 'Failed' ? 'danger' : 'primary'"
                        :timestamp="formatTimestamp(condition.lastTransitionTime || condition.lastProbeTime)"
                      >
                        <h5>{{ condition.type }}</h5>
                        <p>{{ condition.message }}</p>
                        <p v-if="condition.reason" class="text-gray-500 text-sm">{{ t('common.reason') }}: {{ condition.reason }}</p>
                      </el-timeline-item>
                    </el-timeline>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
            <template #footer>
              <el-button @click="showJobDetailDialog = false">{{ t('common.close') }}</el-button>
            </template>
          </el-dialog>

          <!-- Service管理页面 -->
          <div v-if="activeMenu === 'service'" class="service-page">
            <div class="page-header">
              <h3>{{ t('service.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadServiceList" :loading="serviceLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="serviceNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  class="w-60 ml-2"
                  @change="loadServiceList"
                >
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                  <template #prefix v-if="serviceNamespaceFilter">
                    <span>{{ serviceNamespaceFilter === 'all' ? t('common.allNamespaces') : serviceNamespaceFilter }}</span>
                  </template>
                </el-select>
                <el-button type="success" size="small" @click="showCreateResourceDialog = true; currentResourceType = 'service'" class="ml-2">
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('service.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="serviceList.length === 0 && !serviceLoading" class="empty-state">
              <el-empty :description="t('service.empty')">
                <el-button type="primary" @click="loadServiceList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>

            <el-table v-else :data="serviceList" v-loading="serviceLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('common.type')">
                <template #default="scope">
                  <el-tag :type="getServiceTypeTag(scope.row.spec.type)">
                    {{ scope.row.spec.type }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="ClusterIP" prop="spec.clusterIP" />
              <el-table-column :label="t('service.portConfig')">
                <template #default="scope">
                  <div v-for="(port, index) in scope.row.spec.ports" :key="index" class="port-item">
                    <el-tag size="small" class="mr-1 mb-1">
                      {{ port.port }} → {{ port.targetPort }}/{{ port.protocol }}
                      <span v-if="port.nodePort"> ({{ port.nodePort }})</span>
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('Service', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewServiceDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteService(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- PVC存储卷管理页面 -->
          <div v-if="activeMenu === 'pvc'" class="pvc-page">
            <div class="page-header">
              <h3>{{ t('pvc.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadPvcList" :loading="pvcLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="pvcNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  class="w-60 ml-2"
                  @change="loadPvcList"
                >
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                  <template #prefix v-if="pvcNamespaceFilter">
                    <span>{{ pvcNamespaceFilter === 'all' ? t('common.allNamespaces') : pvcNamespaceFilter }}</span>
                  </template>
                </el-select>
                <el-button type="success" size="small" @click="showCreateResourceDialog = true; currentResourceType = 'pvc'" class="ml-2">
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('pvc.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="pvcList.length === 0 && !pvcLoading" class="empty-state">
              <el-empty :description="t('pvc.empty')">
                <el-button type="primary" @click="loadPvcList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>

            <el-table v-else :data="pvcList" v-loading="pvcLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('common.status')">
                <template #default="scope">
                  <el-tag :type="getPvcStatusType(scope.row)">
                    {{ scope.row.status?.phase || 'Pending' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('pvc.storageClass')" prop="spec.storageClassName" />
              <el-table-column :label="t('pvc.capacity')">
                <template #default="scope">
                  {{ scope.row.spec.resources.requests.storage || '-' }}
                </template>
              </el-table-column>
              <el-table-column :label="t('pvc.accessModes')">
                <template #default="scope">
                  <div v-for="(mode, index) in scope.row.spec.accessModes" :key="index" class="mode-item">
                    <el-tag size="small" class="mr-1 mb-1">
                      {{ mode }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('pvc.mountVolume')" prop="spec.volumeName" />
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('PVC', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewPvcDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeletePvc(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Ingress路由管理页面 -->
          <div v-if="activeMenu === 'ingress'" class="ingress-page">
            <div class="page-header">
              <h3>{{ t('ingress.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadIngressList" :loading="ingressLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="ingressNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  class="w-60 ml-2"
                  @change="loadIngressList"
                >
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                  <template #prefix v-if="ingressNamespaceFilter">
                    <span>{{ ingressNamespaceFilter === 'all' ? t('common.allNamespaces') : ingressNamespaceFilter }}</span>
                  </template>
                </el-select>
                <el-button type="success" size="small" @click="showCreateResourceDialog = true; currentResourceType = 'ingress'" class="ml-2">
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('ingress.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="ingressList.length === 0 && !ingressLoading" class="empty-state">
              <el-empty :description="t('ingress.empty')">
                <el-button type="primary" @click="loadIngressList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>

            <el-table v-else :data="ingressList" v-loading="ingressLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('common.status')">
                <template #default="scope">
                  <el-tag :type="getIngressStatusType(scope.row)">
                    {{ getIngressStatusType(scope.row) === 'success' ? t('ingress.normal') : t('ingress.pending') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('ingress.ingressClass')" prop="spec.ingressClassName" />
              <el-table-column :label="t('ingress.address')">
                <template #default="scope">
                  <div v-if="scope.row.status?.loadBalancer?.ingress && scope.row.status.loadBalancer.ingress.length > 0" class="address-item">
                    <span v-for="(addr, index) in scope.row.status.loadBalancer.ingress" :key="index" class="mr-1">
                      {{ addr.ip || addr.hostname }}
                    </span>
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="TLS">
                <template #default="scope">
                  <el-tag v-if="scope.row.spec.tls && scope.row.spec.tls.length > 0" type="success">{{ t('common.enabled') }}</el-tag>
                  <el-tag v-else type="info">{{ t('common.notEnabled') }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('ingress.rules')">
                <template #default="scope">
                  <div v-for="(rule, index) in scope.row.spec.rules" :key="index" class="rule-item mb-1">
                    <span class="text-sm">
                      <strong>{{ rule.host || t('common.default') }}</strong> → {{ t('ingress.paths', { n: rule.http?.paths?.length || 0 }) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('Ingress', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewIngressDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteIngress(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- ConfigMap管理页面 -->
          <div v-if="activeMenu === 'configmap'" class="configmap-page">
            <div class="page-header">
              <h3>{{ t('configmap.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadConfigMapList" :loading="configMapLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="configMapNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  size="small"
                  style="width: 180px"
                  @change="loadConfigMapList"
                >
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                </el-select>
                <el-button
                  type="success"
                  size="small"
                  @click="showCreateResourceDialog = true; currentResourceType = 'configmap'"
                >
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('configmap.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="configMapList.length === 0 && !configMapLoading" class="empty-state">
              <el-empty :description="t('configmap.empty')" />
            </div>

            <el-table v-else :data="configMapList" v-loading="configMapLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" show-overflow-tooltip />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('configmap.dataCount')">
                <template #default="scope">
                  {{ scope.row.data ? Object.keys(scope.row.data).length : 0 }}
                </template>
              </el-table-column>
              <el-table-column label="Data Key" show-overflow-tooltip>
                <template #default="scope">
                  <el-tag
                    v-for="(_, key) in scope.row.data"
                    :key="key"
                    size="small"
                    class="mr-1 mb-1"
                  >{{ key }}</el-tag>
                  <span v-if="!scope.row.data || Object.keys(scope.row.data).length === 0">-</span>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('ConfigMap', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewConfigMapDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteConfigMap(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- Secret管理页面 -->
          <div v-if="activeMenu === 'secret'" class="configmap-page">
            <div class="page-header">
              <h3>{{ t('secret.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadSecretList" :loading="secretLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select
                  v-model="secretNamespaceFilter"
                  :placeholder="t('common.selectNamespace')"
                  size="small"
                  style="width: 180px"
                  @change="loadSecretList"
                >
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                </el-select>
                <el-button
                  type="success"
                  size="small"
                  @click="showCreateResourceDialog = true; currentResourceType = 'secret'"
                >
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('secret.create') }}
                </el-button>
              </div>
            </div>

            <div v-if="secretList.length === 0 && !secretLoading" class="empty-state">
              <el-empty :description="t('secret.empty')" />
            </div>

            <el-table v-else :data="secretList" v-loading="secretLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" show-overflow-tooltip />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('common.type')">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.type || 'Opaque' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('configmap.dataCount')">
                <template #default="scope">
                  {{ scope.row.data ? Object.keys(scope.row.data).length : 0 }}
                </template>
              </el-table-column>
              <el-table-column label="Data Key" show-overflow-tooltip>
                <template #default="scope">
                  <el-tag
                    v-for="(_, key) in scope.row.data"
                    :key="key"
                    size="small"
                    class="mr-1 mb-1"
                  >{{ key }}</el-tag>
                  <span v-if="!scope.row.data || Object.keys(scope.row.data).length === 0">-</span>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.creator')">
                <template #default="scope">
                  <span>{{ getOwnerName('Secret', scope.row.metadata?.namespace, scope.row.metadata?.name) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewSecretDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteSecret(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- CRD管理页面 -->
          <div v-if="activeMenu === 'crd'" class="configmap-page">
            <div class="page-header">
              <h3>Custom Resource Definition</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadCrdList" :loading="crdLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>

            <div v-if="crdList.length === 0 && !crdLoading" class="empty-state">
              <el-empty :description="t('crd.empty')" />
            </div>

            <el-table v-else :data="crdList" v-loading="crdLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" show-overflow-tooltip />
              <el-table-column label="Group" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.spec?.group }}
                </template>
              </el-table-column>
              <el-table-column label="Scope">
                <template #default="scope">
                  <el-tag :type="scope.row.spec?.scope === 'Namespaced' ? 'primary' : 'warning'" size="small">
                    {{ scope.row.spec?.scope }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('crd.availableVersions')">
                <template #default="scope">
                  <el-tag
                    v-for="v in (scope.row.spec?.versions || [])"
                    :key="v.name"
                    size="small"
                    class="mr-1"
                  >{{ v.name }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status.acceptedNames.kind" label="Kind" show-overflow-tooltip />
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" />
              <el-table-column :label="t('common.actions')" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewCrdDetail(scope.row)">{{ t('common.details') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- K8S Event事件页面 -->
          <div v-if="activeMenu === 'event'" class="event-page">
            <div class="page-header">
              <h3>{{ t('event.title') }}</h3>
              <div class="header-actions">
                <el-select
                  v-model="eventNamespaceFilter"
                  :placeholder="t('common.allNamespacesAll')"
                  size="small"
                  clearable
                  style="width: 200px"
                  @change="loadEventList"
                >
                  <el-option :label="t('common.allNamespacesAll')" value="all" />
                  <el-option
                    v-for="ns in namespaceList"
                    :key="ns.name"
                    :label="ns.name"
                    :value="ns.name"
                  />
                </el-select>
                <el-button type="primary" size="small" @click="loadEventList" :loading="eventLoading">
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>
            <div class="event-container">
              <div v-if="eventList.length === 0 && !eventLoading" class="empty-state">
                <el-icon :size="48" color="#c0c4cc"><WarningFilled /></el-icon>
                <p>{{ t('event.empty') }}</p>
              </div>
              <el-table
                v-else
                :data="eventList"
                v-loading="eventLoading"
                border
                stripe
                height="100%"
                size="small"
              >
                <el-table-column prop="type" :label="t('common.type')">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.type === 'Warning' ? 'danger' : 'info'"
                      size="small"
                    >{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" :label="t('common.reason')" show-overflow-tooltip />
                <el-table-column :label="t('common.message')" min-width="300" show-overflow-tooltip>
                  <template #default="scope">
                    <span class="copyable-cell" @click="copyCellText(scope.row.message)" :title="t('common.clickToCopy')">
                      {{ scope.row.message }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="metadata.namespace" :label="t('common.namespace')" show-overflow-tooltip />
                <el-table-column :label="t('event.resource')" show-overflow-tooltip>
                  <template #default="scope">
                    {{ scope.row.involvedObject?.kind }}/{{ scope.row.involvedObject?.name }}
                  </template>
                </el-table-column>
                <el-table-column prop="count" :label="t('event.count')" />
                <el-table-column :label="t('event.lastSeen')">
                  <template #default="scope">
                    {{ formatTimestamp(scope.row.lastTimestamp || scope.row.eventTime?.time) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- Node节点管理页面 -->
          <div v-if="activeMenu === 'node'" class="node-page">
            <div class="page-header">
              <h3>{{ t('node.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadNodeList" :loading="nodeLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>

            <div v-if="nodeList.length === 0 && !nodeLoading" class="empty-state">
              <el-empty :description="t('node.empty')">
                <el-button type="primary" @click="loadNodeList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="nodeList" v-loading="nodeLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="160" />
              <el-table-column :label="t('node.status')" width="110" align="center">
                <template #default="scope">
                  <el-tag :type="getNodeStatusType(scope.row)">{{ getNodeStatus(scope.row) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('node.roles')" min-width="130" show-overflow-tooltip>
                <template #default="scope">{{ getNodeRoles(scope.row) || '—' }}</template>
              </el-table-column>
              <el-table-column label="IP" width="140">
                <template #default="scope">{{ getNodeInternalIP(scope.row) || '—' }}</template>
              </el-table-column>
              <el-table-column label="Kubelet" width="110">
                <template #default="scope">{{ scope.row.status?.nodeInfo?.kubeletVersion || '—' }}</template>
              </el-table-column>
              <el-table-column label="OS" min-width="130" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.status?.nodeInfo?.osImage || '—' }}</template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="90" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewNodeDetail(scope.row)">{{ t('common.details') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- PersistentVolume 存储卷管理页面 -->
          <div v-if="activeMenu === 'pv'" class="node-page">
            <div class="page-header">
              <h3>{{ t('pv.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadPvList" :loading="pvLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>
            <div v-if="pvList.length === 0 && !pvLoading" class="empty-state">
              <el-empty :description="t('pv.empty')">
                <el-button type="primary" @click="loadPvList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="pvList" v-loading="pvLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="180" show-overflow-tooltip />
              <el-table-column prop="spec.capacity?.storage" :label="t('pv.capacity')" width="110" />
              <el-table-column :label="t('pv.accessModes')" min-width="140" show-overflow-tooltip>
                <template #default="scope">{{ (scope.row.spec?.accessModes || []).join(', ') || '—' }}</template>
              </el-table-column>
              <el-table-column prop="spec.persistentVolumeReclaimPolicy" :label="t('pv.reclaimPolicy')" width="130" />
              <el-table-column :label="t('pv.status')" width="110" align="center">
                <template #default="scope">
                  <el-tag :type="getPvStatusType(scope.row.status?.phase)">{{ scope.row.status?.phase || '—' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('pv.boundClaim')" min-width="160" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.spec?.claimRef ? `${scope.row.spec.claimRef.namespace}/${scope.row.spec.claimRef.name}` : '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="spec.storageClassName" :label="t('pv.storageClass')" width="140" />
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="90" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewPvDetail(scope.row)">{{ t('common.details') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- StorageClass 存储类管理页面 -->
          <div v-if="activeMenu === 'storageclass'" class="node-page">
            <div class="page-header">
              <h3>{{ t('sc.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadScList" :loading="scLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>
            <div v-if="scList.length === 0 && !scLoading" class="empty-state">
              <el-empty :description="t('sc.empty')">
                <el-button type="primary" @click="loadScList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="scList" v-loading="scLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="150" show-overflow-tooltip />
              <el-table-column prop="provisioner" :label="t('sc.provisioner')" min-width="160" show-overflow-tooltip />
              <el-table-column prop="reclaimPolicy" :label="t('sc.reclaimPolicy')" width="130" />
              <el-table-column prop="volumeBindingMode" :label="t('sc.bindingMode')" min-width="160" show-overflow-tooltip />
              <el-table-column :label="t('sc.default')" width="100" align="center">
                <template #default="scope">
                  <el-tag v-if="isDefaultStorageClass(scope.row)" type="warning">{{ t('sc.yesDefault') }}</el-tag>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="90" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewScDetail(scope.row)">{{ t('common.details') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- HPA自动扩缩容管理页面 -->
          <div v-if="activeMenu === 'hpa'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('hpa.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadHpaList" :loading="hpaLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select v-model="hpaNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadHpaList">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>
            <div v-if="hpaList.length === 0 && !hpaLoading" class="empty-state">
              <el-empty :description="t('hpa.empty')">
                <el-button type="primary" @click="loadHpaList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="hpaList" v-loading="hpaLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="140" />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('hpa.target')" min-width="170" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.spec?.scaleTargetRef ? `${scope.row.spec.scaleTargetRef.kind}/${scope.row.spec.scaleTargetRef.name}` : '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="spec.minReplicas" :label="t('hpa.minReplicas')" width="100" align="center">
                <template #default="scope">{{ scope.row.spec?.minReplicas ?? '—' }}</template>
              </el-table-column>
              <el-table-column prop="spec.maxReplicas" :label="t('hpa.maxReplicas')" width="100" align="center" />
              <el-table-column :label="t('hpa.currentReplicas')" width="110" align="center">
                <template #default="scope">
                  <el-tag :type="getHpaStatusType(scope.row)">{{ scope.row.status?.currentReplicas ?? 0 }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="150" align="center" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button size="small" @click="viewHpaDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="primary" size="small" @click="handleHpaMinMax(scope.row)">{{ t('hpa.adjust') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- NetworkPolicy 网络策略管理页面 -->
          <div v-if="activeMenu === 'networkpolicy'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('np.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadNpList" :loading="npLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select v-model="npNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadNpList">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>
            <div v-if="npList.length === 0 && !npLoading" class="empty-state">
              <el-empty :description="t('np.empty')">
                <el-button type="primary" @click="loadNpList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="npList" v-loading="npLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="160" show-overflow-tooltip />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('np.podSelector')" min-width="140" show-overflow-tooltip>
                <template #default="scope">{{ getNpPodSelector(scope.row) }}</template>
              </el-table-column>
              <el-table-column :label="t('np.ingressRules')" width="90" align="center">
                <template #default="scope">{{ scope.row.spec?.ingress?.length ?? 0 }}</template>
              </el-table-column>
              <el-table-column :label="t('np.egressRules')" width="90" align="center">
                <template #default="scope">{{ scope.row.spec?.egress?.length ?? 0 }}</template>
              </el-table-column>
              <el-table-column :label="t('np.policyTypes')" min-width="130">
                <template #default="scope">
                  <el-tag v-for="pt in scope.row.spec?.policyTypes || []" :key="pt" size="small" class="mr-1">{{ pt }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="90" align="center">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewNpDetail(scope.row)">{{ t('common.details') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- RBAC 权限管理页面 -->
          <div v-if="activeMenu === 'rbac'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('rbac.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadRbacCurrent" :loading="rbacLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-select v-model="rbacNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadRbacCurrent">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>
            <el-tabs v-model="rbacActiveTab" @tab-change="loadRbacCurrent">
              <el-tab-pane :label="t('rbac.serviceaccount')" name="serviceaccount" />
              <el-tab-pane :label="t('rbac.role')" name="role" />
              <el-tab-pane :label="t('rbac.clusterrole')" name="clusterrole" />
              <el-tab-pane :label="t('rbac.rolebinding')" name="rolebinding" />
              <el-tab-pane :label="t('rbac.clusterrolebinding')" name="clusterrolebinding" />
            </el-tabs>

            <div v-if="rbacList.length === 0 && !rbacLoading" class="empty-state">
              <el-empty :description="t('rbac.empty')" />
            </div>
            <el-table v-else :data="rbacList" v-loading="rbacLoading" border stripe>
              <template v-if="rbacActiveTab === 'serviceaccount'">
                <el-table-column prop="metadata.name" :label="t('common.name')" min-width="160" show-overflow-tooltip />
                <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
                <el-table-column :label="t('rbac.secretCount')" width="100" align="center">
                  <template #default="scope">{{ scope.row.secrets?.length ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              </template>
              <template v-else-if="rbacActiveTab === 'role' || rbacActiveTab === 'clusterrole'">
                <el-table-column prop="metadata.name" :label="t('common.name')" min-width="160" show-overflow-tooltip />
                <el-table-column v-if="rbacActiveTab === 'role'" prop="metadata.namespace" :label="t('common.namespace')" />
                <el-table-column :label="t('rbac.ruleCount')" width="100" align="center">
                  <template #default="scope">{{ scope.row.rules?.length ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              </template>
              <template v-else>
                <el-table-column prop="metadata.name" :label="t('common.name')" min-width="170" show-overflow-tooltip />
                <el-table-column v-if="rbacActiveTab === 'rolebinding'" prop="metadata.namespace" :label="t('common.namespace')" />
                <el-table-column :label="t('rbac.roleRef')" min-width="150" show-overflow-tooltip>
                  <template #default="scope">
                    {{ scope.row.roleRef ? `${scope.row.roleRef.kind}/${scope.row.roleRef.name}` : '—' }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('rbac.subjectCount')" width="100" align="center">
                  <template #default="scope">{{ scope.row.subjects?.length ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              </template>
              <el-table-column :label="t('common.actions')" width="150" align="center" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="viewRbacDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteRbac(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- ResourceQuota 资源配额管理页面 -->
          <div v-if="activeMenu === 'quota'" class="deployment-page">
            <div class="page-header">
              <h3>{{ t('quota.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="loadQuotaList" :loading="quotaLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-button type="success" size="small" @click="handleCreateQuota">
                  <el-icon class="mr-1"><Plus /></el-icon>
                  {{ t('common.create') }}
                </el-button>
                <el-select v-model="quotaNamespaceFilter" :placeholder="t('common.selectNamespace')" class="w-60 ml-2" @change="loadQuotaList">
                  <el-option :label="t('common.allNamespaces')" value="all" />
                  <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
                </el-select>
              </div>
            </div>
            <div v-if="quotaList.length === 0 && !quotaLoading" class="empty-state">
              <el-empty :description="t('quota.empty')">
                <el-button type="primary" @click="loadQuotaList">{{ t('common.refreshData') }}</el-button>
              </el-empty>
            </div>
            <el-table v-else :data="quotaList" v-loading="quotaLoading" border stripe>
              <el-table-column prop="metadata.name" :label="t('common.name')" min-width="160" show-overflow-tooltip />
              <el-table-column prop="metadata.namespace" :label="t('common.namespace')" />
              <el-table-column :label="t('quota.hardCount')" width="100" align="center">
                <template #default="scope">{{ Object.keys(scope.row.spec?.hard || {}).length }}</template>
              </el-table-column>
              <el-table-column :label="t('quota.cpuUsage')" min-width="140" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.status?.hard?.['requests.cpu']">
                    {{ scope.row.status?.used?.['requests.cpu'] || 0 }} / {{ scope.row.status?.hard?.['requests.cpu'] }}
                  </span>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column :label="t('quota.memoryUsage')" min-width="140" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.status?.hard?.['requests.memory']">
                    {{ scope.row.status?.used?.['requests.memory'] || 0 }} / {{ scope.row.status?.hard?.['requests.memory'] }}
                  </span>
                  <span v-else>—</span>
                </template>
              </el-table-column>
              <el-table-column prop="metadata.creationTimestamp" :label="t('common.createdAt')" width="170" />
              <el-table-column :label="t('common.actions')" width="180" align="center" class-name="sticky-right-col">
                <template #default="scope">
                  <el-button size="small" @click="viewQuotaDetail(scope.row)">{{ t('common.details') }}</el-button>
                  <el-button type="primary" size="small" @click="handleEditQuota(scope.row)">{{ t('common.edit') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteQuota(scope.row)" v-if="scope.row.metadata?.labels?.['dssp.io/managed-by'] === 'dssp' && scope.row.metadata?.labels?.['dssp.io/created-by'] === String(userInfo.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 对象存储OSS管理页面 -->
          <div v-if="activeMenu === 'oss'" class="deployment-page oss-page">
            <div class="page-header">
              <h3>{{ t('oss.title') }}</h3>
              <div class="header-actions">
                <el-button size="small" :disabled="!selectedBucket" @click="loadFileList(selectedBucket)">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
                <el-button type="success" size="small" :disabled="!selectedBucket" @click="triggerUpload">
                  <el-icon class="mr-1"><Upload /></el-icon>
                  {{ t('oss.upload') }}
                </el-button>
                <el-button size="small" :disabled="!selectedBucket" @click="showCreateFolderDialog = true">{{ t('oss.createFolder') }}</el-button>
                <el-button type="danger" size="small" :disabled="selectedOssFiles.length === 0" @click="handleBatchDelete">{{ t('oss.deleteSelected') }}</el-button>
              </div>
            </div>

            <div class="oss-layout">
              <!-- 左侧Bucket列表 -->
              <aside class="oss-bucket-panel">
                <div class="oss-bucket-panel-header">
                  <span>{{ t('oss.buckets') }}</span>
                  <span>
                    <el-button size="small" link @click="loadBucketList"><el-icon><Refresh /></el-icon></el-button>
                    <el-button size="small" link type="primary" @click="showCreateBucketDialog = true"><el-icon><Plus /></el-icon></el-button>
                  </span>
                </div>
                <div v-if="bucketList.length === 0" class="oss-bucket-empty">{{ t('oss.empty') }}</div>
                <div v-for="b in bucketList" :key="b" class="oss-bucket-item" :class="{ active: b === selectedBucket }" @click="selectBucket(b)">
                  <el-icon><Box /></el-icon>
                  <span class="oss-bucket-name">{{ b }}</span>
                </div>
              </aside>

              <!-- 右侧文件浏览区 -->
              <div class="oss-main" :class="{ 'is-dragover': ossDragover }"
                   @dragover.prevent="selectedBucket && (ossDragover = true)"
                   @dragleave="ossDragover = false"
                   @drop.prevent="handleOssDrop">
                <div class="oss-main-toolbar">
                  <el-breadcrumb v-if="selectedBucket" separator="/" class="oss-breadcrumb">
                    <el-breadcrumb-item><el-link type="primary" @click="navigateToFolder('')">{{ t('oss.root') }}</el-link></el-breadcrumb-item>
                    <el-breadcrumb-item v-for="(p, i) in breadcrumbParts" :key="i">
                      <el-link type="primary" @click="navigateToBreadcrumb(i)">{{ p }}</el-link>
                    </el-breadcrumb-item>
                  </el-breadcrumb>
                  <el-input v-model="ossSearchKey" :placeholder="t('oss.searchPlaceholder')" size="small" clearable style="width: 200px; margin-left: auto;">
                    <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                </div>

                <div v-if="!selectedBucket" class="empty-state">
                  <el-empty :description="t('oss.selectBucketFirst')" />
                </div>
                <template v-else>
                  <div v-if="ossFilteredFiles.length === 0 && !ossLoading" class="empty-state">
                    <el-empty :description="ossSearchKey ? t('oss.noResult') : t('oss.empty')" />
                  </div>
                  <el-table v-else ref="ossTableRef" :data="ossFilteredFiles" v-loading="ossLoading" border stripe @selection-change="handleOssSelectionChange">
                    <el-table-column type="selection" width="45" align="center" />
                    <el-table-column :label="t('common.name')" min-width="220" sortable prop="name">
                      <template #default="scope">
                        <el-link v-if="scope.row.folder" type="primary" @click="navigateToFolder(scope.row.name)">
                          <el-icon class="mr-1"><FolderOpened /></el-icon>{{ trimFolderName(scope.row.name) }}
                        </el-link>
                        <span v-else>
                          <el-icon class="mr-1"><Document /></el-icon>{{ scope.row.name }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="t('oss.size')" prop="size" sortable width="120">
                      <template #default="scope">
                        {{ scope.row.folder ? '—' : formatFileSize(scope.row.size || 0) }}
                      </template>
                    </el-table-column>
                    <el-table-column :label="t('oss.type')" width="95" align="center">
                      <template #default="scope">
                        <el-tag size="small" :type="scope.row.folder ? 'warning' : 'info'">{{ scope.row.folder ? t('oss.folder') : t('oss.file') }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column :label="t('common.actions')" width="300" align="center" class-name="sticky-right-col">
                      <template #default="scope">
                        <template v-if="!scope.row.folder">
                          <el-button v-if="isImageFile(scope.row.name)" type="primary" size="small" @click="previewImage(scope.row.downloadUrl, scope.row.name)">{{ t('oss.preview') }}</el-button>
                          <el-button v-else-if="isTextFile(scope.row.name)" type="primary" size="small" @click="previewTextFile(scope.row.name)">{{ t('oss.preview') }}</el-button>
                          <el-button size="small" @click="openDownloadUrl(scope.row.downloadUrl)">{{ t('oss.download') }}</el-button>
                          <el-button size="small" @click="copyDownloadUrl(scope.row.downloadUrl)">{{ t('oss.copy') }}</el-button>
                        </template>
                        <el-button type="danger" size="small" @click="handleDeleteObject(scope.row.name, scope.row.folder)">{{ t('common.delete') }}</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </template>
              </div>
            </div>
          </div>

          <!-- 镜像仓库管理页面 -->
          <div v-if="activeMenu === 'registry'" class="deployment-page oss-page">
            <div class="page-header">
              <h3>{{ t('registry.title') }}</h3>
              <div class="header-actions">
                <el-button size="small" @click="loadRegistryRepos" :loading="registryLoading">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  {{ t('common.refresh') }}
                </el-button>
              </div>
            </div>

            <div class="oss-layout">
              <!-- 左侧仓库列表 -->
              <aside class="oss-bucket-panel">
                <div class="oss-bucket-panel-header">
                  <span>{{ t('registry.repos') }}</span>
                </div>
                <el-input v-model="registrySearchKey" :placeholder="t('registry.searchPlaceholder')" size="small" clearable style="margin: 8px 10px; width: calc(100% - 20px);">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <div v-if="filteredRegistryRepos.length === 0 && !registryLoading" class="oss-bucket-empty">{{ t('oss.noResult') }}</div>
                <div v-for="repo in filteredRegistryRepos" :key="repo" class="oss-bucket-item" :class="{ active: repo === selectedRepo }" @click="selectRepo(repo)">
                  <el-icon><Box /></el-icon>
                  <span class="oss-bucket-name">{{ repo }}</span>
                </div>
              </aside>

              <!-- 右侧Tags区域 -->
              <div class="oss-main">
                <div v-if="!selectedRepo" class="empty-state">
                  <el-empty :description="t('registry.selectRepoFirst')" />
                </div>
                <template v-else>
                  <div class="registry-repo-header">
                    <el-tag size="large" effect="plain"><el-icon class="mr-1"><Box /></el-icon>{{ selectedRepo }}</el-tag>
                    <span v-if="currentRepoData?.registryHost" class="registry-host">{{ t('registry.host') }}: {{ currentRepoData.registryHost }}</span>
                    <span class="registry-tag-count">{{ t('registry.tags') }}: {{ currentRepoData?.tagCount ?? 0 }}</span>
                  </div>

                  <div v-if="!(currentRepoData?.tags || []).length && !registryLoading" class="empty-state">
                    <el-empty :description="t('registry.empty')" />
                  </div>
                  <el-table v-else :data="currentRepoData?.tags || []" v-loading="registryLoading" border stripe>
                    <el-table-column :label="t('registry.tag')" min-width="180">
                      <template #default="scope">
                        <el-link type="primary" @click="selectTag(scope.row)">{{ scope.row }}</el-link>
                      </template>
                    </el-table-column>
                    <el-table-column :label="t('registry.imagePath')" min-width="260" show-overflow-tooltip>
                      <template #default="scope">
                        {{ (currentRepoData?.registryHost || '') + '/' + selectedRepo + ':' + scope.row }}
                      </template>
                    </el-table-column>
                    <el-table-column :label="t('common.actions')" width="240" align="center" class-name="sticky-right-col">
                      <template #default="scope">
                        <el-button type="primary" size="small" @click="selectTag(scope.row)">{{ t('registry.manifest') }}</el-button>
                        <el-button size="small" @click="copyImagePath(scope.row)">{{ t('oss.copy') }}</el-button>
                        <el-button type="danger" size="small" @click="handleDeleteTag(scope.row)">{{ t('common.delete') }}</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </template>
              </div>
            </div>
          </div>

          <!-- 集群配置页面 -->
          <div v-if="activeMenu === 'cluster'" class="cluster-page">
            <div class="page-header">
              <h3>{{ t('cluster.title') }}</h3>
              <div class="header-actions">
                <el-button type="primary" size="small" @click="openClusterUploadDialog">
                  <el-icon><Upload /></el-icon> {{ t('cluster.upload') }}
                </el-button>
                <el-button size="small" @click="loadClusterList" :loading="clusterLoading">
                  <el-icon><Refresh /></el-icon> {{ t('common.refreshData') }}
                </el-button>
              </div>
            </div>
            <div class="cluster-tip">
              <el-alert :title="t('cluster.tip')" type="info" :closable="false" show-icon />
            </div>
            <el-table :data="clusterList" v-loading="clusterLoading" border stripe>
              <el-table-column prop="clusterName" :label="t('cluster.name')" min-width="150">
                <template #default="scope">
                  <el-tag v-if="scope.row.active === 1" type="success" size="small" style="margin-right: 6px;">
                    {{ t('cluster.active') }}
                  </el-tag>
                  {{ scope.row.clusterName }}
                </template>
              </el-table-column>
              <el-table-column prop="server" :label="t('cluster.server')" min-width="200" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.server || '—' }}</template>
              </el-table-column>
              <el-table-column prop="remark" :label="t('cluster.remark')" min-width="140" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.remark || '—' }}</template>
              </el-table-column>
              <el-table-column prop="creatorName" :label="t('cluster.uploader')" width="100" align="center">
                <template #default="scope">{{ scope.row.creatorName || scope.row.creatorId }}</template>
              </el-table-column>
              <el-table-column prop="createdTime" :label="t('cluster.uploadTime')" width="170">
                <template #default="scope">{{ formatTimestamp(scope.row.createdTime) }}</template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="250" align="center">
                <template #default="scope">
                  <el-button
                    size="small"
                    :loading="clusterTestLoadingId === scope.row.id"
                    @click="handleClusterTest(scope.row)"
                  >{{ t('cluster.test') }}</el-button>
                  <el-button
                    v-if="scope.row.active !== 1"
                    type="primary" size="small"
                    @click="handleActivateCluster(scope.row)"
                  >{{ t('cluster.activate') }}</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteCluster(scope.row)">
                    {{ t('common.delete') }}
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty :description="t('cluster.empty')" />
              </template>
            </el-table>
          </div>

        </div>
      </main>

    <!-- OSS隐藏文件选择input -->
    <input type="file" ref="fileInputRef" style="display: none;" @change="handleFileUpload" />

    <!-- OSS新建文件夹弹窗 -->
    <el-dialog v-model="showCreateFolderDialog" :title="t('oss.createFolder')" width="420px">
      <el-input v-model="newFolderName" :placeholder="t('oss.folderNameRequired')" @keyup.enter="handleCreateFolder" />
      <template #footer>
        <el-button @click="showCreateFolderDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="creatingFolder" @click="handleCreateFolder">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- OSS创建Bucket弹窗 -->
    <el-dialog v-model="showCreateBucketDialog" :title="t('oss.createBucket')" width="420px">
      <el-input v-model="newBucketName" :placeholder="t('oss.bucketNameRequired')" @keyup.enter="handleCreateBucket" />
      <template #footer>
        <el-button @click="showCreateBucketDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="creatingBucket" @click="handleCreateBucket">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- OSS图片预览弹窗 -->
    <el-dialog v-model="showPreviewDialog" :title="previewImageName" width="60%">
      <img v-if="previewImageUrl" :src="previewImageUrl" style="width: 100%;" :alt="previewImageName" />
    </el-dialog>

    <!-- OSS文本预览弹窗 -->
    <el-dialog v-model="showTextPreviewDialog" :title="previewTextName" width="700px">
      <pre style="max-height: 60vh; overflow: auto; background: #f5f7fa; padding: 12px; white-space: pre-wrap; word-break: break-all;">{{ previewTextContent }}</pre>
      <template #footer>
        <el-button @click="showTextPreviewDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- OSS上传进度浮窗 -->
    <div v-if="showUploadDialog" class="oss-upload-float" :style="{ top: floatTop + 'px' }">
      <div class="ondrag-title" @mousedown="startDrag" @touchstart="startDrag">
        <el-icon class="mr-1"><Upload /></el-icon>
        <span>{{ t('oss.uploading') }}</span>
        <el-button size="small" link style="margin-left: 8px;" @click="uploadMinimized = true">
          <el-icon><ArrowDown /></el-icon>
        </el-button>
      </div>
      <div class="mt-2 px-2" style="font-size: 12px;">{{ uploadingFileName }}</div>
      <el-progress :percentage="uploadProgress" class="mt-1" style="margin: 0 12px 12px;" />
    </div>
    <div v-else-if="uploadingFile" class="oss-upload-float oss-upload-minimized" :style="{ top: floatTop + 'px' }" @click="restoreUploadDialog">
      <el-progress type="circle" :width="44" :percentage="uploadProgress" />
    </div>

    <!-- 镜像Manifest详情弹窗 -->
    <el-dialog v-model="showManifestDialog" :title="selectedTag ? `${selectedRepo}:${selectedTag} ${t('registry.manifest')}` : t('registry.manifest')" width="700px">
      <div v-if="registryManifest">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('registry.digest')" :span="2">
            <span class="manifest-digest">{{ registryManifest.digest }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('registry.mediaType')" :span="2">{{ registryManifest.mediaType || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('oss.size')">{{ formatFileSize(registryManifest.sizeBytes || 0) }}</el-descriptions-item>
          <el-descriptions-item :label="t('registry.layerCount')">{{ (registryManifest.layers || []).length }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="(registryManifest.layers || []).length" class="mt-4">
          <strong>{{ t('registry.layers') }}</strong>
          <el-table :data="(registryManifest.layers || []).map((l, i) => ({ index: i + 1, digest: l }))" border size="small" class="mt-2">
            <el-table-column prop="index" label="#" width="60" align="center" />
            <el-table-column prop="digest" label="Digest" min-width="260" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="showManifestDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- 上传集群配置弹窗 -->
    <el-dialog v-model="showClusterUploadDialog" :title="t('cluster.uploadDialog')" width="520px">
      <el-form :model="clusterForm" label-width="90px">
        <el-form-item :label="t('cluster.name')" required>
          <el-input v-model="clusterForm.clusterName" :placeholder="t('cluster.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('cluster.remark')">
          <el-input v-model="clusterForm.remark" :placeholder="t('cluster.remarkPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('cluster.file')" required>
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept=".yaml,.yml,.txt,.conf"
            :on-change="onClusterFileChange"
            :on-remove="() => clusterForm.file = null"
            drag
          >
            <el-icon size="36"><UploadFilled /></el-icon>
            <div class="el-upload__text">{{ t('cluster.dropOrClick') }}</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showClusterUploadDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="clusterUploading" @click="handleClusterUpload">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
    </div>

    <!-- 新建命名空间弹窗 -->
    <el-dialog v-model="showCreateNamespaceDialog" :title="t('namespace.createDialog')" width="500px">
      <el-form :model="namespaceForm" label-width="80px">
        <el-form-item :label="t('common.name')" prop="name">
          <el-input v-model="namespaceForm.name" :placeholder="t('namespace.namePlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateNamespaceDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateNamespace" :loading="createLoading">{{ t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- 新建资源弹窗 -->
    <el-dialog v-model="showCreateResourceDialog" :title="t('createResource.title')" width="800px">
      <el-tabs v-model="currentResourceType" type="border-card">
        <el-tab-pane label="Deployment" name="deployment">
          <el-form :model="deploymentForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="deploymentForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="deploymentForm.name" :placeholder="t('createResource.deploymentNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('job.image')" required>
              <el-input v-model="deploymentForm.image" :placeholder="t('createResource.imageNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('createResource.replicas')" required>
              <el-input-number v-model="deploymentForm.replicas" :min="1" :max="100" />
            </el-form-item>
            <el-form-item :label="t('createResource.containerPort')">
              <el-input v-model="deploymentForm.containerPort" :placeholder="t('createResource.containerPortPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('pod.resourceLimits')">
              <el-row :gutter="10">
                <el-col span="12">
                  <el-input v-model="deploymentForm.cpuLimit" :placeholder="t('createResource.cpuLimitPlaceholder')" />
                </el-col>
                <el-col span="12">
                  <el-input v-model="deploymentForm.memoryLimit" :placeholder="t('createResource.memoryLimitPlaceholder')" />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="t('createResource.envVars')">
              <el-input type="textarea" v-model="deploymentForm.env" :placeholder="t('createResource.envPlaceholder')" :rows="3" />
            </el-form-item>
            <el-form-item :label="t('createResource.volumeMapping')">
              <div class="volume-mount-list">
                <div v-for="(vm, index) in deploymentForm.volumeMounts" :key="index" class="volume-mount-row mb-2">
                  <el-select v-model="vm.type" :placeholder="t('createResource.volumeType')" size="small" style="width: 110px" @change="onVolTypeChange(vm)">
                    <el-option label="ConfigMap" value="configMap" />
                    <el-option label="Secret" value="secret" />
                    <el-option label="PVC" value="pvc" />
                    <el-option label="HostPath" value="hostPath" />
                  </el-select>
                  <el-select v-if="vm.type === 'configMap'" v-model="vm.name" :placeholder="t('createResource.selectConfigMap')" size="small" style="width: 200px" filterable>
                    <el-option v-for="cm in volConfigMaps" :key="cm" :label="cm" :value="cm" />
                  </el-select>
                  <el-select v-else-if="vm.type === 'secret'" v-model="vm.name" :placeholder="t('createResource.selectSecret')" size="small" style="width: 200px" filterable>
                    <el-option v-for="s in volSecrets" :key="s" :label="s" :value="s" />
                  </el-select>
                  <el-select v-else-if="vm.type === 'pvc'" v-model="vm.name" :placeholder="t('createResource.selectPVC')" size="small" style="width: 200px" filterable>
                    <el-option v-for="p in volPvcs" :key="p" :label="p" :value="p" />
                  </el-select>
                  <el-input v-else v-model="vm.name" :placeholder="t('createResource.hostPathPlaceholder')" size="small" style="width: 200px" />
                  <el-input v-model="vm.mountPath" :placeholder="t('createResource.containerPathPlaceholder')" size="small" style="width: 200px" />
                  <el-button @click="removeDeployVolumeMount(index)" type="danger" size="small" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <el-button @click="addDeployVolumeMount" type="primary" size="small" plain :loading="loadingVolResources">
                <el-icon><Plus /></el-icon> {{ t('createResource.addVolumeMapping') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Job" name="job">
          <el-form :model="jobForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="jobForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="jobForm.name" :placeholder="t('createResource.jobNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('job.image')" required>
              <el-input v-model="jobForm.image" :placeholder="t('createResource.jobImagePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('createResource.completions')">
              <el-input-number v-model="jobForm.completions" :min="1" :max="100" />
            </el-form-item>
            <el-form-item :label="t('createResource.parallelism')">
              <el-input-number v-model="jobForm.parallelism" :min="1" :max="100" />
            </el-form-item>
            <el-form-item :label="t('pod.restartPolicy')">
              <el-select v-model="jobForm.restartPolicy" :placeholder="t('createResource.restartPolicyPlaceholder')">
                <el-option label="Never" value="Never" />
                <el-option label="OnFailure" value="OnFailure" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('createResource.startCommand')">
              <el-input v-model="jobForm.command" :placeholder="t('createResource.startCommandPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('pod.resourceLimits')">
              <el-row :gutter="10">
                <el-col span="12">
                  <el-input v-model="jobForm.cpuLimit" :placeholder="t('createResource.cpuLimitPlaceholder')" />
                </el-col>
                <el-col span="12">
                  <el-input v-model="jobForm.memoryLimit" :placeholder="t('createResource.memoryLimitPlaceholder')" />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="t('createResource.envVars')">
              <el-input type="textarea" v-model="jobForm.env" :placeholder="t('createResource.envPlaceholder')" :rows="3" />
            </el-form-item>
            <el-form-item :label="t('createResource.volumeMapping')">
              <div class="volume-mount-list">
                <div v-for="(vm, index) in jobForm.volumeMounts" :key="index" class="volume-mount-row mb-2">
                  <el-select v-model="vm.type" :placeholder="t('createResource.volumeType')" size="small" style="width: 110px" @change="onVolTypeChange(vm)">
                    <el-option label="ConfigMap" value="configMap" />
                    <el-option label="Secret" value="secret" />
                    <el-option label="PVC" value="pvc" />
                    <el-option label="HostPath" value="hostPath" />
                  </el-select>
                  <el-select v-if="vm.type === 'configMap'" v-model="vm.name" :placeholder="t('createResource.selectConfigMap')" size="small" style="width: 200px" filterable>
                    <el-option v-for="cm in volConfigMaps" :key="cm" :label="cm" :value="cm" />
                  </el-select>
                  <el-select v-else-if="vm.type === 'secret'" v-model="vm.name" :placeholder="t('createResource.selectSecret')" size="small" style="width: 200px" filterable>
                    <el-option v-for="s in volSecrets" :key="s" :label="s" :value="s" />
                  </el-select>
                  <el-select v-else-if="vm.type === 'pvc'" v-model="vm.name" :placeholder="t('createResource.selectPVC')" size="small" style="width: 200px" filterable>
                    <el-option v-for="p in volPvcs" :key="p" :label="p" :value="p" />
                  </el-select>
                  <el-input v-else v-model="vm.name" :placeholder="t('createResource.hostPathPlaceholder')" size="small" style="width: 200px" />
                  <el-input v-model="vm.mountPath" :placeholder="t('createResource.containerPathPlaceholder')" size="small" style="width: 200px" />
                  <el-button @click="removeJobVolumeMount(index)" type="danger" size="small" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              <el-button @click="addJobVolumeMount" type="primary" size="small" plain :loading="loadingVolResources">
                <el-icon><Plus /></el-icon> {{ t('createResource.addVolumeMapping') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Service" name="service">
          <el-form :model="serviceForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="serviceForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="serviceForm.name" :placeholder="t('createResource.serviceNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('common.type')" required>
              <el-select v-model="serviceForm.type" :placeholder="t('createResource.serviceTypePlaceholder')">
                <el-option label="ClusterIP" value="ClusterIP" />
                <el-option label="NodePort" value="NodePort" />
                <el-option label="LoadBalancer" value="LoadBalancer" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('service.portConfig')" required>
              <el-row :gutter="10">
                <el-col span="8">
                  <el-input v-model="serviceForm.port" :placeholder="t('createResource.servicePortPlaceholder')" />
                </el-col>
                <el-col span="8">
                  <el-input v-model="serviceForm.targetPort" :placeholder="t('createResource.containerPortPlaceholder')" />
                </el-col>
                <el-col span="8">
                  <el-input v-model="serviceForm.nodePort" :placeholder="t('createResource.nodePortPlaceholder')" :disabled="serviceForm.type !== 'NodePort'" />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="t('createResource.selector')">
              <el-input type="textarea" v-model="serviceForm.selector" :placeholder="t('createResource.selectorPlaceholder')" :rows="3" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="PVC" name="pvc">
          <el-form :model="pvcForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="pvcForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="pvcForm.name" :placeholder="t('createResource.pvcNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('pvc.storageClass')" required>
              <el-select v-model="pvcForm.storageClass" :placeholder="t('createResource.storageClassPlaceholder')" filterable allow-create style="width: 100%">
                <el-option
                  v-for="sc in storageClassList"
                  :key="sc.metadata?.name"
                  :label="sc.metadata?.name"
                  :value="sc.metadata?.name"
                >
                  <span>{{ sc.metadata?.name }}</span>
                  <span v-if="sc.metadata?.annotations?.['storageclass.kubernetes.io/is-default-class']" class="text-primary ml-2">{{ t('common.defaultParen') }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="t('createResource.storageSize')" required>
              <el-input v-model="pvcForm.storage" :placeholder="t('createResource.storageSizePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('pvc.accessModes')" required>
              <el-select v-model="pvcForm.accessModes" multiple :placeholder="t('createResource.accessModePlaceholder')">
                <el-option label="ReadWriteOnce" value="ReadWriteOnce" />
                <el-option label="ReadOnlyMany" value="ReadOnlyMany" />
                <el-option label="ReadWriteMany" value="ReadWriteMany" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="ConfigMap" name="configmap">
          <el-form :model="configMapForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="configMapForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="configMapForm.name" :placeholder="t('createResource.configmapNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('configmap.dataConfig')" required>
              <el-input
                type="textarea"
                v-model="configMapForm.dataText"
                :placeholder="t('createResource.configmapDataPlaceholder')"
                :rows="6"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Secret" name="secret">
          <el-form :model="secretForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="secretForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="secretForm.name" :placeholder="t('createResource.secretNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('common.type')">
              <el-select v-model="secretForm.type" :placeholder="t('createResource.secretTypePlaceholder')">
                <el-option label="Opaque" value="Opaque" />
                <el-option label="kubernetes.io/dockerconfigjson" value="kubernetes.io/dockerconfigjson" />
                <el-option label="kubernetes.io/tls" value="kubernetes.io/tls" />
                <el-option label="kubernetes.io/basic-auth" value="kubernetes.io/basic-auth" />
                <el-option label="kubernetes.io/ssh-auth" value="kubernetes.io/ssh-auth" />
                <el-option label="bootstrap.kubernetes.io/token" value="bootstrap.kubernetes.io/token" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('configmap.dataConfig')" required>
              <el-input
                type="textarea"
                v-model="secretForm.dataText"
                :placeholder="t('createResource.secretDataPlaceholder')"
                :rows="6"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="Ingress" name="ingress">
          <el-form :model="ingressForm" label-width="120px">
            <el-form-item :label="t('common.namespace')" required>
              <el-select v-model="ingressForm.namespace" :placeholder="t('createResource.namespacePlaceholder')">
                <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('common.name')" required>
              <el-input v-model="ingressForm.name" :placeholder="t('createResource.ingressNamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('createResource.domain')" required>
              <el-input v-model="ingressForm.host" :placeholder="t('createResource.domainPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('createResource.pathConfig')" required>
              <el-row :gutter="10">
                <el-col span="8">
                  <el-input v-model="ingressForm.path" :placeholder="t('createResource.pathPlaceholder')" />
                </el-col>
                <el-col span="8">
                  <el-input v-model="ingressForm.serviceName" :placeholder="t('createResource.relatedServicePlaceholder')" />
                </el-col>
                <el-col span="8">
                  <el-input v-model="ingressForm.servicePort" :placeholder="t('createResource.servicePortPlaceholder')" />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="t('createResource.tls')">
              <el-checkbox v-model="ingressForm.enableTls">{{ t('createResource.enableHttps') }}</el-checkbox>
              <el-input v-model="ingressForm.tlsSecretName" :placeholder="t('createResource.tlsSecretPlaceholder')" style="margin-top: 10px;" :disabled="!ingressForm.enableTls" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showCreateResourceDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateResource" :loading="createLoading">{{ t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- Pod详情弹窗 -->
    <el-dialog v-model="showPodDetailDialog" :title="t('pod.detail')" width="900px">
      <div v-if="selectedPod" class="detail-container">
        <el-tabs v-model="activePodTab" type="border-card">
          <el-tab-pane :label="t('common.basicInfo')" name="podBase">
            <el-descriptions border column="2" size="small">
              <el-descriptions-item :label="t('common.name')">
                {{ selectedPod.metadata?.name }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('common.namespace')">
                {{ selectedPod.metadata?.namespace }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('common.status')">
                <el-tag :type="getPodStatusType(selectedPod.status?.phase)">
                  {{ selectedPod.status?.phase || '-' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="t('pod.qos')">
                <el-tag size="small">{{ selectedPod.status?.qosClass || '-' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Pod IP">{{ selectedPod.status?.podIP || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Host IP">{{ selectedPod.status?.hostIP || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('pod.node')">{{ selectedPod.spec?.nodeName || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('pod.restartPolicy')">{{ selectedPod.spec?.restartPolicy || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('pod.priorityClass')">{{ selectedPod.spec?.priorityClassName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="ServiceAccount">{{ selectedPod.spec?.serviceAccountName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="UID" span="2">
                <el-text size="small" truncated>{{ selectedPod.metadata?.uid }}</el-text>
              </el-descriptions-item>
              <el-descriptions-item :label="t('common.createdAt')" span="2">{{ selectedPod.metadata?.creationTimestamp }}</el-descriptions-item>
            </el-descriptions>
            <div v-if="selectedPod.metadata?.labels" class="mt-4">
              <h4>{{ t('common.labels') }}</h4>
              <el-tag v-for="(value, key) in selectedPod.metadata.labels" :key="key" size="small" class="mr-1 mb-1">
                {{ key }}: {{ value }}
              </el-tag>
            </div>
            <div v-if="selectedPod.metadata?.annotations" class="mt-3">
              <h4>{{ t('common.annotations') }}</h4>
              <el-tag v-for="(value, key) in selectedPod.metadata.annotations" :key="key" size="small" class="mr-1 mb-1" type="info">
                {{ key }}: {{ value }}
              </el-tag>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="t('pod.containerInfo')" name="podContainers">
            <div v-if="selectedPod.spec?.containers">
              <el-card
                v-for="(container, cIdx) in selectedPod.spec.containers"
                :key="cIdx"
                class="mb-3"
                shadow="hover"
              >
                <template #header>
                  <span class="font-bold">{{ container.name }}</span>
                  <el-tag size="small" class="ml-2">{{ container.image }}</el-tag>
                </template>
                <el-descriptions border column="2" size="small">
                  <el-descriptions-item :label="t('pod.imagePullPolicy')">{{ container.imagePullPolicy }}</el-descriptions-item>
                  <el-descriptions-item :label="t('pod.ports')">
                    <el-tag
                      v-for="(port, pIdx) in container.ports"
                      :key="pIdx"
                      size="small"
                      class="mr-1"
                    >{{ port.name || port.containerPort }}: {{ port.containerPort }}/{{ port.protocol }}</el-tag>
                    <span v-if="!container.ports || container.ports.length === 0">-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.resourceRequests')">
                    <template v-if="container.resources?.requests">
                      <div v-if="container.resources.requests.cpu">CPU: {{ container.resources.requests.cpu }}</div>
                      <div v-if="container.resources.requests.memory">{{ t('pod.memory') }}: {{ container.resources.requests.memory }}</div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.resourceLimits')">
                    <template v-if="container.resources?.limits">
                      <div v-if="container.resources.limits.cpu">CPU: {{ container.resources.limits.cpu }}</div>
                      <div v-if="container.resources.limits.memory">{{ t('pod.memory') }}: {{ container.resources.limits.memory }}</div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.readinessProbe')" span="2">
                    <template v-if="container.readinessProbe">
                      <el-tag size="small" class="mr-1">{{ t('pod.probeType') }}: {{ container.readinessProbe.httpGet ? 'HTTP' : container.readinessProbe.tcpSocket ? 'TCP' : container.readinessProbe.exec ? 'Exec' : t('common.other') }}</el-tag>
                      <span v-if="container.readinessProbe.httpGet">
                        {{ container.readinessProbe.httpGet.path }}:{{ container.readinessProbe.httpGet.port }}
                      </span>
                      <span class="ml-1">{{ t('pod.interval') }}{{ container.readinessProbe.periodSeconds }}s / {{ t('pod.timeout') }}{{ container.readinessProbe.timeoutSeconds }}s</span>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.livenessProbe')" span="2">
                    <template v-if="container.livenessProbe">
                      <el-tag size="small" class="mr-1">{{ t('pod.probeType') }}: {{ container.livenessProbe.httpGet ? 'HTTP' : container.livenessProbe.tcpSocket ? 'TCP' : container.livenessProbe.exec ? 'Exec' : t('common.other') }}</el-tag>
                      <span v-if="container.livenessProbe.httpGet">
                        {{ container.livenessProbe.httpGet.path }}:{{ container.livenessProbe.httpGet.port }}
                      </span>
                      <span class="ml-1">{{ t('pod.interval') }}{{ container.livenessProbe.periodSeconds }}s / {{ t('pod.timeout') }}{{ container.livenessProbe.timeoutSeconds }}s</span>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.volumeMounts')" span="2">
                    <div v-for="(vm, vmIdx) in container.volumeMounts" :key="vmIdx" class="mb-1">
                      <el-tag size="small" type="warning">{{ vm.name }}</el-tag>
                      <span class="ml-1">{{ vm.mountPath }}</span>
                      <el-tag v-if="vm.readOnly" size="small" type="info" class="ml-1">{{ t('common.readOnly') }}</el-tag>
                    </div>
                    <span v-if="!container.volumeMounts || container.volumeMounts.length === 0">-</span>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </div>
            <el-empty v-else :description="t('pod.noContainer')" />
          </el-tab-pane>

          <el-tab-pane :label="t('pod.statusDetail')" name="podStatus">
            <h4 class="mb-2">{{ t('pod.containerStatus') }}</h4>
            <div v-if="selectedPod.status?.containerStatuses">
              <el-card
                v-for="(cs, csIdx) in selectedPod.status.containerStatuses"
                :key="csIdx"
                class="mb-3"
                shadow="hover"
              >
                <template #header>
                  <span class="font-bold">{{ cs.name }}</span>
                  <el-tag
                    :type="cs.ready ? 'success' : 'danger'"
                    size="small"
                    class="ml-2"
                  >{{ cs.ready ? 'Ready' : 'NotReady' }}</el-tag>
                  <el-tag v-if="cs.restartCount > 0" type="warning" size="small" class="ml-1">
                    {{ t('pod.restartCount', { n: cs.restartCount }) }}
                  </el-tag>
                </template>
                <el-descriptions border column="2" size="small">
                  <el-descriptions-item label="Image">{{ cs.image }}</el-descriptions-item>
                  <el-descriptions-item label="ImageID" span="2">
                    <el-text size="small" truncated>{{ cs.imageID }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item label="ContainerID" span="2">
                    <el-text size="small" truncated>{{ cs.containerID }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item label="Started">{{ cs.started ? t('common.yes') : t('common.no') }}</el-descriptions-item>
                  <el-descriptions-item :label="t('pod.currentState')">
                    <template v-if="cs.state?.running">
                      <el-tag type="success" size="small">Running</el-tag>
                      <span class="ml-1">since {{ cs.state.running.startedAt }}</span>
                    </template>
                    <template v-else-if="cs.state?.waiting">
                      <el-tag type="warning" size="small">Waiting: {{ cs.state.waiting.reason }}</el-tag>
                    </template>
                    <template v-else-if="cs.state?.terminated">
                      <el-tag type="danger" size="small">Terminated: {{ cs.state.terminated.reason }}</el-tag>
                    </template>
                    <template v-else>-</template>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </div>
            <el-empty v-else :description="t('pod.noContainerStatus')" />

            <h4 class="mb-2 mt-4">Conditions</h4>
            <el-timeline v-if="selectedPod.status?.conditions">
              <el-timeline-item
                v-for="(condition, condIdx) in selectedPod.status.conditions"
                :key="condIdx"
                :type="condition.status === 'True' ? 'success' : condition.status === 'False' ? 'danger' : 'warning'"
                :timestamp="condition.lastTransitionTime"
              >
                <strong>{{ condition.type }}</strong>
                <span class="ml-2">{{ condition.status }}</span>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else :description="t('pod.noCondition')" />

            <h4 class="mb-2 mt-4">{{ t('common.volumes') }}</h4>
            <el-table
              v-if="selectedPod.spec?.volumes"
              :data="selectedPod.spec.volumes"
              size="small"
              border
            >
              <el-table-column prop="name" :label="t('common.name')" />
              <el-table-column :label="t('common.type')">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.configMap ? 'ConfigMap' : scope.row.secret ? 'Secret' : scope.row.projected ? 'Projected' : scope.row.emptyDir ? 'EmptyDir' : scope.row.hostPath ? 'HostPath' : scope.row.persistentVolumeClaim ? 'PVC' : t('common.other') }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.details')" show-overflow-tooltip>
                <template #default="scope">
                  <span v-if="scope.row.configMap">ConfigMap: {{ scope.row.configMap.name }}</span>
                  <span v-else-if="scope.row.secret">Secret: {{ scope.row.secret.secretName }}</span>
                  <span v-else-if="scope.row.projected">Projected ({{ scope.row.projected.sources?.length }} sources)</span>
                  <span v-else-if="scope.row.persistentVolumeClaim">PVC: {{ scope.row.persistentVolumeClaim.claimName }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="t('pod.noVolume')" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- Pod日志弹窗 -->
    <el-dialog v-model="showPodLogsDialog" :title="t('pod.logs')" width="1000px">
      <el-card class="log-card">
        <pre class="log-content">{{ podLogsContent }}</pre>
      </el-card>
      <template #footer>
        <el-button @click="showPodLogsDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Deployment详情弹窗 -->
    <el-dialog v-model="showDeploymentDetailDialog" :title="t('deployment.detail')" width="950px">
      <div v-if="deploymentDetail" class="detail-container">
        <el-tabs v-model="activeDetailTab" type="border-card">
          <el-tab-pane :label="t('common.basicInfo')" name="deployBase">
            <el-descriptions border column="2" size="small">
              <el-descriptions-item :label="t('common.name')">
                {{ deploymentDetail.metadata?.name }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('common.namespace')">
                {{ deploymentDetail.metadata?.namespace }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.desired')">
                <el-tag type="primary">{{ deploymentDetail.spec?.replicas }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.available')">
                <el-tag :type="deploymentDetail.status?.availableReplicas ? 'success' : 'warning'">
                  {{ deploymentDetail.status?.availableReplicas ?? 0 }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.ready')">
                {{ deploymentDetail.status?.readyReplicas ?? 0 }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.updated')">
                {{ deploymentDetail.status?.updatedReplicas ?? 0 }}
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.updateStrategy')">
                {{ deploymentDetail.spec?.strategy?.type || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="RollingUpdate">
                <template v-if="deploymentDetail.spec?.strategy?.rollingUpdate">
                  <el-tag size="small">MaxSurge: {{ deploymentDetail.spec.strategy.rollingUpdate.maxSurge }}</el-tag>
                  <el-tag size="small" class="ml-1">MaxUnavailable: {{ deploymentDetail.spec.strategy.rollingUpdate.maxUnavailable }}</el-tag>
                </template>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item :label="t('deployment.versionCount')">{{ deploymentDetail.spec?.revisionHistoryLimit ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="ProgressDeadline">{{ deploymentDetail.spec?.progressDeadlineSeconds }}s</el-descriptions-item>
              <el-descriptions-item label="ObservedGen">{{ deploymentDetail.status?.observedGeneration ?? '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('common.createdAt')">{{ deploymentDetail.metadata?.creationTimestamp }}</el-descriptions-item>
            </el-descriptions>
            <div v-if="deploymentDetail.spec?.selector?.matchLabels" class="mt-4">
              <h4>{{ t('deployment.labelSelector') }}</h4>
              <el-tag v-for="(value, key) in deploymentDetail.spec.selector.matchLabels" :key="key" size="small" class="mr-1 mb-1">{{ key }}: {{ value }}</el-tag>
            </div>
            <div v-if="deploymentDetail.metadata?.annotations" class="mt-3">
              <h4>{{ t('common.annotations') }}</h4>
              <el-tag v-for="(value, key) in deploymentDetail.metadata.annotations" :key="key" size="small" type="info" class="mr-1 mb-1">{{ key }}: {{ value }}</el-tag>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="t('common.podTemplate')" name="deployPod">
            <div v-if="deploymentDetail.spec?.template?.spec?.containers">
              <el-card
                v-for="(container, cIdx) in deploymentDetail.spec.template.spec.containers"
                :key="cIdx"
                class="mb-3"
                shadow="hover"
              >
                <template #header>
                  <span class="font-bold">{{ container.name }}</span>
                  <el-tag size="small" class="ml-2">{{ container.image }}</el-tag>
                </template>
                <el-descriptions border column="2" size="small">
                  <el-descriptions-item :label="t('pod.imagePullPolicy')">{{ container.imagePullPolicy || '-' }}</el-descriptions-item>
                  <el-descriptions-item :label="t('pod.ports')">
                    <el-tag v-for="(port, pIdx) in container.ports" :key="pIdx" size="small" class="mr-1">{{ port.name || port.containerPort }}: {{ port.containerPort }}/{{ port.protocol }}</el-tag>
                    <span v-if="!container.ports || container.ports.length === 0">-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.resourceRequests')">
                    <template v-if="container.resources?.requests && Object.keys(container.resources.requests).length">
                      <div v-if="container.resources.requests.cpu">CPU: {{ container.resources.requests.cpu }}</div>
                      <div v-if="container.resources.requests.memory">{{ t('pod.memory') }}: {{ container.resources.requests.memory }}</div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.resourceLimits')">
                    <template v-if="container.resources?.limits && Object.keys(container.resources.limits).length">
                      <div v-if="container.resources.limits.cpu">CPU: {{ container.resources.limits.cpu }}</div>
                      <div v-if="container.resources.limits.memory">{{ t('pod.memory') }}: {{ container.resources.limits.memory }}</div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="Command" span="2">
                    <template v-if="container.command">
                      <el-tag v-for="(arg, aIdx) in container.command" :key="aIdx" size="small" class="mr-1 mb-1">{{ arg }}</el-tag>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="Args" span="2">
                    <template v-if="container.args">
                      <el-tag v-for="(arg, aIdx) in container.args" :key="aIdx" size="small" class="mr-1 mb-1">{{ arg }}</el-tag>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('createResource.envVars')" span="2">
                    <template v-if="container.env && container.env.length">
                      <div v-for="(env, eIdx) in container.env" :key="eIdx" class="mb-1">
                        <el-tag size="small">{{ env.name }}</el-tag>
                        <span class="ml-1" v-if="env.value">= {{ env.value }}</span>
                        <el-tag v-else-if="env.valueFrom" size="small" type="info" class="ml-1">
                          from: {{ env.valueFrom.fieldRef?.fieldPath || Object.keys(env.valueFrom)[0] }}
                        </el-tag>
                      </div>
                    </template>
                    <span v-else>-</span>
                  </el-descriptions-item>
                  <el-descriptions-item :label="t('pod.volumeMounts')" span="2">
                    <div v-for="(vm, vmIdx) in container.volumeMounts" :key="vmIdx" class="mb-1">
                      <el-tag size="small" type="warning">{{ vm.name }}</el-tag>
                      <span class="ml-1">{{ vm.mountPath }}</span>
                      <el-tag v-if="vm.readOnly" size="small" type="info" class="ml-1">{{ t('common.readOnly') }}</el-tag>
                    </div>
                    <span v-if="!container.volumeMounts || container.volumeMounts.length === 0">-</span>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </div>
            <el-empty v-else :description="t('pod.noContainer')" />

            <el-descriptions border column="2" size="small" class="mt-3">
              <el-descriptions-item :label="t('pod.restartPolicy')">
                {{ deploymentDetail.spec?.template?.spec?.restartPolicy || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="ServiceAccount">
                {{ deploymentDetail.spec?.template?.spec?.serviceAccountName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="Scheduler">
                {{ deploymentDetail.spec?.template?.spec?.dashboardName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="TermGracePeriod">
                {{ deploymentDetail.spec?.template?.spec?.terminationGracePeriodSeconds || '-' }}s
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="deploymentDetail.spec?.template?.spec?.nodeSelector" class="mt-3">
              <h4>{{ t('deployment.nodeSelector') }}</h4>
              <el-tag v-for="(value, key) in deploymentDetail.spec.template.spec.nodeSelector" :key="key" size="small" class="mr-1 mb-1">{{ key }}: {{ value }}</el-tag>
            </div>
            <div v-if="deploymentDetail.spec?.template?.spec?.tolerations" class="mt-3">
              <h4>{{ t('deployment.tolerations') }}</h4>
              <el-tag v-for="(tol, tIdx) in deploymentDetail.spec.template.spec.tolerations" :key="tIdx" size="small" class="mr-1 mb-1">{{ tol.key }}: {{ tol.operator }} {{ tol.effect }}</el-tag>
            </div>
            <div v-if="deploymentDetail.spec?.template?.spec?.volumes" class="mt-3">
              <h4>{{ t('common.volumes') }}</h4>
              <el-table :data="deploymentDetail.spec.template.spec.volumes" size="small" border>
                <el-table-column prop="name" :label="t('common.name')" />
                <el-table-column :label="t('common.type')">
                  <template #default="scope">
                    <el-tag size="small">{{ scope.row.configMap ? 'ConfigMap' : scope.row.secret ? 'Secret' : scope.row.projected ? 'Projected' : scope.row.emptyDir ? 'EmptyDir' : scope.row.hostPath ? 'HostPath' : scope.row.persistentVolumeClaim ? 'PVC' : t('common.other') }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="t('common.details')" show-overflow-tooltip>
                  <template #default="scope">
                    <span v-if="scope.row.configMap">ConfigMap: {{ scope.row.configMap.name }}</span>
                    <span v-else-if="scope.row.secret">Secret: {{ scope.row.secret.secretName }}</span>
                    <span v-else-if="scope.row.projected">Projected ({{ scope.row.projected.sources?.length }} sources)</span>
                    <span v-else-if="scope.row.persistentVolumeClaim">PVC: {{ scope.row.persistentVolumeClaim.claimName }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="t('deployment.statusAndConditions')" name="deployStatus">
            <el-descriptions border column="2" size="small">
              <el-descriptions-item :label="t('deployment.desired')">{{ deploymentDetail.spec?.replicas }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.runningReplicas')">{{ deploymentDetail.status?.replicas ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.ready')">{{ deploymentDetail.status?.readyReplicas ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.available')">{{ deploymentDetail.status?.availableReplicas ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.updated')">{{ deploymentDetail.status?.updatedReplicas ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.unavailable')">{{ deploymentDetail.status?.unavailableReplicas ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.collisions')">{{ deploymentDetail.status?.collisionCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item :label="t('deployment.observed')">{{ deploymentDetail.status?.observedGeneration ?? '-' }}</el-descriptions-item>
            </el-descriptions>

            <h4 class="mt-4 mb-2">Conditions</h4>
            <div v-if="deploymentDetail.status?.conditions">
              <el-card
                v-for="(condition, condIdx) in deploymentDetail.status.conditions"
                :key="condIdx"
                class="mb-2"
                shadow="hover"
              >
                <template #header>
                  <el-tag :type="condition.status === 'True' ? 'success' : condition.status === 'False' ? 'danger' : 'warning'">
                    {{ condition.type }}
                  </el-tag>
                  <el-tag size="small" class="ml-2">{{ condition.status }}</el-tag>
                </template>
                <p><strong>{{ t('common.reason') }}:</strong> {{ condition.reason }}</p>
                <p><strong>{{ t('common.message') }}:</strong> {{ condition.message }}</p>
                <p class="text-muted">
                  <strong>{{ t('common.lastTransition') }}:</strong> {{ condition.lastTransitionTime }}
                  <span v-if="condition.lastUpdateTime" class="ml-3"><strong>{{ t('common.lastUpdate') }}:</strong> {{ condition.lastUpdateTime }}</span>
                </p>
              </el-card>
            </div>
            <el-empty v-else :description="t('pod.noCondition')" />
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="showDeploymentDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- StatefulSet详情弹窗 -->
    <el-dialog v-model="showStsDetailDialog" :title="t('sts.detail')" width="800px">
      <div v-if="stsDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ stsDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">{{ stsDetail.metadata?.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('deployment.desired')">
            <el-tag type="primary">{{ stsDetail.spec?.replicas }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('deployment.ready')">
            <el-tag :type="stsDetail.status?.readyReplicas ? 'success' : 'warning'">{{ stsDetail.status?.readyReplicas ?? 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('deployment.current')">{{ stsDetail.status?.currentReplicas ?? '-' }}</el-descriptions-item>
          <el-descriptions-item :label="t('deployment.updated')">{{ stsDetail.status?.updatedReplicas ?? '-' }}</el-descriptions-item>
          <el-descriptions-item :label="t('sts.serviceName')">{{ stsDetail.spec?.serviceName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('sts.podManagement')">{{ stsDetail.spec?.podManagementPolicy || 'OrderedReady' }}</el-descriptions-item>
          <el-descriptions-item label="ObservedGen">{{ stsDetail.status?.observedGeneration ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ stsDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="stsDetail.spec?.selector?.matchLabels" class="mt-4">
          <strong>{{ t('deployment.selector') }}</strong>
          <el-tag v-for="(v, k) in stsDetail.spec.selector.matchLabels" :key="k" size="small" class="mr-1 mt-1">{{ k }}={{ v }}</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="showStsDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- DaemonSet详情弹窗 -->
    <el-dialog v-model="showDsDetailDialog" :title="t('ds.detail')" width="800px">
      <div v-if="dsDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ dsDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">{{ dsDetail.metadata?.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('ds.desiredNode')">
            <el-tag type="primary">{{ dsDetail.status?.desiredNumberScheduled ?? 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('ds.currentNumber')">{{ dsDetail.status?.currentNumberScheduled ?? 0 }}</el-descriptions-item>
          <el-descriptions-item :label="t('ds.readyNode')">
            <el-tag :type="dsDetail.status?.numberReady ? 'success' : 'warning'">{{ dsDetail.status?.numberReady ?? 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('ds.updatedNumber')">{{ dsDetail.status?.updatedNumberScheduled ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('deployment.available')">{{ dsDetail.status?.numberAvailable ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ dsDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="dsDetail.spec?.selector?.matchLabels" class="mt-4">
          <strong>{{ t('deployment.selector') }}</strong>
          <el-tag v-for="(v, k) in dsDetail.spec.selector.matchLabels" :key="k" size="small" class="mr-1 mt-1">{{ k }}={{ v }}</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDsDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Node详情弹窗 -->
    <el-dialog v-model="showNodeDetailDialog" :title="t('node.detail')" width="800px">
      <div v-if="nodeDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ nodeDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('node.status')">
            <el-tag :type="getNodeStatusType(nodeDetail)">{{ getNodeStatus(nodeDetail) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('node.roles')">{{ getNodeRoles(nodeDetail) || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('node.ip')">{{ getNodeInternalIP(nodeDetail) || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('node.kubeletVersion')">{{ nodeDetail.status?.nodeInfo?.kubeletVersion || '—' }}</el-descriptions-item>
          <el-descriptions-item label="OS">{{ nodeDetail.status?.nodeInfo?.osImage || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Arch">{{ nodeDetail.status?.nodeInfo?.architecture || '—' }}</el-descriptions-item>
          <el-descriptions-item label="Container Runtime">{{ nodeDetail.status?.nodeInfo?.containerRuntimeVersion || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ nodeDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <template v-for="cond in nodeDetail.status?.conditions || []" :key="cond.type">
          <el-tag v-if="cond.status === 'True'" size="small" class="mr-1 mt-1">{{ cond.type }}</el-tag>
        </template>
      </div>
      <template #footer>
        <el-button @click="showNodeDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- PersistentVolume详情弹窗 -->
    <el-dialog v-model="showPvDetailDialog" :title="t('pv.detail')" width="800px">
      <div v-if="pvDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ pvDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('pv.status')">
            <el-tag :type="getPvStatusType(pvDetail.status?.phase)">{{ pvDetail.status?.phase || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('pv.capacity')">{{ pvDetail.spec?.capacity?.storage || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('pv.reclaimPolicy')">{{ pvDetail.spec?.persistentVolumeReclaimPolicy || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('pv.accessModes')">{{ (pvDetail.spec?.accessModes || []).join(', ') || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('pv.storageClass')">{{ pvDetail.spec?.storageClassName || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('pv.boundClaim')" :span="2">
            {{ pvDetail.spec?.claimRef ? `${pvDetail.spec.claimRef.namespace}/${pvDetail.spec.claimRef.name}` : '—' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ pvDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="pvDetail.spec?.csi?.driver" class="mt-4">
          <strong>CSI Driver:</strong>{{ pvDetail.spec.csi.driver }}
          <span v-if="pvDetail.spec.csi.volumeHandle" class="ml-2">(volumeHandle: {{ pvDetail.spec.csi.volumeHandle }})</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPvDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- StorageClass详情弹窗 -->
    <el-dialog v-model="showScDetailDialog" :title="t('sc.detail')" width="800px">
      <div v-if="scDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ scDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('sc.default')">
            <el-tag v-if="isDefaultStorageClass(scDetail)" type="warning">{{ t('sc.yesDefault') }}</el-tag>
            <span v-else>—</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('sc.provisioner')" :span="2">{{ scDetail.provisioner || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('sc.reclaimPolicy')">{{ scDetail.reclaimPolicy || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('sc.bindingMode')">{{ scDetail.volumeBindingMode || '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')" :span="2">{{ scDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="scDetail.parameters && Object.keys(scDetail.parameters).length" class="mt-4">
          <strong>{{ t('sc.parameters') }}</strong>
          <el-descriptions border column="2" size="small" class="mt-2">
            <el-descriptions-item v-for="(v, k) in scDetail.parameters" :key="k" :label="k">{{ v }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="showScDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- HPA详情弹窗 -->
    <el-dialog v-model="showHpaDetailDialog" :title="t('hpa.detail')" width="800px">
      <div v-if="hpaDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ hpaDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">{{ hpaDetail.metadata?.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('hpa.target')">
            {{ hpaDetail.spec?.scaleTargetRef ? `${hpaDetail.spec.scaleTargetRef.kind}/${hpaDetail.spec.scaleTargetRef.name}` : '—' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('hpa.currentReplicas')">
            {{ hpaDetail.status?.currentReplicas ?? 0 }} ({{ t('hpa.desired') }} {{ hpaDetail.status?.desiredReplicas ?? 0 }})
          </el-descriptions-item>
          <el-descriptions-item :label="t('hpa.minReplicas')">{{ hpaDetail.spec?.minReplicas ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('hpa.maxReplicas')">{{ hpaDetail.spec?.maxReplicas ?? '—' }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')" :span="2">{{ hpaDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="(hpaDetail.spec?.metrics || []).length" class="mt-4">
          <strong>{{ t('hpa.metrics') }}</strong>
          <el-table :data="hpaDetail.spec.metrics" border size="small" class="mt-2">
            <el-table-column :label="t('hpa.metricType')" width="120">
              <template #default="scope">
                {{ scope.row.type || (scope.row.pods ? 'Pods' : scope.row.resource ? 'Resource' : '—') }}
              </template>
            </el-table-column>
            <el-table-column :label="t('hpa.metricValue')" min-width="200">
              <template #default="scope">
                <pre class="detail-pre">{{ JSON.stringify(scope.row, null, 2) }}</pre>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="(hpaDetail.status?.conditions || []).length" class="mt-4">
          <strong>{{ t('hpa.conditions') }}</strong>
          <el-tag v-for="cond in hpaDetail.status.conditions" :key="cond.type" size="small" class="mr-1 mt-1" :type="cond.status === 'True' ? 'success' : 'info'">
            {{ cond.type }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="showHpaDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- HPA min/max调整弹窗 -->
    <el-dialog v-model="showHpaMinMaxDialog" :title="t('hpa.adjustTitle')" width="500px">
      <el-form label-width="110px">
        <el-form-item :label="t('hpa.minReplicas')">
          <el-input-number v-model="hpaForm.minReplicas" :min="1" :max="1000" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="t('hpa.maxReplicas')">
          <el-input-number v-model="hpaForm.maxReplicas" :min="1" :max="1000" style="width: 100%;" />
        </el-form-item>
        <el-form-item>
          <el-alert :title="t('hpa.adjustTip')" type="info" show-icon :closable="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHpaMinMaxDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirmHpaMinMax">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- NetworkPolicy详情弹窗 -->
    <el-dialog v-model="showNpDetailDialog" :title="t('np.detail')" width="800px">
      <div v-if="npDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ npDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">{{ npDetail.metadata?.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('np.podSelector')" :span="2">{{ getNpPodSelector(npDetail) }}</el-descriptions-item>
          <el-descriptions-item :label="t('np.policyTypes')" :span="2">
            <el-tag v-for="pt in npDetail.spec?.policyTypes || []" :key="pt" size="small" class="mr-1">{{ pt }}</el-tag>
            <span v-if="!(npDetail.spec?.policyTypes || []).length">—</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ npDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-4">
          <strong>{{ t('np.rulesJson') }}</strong>
          <pre class="detail-pre">{{ JSON.stringify({ ingress: npDetail.spec?.ingress, egress: npDetail.spec?.egress }, null, 2) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="showNpDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- RBAC详情弹窗 -->
    <el-dialog v-model="showRbacDetailDialog" :title="rbacDetailTitle" width="800px">
      <div v-if="rbacDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ rbacDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item v-if="rbacDetail.metadata?.namespace" :label="t('common.namespace')">{{ rbacDetail.metadata.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')" :span="2">{{ rbacDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <template v-if="rbacDetailType === 'serviceaccount'">
          <div class="mt-4">
            <strong>{{ t('rbac.secrets') }}</strong>
            <div class="mt-2">
              <el-tag v-for="s in rbacDetail.secrets || []" :key="s.name" size="small" class="mr-1 mt-1">{{ s.name }}</el-tag>
              <span v-if="!(rbacDetail.secrets || []).length">—</span>
            </div>
          </div>
        </template>
        <template v-else-if="rbacDetailType === 'role' || rbacDetailType === 'clusterrole'">
          <div class="mt-4">
            <strong>{{ t('rbac.rules') }}</strong>
            <el-table :data="rbacDetail.rules || []" border size="small" class="mt-2">
              <el-table-column prop="apiGroups" :label="t('rbac.apiGroups')" min-width="140">
                <template #default="scope">{{ (scope.row.apiGroups || []).join(', ') || '—' }}</template>
              </el-table-column>
              <el-table-column prop="resources" :label="t('rbac.resources')" min-width="140">
                <template #default="scope">{{ (scope.row.resources || []).join(', ') || '—' }}</template>
              </el-table-column>
              <el-table-column prop="verbs" :label="t('rbac.verbs')" min-width="140">
                <template #default="scope">{{ (scope.row.verbs || []).join(', ') || '—' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>
        <template v-else>
          <div class="mt-4">
            <el-descriptions border column="2" size="small">
              <el-descriptions-item :label="t('rbac.roleRef')">
                {{ rbacDetail.roleRef ? `${rbacDetail.roleRef.kind}/${rbacDetail.roleRef.name}` : '—' }}
              </el-descriptions-item>
            </el-descriptions>
            <strong class="mt-2 block">{{ t('rbac.subjects') }}</strong>
            <el-table :data="rbacDetail.subjects || []" border size="small" class="mt-2">
              <el-table-column prop="kind" :label="t('rbac.subjectKind')" width="140" />
              <el-table-column prop="name" :label="t('common.name')" min-width="140" />
              <el-table-column prop="namespace" :label="t('common.namespace')" min-width="120">
                <template #default="scope">{{ scope.row.namespace || '—' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="showRbacDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- ResourceQuota详情弹窗 -->
    <el-dialog v-model="showQuotaDetailDialog" :title="t('quota.detail')" width="800px">
      <div v-if="quotaDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">{{ quotaDetail.metadata?.name }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">{{ quotaDetail.metadata?.namespace }}</el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">{{ quotaDetail.metadata?.creationTimestamp }}</el-descriptions-item>
        </el-descriptions>
        <div class="mt-4">
          <strong>{{ t('quota.usage') }}</strong>
          <el-table :data="quotaDetailRows" border size="small" class="mt-2">
            <el-table-column prop="key" label="Resource" min-width="180" />
            <el-table-column prop="used" :label="t('quota.used')" min-width="110" />
            <el-table-column prop="hard" :label="t('quota.hard')" min-width="110" />
          </el-table>
          <el-empty v-if="quotaDetailRows.length === 0" :description="t('quota.noHardLimit')" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showQuotaDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- ResourceQuota 创建/编辑弹窗 -->
    <el-dialog v-model="showQuotaFormDialog" :title="quotaFormEditing ? t('quota.editTitle') : t('quota.createTitle')" width="700px">
      <el-form label-width="100px">
        <el-form-item :label="t('common.name')" required>
          <el-input v-model="quotaForm.name" :disabled="quotaFormEditing" placeholder="quota-name" />
        </el-form-item>
        <el-form-item v-if="!quotaFormEditing" :label="t('common.namespace')" required>
          <el-select v-model="quotaForm.namespace" style="width: 100%;">
            <el-option v-for="ns in namespaceList" :key="ns.name" :label="ns.name" :value="ns.name" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('quota.hardLimit')">
          <el-table :data="quotaForm.rows" border size="small">
            <el-table-column :label="t('quota.resourceKey')" min-width="180">
              <template #default="scope">
                <el-input v-model="scope.row.key" placeholder="requests.cpu" />
              </template>
            </el-table-column>
            <el-table-column :label="t('quota.limitValue')" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.value" placeholder="1 / 1Gi / 10" />
              </template>
            </el-table-column>
            <el-table-column :label="t('common.actions')" width="80" align="center">
              <template #default="scope">
                <el-button type="danger" size="small" link @click="quotaForm.rows.splice(scope.$index, 1)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button size="small" class="mt-2" @click="quotaForm.rows.push({ key: '', value: '' })">
            <el-icon class="mr-1"><Plus /></el-icon>{{ t('quota.addRow') }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-alert :title="t('quota.tip')" type="info" show-icon :closable="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuotaFormDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="quotaSaving" @click="confirmQuotaForm">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- Service详情弹窗 -->
    <el-dialog v-model="showServiceDetailDialog" :title="t('service.detail')" width="800px">
      <div v-if="serviceDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">
            {{ serviceDetail.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">
            {{ serviceDetail.namespace || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.type')">
            <el-tag :type="getServiceTypeTag(serviceDetail.type)">
              {{ serviceDetail.type || '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="ClusterIP">
            {{ serviceDetail.clusterIP || serviceDetail.clusterIp || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">
            {{ serviceDetail.creationTimestamp ? new Date(serviceDetail.creationTimestamp).toLocaleString(dateLocale()) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('service.selector')" span="2">
            <template v-if="serviceDetail.selector && Object.keys(serviceDetail.selector).length > 0">
              <el-tag v-for="(value, key) in serviceDetail.selector" :key="key" class="mr-1 mb-1">
                {{ key }}={{ value }}
              </el-tag>
            </template>
            <span v-else>{{ t('service.noSelector') }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="mt-4">
          <h4>{{ t('service.portConfig') }}</h4>
          <el-table :data="serviceDetail.ports || []" border stripe size="small" class="mt-2 service-detail-table" fit>
            <el-table-column :label="t('common.name')" prop="name" min-width="100" align="center" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('service.protocol')" prop="protocol" min-width="70" align="center">
              <template #default="scope">
                {{ scope.row.protocol || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('service.servicePort')" prop="port" min-width="80" align="center">
              <template #default="scope">
                {{ scope.row.port ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="t('service.targetPort')" prop="targetPort" min-width="80" align="center">
              <template #default="scope">
                {{ scope.row.targetPort ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="NodePort" prop="nodePort" min-width="80" align="center" v-if="serviceDetail.type === 'NodePort' || serviceDetail.type === 'LoadBalancer'">
              <template #default="scope">
                {{ scope.row.nodePort ?? '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="showServiceDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- PVC存储卷详情弹窗 -->
    <el-dialog v-model="showPvcDetailDialog" :title="t('pvc.detail')" width="800px">
      <div v-if="pvcDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">
            {{ pvcDetail.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">
            {{ pvcDetail.namespace || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.status')">
            <el-tag :type="getPvcStatusType(pvcDetail)">
              {{ pvcDetail.phase || 'Pending' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('pvc.storageClass')">
            {{ pvcDetail.storageClassName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('pvc.capacity')">
            {{ pvcDetail.capacity?.storage || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('pvc.mountVolumeName')">
            {{ pvcDetail.volumeName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">
            {{ pvcDetail.creationTimestamp ? new Date(pvcDetail.creationTimestamp).toLocaleString(dateLocale()) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('pvc.accessModes')" span="2">
            <template v-if="pvcDetail.accessModes && pvcDetail.accessModes.length > 0">
              <el-tag v-for="(mode, index) in pvcDetail.accessModes" :key="index" class="mr-1 mb-1">
                {{ mode }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.labels')" span="2">
            <template v-if="pvcDetail.labels && Object.keys(pvcDetail.labels).length > 0">
              <el-tag v-for="(value, key) in pvcDetail.labels" :key="key" class="mr-1 mb-1">
                {{ key }}={{ value }}
              </el-tag>
            </template>
            <span v-else>{{ t('pvc.noLabel') }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="mt-4" v-if="pvcDetail.conditions && pvcDetail.conditions.length > 0">
          <h4>{{ t('job.statusHistory') }}</h4>
          <el-timeline class="mt-3">
            <el-timeline-item
              v-for="(condition, index) in pvcDetail.conditions"
              :key="index"
              :timestamp="condition.lastTransitionTime ? new Date(condition.lastTransitionTime).toLocaleString(dateLocale()) : ''"
            >
              <h5>{{ condition.type }}</h5>
              <p>{{ condition.status === 'True' ? t('pvc.completed') : t('pvc.processing') }}</p>
              <p v-if="condition.message">{{ condition.message }}</p>
              <p v-if="condition.reason">{{ t('common.reason') }}: {{ condition.reason }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPvcDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Ingress路由详情弹窗 -->
    <el-dialog v-model="showIngressDetailDialog" :title="t('ingress.detail')" width="900px">
      <div v-if="ingressDetail" class="detail-container">
        <el-descriptions border column="2" size="small">
          <el-descriptions-item :label="t('common.name')">
            {{ ingressDetail.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.namespace')">
            {{ ingressDetail.namespace || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.status')">
            <el-tag :type="getIngressStatusType(ingressDetail)">
              {{ getIngressStatusType(ingressDetail) === 'success' ? t('ingress.normal') : t('ingress.pending') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('ingress.ingressClass')">
            {{ ingressDetail.ingressClassName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('ingress.loadBalancerAddr')" span="2">
            <template v-if="ingressDetail.loadBalancer?.ingress && ingressDetail.loadBalancer.ingress.length > 0">
              <div v-for="(addr, index) in ingressDetail.loadBalancer.ingress" :key="index" class="mb-1">
                {{ addr.ip || addr.hostname }}
              </div>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item :label="t('ingress.tls')">
            <el-tag v-if="ingressDetail.tls && ingressDetail.tls.length > 0" type="success">{{ t('common.enabled') }}</el-tag>
            <el-tag v-else type="info">{{ t('common.notEnabled') }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.createdAt')">
            {{ ingressDetail.creationTimestamp ? new Date(ingressDetail.creationTimestamp).toLocaleString(dateLocale()) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="t('common.annotations')" span="2">
            <template v-if="ingressDetail.annotations && Object.keys(ingressDetail.annotations).length > 0">
              <el-tag v-for="(value, key) in ingressDetail.annotations" :key="key" class="mr-1 mb-1" size="small">
                {{ key }}: {{ value }}
              </el-tag>
            </template>
            <span v-else>{{ t('ingress.noAnnotation') }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="mt-4" v-if="ingressDetail.tls && ingressDetail.tls.length > 0">
          <h4>{{ t('ingress.tls') }}</h4>
          <el-table :data="ingressDetail.tls" border stripe size="small" class="mt-2" fit>
            <el-table-column :label="t('ingress.hostList')" min-width="300">
              <template #default="scope">
                <div v-for="(host, index) in scope.row.hosts" :key="index" class="mb-1">
                  {{ host }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('ingress.certName')" prop="secretName" min-width="200" />
          </el-table>
        </div>

        <div class="mt-4">
          <h4>{{ t('ingress.rules') }}</h4>
          <el-collapse v-model="activeRuleIndex" class="mt-2">
            <el-collapse-item
              v-for="(rule, index) in ingressDetail.rules"
              :key="index"
              :title="t('ingress.rule', { index: index + 1, host: rule.host || t('ingress.defaultHost') })"
              :name="index"
            >
              <el-table :data="rule.http?.paths || []" border stripe size="small" fit>
                <el-table-column :label="t('ingress.path')" min-width="200" prop="path" />
                <el-table-column :label="t('ingress.pathType')" prop="pathType" />
                <el-table-column :label="t('ingress.relatedService')" min-width="200">
                  <template #default="scope">
                    {{ scope.row.backend?.service?.name || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('ingress.servicePort')">
                  <template #default="scope">
                    {{ scope.row.backend?.service?.port?.number || scope.row.backend?.service?.port?.name || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>

        <div class="mt-4" v-if="ingressDetail.conditions && ingressDetail.conditions.length > 0">
          <h4>{{ t('job.statusHistory') }}</h4>
          <el-timeline class="mt-3">
            <el-timeline-item
              v-for="(condition, index) in ingressDetail.conditions"
              :key="index"
              :timestamp="condition.lastTransitionTime ? new Date(condition.lastTransitionTime).toLocaleString(dateLocale()) : ''"
            >
              <h5>{{ condition.type }}</h5>
              <p>{{ condition.status === 'True' ? t('pvc.completed') : t('pvc.processing') }}</p>
              <p v-if="condition.message">{{ condition.message }}</p>
              <p v-if="condition.reason">{{ t('common.reason') }}: {{ condition.reason }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <el-button @click="showIngressDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- CRD详情弹窗 -->
    <el-dialog v-model="showCrdDetailDialog" :title="t('crd.detail', { name: crdDetail?.metadata?.name || '' })" width="1000px">
      <div v-if="crdDetail" class="detail-container">
        <el-tabs v-model="activeCrdTab" type="border-card">
          <el-tab-pane :label="t('common.basicInfo')" name="crdBase">
            <el-descriptions border column="2" size="small">
              <el-descriptions-item :label="t('common.name')">{{ crdDetail.metadata?.name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="UID">{{ crdDetail.metadata?.uid || '-' }}</el-descriptions-item>
              <el-descriptions-item label="ResourceVersion">{{ crdDetail.metadata?.resourceVersion || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('common.createdAt')">{{ formatTimestamp(crdDetail.metadata?.creationTimestamp) }}</el-descriptions-item>
              <el-descriptions-item label="Group">{{ crdDetail.spec?.group || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Scope">
                <el-tag :type="crdDetail.spec?.scope === 'Namespaced' ? 'primary' : 'warning'" size="small">
                  {{ crdDetail.spec?.scope || '-' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Kind">{{ crdDetail.spec?.names?.kind || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Singular">{{ crdDetail.spec?.names?.singular || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Plural">{{ crdDetail.spec?.names?.plural || '-' }}</el-descriptions-item>
              <el-descriptions-item label="ListKind">{{ crdDetail.spec?.names?.listKind || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('crd.shortName')" span="2">
                <template v-if="crdDetail.spec?.names?.shortNames && crdDetail.spec.names.shortNames.length > 0">
                  <el-tag v-for="(name, idx) in crdDetail.spec.names.shortNames" :key="idx" size="small" class="mr-1 mb-1">
                    {{ name }}
                  </el-tag>
                </template>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="crdDetail.metadata?.labels" class="mt-4">
              <h4>{{ t('common.labels') }}</h4>
              <el-tag v-for="(value, key) in crdDetail.metadata.labels" :key="key" size="small" class="mr-1 mb-1">
                {{ key }}: {{ value }}
              </el-tag>
            </div>
            <div v-if="crdDetail.metadata?.annotations" class="mt-3">
              <h4>{{ t('common.annotations') }}</h4>
              <el-tag v-for="(value, key) in crdDetail.metadata.annotations" :key="key" size="small" type="info" class="mr-1 mb-1" style="max-width: 100%; overflow: hidden; text-overflow: ellipsis;">
                {{ key }}: {{ value }}
              </el-tag>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="t('crd.versionInfo')" name="crdVersions">
            <div v-if="crdDetail.spec?.versions && crdDetail.spec.versions.length > 0">
              <el-collapse v-model="activeCrdVersions" accordion>
                <el-collapse-item v-for="(version, idx) in crdDetail.spec.versions" :key="idx" :name="idx">
                  <template #title>
                    <div class="flex items-center justify-between w-full pr-4">
                      <span class="font-bold">{{ version.name }}</span>
                      <div>
                        <el-tag v-if="version.served" type="success" size="small" class="mr-1">Served</el-tag>
                        <el-tag v-if="version.storage" type="warning" size="small">Storage</el-tag>
                      </div>
                    </div>
                  </template>
                  <div class="version-content">
                    <el-descriptions border column="1" size="small" v-if="version.schema?.openAPIV3Schema">
                      <el-descriptions-item :label="t('crd.description')">
                        {{ version.schema.openAPIV3Schema.description || '-' }}
                      </el-descriptions-item>
                      <el-descriptions-item :label="t('common.type')">
                        {{ version.schema.openAPIV3Schema.type || '-' }}
                      </el-descriptions-item>
                    </el-descriptions>
                    <div v-if="version.schema?.openAPIV3Schema" class="mt-3">
                      <h5>{{ t('crd.schemaStruct') }}</h5>
                      <el-tree
                        :data="transformSchemaToTree(version.schema.openAPIV3Schema)"
                        :props="{ label: 'label', children: 'children' }"
                        default-expand-all
                        node-key="id"
                      >
                        <template #default="{ node, data }">
                          <span class="custom-tree-node">
                            <span class="font-mono">{{ node.label }}</span>
                            <span v-if="data.type" class="ml-2 text-gray-500 text-xs">({{ data.type }})</span>
                            <span v-if="data.description" class="ml-2 text-gray-400 text-xs">— {{ data.description }}</span>
                          </span>
                        </template>
                      </el-tree>
                    </div>
                    <div v-if="version.subresources" class="mt-3">
                      <h5>{{ t('crd.subResources') }}</h5>
                      <el-descriptions border column="2" size="small">
                        <el-descriptions-item label="Status">
                          <el-tag :type="version.subresources.status ? 'success' : 'info'" size="small">
                            {{ version.subresources.status ? t('common.enable') : t('common.disable') }}
                          </el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="Scale">
                          <el-tag :type="version.subresources.scale ? 'success' : 'info'" size="small">
                            {{ version.subresources.scale ? t('common.enable') : t('common.disable') }}
                          </el-tag>
                        </el-descriptions-item>
                      </el-descriptions>
                    </div>
                    <div v-if="version.additionalPrinterColumns && version.additionalPrinterColumns.length > 0" class="mt-3">
                      <h5>{{ t('crd.extraColumns') }}</h5>
                      <el-table :data="version.additionalPrinterColumns" size="small" border>
                        <el-table-column prop="name" :label="t('common.name')" />
                        <el-table-column prop="type" :label="t('common.type')" />
                        <el-table-column prop="jsonPath" label="JSONPath" min-width="200" show-overflow-tooltip />
                        <el-table-column prop="description" :label="t('crd.description')" min-width="150" show-overflow-tooltip />
                      </el-table>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
            <el-empty v-else :description="t('crd.noVersion')" />
          </el-tab-pane>

          <el-tab-pane :label="t('crd.statusInfo')" name="crdStatus">
            <el-descriptions border column="2" size="small" v-if="crdDetail.status">
              <el-descriptions-item label="Accepted Kind">{{ crdDetail.status.acceptedNames?.kind || '-' }}</el-descriptions-item>
              <el-descriptions-item label="Accepted Plural">{{ crdDetail.status.acceptedNames?.plural || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="t('crd.storedVersion')" span="2">
                <el-tag v-for="(v, idx) in crdDetail.status.storedVersions" :key="idx" size="small" type="warning" class="mr-1 mb-1">
                  {{ v }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="crdDetail.status?.conditions && crdDetail.status.conditions.length > 0" class="mt-4">
              <h4>{{ t('crd.statusConditions') }}</h4>
              <el-timeline>
                <el-timeline-item
                  v-for="(condition, index) in crdDetail.status.conditions"
                  :key="index"
                  :type="condition.status === 'True' ? 'success' : condition.status === 'False' ? 'danger' : 'warning'"
                  :timestamp="formatTimestamp(condition.lastTransitionTime)"
                >
                  <h5>{{ condition.type }}</h5>
                  <p><strong>{{ t('common.status') }}:</strong> {{ condition.status }}</p>
                  <p v-if="condition.reason"><strong>{{ t('common.reason') }}:</strong> {{ condition.reason }}</p>
                  <p v-if="condition.message"><strong>{{ t('common.message') }}:</strong> {{ condition.message }}</p>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="t('crd.rawData')" name="crdRaw">
            <el-card>
              <pre class="code-display">{{ JSON.stringify(crdDetail, null, 2) }}</pre>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="showCrdDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- 扩缩容弹窗 -->
    <el-dialog v-model="showScaleDialog" :title="t('scale.title')" width="500px">
      <el-form :model="scaleForm" label-width="100px">
        <el-form-item :label="t('scale.current')">
          <span>{{ selectedDeployment?.spec?.replicas || 0 }}</span>
        </el-form-item>
        <el-form-item :label="t('scale.target')" required>
          <el-input-number v-model="scaleForm.replicas" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="t('scale.note')">
          <el-alert
            :title="t('scale.tip')"
            type="info"
            :description="t('scale.tipContent')"
            show-icon
            :closable="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScaleDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirmScale">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- StatefulSet扩缩容弹窗 -->
    <el-dialog v-model="showStsScaleDialog" :title="t('scale.title')" width="500px">
      <el-form label-width="100px">
        <el-form-item :label="t('scale.current')">
          <span>{{ selectedSts?.spec?.replicas || 0 }}</span>
        </el-form-item>
        <el-form-item :label="t('scale.target')" required>
          <el-input-number v-model="stsScaleReplicas" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item :label="t('scale.note')">
          <el-alert :title="t('scale.tip')" type="info" :description="t('scale.tipContent')" show-icon :closable="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStsScaleDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirmStsScale">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 用户信息维护弹窗 -->
    <el-dialog v-model="showUserProfileDialog" :title="t('profile.title')" width="600px">
      <el-tabs v-model="userProfileActiveTab" type="border-card">
        <el-tab-pane :label="t('common.basicInfo')" name="info">
          <el-form :model="userProfileForm" label-width="80px">
            <el-form-item :label="t('profile.account')">
              <span>{{ userInfo.account }}</span>
            </el-form-item>
            <el-form-item :label="t('profile.username')" prop="username">
              <el-input v-model="userProfileForm.username" :placeholder="t('profile.usernamePlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('profile.avatar')">
              <div class="avatar-group">
                <el-avatar :size="60">
                  <img v-if="userProfileForm.avatar" :src="userProfileForm.avatar" :alt="t('profile.avatar')" style="width: 100%; height: 100%; object-fit: cover;">
                  <span v-else>{{ t('common.loading') }}</span>
                </el-avatar>
                <el-button type="primary" size="small" @click="handleGetRandomAvatar">{{ t('profile.changeAvatar') }}</el-button>
              </div>
            </el-form-item>
            <el-form-item :label="t('profile.email')" prop="email">
              <el-input v-model="userProfileForm.email" :placeholder="t('profile.emailPlaceholder')" />
            </el-form-item>
            <el-form-item :label="t('profile.phone')" prop="phone">
              <el-input v-model="userProfileForm.phone" :placeholder="t('profile.phonePlaceholder')" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane :label="t('profile.changePassword')" name="password">
          <el-form :model="passwordForm" label-width="100px">
            <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" :placeholder="t('profile.oldPasswordPlaceholder')" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.newPassword')" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" :placeholder="t('profile.newPasswordPlaceholder')" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" :placeholder="t('profile.confirmPasswordPlaceholder')" show-password />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="showUserProfileDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button v-if="userProfileActiveTab === 'info'" type="primary" @click="handleUpdateUserInfo" :loading="userProfileLoading">{{ t('common.save') }}</el-button>
        <el-button v-if="userProfileActiveTab === 'password'" type="primary" @click="handleUpdatePassword" :loading="passwordLoading">{{ t('profile.changePassword') }}</el-button>
      </template>
    </el-dialog>

    <!-- 收件箱弹窗 -->
    <el-dialog v-model="showInboxDialog" :title="t('inbox.title')" width="700px">
      <div class="inbox-header" style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 500;">
          {{ t('inbox.total', { n: siteMessageList.length }) }}
          <span v-if="unreadCount > 0" style="color: #f56c6c; margin-left: 8px;">{{ t('inbox.unreadCount', { n: unreadCount }) }}</span>
        </span>
        <el-button type="primary" size="small" link @click="handleMarkAllRead" v-if="unreadCount > 0">
          {{ t('inbox.markAllRead') }}
        </el-button>
      </div>
      <div class="inbox-list" style="max-height: 500px; overflow-y: auto;">
        <el-empty v-if="siteMessageList.length === 0" :description="t('inbox.empty')" />
        <div v-for="msg in siteMessageList" :key="msg.id" class="message-item-wrapper" style="margin-bottom: 12px;">
          <el-card :shadow="isMessageRead(msg.id) ? 'never' : 'hover'" class="message-card" :class="{ 'unread': !isMessageRead(msg.id), ['msg-' + (msg.type || 'INFO').toLowerCase()]: true }">
            <div class="message-header" style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
              <div class="message-title-wrapper" style="display: flex; align-items: center; gap: 8px;">
                <el-tag :type="getMessageTypeTag(msg.type)" size="small">
                  <el-icon v-if="msg.type === 'ERROR'" style="margin-right: 4px;"><CircleCloseFilled /></el-icon>
                  <el-icon v-else-if="msg.type === 'WARNING'" style="margin-right: 4px;"><Warning /></el-icon>
                  <el-icon v-else-if="msg.type === 'SUCCESS'" style="margin-right: 4px;"><SuccessFilled /></el-icon>
                  <el-icon v-else style="margin-right: 4px;"><InfoFilled /></el-icon>
                  {{ getMessageTypeText(msg.type) }}
                </el-tag>
                <span class="message-title" style="font-weight: 600; font-size: 15px;">{{ msg.title }}</span>
                <el-tag v-if="!isMessageRead(msg.id)" type="danger" size="small" effect="dark">{{ t('inbox.unread') }}</el-tag>
              </div>
              <div class="message-time" style="color: #999; font-size: 12px;">{{ formatTime(msg.createdTime) }}</div>
            </div>
            <div class="message-content" style="color: #606266; line-height: 1.6; white-space: pre-wrap;">{{ msg.content }}</div>
            <div class="message-footer" style="margin-top: 12px; text-align: right;">
              <el-button type="primary" size="small" link @click="handleMarkRead(msg.id)" v-if="!isMessageRead(msg.id)">
                {{ t('inbox.markRead') }}
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>

    <!-- AI助手悬浮按钮 -->
    <div class="ai-assistant-btn" @click="showAIChat = !showAIChat">
      <img src="/ai-icon.svg" height="30" alt="AI" style="filter: brightness(0) invert(1);" />
    </div>

    <!-- AI聊天弹窗 - 完全重构版 -->
    <el-dialog
      v-model="showAIChat"
      :title="t('ai.title')"
      width="100vw"
      top="0"
      :close-on-click-modal="false"
      @close="handleAIChatClose"
      @opened="handleChatDialogOpened"
      custom-class="ai-chat-dialog"
      :destroy-on-close="false"
    >
      <!-- 最外层容器 - 完全控制布局 -->
      <div class="ai-chat-fullscreen">
        <!-- 左侧对话列表 - 固定宽度，独立滚动 -->
        <div class="ai-chat-sidebar">
          <div class="ai-chat-sidebar-header">
            <el-button type="primary" @click="createNewConversation" style="width: 100%;">
              <el-icon><Plus /></el-icon> {{ t('ai.newChat') }}
            </el-button>
          </div>
          <div class="ai-chat-conversation-list" ref="conversationListRef">
            <div
              v-for="conv in conversationList"
              :key="conv.id"
              :class="['ai-chat-conversation-item', { active: conv.id === conversationId }]"
            >
              <div class="ai-chat-conversation-title" @click="switchConversation(conv.id)">
                <span v-if="editingTitle !== conv.id">{{ conv.title || t('ai.newChat') }}</span>
                <el-input
                  v-else
                  v-model="newTitle"
                  @blur="saveTitle(conv.id)"
                  @keyup.enter="saveTitle(conv.id)"
                  size="small"
                  :ref="el => { if (el) titleInput.value = el }"
                />
              </div>
              <div class="ai-chat-conversation-actions">
                <el-button
                  link
                  size="small"
                  @click="startEditTitle(conv)"
                  :title="t('ai.editTitle')"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button
                  link
                  size="small"
                  type="danger"
                  @click="deleteConversationItem(conv.id)"
                  :title="t('ai.deleteChat')"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div class="ai-chat-conversation-time">
                {{ new Date(conv.updatedAt).toLocaleString(dateLocale(), { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧聊天区域 - 自适应宽度，独立滚动 -->
        <div class="ai-chat-content">
          <!-- 消息列表 - 独立滚动 -->
          <div class="ai-chat-message-list" ref="chatMessagesRef">
            <div v-for="(msg, index) in messageList" :key="index" :class="['ai-chat-message', msg.role]">
              <div class="ai-chat-message-avatar">
                <el-icon v-if="msg.role === 'user'" size="20"><User /></el-icon>
                <el-icon v-else size="20" color="#409eff"><ChatDotRound /></el-icon>
              </div>
              <div class="ai-chat-message-content">
                <div class="markdown-content" v-html="msg.renderedContent"></div>
                <!-- 附件列表 -->
                <div v-if="msg.attachments && msg.attachments.length > 0" class="message-attachments">
                  <div
                    v-for="(file, idx) in msg.attachments"
                    :key="idx"
                    class="message-attachment-item"
                    @click="window.open(file.url, '_blank')"
                  >
                    <el-icon size="18" class="attachment-icon"><Document /></el-icon>
                    <div class="attachment-info">
                      <div class="attachment-name">{{ file.name }}</div>
                      <div class="attachment-size">{{ formatFileSize(file.size) }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="aiThinking" class="ai-chat-message assistant">
              <div class="ai-chat-message-avatar">
                <el-icon size="20" color="#409eff"><ChatDotRound /></el-icon>
              </div>
              <div class="ai-chat-message-content thinking">
                <span class="dot">{{ t('ai.thinking') }}</span>
              </div>
            </div>
          </div>

          <!-- Agent专家区域 -->
          <div class="expert-prompts" v-if="expertList.length > 0">
            <div class="expert-header">
              <span class="expert-title">{{ t('ai.expertTitle') }}</span>
              <div class="expert-header-actions">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="showExpertList = !showExpertList"
                >
                  {{ showExpertList ? t('ai.collapse') : t('ai.expand') }}
                </el-button>
                <el-button
                  v-if="activeExpert"
                  type="warning"
                  link
                  size="small"
                  @click="deselectExpert"
                >
                  {{ t('ai.clearSelection') }}
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="showExpertManager = true"
                >
                  {{ t('ai.manage') }}
                </el-button>
              </div>
            </div>
            <div class="expert-list" v-show="showExpertList">
              <div
                v-for="expert in expertList"
                :key="expert.id"
                :class="['expert-item', { active: activeExpert?.id === expert.id }]"
                @click="selectExpert(expert)"
              >
                <span class="expert-icon">{{ expert.icon || '🤖' }}</span>
                <div class="expert-info">
                  <div class="expert-name">{{ expert.name }}</div>
                  <div class="expert-desc">{{ expert.description }}</div>
                </div>
                <el-tag v-if="activeExpert?.id === expert.id" size="small" type="warning" effect="dark" class="expert-tag">{{ t('ai.selected') }}</el-tag>
              </div>
            </div>
          </div>

          <!-- 专家管理对话框 -->
          <el-dialog
            v-model="showExpertManager"
            :title="t('ai.manageExpert')"
            width="650px"
            :close-on-click-modal="false"
          >
            <div class="expert-manager-list">
              <div v-for="expert in expertList" :key="expert.id" class="expert-manager-item">
                <div class="expert-manager-info">
                  <span class="expert-manager-icon">{{ expert.icon || '🤖' }}</span>
                  <div>
                    <div class="expert-manager-name">{{ expert.name }}</div>
                    <div class="expert-manager-desc">{{ expert.description }}</div>
                  </div>
                </div>
                <div class="expert-manager-actions">
                  <el-tag v-if="expert.isSystem" size="small">{{ t('ai.system') }}</el-tag>
                  <el-button v-if="!expert.isSystem" type="primary" link size="small" @click="editExpert(expert)">{{ t('common.edit') }}</el-button>
                  <el-button v-if="!expert.isSystem" type="danger" link size="small" @click="handleDeleteExpert(expert.id)">{{ t('common.delete') }}</el-button>
                </div>
              </div>
            </div>
            <template #footer>
              <el-button type="primary" @click="showExpertForm = true">{{ t('ai.addExpert') }}</el-button>
              <el-button @click="showExpertManager = false">{{ t('common.close') }}</el-button>
            </template>
          </el-dialog>

          <!-- 专家表单对话框 -->
          <el-dialog
            v-model="showExpertForm"
            :title="editingExpertId ? t('ai.editExpert') : t('ai.addExpert')"
            width="550px"
            :close-on-click-modal="false"
          >
            <el-form :model="expertForm" label-width="100px">
              <el-form-item :label="t('ai.expertName')" required>
                <el-input v-model="expertForm.name" :placeholder="t('ai.expertNamePlaceholder')" />
              </el-form-item>
              <el-form-item :label="t('ai.description')">
                <el-input v-model="expertForm.description" :placeholder="t('ai.descriptionPlaceholder')" />
              </el-form-item>
              <el-form-item :label="t('ai.icon')">
                <el-input v-model="expertForm.icon" :placeholder="t('ai.iconPlaceholder')" />
              </el-form-item>
              <el-form-item :label="t('ai.systemPrompt')" required>
                <el-input
                  v-model="expertForm.systemPrompt"
                  type="textarea"
                  :rows="6"
                  :placeholder="t('ai.systemPromptPlaceholder')"
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button type="primary" :loading="savingExpert" @click="saveExpert">{{ t('common.save') }}</el-button>
              <el-button @click="showExpertForm = false">{{ t('common.cancel') }}</el-button>
            </template>
          </el-dialog>

          <!-- 输入区域 -->
          <div class="ai-chat-input">
            <!-- 上传进度条 -->
            <div v-if="uploadingFile" class="upload-progress">
              <el-progress :percentage="uploadProgress" :show-text="true" striped />
            </div>

            <!-- 当前附件列表 -->
            <div v-if="currentAttachments.length > 0" class="attachment-list">
              <div v-for="(file, index) in currentAttachments" :key="index" class="attachment-item">
                <el-icon size="16" class="attachment-icon"><Document /></el-icon>
                <span class="attachment-name">{{ file.name }}</span>
                <span class="attachment-size">{{ formatFileSize(file.size) }}</span>
                <el-button
                  type="danger"
                  link
                  size="small"
                  @click="removeAttachment(index)"
                  :disabled="uploadingFile || aiThinking"
                >
                  {{ t('common.delete') }}
                </el-button>
              </div>
            </div>

            <div class="input-row">
              <el-upload
                ref="uploadRef"
                :show-file-list="false"
                :http-request="handleChatFileUpload"
                :before-upload="beforeFileUpload"
                :limit="5"
                accept=".txt,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.md,.jpg,.png,.jpeg,.gif,.webp"
                :disabled="uploadingFile || aiThinking"
              >
                <el-button
                  :icon="Paperclip"
                  :loading="uploadingFile"
                  :disabled="aiThinking"
                  circle
                  size="small"
                  :title="t('ai.uploadAttachment')"
                />
              </el-upload>
              <el-input
                v-model="inputMessage"
                :placeholder="t('ai.inputPlaceholder')"
                :rows="1"
                @keyup.enter="sendMessage"
                :disabled="aiThinking || uploadingFile"
              />
              <el-button type="primary" @click="sendMessage" :loading="aiThinking" :disabled="uploadingFile">
                {{ t('ai.send') }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>

  <!-- 后台上传浮动指示器 -->
  <div
    v-if="uploadingFile && uploadMinimized"
    class="upload-float-indicator"
    :style="{ top: floatTop + 'px' }"
    @mousedown.prevent="startDrag"
    @touchstart.prevent="startDrag"
    @click="restoreUploadDialog"
  >
    <el-icon class="upload-float-icon"><Upload /></el-icon>
    <span class="upload-float-percent">{{ uploadProgress }}%</span>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { dateLocale } from '@/i18n'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, DataAnalysis, Box, Service, ArrowDown, Plus,
  CircleCheck, CircleClose, WarningFilled, Cpu, Refresh,
  ChatDotRound, User, Edit, Delete, FolderOpened, Document,
  FolderAdd, Upload, DocumentCopy, Loading, Search, Key, Link, Files, Grid, Operation,
  Bell, Message, InfoFilled, Warning, SuccessFilled, CircleCloseFilled, Paperclip,
  Connection, UploadFilled
} from '@element-plus/icons-vue'
// 引入进度条组件
import { ElProgress } from 'element-plus'
import { logout, updateUserInfo, updatePassword, getCurrentUserInfo } from '@/api/user'
import { generateRandomAvatar } from '@/utils/avatar'
import {
  getActiveMessagesWithStatus,
  getUnreadMessages,
  markAsRead,
  markAllAsRead,
  triggerClusterCheck
} from '@/api/message'
import {
  getNamespaceList, createNamespace, deleteNamespace,
  getDeploymentList, getDeployment, createDeployment, updateDeployment, deleteDeployment,
  scaleDeployment, restartDeployment,
  getServiceList, getService, createService, updateService, deleteService, getServiceStatus,
  getPvcList, createPvc, deletePvc, getPvcStatus, getStorageClassList,
  getIngressList, createIngress, deleteIngress, getIngressStatus,
  getPodList, getPod, deletePod, getPodLogs, getClusterStats,
  createConfigMap,
  getEventList,
  getConfigMapList, deleteConfigMap,
  getSecretList, deleteSecret, createSecret,
  getCrdList, getCrd,
  getJobList, getJob, createJob, deleteJob, getJobLogs,
  getOwnershipRecords
} from '@/api/k8s'
import {
  getUserConversations,
  getConversationHistory,
  deleteConversation,
  updateConversationTitle,
  generateNewConversationId,
  getPresetPrompts,
  getAiExperts,
  createAiExpert,
  updateAiExpert,
  deleteAiExpert
} from '@/api/ai'
import {
  getBucketList,
  getFileListByBucket,
  createFolder,
  uploadFile,
  deleteObject,
  createBucket,
  previewFile
} from '@/api/oss'
import { listRepositories, listTags, getManifest, deleteImage } from '@/api/registry'
import {
  uploadClusterConfig,
  getClusterConfigList,
  getActiveClusterConfig,
  activateClusterConfig,
  deleteClusterConfig,
  testClusterConfig,
  getStatefulSetList,
  getStatefulSet,
  scaleStatefulSet,
  deleteStatefulSet,
  getDaemonSetList,
  getDaemonSet,
  deleteDaemonSet,
  getNodeList,
  getPvList,
  getPv,
  getHpaList,
  getHpa,
  updateHpaMinMax,
  getNetworkPolicyList,
  getNetworkPolicy,
  getResourceQuotaList,
  getResourceQuota,
  createResourceQuota,
  updateResourceQuota,
  deleteResourceQuota,
  getServiceAccountList,
  getServiceAccount,
  deleteServiceAccount,
  getRoleList,
  getRole,
  deleteRole,
  getClusterRoleList,
  getClusterRole,
  deleteClusterRole,
  getRoleBindingList,
  getRoleBinding,
  deleteRoleBinding,
  getClusterRoleBindingList,
  getClusterRoleBinding,
  deleteClusterRoleBinding
} from '@/api/k8s'
// 引入Markdown解析库
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css' // 代码高亮主题

const router = useRouter()
const { t } = useI18n({ useScope: 'global' })
const activeMenu = ref('overview')
const showCreateNamespaceDialog = ref(false)
const showCreateResourceDialog = ref(false)
const currentResourceType = ref('deployment')
const createLoading = ref(false)
const currentNamespace = ref('default')

// Pod管理相关变量
const podList = ref([])
const podLoading = ref(false)
const podNamespaceFilter = ref('all') // 用all代替空字符串，避免Element匹配bug
const showPodDetailDialog = ref(false)
const showPodLogsDialog = ref(false)
const selectedPod = ref(null)
const podLogsContent = ref('')
const activePodTab = ref('podBase')

// Deployment管理相关变量
const deploymentList = ref([])
const deploymentLoading = ref(false)
const deploymentNamespaceFilter = ref('all')
const showDeploymentDetailDialog = ref(false)
const showScaleDialog = ref(false)
const selectedDeployment = ref(null)
const deploymentDetail = ref(null)
const activeDetailTab = ref('deployBase') // 详情页当前激活的标签页
const scaleForm = reactive({
  replicas: 1
})

// Service管理相关变量
const serviceList = ref([])
const serviceLoading = ref(false)
const serviceNamespaceFilter = ref('all')
const showServiceDetailDialog = ref(false)
const selectedService = ref(null)
const serviceDetail = ref(null)
const serviceStatus = ref({})

// PVC存储卷管理相关变量
const pvcList = ref([])
const pvcLoading = ref(false)
const storageClassList = ref([])
const storageClassLoading = ref(false)
const pvcNamespaceFilter = ref('all')
const showPvcDetailDialog = ref(false)
const selectedPvc = ref(null)
const pvcDetail = ref(null)
const pvcStatus = ref({})

// Ingress路由管理相关变量
const ingressList = ref([])
const ingressLoading = ref(false)
const ingressNamespaceFilter = ref('all')
const showIngressDetailDialog = ref(false)
const selectedIngress = ref(null)
const ingressDetail = ref(null)
const activeRuleIndex = ref(0) // 详情页路由规则折叠面板索引

// 定时刷新定时器
const refreshTimer = ref(null)

// AI助手相关
const showAIChat = ref(false)
const messageList = ref([
  { role: 'assistant', content: t('ai.welcome') }
])
const inputMessage = ref('')
const aiThinking = ref(false)
const controller = ref(null)
const chatMessagesRef = ref(null)
const conversationId = ref(null) // 保存会话ID
const conversationList = ref([]) // 用户对话列表
const editingTitle = ref(null) // 正在编辑标题的对话ID
const newTitle = ref('') // 新标题输入
const activeExpert = ref(null) // 当前选中的专家
const expertList = ref([]) // 专家列表
const showExpertList = ref(true) // 控制专家列表展开收起
const showExpertManager = ref(false) // 显示专家管理对话框
const showExpertForm = ref(false) // 显示专家表单对话框
const editingExpertId = ref(null) // 编辑中的专家ID
const savingExpert = ref(false) // 保存专家中
const expertForm = reactive({
  name: '',
  description: '',
  icon: '',
  systemPrompt: ''
}) // 专家表单
const currentAttachments = ref([]) // 当前待发送的附件列表

// 镜像管理相关变量
const registryRepos = ref([]) // 仓库列表
const registrySearchKey = ref('') // 搜索关键字
const filteredRegistryRepos = computed(() => {
  if (!registrySearchKey.value) return registryRepos.value
  return registryRepos.value.filter(repo =>
    repo.toLowerCase().includes(registrySearchKey.value.toLowerCase())
  )
})
const selectedRepo = ref('') // 当前选中的仓库
const currentRepoData = ref(null) // 当前仓库的tags数据
const selectedTag = ref('') // 当前选中的tag
const registryManifest = ref(null) // manifest详情
const registryLoading = ref(false) // 加载状态
const manifestLoading = ref(false) // manifest加载状态
const showManifestDialog = ref(false) // manifest详情弹窗

// ConfigMap相关变量
const configMapList = ref([])
const configMapLoading = ref(false)
const configMapNamespaceFilter = ref('default')

// Secret相关变量
const secretList = ref([])
const secretLoading = ref(false)
const secretNamespaceFilter = ref('default')

// Job相关变量
const jobList = ref([])
const jobLoading = ref(false)
const jobNamespaceFilter = ref('default')
const showJobDetailDialog = ref(false)
const jobDetail = ref(null)
const selectedJob = ref(null)
const activeJobTab = ref('jobBase')
const showJobLogsDialog = ref(false)
const jobLogs = ref({})
const activeJobLogTab = ref('')

// CRD相关变量
const crdList = ref([])
const crdLoading = ref(false)
const showCrdDetailDialog = ref(false)
const crdDetail = ref(null)
const activeCrdTab = ref('crdBase')
const activeCrdVersions = ref([0])

// Event相关变量
const eventList = ref([])
const eventLoading = ref(false)
const eventNamespaceFilter = ref('all')

// OSS相关变量
const bucketList = ref([]) // Bucket列表
const selectedBucket = ref('') // 当前选中的Bucket
const currentPrefix = ref('') // 当前文件夹前缀
const ossFileList = ref([]) // 文件列表
const ossLoading = ref(false) // 加载状态
const selectedOssFiles = ref([]) // 选中的文件/文件夹
const ossTableRef = ref(null) // OSS表格实例
const showCreateFolderDialog = ref(false) // 新建文件夹弹窗
const newFolderName = ref('') // 新文件夹名称
const creatingFolder = ref(false) // 创建文件夹中
const showPreviewDialog = ref(false) // 图片预览弹窗
const previewImageUrl = ref('') // 预览图片URL
const previewImageName = ref('') // 预览图片名称
const showTextPreviewDialog = ref(false) // 文本预览弹窗
const previewTextContent = ref('') // 预览文本内容
const previewTextName = ref('') // 预览文本文件名
const uploadingFile = ref(false) // 上传文件中
const uploadProgress = ref(0) // 上传进度百分比
const uploadingFileName = ref('') // 正在上传的文件名
const uploadMinimized = ref(false) // 上传进度是否收起
const floatTop = ref(80) // 浮动指示器top位置
const fileInputRef = ref(null) // 文件上传input引用

// 上传弹窗是否显示
const showUploadDialog = computed(() => uploadingFile.value && !uploadMinimized.value)

// 拖动相关
let isDragging = false
let hasMoved = false
let dragStartY = 0
let dragStartTop = 0

const startDrag = (e) => {
  isDragging = true
  hasMoved = false
  dragStartY = e.touches ? e.touches[0].clientY : e.clientY
  dragStartTop = floatTop.value
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, { passive: false })
  document.addEventListener('touchend', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging) return
  const clientY = e.touches ? e.touches[0].clientY : e.clientY
  const deltaY = clientY - dragStartY
  if (Math.abs(deltaY) > 3) hasMoved = true
  const newTop = dragStartTop + deltaY
  const maxTop = window.innerHeight - 60
  floatTop.value = Math.max(10, Math.min(newTop, maxTop))
}

const stopDrag = () => {
  isDragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

const restoreUploadDialog = () => {
  if (!hasMoved) {
    uploadMinimized.value = false
  }
  hasMoved = false
}
const showCreateBucketDialog = ref(false) // 创建Bucket弹窗
const newBucketName = ref('') // 新Bucket名称
const creatingBucket = ref(false) // 创建Bucket中

// 面包屑分段
const breadcrumbParts = computed(() => {
  if (!currentPrefix.value) return []
  return currentPrefix.value.replace(/\/$/, '').split('/').filter(p => p)
})

// 配置Markdown解析
marked.setOptions({
  highlight: function (code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  },
  breaks: true, // 支持换行
  gfm: true // 支持GitHub Flavored Markdown
})

// 最轻量 HTML 转义，用于降级路径，保证 v-html 安全
const escapeHtml = (s) => String(s)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

// Markdown 解析函数：永远返回 string，绝不让 v-html 拿到原始 markdown 或 Promise
// （marked v5+ 在某些情况仍可能返回 Promise；此时降级为简单换行处理）
const parseMarkdown = (content) => {
  if (!content) return ''
  try {
    const result = marked.parse(content, { async: false })
    if (typeof result === 'string' && result.length > 0) return result
    // 降级：marked 返回了空串或非 string（罕见），做最基本的安全转义 + 换行
    return escapeHtml(content).replace(/\n/g, '<br>')
  } catch (e) {
    console.error('Markdown解析失败:', e)
    return escapeHtml(content).replace(/\n/g, '<br>')
  }
}

// 获取会话ID
const getConversationId = async () => {
  if (conversationId.value) return conversationId.value

  try {
    const res = await generateNewConversationId()
    conversationId.value = res.data
    return conversationId.value
  } catch (error) {
    console.error('获取会话ID失败:', error)
    throw error
  }
}

// 加载用户对话列表
const loadConversationList = async () => {
  try {
    const res = await getUserConversations()
    conversationList.value = res.data || []
  } catch (error) {
    console.error('加载对话列表失败:', error)
  }
}

// 创建新对话
const createNewConversation = async () => {
  try {
    conversationId.value = null
    messageList.value = [
      { role: 'assistant', content: t('ai.welcome'), renderedContent: parseMarkdown(t('ai.welcome')) }
    ]
    await getConversationId()
    await loadConversationList()
    // 等待DOM更新后滚动
    nextTick(scrollChatToBottom)
  } catch (error) {
    ElMessage.error(t('ai.newChatFailed'))
  }
}

// 加载专家列表
const loadExpertList = async () => {
  try {
    const res = await getAiExperts()
    expertList.value = res.data || []
  } catch (error) {
    console.error('加载专家列表失败:', error)
  }
}

// 选中专家
const selectExpert = (expert) => {
  if (activeExpert.value?.id === expert.id) {
    deselectExpert()
  } else {
    activeExpert.value = expert
    const msgText = t('ai.switchedExpert', { name: expert.name })
    messageList.value.push({
      role: 'assistant',
      content: msgText,
      renderedContent: parseMarkdown(msgText)
    })
    scrollChatToBottom()
  }
}

// 取消选中专家
const deselectExpert = () => {
  activeExpert.value = null
  const msgText = t('ai.switchedNormal')
  messageList.value.push({
    role: 'assistant',
    content: msgText,
    renderedContent: parseMarkdown(msgText)
  })
  scrollChatToBottom()
}

// 打开编辑专家
const editExpert = (expert) => {
  editingExpertId.value = expert.id
  expertForm.name = expert.name
  expertForm.description = expert.description || ''
  expertForm.icon = expert.icon || ''
  expertForm.systemPrompt = expert.systemPrompt || ''
  showExpertForm.value = true
}

// 保存专家
const saveExpert = async () => {
  if (!expertForm.name || !expertForm.systemPrompt) {
    ElMessage.warning(t('ai.expertNameRequired'))
    return
  }
  savingExpert.value = true
  try {
    if (editingExpertId.value) {
      await updateAiExpert(editingExpertId.value, expertForm)
      ElMessage.success(t('ai.expertUpdated'))
    } else {
      await createAiExpert(expertForm)
      ElMessage.success(t('ai.expertCreated'))
    }
    showExpertForm.value = false
    resetExpertForm()
    await loadExpertList()
  } catch (error) {
    console.error('保存专家失败:', error)
  } finally {
    savingExpert.value = false
  }
}

// 删除专家
const handleDeleteExpert = async (id) => {
  try {
    await ElMessageBox.confirm(t('ai.expertDeleteConfirm'), t('common.tip'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteAiExpert(id)
    ElMessage.success(t('ai.expertDeleted'))
    if (activeExpert.value?.id === id) {
      activeExpert.value = null
    }
    await loadExpertList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除专家失败:', error)
      ElMessage.error(t('ai.expertDeleteFailed'))
    }
  }
}

// 重置专家表单
const resetExpertForm = () => {
  editingExpertId.value = null
  expertForm.name = ''
  expertForm.description = ''
  expertForm.icon = ''
  expertForm.systemPrompt = ''
}

// 附件上传前校验
const beforeFileUpload = (file) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error(t('ai.fileTooLarge'))
    return false
  }
  if (currentAttachments.value.length >= 5) {
    ElMessage.error(t('ai.tooManyFiles'))
    return false
  }
  return true
}

// 自定义上传处理（使用现有oss API）
const handleChatFileUpload = async (options) => {
  const { file, onProgress, onSuccess, onError } = options
  uploadingFile.value = true
  uploadProgress.value = 0

  try {
    const res = await uploadFile(file, 'dssp', 'chat-attachments', (progressEvent) => {
      const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
      uploadProgress.value = percent
      onProgress({ percent })
    })

    if (res.code === 200 || res.code === 0) {
      const fileUrl = res.data?.url || res.data
      currentAttachments.value.push({
        name: file.name,
        size: file.size,
        url: fileUrl,
        type: file.raw?.type || file.type
      })
      ElMessage.success(t('oss.uploadSuccess', { name: file.name }))
      onSuccess(res, file)
    } else {
      ElMessage.error(res.message || t('ai.uploadFailed'))
      onError(new Error(res.message || t('ai.uploadFailed')))
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error(t('ai.uploadRetry'))
    onError(error)
  } finally {
    uploadingFile.value = false
    uploadProgress.value = 0
  }
}

// 删除附件
const removeAttachment = (index) => {
  currentAttachments.value.splice(index, 1)
}


// 加载Bucket列表
const loadBucketList = async () => {
  try {
    const res = await getBucketList()
    bucketList.value = res.data || []
    // 默认选中第一个bucket
    if (bucketList.value.length > 0 && !selectedBucket.value) {
      selectedBucket.value = bucketList.value[0]
      await loadFileList(selectedBucket.value)
    }
  } catch (error) {
    console.error('加载Bucket列表失败:', error)
    ElMessage.error(t('oss.loadBucketsFailed'))
  }
}

// 加载文件列表
const loadFileList = async (bucketName) => {
  if (!bucketName) return
  ossLoading.value = true
  try {
    const prefix = currentPrefix.value || undefined
    const res = await getFileListByBucket(bucketName, prefix)
    ossFileList.value = res.data || []
  } catch (error) {
    console.error('加载文件列表失败:', error)
    ElMessage.error(t('oss.loadFilesFailed'))
  } finally {
    ossLoading.value = false
  }
}

// 选择Bucket
const selectBucket = async (bucketName) => {
  selectedBucket.value = bucketName
  currentPrefix.value = ''
  ossSearchKey.value = ''
  ossTableRef.value?.clearSelection()
  await loadFileList(bucketName)
}

// 点击文件夹进入子目录，传空字符串回到根目录
const navigateToFolder = async (target) => {
  if (!target) {
    currentPrefix.value = ''
  } else {
    let name = target.endsWith('/') ? target : target + '/'
    currentPrefix.value = currentPrefix.value ? currentPrefix.value + name : name
  }
  ossSearchKey.value = ''
  ossTableRef.value?.clearSelection()
  await loadFileList(selectedBucket.value)
}

// 面包屑导航，跳转到指定层级
const navigateToBreadcrumb = async (index) => {
  const parts = breadcrumbParts.value
  currentPrefix.value = parts.slice(0, index + 1).join('/') + '/'
  ossSearchKey.value = ''
  ossTableRef.value?.clearSelection()
  await loadFileList(selectedBucket.value)
}

// OSS页面目录搜索与拖拽上传
const ossSearchKey = ref('')
const ossDragover = ref(false)
const ossFilteredFiles = computed(() => {
  const key = ossSearchKey.value.trim().toLowerCase()
  if (!key) return ossFileList.value
  return ossFileList.value.filter(f => f.name.toLowerCase().includes(key))
})

// 文件夹名去掉结尾斜杠展示
const trimFolderName = (name) => (name || '').replace(/\/+$/, '')

// 拖拽文件到当前目录上传
const handleOssDrop = async (event) => {
  ossDragover.value = false
  if (!selectedBucket.value) return
  const files = Array.from(event.dataTransfer?.files || [])
  if (!files.length) return
  let successCount = 0
  for (const f of files) {
    uploadingFile.value = true
    uploadingFileName.value = f.name
    uploadProgress.value = 0
    try {
      await uploadFile(f, selectedBucket.value, currentPrefix.value, (progressEvent) => {
        if (progressEvent.total) {
          uploadProgress.value = Math.round((progressEvent.loaded / progressEvent.total) * 100)
        }
      })
      successCount++
    } catch (error) {
      console.error('上传文件失败:', f.name, error)
      ElMessage.error(t('oss.uploadFailed') + ': ' + f.name)
    }
  }
  uploadingFile.value = false
  uploadingFileName.value = ''
  uploadProgress.value = 0
  if (successCount > 0) {
    ElMessage.success(t('oss.uploadBatchDone', { n: successCount }))
  }
  await loadFileList(selectedBucket.value)
}

// 新建文件夹
const handleCreateFolder = async () => {
  const name = newFolderName.value.trim()
  if (!name) {
    ElMessage.warning(t('oss.folderNameRequired'))
    return
  }
  creatingFolder.value = true
  try {
    await createFolder(selectedBucket.value, currentPrefix.value, name)
    ElMessage.success(t('oss.folderCreated', { name }))
    showCreateFolderDialog.value = false
    newFolderName.value = ''
    await loadFileList(selectedBucket.value)
  } catch (error) {
    console.error('创建文件夹失败:', error)
    ElMessage.error(t('oss.createFolderFailed'))
  } finally {
    creatingFolder.value = false
  }
}

// 创建Bucket
const handleCreateBucket = async () => {
  const name = newBucketName.value.trim()
  if (!name) {
    ElMessage.warning(t('oss.bucketNameRequired'))
    return
  }
  creatingBucket.value = true
  try {
    await createBucket(name)
    ElMessage.success(t('oss.bucketCreated', { name }))
    showCreateBucketDialog.value = false
    newBucketName.value = ''
    await loadBucketList()
    if (!selectedBucket.value) {
      selectedBucket.value = name
      await loadFileList(name)
    }
  } catch (error) {
    console.error('创建Bucket失败:', error)
    ElMessage.error(t('oss.createBucketFailed'))
  } finally {
    creatingBucket.value = false
  }
}

// 触发文件选择
const triggerUpload = () => {
  fileInputRef.value?.click()
}

// 处理文件上传
const handleFileUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  uploadingFile.value = true
  uploadingFileName.value = file.name
  uploadProgress.value = 0
  try {
    await uploadFile(file, selectedBucket.value, currentPrefix.value, (progressEvent) => {
      if (progressEvent.total) {
        uploadProgress.value = Math.round((progressEvent.loaded / progressEvent.total) * 100)
      }
    })
    ElMessage.success(t('oss.uploadSuccess', { name: file.name }))
    await loadFileList(selectedBucket.value)
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error(t('oss.uploadFailed'))
  } finally {
    uploadingFile.value = false
    uploadingFileName.value = ''
    uploadProgress.value = 0
    uploadMinimized.value = false
    floatTop.value = 80
    event.target.value = ''
  }
}

// 删除对象
const handleDeleteObject = async (objectName, isFolder) => {
  const label = `${isFolder ? t('oss.folder') : t('oss.file')} "${objectName}"`
  try {
    await ElMessageBox.confirm(t('oss.deleteConfirm', { label }), t('common.confirmDelete'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteObject(selectedBucket.value, currentPrefix.value, objectName)
    ElMessage.success(t('common.deleteSuccess'))
    await loadFileList(selectedBucket.value)
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error(t('common.deleteFailed'))
  }
}

// OSS选中变化处理
const handleOssSelectionChange = (selection) => {
  selectedOssFiles.value = selection
}

// 批量删除对象
const handleBatchDelete = async () => {
  if (selectedOssFiles.value.length === 0) {
    ElMessage.warning(t('oss.selectToDelete'))
    return
  }

  const fileCount = selectedOssFiles.value.filter(item => !item.folder).length
  const folderCount = selectedOssFiles.value.filter(item => item.folder).length

  // 构建确认提示文本
  let confirmText = ''
  if (folderCount > 0 && fileCount > 0) {
    confirmText = t('oss.deleteSelectedConfirm', { folders: folderCount, files: fileCount })
  } else if (folderCount > 0) {
    confirmText = t('oss.deleteFoldersConfirm', { folders: folderCount })
  } else {
    confirmText = t('oss.deleteFilesConfirm', { files: fileCount })
  }

  try {
    await ElMessageBox.confirm(confirmText, t('oss.batchDeleteTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    // 批量调用删除接口
    const deletePromises = selectedOssFiles.value.map(item =>
      deleteObject(selectedBucket.value, currentPrefix.value, item.name)
    )
    await Promise.all(deletePromises)

    ElMessage.success(t('oss.batchDeleteSuccess', { n: selectedOssFiles.value.length }))
    // 清空选中状态
    selectedOssFiles.value = []
    ossTableRef.value?.clearSelection()
    // 刷新列表
    await loadFileList(selectedBucket.value)
  } catch (error) {
    console.error('批量删除失败:', error)
    ElMessage.error(t('oss.batchDeleteFailed'))
    // 刷新列表显示最新状态
    await loadFileList(selectedBucket.value)
  }
}

// 判断是否为图片文件
const isImageFile = (name) => {
  if (!name) return false
  const ext = name.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
}

const isTextFile = (name) => {
  if (!name) return false
  const ext = name.split('.').pop().toLowerCase()
  return ['yml', 'yaml', 'json', 'txt', 'xml', 'properties', 'env', 'conf', 'cfg', 'ini', 'log', 'md', 'sh', 'py', 'java', 'js', 'ts', 'css', 'html', 'sql', 'csv', 'toml'].includes(ext)
}

// 预览图片
const previewImage = (url, name) => {
  previewImageUrl.value = url
  previewImageName.value = name
  showPreviewDialog.value = true
}

// 预览文本文件
const previewTextFile = async (objectName) => {
  try {
    const res = await previewFile(selectedBucket.value, currentPrefix.value, objectName)
    previewTextContent.value = res.data || ''
    previewTextName.value = objectName
    showTextPreviewDialog.value = true
  } catch (error) {
    console.error('预览文本文件失败:', error)
    ElMessage.error(t('oss.previewFailed'))
  }
}

// 复制下载链接
const copyDownloadUrl = async (url) => {
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success(t('oss.copyLink'))
  } catch {
    ElMessage.error(t('oss.copyFailed'))
  }
}

// 打开下载链接
const openDownloadUrl = (url) => {
  window.open(url, '_blank')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 加载镜像仓库列表
const loadRegistryRepos = async () => {
  registryLoading.value = true
  try {
    const res = await listRepositories()
    registryRepos.value = res.data || []
    // 自动选中第一个仓库
    if (!selectedRepo.value && registryRepos.value.length) {
      await selectRepo(registryRepos.value[0])
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error)
    ElMessage.error(t('registry.loadReposFailed'))
  } finally {
    registryLoading.value = false
  }
}

// 加载ConfigMap列表
const loadConfigMapList = async () => {
  configMapLoading.value = true
  try {
    const res = await getConfigMapList(configMapNamespaceFilter.value)
    configMapList.value = res.data || []
  } catch (error) {
    console.error('加载ConfigMap列表失败:', error)
    ElMessage.error(t('configmap.loadFailed'))
  } finally {
    configMapLoading.value = false
  }
}

const viewConfigMapDetail = (cm) => {
  const data = cm.data || {}
  const content = Object.entries(data).map(([k, v]) => `${k} = ${v}`).join('\n')
  ElMessageBox.alert(content, `ConfigMap: ${cm.metadata.name}`, {
    confirmButtonText: t('common.close'),
    customClass: 'configmap-detail-msgbox'
  }).catch(() => {})
}

const handleDeleteConfigMap = async (cm) => {
  try {
    await ElMessageBox.confirm(
      t('configmap.confirmDelete', { name: cm.metadata.name }),
      t('common.confirmDelete'),
      { type: 'warning' }
    )
    await deleteConfigMap(cm.metadata.namespace, cm.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadConfigMapList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除ConfigMap失败:', error)
      ElMessage.error(t('configmap.deleteFailed'))
    }
  }
}

// 加载Secret列表
const loadSecretList = async () => {
  secretLoading.value = true
  try {
    const res = await getSecretList(secretNamespaceFilter.value)
    secretList.value = res.data || []
  } catch (error) {
    console.error('加载Secret列表失败:', error)
    ElMessage.error(t('secret.loadFailed'))
  } finally {
    secretLoading.value = false
  }
}

const viewSecretDetail = (secret) => {
  const data = secret.data || {}
  const decoded = Object.entries(data).map(([k, v]) => {
    try {
      return `${k} = ${atob(v)}`
    } catch (_) {
      return `${k} = ${v}`
    }
  }).join('\n')
  ElMessageBox.alert(decoded, `Secret: ${secret.metadata.name}`, {
    confirmButtonText: t('common.close'),
    customClass: 'configmap-detail-msgbox'
  }).catch(() => {})
}

const handleDeleteSecret = async (secret) => {
  try {
    await ElMessageBox.confirm(
      t('secret.confirmDelete', { name: secret.metadata.name }),
      t('common.confirmDelete'),
      { type: 'warning' }
    )
    await deleteSecret(secret.metadata.namespace, secret.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadSecretList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Secret失败:', error)
      ElMessage.error(t('secret.deleteFailed'))
    }
  }
}

// 加载CRD列表
const loadCrdList = async () => {
  crdLoading.value = true
  try {
    const res = await getCrdList()
    crdList.value = res.data || []
  } catch (error) {
    console.error('加载CRD列表失败:', error)
    ElMessage.error(t('crd.loadFailed'))
  } finally {
    crdLoading.value = false
  }
}

const viewCrdDetail = async (crd) => {
  try {
    const res = await getCrd(crd.metadata.name)
    crdDetail.value = res.data
  } catch (error) {
    console.error('获取CRD详情失败:', error)
    crdDetail.value = crd
  }
  showCrdDetailDialog.value = true
}

// 加载Job列表
const loadJobList = async () => {
  jobLoading.value = true
  try {
    const res = await getJobList(jobNamespaceFilter.value)
    jobList.value = res.data || []
  } catch (error) {
    console.error('加载Job列表失败:', error)
    ElMessage.error(t('job.loadFailed'))
  } finally {
    jobLoading.value = false
  }
}

const viewJobDetail = async (job) => {
  selectedJob.value = job
  jobDetail.value = null
  activeJobTab.value = 'jobBase'
  try {
    const res = await getJob(job.metadata.namespace, job.metadata.name)
    jobDetail.value = res.data || {}
  } catch (error) {
    console.error('获取Job详情失败:', error)
    ElMessage.error(t('job.getDetailFailed'))
    return
  }
  showJobDetailDialog.value = true
}

const handleDeleteJob = async (job) => {
  try {
    await ElMessageBox.confirm(
      t('job.confirmDelete', { name: job.metadata.name }),
      t('common.confirmDelete'),
      { type: 'warning' }
    )
    await deleteJob(job.metadata.namespace, job.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadJobList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Job失败:', error)
      ElMessage.error(t('job.deleteFailed'))
    }
  }
}

const viewJobLogs = async (job) => {
  selectedJob.value = job
  jobLogs.value = {}
  activeJobLogTab.value = ''
  try {
    const res = await getJobLogs(job.metadata.namespace, job.metadata.name)
    jobLogs.value = res.data || {}
    const keys = Object.keys(jobLogs.value)
    if (keys.length > 0) {
      activeJobLogTab.value = keys[0]
    }
  } catch (error) {
    console.error('获取Job日志失败:', error)
    ElMessage.error(t('job.getLogsFailed'))
    return
  }
  showJobLogsDialog.value = true
}

// 卷映射相关（Job/Deployment共用）
const loadVolResources = async (namespace) => {
  if (!namespace) return
  loadingVolResources.value = true
  try {
    const [cmRes, secRes, pvcRes] = await Promise.all([
      getConfigMapList(namespace),
      getSecretList(namespace),
      getPvcList(namespace)
    ])
    volConfigMaps.value = (cmRes.data || []).map(c => c.metadata?.name).filter(Boolean)
    volSecrets.value = (secRes.data || []).map(s => s.metadata?.name).filter(Boolean)
    volPvcs.value = (pvcRes.data || []).map(p => p.metadata?.name).filter(Boolean)
  } catch (error) {
    console.error('加载卷资源列表失败:', error)
  } finally {
    loadingVolResources.value = false
  }
}

const onVolTypeChange = (vm) => {
  vm.name = ''
}

const addJobVolumeMount = async () => {
  if (!jobForm.volumeMounts.length) {
    await loadVolResources(jobForm.namespace)
  }
  jobForm.volumeMounts.push({ type: 'configMap', name: '', mountPath: '' })
}

const removeJobVolumeMount = (index) => {
  jobForm.volumeMounts.splice(index, 1)
}

const addDeployVolumeMount = async () => {
  if (!deploymentForm.volumeMounts.length) {
    await loadVolResources(deploymentForm.namespace)
  }
  deploymentForm.volumeMounts.push({ type: 'configMap', name: '', mountPath: '' })
}

const removeDeployVolumeMount = (index) => {
  deploymentForm.volumeMounts.splice(index, 1)
}

// 加载Event列表
const loadEventList = async () => {
  eventLoading.value = true
  try {
    const ns = eventNamespaceFilter.value === 'all' ? null : eventNamespaceFilter.value
    const res = await getEventList(ns)
    // 按最后出现时间从新到旧排序
    eventList.value = (res.data || []).sort((a, b) => {
      const ta = new Date(a.lastTimestamp || a.eventTime?.time || 0).getTime()
      const tb = new Date(b.lastTimestamp || b.eventTime?.time || 0).getTime()
      return tb - ta
    })
  } catch (error) {
    console.error('加载Event列表失败:', error)
    ElMessage.error(t('event.loadFailed'))
  } finally {
    eventLoading.value = false
  }
}

// 选择仓库
const selectRepo = async (repo) => {
  selectedRepo.value = repo
  selectedTag.value = ''
  registryManifest.value = null
  try {
    const res = await listTags(repo)
    currentRepoData.value = res.data
  } catch (error) {
    console.error('加载Tags失败:', error)
    ElMessage.error(t('registry.loadTagsFailed'))
  }
}

// 选择tag
const selectTag = async (tag) => {
  selectedTag.value = tag
  manifestLoading.value = true
  try {
    const res = await getManifest(selectedRepo.value, tag)
    registryManifest.value = res.data
    showManifestDialog.value = true
  } catch (error) {
    console.error('加载Manifest失败:', error)
    ElMessage.error(t('registry.loadManifestFailed'))
  } finally {
    manifestLoading.value = false
  }
}

// 复制完整镜像路径
const copyImagePath = (tag) => {
  const host = currentRepoData.value?.registryHost || ''
  const fullPath = host ? `${host}/${selectedRepo.value}:${tag}` : `${selectedRepo.value}:${tag}`
  navigator.clipboard.writeText(fullPath).then(() => {
    ElMessage.success(t('registry.copyPath', { path: fullPath }))
  }).catch(() => {
    ElMessage.error(t('oss.copyFailed'))
  })
}

// 删除tag
const handleDeleteTag = async (tag) => {
  try {
    await deleteImage(selectedRepo.value, tag)
    ElMessage.success(t('common.deleteSuccess'))
    // 刷新tags列表
    const res = await listTags(selectedRepo.value)
    currentRepoData.value = res.data
    if (selectedTag.value === tag) {
      selectedTag.value = ''
      registryManifest.value = null
    }
  } catch (error) {
    console.error('删除tag失败:', error)
    ElMessage.error(t('registry.deleteTagFailed'))
  }
}

// 切换对话
const switchConversation = async (convId) => {
  if (convId === conversationId.value) return

  try {
    conversationId.value = convId
    // 加载对话历史
    const res = await getConversationHistory(convId)
    const messages = res.data || []
    // 转换消息格式
    messageList.value = messages.map(msg => {
      const text = msg.text || ''
      return {
        role: msg.messageType === 'USER' ? 'user' : 'assistant',
        content: text,
        renderedContent: parseMarkdown(text)
      }
    })
    // 如果没有历史消息，显示欢迎语
    if (messageList.value.length === 0) {
      const welcome = t('ai.welcome')
      messageList.value = [
        { role: 'assistant', content: welcome, renderedContent: parseMarkdown(welcome) }
      ]
    }
    // 等待DOM更新后滚动
    nextTick(scrollChatToBottom)
    // 再加一次保险
    setTimeout(scrollChatToBottom, 100)
  } catch (error) {
    ElMessage.error(t('ai.historyFailed'))
  }
}

// 删除对话
const deleteConversationItem = async (convId) => {
  try {
    await ElMessageBox.confirm(t('ai.deleteChatConfirm'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    await deleteConversation(convId)
    ElMessage.success(t('common.deleteSuccess'))

    // 如果删除的是当前对话，切换到第一个对话或者创建新对话
    if (convId === conversationId.value) {
      conversationId.value = null
      if (conversationList.value.length > 1) {
        const firstConv = conversationList.value.find(c => c.id !== convId)
        if (firstConv) {
          await switchConversation(firstConv.id)
        } else {
          await createNewConversation()
        }
      } else {
        await createNewConversation()
      }
    }

    // 重新加载列表
    await loadConversationList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.deleteFailed'))
    }
  }
}

// 输入框ref
const titleInput = ref(null)

// 开始编辑标题
const startEditTitle = (conv) => {
  editingTitle.value = conv.id
  newTitle.value = conv.title || ''
  nextTick(() => {
    // 聚焦输入框
    if (titleInput.value) {
      titleInput.value.focus()
    }
  })
}

// 保存标题
const saveTitle = async (convId) => {
  if (!newTitle.value.trim()) {
    editingTitle.value = null
    return
  }

  try {
    await updateConversationTitle(convId, newTitle.value.trim())
    ElMessage.success(t('ai.titleUpdated'))
    // 更新本地列表
    const conv = conversationList.value.find(c => c.id === convId)
    if (conv) {
      conv.title = newTitle.value.trim()
    }
  } catch (error) {
    ElMessage.error(t('ai.titleUpdateFailed'))
  } finally {
    editingTitle.value = null
    newTitle.value = ''
  }
}

// 用户信息
const userInfo = reactive(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 资源归属（创建人）查询
const ownerMap = ref({}) // key: "resourceType|namespace|name" -> { creatorId, creatorName }
const loadOwnerMap = async () => {
  try {
    const res = await getOwnershipRecords()
    const map = {}
    ;(res.data || []).forEach(r => {
      const key = `${r.resourceType}|${r.namespace || ''}|${r.resourceName}`
      map[key] = { creatorId: r.creatorId, creatorName: r.creatorName }
    })
    ownerMap.value = map
  } catch (e) {
    console.error('加载资源归属数据失败:', e)
  }
}
const getOwnerName = (resourceType, namespace, name) => {
  const key = `${resourceType}|${namespace || ''}|${name}`
  return ownerMap.value[key]?.creatorName || '-'
}

// 用户信息维护相关变量
const showUserProfileDialog = ref(false)
const userProfileActiveTab = ref('info')
const userProfileLoading = ref(false)
const passwordLoading = ref(false)
const userProfileForm = reactive({
  username: '',
  avatar: '',
  email: '',
  phone: ''
})
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 收件箱相关变量
const showInboxDialog = ref(false)
const siteMessageList = ref([])
const readMessageIds = ref([])
const unreadCount = computed(() => {
  return siteMessageList.value.filter(msg => !readMessageIds.value.includes(msg.id)).length
})

// 统计数据
const k8sStats = reactive({
  // 核心资源统计
  namespaceCount: 0,
  podRunningCount: 0,
  podTotalCount: 0,
  serviceCount: 0,
  pvcCount: 0,
  deploymentCount: 0,
  ingressCount: 0,
  // 节点统计
  nodeCount: 0,
  readyNodeCount: 0,
  // 资源使用详情
  cpuUsage: 0,
  cpuUsed: t('overview.cpuZero'),
  cpuTotal: t('overview.cpuZero'),
  memoryUsage: 0,
  memoryUsed: '0Gi',
  memoryTotal: '0Gi',
  storageUsage: 0,
  storageUsed: '0Gi',
  storageTotal: '0Gi',
  // 健康状态
  controlPlaneHealth: 'normal', // normal/warning/error
  nodeHealth: 'normal',
  networkHealth: 'warning',
  storageHealth: 'normal',
  // 异常数量
  abnormalNodeCount: 0,
  abnormalPodCount: 0,
  abnormalVolumeCount: 0
})

// 命名空间列表
const namespaceList = ref([])

// 命名空间表单
const namespaceForm = reactive({
  name: ''
})

// Deployment表单
const deploymentForm = reactive({
  namespace: 'default',
  name: '',
  image: '',
  replicas: 1,
  containerPort: '80',
  cpuLimit: '1000m',
  memoryLimit: '1Gi',
  env: '',
  volumeMounts: []
})

// Service表单
const serviceForm = reactive({
  namespace: 'default',
  name: '',
  type: 'ClusterIP',
  port: '80',
  targetPort: '80',
  nodePort: '',
  selector: ''
})

// PVC表单
const pvcForm = reactive({
  namespace: 'default',
  name: '',
  storageClass: 'standard',
  storage: '10Gi',
  accessModes: ['ReadWriteOnce']
})

// Ingress表单
const ingressForm = reactive({
  namespace: 'default',
  name: '',
  host: '',
  path: '/',
  serviceName: '',
  servicePort: '80',
  enableTls: false,
  tlsSecretName: ''
})

const configMapForm = reactive({
  namespace: 'default',
  name: '',
  dataText: ''
})

const secretForm = reactive({
  namespace: 'default',
  name: '',
  type: 'Opaque',
  dataText: ''
})

const jobForm = reactive({
  namespace: 'default',
  name: '',
  image: '',
  completions: 1,
  parallelism: 1,
  restartPolicy: 'Never',
  command: '',
  cpuLimit: '1000m',
  memoryLimit: '1Gi',
  env: '',
  volumeMounts: []
})
const volConfigMaps = ref([])
const volSecrets = ref([])
const volPvcs = ref([])
const loadingVolResources = ref(false)

// 菜单选择
const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key === 'namespace') {
    loadNamespaceList()
  } else if (key === 'pod') {
    loadPodList()
  } else if (key === 'deployment') {
    loadDeploymentList()
  } else if (key === 'job') {
    loadJobList()
    loadNamespaceList()
  } else if (key === 'service') {
    loadServiceList()
  } else if (key === 'pvc') {
    loadPvcList()
  } else if (key === 'ingress') {
    loadIngressList()
  } else if (key === 'oss') {
    loadBucketList()
  } else if (key === 'registry') {
    loadRegistryRepos()
  } else if (key === 'configmap') {
    loadConfigMapList()
    loadNamespaceList()
  } else if (key === 'secret') {
    loadSecretList()
    loadNamespaceList()
  } else if (key === 'crd') {
    loadCrdList()
  } else if (key === 'event') {
    loadEventList()
    loadNamespaceList()
  } else if (key === 'statefulset') {
    loadStsList()
    loadNamespaceList()
  } else if (key === 'daemonset') {
    loadDsList()
    loadNamespaceList()
  } else if (key === 'node') {
    loadNodeList()
  } else if (key === 'pv') {
    loadPvList()
  } else if (key === 'storageclass') {
    loadScList()
  } else if (key === 'hpa') {
    loadHpaList()
    loadNamespaceList()
  } else if (key === 'networkpolicy') {
    loadNpList()
    loadNamespaceList()
  } else if (key === 'rbac') {
    loadRbacCurrent()
    loadNamespaceList()
  } else if (key === 'quota') {
    loadQuotaList()
    loadNamespaceList()
  } else if (key === 'cluster') {
    loadClusterList()
    loadActiveClusterName()
  }
}

// 加载命名空间列表
const loadNamespaceList = async () => {
  try {
    loadOwnerMap()
    // 调用真实后端接口获取namespace列表
    const res = await getNamespaceList()
    // 适配后端返回的NamespaceList结构，items字段为命名空间数组
    namespaceList.value = res.data.items.map(item => ({
      name: item.metadata.name,
      status: item.status.phase,
      creationTimestamp: new Date(item.metadata.creationTimestamp).toLocaleString(dateLocale()),
      labels: item.metadata.labels || {}
    }))
    // 更新统计数
    k8sStats.namespaceCount = namespaceList.value.length
  } catch (error) {
    console.error('加载命名空间列表失败:', error)
    ElMessage.error(t('common.loadNamespacesFailed') + '：' + (error.message || t('common.networkError')))
  }
}

// 创建命名空间
const handleCreateNamespace = async () => {
  if (!namespaceForm.name) {
    ElMessage.error(t('namespace.nameRequired'))
    return
  }
  createLoading.value = true
  try {
    await createNamespace({
      metadata: {
        name: namespaceForm.name
      }
    })
    ElMessage.success(t('common.createSuccess'))
    showCreateNamespaceDialog.value = false
    namespaceForm.name = ''
    loadNamespaceList()
  } catch (error) {
    console.error('创建命名空间失败:', error)
    ElMessage.error(t('createResource.createFailed') + ': ' + (error.message || t('common.unknownError')))
  } finally {
    createLoading.value = false
  }
}

// 删除命名空间
const handleDeleteNamespace = async (name) => {
  try {
    await ElMessageBox.confirm(t('namespace.confirmDelete', { name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteNamespace(name)
    ElMessage.success(t('common.deleteSuccess'))
    loadNamespaceList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除命名空间失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// 获取Pod状态标签类型
const getPodStatusType = (status) => {
  const statusMap = {
    Running: 'success',
    Pending: 'warning',
    Succeeded: 'info',
    Failed: 'danger',
    Unknown: 'info'
  }
  return statusMap[status] || 'info'
}

// 加载Pod列表
const loadPodList = async () => {
  podLoading.value = true
  try {
    let res
    if (podNamespaceFilter.value && podNamespaceFilter.value !== 'all') {
      res = await getPodList(podNamespaceFilter.value)
    } else {
      res = await getPodList()
    }
    // 适配接口返回结构: { "data": [Pod数组] }
    podList.value = res.data || res || []

    // 格式化UTC时间为本地友好显示
    podList.value.forEach(pod => {
      if (pod.metadata?.creationTimestamp) {
        pod.metadata.creationTimestamp = new Date(pod.metadata.creationTimestamp).toLocaleString(dateLocale())
      }
      if (pod.status?.startTime) {
        pod.status.startTime = new Date(pod.status.startTime).toLocaleString(dateLocale())
      }
    })
  } catch (error) {
    ElMessage.error(t('pod.loadFailed') + ': ' + (error.message || t('common.networkError')))
    podList.value = []
  } finally {
    podLoading.value = false
  }
}

// 查看Pod详情
const viewPodDetail = async (row) => {
  try {
    const res = await getPod(row.metadata.namespace, row.metadata.name)
    selectedPod.value = res.data
  } catch (error) {
    console.error('获取Pod详情失败:', error)
    ElMessage.error(t('common.getDetailFailed'))
    selectedPod.value = row // fallback to list data
  }
  showPodDetailDialog.value = true
}

// 查看Pod日志
const viewPodLogs = async (row) => {
  try {
    const res = await getPodLogs(row.metadata.namespace, row.metadata.name)
    // 兼容后端返回结构：成功时优先显示data，data为空显示message
    if (res.ok || res.code === 0 || res.code === 200) {
      podLogsContent.value = res.data || res.message || t('common.noLogContent')
      showPodLogsDialog.value = true
    } else {
      ElMessage.error(t('common.getLogsFailed') + ': ' + (res.message || t('common.unknownError')))
    }
  } catch (error) {
    ElMessage.error(t('common.getLogsFailed') + ': ' + (error.message || t('common.networkError')))
  }
}

// 删除Pod
const handleDeletePod = async (row) => {
  try {
    await ElMessageBox.confirm(t('pod.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    // 调用导入的deletePod API
    await deletePod(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadPodList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Pod失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// ======================================
// Deployment相关方法
// ======================================
// 加载Deployment列表
const loadDeploymentList = async () => {
  deploymentLoading.value = true
  try {
    let res
    if (deploymentNamespaceFilter.value && deploymentNamespaceFilter.value !== 'all') {
      res = await getDeploymentList(deploymentNamespaceFilter.value)
    } else {
      // 如果是all，需要遍历所有命名空间获取Deployment（这里简化处理，先获取所有命名空间再逐个查询）
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allDeployments = []
      for (const ns of namespaces) {
        try {
          const deployRes = await getDeploymentList(ns)
          if (deployRes.data && Array.isArray(deployRes.data)) {
            allDeployments.push(...deployRes.data)
          }
        } catch (e) {
          console.warn(`获取命名空间${ns}的Deployment失败:`, e)
        }
      }
      res = { data: allDeployments }
    }
    // 适配接口返回结构
    deploymentList.value = res.data || res || []

    // 格式化时间
    deploymentList.value.forEach(deploy => {
      if (deploy.metadata?.creationTimestamp) {
        deploy.metadata.creationTimestamp = new Date(deploy.metadata.creationTimestamp).toLocaleString(dateLocale())
      }
    })
  } catch (error) {
    ElMessage.error(t('deployment.loadFailed') + ': ' + (error.message || t('common.networkError')))
    deploymentList.value = []
  } finally {
    deploymentLoading.value = false
  }
}

// 获取Deployment状态标签类型
const getDeploymentStatusType = (deploy) => {
  const availableReplicas = deploy.status?.availableReplicas || 0
  const replicas = deploy.spec?.replicas || 0
  if (availableReplicas === replicas && replicas > 0) {
    return 'success'
  } else if (availableReplicas > 0) {
    return 'warning'
  } else {
    return 'danger'
  }
}

// 查看Deployment详情
const viewDeploymentDetail = async (row) => {
  selectedDeployment.value = row
  try {
    const detailRes = await getDeployment(row.metadata.namespace, row.metadata.name)
    deploymentDetail.value = detailRes.data
    showDeploymentDetailDialog.value = true
  } catch (error) {
    console.error('获取Deployment详情失败:', error)
    ElMessage.error(t('common.getDetailFailed') + ': ' + (error.message || t('common.unknownError')))
  }
}

// 打开扩缩容弹窗
const handleScaleDeployment = (row) => {
  selectedDeployment.value = row
  scaleForm.replicas = row.spec?.replicas || 1
  showScaleDialog.value = true
}

// 确认扩缩容
const confirmScale = async () => {
  try {
    await scaleDeployment(selectedDeployment.value.metadata.namespace, selectedDeployment.value.metadata.name, scaleForm.replicas)
    ElMessage.success(t('deployment.scaleSuccess'))
    showScaleDialog.value = false
    loadDeploymentList()
  } catch (error) {
    console.error('扩缩容失败:', error)
    ElMessage.error(t('deployment.scaleFailed') + ': ' + (error.message || t('common.unknownError')))
  }
}

// 重启Deployment
const handleRestartDeployment = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmRestart', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await restartDeployment(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('deployment.restartSuccess'))
    loadDeploymentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重启Deployment失败:', error)
      ElMessage.error(t('deployment.restartFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// 删除Deployment
const handleDeleteDeployment = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteDeployment(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadDeploymentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Deployment失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// ======================================
// StatefulSet相关方法
// ======================================
const stsList = ref([])
const stsLoading = ref(false)
const stsNamespaceFilter = ref('all')
const selectedSts = ref(null)
const stsDetail = ref(null)
const showStsDetailDialog = ref(false)
const showStsScaleDialog = ref(false)
const stsScaleReplicas = ref(1)

const loadStsList = async () => {
  stsLoading.value = true
  try {
    let res
    if (stsNamespaceFilter.value && stsNamespaceFilter.value !== 'all') {
      res = await getStatefulSetList(stsNamespaceFilter.value)
    } else {
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allSts = []
      for (const ns of namespaces) {
        try {
          const stsRes = await getStatefulSetList(ns)
          if (stsRes.data && Array.isArray(stsRes.data)) allSts.push(...stsRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的StatefulSet失败:`, e)
        }
      }
      res = { data: allSts }
    }
    stsList.value = res.data || []
  } catch (error) {
    console.error('加载StatefulSet列表失败:', error)
  } finally {
    stsLoading.value = false
  }
}

const viewStsDetail = async (row) => {
  selectedSts.value = row
  try {
    const res = await getStatefulSet(row.metadata.namespace, row.metadata.name)
    stsDetail.value = res.data
    showStsDetailDialog.value = true
  } catch (error) {
    console.error('获取StatefulSet详情失败:', error)
  }
}

const getStsStatusType = (row) => {
  const ready = row.status?.readyReplicas || 0
  const replicas = row.spec?.replicas || 0
  if (ready === replicas && replicas > 0) return 'success'
  else if (ready > 0) return 'warning'
  return 'danger'
}

const handleStsScale = (row) => {
  selectedSts.value = row
  stsScaleReplicas.value = row.spec?.replicas || 1
  showStsScaleDialog.value = true
}

const confirmStsScale = async () => {
  try {
    await scaleStatefulSet(selectedSts.value.metadata.namespace, selectedSts.value.metadata.name, stsScaleReplicas.value)
    ElMessage.success(t('deployment.scaleSuccess'))
    showStsScaleDialog.value = false
    loadStsList()
  } catch (error) {
    console.error('StatefulSet扩缩容失败:', error)
  }
}

const handleDeleteSts = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteStatefulSet(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadStsList()
  } catch (error) {
    if (error !== 'cancel') console.error('删除StatefulSet失败:', error)
  }
}

// ======================================
// DaemonSet相关方法
// ======================================
const dsList = ref([])
const dsLoading = ref(false)
const dsNamespaceFilter = ref('all')
const dsDetail = ref(null)
const showDsDetailDialog = ref(false)

const loadDsList = async () => {
  dsLoading.value = true
  try {
    let res
    if (dsNamespaceFilter.value && dsNamespaceFilter.value !== 'all') {
      res = await getDaemonSetList(dsNamespaceFilter.value)
    } else {
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allDs = []
      for (const ns of namespaces) {
        try {
          const dsRes = await getDaemonSetList(ns)
          if (dsRes.data && Array.isArray(dsRes.data)) allDs.push(...dsRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的DaemonSet失败:`, e)
        }
      }
      res = { data: allDs }
    }
    dsList.value = res.data || []
  } catch (error) {
    console.error('加载DaemonSet列表失败:', error)
  } finally {
    dsLoading.value = false
  }
}

const viewDsDetail = async (row) => {
  try {
    const res = await getDaemonSet(row.metadata.namespace, row.metadata.name)
    dsDetail.value = res.data
    showDsDetailDialog.value = true
  } catch (error) {
    console.error('获取DaemonSet详情失败:', error)
  }
}

const getDsStatusType = (row) => {
  const ready = row.status?.numberReady || 0
  const desired = row.status?.desiredNumberScheduled || 0
  if (ready === desired && desired > 0) return 'success'
  else if (ready > 0) return 'warning'
  return 'danger'
}

const handleDeleteDs = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteDaemonSet(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadDsList()
  } catch (error) {
    if (error !== 'cancel') console.error('删除DaemonSet失败:', error)
  }
}

// ======================================
// Node节点相关方法
// ======================================
const nodeList = ref([])
const nodeLoading = ref(false)
const nodeDetail = ref(null)
const showNodeDetailDialog = ref(false)

const loadNodeList = async () => {
  nodeLoading.value = true
  try {
    const res = await getNodeList()
    nodeList.value = res.data || []
  } catch (error) {
    console.error('加载节点列表失败:', error)
  } finally {
    nodeLoading.value = false
  }
}

const viewNodeDetail = (row) => {
  nodeDetail.value = row
  showNodeDetailDialog.value = true
}

const getNodeStatus = (row) => {
  const ready = (row.status?.conditions || []).find(c => c.type === 'Ready')
  return ready?.status === 'True' ? 'Ready' : (ready?.reason || 'NotReady')
}

const getNodeStatusType = (row) => getNodeStatus(row).startsWith('Ready') ? 'success' : 'danger'

const getNodeRoles = (row) => {
  const labels = row.metadata?.labels || {}
  return Object.keys(labels)
    .filter(l => l.startsWith('node-role.kubernetes.io/'))
    .map(l => l.replace('node-role.kubernetes.io/', ''))
    .join(', ')
}

const getNodeInternalIP = (row) => {
  const addrs = row.status?.addresses || []
  return (addrs.find(a => a.type === 'InternalIP') || {}).address || ''
}

// ======================================
// PersistentVolume相关方法（只读）
// ======================================
const pvList = ref([])
const pvLoading = ref(false)
const pvDetail = ref(null)
const showPvDetailDialog = ref(false)

const loadPvList = async () => {
  pvLoading.value = true
  try {
    const res = await getPvList()
    pvList.value = res.data || []
  } catch (error) {
    console.error('加载PersistentVolume列表失败:', error)
  } finally {
    pvLoading.value = false
  }
}

const viewPvDetail = async (row) => {
  try {
    const res = await getPv(row.metadata.name)
    pvDetail.value = res.data
    showPvDetailDialog.value = true
  } catch (error) {
    console.error('获取PersistentVolume详情失败:', error)
  }
}

const getPvStatusType = (phase) => {
  if (phase === 'Bound' || phase === 'Available') return 'success'
  if (phase === 'Released') return 'warning'
  if (phase === 'Failed') return 'danger'
  return 'info'
}

// ======================================
// StorageClass相关方法（只读）
// ======================================
const scList = ref([])
const scLoading = ref(false)
const scDetail = ref(null)
const showScDetailDialog = ref(false)

const loadScList = async () => {
  scLoading.value = true
  try {
    const res = await getStorageClassList()
    scList.value = res.data || []
  } catch (error) {
    console.error('加载StorageClass列表失败:', error)
  } finally {
    scLoading.value = false
  }
}

const viewScDetail = async (row) => {
  try {
    const res = await getStorageClass(row.metadata.name)
    scDetail.value = res.data
    showScDetailDialog.value = true
  } catch (error) {
    console.error('获取StorageClass详情失败:', error)
  }
}

const isDefaultStorageClass = (sc) => {
  const anns = sc.metadata?.annotations || {}
  return anns['storageclass.kubernetes.io/is-default-class'] === 'true' || anns['storageclass.beta.kubernetes.io/is-default-class'] === 'true'
}

// ======================================
// HPA相关方法
// ======================================
const hpaList = ref([])
const hpaLoading = ref(false)
const hpaNamespaceFilter = ref('all')
const hpaDetail = ref(null)
const showHpaDetailDialog = ref(false)
const showHpaMinMaxDialog = ref(false)
const selectedHpa = ref(null)
const hpaForm = reactive({ minReplicas: 1, maxReplicas: 1 })

const loadHpaList = async () => {
  hpaLoading.value = true
  try {
    let res
    if (hpaNamespaceFilter.value && hpaNamespaceFilter.value !== 'all') {
      res = await getHpaList(hpaNamespaceFilter.value)
    } else {
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allHpas = []
      for (const ns of namespaces) {
        try {
          const hpaRes = await getHpaList(ns)
          if (hpaRes.data && Array.isArray(hpaRes.data)) allHpas.push(...hpaRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的HPA失败:`, e)
        }
      }
      res = { data: allHpas }
    }
    hpaList.value = res.data || []
  } catch (error) {
    console.error('加载HPA列表失败:', error)
  } finally {
    hpaLoading.value = false
  }
}

const viewHpaDetail = async (row) => {
  try {
    const res = await getHpa(row.metadata.namespace, row.metadata.name)
    hpaDetail.value = res.data
    showHpaDetailDialog.value = true
  } catch (error) {
    console.error('获取HPA详情失败:', error)
  }
}

const handleHpaMinMax = (row) => {
  selectedHpa.value = row
  hpaForm.minReplicas = row.spec?.minReplicas ?? 1
  hpaForm.maxReplicas = row.spec?.maxReplicas ?? 1
  showHpaMinMaxDialog.value = true
}

const confirmHpaMinMax = async () => {
  if (hpaForm.minReplicas > hpaForm.maxReplicas) {
    ElMessage.error(t('hpa.minTooLarge'))
    return
  }
  try {
    await updateHpaMinMax(selectedHpa.value.metadata.namespace, selectedHpa.value.metadata.name, hpaForm.minReplicas, hpaForm.maxReplicas)
    ElMessage.success(t('hpa.adjustSuccess'))
    showHpaMinMaxDialog.value = false
    loadHpaList()
  } catch (error) {
    console.error('调整HPA失败:', error)
  }
}

const getHpaStatusType = (row) => {
  const current = row.status?.currentReplicas ?? 0
  const desired = row.status?.desiredReplicas ?? current
  if (current === desired) return 'success'
  return 'warning'
}

// ======================================
// NetworkPolicy相关方法（只读）
// ======================================
const npList = ref([])
const npLoading = ref(false)
const npNamespaceFilter = ref('all')
const npDetail = ref(null)
const showNpDetailDialog = ref(false)

const loadNpList = async () => {
  npLoading.value = true
  try {
    let res
    if (npNamespaceFilter.value && npNamespaceFilter.value !== 'all') {
      res = await getNetworkPolicyList(npNamespaceFilter.value)
    } else {
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allNps = []
      for (const ns of namespaces) {
        try {
          const npRes = await getNetworkPolicyList(ns)
          if (npRes.data && Array.isArray(npRes.data)) allNps.push(...npRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的NetworkPolicy失败:`, e)
        }
      }
      res = { data: allNps }
    }
    npList.value = res.data || []
  } catch (error) {
    console.error('加载NetworkPolicy列表失败:', error)
  } finally {
    npLoading.value = false
  }
}

const viewNpDetail = async (row) => {
  try {
    const res = await getNetworkPolicy(row.metadata.namespace, row.metadata.name)
    npDetail.value = res.data
    showNpDetailDialog.value = true
  } catch (error) {
    console.error('获取NetworkPolicy详情失败:', error)
  }
}

const getNpPodSelector = (row) => {
  const sel = row.spec?.podSelector?.matchLabels || {}
  const entries = Object.entries(sel).map(([k, v]) => `${k}=${v}`)
  const exprs = row.spec?.podSelector?.matchExpressions || []
  exprs.forEach(e => entries.push(`${e.key} ${e.operator} ${(e.values || []).join(',')}`))
  return entries.length ? entries.join(', ') : t('np.allPods')
}

// ======================================
// RBAC相关方法（只读 + 平台创建删除）
// ======================================
const rbacActiveTab = ref('serviceaccount')
const rbacNamespaceFilter = ref('all')
const rbacList = ref([])
const rbacLoading = ref(false)
const rbacDetail = ref(null)
const rbacDetailType = ref('serviceaccount')
const showRbacDetailDialog = ref(false)

const rbacDetailTitle = computed(() => {
  const names = {
    serviceaccount: t('rbac.serviceaccount'),
    role: t('rbac.role'),
    clusterrole: t('rbac.clusterrole'),
    rolebinding: t('rbac.rolebinding'),
    clusterrolebinding: t('rbac.clusterrolebinding')
  }
  return names[rbacDetailType.value] || t('rbac.detail')
})

const loadRbacCurrent = async () => {
  rbacLoading.value = true
  const tab = rbacActiveTab.value
  const namespaced = tab === 'serviceaccount' || tab === 'role' || tab === 'rolebinding'
  try {
    let res
    if (namespaced && rbacNamespaceFilter.value && rbacNamespaceFilter.value !== 'all') {
      res = await (tab === 'serviceaccount' ? getServiceAccountList(rbacNamespaceFilter.value)
        : tab === 'role' ? getRoleList(rbacNamespaceFilter.value)
        : getRoleBindingList(rbacNamespaceFilter.value))
    } else if (namespaced) {
      const nsRes = await getNamespaceList().catch(() => ({ data: { items: [] } }))
      const namespaces = (nsRes.data?.items || []).map(item => item.metadata.name)
      const allItems = []
      for (const ns of namespaces) {
        try {
          const listRes = await (tab === 'serviceaccount' ? getServiceAccountList(ns)
            : tab === 'role' ? getRoleList(ns)
            : getRoleBindingList(ns))
          if (listRes.data && Array.isArray(listRes.data)) allItems.push(...listRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的RBAC资源失败:`, e)
        }
      }
      res = { data: allItems }
    } else if (tab === 'clusterrole') {
      res = await getClusterRoleList()
    } else {
      res = await getClusterRoleBindingList()
    }
    rbacList.value = res.data || []
  } catch (error) {
    console.error('加载RBAC列表失败:', error)
  } finally {
    rbacLoading.value = false
  }
}

const viewRbacDetail = async (row) => {
  try {
    const tab = rbacActiveTab.value
    rbacDetailType.value = tab
    let res
    if (tab === 'serviceaccount') res = await getServiceAccount(row.metadata.namespace, row.metadata.name)
    else if (tab === 'role') res = await getRole(row.metadata.namespace, row.metadata.name)
    else if (tab === 'clusterrole') res = await getClusterRole(row.metadata.name)
    else if (tab === 'rolebinding') res = await getRoleBinding(row.metadata.namespace, row.metadata.name)
    else res = await getClusterRoleBinding(row.metadata.name)
    rbacDetail.value = res.data
    showRbacDetailDialog.value = true
  } catch (error) {
    console.error('获取RBAC详情失败:', error)
  }
}

const handleDeleteRbac = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    const tab = rbacActiveTab.value
    if (tab === 'serviceaccount') await deleteServiceAccount(row.metadata.namespace, row.metadata.name)
    else if (tab === 'role') await deleteRole(row.metadata.namespace, row.metadata.name)
    else if (tab === 'clusterrole') await deleteClusterRole(row.metadata.name)
    else if (tab === 'rolebinding') await deleteRoleBinding(row.metadata.namespace, row.metadata.name)
    else await deleteClusterRoleBinding(row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadRbacCurrent()
  } catch (error) {
    if (error !== 'cancel') console.error('删除RBAC资源失败:', error)
  }
}

// ======================================
// ResourceQuota相关方法
// ======================================
const quotaList = ref([])
const quotaLoading = ref(false)
const quotaNamespaceFilter = ref('all')
const quotaDetail = ref(null)
const quotaDetailRows = ref([])
const showQuotaDetailDialog = ref(false)
const showQuotaFormDialog = ref(false)
const quotaFormEditing = ref(false)
const quotaSaving = ref(false)
const selectedQuota = ref(null)
const quotaForm = reactive({ name: '', namespace: '', rows: [] })

const loadQuotaList = async () => {
  quotaLoading.value = true
  try {
    let res
    if (quotaNamespaceFilter.value && quotaNamespaceFilter.value !== 'all') {
      res = await getResourceQuotaList(quotaNamespaceFilter.value)
    } else {
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allQuotas = []
      for (const ns of namespaces) {
        try {
          const quotaRes = await getResourceQuotaList(ns)
          if (quotaRes.data && Array.isArray(quotaRes.data)) allQuotas.push(...quotaRes.data)
        } catch (e) {
          console.warn(`获取命名空间${ns}的ResourceQuota失败:`, e)
        }
      }
      res = { data: allQuotas }
    }
    quotaList.value = res.data || []
  } catch (error) {
    console.error('加载ResourceQuota列表失败:', error)
  } finally {
    quotaLoading.value = false
  }
}

const viewQuotaDetail = async (row) => {
  try {
    const res = await getResourceQuota(row.metadata.namespace, row.metadata.name)
    quotaDetail.value = res.data
    const hard = res.data?.status?.hard || res.data?.spec?.hard || {}
    const used = res.data?.status?.used || {}
    quotaDetailRows.value = Object.keys(hard).map(k => ({
      key: k,
      hard: hard[k],
      used: used[k] ?? '—'
    }))
    showQuotaDetailDialog.value = true
  } catch (error) {
    console.error('获取ResourceQuota详情失败:', error)
  }
}

const handleCreateQuota = () => {
  quotaFormEditing.value = false
  quotaForm.name = ''
  quotaForm.namespace = namespaceList.value[0]?.name || 'default'
  quotaForm.rows = [{ key: 'requests.cpu', value: '1' }]
  showQuotaFormDialog.value = true
}

const handleEditQuota = (row) => {
  quotaFormEditing.value = true
  selectedQuota.value = row
  quotaForm.name = row.metadata?.name || ''
  quotaForm.namespace = row.metadata?.namespace || ''
  const hard = row.spec?.hard || {}
  quotaForm.rows = Object.keys(hard).length
    ? Object.keys(hard).map(k => ({ key: k, value: hard[k] }))
    : [{ key: '', value: '' }]
  showQuotaFormDialog.value = true
}

const confirmQuotaForm = async () => {
  if (!quotaForm.name) {
    ElMessage.error(t('quota.nameRequired'))
    return
  }
  const hard = {}
  quotaForm.rows.forEach(r => {
    const key = (r.key || '').trim()
    if (key) hard[key] = (r.value || '').trim()
  })
  if (Object.keys(hard).length === 0) {
    ElMessage.error(t('quota.emptyHard'))
    return
  }
  quotaSaving.value = true
  try {
    if (quotaFormEditing.value) {
      await updateResourceQuota(selectedQuota.value.metadata.namespace, selectedQuota.value.metadata.name, {
        metadata: { name: selectedQuota.value.metadata.name, namespace: selectedQuota.value.metadata.namespace },
        spec: { hard }
      })
    } else {
      await createResourceQuota(quotaForm.namespace, {
        metadata: { name: quotaForm.name, namespace: quotaForm.namespace },
        spec: { hard }
      })
    }
    ElMessage.success(quotaFormEditing.value ? t('common.updateSuccess') : t('common.createSuccess'))
    showQuotaFormDialog.value = false
    loadQuotaList()
  } catch (error) {
    console.error('保存ResourceQuota失败:', error)
  } finally {
    quotaSaving.value = false
  }
}

const handleDeleteQuota = async (row) => {
  try {
    await ElMessageBox.confirm(t('deployment.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteResourceQuota(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadQuotaList()
  } catch (error) {
    if (error !== 'cancel') console.error('删除ResourceQuota失败:', error)
  }
}

// ======================================
// Service相关方法
// ======================================
// 加载Service列表
const loadServiceList = async () => {
  serviceLoading.value = true
  try {
    let res
    if (serviceNamespaceFilter.value && serviceNamespaceFilter.value !== 'all') {
      res = await getServiceList(serviceNamespaceFilter.value)
    } else {
      // 如果是all，需要遍历所有命名空间获取Service
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allServices = []
      for (const ns of namespaces) {
        try {
          const serviceRes = await getServiceList(ns)
          if (serviceRes.data && Array.isArray(serviceRes.data)) {
            allServices.push(...serviceRes.data)
          }
        } catch (e) {
          console.warn(`获取命名空间${ns}的Service失败:`, e)
        }
      }
      res = { data: allServices }
    }
    // 适配接口返回结构
    serviceList.value = res.data || res || []

    // 格式化时间
    serviceList.value.forEach(service => {
      if (service.metadata?.creationTimestamp) {
        service.metadata.creationTimestamp = new Date(service.metadata.creationTimestamp).toLocaleString(dateLocale())
      }
    })
  } catch (error) {
    ElMessage.error(t('service.loadFailed') + ': ' + (error.message || t('common.networkError')))
    serviceList.value = []
  } finally {
    serviceLoading.value = false
  }
}

// 获取Service类型标签样式
const getServiceTypeTag = (type) => {
  const typeMap = {
    'ClusterIP': 'info',
    'NodePort': 'warning',
    'LoadBalancer': 'success',
    'ExternalName': 'primary'
  }
  return typeMap[type] || 'info'
}

// 查看Service详情
const viewServiceDetail = async (row) => {
  selectedService.value = row
  try {
    // 优先用status接口返回的简化结构，更符合展示需求
    const statusRes = await getServiceStatus(row.metadata.namespace, row.metadata.name)
    serviceDetail.value = statusRes.data
    showServiceDetailDialog.value = true
  } catch (error) {
    console.error('获取Service详情失败:', error)
    ElMessage.error(t('common.getDetailFailed') + ': ' + (error.message || t('common.unknownError')))
  }
}

// 删除Service
const handleDeleteService = async (row) => {
  try {
    await ElMessageBox.confirm(t('service.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteService(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadServiceList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Service失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// ======================================
// PVC存储卷相关方法
// ======================================
// 加载StorageClass列表
const loadStorageClassList = async () => {
  storageClassLoading.value = true
  try {
    const res = await getStorageClassList()
    storageClassList.value = res.data || []
  } catch (error) {
    console.error('加载StorageClass列表失败:', error)
  } finally {
    storageClassLoading.value = false
  }
}

// 加载PVC列表
const loadPvcList = async () => {
  pvcLoading.value = true
  try {
    let res
    if (pvcNamespaceFilter.value && pvcNamespaceFilter.value !== 'all') {
      res = await getPvcList(pvcNamespaceFilter.value)
    } else {
      // 如果是all，需要遍历所有命名空间获取PVC
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allPvcs = []
      for (const ns of namespaces) {
        try {
          const pvcRes = await getPvcList(ns)
          if (pvcRes.data && Array.isArray(pvcRes.data)) {
            allPvcs.push(...pvcRes.data)
          }
        } catch (e) {
          console.warn(`获取命名空间${ns}的PVC失败:`, e)
        }
      }
      res = { data: allPvcs }
    }
    // 适配接口返回结构
    pvcList.value = res.data || res || []

    // 格式化时间
    pvcList.value.forEach(pvc => {
      if (pvc.metadata?.creationTimestamp) {
        pvc.metadata.creationTimestamp = new Date(pvc.metadata.creationTimestamp).toLocaleString(dateLocale())
      }
    })
  } catch (error) {
    console.error('获取PVC列表失败:', error)
    ElMessage.error(t('pvc.loadFailed') + ': ' + (error.message || t('common.unknownError')))
  } finally {
    pvcLoading.value = false
  }
}

// 获取PVC状态标签类型
const getPvcStatusType = (pvc) => {
  const phase = pvc.status?.phase || 'Pending'
  switch (phase) {
    case 'Bound':
      return 'success'
    case 'Pending':
      return 'warning'
    case 'Lost':
      return 'danger'
    default:
      return 'info'
  }
}

// 查看PVC详情
const viewPvcDetail = async (row) => {
  selectedPvc.value = row
  try {
    // 优先用status接口返回的简化结构，更符合展示需求
    const statusRes = await getPvcStatus(row.metadata.namespace, row.metadata.name)
    pvcDetail.value = statusRes.data
    showPvcDetailDialog.value = true
  } catch (error) {
    console.error('获取PVC详情失败:', error)
    ElMessage.error(t('common.getDetailFailed') + ': ' + (error.message || t('common.unknownError')))
  }
}

// 删除PVC
const handleDeletePvc = async (row) => {
  try {
    await ElMessageBox.confirm(t('pvc.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deletePvc(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadPvcList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除PVC失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

// ======================================
// Ingress路由相关方法
// ======================================
// 加载Ingress列表
const loadIngressList = async () => {
  ingressLoading.value = true
  try {
    let res
    if (ingressNamespaceFilter.value && ingressNamespaceFilter.value !== 'all') {
      res = await getIngressList(ingressNamespaceFilter.value)
    } else {
      // 如果是all，需要遍历所有命名空间获取Ingress
      const nsRes = await getNamespaceList()
      const namespaces = nsRes.data.items.map(item => item.metadata.name)
      const allIngresses = []
      for (const ns of namespaces) {
        try {
          const ingressRes = await getIngressList(ns)
          if (ingressRes.data && Array.isArray(ingressRes.data)) {
            allIngresses.push(...ingressRes.data)
          }
        } catch (e) {
          console.warn(`获取命名空间${ns}的Ingress失败:`, e)
        }
      }
      res = { data: allIngresses }
    }
    // 适配接口返回结构
    ingressList.value = res.data || res || []

    // 格式化时间
    ingressList.value.forEach(ingress => {
      if (ingress.metadata?.creationTimestamp) {
        ingress.metadata.creationTimestamp = new Date(ingress.metadata.creationTimestamp).toLocaleString(dateLocale())
      }
    })
  } catch (error) {
    console.error('获取Ingress列表失败:', error)
    ElMessage.error(t('ingress.loadFailed') + ': ' + (error.message || t('common.unknownError')))
  } finally {
    ingressLoading.value = false
  }
}

// 获取Ingress状态标签类型
const getIngressStatusType = (ingress) => {
  // 检查是否有负载均衡地址
  const hasLoadBalancer = ingress.status?.loadBalancer?.ingress && ingress.status.loadBalancer.ingress.length > 0
  if (hasLoadBalancer) {
    return 'success'
  }
  // 检查是否有就绪条件
  const conditions = ingress.status?.conditions || []
  const readyCondition = conditions.find(c => c.type === 'Ready')
  if (readyCondition && readyCondition.status === 'True') {
    return 'success'
  } else if (readyCondition && readyCondition.status === 'False') {
    return 'danger'
  }
  return 'warning'
}

// 查看Ingress详情
const viewIngressDetail = async (row) => {
  selectedIngress.value = row
  try {
    // 优先用status接口返回的简化结构，更符合展示需求
    const statusRes = await getIngressStatus(row.metadata.namespace, row.metadata.name)
    ingressDetail.value = statusRes.data
    showIngressDetailDialog.value = true
  } catch (error) {
    console.error('获取Ingress详情失败:', error)
    ElMessage.error(t('common.getDetailFailed') + ': ' + (error.message || t('common.unknownError')))
  }
}

// 删除Ingress
const handleDeleteIngress = async (row) => {
  try {
    await ElMessageBox.confirm(t('ingress.confirmDelete', { name: row.metadata.name }), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteIngress(row.metadata.namespace, row.metadata.name)
    ElMessage.success(t('common.deleteSuccess'))
    loadIngressList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Ingress失败:', error)
      ElMessage.error(t('common.deleteFailed') + ': ' + (error.message || t('common.unknownError')))
    }
  }
}

const formatTimestamp = (ts) => {
  if (!ts) return '-'
  return new Date(ts).toLocaleString(dateLocale())
}

let schemaNodeId = 0
const transformSchemaToTree = (schema, prefix = '') => {
  if (!schema) return []

  const result = []

  if (schema.properties) {
    Object.entries(schema.properties).forEach(([key, prop]) => {
      const node = {
        id: ++schemaNodeId,
        label: key,
        type: prop.type,
        description: prop.description,
        children: []
      }

      if (prop.properties) {
        node.children = transformSchemaToTree(prop, prefix + key + '.')
      } else if (prop.items?.properties) {
        node.children = transformSchemaToTree({ properties: prop.items.properties }, prefix + key + '.')
      } else if (prop.items) {
        node.children = [{
          id: ++schemaNodeId,
          label: 'items',
          type: prop.items.type,
          description: prop.items.description,
          children: []
        }]
      }

      result.push(node)
    })
  }

  return result
}

const copyCellText = (text) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success(t('common.copied'))
  }).catch(() => {
    ElMessage.error(t('oss.copyFailed'))
  })
}

// 创建资源
const handleCreateResource = async () => {
  createLoading.value = true
  try {
    switch (currentResourceType.value) {
      case 'deployment': {
        // 转换环境变量
        const env = deploymentForm.env.trim() ? deploymentForm.env.split('\n').map(line => {
          const [key, value] = line.split('=')
          return { name: key.trim(), value: value.trim() }
        }) : []

        // Build volumes and volumeMounts
        const volumes = []
        const volumeMounts = []
        deploymentForm.volumeMounts.forEach(vm => {
          if (!vm.type || !vm.name || !vm.mountPath) return
          const volId = 'vol-' + volumes.length
          volumeMounts.push({ name: volId, mountPath: vm.mountPath })
          if (vm.type === 'hostPath') {
            volumes.push({ name: volId, hostPath: { path: vm.name, type: 'DirectoryOrCreate' } })
          } else if (vm.type === 'configMap') {
            volumes.push({ name: volId, configMap: { name: vm.name } })
          } else if (vm.type === 'secret') {
            volumes.push({ name: volId, secret: { secretName: vm.name } })
          } else if (vm.type === 'pvc') {
            volumes.push({ name: volId, persistentVolumeClaim: { claimName: vm.name } })
          }
        })

        const deploymentData = {
          apiVersion: 'apps/v1',
          kind: 'Deployment',
          metadata: {
            name: deploymentForm.name,
            namespace: deploymentForm.namespace
          },
          spec: {
            replicas: deploymentForm.replicas,
            selector: {
              matchLabels: {
                app: deploymentForm.name
              }
            },
            template: {
              metadata: {
                labels: {
                  app: deploymentForm.name
                }
              },
              spec: {
                containers: [
                  {
                    name: deploymentForm.name,
                    image: deploymentForm.image,
                    ports: deploymentForm.containerPort ? [{ containerPort: parseInt(deploymentForm.containerPort) }] : [],
                    env: env,
                    ...(volumeMounts.length > 0 ? { volumeMounts } : {}),
                    resources: {
                      limits: {
                        cpu: deploymentForm.cpuLimit,
                        memory: deploymentForm.memoryLimit
                      }
                    }
                  }
                ],
                ...(volumes.length > 0 ? { volumes } : {})
              }
            }
          }
        }
        await createDeployment(deploymentForm.namespace, deploymentData)
        break
      }
      case 'service': {
        // 转换选择器
        const selector = serviceForm.selector.trim() ? serviceForm.selector.split('\n').reduce((obj, line) => {
          const [key, value] = line.split('=')
          obj[key.trim()] = value.trim()
          return obj
        }, {}) : { app: serviceForm.name }

        const serviceData = {
          apiVersion: 'v1',
          kind: 'Service',
          metadata: {
            name: serviceForm.name,
            namespace: serviceForm.namespace
          },
          spec: {
            type: serviceForm.type,
            ports: [
              {
                port: parseInt(serviceForm.port),
                targetPort: parseInt(serviceForm.targetPort),
                nodePort: serviceForm.type === 'NodePort' && serviceForm.nodePort ? parseInt(serviceForm.nodePort) : undefined
              }
            ],
            selector: selector
          }
        }
        await createService(serviceForm.namespace, serviceData)
        break
      }
      case 'pvc': {
        const pvcData = {
          apiVersion: 'v1',
          kind: 'PersistentVolumeClaim',
          metadata: {
            name: pvcForm.name,
            namespace: pvcForm.namespace
          },
          spec: {
            storageClassName: pvcForm.storageClass,
            accessModes: pvcForm.accessModes,
            resources: {
              requests: {
                storage: pvcForm.storage
              }
            }
          }
        }
        await createPvc(pvcForm.namespace, pvcData)
        break
      }
      case 'ingress': {
        const ingressData = {
          apiVersion: 'networking.k8s.io/v1',
          kind: 'Ingress',
          metadata: {
            name: ingressForm.name,
            namespace: ingressForm.namespace,
            annotations: ingressForm.enableTls ? {
              'kubernetes.io/ingress.class': 'nginx',
              'nginx.ingress.kubernetes.io/ssl-redirect': 'true'
            } : {
              'kubernetes.io/ingress.class': 'nginx'
            }
          },
          spec: {
            tls: ingressForm.enableTls ? [
              {
                hosts: [ingressForm.host],
                secretName: ingressForm.tlsSecretName
              }
            ] : [],
            rules: [
              {
                host: ingressForm.host,
                http: {
                  paths: [
                    {
                      path: ingressForm.path,
                      pathType: 'Prefix',
                      backend: {
                        service: {
                          name: ingressForm.serviceName,
                          port: {
                            number: parseInt(ingressForm.servicePort)
                          }
                        }
                      }
                    }
                  ]
                }
              }
            ]
          }
        }
        await createIngress(ingressForm.namespace, ingressData)
        break
      }
      case 'configmap': {
        const data = {}
        if (configMapForm.dataText.trim()) {
          configMapForm.dataText.split('\n').forEach(line => {
            const eqIdx = line.indexOf('=')
            if (eqIdx > 0) {
              data[line.substring(0, eqIdx).trim()] = line.substring(eqIdx + 1).trim()
            }
          })
        }
        await createConfigMap(configMapForm.namespace, {
          metadata: { name: configMapForm.name, namespace: configMapForm.namespace },
          data
        })
        break
      }
      case 'secret': {
        const data = {}
        if (secretForm.dataText.trim()) {
          secretForm.dataText.split('\n').forEach(line => {
            const eqIdx = line.indexOf('=')
            if (eqIdx > 0) {
              const key = line.substring(0, eqIdx).trim()
              const value = line.substring(eqIdx + 1).trim()
              data[key] = btoa(value)
            }
          })
        }
        await createSecret(secretForm.namespace, {
          metadata: { name: secretForm.name, namespace: secretForm.namespace },
          type: secretForm.type || 'Opaque',
          data
        })
        break
      }
      case 'job': {
        const env = jobForm.env.trim() ? jobForm.env.split('\n').reduce((obj, line) => {
          const [key, value] = line.split('=')
          if (key && value) obj[key.trim()] = value.trim()
          return obj
        }, {}) : {}
        const envVars = Object.keys(env).length > 0
          ? Object.entries(env).map(([k, v]) => ({ name: k, value: v }))
          : []
        const commandParts = jobForm.command.trim() ? jobForm.command.split(/\s+/) : null

        // Build volumes and volumeMounts from array
        const volumes = []
        const volumeMounts = []
        jobForm.volumeMounts.forEach(vm => {
          if (!vm.type || !vm.name || !vm.mountPath) return
          const volId = 'vol-' + volumes.length
          volumeMounts.push({ name: volId, mountPath: vm.mountPath })
          if (vm.type === 'hostPath') {
            volumes.push({ name: volId, hostPath: { path: vm.name, type: 'DirectoryOrCreate' } })
          } else if (vm.type === 'configMap') {
            volumes.push({ name: volId, configMap: { name: vm.name } })
          } else if (vm.type === 'secret') {
            volumes.push({ name: volId, secret: { secretName: vm.name } })
          } else if (vm.type === 'pvc') {
            volumes.push({ name: volId, persistentVolumeClaim: { claimName: vm.name } })
          }
        })

        await createJob(jobForm.namespace, {
          metadata: { name: jobForm.name, namespace: jobForm.namespace },
          spec: {
            completions: jobForm.completions,
            parallelism: jobForm.parallelism,
            template: {
              spec: {
                restartPolicy: jobForm.restartPolicy,
                containers: [{
                  name: jobForm.name,
                  image: jobForm.image,
                  ...(commandParts ? { command: commandParts } : {}),
                  ...(envVars.length > 0 ? { env: envVars } : {}),
                  ...(volumeMounts.length > 0 ? { volumeMounts } : {}),
                  resources: {
                    limits: {
                      cpu: jobForm.cpuLimit || '1000m',
                      memory: jobForm.memoryLimit || '1Gi'
                    },
                    requests: {
                      cpu: jobForm.cpuLimit || '1000m',
                      memory: jobForm.memoryLimit || '1Gi'
                    }
                  }
                }],
                ...(volumes.length > 0 ? { volumes } : {})
              }
            }
          }
        })
        break
      }
    }
    ElMessage.success(t('common.createSuccess'))
    showCreateResourceDialog.value = false
    // 清空表单
    Object.assign(deploymentForm, { namespace: 'default', name: '', image: '', replicas: 1, containerPort: '80', cpuLimit: '1000m', memoryLimit: '1Gi', env: '', volumeMounts: [] })
    Object.assign(serviceForm, { namespace: 'default', name: '', type: 'ClusterIP', port: '80', targetPort: '80', nodePort: '', selector: '' })
    Object.assign(pvcForm, { namespace: 'default', name: '', storageClass: 'standard', storage: '10Gi', accessModes: ['ReadWriteOnce'] })
    Object.assign(ingressForm, { namespace: 'default', name: '', host: '', path: '/', serviceName: '', servicePort: '80', enableTls: false, tlsSecretName: '' })
    Object.assign(configMapForm, { namespace: 'default', name: '', dataText: '' })
    Object.assign(secretForm, { namespace: 'default', name: '', type: 'Opaque', dataText: '' })
    Object.assign(jobForm, { namespace: 'default', name: '', image: '', completions: 1, parallelism: 1, restartPolicy: 'Never', command: '', cpuLimit: '1000m', memoryLimit: '1Gi', env: '', volumeMounts: [] })
    // 刷新对应列表
    if (currentResourceType.value === 'deployment' && activeMenu.value === 'deployment') {
      loadDeploymentList()
    } else if (currentResourceType.value === 'service' && activeMenu.value === 'service') {
      loadServiceList()
    } else if (currentResourceType.value === 'pvc' && activeMenu.value === 'pvc') {
      loadPvcList()
    } else if (currentResourceType.value === 'ingress' && activeMenu.value === 'ingress') {
      loadIngressList()
    }
  } catch (error) {
    console.error('创建资源失败:', error)
    ElMessage.error(t('createResource.createFailed') + ': ' + (error.message || t('common.unknownError')))
  } finally {
    createLoading.value = false
  }
}

// 用户操作
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await logout()
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success(t('profile.logoutSuccess'))
      router.push('/login')
    } catch (error) {
      console.error('退出失败:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  } else if (command === 'profile') {
    await openUserProfileDialog()
  } else if (command === 'inbox') {
    await openInboxDialog()
  } else if (command === 'check-cluster') {
    try {
      ElMessage.info(t('cluster.inspectStart'))
      await triggerClusterCheck()
      await loadMessages()
      ElMessage.success(t('cluster.inspectDone'))
    } catch (error) {
      console.error('巡检失败:', error)
      ElMessage.error(t('cluster.inspectFailed') + '：' + (error.message || t('common.unknownError')))
    }
  }
}

// 打开用户信息维护弹窗
const openUserProfileDialog = async () => {
  try {
    // 加载当前用户信息
    const res = await getCurrentUserInfo()
    const currentUser = res.data
    Object.assign(userProfileForm, {
      username: currentUser.username || '',
      avatar: currentUser.avatar || '',
      email: currentUser.email || '',
      phone: currentUser.phone || ''
    })
    // 如果没有头像，获取一个随机头像
    if (!userProfileForm.avatar) {
      await handleGetRandomAvatar()
    }
    // 重置密码表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    userProfileActiveTab.value = 'info'
    showUserProfileDialog.value = true
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error(t('profile.loadFailed'))
  }
}

// 获取随机头像（前端 dicebear 本地生成）
const handleGetRandomAvatar = () => {
  userProfileForm.avatar = generateRandomAvatar()
}

// 更新用户信息
const handleUpdateUserInfo = async () => {
  userProfileLoading.value = true
  try {
    await updateUserInfo(userProfileForm)
    // 更新本地userInfo
    Object.assign(userInfo, userProfileForm)
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    ElMessage.success(t('common.updateSuccess'))
    showUserProfileDialog.value = false
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error(error.message || t('profile.updateFailed'))
  } finally {
    userProfileLoading.value = false
  }
}

// 更新密码
const handleUpdatePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning(t('profile.oldPasswordRequired'))
    return
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning(t('profile.newPasswordRequired'))
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning(t('profile.passwordMismatch'))
    return
  }
  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success(t('profile.passwordChanged'))
    showUserProfileDialog.value = false
    // 退出登录
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } catch (error) {
    console.error('更新密码失败:', error)
    ElMessage.error(error.message || t('profile.updateFailed'))
  } finally {
    passwordLoading.value = false
  }
}

// 打开收件箱
const openInboxDialog = async () => {
  showInboxDialog.value = true
  await loadMessages()
}

// 加载消息
const loadMessages = async () => {
  try {
    const res = await getActiveMessagesWithStatus()
    siteMessageList.value = res.data.messages || []
    readMessageIds.value = res.data.readMessageIds || []
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error(t('inbox.loadFailed'))
  }
}

// 检查消息是否已读
const isMessageRead = (id) => {
  return readMessageIds.value.includes(id)
}

// 标记单个消息已读
const handleMarkRead = async (id) => {
  try {
    await markAsRead(id)
    if (!readMessageIds.value.includes(id)) {
      readMessageIds.value.push(id)
    }
    ElMessage.success(t('inbox.markedRead'))
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error(t('common.operateFailed'))
  }
}

// 标记所有消息已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    // 更新所有消息为已读
    readMessageIds.value = siteMessageList.value.map(msg => msg.id)
    ElMessage.success(t('inbox.allMarkedRead'))
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error(t('common.operateFailed'))
  }
}

// 获取消息类型对应的tag
const getMessageTypeTag = (type) => {
  switch (type?.toUpperCase()) {
    case 'WARNING':
      return 'warning'
    case 'ERROR':
      return 'danger'
    case 'SUCCESS':
      return 'success'
    default:
      return 'info'
  }
}

// 获取消息类型文本
const getMessageTypeText = (type) => {
  switch (type?.toUpperCase()) {
    case 'WARNING':
      return t('common.warning')
    case 'ERROR':
      return t('common.error')
    case 'SUCCESS':
      return t('common.success')
    default:
      return t('common.notice')
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  // 1分钟内
  if (diff < 60 * 1000) {
    return t('common.justNow')
  }
  // 1小时内
  if (diff < 60 * 60 * 1000) {
    return t('common.minutesAgo', { n: Math.floor(diff / (60 * 1000)) })
  }
  // 1天内
  if (diff < 24 * 60 * 60 * 1000) {
    return t('common.hoursAgo', { n: Math.floor(diff / (60 * 60 * 1000)) })
  }
  // 超过1天显示日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 定时刷新消息（每30秒）
let messageRefreshTimer = null

// 聊天消息滚动到底部（最终版，最可靠的实现）
const scrollChatToBottom = () => {
  nextTick(() => {
    const container = chatMessagesRef.value
    if (!container) return

    // 方案1：直接设置scrollTop（最基础）
    container.scrollTop = container.scrollHeight

    // 方案2：使用scrollTo API（兼容性更好）
    container.scrollTo({
      top: container.scrollHeight,
      left: 0,
      behavior: 'instant' // 无动画，直接跳到位置
    })

    // 方案3：使用scrollIntoView让最后一个元素可见（终极方案）
    const lastMessage = container.lastElementChild
    if (lastMessage) {
      lastMessage.scrollIntoView({
        block: 'end',
        behavior: 'instant'
      })
    }

    // 控制台输出调试信息，方便排查问题
    console.log('滚动执行', {
      scrollHeight: container.scrollHeight,
      clientHeight: container.clientHeight,
      scrollTop: container.scrollTop,
      lastMessage: lastMessage
    })
  })
}

// 发送消息给AI
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || aiThinking.value) return

  // 清空输入框
  inputMessage.value = ''

  // 添加用户消息到列表
  messageList.value.push({
    role: 'user',
    content: content,
    renderedContent: parseMarkdown(content),
    attachments: [...currentAttachments.value]
  })

  // 清空附件列表
  currentAttachments.value = []
  scrollChatToBottom()

  // 设置加载状态
  aiThinking.value = true

  // 添加AI的空消息，用来逐字追加
  const aiMsgIndex = messageList.value.length
  messageList.value.push({
    role: 'assistant',
    content: '',
    renderedContent: ''
  })

  try {
    // 先获取会话ID
    const currentConversationId = await getConversationId()

    // 创建AbortController用来终止请求
    controller.value = new AbortController()
    const signal = controller.value.signal

    // 构建请求体
    const requestBody = {
      conversationId: currentConversationId,
      message: content,
      attachments: currentAttachments.value
    }
    // 如果选中了专家，传入系统提示词
    if (activeExpert.value) {
      requestBody.systemPrompt = activeExpert.value.systemPrompt
    }

    // 发起流式请求，接口地址根据实际调整
    const response = await fetch('/api/v1/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'satoken': localStorage.getItem('token') || ''
      },
      body: JSON.stringify(requestBody),
      signal: signal
    })

    if (!response.ok) {
      throw new Error(`${t('error.requestFailed')}: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      // 解码响应片段
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk

      // 按 SSE 消息边界（\n\n）分割，逐条处理完整消息；
      // 最后一段可能不完整，保留为 buffer 等待下一帧
      const messages = buffer.split('\n\n')
      buffer = messages.pop() || ''

      for (const message of messages) {
        const line = message.replace(/^data:\s?/gm, '').trim()
        if (!line || line === '[DONE]') continue

        let content
        // 优先按 OpenAI 兼容协议解析（choices[0].delta.content），
        // 兼容其他常见字段，最后兜底为整行（纯文本流）
        try {
          const res = JSON.parse(line)
          const delta = res.choices?.[0]?.delta?.content
          content = delta !== undefined && delta !== null
            ? delta
            : (res.content ?? res.text ?? res.data ?? res.response)
          if (content === undefined || content === null) continue
        } catch (e) {
          // 非 JSON 格式：后端直接流式输出纯文本
          content = line
        }

        if (content) {
          appendContent(aiMsgIndex, content)
        }
      }
    }

    // 处理最后剩余的buffer内容
    if (buffer.trim()) {
      appendContent(aiMsgIndex, buffer)
    }

  } catch (error) {
    if (error.name !== 'AbortError') {
      ElMessage.error(t('ai.requestFailed'))
      const errText = t('ai.errorMsg')
      const msg = messageList.value[aiMsgIndex]
      if (msg) {
        msg.content = errText
        msg.renderedContent = parseMarkdown(errText)
      }
    }
  } finally {
    // 流式结束后，从后端 Spring AI memory 拉取一次完整消息，
    // 用后端权威的 assistant content 覆写前端消息并重渲。
    // 表格/代码块等需要完整结构才能正确解析的 markdown，
    // 在流式中间态被 marked 渲染成段落/文本，Vue 对相同字符串的 v-html
    // 更新会去重；用后端 memory 的完整内容作为权威源，强制走一次新字符串。
    // 短暂等待确保 MessageChatMemoryAdvisor 已把响应写入 SPRING_AI_CHAT_MEMORY
    if (conversationId.value) {
      try {
        await new Promise(resolve => setTimeout(resolve, 150))
        const res = await getConversationHistory(conversationId.value)
        const msgs = res.data || []
        // 从后往前找最后一条 ASSISTANT 消息
        for (let i = msgs.length - 1; i >= 0; i--) {
          const m = msgs[i]
          const mt = m.messageType
          if (mt === 'ASSISTANT' || mt === 'assistant') {
            const text = m.content || m.text || ''
            const aiMsg = messageList.value[aiMsgIndex]
            if (aiMsg && text) {
              aiMsg.content = text
              aiMsg.renderedContent = parseMarkdown(text)
            }
            break
          }
        }
      } catch (e) {
        console.error('流式结束后刷新后端 memory 失败:', e)
      }
    }
    aiThinking.value = false
    controller.value = null
    scrollChatToBottom()
  }
}

// 新增：内容追加工具函数
const appendContent = (msgIndex, newContent) => {
  if (!newContent) return

  const msg = messageList.value[msgIndex]
  if (!msg) return
  const currentContent = msg.content || ''

  // 处理英文单词空格
  if (currentContent && newContent) {
    const lastChar = currentContent.slice(-1)
    const firstChar = newContent.slice(0, 1)
    if (/[a-zA-Z0-9]/.test(lastChar) && /[a-zA-Z0-9]/.test(firstChar)) {
      msg.content = currentContent + ' ' + newContent
    } else {
      msg.content = currentContent + newContent
    }
  } else {
    msg.content = currentContent + newContent
  }

  // 显式同步重算渲染后的 HTML，绕过 computed 依赖追踪的潜在异步问题
  msg.renderedContent = parseMarkdown(msg.content)

  scrollChatToBottom()
}

// 关闭AI聊天框 - 不中断请求，后台继续接收消息
const handleAIChatClose = () => {
  // 注释掉终止逻辑，让请求在后台继续运行
  // if (controller.value) {
  //   controller.value.abort()
  //   controller.value = null
  // }
  // 不重置aiThinking状态，下次打开时还能看到是否还在回答
  // aiThinking.value = false
}

// 监听聊天弹窗打开，加载对话列表和专家列表
watch(showAIChat, async (newVal) => {
  if (newVal) {
    await Promise.all([
      loadConversationList(),
      loadExpertList()
    ])
    if (conversationList.value.length > 0) {
      // 默认选中并显示最新的对话（第一个）
      await switchConversation(conversationList.value[0].id)
    } else {
      // 没有历史对话，创建新对话
      await createNewConversation()
    }
  }
})

// 弹窗完全打开后回调，这时候DOM和动画都已完成，滚动最可靠
const handleChatDialogOpened = () => {
  // 弹窗完全打开后滚动到最新消息
  scrollChatToBottom()
  // 再加一次保险，防止内容加载延迟
  setTimeout(scrollChatToBottom, 100)
}

// 关闭专家表单对话框时重置表单
watch(showExpertForm, (newVal) => {
  if (!newVal) {
    resetExpertForm()
  }
})

// 新建Job对话框打开时加载卷资源列表
watch([showCreateResourceDialog, () => currentResourceType.value], ([open, type]) => {
  if (open && type === 'job') {
    loadVolResources(jobForm.namespace)
  }
  if (open && type === 'deployment') {
    loadVolResources(deploymentForm.namespace)
  }
  if (open && type === 'pvc') {
    loadStorageClassList()
  }
})

// 命名空间变化时重新加载卷资源
watch(() => jobForm.namespace, (ns) => {
  if (showCreateResourceDialog.value && currentResourceType.value === 'job') {
    jobForm.volumeMounts = []
    loadVolResources(ns)
  }
})
watch(() => deploymentForm.namespace, (ns) => {
  if (showCreateResourceDialog.value && currentResourceType.value === 'deployment') {
    deploymentForm.volumeMounts = []
    loadVolResources(ns)
  }
})

// 加载集群统计数据
const loadClusterStats = async () => {
  try {
    const res = await getClusterStats()
    if (res.code === 200 || res.ok) {
      const stats = res.data
      // 适配后端返回结构 - 核心资源
      k8sStats.namespaceCount = stats.workloads.namespaces
      k8sStats.podTotalCount = stats.workloads.podsTotal
      k8sStats.podRunningCount = stats.workloads.podsRunning
      k8sStats.serviceCount = stats.workloads.services
      k8sStats.pvcCount = stats.workloads.pvc
      k8sStats.deploymentCount = stats.workloads.deployments
      k8sStats.ingressCount = stats.workloads.ingresses

      // 节点数据
      k8sStats.nodeCount = stats.nodes.total
      k8sStats.readyNodeCount = stats.nodes.ready
      k8sStats.abnormalNodeCount = stats.nodes.notReady
      k8sStats.abnormalPodCount = stats.workloads.podsAbnormal

      // 资源使用详情
      k8sStats.cpuUsage = stats.resources.cpuUsage
      k8sStats.cpuUsed = stats.resources.cpuUsed
      k8sStats.cpuTotal = stats.resources.cpuTotal
      k8sStats.memoryUsage = stats.resources.memoryUsage
      k8sStats.memoryUsed = stats.resources.memoryUsed
      k8sStats.memoryTotal = stats.resources.memoryTotal
      k8sStats.storageUsage = stats.resources.storageUsage
      k8sStats.storageUsed = stats.resources.storageUsed
      k8sStats.storageTotal = stats.resources.storageTotal

      // 健康状态
      k8sStats.controlPlaneHealth = stats.health.controlPlane
      k8sStats.nodeHealth = stats.health.nodeHealth
      k8sStats.networkHealth = stats.health.network
      k8sStats.storageHealth = stats.health.storage
    }
  } catch (error) {
    console.error('加载集群统计失败:', error)
    ElMessage.warning(t('cluster.monitorLoadFailed'))
  }
}

// ==================== 集群配置（kubeconfig 上传/激活） ====================
const clusterList = ref([])
const clusterLoading = ref(false)
const showClusterUploadDialog = ref(false)
const clusterUploading = ref(false)
const clusterForm = reactive({ clusterName: '', remark: '', file: null })
const activeClusterName = ref('')
const clusterTestLoadingId = ref(null)
const headerTitle = computed(() =>
  activeClusterName.value
    ? activeClusterName.value
    : t('cluster.noActive')
)

const loadActiveClusterName = async () => {
  try {
    const res = await getActiveClusterConfig()
    activeClusterName.value = res.data?.clusterName || ''
  } catch (error) {
    activeClusterName.value = ''
    console.error('加载当前激活集群失败:', error)
  }
}

const loadClusterList = async () => {
  clusterLoading.value = true
  try {
    const res = await getClusterConfigList()
    clusterList.value = res.data || []
  } catch (error) {
    console.error('加载集群配置列表失败:', error)
  } finally {
    clusterLoading.value = false
  }
}

const openClusterUploadDialog = () => {
  clusterForm.clusterName = ''
  clusterForm.remark = ''
  clusterForm.file = null
  showClusterUploadDialog.value = true
}

const onClusterFileChange = (file) => {
  clusterForm.file = file.raw
}

const handleClusterUpload = async () => {
  if (!clusterForm.clusterName.trim()) {
    ElMessage.warning(t('cluster.nameRequired'))
    return
  }
  if (!clusterForm.file) {
    ElMessage.warning(t('cluster.fileRequired'))
    return
  }
  clusterUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', clusterForm.file)
    fd.append('clusterName', clusterForm.clusterName.trim())
    if (clusterForm.remark) fd.append('remark', clusterForm.remark)
    await uploadClusterConfig(fd)
    ElMessage.success(t('cluster.uploadSuccess'))
    showClusterUploadDialog.value = false
    loadClusterList()
    loadActiveClusterName()
  } catch (error) {
    console.error('上传集群配置失败:', error)
  } finally {
    clusterUploading.value = false
  }
}

const handleClusterTest = async (row) => {
  clusterTestLoadingId.value = row.id
  try {
    const res = await testClusterConfig(row.id)
    const version = res.data?.version || '?'
    ElMessage.success(t('cluster.testSuccess').replace('{version}', version))
  } catch (error) {
    console.error('测试集群连接失败:', error)
  } finally {
    clusterTestLoadingId.value = null
  }
}

const handleActivateCluster = (row) => {
  ElMessageBox.confirm(
    t('cluster.activateConfirm').replace('{name}', row.clusterName),
    t('common.tip'),
    { type: 'warning' }
  ).then(async () => {
    try {
      await activateClusterConfig(row.id)
      ElMessage.success(t('cluster.activateSuccess'))
      loadClusterList()
      loadActiveClusterName()
    } catch (error) {
      console.error('激活集群失败:', error)
    }
  }).catch(() => {})
}

const handleDeleteCluster = (row) => {
  ElMessageBox.confirm(
    t('cluster.deleteConfirm').replace('{name}', row.clusterName),
    t('common.tip'),
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteClusterConfig(row.id)
      ElMessage.success(t('cluster.deleteSuccess'))
      loadClusterList()
      loadActiveClusterName()
    } catch (error) {
      console.error('删除集群配置失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  // 加载基础数据
  loadNamespaceList()
  loadActiveClusterName()
  loadClusterStats()
  loadOwnerMap()
  loadMessages()
  // 初始化命名空间选择器默认值
  podNamespaceFilter.value = 'all'
  // 启动定时刷新：每30秒刷新一次集群监控数据
  refreshTimer.value = setInterval(() => {
    loadClusterStats()
  }, 30 * 1000)
  // 定时刷新消息
  messageRefreshTimer = setInterval(() => {
    loadMessages()
  }, 30 * 1000)
})

// 组件卸载时清除定时器，避免内存泄漏
onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
  if (messageRefreshTimer) {
    clearInterval(messageRefreshTimer)
    messageRefreshTimer = null
  }
})
</script>

<style scoped>
.dashboard-container {
  width: 100vw;
  height: 100vh;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  position: fixed;
  top: 0;
  left: 0;
}

.dashboard-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left h2 {
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.dashboard-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  height: calc(100vh - 60px);
}

.dashboard-sidebar {
  width: 240px;
  background: #001529;
  overflow-y: auto;
  height: 100%;
  max-height: calc(100vh - 60px);
  padding-bottom: 0;
}

.sidebar-menu {
  border-right: none;
  height: auto;
  min-height: 100%;
  padding-bottom: 0;
}
/* 滚动条美化 */
.dashboard-sidebar::-webkit-scrollbar {
  width: 6px;
}
.dashboard-sidebar::-webkit-scrollbar-track {
  background: #001529;
}
.dashboard-sidebar::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 3px;
}
.dashboard-sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.3);
}

.dashboard-content {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 20px;
}

.content-wrapper {
  width: 100%;
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h3 {
  font-size: 18px;
  color: #333;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.mr-1 {
  margin-right: 8px;
}
.ml-2 {
  margin-left: 16px;
}
.empty-state {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pod-page {
  padding-bottom: 60px; /* 增加底部内边距，防止最后一行被遮挡 */
}

.deployment-page {
  padding-bottom: 60px;
}

.service-page {
  padding-bottom: 60px;
}

.pvc-page {
  padding-bottom: 60px;
}

.ingress-page {
  padding-bottom: 60px;
}

.port-item {
  display: inline-block;
}

.mode-item {
  display: inline-block;
}

.rule-item {
  display: block;
}

.address-item {
  display: block;
  word-break: break-all;
}

/* Service详情页表格样式 */
.service-detail-table {
  width: 100% !important;
  table-layout: fixed !important;
  box-sizing: border-box !important;
}
.service-detail-table .el-table__header,
.service-detail-table .el-table__body {
  width: 100% !important;
  table-layout: fixed !important;
}
.service-detail-table .el-table__cell {
  word-wrap: break-word;
  word-break: break-all;
  box-sizing: border-box !important;
  padding: 6px 0 !important;
}
.service-detail-table .el-table__body-wrapper {
  overflow-x: hidden !important;
}
.service-detail-table .el-scrollbar__wrap {
  overflow-x: hidden !important;
}

/* 时间线样式 */
.el-timeline {
  padding-left: 20px;
}
.el-timeline-item__content {
  padding-bottom: 20px;
}
.el-timeline-item h5 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}
.el-timeline-item p {
  margin: 2px 0;
  font-size: 13px;
  color: #666;
}

.detail-container {
  max-height: 600px;
  overflow-y: auto;
}

.detail-container .el-descriptions {
  margin-bottom: 20px;
}

.command-display {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.command-display code {
  background: transparent;
  color: inherit;
  padding: 0;
}

.detail-container h4 {
  margin: 20px 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

/* 时间线样式 */
.el-timeline {
  padding-left: 20px;
}
.el-timeline-item__content {
  padding-bottom: 20px;
}
.el-timeline-item h5 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}
.el-timeline-item p {
  margin: 2px 0;
  font-size: 13px;
  color: #666;
}
.log-card {
  background: #f5f7fa;
}
.log-content {
  height: 70vh;
  overflow-y: auto;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin: 0;
  text-align: left !important;
}
/* 调整表格容器高度，确保完整显示 */
.dashboard-content {
  overflow-y: auto;
  min-height: calc(100vh - 60px);
  padding-bottom: 80px;
}
/* 修复命名空间下拉选择器文字显示问题 */
.pod-namespace-select :deep(.el-input__wrapper) {
  color: #303133 !important;
}
.pod-namespace-select :deep(.el-input__inner) {
  color: #303133 !important;
  -webkit-text-fill-color: #303133 !important;
}


.stat-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.green {
  background: linear-gradient(135deg, #13c2c2 0%, #52c41a 100%);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #fa8c16 0%, #faad14 100%);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.mt-20 {
  margin-top: 20px;
}

.empty-page {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 健康状态卡片样式 */
.health-card {
  height: 100%;
}
.health-item {
  display: flex;
  align-items: center;
  gap: 15px;
}
.health-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}
.health-icon.normal {
  background: #67c23a;
}
.health-icon.warning {
  background: #e6a23c;
}
.health-icon.error {
  background: #f56c6c;
}
.health-info {
  flex: 1;
}
.health-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}
.health-status {
  font-size: 14px;
  font-weight: 500;
}
.health-status.normal {
  color: #67c23a;
}
.health-status.warning {
  color: #e6a23c;
}
.health-status.error {
  color: #f56c6c;
}
.abnormal-count {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  margin-left: 4px;
}

/* AI助手悬浮按钮 */
.ai-assistant-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  cursor: pointer;
  z-index: 9999;
  transition: transform 0.3s ease;
}
.ai-assistant-btn:hover {
  transform: scale(1.1);
}

/* AI助手悬浮按钮外围呼吸灯效果 */
.ai-assistant-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.6);
  animation: ai-btn-breath 2s ease-in-out infinite;
  z-index: -1;
  pointer-events: none;
}

@keyframes ai-btn-breath {
  0% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.6);
  }
  100% {
    box-shadow: 0 0 0 18px rgba(102, 126, 234, 0);
  }
}

/* 增大右上角创建资源按钮 */
.header-right .el-button--primary {
  padding: 11px 28px;
  font-size: 16px;
  height: auto;
  min-width: 120px;
}

/* AI聊天弹窗 */
/* ======================================
   AI聊天弹窗 - 完全重写的全新样式
   ====================================== */
/* 弹窗基础样式 */
:deep(.ai-chat-dialog) {
  --el-dialog-bg-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  margin: 0 !important;
  height: 100vh !important;
  border-radius: 0 !important;
  --el-dialog-padding-primary: 0 !important;
  --el-dialog-header-height: 54px !important;
  overflow: hidden !important;
}
:deep(.ai-chat-dialog .el-dialog__header) {
  border-bottom: 1px solid #eee;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}
:deep(.ai-chat-dialog .el-dialog__body) {
  padding: 0 !important;
  height: calc(100vh - 54px) !important;
  overflow: hidden !important;
}

/* 右上角关闭按钮 - 醒目 */
:deep(.ai-chat-dialog .el-dialog__headerbtn) {
  width: 44px !important;
  height: 44px !important;
  font-size: 26px !important;
  color: #f56c6c !important;
  background: rgba(245, 108, 108, 0.15) !important;
  border-radius: 8px !important;
  top: 5px !important;
  right: 16px !important;
  transition: all 0.2s ease !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}
:deep(.ai-chat-dialog .el-dialog__headerbtn:hover) {
  background: rgba(245, 108, 108, 0.25) !important;
  transform: scale(1.15) !important;
}
:deep(.ai-chat-dialog .el-dialog__close) {
  font-size: 26px !important;
  font-weight: bold !important;
}
:deep(.ai-chat-dialog .el-dialog__close svg) {
  width: 26px !important;
  height: 26px !important;
}

/* 最外层全屏容器 */
.ai-chat-fullscreen {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: hidden;
  background: #f8f9fa;
}

/* 左侧侧边栏 - 固定宽度，独立滚动 */
.ai-chat-sidebar {
  width: 220px;
  border-right: 1px solid #eee;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  height: 100%;
  overflow: hidden;
}
.ai-chat-sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}
.ai-chat-conversation-list {
  flex: 1;
  overflow-y: auto !important;
  padding: 8px;
  min-height: 0;
}
.ai-chat-conversation-item {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background: #f8f9fa;
}
.ai-chat-conversation-item:hover {
  background: #e8f3ff;
}
.ai-chat-conversation-item.active {
  background: #409eff;
  color: white;
}
.ai-chat-conversation-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ai-chat-conversation-actions {
  position: absolute;
  right: 8px;
  top: 12px;
  opacity: 0;
  transition: opacity 0.2s;
  display: flex;
  gap: 2px;
}
.ai-chat-conversation-item:hover .ai-chat-conversation-actions {
  opacity: 1;
}
.ai-chat-conversation-actions .el-button {
  padding: 0px;
  width: 24px;
  height: 24px;
}
.ai-chat-conversation-item.active .el-button {
  color: white !important;
}
.ai-chat-conversation-time {
  font-size: 11px;
  opacity: 0.7;
}

/* 右侧聊天区域 - 自适应，独立滚动 */
.ai-chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
  margin: 0 auto;
  padding: 16px;
}

/* 消息列表 - 独立滚动 */
.ai-chat-message-list {
  flex: 1;
  overflow-y: auto !important;
  min-height: 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}
.ai-chat-message {
  display: flex;
  margin-bottom: 24px;
  gap: 12px;
  align-items: flex-start;
}
.ai-chat-message.user {
  flex-direction: row-reverse;
}
.ai-chat-message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8f3ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.ai-chat-message.user .ai-chat-message-avatar {
  background: #409eff;
  color: white;
}
.ai-chat-message-content {
  max-width: min(75%, 800px);
  width: fit-content;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  text-align: left !important; /* 所有对话内容左对齐 */
}
.ai-chat-message.user .ai-chat-message-content {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 2px;
}
.ai-chat-message.assistant .ai-chat-message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 2px;
}
.ai-chat-message-content.thinking {
  color: #999;
  font-style: italic;
  background: #f5f5f5;
  animation: blink 1.5s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
/* Markdown内容样式 */
.markdown-content {
  width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-word;
  text-align: left !important; /* Markdown内容左对齐 */
}
/* 确保所有Markdown文本元素都是左对齐 */
.markdown-content p,
.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6,
.markdown-content li,
.markdown-content blockquote,
.markdown-content pre,
.markdown-content code {
  text-align: left !important;
}
.markdown-content > *:first-child {
  margin-top: 0;
}
.markdown-content > *:last-child {
  margin-bottom: 0;
}
.markdown-content p {
  margin: 8px 0;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: break-word;
}
.markdown-content a {
  color: #409eff;
  text-decoration: none;
  overflow-wrap: break-word;
  word-break: break-all;
}
.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin: 12px 0 6px 0;
  font-weight: 600;
  line-height: 1.4;
}
.markdown-content h1 { font-size: 20px; border-bottom: 1px solid #e5e7eb; padding-bottom: 4px; }
.markdown-content h2 { font-size: 18px; border-bottom: 1px solid #e5e7eb; padding-bottom: 3px; }
.markdown-content h3 { font-size: 16px; }
.markdown-content h4 { font-size: 15px; }
.markdown-content ul,
.markdown-content ol {
  margin: 8px 0;
  padding-left: 24px;
}
.markdown-content li {
  margin: 4px 0;
  line-height: 1.6;
}
.markdown-content blockquote {
  margin: 8px 0;
  padding: 8px 12px;
  border-left: 4px solid #d1d5db;
  background-color: #f9fafb;
  color: #6b7280;
  border-radius: 0 4px 4px 0;
}
.markdown-content code {
  padding: 2px 6px;
  background-color: #f3f4f6;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}
.markdown-content pre {
  margin: 10px 0;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 6px;
  overflow-x: auto;
  border: 1px solid #e5e7eb;
}
.markdown-content pre code {
  padding: 0;
  background: none;
  border: none;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.markdown-content a:hover {
  text-decoration: underline;
}
/* 表格外层容器，支持横向滚动 */
.markdown-content {
  overflow-x: auto;
}
.markdown-content table {
  width: 100%;
  min-width: 100%;
  max-width: 100%;
  table-layout: fixed;
  margin: 10px 0;
  border-collapse: collapse;
  border: 1px solid #e5e7eb;
  font-size: 12px;
  background-color: #fff;
}
.markdown-content th,
.markdown-content td {
  padding: 5px 8px;
  border: 1px solid #e5e7eb;
  text-align: left;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
  max-width: 200px; /* 最大宽度，防止单个单元格太宽 */
}
.markdown-content th {
  background-color: #f3f4f6;
  font-weight: 600;
  position: sticky;
  top: 0;
}
/* 鼠标悬停效果 */
.markdown-content tr:hover {
  background-color: rgba(0, 0, 0, 0.02);
}
.markdown-content hr {
  margin: 12px 0;
  border: none;
  border-top: 1px solid #e5e7eb;
}

/* 暗色主题适配（用户消息是蓝色背景，需要调整Markdown样式） */
.ai-chat-message.user .markdown-content {
  color: #fff;
}
.ai-chat-message.user .markdown-content code,
.ai-chat-message.user .markdown-content pre {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.ai-chat-message.user .markdown-content blockquote {
  background-color: rgba(255, 255, 255, 0.1);
  border-left-color: rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.9);
}
.ai-chat-message.user .markdown-content a {
  color: #fff;
  text-decoration: underline;
}
.ai-chat-message.user .markdown-content h1,
.ai-chat-message.user .markdown-content h2 {
  border-bottom-color: rgba(255, 255, 255, 0.3);
}
/* 用户消息（蓝色背景）下的表格适配 */
.ai-chat-message.user .markdown-content table {
  border-color: rgba(255, 255, 255, 0.3);
  background-color: rgba(255, 255, 255, 0.1);
}
.ai-chat-message.user .markdown-content th,
.ai-chat-message.user .markdown-content td {
  border-color: rgba(255, 255, 255, 0.3);
}
.ai-chat-message.user .markdown-content th {
  background-color: rgba(255, 255, 255, 0.2);
}
/* 消息附件样式 */
.message-attachments {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 收件箱消息内容左对齐 */
.message-content {
  text-align: left !important;
}
.message-attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.message-attachment-item:hover {
  background: rgba(255, 255, 255, 0.5);
  border-color: #409eff;
}
.ai-chat-message.user .message-attachment-item {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: white;
}
.ai-chat-message.user .message-attachment-item:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}
.attachment-info {
  flex: 1;
  min-width: 0;
}
.attachment-name {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.attachment-size {
  font-size: 11px;
  opacity: 0.8;
}

.thinking .dot {
  display: inline-block;
  animation: dot-flashing 1.4s infinite linear;
}
@keyframes dot-flashing {
  0% { opacity: .2; }
  50% { opacity: 1; }
  100% { opacity: .2; }
}
.ai-chat-input {
  flex-shrink: 0;
}
.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.input-row .el-input {
  flex: 1;
}
.input-row .el-button {
  flex-shrink: 0;
}
/* 上传进度条 */
.upload-progress {
  margin: 8px 0;
}
/* 输入区域附件列表 */
.attachment-list {
  margin: 8px 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 120px;
  overflow-y: auto;
}
.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
}
.attachment-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.attachment-size {
  font-size: 11px;
  color: #999;
  margin-right: 8px;
}
/* 聊天框滚动条 */
.ai-chat-messages::-webkit-scrollbar,
.conversation-list::-webkit-scrollbar {
  width: 6px;
}
.ai-chat-messages::-webkit-scrollbar-track,
.conversation-list::-webkit-scrollbar-track {
  background: #f5f7fa;
}
.ai-chat-messages::-webkit-scrollbar-thumb,
.conversation-list::-webkit-scrollbar-thumb {
  background: #c1c5ce;
  border-radius: 3px;
}
.ai-chat-messages::-webkit-scrollbar-thumb:hover,
.conversation-list::-webkit-scrollbar-thumb:hover {
  background: #a9aeb8;
}

/* 资源利用率样式 */
.usage-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  font-weight: 500;
  line-height: 1.4;
}
.usage-title small {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  display: block;
  margin-top: 4px;
}

/* 业务指标样式 */
.business-item {
  text-align: center;
}
.business-value {
  font-size: 36px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}
.business-label {
  font-size: 14px;
  color: #999;
}
/* 头像选择器样式 */
.avatar-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 收件箱样式 */
.message-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.message-icon-wrapper:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.message-card {
  border-left: 4px solid #dcdfe6;
  transition: all 0.2s;
}

.message-card.unread {
  border-left-color: #409eff;
  background-color: #f5f7fa;
}

.message-card.unread:hover {
  background-color: #ecf5ff;
}

.message-card:hover {
  transform: translateX(2px);
}

/* 消息类型样式 */
.message-card.msg-info {
  border-left-color: #409eff;
}

.message-card.msg-success {
  border-left-color: #67c23a;
}

.message-card.msg-warning {
  border-left-color: #e6a23c;
}

.message-card.msg-error {
  border-left-color: #f56c6c;
}

.message-card.msg-error.unread {
  background-color: #fef0f0;
}

.message-card.msg-error.unread:hover {
  background-color: #fde2e2;
}

.message-card.msg-warning.unread {
  background-color: #fdf6ec;
}

.message-card.msg-warning.unread:hover {
  background-color: #faecd8;
}

.inbox-list::-webkit-scrollbar {
  width: 6px;
}

.inbox-list::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.inbox-list::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* Agent专家样式 */
.expert-prompts {
  margin-bottom: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}
.expert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
.expert-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.expert-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.expert-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
}
.expert-item {
  flex: 0 0 calc(50% - 4px);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  box-sizing: border-box;
}
.expert-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}
.expert-item.active {
  border-color: #e6a23c;
  background: #fdf6ec;
}
.expert-icon {
  font-size: 24px;
  flex-shrink: 0;
}
.expert-info {
  flex: 1;
  min-width: 0;
}
.expert-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.expert-desc {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.expert-tag {
  flex-shrink: 0;
}

/* 专家管理对话框样式 */
.expert-manager-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 8px 0;
}
.expert-manager-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.expert-manager-item:last-child {
  border-bottom: none;
}
.expert-manager-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.expert-manager-icon {
  font-size: 28px;
  line-height: 1;
}
.expert-manager-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.expert-manager-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.expert-manager-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* OSS对象存储样式 */
.oss-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.oss-container {
  flex: 1;
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.bucket-sidebar {
  width: 250px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.sidebar-title {
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.create-bucket-btn {
  flex-shrink: 0;
}
.bucket-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.bucket-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}
.bucket-item:hover {
  background: #f5f7fa;
}
.bucket-item.active {
  background: #ecf5ff;
  color: #409eff;
}
.file-content {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.file-header {
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 8px 8px 0 0;
}
.current-bucket {
  font-size: 14px;
  color: #666;
}
.file-list {
  flex: 1;
  overflow: auto;
  padding: 16px;
}
.file-list .el-table {
  border-radius: 8px;
}
.oss-breadcrumb {
  margin-top: 8px;
}
.breadcrumb-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}
.breadcrumb-link:hover {
  color: #337ecc;
}
.file-name {
  display: inline-flex;
  align-items: center;
}
.folder-name {
  color: #409eff;
  cursor: pointer;
}
.folder-name:hover {
  color: #337ecc;
  text-decoration: underline;
}
.upload-progress-container {
  padding: 8px 0;
}
.upload-filename {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}
.upload-float-indicator {
  position: fixed;
  right: 20px;
  z-index: 9999;
  background: rgba(64, 158, 255, 0.9);
  color: #fff;
  border-radius: 24px;
  padding: 10px 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s;
}
.upload-float-indicator:hover {
  transform: scale(1.05);
}
.upload-float-icon {
  font-size: 20px;
}
.upload-float-percent {
  font-size: 14px;
  font-weight: 600;
}
.image-preview-container {
  display: flex;
  align-items: center;
  justify-content: center;
  max-height: 70vh;
  overflow: auto;
}
.image-preview-container img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.text-preview-container {
  max-height: 70vh;
  overflow: auto;
}
.text-preview-content {
  margin: 0;
  padding: 16px;
  background: #1e1e1e;
  color: #d4d4d4;
  font-size: 13px;
  line-height: 1.6;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  text-align: left;
}

/* 镜像管理页面样式 */
.registry-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.registry-layout {
  flex: 1;
  display: flex;
  gap: 12px;
  min-height: 0;
}
.registry-repos-panel {
  width: 286px;
  flex-shrink: 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.registry-repos-panel .repo-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.repo-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.repo-item .el-icon {
  flex-shrink: 0;
}
.repo-item:hover {
  background: #ecf5ff;
}
.repo-item.active {
  background: #409eff;
  color: #fff;
}
.registry-tags-panel {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.registry-tags-panel .tags-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.tag-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.tag-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.tag-item:hover {
  background: #f5f7fa;
}
.tag-item.active {
  background: #ecf5ff;
}
.tag-count {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}
.registry-detail-panel {
  width: 360px;
  flex-shrink: 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  overflow-y: auto;
}
.layers-section {
  margin-top: 8px;
}

/* ConfigMap页面样式 */
.configmap-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.volume-mount-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.form-tip {
  font-size: 12px;
  color: #909399;
}
.copyable-cell {
  cursor: pointer;
}
.copyable-cell:hover {
  color: #409eff;
  text-decoration: underline;
}

/* K8S Event页面样式 */
.event-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.event-container {
  flex: 1;
  min-height: 0;
}

/* CRD详情页面样式 */
.version-content h5 {
  margin: 12px 0 8px 0;
  font-size: 14px;
  color: #333;
}
.custom-tree-node {
  font-size: 13px;
}
.code-display {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  overflow: auto;
  max-height: 600px;
  margin: 0;
  text-align: left;
}
.command-display {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  overflow: auto;
}
.command-display code {
  background: transparent;
  padding: 0;
}
.mt-3 {
  margin-top: 12px;
}
.mt-4 {
  margin-top: 16px;
}
.mb-2 {
  margin-bottom: 8px;
}
.mb-3 {
  margin-bottom: 12px;
}
.mb-4 {
  margin-bottom: 16px;
}
.text-gray-400 {
  color: #909399;
}
.text-gray-500 {
  color: #606266;
}
.text-center {
  text-align: center;
}
.py-8 {
  padding: 32px 0;
}
.w-full {
  width: 100%;
}
.pr-4 {
  padding-right: 16px;
}
.flex {
  display: flex;
}
.items-center {
  align-items: center;
}
.justify-between {
  justify-content: space-between;
}
.font-bold {
  font-weight: 600;
}
.font-mono {
  font-family: monospace;
}
.text-xs {
  font-size: 12px;
}
.ml-1 {
  margin-left: 4px;
}
.ml-2 {
  margin-left: 8px;
}
.mr-1 {
  margin-right: 4px;
}

/* OSS页面布局 */
.oss-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.oss-bucket-panel {
  width: 200px;
  flex-shrink: 0;
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 8px;
  background: var(--el-bg-color);
  overflow: hidden;
}
.oss-bucket-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-weight: 600;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}
.oss-bucket-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  cursor: pointer;
  color: var(--el-text-color-primary);
  transition: background 0.15s;
}
.oss-bucket-item:hover {
  background: var(--el-fill-color-light);
}
.oss-bucket-item.active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}
.oss-bucket-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.oss-bucket-empty {
  padding: 16px 12px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  text-align: center;
}
.oss-main {
  flex: 1;
  min-width: 0;
  border-radius: 8px;
  border: 1px dashed transparent;
  padding-top: 2px;
  transition: border-color 0.2s, background 0.2s;
}
.oss-main.is-dragover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.oss-main-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.oss-breadcrumb {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

/* OSS上传进度浮窗 */
.oss-upload-float {
  position: fixed;
  right: 24px;
  z-index: 2100;
  width: 280px;
  background: var(--el-bg-color, #fff);
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  user-select: none;
}
.oss-upload-float .ondrag-title {
  display: flex;
  align-items: center;
  padding: 10px 12px 0 12px;
  font-weight: 500;
  cursor: grab;
}
.oss-upload-minimized {
  width: 56px;
  border-radius: 50%;
  cursor: pointer;
  padding: 6px;
}
.px-2 {
  padding-left: 8px;
  padding-right: 8px;
}
.mt-1 {
  margin-top: 4px;
}
.block {
  display: block;
}

</style>
