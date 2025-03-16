<template>
  <div class="problem-layout">
    <!-- 左侧：题目详情区域 -->
    <div class="left-panel">
      <el-card class="problem-card">
        <template #header>
          <div class="problem-header">
            <div class="problem-title">
              <h2>{{ problem?.title || '加载中...' }}</h2>
              <div class="problem-meta">
                <el-tag type="success" size="small">{{ problem?.isMultiple ? '多选题' : '单选题' }}</el-tag>
                <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="ml-2">{{ problem?.difficulty || '未知' }}</el-tag>
                <span class="problem-stats">通过率: {{ problem?.acceptCount && problem?.submitCount ? Math.round((problem.acceptCount / problem.submitCount) * 100) + '%' : '0%' }}</span>
              </div>
            </div>
            <!-- 添加返回按钮 -->
            <div class="problem-actions">
              <el-button @click="returnToList" type="primary" plain size="small">
                <el-icon><Back /></el-icon> 返回题目列表
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 题目内容 -->
        <div class="problem-content">
          <div v-if="problem?.content" class="markdown-body" v-html="renderMarkdown(problem.content)"></div>
          <div v-else class="loading-content">
            <el-skeleton :rows="5" animated />
          </div>
        </div>
        
        <!-- 选项列表和提交区域 -->
        <div class="choice-options">
          <h3>
            请{{ problem?.isMultiple ? '选择所有正确答案' : '选择正确答案' }}：
            <el-tag v-if="problem?.isMultiple" type="warning" size="small" class="multiple-tag">多选题</el-tag>
            <el-tag v-else type="success" size="small" class="single-tag">单选题</el-tag>
          </h3>
          
          <!-- 选项列表 - 有选项时显示 -->
          <div v-if="problem?.options && Array.isArray(problem.options) && problem.options.length > 0">
            <!-- 多选题使用复选框 -->
            <el-checkbox-group v-if="problem?.isMultiple" v-model="selectedAnswers" class="choice-options-group">
              <div v-for="(option, index) in problem.options" :key="index" class="choice-option">
                <el-checkbox :value="getOptionKey(option, index)" class="option-checkbox">
                  <div class="option-container">
                    <div class="option-label">{{ getOptionKey(option, index) }}</div>
                    <div class="option-content">{{ option.content || '选项内容缺失' }}</div>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            
            <!-- 单选题使用单选框 -->
            <el-radio-group v-else v-model="singleSelectedAnswer" class="choice-options-group">
              <div v-for="(option, index) in problem.options" :key="index" class="choice-option">
                <el-radio :label="getOptionKey(option, index)" class="option-radio">
                  <div class="option-container">
                    <div class="option-label">{{ getOptionKey(option, index) }}</div>
                    <div class="option-content">{{ option.content || '选项内容缺失' }}</div>
                  </div>
                </el-radio>
              </div>
            </el-radio-group>
            
            <div class="submit-btn">
              <el-button type="primary" @click="submitAnswer" :disabled="!problem?.id">提交答案</el-button>
            </div>
          </div>
          
          <!-- 选项加载状态 - 无选项时 -->
          <div v-else-if="problem && problem.type === 'CHOICE'">
            <el-empty 
              description="当前题目尚未设置选项，请联系管理员添加选项。" 
              :image-size="200"
            >
              <template #default>
                <p class="option-warning">无法获取选项数据，请稍后再试</p>
                <el-button @click="loadProblem(Array.isArray(route.params.id) ? route.params.id[0] : route.params.id)">重新加载</el-button>
              </template>
            </el-empty>
          </div>
          
          <!-- 加载中状态 -->
          <div v-else>
            <el-skeleton :rows="4" animated />
          </div>
        </div>
        
        <!-- 答案与解析 (提交后显示) -->
        <div class="answer-analysis" v-if="showAnalysis">
          <div class="divider"></div>
          <h3>正确答案与解析</h3>
          <div class="correct-answer">
            <strong>正确答案：</strong> {{ correctAnswerDisplay }}
          </div>
          <div class="analysis-content markdown-body" v-html="renderMarkdown(analysisContent)"></div>
        </div>
      </el-card>
    </div>
    
    <!-- 右侧面板 -->
    <div class="right-panel">
      <!-- 右侧上方：推荐题目 -->
      <el-card class="similar-problems-card">
        <template #header>
          <div class="card-header">
            <h3>推荐题目</h3>
          </div>
        </template>
        
        <div v-if="loadingSimilarProblems" class="loading-content">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="similarProblems.length === 0" class="empty-content">
          <el-empty description="暂无推荐题目" :image-size="100"></el-empty>
        </div>
        
        <div v-else class="similar-problems-list">
          <div v-for="item in similarProblems" :key="item.id" class="similar-problem-item">
            <router-link :to="`/problem/${item.type.toLowerCase()}/${item.id}`" class="problem-link">
              <div class="problem-info">
                <div class="problem-title-row">
                  <span class="problem-title-text">{{ item.title }}</span>
                  <el-tag :type="getTypeTagType(item.type)" size="small">{{ getTypeText(item.type) }}</el-tag>
                </div>
                <div class="problem-tags">
                  <el-tag v-for="tag in item.tags" :key="tag" size="small" class="problem-tag" type="info">{{ tag }}</el-tag>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </el-card>
      
      <!-- 右侧下方：提交记录 -->
      <el-card class="submissions-card">
        <template #header>
          <div class="card-header">
            <h3>提交记录</h3>
          </div>
        </template>
        
        <div v-if="loadingSubmissions" class="loading-content">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="loadSubmissionError" class="empty-content error-content">
          <el-alert
            :title="loadSubmissionError"
            type="error"
            show-icon
            :closable="false"
          />
          <div class="retry-button">
            <el-button @click="loadSubmissions" size="small">重试</el-button>
          </div>
        </div>
        
        <div v-else-if="!isUserLoggedIn" class="empty-content login-required">
          <el-empty description="登录后查看提交记录" :image-size="100">
            <template #default>
              <el-button @click="goToLogin" type="primary" size="small">去登录</el-button>
            </template>
          </el-empty>
        </div>
        
        <div v-else-if="submissions.length === 0" class="empty-content">
          <el-empty description="暂无提交记录" :image-size="100"></el-empty>
        </div>
        
        <div v-else class="submissions-list">
          <div v-for="item in submissions" :key="item.id" class="submission-item">
            <div class="submission-info">
              <div class="submission-status">
                <el-tag :type="getStatusTagType(getSubmissionStatus(item))">{{ getStatusText(getSubmissionStatus(item)) }}</el-tag>
              </div>
              <div class="submission-time">{{ formatTime(getSubmissionTime(item)) }}</div>
            </div>
          </div>
          
          <!-- 分页 -->
          <div class="pagination-container" v-if="totalSubmissions > pageSize">
            <el-pagination
              v-model:current-page="currentSubmissionPage"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="totalSubmissions"
              @current-change="handleSubmissionPageChange"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.problem-layout {
  display: flex;
  gap: 20px;
  margin: 20px;
  min-height: calc(100vh - 140px); /* 确保整体高度填满视口 */
  align-items: stretch; /* 使容器内所有元素等高 */
}

.left-panel {
  flex: 2; /* 占据2/3宽度 */
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.right-panel {
  flex: 1; /* 占据1/3宽度 */
  min-width: 250px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%; /* 保证高度与左侧一致 */
}

.problem-card {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  height: 100%; /* 占满左侧面板高度 */
}

.problem-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止内容溢出 */
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-title {
  flex: 1;
}

.problem-title h2 {
  margin: 0;
  font-size: 1.5rem;
}

.problem-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

.problem-stats {
  margin-left: 10px;
  color: #606266;
  font-size: 0.9rem;
}

.problem-actions {
  margin-left: 20px;
}

/* 题目内容区域自适应高度，可滚动 */
.problem-content {
  margin: 20px 0;
  overflow-y: auto;
  flex: 0 1 auto; /* 允许收缩，但不会伸展 */
}

/* 选项区域固定高度，不会压缩 */
.choice-options {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  flex: 0 0 auto; /* 不会伸缩 */
}

.choice-options-group {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.choice-option {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
  display: block; /* 确保选项垂直排列 */
  width: 100%;
}

.choice-option:hover {
  background-color: #f5f7fa;
}

.option-container {
  display: flex;
  width: 100%;
  align-items: flex-start;
}

/* 覆盖Element Plus默认样式 */
.el-radio, .el-checkbox {
  width: 100% !important;
  margin-right: 0 !important;
  display: flex !important;
}

.el-radio :deep(.el-radio__input), 
.el-checkbox :deep(.el-checkbox__input) {
  align-self: flex-start;
  margin-top: 3px;
}

.el-radio :deep(.el-radio__label), 
.el-checkbox :deep(.el-checkbox__label) {
  flex: 1;
  padding-left: 8px;
  width: calc(100% - 20px);
}

.submit-btn {
  margin-top: 20px;
  margin-bottom: 20px;
  text-align: center;
}

.answer-analysis {
  margin-top: 30px;
}

.divider {
  height: 1px;
  background-color: #dcdfe6;
  margin: 20px 0;
}

.correct-answer {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}

.analysis-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.option-warning {
  color: #e6a23c;
  margin-bottom: 10px;
}

.multiple-tag, .single-tag {
  margin-left: 10px;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
  display: inline-block;
  width: 30px;
  flex-shrink: 0;
}

.option-content {
  display: block;
  flex: 1;
  text-align: left;
}

.similar-problems-card,
.submissions-card {
  margin-bottom: 20px;
}

.similar-problems-card {
  flex: 0 0 40%; /* 固定为右侧面板高度的40% */
  overflow-y: auto;
  margin-bottom: 20px;
}

.submissions-card {
  flex: 0 0 calc(60% - 20px); /* 固定为右侧面板高度的60%减去间距 */
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  margin-bottom: 0; /* 去掉底部边距 */
  height: auto; /* 自适应高度 */
  max-height: 400px; /* 设置最大高度 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.similar-problems-list,
.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  max-height: 300px; /* 添加最大高度限制 */
  flex: 1; /* 让列表自适应填充剩余空间 */
}

.similar-problem-item,
.submission-item {
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  margin-bottom: 5px; /* 添加底部间距 */
}

.similar-problem-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.problem-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.problem-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.problem-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-title-text {
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.problem-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.problem-tag {
  margin-right: 5px;
}

.submission-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.submission-time {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.empty-content,
.loading-content {
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.login-required {
  padding: 30px 0;
}

.error-content {
  padding: 20px;
}

.retry-button {
  margin-top: 15px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .problem-layout {
    flex-direction: column;
    min-height: auto;
  }
  
  .left-panel {
    margin-bottom: 20px;
  }
  
  .right-panel {
    flex-direction: row;
  }
  
  .similar-problems-card,
  .submissions-card {
    flex: 1;
    margin-bottom: 0;
    max-height: 500px;
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .problem-layout {
    margin: 10px;
  }
  
  .right-panel {
    flex-direction: column;
  }
  
  .similar-problems-card {
    margin-bottom: 20px;
    max-height: none;
  }
  
  .submissions-card {
    max-height: none;
  }
}

.submission-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>

<script lang="ts" setup>
import { ref, computed, onMounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { problemApi } from '@/api/problem'
import { useUserStore } from '@/stores/user'
import { submissionApi } from '@/api/submission'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

// 调试模式 - 临时设置为true，用于调试
const debug = ref(false)

// 定义组件props
const props = defineProps({
  problemId: {
    type: [Number, String],
    required: false
  }
})

// 组件状态
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const problem = ref<any>(null)
const selectedAnswers = ref<string[]>([])
const singleSelectedAnswer = ref<string>('') // 单选题选中的答案
const submitting = ref(false)
const showResult = ref(false)
const isCorrect = ref(false)
const correctAnswer = ref('')
const analysis = ref('')
const showAnalysis = ref(false)
const correctAnswerDisplay = ref('')
const analysisContent = ref('')
const originalOptions = ref<any>(null)

// 推荐题目相关
const similarProblems = ref<any[]>([])
const loadingSimilarProblems = ref(false)

// 提交记录相关
const submissions = ref<any[]>([])
const loadingSubmissions = ref(false)
const currentSubmissionPage = ref(1)
const pageSize = ref(10)
const totalSubmissions = ref(0)
const loadSubmissionError = ref<string | null>(null) // 添加错误状态存储

// 加载题目详情
const loadProblem = async (id: string | number) => {
  try {
    const res = await problemApi.getProblemDetail(Number(id))
    if (res.code === 0 && res.data) {
      problem.value = res.data
      originalOptions.value = res.data.options
      
      // 如果是多选题，初始化为空数组；如果是单选题，初始化为空字符串
      selectedAnswers.value = problem.value.isMultiple ? [] : []
      singleSelectedAnswer.value = ''
      
      // 调试信息
      console.log('成功加载题目详情:', JSON.stringify(problem.value))
      console.log('problem.options是否存在:', !!problem.value?.options)
      console.log('选项数量:', problem.value?.options?.length || 0)
      console.log('选项数据:', JSON.stringify(problem.value?.options))
      console.log('答案格式:', typeof problem.value?.answer, problem.value?.answer)
      console.log('是否多选题:', problem.value?.isMultiple)
    } else {
      console.error('获取题目详情API返回错误:', res.message)
      ElMessage.error(res.message || '获取题目详情失败')
      router.push('/problems')
    }
  } catch (error) {
    console.error('加载题目详情异常:', error)
    ElMessage.error('获取题目详情失败，请稍后重试')
    router.push('/problems')
  }
}

// 使用watchEffect监听props或route参数的变化
watchEffect(() => {
  let problemId: number | string | undefined = props.problemId

  // 处理路由参数，可能是字符串、数字或数组
  if (!problemId && route.params.id) {
    problemId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
    console.log('从路由参数获取的问题ID:', problemId)
  }

  if (problemId) {
    console.log('watchEffect触发加载问题:', problemId)
    loadProblem(problemId)
  } else {
    console.warn('watchEffect未检测到有效的问题ID')
  }
})

// 页面加载时不再主动调用loadProblem，由watchEffect触发
onMounted(() => {
  // watchEffect会自动触发加载
  console.log('ChoiceProblemDetail组件已挂载，路由参数:', route.params)
})

// 返回题目列表
const returnToList = () => {
  // 确保缓存被清除，真正强制刷新
  try {
    // 尝试强制更新本地缓存，确保在返回列表时可以显示最新状态
    if (problem.value?.id) {
      updateLocalStatusCache(problem.value.id);
    }
    
    // 清除通用缓存
    localStorage.removeItem('problem_status_cache');
    
    console.log('返回题目列表，强制刷新状态');
  } catch (e) {
    console.error('清除缓存失败:', e);
  }
  
  // 添加强制刷新参数，确保返回列表时重新加载数据
  router.push({
    path: '/problems',
    query: { 
      t: Date.now().toString(), // 添加时间戳强制刷新
      forceRefresh: 'true' // 添加强制刷新标记
    }
  });
}

// 添加新方法：更新本地状态缓存
const updateLocalStatusCache = (problemId: number, status?: string) => {
  try {
    // 如果未提供状态，根据isCorrect决定状态
    const newStatus = status || (isCorrect.value ? 'SOLVED' : 'ATTEMPTED');
    console.log(`更新题目 ${problemId} 的本地缓存状态为 ${newStatus}`);
    
    // 读取现有缓存
    const statusCacheStr = localStorage.getItem('userProblemStatuses');
    let statusCache: Record<number, string> = {};
    
    if (statusCacheStr) {
      try {
        statusCache = JSON.parse(statusCacheStr);
      } catch (e) {
        console.warn('无法解析现有缓存，将创建新缓存');
      }
    }
    
    // 更新缓存
    statusCache[problemId] = newStatus;
    
    // 保存回本地存储
    localStorage.setItem('userProblemStatuses', JSON.stringify(statusCache));
    localStorage.setItem('statusCacheTime', Date.now().toString());
    console.log('状态缓存已更新:', statusCache);
    
    return true;
  } catch (e) {
    console.error('更新本地状态缓存失败:', e);
    return false;
  }
}

// 创建markdown-it实例
const md = new MarkdownIt()

// 渲染Markdown内容
const renderMarkdown = (content: string) => {
  if (!content) return ''
  const html = md.render(content)
  return DOMPurify.sanitize(html)
}

// 根据难度获取标签类型
const getDifficultyType = (difficulty: string) => {
  switch (difficulty) {
    case '简单': return 'success'
    case '中等': return 'warning'
    case '困难': return 'danger'
    default: return 'info'
  }
}

// 获取选项的键
const getOptionKey = (option: any, index: number) => {
  return option.key || String.fromCharCode(65 + index) // A, B, C, D...
}

// 检查用户是否登录
const isUserLoggedIn = computed(() => {
  return !!userStore.token
})

// 提交答案
const submitAnswer = async () => {
  if (!isUserLoggedIn.value) {
    ElMessageBox.confirm('需要登录后才能提交答案，是否前往登录页面？', '未登录提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {
      // 用户取消操作
    })
    return
  }
  
  if (!problem.value) {
    ElMessage.warning('题目加载中，请稍后提交')
    return
  }
  
  let answer = '';
  
  if (problem.value.isMultiple) {
    if (!selectedAnswers.value || selectedAnswers.value.length === 0) {
      ElMessage.warning('请至少选择一个选项')
      return
    }
    answer = selectedAnswers.value.sort().join(',') // 多选题答案按字母排序拼接
  } else {
    if (!singleSelectedAnswer.value) {
      ElMessage.warning('请选择一个选项')
      return
    }
    answer = singleSelectedAnswer.value // 单选题直接获取选中的值
  }
  
  submitting.value = true
  
  try {
    console.log('提交的答案:', answer)
    console.log('标准答案:', problem.value.answer)
    console.log('是否多选题:', problem.value.isMultiple)
    
    // 前端预检查，如果答案与标准答案匹配，记录日志
    if (answer === problem.value.answer) {
      console.log('前端预检查: 答案与标准答案匹配，应该是正确的');
    } else {
      console.log('前端预检查: 答案与标准答案不匹配');
    }
    
    const submitData = {
      problemId: problem.value.id,
      type: 'CHOICE' as const,
      answer: answer
    }
    
    console.log('提交数据:', submitData)
    const res = await submissionApi.submitChoice(submitData)
    submitting.value = false
    
    if (res.code === 0) {
      // 显示提交结果
      showResult.value = true
      
      // 添加更多日志输出，帮助调试
      console.log('提交响应数据:', res.data);
      console.log('提交状态:', res.data?.status);
      console.log('标准答案:', problem.value.answer);
      console.log('用户答案:', answer);
      
      // 修改判断逻辑，增加对比用户答案和标准答案
      const serverStatus = res.data?.status;
      const userAnswerMatches = answer === problem.value.answer;
      
      console.log('服务器返回状态是否为CORRECT/ACCEPTED:', 
                 serverStatus === 'ACCEPTED' || serverStatus?.includes('CORRECT'));
      console.log('用户答案是否与标准答案匹配:', userAnswerMatches);
      
      // 综合判断：服务器状态或答案匹配都认为是正确的
      isCorrect.value = (serverStatus === 'ACCEPTED' || 
                         serverStatus?.includes('CORRECT') || 
                         userAnswerMatches);
      
      console.log('最终判断结果 isCorrect:', isCorrect.value);
      
      // 设置解析相关变量
      showAnalysis.value = true
      correctAnswerDisplay.value = problem.value.answer || ''
      analysisContent.value = problem.value.analysis || ''
      
      // 立即更新本地状态缓存
      const newStatus = isCorrect.value ? 'SOLVED' : 'ATTEMPTED';
      if (problem.value.id) {
        updateLocalStatusCache(problem.value.id, newStatus);
      }
      
      // 直接从服务器获取最新状态，确保状态立即更新
      try {
        const token = localStorage.getItem('token');
        if (token && problem.value.id) {
          console.log('直接从服务器获取最新状态');
          
          // 调用API获取最新状态
          const baseURL = import.meta.env.VITE_API_BASE_URL || '';
          const statusURL = `${baseURL}/user/problem-status/get?problemId=${problem.value.id}`;
          
          fetch(statusURL, {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          .then(response => response.json())
          .then(data => {
            console.log('最新状态获取成功:', data);
            if (data.code === 0 && data.data) {
              // 再次更新本地缓存，确保与服务器同步
              updateLocalStatusCache(problem.value.id, data.data.status);
            }
          })
          .catch(error => {
            console.error('获取最新状态失败:', error);
          });
        }
      } catch (statusError) {
        console.error('尝试获取最新状态时出错:', statusError);
      }
      
      // 提示用户
      if (isCorrect.value) {
        ElMessageBox.confirm('恭喜，回答正确！是否返回题目列表？', '提交成功', {
          confirmButtonText: '返回题目列表',
          cancelButtonText: '继续查看',
          type: 'success'
        }).then(() => {
          returnToList()
        }).catch(() => {
          // 用户选择继续查看，不做操作
        })
      } else {
        // 添加额外检查，防止误报
        if (answer === problem.value.answer) {
          console.log('检测到答案匹配但服务器返回错误状态，覆盖为正确');
          isCorrect.value = true;
          
          // 确保状态更新为已解决
          if (problem.value.id) {
            updateLocalStatusCache(problem.value.id, 'SOLVED');
            
            // 再次尝试直接更新服务器状态
            try {
              const token = localStorage.getItem('token');
              if (token) {
                console.log('尝试强制更新服务器状态为SOLVED');
                // 这里可以添加一个API调用来强制更新状态
                const baseURL = import.meta.env.VITE_API_BASE_URL || '';
                const forceUpdateURL = `${baseURL}/user/problem-status/update`;
                
                fetch(forceUpdateURL, {
                  method: 'POST',
                  headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                  },
                  body: JSON.stringify({
                    problemId: problem.value.id,
                    status: 'SOLVED'
                  })
                })
                .then(response => response.json())
                .then(data => {
                  console.log('强制更新状态结果:', data);
                  if (data.code === 0) {
                    console.log('服务器状态已成功更新为SOLVED');
                  } else {
                    console.warn('服务器状态更新失败，但本地缓存已更新');
                  }
                })
                .catch(error => {
                  console.error('强制更新状态请求失败:', error);
                });
              }
            } catch (e) {
              console.error('强制更新状态失败:', e);
            }
          }
          
          ElMessageBox.confirm('恭喜，回答正确！是否返回题目列表？', '提交成功', {
            confirmButtonText: '返回题目列表',
            cancelButtonText: '继续查看',
            type: 'success'
          }).then(() => {
            returnToList()
          }).catch(() => {
            // 用户选择继续查看，不做操作
          });
        } else {
          ElMessage.error('很遗憾，回答错误！')
        }
      }
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交答案出错:', error)
    
    // 如果服务器错误但前端预检查认为答案正确，显示特殊提示
    if (answer === problem.value.answer) {
      console.log('服务器错误但前端预检查认为答案正确，显示特殊提示');
      ElMessageBox.confirm(
        '您的答案与标准答案匹配，但服务器返回错误。这可能是服务器问题，您的答案很可能是正确的。是否返回题目列表？', 
        '提交结果', 
        {
          confirmButtonText: '返回题目列表',
          cancelButtonText: '继续查看',
          type: 'warning'
        }
      ).then(() => {
        returnToList()
      }).catch(() => {
        // 用户选择继续查看，不做操作
      });
      
      // 在本地标记为已尝试
      if (problem.value.id) {
        updateLocalStatusCache(problem.value.id, 'ATTEMPTED');
      }
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
    
    submitting.value = false
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath }
  });
}

// // 加载提交记录
// const loadSubmissions = async () => {
//   if (!problem.value?.id) return
  
//   loadingSubmissions.value = true
//   loadSubmissionError.value = null // 重置错误状态
  
//   try {
//     const userStore = useUserStore()
//     console.log('开始加载题目提交记录:', problem.value.id)
    
//     // 如果用户未登录，不进行API调用
//     if (!isUserLoggedIn.value) {
//       console.log('用户未登录，跳过提交记录API调用')
//       submissions.value = []
//       totalSubmissions.value = 0
//       loadingSubmissions.value = false
//       return
//     }
    
//     const res = await submissionApi.getProblemSubmissionList(
//       problem.value.id, // 题目ID作为主要参数
//       {
//         current: currentSubmissionPage.value,
//         pageSize: pageSize.value
//         // 不需要传userId，在getProblemSubmissionList内部会自动获取当前用户ID
//       }
//     )
    
//     console.log('获取提交记录返回数据:', res)
    
//     if (res.code === 0 && res.data) {
//       console.log('解析提交记录数据:', res.data)
//       submissions.value = res.data.records || []
//       totalSubmissions.value = res.data.total || 0
      
//       // 调试每条提交记录
//       if (res.data.records && res.data.records.length > 0) {
//         console.log('第一条提交记录详情:', JSON.stringify(res.data.records[0]));
//         console.log('提交记录字段列表:', Object.keys(res.data.records[0]).join(', '));
//         console.log('提交记录状态字段:', getSubmissionStatus(res.data.records[0]));
//         console.log('提交记录状态字段类型:', typeof getSubmissionStatus(res.data.records[0]));
//         console.log('提交记录时间字段:', getSubmissionTime(res.data.records[0]));
//         // 检查状态是否能正确映射
//         const mappedStatus = getStatusText(getSubmissionStatus(res.data.records[0]));
//         console.log(`状态"${getSubmissionStatus(res.data.records[0])}"映射结果: "${mappedStatus}"`);
//       } else {
//         console.log('未获取到提交记录')
//       }
//     } else {
//       console.warn('获取提交记录API返回错误:', res.message)
//       loadSubmissionError.value = res.message || '获取提交记录失败'
//     }
//   } catch (error: any) {
//     console.error('加载提交记录失败:', error)
//     loadSubmissionError.value = error?.message || '加载提交记录失败'
//   } finally {
//     loadingSubmissions.value = false
//   }
// }
const loadSubmissions = async () => {
  if (!problem.value?.id) return
  
  loadingSubmissions.value = true
  loadSubmissionError.value = null // 重置错误状态
  
  try {
    console.log('开始加载题目提交记录:', problem.value.id)
    
    // 如果用户未登录，不进行API调用
    if (!isUserLoggedIn.value) {
      console.log('用户未登录，跳过提交记录API调用')
      submissions.value = []
      totalSubmissions.value = 0
      loadingSubmissions.value = false
      return
    }
    
    const res = await submissionApi.getProblemSubmissionList(
      problem.value.id,
      {
        current: currentSubmissionPage.value,
        pageSize: pageSize.value
      }
    )
    
    console.log('获取提交记录返回数据:', res)
    
    if (res.code === 0 && res.data) {
      console.log('解析提交记录数据:', res.data)
      submissions.value = res.data.records || []
      totalSubmissions.value = res.data.total || 0
      
      // 调试日志...
    } else {
      console.warn('获取提交记录API返回错误:', res.message)
      loadSubmissionError.value = res.message || '获取提交记录失败'
    }
  } catch (error: any) {
    console.error('加载提交记录失败:', error)
    loadSubmissionError.value = error?.message || '加载提交记录失败'
  } finally {
    loadingSubmissions.value = false
  }
}
// 获取题目类型标签样式
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'CHOICE': 'success',
    'JUDGE': 'warning',
    'PROGRAM': 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取题目类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return typeMap[type] || '未知类型'
}

// 获取提交状态标签样式
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACCEPTED': 'success',
    'WRONG_ANSWER': 'danger',
    'PENDING': 'info',
    'COMPILE_ERROR': 'warning',
    'RUNTIME_ERROR': 'error',
    'TIME_LIMIT_EXCEEDED': 'warning',
    'MEMORY_LIMIT_EXCEEDED': 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取提交状态文本
const getStatusText = (status: string) => {
  if (!status) return '未知状态'
  
  // 先转为大写以确保匹配
  const upperStatus = typeof status === 'string' ? status.toUpperCase() : status;
  
  const statusMap: Record<string, string> = {
    'ACCEPTED': '通过',
    'CORRECT': '正确',
    'WRONG_ANSWER': '错误',
    'WRONG': '错误',
    'PENDING': '等待中',
    'COMPILE_ERROR': '编译错误',
    'RUNTIME_ERROR': '运行错误',
    'TIME_LIMIT_EXCEEDED': '超时',
    'MEMORY_LIMIT_EXCEEDED': '内存超限'
  }
  
  return statusMap[upperStatus] || '未知状态'
}

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取提交的时间字段（适配不同的API返回格式）
const getSubmissionTime = (item: any) => {
  // 优先使用submissionTime，后备使用createTime
  return item.submissionTime || item.createTime
}

// 获取提交的状态字段（适配不同的API返回格式）
const getSubmissionStatus = (item: any) => {
  // 处理可能的null/undefined
  if (!item) return '';
  
  // 尝试从不同位置获取状态
  const status = item.status || 
                (item.result && item.result.status) || 
                (item.submission && item.submission.status);
  
  return status || '';
}

// 处理提交记录分页变化
const handleSubmissionPageChange = (page: number) => {
  currentSubmissionPage.value = page
  loadSubmissions()
}

// 加载相似题目
const loadSimilarProblems = async () => {
  if (!problem.value?.id) return
  
  loadingSimilarProblems.value = true
  try {
    // 获取相同类型和标签的题目
    const type = problem.value.type
    const tags = problem.value.tags || []
    
    const res = await problemApi.getProblemList({
      current: 1,
      pageSize: 10, // 获取更多，然后过滤
      type,
      tags, // 使用tags数组
    })
    
    if (res.code === 0 && res.data) {
      // 过滤掉当前题目
      similarProblems.value = res.data.records
        .filter(item => item.id !== problem.value?.id)
        .slice(0, 5) // 只取5个
    }
  } catch (error) {
    console.error('加载推荐题目失败:', error)
  } finally {
    loadingSimilarProblems.value = false
  }
}

// 监听题目ID变化，重新加载相关数据
watchEffect(() => {
  const id = route.params.id
  if (id) {
    loadSimilarProblems()
    loadSubmissions()
  }
})
</script>