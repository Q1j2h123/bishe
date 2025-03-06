<template>
  <div class="problem-detail">
    <!-- 头部信息 -->
    <div class="problem-header">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <span class="problem-id">#{{ problem.id }}</span>
            <span class="problem-title">{{ problem.title }}</span>
            <el-tag :type="getDifficultyType(problem.difficulty)" size="small" class="difficulty-tag">
              {{ getDifficultyText(problem.difficulty) }}
            </el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <!-- 主要内容区 -->
    <el-container class="main-container">
      <el-main class="problem-content">
        <!-- 题目描述卡片 -->
        <el-card class="description-card">
          <template #header>
            <div class="card-header">
              <span>题目描述</span>
              <div class="problem-stats">
                <el-tooltip content="通过率" placement="top">
                  <span class="stat-item">
                    <el-icon><CircleCheckFilled /></el-icon>
                    {{ problem.acceptRate }}%
                  </span>
                </el-tooltip>
                <el-tooltip content="提交次数" placement="top">
                  <span class="stat-item">
                    <el-icon><Document /></el-icon>
                    {{ problem.submissionCount }}
                  </span>
                </el-tooltip>
              </div>
            </div>
          </template>
          <div class="problem-description" v-html="problem.content"></div>
        </el-card>

        <!-- 答题区域 -->
        <el-card class="answer-card">
          <template #header>
            <div class="card-header">
              <span>答题区域</span>
            </div>
          </template>
          
          <!-- 根据题目类型显示不同的答题组件 -->
          <component
            :is="currentAnswerComponent"
            :problem="problem"
            @submit="handleSubmit"
          />
        </el-card>
      </el-main>

      <!-- 右侧信息栏 -->
      <el-aside width="300px" class="problem-aside">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>提交记录</span>
            </div>
          </template>
          <el-table :data="submissions" style="width: 100%">
            <el-table-column prop="submitTime" label="提交时间" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getSubmissionStatusType(scope.row.status)" size="small">
                  {{ getSubmissionStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineAsyncComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, Document } from '@element-plus/icons-vue'
import { problemApi, type Problem, type Submission } from '../api/problem'
import { formatDate } from '../utils/format'

// 路由
const route = useRoute()
const router = useRouter()

// 数据
const problem = ref<Problem>({
  id: 0,
  title: '',
  content: '',
  type: 'CHOICE',
  jobType: 'FRONTEND',
  tags: '',
  difficulty: 'EASY',
  acceptRate: '0',
  submissionCount: 0,
  status: 'UNSOLVED',
  createTime: '',
  updateTime: ''
})
const submissions = ref<Submission[]>([])

// 根据题目类型动态加载答题组件
const currentAnswerComponent = computed(() => {
  switch (problem.value.type) {
    case 'CHOICE':
      return defineAsyncComponent(() => import('../components/answer/ChoiceAnswer.vue'))
    case 'JUDGE':
      return defineAsyncComponent(() => import('../components/answer/JudgeAnswer.vue'))
    case 'PROGRAM':
      return defineAsyncComponent(() => import('../components/answer/ProgramAnswer.vue'))
    default:
      return null
  }
})

// 获取题目信息
const getProblemDetail = async () => {
  try {
    const problemId = parseInt(route.params.id as string)
    const data = await problemApi.getProblemDetail(problemId)
    problem.value = data
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 获取提交记录
const getSubmissions = async () => {
  try {
    const problemId = parseInt(route.params.id as string)
    // TODO: 实现获取指定题目的提交记录
    submissions.value = []
  } catch (error) {
    console.error('获取提交记录失败:', error)
    ElMessage.error('获取提交记录失败')
  }
}

// 处理提交答案
const handleSubmit = async (answer: any) => {
  try {
    // TODO: 根据题目类型处理不同的提交逻辑
    ElMessage.success('提交成功')
    await getSubmissions() // 刷新提交记录
  } catch (error) {
    console.error('提交答案失败:', error)
    ElMessage.error('提交答案失败')
  }
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

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const difficulties: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return difficulties[difficulty] || difficulty
}

// 获取提交状态标签类型
const getSubmissionStatusType = (status: Submission['status']) => {
  const types: Record<string, string> = {
    'ACCEPTED': 'success',
    'WRONG_ANSWER': 'danger',
    'TIME_LIMIT_EXCEEDED': 'warning',
    'MEMORY_LIMIT_EXCEEDED': 'warning',
    'RUNTIME_ERROR': 'danger',
    'COMPILE_ERROR': 'info'
  }
  return types[status] || 'info'
}

// 获取提交状态文本
const getSubmissionStatusText = (status: Submission['status']) => {
  const texts: Record<string, string> = {
    'ACCEPTED': '通过',
    'WRONG_ANSWER': '错误',
    'TIME_LIMIT_EXCEEDED': '超时',
    'MEMORY_LIMIT_EXCEEDED': '内存超限',
    'RUNTIME_ERROR': '运行错误',
    'COMPILE_ERROR': '编译错误'
  }
  return texts[status] || status
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  getProblemDetail()
  getSubmissions()
})
</script>

<style scoped>
.problem-detail {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.problem-header {
  background-color: #fff;
  padding: 16px;
  border-bottom: 1px solid #dcdfe6;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.problem-id {
  color: #909399;
  font-size: 16px;
}

.problem-title {
  font-size: 16px;
  font-weight: bold;
}

.difficulty-tag {
  margin-left: auto;
}

.main-container {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}

.problem-content {
  padding-right: 16px;
}

.description-card,
.answer-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.problem-description {
  line-height: 1.6;
}

.problem-aside {
  background-color: #fff;
  border-left: 1px solid #dcdfe6;
  padding-left: 16px;
}

.info-card {
  margin-bottom: 16px;
}
</style> 