import request from '@/utils/request'

// 获取所有Bucket列表
export const getBucketList = () => {
  return request({
    url: '/v1/oss/buckets',
    method: 'get'
  })
}

// 创建Bucket
export const createBucket = (bucketName) => {
  return request({
    url: '/v1/oss/buckets',
    method: 'post',
    params: { bucketName }
  })
}

// 获取指定Bucket下的文件列表，支持按前缀（文件夹）过滤
export const getFileListByBucket = (bucketName, prefix) => {
  return request({
    url: `/v1/oss/buckets/${bucketName}/files`,
    method: 'get',
    params: prefix ? { prefix } : {}
  })
}

// 在指定Bucket中创建文件夹
export const createFolder = (bucketName, prefix, folderName) => {
  return request({
    url: `/v1/oss/buckets/${bucketName}/folders`,
    method: 'post',
    params: { prefix, folderName }
  })
}

// 删除指定Bucket中的对象
export const deleteObject = (bucketName, prefix, objectName) => {
  return request({
    url: `/v1/oss/buckets/${bucketName}/objects`,
    method: 'delete',
    params: { prefix, objectName }
  })
}

// 预览文本文件内容
export const previewFile = (bucketName, prefix, objectName) => {
  return request({
    url: `/v1/oss/buckets/${bucketName}/preview`,
    method: 'get',
    params: { prefix, objectName }
  })
}

// 上传文件到指定Bucket和目录
export const uploadFile = (file, bucket, prefix, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  const params = {}
  if (bucket) params.bucket = bucket
  if (prefix) params.prefix = prefix
  return request({
    url: '/v1/oss/upload',
    method: 'post',
    data: formData,
    params: Object.keys(params).length > 0 ? params : undefined,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 600000, // 大文件上传超时10分钟
    onUploadProgress: onProgress
  })
}
