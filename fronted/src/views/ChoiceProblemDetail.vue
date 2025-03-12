<template>
  <div class="choice-problem-container">
    <!-- 题目详情区域 -->
    <el-card class="problem-card">
      <template #header>
        <div class="problem-header">
          <div class="problem-title">
            <h2>{{ problem?.title || '加载中...' }}</h2>
            <div class="problem-meta">
              <el-tag type="success" size="small">选择题</el-tag>
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
      
      <!-- 选项列表和提交区域 -->
      <div class="choice-options">
        <h3>请选择答案：</h3>
        
        <!-- 选项列表 - 有选项时显示 -->
        <div v-if="problem?.options && Array.isArray(problem.options) && problem.options.length > 0">
          <el-checkbox-group v-model="selectedAnswers">
            <div v-for="(option, index) in problem.options" :key="index" class="choice-option">
              <el-checkbox :label="getOptionKey(option, index)" class="option-checkbox">
                <div class="option-label">{{ getOptionKey(option, index) }}</div>
                <div class="option-content">{{ option.content || '选项内容缺失' }}</div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          
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
        <pre>选项数量: {{ problem?.options?.length || 0 }}</pre>
        <pre>已选答案: {{ selectedAnswers }}</pre>
        <pre>标准答案: {{ problem?.answer }}</pre>
        
        <h4>选项数据：</h4>
        <div v-if="problem?.options?.length">
          <div v-for="(opt, index) in problem.options" :key="index" class="debug-option">
            <pre>选项 {{ index + 1 }}: {{ JSON.stringify(opt, null, 2) }}</pre>
          </div>
        </div>
        <div v-else>
          <pre>无选项数据</pre>
        </div>
        
        <h4>完整题目数据：</h4>
        <pre>{{ JSON.stringify(problem, null, 2) }}</pre>
        
        <h4>原始API返回数据：</h4>
        <pre>{{ JSON.stringify(originalOptions, null, 2) }}</pre>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.choice-problem-container {
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

.choice-options {
  margin-top: 30px;
}

.choice-options h3 {
  margin-bottom: 15px;
  color: #303133;
}

.choice-option {
  margin-bottom: 15px;
  padding: 15px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: background-color 0.2s;
  border: 1px solid #ebeef5;
}

.choice-option:hover {
  background-color: #f0f0f0;
}

.option-checkbox {
  width: 100%;
  display: flex;
  align-items: flex-start;
}

.option-label {
  margin-right: 15px;
  font-weight: bold;
  color: #409EFF;
  min-width: 25px;
  font-size: 16px;
}

.option-content {
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.submit-btn {
  margin-top: 20px;
  text-align: right;
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

.debug-option {
  margin-bottom: 8px;
  border-left: 3px solid #409EFF;
  padding-left: 10px;
}

.loading-content {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.option-warning {
  color: #E6A23C;
  background-color: #fdf6ec;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #E6A23C;
}
</style>

<script setup lang="ts">
import { ref, onMounted, defineProps, watchEffect } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { problemApi, type ProblemVO, type ChoiceOption } from '@/api/problem'
import { submissionApi, type SubmissionVO, type ChoiceSubmitRequest } from '@/api/submission'
// @ts-ignore
import MarkdownIt from 'markdown-it'

// 调试模式 - 临时设置为true，用于调试
const debug = ref(true)

// 定义属性，允许从父组件传入问题ID
const props = defineProps<{
  problemId?: number
}>()

// 保存原始选项数据用于调试
const originalOptions = ref<any>(null)

// 自定义选项类型，兼容后端返回的不同格式
interface ProblemOptionDTO {
  key?: string;
  id?: number;
  content: string;
  isCorrect?: boolean;
}

// 扩展自定义的ChoiceProblemVO类型，避免直接扩展ProblemVO导致类型冲突
interface ChoiceProblemVO {
  id?: number;
  title?: string;
  content?: string;
  difficulty?: string;
  type?: string;
  options?: ProblemOptionDTO[];
  answer?: string; // 选择题的答案是字符串类型
  analysis?: string;
  tags?: string[];
  submitCount?: number;
  acceptCount?: number;
  createTime?: string;
  updateTime?: string;
  userId?: number;
  userName?: string;
}

// 获取选项的key值，兼容不同数据格式
const getOptionKey = (option: ProblemOptionDTO, index: number): string => {
  if (option.key) return option.key;
  return String.fromCharCode(65 + index); // 生成A, B, C, D...
}

// 题目数据
const problem = ref<ChoiceProblemVO | null>(null)
// 选中的答案
const selectedAnswers = ref<string[]>([])
// 是否显示解析
const showAnalysis = ref<boolean>(false)
// 正确答案
const correctAnswerDisplay = ref<string>('')
// 解析内容
const analysisContent = ref<string>('')

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
  if (!selectedAnswers.value.length) {
    ElMessage.warning('请至少选择一个答案')
    return
  }
  
  if (!problem.value?.id) {
    ElMessage.error('题目信息不完整')
    return
  }
  
  try {
    console.log('准备提交选择题答案:', selectedAnswers.value)
    
    // 将选择的答案排序
    const sortedAnswers = [...selectedAnswers.value].sort()
    
    console.log('排序后的答案:', sortedAnswers)
    
    // 提交请求对象 - 使用字母答案格式（如 A,B,C）
    const submitData: ChoiceSubmitRequest = {
      problemId: problem.value.id,
      answer: sortedAnswers // 提交选项的key值
    }
    
    console.log('提交数据:', JSON.stringify(submitData))
    
    const res = await submissionApi.submitChoice(submitData)
    
    console.log('提交答案API返回:', JSON.stringify(res))
    
    if (res.code === 0 && res.data) {
      ElMessage.success('提交成功')
      
      // 提交成功后显示解析
      showAnalysis.value = true
      
      // 设置正确答案和解析内容
      if (problem.value?.answer) {
        correctAnswerDisplay.value = problem.value.answer
      }
      
      if (problem.value?.analysis) {
        analysisContent.value = problem.value.analysis
      }
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
    
    if (res.code === 0 && res.data) {
      console.log('题目类型:', res.data.type)
      console.log('原始选项数据:', JSON.stringify(res.data.options))
      originalOptions.value = res.data.options // 保存原始数据用于调试
      
      // 修改判断逻辑，同时检查type和options
      if (res.data.type !== 'CHOICE' && (!res.data.options || !Array.isArray(res.data.options))) {
        console.warn('当前题目不是选择题或没有选项数据:', res.data.type)
        ElMessage.warning('当前题目不是选择题或缺少选项数据')
        router.push(`/problem/${problemId}`)
        return
      }

      // 如果type为null但存在options，视为选择题处理
      if (!res.data.type && res.data.options && Array.isArray(res.data.options) && res.data.options.length > 0) {
        console.log('题目type为null但有选项数据，按选择题处理')
      }

      // 直接使用后端返回的数据，不做修改
      problem.value = {
        id: res.data.id,
        title: res.data.title,
        content: res.data.content,
        difficulty: res.data.difficulty,
        type: res.data.type,
        options: res.data.options, // 使用原始选项数据
        answer: typeof res.data.answer === 'string' ? res.data.answer : String(res.data.answer),
        analysis: (res.data as any).analysis || '',
        tags: res.data.tags,
        submitCount: res.data.submitCount,
        acceptCount: res.data.acceptCount,
        createTime: res.data.createTime,
        updateTime: res.data.updateTime,
        userId: res.data.userId,
        userName: res.data.userName
      }
      
      // 调试信息
      console.log('成功加载题目详情:', JSON.stringify(problem.value))
      console.log('problem.options是否存在:', !!problem.value?.options)
      console.log('选项数量:', problem.value?.options?.length || 0)
      console.log('选项数据:', JSON.stringify(problem.value?.options))
      console.log('答案格式:', typeof problem.value?.answer, problem.value?.answer)
      
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
</script>