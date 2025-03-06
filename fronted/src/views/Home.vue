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
      <!-- 主体内容区 -->
      <el-main class="main-content">
        <!-- 搜索和筛选区域 -->
        <div class="filter-container">
          <!-- 第一行：所有筛选条件 -->
          <div class="filter-row">
            <!-- 题库选择 -->
            <el-popover
              placement="bottom-start"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-button>
                  题库
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
              </template>
              <div class="category-menu">
                <div class="category-group">
                  <div class="category-item" @click="handleCategorySelect('')">
                    <span>全部题目</span>
                  </div>
                  <div class="category-item" @click="handleCategorySelect('字符串练习')">
                    <span>字符串练习</span>
                  </div>
                  <div class="category-item" @click="handleCategorySelect('数组练习')">
                    <span>数组练习</span>
                  </div>
                  <div class="category-item hot" @click="handleCategorySelect('程序员面试金典')">
                    <span>程序员面试金典（第 6 版）</span>
                    <el-icon color="#ff6b6b"><Promotion /></el-icon>
                  </div>
                  <div class="category-item" @click="handleCategorySelect('递归练习')">
                    <span>递归练习</span>
                  </div>
                </div>
                <div class="category-group">
                  <div class="category-title">精选题集</div>
                  <div class="category-item hot" @click="handleCategorySelect('leetcode-hot-100')">
                    <span>LeetCode 热题 HOT 100</span>
                    <el-icon color="#ff6b6b"><Promotion /></el-icon>
                  </div>
                  <div class="category-item recommended" @click="handleCategorySelect('leetcode-db')">
                    <span>LeetCode 精选数据库 70</span>
                    <el-icon color="#409eff"><Star /></el-icon>
                  </div>
                  <div class="category-item recommended" @click="handleCategorySelect('leetcode-algo')">
                    <span>LeetCode 精选算法 200</span>
                    <el-icon color="#ff9f43"><Heart /></el-icon>
                  </div>
                </div>
                <div class="category-group">
                  <div class="category-title">企业题库</div>
                  <div class="category-item" @click="handleCategorySelect('bytedance')">
                    <span>腾讯精选练习 50 题</span>
                    <el-avatar :size="16" src="https://example.com/tencent-logo.png" />
                  </div>
                  <div class="category-item" @click="handleCategorySelect('leetcode-top')">
                    <span>LeetCode 精选 TOP 面试题</span>
                    <el-icon color="#ffd43b"><Trophy /></el-icon>
                  </div>
                </div>
                <div v-if="isAdmin" class="category-footer">
                  <el-button type="primary" link @click="handleAddCategory">
                    <el-icon><Plus /></el-icon>
                    添加题库
                  </el-button>
                </div>
              </div>
            </el-popover>

            <!-- 其他筛选条件 -->
            <el-select v-model="jobType" placeholder="岗位方向" clearable @change="handleFilter">
              <el-option label="全部岗位" value="" />
              <el-option-group label="前端开发">
                <el-option label="Web前端" value="WEB_FRONTEND" />
                <el-option label="移动前端" value="MOBILE_FRONTEND" />
                <el-option label="小程序开发" value="MINIAPP" />
              </el-option-group>
              <el-option-group label="后端开发">
                <el-option label="Java开发" value="JAVA" />
                <el-option label="Python开发" value="PYTHON" />
                <el-option label="Go开发" value="GOLANG" />
                <el-option label="Node.js开发" value="NODEJS" />
                <el-option label="PHP开发" value="PHP" />
              </el-option-group>
              <el-option-group label="算法">
                <el-option label="机器学习" value="ML" />
                <el-option label="深度学习" value="DL" />
                <el-option label="计算机视觉" value="CV" />
                <el-option label="自然语言处理" value="NLP" />
              </el-option-group>
              <el-option-group label="其他">
                <el-option label="测试开发" value="TEST" />
                <el-option label="运维开发" value="DEVOPS" />
                <el-option label="数据开发" value="DATA" />
              </el-option-group>
            </el-select>

            <el-select v-model="problemType" placeholder="题目类型" clearable @change="handleFilter">
              <el-option label="全部类型" value="" />
              <el-option label="选择题" value="CHOICE" />
              <el-option label="判断题" value="JUDGE" />
              <el-option label="编程题" value="PROGRAM" />
            </el-select>

            <el-select v-model="status" placeholder="状态" clearable @change="handleFilter">
              <el-option label="全部状态" value="" />
              <el-option label="已解决" value="SOLVED">
                <el-icon color="#67C23A"><CircleCheck /></el-icon>
                <span style="margin-left: 8px">已解决</span>
              </el-option>
              <el-option label="尝试过" value="ATTEMPTED">
                <el-icon color="#E6A23C"><InfoFilled /></el-icon>
                <span style="margin-left: 8px">尝试过</span>
              </el-option>
              <el-option label="未开始" value="UNSOLVED">
                <el-icon color="#909399"><CircleClose /></el-icon>
                <span style="margin-left: 8px">未开始</span>
              </el-option>
            </el-select>

            <el-select v-model="difficulty" placeholder="难度" clearable @change="handleFilter">
              <el-option label="全部难度" value="" />
              <el-option label="简单" value="EASY">
                <el-tag size="small" type="success">简单</el-tag>
              </el-option>
              <el-option label="中等" value="MEDIUM">
                <el-tag size="small" type="warning">中等</el-tag>
              </el-option>
              <el-option label="困难" value="HARD">
                <el-tag size="small" type="danger">困难</el-tag>
              </el-option>
            </el-select>

            <el-select v-model="tags" multiple collapse-tags placeholder="题目标签" clearable @change="handleFilter">
              <el-option-group label="数据结构">
                <el-option label="数组" value="array" />
                <el-option label="字符串" value="string" />
                <el-option label="链表" value="linked-list" />
                <el-option label="栈" value="stack" />
                <el-option label="队列" value="queue" />
                <el-option label="哈希表" value="hash-table" />
                <el-option label="树" value="tree" />
                <el-option label="图" value="graph" />
                <el-option label="堆" value="heap" />
              </el-option-group>
              <el-option-group label="算法">
                <el-option label="排序" value="sorting" />
                <el-option label="搜索" value="searching" />
                <el-option label="动态规划" value="dynamic-programming" />
                <el-option label="贪心" value="greedy" />
                <el-option label="回溯" value="backtracking" />
                <el-option label="分治" value="divide-and-conquer" />
                <el-option label="深度优先搜索" value="dfs" />
                <el-option label="广度优先搜索" value="bfs" />
              </el-option-group>
              <el-option-group label="前端">
                <el-option label="HTML" value="html" />
                <el-option label="CSS" value="css" />
                <el-option label="JavaScript" value="javascript" />
                <el-option label="Vue" value="vue" />
                <el-option label="React" value="react" />
                <el-option label="TypeScript" value="typescript" />
              </el-option-group>
              <el-option-group label="后端">
                <el-option label="数据库" value="database" />
                <el-option label="缓存" value="cache" />
                <el-option label="消息队列" value="message-queue" />
                <el-option label="系统设计" value="system-design" />
                <el-option label="网络" value="network" />
                <el-option label="安全" value="security" />
              </el-option-group>
            </el-select>
          </div>

          <!-- 第二行：搜索框、随机一题和重置按钮 -->
          <div class="search-row">
            <el-input
              v-model="searchQuery"
              placeholder="搜索题目、标签"
              class="search-input"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="success" @click="handleRandomProblem">
              <el-icon><Refresh /></el-icon>
              随机一题
            </el-button>
            <el-button @click="handleReset" class="reset-button">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </div>

        <!-- 题目列表 -->
        <el-table :data="problems" style="width: 100%">
          <el-table-column label="状态" width="60" align="center">
            <template #default="scope">
              <div class="problem-status">
                <el-icon v-if="scope.row.status === 'SOLVED'" color="#67C23A"><CircleCheck /></el-icon>
                <el-icon v-else-if="scope.row.status === 'ATTEMPTED'" color="#E6A23C"><InfoFilled /></el-icon>
                <el-icon v-else color="#909399"><CircleClose /></el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="题目" min-width="300">
            <template #default="scope">
              <div class="problem-title-cell">
                <span class="problem-id">{{ scope.row.id }}. </span>
                <router-link :to="`/problems/${scope.row.id}`" class="problem-link">
                  {{ scope.row.title }}
                </router-link>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="题目类型" width="100" align="center">
            <template #default="scope">
              <el-tag size="small">{{ getProblemType(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                {{ getDifficultyText(scope.row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="acceptRate" label="通过率" width="100" align="center" />
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-main>

      <!-- 右侧用户信息 -->
      <el-aside width="300px" class="user-aside">
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
import { ref, onMounted, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Refresh,
  List,
  Monitor,
  Connection,
  Share,
  CircleCheck,
  CircleClose,
  InfoFilled,
  RefreshLeft,
  ArrowDown,
  Plus,
  Star,
  Trophy,
  Promotion
} from '@element-plus/icons-vue'
import { userApi } from '../api/user'
import { problemApi, type Problem, type Submission, type UserStats } from '../api/problem'
import type { UserInfo } from '../types/api'

const router = useRouter()
const route = useRoute()
const userInfo = ref<UserInfo>({
  id: 0,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
  createTime: ''
})

// 筛选条件
const searchQuery = ref('')
const difficulty = ref('')
const problemType = ref('')
const jobType = ref('')
const sortBy = ref('createTime')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const currentCategory = ref('')

// 数据
const problems = ref<Problem[]>([])
const userStats = ref<UserStats>({
  solvedCount: 0,
  submissionCount: 0,
  acceptRate: 0
})
const recentSubmissions = ref<Submission[]>([])

// 添加新的响应式数据
const status = ref('')
const tags = ref([])

// 判断是否为管理员
const isAdmin = computed(() => userInfo.value.userRole === 'ADMIN')

// 使用自定义的Heart图标组件
const Heart = {
  name: 'Heart',
  render() {
    return h('svg', {
      viewBox: '0 0 1024 1024',
      width: '1em',
      height: '1em'
    }, [
      h('path', {
        fill: 'currentColor',
        d: 'M923 283.6c-13.4-31.1-32.6-58.9-56.9-82.8-24.3-23.8-52.5-42.4-84-55.5-32.5-13.5-66.9-20.3-102.4-20.3-49.3 0-97.4 13.5-139.2 39-10 6.1-19.5 12.8-28.5 20.1-9-7.3-18.5-14-28.5-20.1-41.8-25.5-89.9-39-139.2-39-35.5 0-69.9 6.8-102.4 20.3-31.4 13-59.7 31.7-84 55.5-24.4 23.9-43.5 51.7-56.9 82.8-13.9 32.3-21 66.6-21 101.9 0 33.3 6.8 68 20.3 103.3 11.3 29.5 27.5 60.1 48.2 91 32.8 48.9 77.9 99.9 133.9 151.6 92.8 85.7 184.7 144.9 188.6 147.3l23.7 15.2c10.5 6.7 24 6.7 34.5 0l23.7-15.2c3.9-2.5 95.7-61.6 188.6-147.3 56-51.7 101.1-102.7 133.9-151.6 20.7-30.9 37-61.5 48.2-91 13.5-35.3 20.3-70 20.3-103.3 0.1-35.3-7-69.6-20.9-101.9z'
      })
    ])
  }
}

// 获取题目列表
const getProblems = async () => {
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      search: searchQuery.value,
      category: currentCategory.value,
      difficulty: difficulty.value,
      status: status.value,
      type: problemType.value,
      jobType: jobType.value,
      tags: tags.value.join(','),
      sortBy: sortBy.value
    }
    const data = await problemApi.getProblems(params)
    problems.value = data.records
    total.value = data.total
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  }
}

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1
  getProblems()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  getProblems()
}

// 处理排序
const handleSort = () => {
  currentPage.value = 1
  getProblems()
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getProblems()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getProblems()
}

// 获取题目类型文本
const getProblemType = (type: string) => {
  const types: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return types[type] || type
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const difficulties: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return difficulties[difficulty] || difficulty
}

// 获取难度标签类型
const getDifficultyType = (difficulty: string) => {
  const types: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return types[difficulty] || 'info'
}

// 获取提交状态类型
const getSubmissionType = (status: Submission['status']) => {
  const types: Record<Submission['status'], string> = {
    'ACCEPTED': 'success',
    'WRONG_ANSWER': 'danger',
    'TIME_LIMIT_EXCEEDED': 'warning',
    'MEMORY_LIMIT_EXCEEDED': 'warning',
    'RUNTIME_ERROR': 'danger',
    'COMPILE_ERROR': 'info'
  }
  return types[status] || 'info'
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response && response.data) {
      userInfo.value = response.data
    } else {
      throw new Error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
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

// 处理个人中心
const handleProfile = () => {
  router.push('/profile')
}

// 处理退出登录
const handleLogout = async () => {
  try {
    userApi.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 处理随机一题
const handleRandomProblem = async () => {
  try {
    const response = await problemApi.getRandomProblem()
    if (response) {
      router.push(`/problem/${response.id}`)
    }
  } catch (error) {
    console.error('获取随机题目失败:', error)
    ElMessage.error('获取随机题目失败')
  }
}

// 处理重置
const handleReset = () => {
  searchQuery.value = ''
  currentCategory.value = ''
  difficulty.value = ''
  status.value = ''
  problemType.value = ''
  jobType.value = ''
  tags.value = []
  sortBy.value = 'createTime'
  currentPage.value = 1
  getProblems()
}

// 处理题库选择
const handleCategorySelect = (category: string) => {
  currentCategory.value = category
  handleFilter()
}

// 处理添加题库
const handleAddCategory = () => {
  // TODO: 实现添加题库的逻辑
  ElMessage.info('添加题库功能开发中...')
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

.main-content {
  padding: 16px;
  background-color: #f5f7fa;
}

.filter-container {
  background-color: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-row .el-select {
  width: 160px;
}

.filter-row .el-select:last-child {
  width: 240px;
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
  max-width: 800px;
}

.reset-button {
  padding: 8px 16px;
}

.problem-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.problem-status {
  display: flex;
  justify-content: center;
}

.problem-id {
  color: #909399;
}

.problem-link {
  color: #409eff;
  text-decoration: none;
}

.problem-link:hover {
  color: #66b1ff;
}

.tags-container {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding: 4px 0;
}

.tag {
  flex-shrink: 0;
}

.user-aside {
  background-color: #fff;
  border-left: 1px solid #dcdfe6;
  padding: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.category-menu {
  padding: 8px 0;
}

.category-group {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.category-group:last-child {
  border-bottom: none;
}

.category-title {
  padding: 0 16px 8px;
  color: #909399;
  font-size: 13px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item.hot {
  color: #ff6b6b;
}

.category-item.recommended {
  color: #409eff;
}

.category-footer {
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

/* 调整表格样式 */
.el-table {
  border-radius: 4px;
  overflow: hidden;
}
</style> 