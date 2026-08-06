import request from '../utils/request'

/**
 * AI聊天相关API
 */

// 简单聊天
export function simpleChat(data) {
  return request({
    url: '/v1/ai/simpleChat',
    method: 'post',
    data
  })
}

// 流式聊天
export function chat(data) {
  return request({
    url: '/v1/ai/chat',
    method: 'post',
    data,
    responseType: 'stream'
  })
}

// 生成新的对话ID
export function generateNewConversationId() {
  return request({
    url: '/v1/ai/generateNewConversationId',
    method: 'get'
  })
}

/**
 * 聊天记忆管理API
 */

// 获取对话历史消息
export function getConversationHistory(conversationId) {
  return request({
    url: `/v1/ai/memory/history/${conversationId}`,
    method: 'get'
  })
}

// 删除对话记忆
export function deleteConversation(conversationId) {
  return request({
    url: `/v1/ai/memory/${conversationId}`,
    method: 'delete'
  })
}

// 获取当前用户的对话列表
export function getUserConversations() {
  return request({
    url: '/v1/ai/memory/conversations',
    method: 'get'
  })
}

// 删除当前用户所有对话记忆
export function deleteAllConversations() {
  return request({
    url: '/v1/ai/memory/all',
    method: 'delete'
  })
}

// 更新对话标题
export function updateConversationTitle(conversationId, title) {
  return request({
    url: `/v1/ai/memory/${conversationId}/title`,
    method: 'put',
    data: { title }
  })
}

/**
 * 预设指令API
 */

// 获取所有启用的预设指令
export function getPresetPrompts() {
  return request({
    url: '/v1/preset/list',
    method: 'get'
  })
}

// 按分类获取预设指令
export function getPresetPromptsByCategory(category) {
  return request({
    url: `/v1/preset/list/${category}`,
    method: 'get'
  })
}

// 创建预设指令
export function createPresetPrompt(data) {
  return request({
    url: '/v1/preset/create',
    method: 'post',
    data
  })
}

// 更新预设指令
export function updatePresetPrompt(id, data) {
  return request({
    url: `/v1/preset/update/${id}`,
    method: 'post',
    data
  })
}

// 删除预设指令
export function deletePresetPrompt(id) {
  return request({
    url: `/v1/preset/delete/${id}`,
    method: 'post'
  })
}

/**
 * AI专家管理API
 */

// 获取已启用的专家列表
export function getAiExperts() {
  return request({
    url: '/v1/ai/expert/list',
    method: 'get'
  })
}

// 创建自定义专家
export function createAiExpert(data) {
  return request({
    url: '/v1/ai/expert/create',
    method: 'post',
    data
  })
}

// 更新自定义专家
export function updateAiExpert(id, data) {
  return request({
    url: `/v1/ai/expert/update/${id}`,
    method: 'post',
    data
  })
}

// 删除自定义专家
export function deleteAiExpert(id) {
  return request({
    url: `/v1/ai/expert/delete/${id}`,
    method: 'post'
  })
}
