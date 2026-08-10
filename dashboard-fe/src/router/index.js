import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: 'route.login' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: 'route.register' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: 'route.dashboard', requiresAuth: true }
  },
  {
    path: '/pod',
    name: 'Pod',
    component: () => import('@/views/Pod.vue'),
    meta: { title: 'route.pod', requiresAuth: true }
  },
  {
    path: '/service',
    name: 'Service',
    component: () => import('@/views/Service.vue'),
    meta: { title: 'route.service', requiresAuth: true }
  },
  {
    path: '/ingress',
    name: 'Ingress',
    component: () => import('@/views/Ingress.vue'),
    meta: { title: 'route.ingress', requiresAuth: true }
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

// 路由守卫（页面标题由 App.vue 根据 i18n 语言统一设置，这里只做鉴权）
router.beforeEach((to, from, next) => {
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
