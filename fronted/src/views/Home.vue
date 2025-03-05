<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <img src="../assets/logo.png" alt="Logo" class="logo">
        <span class="title">在线评测系统</span>
      </div>
      <el-menu mode="horizontal" :router="true" class="header-menu">
        <el-menu-item index="/problems">题目列表</el-menu-item>
        <el-menu-item index="/submissions">提交记录</el-menu-item>
        <el-menu-item index="/rank">排行榜</el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown>
          <span class="user-info">
            <el-avatar :size="32" :src="userInfo.userAvatar" />
            <span class="username">{{ userInfo.userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-container class="main-container">
      <!-- 左侧题目分类 -->
      <el-aside width="200px" class="aside">
        <el-menu
          :default-active="activeCategory"
          class="category-menu"
          @select="handleCategorySelect"
        >
          <el-menu-item index="all">全部题目</el-menu-item>
          <el-menu-item index="easy">简单</el-menu-item>
          <el-menu-item index="medium">中等</el-menu-item>
          <el-menu-item index="hard">困难</el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 中间题目列表 -->
      <el-main class="main">
        <el-table :data="problems" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="题目">
            <template #default="scope">
              <router-link :to="'/problem/' + scope.row.id" class="problem-link">
                {{ scope.row.title }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyType(scope.row.difficulty)">
                {{ scope.row.difficulty }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="acceptRate" label="通过率" width="100" />
          <el-table-column prop="submissionCount" label="提交次数" width="100" />
        </el-table>
      </el-main>

      <!-- 右侧用户信息 -->
      <el-aside width="300px" class="aside">
        <el-card class="user-card">
          <template #header>
            <div class="card-header">
              <span>用户信息</span>
            </div>
          </template>
          <div class="user-stats">
            <div class="stat-item">
              <span class="label">已解决题目</span>
              <span class="value">{{ userStats.solvedCount }}</span>
            </div>
            <div class="stat-item">
              <span class="label">提交次数</span>
              <span class="value">{{ userStats.submissionCount }}</span>
            </div>
            <div class="stat-item">
              <span class="label">通过率</span>
              <span class="value">{{ userStats.acceptRate }}%</span>
            </div>
          </div>
        </el-card>

        <el-card class="recent-submissions">
          <template #header>
            <div class="card-header">
              <span>最近提交</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="submission in recentSubmissions"
              :key="submission.id"
              :timestamp="submission.submitTime"
              :type="getSubmissionType(submission.status)"
            >
              {{ submission.problemTitle }} - {{ submission.status }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/user'
import { problemApi, type Problem, type Submission, type UserStats } from '../api/problem'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userInfo = ref({
  userName: '',
  userAvatar: ''
})
const activeCategory = ref('all')
const problems = ref<Problem[]>([])
const userStats = ref<UserStats>({
  solvedCount: 0,
  submissionCount: 0,
  acceptRate: 0
})
const recentSubmissions = ref<Submission[]>([])

// 获取用户信息
const getUserInfo = async () => {
  try {
    const data = await userApi.getCurrentUser()
    userInfo.value = {
      userName: data.userName,
      userAvatar: data.userAvatar
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取题目列表
const getProblems = async () => {
  try {
    const data = await problemApi.getProblems(activeCategory.value)
    problems.value = data
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  }
}

// 获取用户统计信息
const getUserStats = async () => {
  try {
    const data = await problemApi.getUserStats()
    userStats.value = data
  } catch (error) {
    console.error('获取用户统计信息失败:', error)
    ElMessage.error('获取用户统计信息失败')
  }
}

// 获取最近提交记录
const getRecentSubmissions = async () => {
  try {
    const data = await problemApi.getRecentSubmissions()
    recentSubmissions.value = data
  } catch (error) {
    console.error('获取最近提交记录失败:', error)
    ElMessage.error('获取最近提交记录失败')
  }
}

// 处理题目分类选择
const handleCategorySelect = (category: string) => {
  activeCategory.value = category
  getProblems()
}

// 获取难度标签类型
const getDifficultyType = (difficulty: Problem['difficulty']) => {
  const types: Record<Problem['difficulty'], string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}

// 获取提交状态类型
const getSubmissionType = (status: Submission['status']) => {
  const types: Record<Submission['status'], string> = {
    'Accepted': 'success',
    'Wrong Answer': 'danger',
    'Time Limit Exceeded': 'warning',
    'Memory Limit Exceeded': 'warning',
    'Runtime Error': 'danger',
    'Compile Error': 'info'
  }
  return types[status] || 'info'
}

// 处理个人中心
const handleProfile = () => {
  router.push('/profile')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    // TODO: 实现退出登录的API调用
    localStorage.removeItem('token')
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(() => {
  getUserInfo()
  getProblems()
  getUserStats()
  getRecentSubmissions()
})
</script>

<style scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #dcdfe6;
  background-color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.logo {
  height: 32px;
  margin-right: 10px;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.header-menu {
  flex: 1;
  justify-content: center;
  border-bottom: none;
}

.header-right {
  margin-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.aside {
  background-color: #fff;
  border-right: 1px solid #dcdfe6;
}

.category-menu {
  border-right: none;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
}

.problem-link {
  color: #409eff;
  text-decoration: none;
}

.problem-link:hover {
  color: #66b1ff;
}

.user-card,
.recent-submissions {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  color: #606266;
}

.value {
  font-weight: bold;
  color: #409eff;
}
</style> 