import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/admin/import',
      name: 'ImportProblems',
      component: () => import('@/views/admin/ImportProblems.vue'),
      meta: {
        title: '导入题目',
        requiresAuth: true,
        requiresAdmin: true // 需要管理员权限
      }
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: false, title: '首页' }
    },
    {
      path: '/problems',
      name: 'Problems',
      component: () => import('@/views/Problems.vue'),
      meta: { requiresAuth: true, title: '题目列表' }
    },
    {
      path: '/problem/:id',
      name: 'ProblemDetail',
      component: () => import('@/views/ProblemDetail.vue'),
      meta: { requiresAuth: false, title: '题目详情' }
    },
    {
      path: '/problem/choice/:id',
      name: 'ChoiceProblemDetail',
      component: () => import('@/views/ChoiceProblemDetail.vue'),
      meta: { requiresAuth: false, title: '选择题详情' }
    },
    {
      path: '/problem/judge/:id',
      name: 'JudgeProblemDetail',
      component: () => import('@/views/JudgeProblemDetail.vue'),
      meta: { requiresAuth: false, title: '判断题详情' }
    },
    {
      path: '/problem/program/:id',
      name: 'ProgramProblemDetail',
      component: () => import('@/views/ProgramProblemDetail.vue'),
      meta: { requiresAuth: false, title: '编程题详情' }
    },
    {
      path: '/my-submissions',
      name: 'UserSubmissions',
      component: () => import('@/views/UserSubmissions.vue'),
      meta: { requiresAuth: true, title: '我的提交记录' }
    },
    {
      path: '/submission/:id',
      name: 'SubmissionDetail',
      component: () => import('@/views/SubmissionDetail.vue'),
      meta: { requiresAuth: true, title: '提交详情' }
    },
    {
      path: '/leaderboard',
      name: 'Leaderboard',
      component: () => import('@/views/Leaderboard.vue'),
      meta: { requiresAuth: true, title: '排行榜' }
    },
    {
      path: '/user-center',
      name: 'UserCenter',
      component: () => import('@/views/UserCenter.vue'),
      meta: { requiresAuth: true, title: '个人中心' }
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/Layout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '管理控制台' }
        },
        {
          path: 'problems',
          name: 'AdminProblems',
          component: () => import('@/views/admin/Problems.vue'),
          meta: { title: '题目管理' }
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/Users.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'problem/add/:type',
          name: 'AdminProblemAdd',
          component: () => import('@/views/admin/problem/ProblemEdit.vue'),
          meta: { title: '添加题目' }
        },
        {
          path: 'problem/edit/:type/:id',
          name: 'AdminProblemEdit',
          component: () => import('@/views/admin/problem/ProblemEdit.vue'),
          meta: { title: '编辑题目' }
        }
      ]
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果已登录且要去登录/注册页，重定向到首页
  if (userStore.token && (to.path === '/login' || to.path === '/register' || to.path === '/admin/login')) {
    if (userStore.currentUser?.userRole === 'admin') {
      next('/admin/dashboard')
    } else {
      next('/home')
    }
    return
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && (!userStore.currentUser || userStore.currentUser.userRole !== 'admin')) {
    ElMessage.error('需要管理员权限')
    next({ path: '/admin/login' })
    return
  }
  
  // 进入需要登录的页面，但未登录时，重定向到登录页
  if (to.meta.requiresAuth && !userStore.token) {
    ElMessage.warning('登录已过期，请重新登录')
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  next()
})


export default router 