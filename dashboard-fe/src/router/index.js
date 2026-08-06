import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '控制台', requiresAuth: true }
  },
  {
    path: '/pod',
    name: 'Pod',
    component: () => import('@/views/Pod.vue'),
    meta: { title: 'Pod管理', requiresAuth: true }
  },
  {
    path: '/service',
    name: 'Service',
    component: () => import('@/views/Service.vue'),
    meta: { title: 'Service管理', requiresAuth: true }
  },
  {
    path: '/ingress',
    name: 'Ingress',
    component: () => import('@/views/Ingress.vue'),
    meta: { title: 'Ingress管理', requiresAuth: true }
  },
  {
    path: '/',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'MPI调度平台'

  const token = localStorage.getItem('token')
  // 如果是登录或注册页面，直接放行
  if (to.path === '/login' || to.path === '/register') {
    // 如果已经登录了，跳转到控制台
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 需要登录的页面
    if (to.meta.requiresAuth && !token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
