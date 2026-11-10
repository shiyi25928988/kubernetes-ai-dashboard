import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import i18n from '@/i18n'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// K8s 类接口 /v1/k8s/** 是对目标集群 API 服务器的实时转发，集群存在间歇性的 TLS 慢窗口
// （直连握手即需数秒到十余秒），统一放宽到 30s，避免慢窗口下整页接口全部超时报错
const SLOW_CLUSTER_PREFIXES = ['/v1/k8s']
request.interceptors.request.use(config => {
  const url = config.url || ''
  if (config.timeout && config.timeout !== 10000) {
    // 调用处显式指定的超时（如大文件上传）优先级更高，不覆盖
    return config
  }
  if (SLOW_CLUSTER_PREFIXES.some(p => url.startsWith(p))) {
    config.timeout = 30000
  }
  return config
})

// 统一处理401跳转登录页
const handleUnauthorized = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.replace('/login')
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['satoken'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    const t = i18n.global.t
    // 401表示未登录或token过期，立刻跳转登录页
    if (res.code === 401 || response.status === 401) {
      handleUnauthorized()
      ElMessage.error(t('login.expired'))
      return Promise.reject(new Error(t('login.expired')))
    }
    // code不等于0说明请求出错
    if (res.code !== 0) {
      ElMessage.error(res.message || t('error.requestFailed'))
      return Promise.reject(new Error(res.message || t('error.requestFailed')))
    }
    return res
  },
  error => {
    const t = i18n.global.t
    if (error.response?.status === 401) {
      handleUnauthorized()
      ElMessage.error(t('login.expired'))
      return Promise.reject(error)
    }
    ElMessage.error(error.message || t('error.networkError'))
    return Promise.reject(error)
  }
)

export default request
