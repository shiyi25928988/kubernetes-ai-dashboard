import request from '@/utils/request'

// 获取活跃消息及阅读状态
export const getActiveMessagesWithStatus = () => {
  return request({
    url: '/v1/messages/active-with-status',
    method: 'get'
  })
}

// 获取未读消息
export const getUnreadMessages = () => {
  return request({
    url: '/v1/messages/unread',
    method: 'get'
  })
}

// 获取活跃消息
export const getActiveMessages = () => {
  return request({
    url: '/v1/messages/active',
    method: 'get'
  })
}

// 标记单个消息已读
export const markAsRead = (id) => {
  return request({
    url: `/v1/messages/mark-read/${id}`,
    method: 'post'
  })
}

// 标记所有消息已读
export const markAllAsRead = () => {
  return request({
    url: '/v1/messages/mark-all-read',
    method: 'post'
  })
}

// 检查消息是否已读
export const isMessageRead = (id) => {
  return request({
    url: `/v1/messages/is-read/${id}`,
    method: 'get'
  })
}

// 获取消息详情
export const getMessageById = (id) => {
  return request({
    url: `/v1/messages/${id}`,
    method: 'get'
  })
}

// 获取所有消息 (管理员)
export const getAllMessages = () => {
  return request({
    url: '/v1/messages/all',
    method: 'get'
  })
}

// 创建消息 (管理员)
export const createMessage = (data) => {
  return request({
    url: '/v1/messages/create',
    method: 'post',
    data
  })
}

// 更新消息 (管理员)
export const updateMessage = (data) => {
  return request({
    url: '/v1/messages/update',
    method: 'post',
    data
  })
}

// 删除消息 (管理员)
export const deleteMessage = (id) => {
  return request({
    url: `/v1/messages/delete/${id}`,
    method: 'post'
  })
}

// 手动触发K8S集群巡检
export const triggerClusterCheck = () => {
  return request({
    url: '/v1/messages/cluster/check',
    method: 'post'
  })
}

// 获取集群自动巡检设置
export const getInspectionConfig = () => {
  return request({
    url: '/v1/messages/cluster/inspection-config',
    method: 'get'
  })
}

// 更新集群自动巡检设置（开关/间隔/AI Agent 诊断）
export const updateInspectionConfig = (data) => {
  return request({
    url: '/v1/messages/cluster/inspection-config',
    method: 'post',
    data
  })
}
