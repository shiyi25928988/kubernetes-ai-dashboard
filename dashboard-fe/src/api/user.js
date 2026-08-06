import request from '@/utils/request'

// 用户注册
export const registerUser = (data) => {
  return request({
    url: '/v1/user/register',
    method: 'post',
    data: data
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/v1/user/login',
    method: 'post',
    params: data
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/v1/user/logout',
    method: 'post'
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/v1/user/update',
    method: 'post',
    data
  })
}

// 更新密码
export const updatePassword = (data) => {
  return request({
    url: '/v1/user/updatePassword',
    method: 'post',
    params: data
  })
}

// 获取当前用户信息
export const getCurrentUserInfo = () => {
  return request({
    url: '/v1/user/getCurrentUserInfo',
    method: 'get'
  })
}
