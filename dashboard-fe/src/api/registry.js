import request from '@/utils/request'

// ===== 仓库源配置管理 =====

// 仓库源列表
export function listRegistryConfigs() {
  return request.get('/v1/registry/configs')
}

// 新增仓库源
export function createRegistryConfig(data) {
  return request.post('/v1/registry/configs', data)
}

// 更新仓库源（password 传空表示保持原密码）
export function updateRegistryConfig(id, data) {
  return request.put(`/v1/registry/configs/${id}`, data)
}

// 测试仓库源连接
export function testRegistryConfig(id) {
  return request.post(`/v1/registry/configs/${id}/test`)
}

// 删除仓库源
export function deleteRegistryConfig(id) {
  return request.delete(`/v1/registry/configs/${id}`)
}

// ===== 镜像浏览（registryId 不传时使用默认仓库源） =====

// 列出所有镜像仓库
export function listRepositories(registryId) {
  return request.get('/v1/registry/repositories', { params: { registryId } })
}

// 查询指定镜像的所有tags
export function listTags(repository, registryId) {
  return request.get('/v1/registry/repositories/tags', { params: { repository, registryId } })
}

// 获取镜像manifest详情
export function getManifest(repository, tag, registryId) {
  return request.get('/v1/registry/repositories/manifests', { params: { repository, tag, registryId } })
}

// 删除指定镜像tag
export function deleteImage(repository, tag, registryId) {
  return request.delete('/v1/registry/repositories/tags', { params: { repository, tag, registryId } })
}
