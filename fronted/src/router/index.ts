import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Problems from '../views/Problems.vue'
import Profile from '../views/Profile.vue'
import ProblemDetail from '../views/ProblemDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'Home',
      component: Home,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/problems',
      name: 'Problems',
      component: Problems,
      meta: { requiresAuth: true }
    },
    {
      path: '/problems/:id',
      name: 'ProblemDetail',
      component: ProblemDetail,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  console.log('路由守卫触发:', {
    to: to.path,
    from: from.path,
    requiresAuth: to.meta.requiresAuth
  })
  
  const token = localStorage.getItem('token')
  console.log('当前token:', token)
  
  // 如果需要认证
  if (to.meta.requiresAuth) {
    if (!token) {
      console.log('需要认证但无token，重定向到登录页')
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    console.log('有token，允许访问')
    next()
    return
  }
  
  // 如果已登录且访问登录页，重定向到首页
  if (token && to.path === '/login') {
    console.log('已登录用户访问登录页，重定向到首页')
    next('/home')
    return
  }
  
  console.log('其他情况，正常访问')
  next()
})

export default router 