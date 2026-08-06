import request from '@/utils/request'

// 列出所有镜像仓库
export function listRepositories() {
  return request.get('/v1/registry/repositories')
}

// 查询指定镜像的所有tags
export function listTags(repository) {
  return request.get('/v1/registry/repositories/tags', { params: { repository } })
}

// 获取镜像manifest详情
export function getManifest(repository, tag) {
  return request.get('/v1/registry/repositories/manifests', { params: { repository, tag } })
}

// 删除指定镜像tag
export function deleteImage(repository, tag) {
  return request.delete('/v1/registry/repositories/tags', { params: { repository, tag } })
}
