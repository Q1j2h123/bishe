<template>
  <div class="judge-problem-container">
    <!-- 题目详情区域 -->
    <el-card class="problem-card">
      <template #header>
        <div class="problem-header">
          <div class="problem-title">
            <h2>{{ problem?.title || '加载中...' }}</h2>
            <div class="problem-meta">
              <el-tag type="warning" size="small">判断题</el-tag>
              <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="ml-2">{{ problem?.difficulty || '未知' }}</el-tag>
              <span class="problem-stats">通过率: {{ problem?.acceptCount && problem?.submitCount ? Math.round((problem.acceptCount / problem.submitCount) * 100) + '%' : '0%' }}</span>
            </div>
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
      
      <!-- 判断题选项区域 -->
      <div class="judge-options">
        <h3>请判断：</h3>
        
        <!-- 判断选项 -->
        <div v-if="problem">
          <el-radio-group v-model="selectedAnswer" class="judge-radio-group">
            <el-radio :label="true" class="judge-option">
              <div class="option-content">
                <el-tag type="success" size="large">正确</el-tag>
              </div>
            </el-radio>
            <el-radio :label="false" class="judge-option">
              <div class="option-content">
                <el-tag type="danger" size="large">错误</el-tag>
              </div>
            </el-radio>
          </el-radio-group>
          
          <div class="submit-btn">
            <el-button type="primary" @click="submitAnswer" :disabled="selectedAnswer === null">提交答案</el-button>
          </div>
        </div>
        
        <!-- 加载中状态 -->
        <div v-else>
          <el-skeleton :rows="2" animated />
        </div>
      </div>
      
      <!-- 答案与解析 (提交后显示) -->
      <div class="answer-analysis" v-if="showAnalysis">
        <div class="divider"></div>
        <h3>正确答案与解析</h3>
        <div class="correct-answer">
          <strong>正确答案：</strong> 
          <el-tag v-if="problem?.answer" type="success">正确</el-tag>
          <el-tag v-else type="danger">错误</el-tag>
        </div>
        <div v-if="problem?.analysis" class="analysis-content markdown-body" v-html="renderMarkdown(problem.analysis)"></div>
        <div v-else class="analysis-content">
          <el-empty description="暂无解析" />
        </div>
      </div>
    </el-card>

    <!-- DEBUG面板 - 开发阶段使用 -->
    <el-card v-if="debug" class="debug-panel">
      <template #header>
        <div class="card-header">
          <h3>调试信息</h3>
          <el-button type="danger" size="small" @click="debug = false">关闭</el-button>
        </div>
      </template>
      <div>
        <h4>基本信息：</h4>
        <pre>题目ID: {{ route.params.id }}</pre>
        <pre>题目类型: {{ problem?.type }}</pre>
        <pre>已选答案: {{ selectedAnswer }}</pre>
        <pre>标准答案: {{ problem?.answer }}</pre>
        
        <h4>完整题目数据：</h4>
        <pre>{{ JSON.stringify(problem, null, 2) }}</pre>
        
        <h4>原始API返回数据：</h4>
        <pre>{{ JSON.stringify(originalResponse, null, 2) }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, watchEffect } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { problemApi, type ProblemVO } from '@/api/problem'
import { submissionApi, type SubmissionVO, type JudgeSubmitRequest } from '@/api/submission'
// @ts-ignore
import MarkdownIt from 'markdown-it'

// 调试模式 - 临时设置为true，用于调试
const debug = ref(true)

// 定义属性，允许从父组件传入问题ID
const props = defineProps<{
  problemId?: number
}>()

// 保存原始API响应用于调试
const originalResponse = ref<any>(null)

// 自定义的JudgeProblemVO类型，不再扩展ProblemVO
interface JudgeProblemVO {
  id?: number;
  title?: string;
  content?: string;
  difficulty?: string;
  type?: string;
  answer?: boolean; // 判断题的答案是布尔值
  analysis?: string;
  tags?: string[];
  submitCount?: number;
  acceptCount?: number;
  createTime?: string;
  updateTime?: string;
  userId?: number;
  userName?: string;
}

// 使用我们自定义的JudgeProblemVO类型
const problem = ref<JudgeProblemVO | null>(null)
// 选中的答案
const selectedAnswer = ref<boolean | null>(null)
// 是否显示解析
const showAnalysis = ref<boolean>(false)

// 路由工具
const router = useRouter()
const route = useRoute()

// Markdown 渲染器
const md = new MarkdownIt()
const renderMarkdown = (text?: string): string => {
  if (!text) return ''
  return md.render(text)
}

// 获取难度颜色类型
const getDifficultyType = (difficulty?: string): string => {
  if (!difficulty) return 'info'
  
  const map: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger',
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  
  return map[difficulty] || 'info'
}

// 提交答案
const submitAnswer = async (): Promise<void> => {
  if (selectedAnswer.value === null) {
    ElMessage.warning('请选择答案')
    return
  }
  
  if (!problem.value?.id) {
    ElMessage.error('题目信息不完整')
    return
  }
  
  try {
    console.log('准备提交判断题答案:', selectedAnswer.value)
    
    // 提交请求对象
    const submitData: JudgeSubmitRequest = {
      problemId: problem.value.id,
      // 确保answer字段是一个boolean类型，JudgeSubmitRequest接口要求是boolean而非null
      answer: selectedAnswer.value === null ? false : selectedAnswer.value
    }
    
    console.log('提交数据:', JSON.stringify(submitData))
    
    const res = await submissionApi.submitJudge(submitData)
    
    console.log('提交答案API返回:', JSON.stringify(res))
    
    if (res.code === 0 && res.data) {
      ElMessage.success('提交成功')
      
      // 提交成功后显示解析
      showAnalysis.value = true
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交答案异常:', error)
    ElMessage.error('提交失败，请稍后重试')
  }
}

// 加载题目数据
const loadProblem = async (id?: number | string): Promise<void> => {
  // 优先使用传入的ID，其次使用props，最后使用路由参数
  let problemId = id || props.problemId
  
  // 处理路由参数，可能是字符串、数字或数组
  if (!problemId && route.params.id) {
    // 如果是数组，取第一个元素
    problemId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  }
  
  if (!problemId) {
    console.error('题目ID不能为空')
    ElMessage.error('题目ID不能为空')
    router.push('/problems')
    return
  }
  
  try {
    console.log('开始加载题目详情，ID:', problemId)
    const res = await problemApi.getProblemDetail(Number(problemId))
    
    console.log('题目详情API完整响应:', JSON.stringify(res))
    originalResponse.value = res.data // 保存原始数据用于调试
    
    if (res.code === 0 && res.data) {
      console.log('题目类型:', res.data.type)
      
      // 修改判断逻辑，同时检查type和answer字段
      if (res.data.type !== 'JUDGE' && res.data.answer === undefined) {
        console.warn('当前题目不是判断题:', res.data.type)
        ElMessage.warning('当前题目不是判断题')
        router.push(`/problem/${problemId}`)
        return
      }

      // 如果type为null但存在answer，视为判断题处理
      if (!res.data.type && res.data.answer !== undefined) {
        console.log('题目type为null但有answer字段，按判断题处理')
      }

      // 将后端返回的数据转换为我们自定义的JudgeProblemVO类型
      const responseData = res.data as any; // 临时使用any类型避免类型错误
      
      problem.value = {
        id: responseData.id,
        title: responseData.title,
        content: responseData.content,
        difficulty: responseData.difficulty,
        type: responseData.type,
        // 确保answer字段类型正确
        answer: typeof responseData.answer === 'boolean' ? responseData.answer : undefined,
        // 从响应中获取analysis字段
        analysis: responseData.analysis || '',
        tags: responseData.tags,
        submitCount: responseData.submitCount,
        acceptCount: responseData.acceptCount,
        createTime: responseData.createTime,
        updateTime: responseData.updateTime,
        userId: responseData.userId,
        userName: responseData.userName
      }
      
      // 调试信息
      console.log('成功加载题目详情:', JSON.stringify(problem.value))
      console.log('answer格式:', typeof problem.value?.answer, problem.value?.answer)
      
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
  console.log('JudgeProblemDetail组件已挂载，路由参数:', route.params)
})
</script>

<style scoped>
.judge-problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-card {
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 900px;
  margin: 0 auto;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-title {
  display: flex;
  flex-direction: column;
}

.problem-title h2 {
  margin-bottom: 10px;
  color: #303133;
}

.problem-meta {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.problem-stats {
  margin-left: 15px;
  color: #606266;
  font-size: 14px;
}

.ml-2 {
  margin-left: 8px;
}

.problem-content {
  margin: 20px 0;
  line-height: 1.6;
  font-size: 16px;
  color: #303133;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.judge-options {
  margin-top: 30px;
}

.judge-options h3 {
  margin-bottom: 20px;
  color: #303133;
}

.judge-radio-group {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 30px;
}

.judge-option {
  padding: 20px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
  width: 140px;
  height: 140px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
}

.judge-option:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.judge-option .el-radio__label {
  font-size: 18px;
}

.submit-btn {
  margin-top: 20px;
  text-align: center;
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
  color: #303133;
}

.divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 30px 0 20px;
}

.answer-analysis {
  margin-top: 20px;
}

.answer-analysis h3 {
  margin-bottom: 15px;
  color: #303133;
}

.correct-answer {
  margin-bottom: 15px;
  font-size: 16px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
  color: #303133;
}

.analysis-content {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  color: #303133;
}

.debug-panel {
  margin-top: 20px;
  background-color: #fef0f0;
  color: #f56c6c;
  max-width: 900px;
  margin: 20px auto 0;
}

.debug-panel pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #303133;
  color: #ffffff;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
  margin: 10px 0;
}

.debug-panel h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  border-bottom: 1px solid #f56c6c;
  padding-bottom: 5px;
}

.loading-content {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}
</style>