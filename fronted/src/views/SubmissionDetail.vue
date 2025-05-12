<template>
  <div class="submission-detail-container">
    <el-header class="header">
      <div class="logo" @click="router.push('/home')">
        <h1>面试刷题平台</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text @click="router.push('/problems')">题目列表</el-button>
        <el-button text @click="router.push('/my-submissions')">提交记录</el-button>
        <el-button text @click="router.push('/user-center')">个人中心</el-button>
      </div>
    </el-header>
    
    <el-card class="submission-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="title-actions">
            <h2>提交详情</h2>
            <el-button @click="router.push('/my-submissions')" text type="primary">
              <el-icon><Back /></el-icon> 返回列表
            </el-button>
          </div>
          <div class="submission-meta">
            <el-tag v-if="submission?.type" :type="getTypeTagType(submission.type)" effect="plain">
              {{ formatType(submission.type) }}
            </el-tag>
            <el-tag v-if="submission?.status" :type="getStatusTagType(submission.status)">
              {{ formatStatus(submission.status) }}
            </el-tag>
          </div>
        </div>
      </template>
      
      <div v-if="submission" class="submission-content">
        <!-- 题目基本信息 -->
        <div class="section-title">基本信息</div>
        <div class="info-grid">
          <div class="info-row">
            <div class="info-label">题目：</div>
            <div class="info-value">
              <router-link :to="`/problem/${submission.problemId}`">
                {{ submission.problemTitle }}
              </router-link>
            </div>
          </div>
          
          <div class="info-row">
            <div class="info-label">提交时间：</div>
            <div class="info-value">{{ formatDateTime(submission.submissionTime) }}</div>
          </div>
          
          <div class="info-row">
            <div class="info-label">难度：</div>
            <div class="info-value">
              <el-tag v-if="submission.difficulty" :type="getDifficultyTagType(submission.difficulty)" size="small">
                {{ formatDifficulty(submission.difficulty) }}
              </el-tag>
              <span v-else>-</span>
            </div>
          </div>
          
          <div class="info-row">
            <div class="info-label">岗位：</div>
            <div class="info-value">{{ submission.jobType || '-' }}</div>
          </div>
          
          <div class="info-row">
            <div class="info-label">标签：</div>
            <div class="info-value tags-container">
              <el-tag 
                v-for="tag in submission.problemTags" 
                :key="tag" 
                size="small" 
                effect="light"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!submission.problemTags || submission.problemTags.length === 0">-</span>
            </div>
          </div>
        </div>
        
        <!-- 选择/判断题详情 -->
        <template v-if="submission.type === 'CHOICE' || submission.type === 'JUDGE'">
          <div v-if="choiceJudgeSubmission" class="choice-judge-details">
            <div class="section-title">答题详情</div>
            
            <div class="info-grid">
              <div class="info-row">
                <div class="info-label">您的答案：</div>
                <div class="info-value">
                  <span 
                    :class="{ 
                      'text-success': submission.status === 'ACCEPTED', 
                      'text-danger': submission.status === 'WRONG_ANSWER' 
                    }"
                  >
                    {{ formatSubmissionAnswer(choiceJudgeSubmission.answer, submission.type) }}
                  </span>
                </div>
              </div>
              
              <div v-if="choiceJudgeSubmission.canViewAnalysis" class="info-row">
                <div class="info-label">正确答案：</div>
                <div class="info-value text-success">
                  {{ formatSubmissionAnswer(choiceJudgeSubmission.correctAnswer, submission.type) }}
                </div>
              </div>
            </div>
            
            <div v-if="choiceJudgeSubmission.canViewAnalysis && choiceJudgeSubmission.analysis" class="analysis-section">
              <div class="section-title">解析</div>
              <div class="analysis-content" v-html="formatContent(choiceJudgeSubmission.analysis)"></div>
            </div>
          </div>
          
          <div v-else-if="!loading" class="empty-data">
            <el-alert title="无法加载详细信息" type="warning" :closable="false" />
          </div>
        </template>
        
        <!-- 编程题详情 -->
        <template v-else-if="submission.type === 'PROGRAM'">
          <div v-if="programSubmission" class="program-details">
            <div class="section-title">提交详情</div>
            
            <div class="info-grid">
              <div class="info-row">
                <div class="info-label">编程语言：</div>
                <div class="info-value">{{ formatLanguage(programSubmission.language) }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">执行时间：</div>
                <div class="info-value">{{ programSubmission.executeTime ? `${programSubmission.executeTime} ms` : '-' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">内存使用：</div>
                <div class="info-value">{{ programSubmission.memoryUsage ? `${Math.round(programSubmission.memoryUsage / 1024)} MB` : '-' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">测试用例：</div>
                <div class="info-value">
                  <span v-if="programSubmission.passedTestCases !== undefined && programSubmission.totalTestCases !== undefined">
                    通过 {{ programSubmission.passedTestCases }}/{{ programSubmission.totalTestCases }}
                  </span>
                  <span v-else>-</span>
                </div>
              </div>
            </div>
            
            <div class="code-section">
              <div class="section-title">代码</div>
              <pre class="code-block">{{ programSubmission.code }}</pre>
            </div>
            
            <div v-if="programSubmission.testcaseResults" class="testcase-section">
              <div class="section-title">测试用例运行结果</div>
              <pre class="testcase-block">{{ formatTestcaseResults(programSubmission.testcaseResults) }}</pre>
            </div>
            
            <div v-if="programSubmission.errorMessage" class="error-section">
              <div class="section-title">错误信息</div>
              <pre class="error-block">{{ programSubmission.errorMessage }}</pre>
            </div>
            
            <!-- 标准答案显示 -->
            <div v-if="programSubmission.standardSolution" class="standard-solution-section">
              <div class="section-title">标准答案</div>
              <div v-for="(code, language) in programSubmission.standardSolution" :key="language" class="solution-item">
                <div class="language-title">{{ formatLanguage(language) }}</div>
                <pre class="code-block">{{ code }}</pre>
              </div>
            </div>
          </div>
          
          <div v-else-if="!loading" class="empty-data">
            <el-alert title="无法加载详细信息" type="warning" :closable="false" />
          </div>
        </template>
      </div>
      
      <div v-else-if="!loading" class="empty-data">
        <el-empty description="未找到提交记录" />
        <div class="back-link">
          <router-link to="/my-submissions">返回提交列表</router-link>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { submissionApi, type SubmissionDetailVO, type ChoiceJudgeSubmissionDetailVO, type ProgramSubmissionDetailVO } from '@/api/submission'
import DOMPurify from 'dompurify'

// 路由
const route = useRoute()
const router = useRouter()

// 提交数据
const submission = ref<SubmissionDetailVO | null>(null)
const choiceJudgeSubmission = ref<ChoiceJudgeSubmissionDetailVO | null>(null)
const programSubmission = ref<ProgramSubmissionDetailVO | null>(null)
const loading = ref(false)

// 加载提交详情
const loadSubmissionDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('提交ID不能为空')
    router.push('/my-submissions')
    return
  }
  
  loading.value = true
  
  try {
    // 获取基本提交信息
    const res = await submissionApi.getSubmission(id)
    
    if (res.code === 0 && res.data) {
      submission.value = res.data
      
      // 根据提交类型获取详细信息
      if (res.data.type === 'CHOICE' || res.data.type === 'JUDGE') {
        try {
          const detailRes = await submissionApi.getChoiceJudgeSubmissionDetail(id)
          if (detailRes.code === 0 && detailRes.data) {
            choiceJudgeSubmission.value = detailRes.data
          }
        } catch (error) {
          console.error('获取选择/判断题提交详情异常:', error)
          ElMessage.warning('无法加载完整的提交详情')
        }
      } else if (res.data.type === 'PROGRAM') {
        try {
          const detailRes = await submissionApi.getProgramSubmissionDetail(id)
          if (detailRes.code === 0 && detailRes.data) {
            programSubmission.value = detailRes.data
          }
        } catch (error) {
          console.error('获取编程题提交详情异常:', error)
          ElMessage.warning('无法加载完整的提交详情')
        }
      }
    } else {
      ElMessage.error(res.message || '获取提交详情失败')
    }
  } catch (error) {
    console.error('获取提交详情异常:', error)
    ElMessage.error('获取提交详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化语言
const formatLanguage = (language: string | undefined): string => {
  if (!language) return '-'
  
  const languageMap: Record<string, string> = {
    'java': 'Java',
    'python': 'Python',
    'python3': 'Python 3',
    'c': 'C',
    'cpp': 'C++',
    'javascript': 'JavaScript',
    'typescript': 'TypeScript',
    'go': 'Go',
    'rust': 'Rust',
    'c#': 'C#',
    'php': 'PHP'
  }
  
  return languageMap[language.toLowerCase()] || language
}

// 格式化类型
const formatType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return typeMap[type] || type
}

// 获取类型标签样式
const getTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'CHOICE': 'success',
    'JUDGE': 'warning',
    'PROGRAM': 'info'
  }
  return typeMap[type] || ''
}

// 格式化状态
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': '评测中',
    'ACCEPTED': '正确',
    'WRONG_ANSWER': '错误',
    'COMPILE_ERROR': '编译错误',
    'RUNTIME_ERROR': '运行错误',
    'TIME_LIMIT_EXCEEDED': '超时',
    'MEMORY_LIMIT_EXCEEDED': '内存超限'
  }
  return statusMap[status] || status
}

// 获取状态标签样式
const getStatusTagType = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': 'info',
    'ACCEPTED': 'success',
    'WRONG_ANSWER': 'danger',
    'COMPILE_ERROR': 'warning',
    'RUNTIME_ERROR': 'danger',
    'TIME_LIMIT_EXCEEDED': 'warning',
    'MEMORY_LIMIT_EXCEEDED': 'warning'
  }
  return statusMap[status] || 'info'
}

// 格式化提交答案
const formatSubmissionAnswer = (answer: string | undefined, type: string): string => {
  if (!answer) return '-'
  
  if (type === 'JUDGE') {
    if (answer === 'true') return '正确'
    if (answer === 'false') return '错误'
  }
  return answer
}

// 格式化难度
const formatDifficulty = (difficulty: string): string => {
  const difficultyMap: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

// 获取难度标签样式
const getDifficultyTagType = (difficulty: string): string => {
  const difficultyMap: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return difficultyMap[difficulty] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined): string => {
  if (!dateTime) return ''
  
  try {
    const date = new Date(dateTime)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (error) {
    console.error('日期格式化异常:', error)
    return dateTime
  }
}

// 格式化内容（防XSS）
const formatContent = (content: string | undefined): string => {
  if (!content) return ''
  return DOMPurify.sanitize(content)
}

// 格式化测试用例结果
const formatTestcaseResults = (results: string | undefined): string => {
  if (!results) return ''
  
  try {
    // 尝试解析JSON格式的测试用例结果
    const parsedResults = JSON.parse(results)
    return JSON.stringify(parsedResults, null, 2)
  } catch (error) {
    // 如果解析失败，直接返回原始字符串
    return results
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadSubmissionDetail()
})
</script>

<style scoped>
.submission-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
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
  font-size: 20px;
  color: #409EFF;
}

.nav-links {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.title-actions h2 {
  margin: 0;
}

.submission-meta {
  display: flex;
  gap: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

@media (min-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.info-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
}

.code-section,
.error-section,
.testcase-section,
.analysis-section {
  margin-top: 20px;
}

.code-block,
.error-block,
.testcase-block {
  background-color: #f8f8f8;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  margin: 10px 0;
  overflow-x: auto;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-block {
  color: #f56c6c;
  background-color: #fef0f0;
}

.empty-data {
  text-align: center;
  padding: 30px;
}

.back-link {
  margin-top: 15px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag-item {
  margin: 2px;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.analysis-content {
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  line-height: 1.6;
}

.standard-solution-section {
  margin-top: 20px;
}

.solution-item {
  margin-bottom: 10px;
}

.language-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}
</style> 