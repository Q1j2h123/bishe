<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElCarousel, ElCarouselItem } from 'element-plus'
import { User, Key, Switch, ArrowRight, Trophy, Edit, List, Promotion } from '@element-plus/icons-vue'
import { problemApi } from '@/api/problem'
import { dashboardApi } from '@/api/dashboard'
import type { ProblemVO } from '@/api/problem'
import type { HomeStats } from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = ref(false)
const dailyProblem = ref<ProblemVO | null>(null)
const statistics = ref({
  totalProblems: 0,
  totalUsers: 0,
  totalSubmissions: 0
})
const dataLoading = ref(false)

// 获取每日推荐题目
// const fetchDailyProblem = async () => {
//   try {
//     const res = await problemApi.getDailyProblem()
//     if (res.code === 0 && res.data) {
//       dailyProblem.value = res.data
//     }
//   } catch (error) {
//     console.error('获取每日推荐题目失败', error)
//   }
// }
const fetchDailyProblem = async () => {
     try {
       // 只有登录后才获取每日推荐
       if (userStore.token) {
         const res = await problemApi.getDailyProblem()
         if (res.code === 0 && res.data) {
           dailyProblem.value = res.data
         }
       }
     } catch (error) {
       console.error('获取每日推荐题目失败', error)
     }
   }
// 获取统计数据
const fetchStatistics = async () => {
  dataLoading.value = true
  try {
    if(userStore.token){
    const res = await dashboardApi.getHomeStats()
    if (res.code === 0 && res.data) {
      statistics.value = res.data
    }} else {
      // 如果获取失败，使用默认值
      statistics.value = {
        totalProblems: 0,
        totalUsers: 0,
        totalSubmissions: 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    // 如果API不存在或发生错误，使用默认值
    statistics.value = {
      totalProblems: 0,
      totalUsers: 0,
      totalSubmissions: 0
    }
  } finally {
    dataLoading.value = false
  }
}

// 初始化页面数据
onMounted(async () => {
  // 尝试加载用户信息
  if (userStore.token) {
    isLoggedIn.value = true
    if (!userStore.currentUser) {
      try {
        await userStore.getCurrentUser()
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    }
  }
  
  // 获取每日推荐题目
  fetchDailyProblem()
  
  // 获取最新统计数据
  fetchStatistics()
})

// 处理登录/注册
const navigateToLogin = () => {
  router.push('/login')
}

const navigateToRegister = () => {
  router.push('/register')
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  isLoggedIn.value = false
  ElMessage.success('退出登录成功')
}

// 添加查看每日题目详情的方法
const viewDailyProblem = () => {
  if (!dailyProblem.value || !dailyProblem.value.id) {
    ElMessage.warning('题目信息不完整，无法查看详情')
    return
  }
  
  const problemId = dailyProblem.value.id
  const problemType = dailyProblem.value.type
  
  // 根据题目类型跳转到对应的详情页面
  if (problemType === 'CHOICE') {
    router.push(`/problem/choice/${problemId}`)
  } else if (problemType === 'JUDGE') {
    router.push(`/problem/judge/${problemId}`)
  } else if (problemType === 'PROGRAM') {
    router.push(`/problem/program/${problemId}`)
  } else {
    // 如果类型不确定，先跳转到通用详情页
    router.push(`/problem/${problemId}`)
  }
}
</script>

<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="logo" @click="router.push('/home')">
        <h1>面试刷题平台</h1>
      </div>
      
      <!-- 主导航 -->
      <div class="main-nav">
        <el-button text :type="$route.path === '/home' ? 'primary' : 'default'" @click="router.push('/home')">首页</el-button>
        <el-button text :type="$route.path === '/problems' ? 'primary' : 'default'" @click="router.push('/problems')">题库</el-button>
        <el-button text :type="$route.path === '/my-submissions' ? 'primary' : 'default'" @click="router.push('/my-submissions')">提交记录</el-button>
        <!-- <el-button text :type="$route.path === '/leaderboard' ? 'primary' : 'default'" @click="router.push('/leaderboard')">排行榜</el-button> -->
      </div>
      
      <!-- 用户区域 -->
      <div class="user-area">
        <template v-if="isLoggedIn && userStore.currentUser">
          <el-dropdown>
            <span class="user-dropdown-link">
              <el-avatar 
                :size="32" 
                :src="userStore.currentUser.userAvatar || ''" 
                :icon="User"
              />
              <span class="username">{{ userStore.currentUser.userName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                
                <el-dropdown-item @click="router.push('/user-center')">
                  <el-icon><Promotion /></el-icon>
                  <span>个人数据中心</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.currentUser.userRole === 'admin'" @click="router.push('/admin/dashboard')">
                  <el-icon><List /></el-icon>
                  <span>管理控制台</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><Switch /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="navigateToLogin">登录</el-button>
          <el-button size="small" @click="navigateToRegister">注册</el-button>
        </template>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main">
      <!-- 顶部横幅 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1>提升你的编程技能</h1>
          <p>挑战自我，解决各种难度的编程题目，提高你的编程能力和算法思维</p>
          <el-button type="primary" size="large" @click="router.push('/problems')">
            开始挑战
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalProblems }}</div>
            <div class="stat-label">题目总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalUsers }}</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalSubmissions }}</div>
            <div class="stat-label">提交次数</div>
          </div>
        </div>
      </div>
      
      <!-- 特色内容区 -->
      <div class="features-section">
        <h2 class="section-title">平台特色</h2>
        
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <el-icon class="feature-icon"><Edit /></el-icon>
              <h3>多种题型</h3>
              <p>包含选择题、判断题和编程题，覆盖各种编程知识点和技能测试</p>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <el-icon class="feature-icon"><Trophy /></el-icon>
              <h3>实时排行</h3>
              <p>查看解题排行榜，与其他用户比拼，激励自己不断进步</p>
            </div>
          </el-col>
          
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <el-icon class="feature-icon"><Promotion /></el-icon>
              <h3>就业导向</h3>
              <p>题目按照不同岗位类型分类，针对性提升岗位相关的编程技能</p>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 每日推荐区 -->
      <div class="daily-section" v-if="dailyProblem">
        <h2 class="section-title">每日推荐题目</h2>
        
        <el-card class="daily-problem-card">
          <div class="daily-problem-header">
            <h3 class="daily-problem-title">{{ dailyProblem.title }}</h3>
            <div class="daily-problem-tags">
              <el-tag 
                :type="dailyProblem.difficulty === 'EASY' ? 'success' : 
                      dailyProblem.difficulty === 'MEDIUM' ? 'warning' : 'danger'"
                size="small">
                {{ dailyProblem.difficulty === 'EASY' ? '简单' : 
                   dailyProblem.difficulty === 'MEDIUM' ? '中等' : '困难' }}
              </el-tag>
              <el-tag 
                type="info"
                size="small"
                class="ml-2">
                {{ dailyProblem.type === 'CHOICE' ? '选择题' : 
                   dailyProblem.type === 'JUDGE' ? '判断题' : '编程题' }}
              </el-tag>
            </div>
          </div>
          
          <div class="daily-problem-content">
            <p>{{ dailyProblem.content && dailyProblem.content.length > 150 ? dailyProblem.content.substring(0, 150) + '...' : dailyProblem.content }}</p>
          </div>
          
          <div class="daily-problem-footer">
            <el-button type="primary" @click="viewDailyProblem">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 开始挑战区 -->
      <div class="start-challenge-section">
        <div class="start-challenge-content">
          <h2>准备好接受挑战了吗？</h2>
          <p>从简单的题目开始，一步步提升你的编程技能</p>
          <el-button type="success" size="large" @click="router.push('/problems')">
            开始挑战
          </el-button>
        </div>
      </div>
    </el-main>
    
    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2025 面试刷题平台. 版权所有.</p>
      </div>
    </el-footer>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.logo {
  cursor: pointer;
  transition: color 0.3s;
}

.logo:hover {
  color: #66b1ff;
}

.logo h1 {
  margin: 0;
  font-size: 22px;
  color: #409EFF;
}

.main-nav {
  display: flex;
  gap: 16px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.main {
  flex: 1;
  padding: 0;
  background-color: var(--bg-color);
}

/* 顶部横幅样式 */
.hero-section {
  background: linear-gradient(to right, #409eff, #36d1dc);
  color: white;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin-bottom: 40px;
}

.hero-content h1 {
  font-size: 42px;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-stats {
  display: flex;
  gap: 40px;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 16px;
  opacity: 0.8;
}

/* 特色部分样式 */
.features-section {
  padding: 60px 40px;
  background-color: white;
}

.section-title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 40px;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 3px;
  background-color: var(--primary-color);
  margin: 15px auto 0;
}

.feature-card {
  padding: 30px;
  height: 100%;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 40px;
  color: var(--primary-color);
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 15px;
}

.feature-card p {
  color: var(--text-color-secondary);
  line-height: 1.6;
}

/* 每日推荐区样式 */
.daily-section {
  padding: 60px 40px;
  background-color: var(--bg-color);
}

.daily-problem-card {
  max-width: 800px;
  margin: 0 auto;
}

.daily-problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.daily-problem-title {
  font-size: 20px;
  margin: 0;
}

.daily-problem-tags {
  display: flex;
  gap: 8px;
}

.daily-problem-content {
  margin-bottom: 20px;
  line-height: 1.6;
}

.daily-problem-footer {
  display: flex;
  justify-content: flex-end;
}

/* 开始挑战区域样式 */
.start-challenge-section {
  padding: 80px 40px;
  background: linear-gradient(to right, #6dd5ed, #2193b0);
  color: white;
  text-align: center;
}

.start-challenge-content {
  max-width: 600px;
  margin: 0 auto;
}

.start-challenge-content h2 {
  font-size: 32px;
  margin-bottom: 20px;
}

.start-challenge-content p {
  font-size: 18px;
  margin-bottom: 30px;
  opacity: 0.9;
}

/* 页脚样式 */
.footer {
  background-color: #2c3e50;
  color: white;
  padding: 20px 0;
  text-align: center;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.ml-2 {
  margin-left: 8px;
}

@media (max-width: 768px) {
  .hero-section {
    padding: 40px 20px;
  }
  
  .hero-content h1 {
    font-size: 32px;
  }
  
  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }
  
  .features-section,
  .daily-section {
    padding: 40px 20px;
  }
}
</style> 