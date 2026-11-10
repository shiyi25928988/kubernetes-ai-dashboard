import request from '@/utils/request'

// 分页查询知识文档
export const listKnowledgeDocs = (params) => {
  return request({
    url: '/v1/knowledge/docs',
    method: 'get',
    params
  })
}

// 文档详情
export const getKnowledgeDoc = (id) => {
  return request({
    url: `/v1/knowledge/docs/${id}`,
    method: 'get'
  })
}

// 全部分类列表
export const listKnowledgeCategories = () => {
  return request({
    url: '/v1/knowledge/categories',
    method: 'get'
  })
}

// 全文检索（Lucene+IK）
export const searchKnowledge = (keyword, limit = 10) => {
  return request({
    url: '/v1/knowledge/search',
    method: 'get',
    params: { keyword, limit }
  })
}

// 创建文档
export const createKnowledgeDoc = (data) => {
  return request({
    url: '/v1/knowledge/docs',
    method: 'post',
    data
  })
}

// 更新文档
export const updateKnowledgeDoc = (data) => {
  return request({
    url: '/v1/knowledge/docs',
    method: 'put',
    data
  })
}

// 删除文档
export const deleteKnowledgeDoc = (id) => {
  return request({
    url: `/v1/knowledge/docs/${id}/delete`,
    method: 'post'
  })
}
