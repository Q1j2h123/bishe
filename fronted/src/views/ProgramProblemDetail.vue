<template>
  <div class="program-problem-container">
    <el-row :gutter="20" justify="center">
      <el-col :xs="24" :sm="22" :md="20" :lg="18" :xl="16">
        <!-- 题目基本信息 -->
        <el-card class="problem-card">
          <template #header>
            <div class="problem-header">
              <div class="problem-title">
                <h2>{{ problem?.title }}</h2>
                <div class="problem-meta">
                  <el-tag type="primary" size="small">编程题</el-tag>
                  <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="ml-2">{{ problem?.difficulty }}</el-tag>
                  <span class="problem-stats">通过率: {{ problem?.acceptCount && problem?.submitCount ? Math.round((problem.acceptCount / problem.submitCount) * 100) + '%' : '0%' }}</span>
                </div>
              </div>
            </div>
          </template>
          
          <!-- 题目内容 -->
          <div class="problem-content markdown-body" v-html="renderMarkdown(problem?.content || '')"></div>
          
          <!-- 函数信息 -->
          <div class="function-info" v-if="problem?.functionInfo">
            <h3>函数信息</h3>
            <div class="function-signature">
              <pre><code>{{ problem.functionInfo.signature }}</code></pre>
            </div>
            <div class="function-description">{{ problem.functionInfo.description }}</div>
            
            <h4>参数说明：</h4>
            <ul class="params-list">
              <li v-for="(param, index) in problem.functionInfo.params" :key="index">
                <strong>{{ param.name }}</strong>: {{ param.description }} ({{ param.type }})
              </li>
            </ul>
            
            <h4>返回值：</h4>
            <div class="return-info">
              <p><strong>类型：</strong> {{ problem.functionInfo.returnType }}</p>
              <p>{{ problem.functionInfo.returnDescription }}</p>
            </div>
          </div>
          
          <!-- 限制信息 -->
          <div class="constraints" v-if="problem?.timeLimit || problem?.memoryLimit">
            <h3>限制条件</h3>
            <ul>
              <li v-if="problem?.timeLimit">时间限制: {{ problem.timeLimit }}ms</li>
              <li v-if="problem?.memoryLimit">内存限制: {{ problem.memoryLimit }}MB</li>
            </ul>
          </div>
          
          <!-- 示例测试用例 -->
          <div class="test-cases" v-if="problem?.testCases && problem.testCases.length > 0">
            <h3>示例测试用例</h3>
            <el-collapse>
              <el-collapse-item v-for="(testCase, index) in problem.testCases" :key="index" :title="`示例 ${index + 1}`">
                <div class="test-case">
                  <div class="test-case-input">
                    <h4>输入:</h4>
                    <pre><code>{{ testCase.input }}</code></pre>
                  </div>
                  <div class="test-case-output">
                    <h4>预期输出:</h4>
                    <pre><code>{{ testCase.output }}</code></pre>
                  </div>
                  <div class="test-case-explanation" v-if="testCase.explanation">
                    <h4>解释:</h4>
                    <p>{{ testCase.explanation }}</p>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
        
        <!-- 代码编辑器 -->
        <el-card class="code-editor-card">
          <template #header>
            <div class="card-header">
              <h3>代码编辑器</h3>
              <div class="editor-options">
                <el-select v-model="selectedLanguage" placeholder="选择语言" size="small">
                  <el-option v-for="lang in supportedLanguages" :key="lang.value" :label="lang.label" :value="lang.value" />
                </el-select>
              </div>
            </div>
          </template>
          
          <!-- Monaco编辑器 -->
          <div class="monaco-editor-container">
            <div id="monaco-editor" ref="monacoEditorContainer" style="height: 500px; width: 100%;"></div>
          </div>
          
          <!-- 编辑器功能按钮 -->
          <div class="editor-actions">
            <el-button type="primary" @click="runCode" :disabled="isSubmitting">
              <el-icon><VideoPlay /></el-icon> 运行代码
            </el-button>
            <el-button type="success" @click="submitCode" :disabled="isSubmitting">
              <el-icon><Upload /></el-icon> 提交代码
            </el-button>
            <el-button @click="resetEditor">
              <el-icon><RefreshRight /></el-icon> 重置代码
            </el-button>
          </div>
          
          <!-- 运行结果 -->
          <div class="run-result" v-if="runResult">
            <h3>运行结果</h3>
            <el-alert
              :type="runResult.status === 'ACCEPTED' ? 'success' : 'error'"
              :title="runResult.status === 'ACCEPTED' ? '通过' : '失败'"
              :description="runResult.message"
              show-icon
            />
            
            <div class="result-details" v-if="runResult.testCaseResults && runResult.testCaseResults.length > 0">
              <h4>测试用例结果:</h4>
              <el-table :data="runResult.testCaseResults" border stripe>
                <el-table-column label="测试用例" width="100" type="index" />
                <el-table-column label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.passed ? 'success' : 'danger'">
                      {{ scope.row.passed ? '通过' : '失败' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="输入" show-overflow-tooltip>
                  <template #default="scope">
                    <pre>{{ scope.row.input }}</pre>
                  </template>
                </el-table-column>
                <el-table-column label="预期输出" show-overflow-tooltip>
                  <template #default="scope">
                    <pre>{{ scope.row.expectedOutput }}</pre>
                  </template>
                </el-table-column>
                <el-table-column label="实际输出" show-overflow-tooltip>
                  <template #default="scope">
                    <pre>{{ scope.row.actualOutput }}</pre>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="execution-stats" v-if="runResult.executeTime !== undefined || runResult.executeMemory !== undefined">
              <h4>执行统计:</h4>
              <p>执行时间: {{ runResult.executeTime }}ms</p>
              <p>内存消耗: {{ runResult.executeMemory }}MB</p>
            </div>
          </div>
        </el-card>
        
        <!-- 提交历史 -->
        <el-card class="submission-history-card">
          <template #header>
            <div class="card-header">
              <h3>我的提交历史</h3>
            </div>
          </template>
          <div v-if="submissionHistory.length > 0">
            <el-table :data="submissionHistory" style="width: 100%">
              <el-table-column prop="id" label="提交ID" width="80" />
              <el-table-column prop="createTime" label="提交时间" width="160">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="150">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="language" label="语言" width="100" />
              <el-table-column prop="executeTime" label="执行时间" width="100">
                <template #default="scope">
                  {{ scope.row.executeTime ? scope.row.executeTime + 'ms' : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="executeMemory" label="内存" width="100">
                <template #default="scope">
                  {{ scope.row.executeMemory ? scope.row.executeMemory + 'MB' : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" type="primary" text @click="viewSubmission(scope.row.id)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无提交记录" />
        </el-card>
        
        <!-- 相关题目推荐 -->
        <el-card class="related-problems-card">
          <template #header>
            <div class="card-header">
              <h3>相关题目推荐</h3>
            </div>
          </template>
          <div class="related-problems" v-if="relatedProblems.length > 0">
            <el-table :data="relatedProblems" style="width: 100%">
              <el-table-column prop="id" label="题号" width="70" />
              <el-table-column prop="title" label="题目">
                <template #default="scope">
                  <router-link :to="`/problem/${scope.row.id}`">{{ scope.row.title }}</router-link>
                </template>
              </el-table-column>
              <el-table-column prop="difficulty" label="难度" width="80">
                <template #default="scope">
                  <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">{{ scope.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无相关题目推荐" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Upload, RefreshRight } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO } from '@/api/problem'
import { submissionApi, type SubmissionVO } from '@/api/submission'
// @ts-ignore
import MarkdownIt from 'markdown-it'
// Monaco编辑器相关导入
import * as monaco from 'monaco-editor'

// 编辑器实例
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// 扩展ProblemVO类型，添加编程题需要的字段
interface ExtendedProblemVO extends ProblemVO {
  functionInfo?: {
    signature: string;
    description: string;
    params: Array<{
      name: string;
      type: string;
      description: string;
    }>;
    returnType: string;
    returnDescription: string;
  };
  timeLimit?: number;
  memoryLimit?: number;
  testCases: Array<{
    input: string;
    output: string;
    explanation?: string;
  }>;
}

// 定义运行结果类型
interface RunResult {
  status: string;
  message: string;
  executeTime?: number;
  executeMemory?: number;
  testCaseResults?: Array<{
    passed: boolean;
    input: string;
    expectedOutput: string;
    actualOutput: string;
  }>;
}

// 题目数据
const problem = ref<ExtendedProblemVO | null>(null)
// 编辑器所在容器
const monacoEditorContainer = ref<HTMLElement | null>(null)
// 选择的编程语言
const selectedLanguage = ref<string>('java')
// 支持的编程语言列表
const supportedLanguages = [
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'python' },
  { label: 'C++', value: 'cpp' },
  { label: 'JavaScript', value: 'javascript' }
]
// 是否正在提交
const isSubmitting = ref<boolean>(false)
// 运行结果
const runResult = ref<RunResult | null>(null)
// 路由工具
const router = useRouter()
const route = useRoute()
// 相关题目
const relatedProblems = ref<Array<any>>([])
// 提交历史
const submissionHistory = ref<SubmissionVO[]>([])

// 获取代码编辑器内容
const getEditorValue = (): string => {
  return editor?.getValue() || ''
}

// 设置代码编辑器内容
const setEditorValue = (value: string) => {
  editor?.setValue(value)
}

// 初始化Monaco编辑器
const initEditor = async () => {
  if (!monacoEditorContainer.value) return
  
  // 创建编辑器实例
  editor = monaco.editor.create(monacoEditorContainer.value, {
    value: getTemplateCode(selectedLanguage.value),
    language: getMonacoLanguage(selectedLanguage.value),
    theme: 'vs-dark',
    automaticLayout: true,
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    fontSize: 14,
    tabSize: 2,
    wordWrap: 'on'
  })
  
  // 监听语言变化，更新编辑器语言
  watch(selectedLanguage, (newLang: string) => {
    if (!editor) return
    
    // 当切换语言时，提示用户是否切换模板代码
    if (getEditorValue().trim() && getEditorValue() !== getTemplateCode(newLang)) {
      ElMessageBox.confirm('切换语言将会替换当前代码，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        monaco.editor.setModelLanguage(editor!.getModel()!, getMonacoLanguage(newLang))
        setEditorValue(getTemplateCode(newLang))
      }).catch(() => {
        // 取消切换，恢复到原来的语言
        selectedLanguage.value = newLang
      })
    } else {
      // 直接切换语言和代码
      monaco.editor.setModelLanguage(editor!.getModel()!, getMonacoLanguage(newLang))
      setEditorValue(getTemplateCode(newLang))
    }
  })
}

// 根据语言获取对应的Monaco编辑器语言
const getMonacoLanguage = (lang: string): string => {
  const map: Record<string, string> = {
    'java': 'java',
    'python': 'python',
    'cpp': 'cpp',
    'javascript': 'javascript'
  }
  return map[lang] || 'java'
}

// 根据语言获取对应的代码模板
const getTemplateCode = (lang: string): string => {
  if (!problem.value || !problem.value.functionInfo) return ''
  
  // 根据题目的函数信息生成不同语言的代码模板
  const { signature, params, returnType } = problem.value.functionInfo
  
  switch (lang) {
    case 'java':
      return `
/**
 * ${problem.value.title}
 */
public class Solution {
    ${signature} {
        // 请在此处编写你的代码
        
    }
}
`.trim()
    
    case 'python':
      return `
# ${problem.value.title}
def ${signature.split(' ')[1].split('(')[0]}(${params.map((p) => p.name).join(', ')}):
    # 请在此处编写你的代码
    pass
`.trim()
    
    case 'cpp':
      return `
// ${problem.value.title}
#include <vector>
#include <string>
using namespace std;

class Solution {
public:
    ${signature} {
        // 请在此处编写你的代码
        
    }
};
`.trim()
    
    case 'javascript':
      return `
/**
 * ${problem.value.title}
 */
function ${signature.split(' ')[1].split('(')[0]}(${params.map((p) => p.name).join(', ')}) {
    // 请在此处编写你的代码
    
}
`.trim()
    
    default:
      return ''
  }
}

// 运行代码
const runCode = async () => {
  if (!problem.value?.id) {
    ElMessage.error('题目信息不完整')
    return
  }
  
  const code = getEditorValue()
  if (!code.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }
  
  isSubmitting.value = true
  try {
    // 实际开发中需要调用后端API运行代码
    // const res = await problemApi.runCode({
    //   problemId: problem.value.id,
    //   language: selectedLanguage.value,
    //   code
    // })
    
    // 模拟运行结果
    setTimeout(() => {
      runResult.value = {
        status: Math.random() > 0.3 ? 'ACCEPTED' : 'WRONG_ANSWER',
        message: Math.random() > 0.3 ? '所有测试用例通过!' : '测试用例未通过',
        executeTime: Math.floor(Math.random() * 100),
        executeMemory: Math.floor(Math.random() * 20),
        testCaseResults: [
          {
            passed: true,
            input: '[1, 2, 3]',
            expectedOutput: '6',
            actualOutput: '6'
          },
          {
            passed: Math.random() > 0.3,
            input: '[-1, 5, 3, 2]',
            expectedOutput: '9',
            actualOutput: Math.random() > 0.3 ? '9' : '8'
          }
        ]
      }
      isSubmitting.value = false
    }, 1000)
    
  } catch (error) {
    console.error('运行代码异常:', error)
    ElMessage.error('运行代码失败，请稍后重试')
    isSubmitting.value = false
  }
}

// 提交代码
const submitCode = async () => {
  if (!problem.value?.id) {
    ElMessage.error('题目信息不完整')
    return
  }
  
  const code = getEditorValue()
  if (!code.trim()) {
    ElMessage.warning('请先编写代码')
    return
  }
  
  isSubmitting.value = true
  try {
    const res = await submissionApi.submitProgram({
      problemId: problem.value.id,
      language: selectedLanguage.value,
      code
    })
    
    if (res.code === 0 && res.data) {
      ElMessage.success('提交成功')
      // 跳转到提交详情页面
      router.push(`/submissions/${res.data.id}`)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交代码异常:', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

// 重置编辑器内容
const resetEditor = () => {
  ElMessageBox.confirm('确定要重置代码编辑器吗？当前的代码将会丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    setEditorValue(getTemplateCode(selectedLanguage.value))
    ElMessage.success('代码已重置')
  }).catch(() => {
    // 用户取消操作
  })
}

// 查看提交详情
const viewSubmission = (id: number) => {
  router.push(`/submissions/${id}`)
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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

// 获取状态颜色类型
const getStatusType = (status?: string): string => {
  if (!status) return 'info'
  
  const map: Record<string, string> = {
    'ACCEPTED': 'success',
    'WRONG_ANSWER': 'danger',
    'COMPILE_ERROR': 'warning',
    'RUNTIME_ERROR': 'danger',
    'TIME_LIMIT_EXCEEDED': 'danger',
    'MEMORY_LIMIT_EXCEEDED': 'danger',
    'PENDING': 'info'
  }
  
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status?: string): string => {
  if (!status) return '未知'
  
  const map: Record<string, string> = {
    'ACCEPTED': '通过',
    'WRONG_ANSWER': '答案错误',
    'COMPILE_ERROR': '编译错误',
    'RUNTIME_ERROR': '运行时错误',
    'TIME_LIMIT_EXCEEDED': '超时',
    'MEMORY_LIMIT_EXCEEDED': '内存超限',
    'PENDING': '评测中'
  }
  
  return map[status] || '未知'
}

// 获取相关题目
const loadRelatedProblems = async () => {
  if (!problem.value?.id) return
  
  try {
    // 这里假设有一个API可以获取相关题目
    // const res = await problemApi.getRelatedProblems(problem.value.id)
    // if (res.code === 0 && res.data) {
    //   relatedProblems.value = res.data
    // }
    
    // 暂时使用模拟数据
    relatedProblems.value = [
      { id: 101, title: '二叉树的最大深度', difficulty: '简单', type: 'PROGRAM' },
      { id: 102, title: '合并两个有序链表', difficulty: '简单', type: 'PROGRAM' },
      { id: 103, title: '最长回文子串', difficulty: '中等', type: 'PROGRAM' }
    ]
  } catch (error) {
    console.error('加载相关题目异常:', error)
  }
}

// 获取提交历史
const loadSubmissionHistory = async () => {
  if (!problem.value?.id) return
  
  try {
    // 实际API调用
    // const res = await submissionApi.getProblemSubmissionList(problem.value.id)
    // if (res.code === 0 && res.data) {
    //   submissionHistory.value = res.data.records
    // }
    
    // 模拟数据
    submissionHistory.value = [
      { id: 3001, problemId: problem.value.id, type: 'PROGRAM', language: 'Java', createTime: '2023-06-18T10:30:00', status: 'ACCEPTED', executeTime: 10, executeMemory: 10 },
      { id: 3002, problemId: problem.value.id, type: 'PROGRAM', language: 'Python', createTime: '2023-06-17T16:20:00', status: 'WRONG_ANSWER', executeTime: 15, executeMemory: 8 },
      { id: 3003, problemId: problem.value.id, type: 'PROGRAM', language: 'Java', createTime: '2023-06-16T14:10:00', status: 'COMPILE_ERROR', executeTime: undefined, executeMemory: undefined }
    ]
  } catch (error) {
    console.error('加载提交历史异常:', error)
  }
}

// Markdown 渲染器
const md = new MarkdownIt()
const renderMarkdown = (text: string) => {
  return md.render(text)
}

// 加载题目数据
const loadProblem = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('题目ID不能为空')
    router.push('/problems')
    return
  }
  
  try {
    const res = await problemApi.getProblemDetail(Number(id))
    
    if (res.code === 0 && res.data) {
      problem.value = res.data
      
      // 题目加载完成后，初始化编辑器（需要函数信息生成模板代码）
      nextTick(() => {
        initEditor()
      })
      
      // 成功加载题目后，加载相关数据
      loadRelatedProblems()
      loadSubmissionHistory()
      
    } else {
      ElMessage.error(res.message || '获取题目详情失败')
      router.push('/problems')
    }
  } catch (error) {
    console.error('加载题目详情异常:', error)
    ElMessage.error('获取题目详情失败，请稍后重试')
    router.push('/problems')
  }
}

// 页面加载时获取题目详情
onMounted(() => {
  loadProblem()
})

// 组件销毁前清理编辑器实例
onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
})
</script>

<style scoped>
.program-problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-card, .code-editor-card, .related-problems-card, .submission-history-card {
  margin-bottom: 20px;
  border-radius: 8px;
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
}

.function-info {
  margin: 20px 0;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.function-signature {
  margin: 15px 0;
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow-x: auto;
}

.function-signature pre {
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
}

.params-list {
  padding-left: 20px;
}

.params-list li {
  margin-bottom: 8px;
}

.return-info {
  margin-top: 10px;
}

.constraints {
  margin: 20px 0;
  padding: 15px;
  background-color: #fff7e6;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.test-cases {
  margin: 20px 0;
}

.test-case {
  margin-top: 10px;
}

.test-case-input, .test-case-output, .test-case-explanation {
  margin-bottom: 15px;
}

.test-case-input pre, .test-case-output pre {
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', Courier, monospace;
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

.editor-options {
  display: flex;
  gap: 10px;
}

.monaco-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.run-result {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.result-details, .execution-stats {
  margin-top: 15px;
}

.execution-stats p {
  margin: 5px 0;
  font-family: 'Courier New', Courier, monospace;
}

/* 固定高度的表格 */
.el-table {
  --el-table-header-bg-color: #f5f7fa;
}

.el-table :deep(.el-table__body-wrapper) {
  max-height: 300px;
  overflow-y: auto;
}

pre {
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>