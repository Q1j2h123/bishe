<template>
  <div class="program-problem-container">
    <!-- 题目详情区域 -->
    <el-card class="problem-card">
      <template #header>
        <div class="problem-header">
          <div class="problem-title">
            <h2>{{ problem?.title || '加载中...' }}</h2>
            <div class="problem-meta">
              <el-tag type="primary" size="small">编程题</el-tag>
              <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="ml-2">{{ problem?.difficulty || '未知' }}</el-tag>
              <span class="problem-stats">通过率: {{ problem?.acceptCount && problem?.submitCount ? Math.round((problem.acceptCount / problem.submitCount) * 100) + '%' : '0%' }}</span>
            </div>
          </div>
          <el-button type="primary" plain size="small" @click="returnToList" :icon="Back">返回题目列表</el-button>
        </div>
      </template>
      
      <!-- 题目内容 -->
      <div class="problem-content">
        <div v-if="problem?.content" class="markdown-body" v-html="renderMarkdown(problem.content)"></div>
        <div v-else class="loading-content">
          <el-skeleton :rows="5" animated />
        </div>
      </div>
      
      <!-- 函数签名信息 -->
      <div class="function-signature" v-if="problem">
        <h3>函数签名</h3>
        <el-alert
          type="info"
          :closable="false"
        >
          <pre class="function-def">{{ generateFunctionSignature() }}</pre>
        </el-alert>
      </div>
      
      <!-- 示例测试用例 -->
      <div class="test-cases" v-if="problem && problem.testCases && problem.testCases.length > 0">
        <h3>示例用例</h3>
        <div v-for="(testCase, index) in problem.testCases.slice(0, 3)" :key="index" class="test-case">
          <div class="test-case-header">
            <span class="test-case-title">示例 {{ index + 1 }}</span>
          </div>
          <div class="test-case-content">
            <div class="test-case-input">
              <strong>输入:</strong> 
              <pre>{{ testCase.input }}</pre>
            </div>
            <div class="test-case-output">
              <strong>输出:</strong>
              <pre>{{ testCase.output }}</pre>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 代码编辑器 -->
      <div class="code-editor-section">
        <h3>代码编辑器</h3>
        
        <!-- 语言选择器 -->
        <div class="language-selector">
          <span>选择语言:</span>
          <el-select 
            v-model="selectedLanguage" 
            placeholder="请选择编程语言"
            @change="updateCodeTemplate"
          >
            <el-option
              v-for="lang in availableLanguages"
              :key="lang.value"
              :label="lang.label"
              :value="lang.value"
            />
          </el-select>
        </div>
        
        <!-- 代码编辑器 - 简单文本区域 -->
        <div class="editor-container">
          <textarea 
            v-model="code" 
            class="code-textarea"
            spellcheck="false"
            placeholder="在此编写代码..."
            rows="15"
          ></textarea>
        </div>
        
        <!-- 操作按钮 -->
        <div class="editor-actions">
          <el-button type="primary" @click="runCode">运行代码</el-button>
          <el-button type="success" @click="submitCode">提交代码</el-button>
          <el-button @click="resetCode">重置代码</el-button>
        </div>
      </div>
      
      <!-- 运行结果 -->
      <div class="execution-results" v-if="runResults.show">
        <h3>运行结果</h3>
        <el-alert
          :type="runResults.success ? 'success' : 'error'"
          :title="runResults.success ? '测试通过' : '测试失败'"
          :closable="false"
          show-icon
        >
          <div v-if="runResults.success">
            <div>执行时间: {{ runResults.executionTime }} ms</div>
            <div>内存消耗: {{ runResults.memoryUsage }} MB</div>
          </div>
          <div v-else>
            <pre class="error-message">{{ runResults.errorMessage }}</pre>
          </div>
        </el-alert>
      </div>
      
      <!-- 运行限制信息 -->
      <div class="execution-limits" v-if="problem">
        <h3>执行限制</h3>
        <div class="limits-info">
          <el-tag type="info">时间限制: {{ problem.timeLimit || 1000 }} ms</el-tag>
          <el-tag type="info" class="ml-2">内存限制: {{ problem.memoryLimit || 256 }} MB</el-tag>
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
        <pre>函数名: {{ problem?.functionName }}</pre>
        <pre>参数类型: {{ problem?.paramTypes }}</pre>
        <pre>返回类型: {{ problem?.returnType }}</pre>
        
        <h4>测试用例信息:</h4>
        <pre>{{ JSON.stringify(problem?.testCases, null, 2) }}</pre>
        
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO } from '@/api/problem'
import { submissionApi, type ProgramSubmitRequest } from '@/api/submission'
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

// 编程题数据类型 - 移除了analysis字段
interface ProgramProblemVO {
  id?: number;
  title?: string;
  content?: string;
  difficulty?: string;
  type?: string;
  functionName?: string;
  paramTypes?: string[];
  returnType?: string;
  testCases?: Array<{
    input: string;
    output: string;
  }>;
  templates?: Record<string, string>;
  timeLimit?: number;
  memoryLimit?: number;
  tags?: string[];
  submitCount?: number;
  acceptCount?: number;
  createTime?: string;
  updateTime?: string;
  userId?: number;
  userName?: string;
}

// 运行结果类型
interface RunResult {
  show: boolean;
  success: boolean;
  executionTime?: number;
  memoryUsage?: number;
  output?: string;
  errorMessage?: string;
}

// 题目数据
const problem = ref<ProgramProblemVO | null>(null)

// 代码编辑器状态
const selectedLanguage = ref<string>('')
const code = ref<string>('')
const latestSubmissionId = ref<number | null>(null)
const runResults = ref<RunResult>({
  show: false,
  success: false
})

// 可用编程语言
const languageDisplayNames: Record<string, string> = {
  'java': 'Java',
  'python': 'Python',
  'cpp': 'C++',
  'javascript': 'JavaScript',
  'c': 'C',
  'csharp': 'C#',
  'go': 'Go',
  'ruby': 'Ruby',
  'php': 'PHP',
  'swift': 'Swift',
  'kotlin': 'Kotlin',
  'rust': 'Rust'
};

const availableLanguages = ref<{ value: string; label: string }[]>([])

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

// 生成函数签名
const generateFunctionSignature = (): string => {
  if (!problem.value) return ''
  
  const func = problem.value
  const funcName = func.functionName || 'solution'
  const returnType = func.returnType || 'void'
  const params = func.paramTypes || []
  
  let signature = ''
  
  // 根据不同语言生成不同的函数签名
  switch (selectedLanguage.value) {
    case 'java':
      signature = `public ${returnType} ${funcName}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {
    // 请实现此函数
}`
      break
    case 'python':
      signature = `def ${funcName}(${params.map((_, index) => `param${index + 1}`).join(', ')}):
    # 请实现此函数
    pass`
      break
    case 'cpp':
      signature = `${returnType} ${funcName}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {
    // 请实现此函数
}`
      break
    case 'javascript':
      signature = `function ${funcName}(${params.map((_, index) => `param${index + 1}`).join(', ')}) {
    // 请实现此函数
}`
      break
    default:
      signature = `${returnType} ${funcName}(${params.join(', ')}) {
    // 请实现此函数
}`
  }
  
  return signature
}

// 处理模板数据并配置编辑器
const processTemplates = () => {
  if (!problem.value || !problem.value.templates) {
    return;
  }
  
  const templates = problem.value.templates;
  const templateLanguages = Object.keys(templates);
  
  if (templateLanguages.length === 0) {
    return;
  }
  
  // 根据templates中的语言生成可选列表
  availableLanguages.value = templateLanguages.map(lang => ({
    value: lang,
    label: languageDisplayNames[lang.toLowerCase()] || lang
  }));
  
  console.log('可用编程语言:', availableLanguages.value);
  
  // 设置默认语言为第一个可用语言
  if (selectedLanguage.value === '' || !templateLanguages.includes(selectedLanguage.value)) {
    selectedLanguage.value = templateLanguages[0];
  }
  
  // 设置默认代码为当前选中语言的模板
  updateCodeTemplate();
};

// 更新代码模板
const updateCodeTemplate = () => {
  if (!problem.value || !problem.value.templates || !selectedLanguage.value) {
    return;
  }
  
  const template = problem.value.templates[selectedLanguage.value];
  if (template) {
    code.value = template;
    console.log(`已加载${selectedLanguage.value}语言模板`);
  }
};

// 监听语言选择变化，更新代码模板
watchEffect(() => {
  if (selectedLanguage.value) {
    updateCodeTemplate();
  }
});

// 重置代码
const resetCode = () => {
  if (!problem.value || !problem.value.templates || !selectedLanguage.value) {
    return;
  }
  
  const template = problem.value.templates[selectedLanguage.value];
  if (template) {
    code.value = template;
    ElMessage.info('已重置代码为初始模板');
  }
};

// 运行代码
const runCode = async () => {
  if (!problem.value || !problem.value.id) {
    ElMessage.error('题目信息不完整');
    return;
  }
  
  if (!code.value.trim()) {
    ElMessage.warning('请编写代码后再运行');
    return;
  }
  
  ElMessage.info('正在运行代码...');
  
  try {
    // 准备运行请求数据
    const runData = {
      problemId: problem.value.id,
      language: selectedLanguage.value,
      code: code.value
    };
    
    console.log('运行代码数据:', runData);
    
    // 在实际应用中，这里应当调用API
    // const res = await codeApi.runCode(runData);
    
    // 模拟运行结果 - 实际中应替换为API调用
    setTimeout(() => {
      // 随机模拟成功或失败
      const success = Math.random() > 0.3;
      
      runResults.value = {
        show: true,
        success: success,
        executionTime: Math.floor(Math.random() * 100) + 10, // 10-110ms
        memoryUsage: Math.floor(Math.random() * 50) + 20, // 20-70MB
        output: success ? '执行成功!' : '',
        errorMessage: success ? '' : '运行时错误: 数组越界或空指针异常'
      };
      
      if (success) {
        ElMessage.success('代码运行成功');
      } else {
        ElMessage.error('代码运行失败');
      }
    }, 1000);
  } catch (error) {
    console.error('运行代码异常:', error);
    runResults.value = {
      show: true,
      success: false,
      errorMessage: '系统错误，请稍后重试'
    };
    ElMessage.error('运行失败，请稍后重试');
  }
};

// 添加返回列表函数
const returnToList = () => {
  router.push({
    path: '/problems',
    query: { 
      t: Date.now().toString(), // 添加时间戳强制刷新
      forceRefresh: 'true' // 添加强制刷新标记
    }
  });
}

// 提交代码
const submitCode = async () => {
  // 与运行代码类似，但调用提交API
  if (!problem.value || !problem.value.id) {
    ElMessage.error('题目信息不完整');
    return;
  }
  
  if (!code.value.trim()) {
    ElMessage.warning('请编写代码后再提交');
    return;
  }
  
  try {
    ElMessage.info('正在提交代码...');
    
    // 准备提交请求
    const submitData: ProgramSubmitRequest = {
      problemId: problem.value.id,
      language: selectedLanguage.value,
      code: code.value
    };
    
    console.log('提交数据:', submitData);
    
    // 调用提交API
    const res = await submissionApi.submitProgram(submitData);
    
    if (res.code === 0) {
      // 获取提交ID
      const submissionId = res.data?.id
      
      if (submissionId) {
        // 存储当前提交的ID
        latestSubmissionId.value = submissionId
        
        // 显示提交成功消息
        ElMessage.success('代码提交成功，正在评测...')
        
        // 开始轮询结果
        setTimeout(() => {
          pollSubmissionResult(submissionId)
        }, 1000)
      } else {
        ElMessage.warning('提交成功，但未返回评测ID')
      }
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交代码出错:', error)
    ElMessage.error('提交失败，请稍后重试')
  }
};

// 轮询提交结果
const pollSubmissionResult = async (submissionId: number) => {
  try {
    const res = await submissionApi.getSubmission(submissionId)
    if (res.code === 0 && res.data) {
      const result = res.data
      console.log('轮询得到的结果:', result)
      
      // 如果结果已经出来（状态不是PENDING或JUDGING）
      if (result.status !== 'PENDING' && result.status !== 'JUDGING') {
        // 处理最终结果
        handlePollResult(result)
      } else {
        // 继续轮询
        setTimeout(() => {
          pollSubmissionResult(submissionId)
        }, 1000)
      }
    } else {
      console.error('获取提交详情失败:', res.message)
    }
  } catch (error) {
    console.error('轮询提交结果出错:', error)
  }
}

// 处理轮询结果
const handlePollResult = (result: any) => {
  // 根据评测结果更新UI
  if (result.status === 'ACCEPTED') {
    ElMessageBox.confirm('恭喜，您的代码通过了所有测试用例！是否返回题目列表？', '提交成功', {
      confirmButtonText: '返回题目列表',
      cancelButtonText: '继续编写',
      type: 'success'
    }).then(() => {
      returnToList()
    }).catch(() => {
      // 用户选择继续编写，不做操作
    })
  } else if (result.status === 'WRONG_ANSWER') {
    ElMessageBox.alert('很遗憾，您的代码未通过所有测试用例，请检查代码逻辑后重新提交。', '提交结果', {
      confirmButtonText: '我知道了',
      type: 'error'
    });
  } else if (result.status?.includes('ERROR')) {
    ElMessageBox.alert(`您的代码执行出错: ${result.message || '未知错误'}`, '提交结果', {
      confirmButtonText: '我知道了',
      type: 'warning'
    });
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
      
      // 验证题目类型
      if (res.data.type !== 'PROGRAM') {
        console.warn('当前题目不是编程题:', res.data.type)
        ElMessage.warning('当前题目不是编程题')
        router.push(`/problem/${problemId}`)
        return
      }

      // 将后端返回的数据转换为我们自定义的ProgramProblemVO类型
      const responseData = res.data as any // 临时使用any类型避免类型错误
      
      problem.value = {
        id: responseData.id,
        title: responseData.title,
        content: responseData.content,
        difficulty: responseData.difficulty,
        type: responseData.type,
        functionName: responseData.functionName,
        paramTypes: responseData.paramTypes || [],
        returnType: responseData.returnType,
        testCases: responseData.testCases || [],
        templates: responseData.templates || {},
        timeLimit: responseData.timeLimit,
        memoryLimit: responseData.memoryLimit,
        tags: responseData.tags,
        submitCount: responseData.submitCount,
        acceptCount: responseData.acceptCount,
        createTime: responseData.createTime,
        updateTime: responseData.updateTime,
        userId: responseData.userId,
        userName: responseData.userName
      }
      
      // 设置默认代码
      if (problem.value.templates && problem.value.templates[selectedLanguage.value]) {
        code.value = problem.value.templates[selectedLanguage.value]
      } else {
        code.value = generateFunctionSignature()
      }
      
      // 调试信息
      console.log('成功加载题目详情:', JSON.stringify(problem.value))
      
      // 处理模板数据
      processTemplates();
      
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
  console.log('ProgramProblemDetail组件已挂载，路由参数:', route.params)
})
</script>

<style scoped>
.program-problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-card {
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-width: 1000px;
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
  
  .function-signature {
  margin: 20px 0;
}

.function-signature h3 {
  margin-bottom: 15px;
  color: #303133;
}

.function-def {
  font-family: 'Consolas', 'Monaco', monospace;
    background-color: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  }
  
  .test-cases {
  margin: 20px 0;
}

.test-cases h3 {
  margin-bottom: 15px;
  color: #303133;
  }
  
  .test-case {
    background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  border: 1px solid #ebeef5;
  }
  
  .test-case-header {
  margin-bottom: 10px;
    font-weight: bold;
  color: #303133;
  }
  
  .test-case-content {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
.test-case-input pre,
.test-case-output pre {
  background-color: #ffffff;
  padding: 8px;
  border-radius: 4px;
  margin: 5px 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-x: auto;
}

.code-editor-section {
  margin: 20px 0;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.code-editor-section h3 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 18px;
}

.language-selector {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.editor-container {
  margin: 15px 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background-color: #1e1e1e;
}

.code-textarea {
  width: 100%;
  min-height: 350px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  padding: 15px;
  font-size: 14px;
  line-height: 1.6;
  color: #e2e2e2;
  background-color: #1e1e1e;
  border: none;
  resize: vertical;
}

.editor-actions {
  display: flex;
  gap: 10px;
  margin: 15px 0;
}

.execution-results {
  margin: 20px 0;
}

.execution-results h3 {
  margin-bottom: 15px;
  color: #303133;
}

.error-message {
  color: #f56c6c;
  background-color: #fef0f0;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.execution-limits {
  margin: 20px 0;
}

.execution-limits h3 {
  margin-bottom: 15px;
  color: #303133;
}

.limits-info {
    display: flex;
    gap: 10px;
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

.debug-panel {
    margin-top: 20px;
  background-color: #fef0f0;
  color: #f56c6c;
  max-width: 1000px;
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