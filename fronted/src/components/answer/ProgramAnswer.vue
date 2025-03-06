<template>
  <div class="program-answer">
    <!-- 语言选择 -->
    <div class="language-select">
      <el-select v-model="selectedLanguage" placeholder="选择编程语言">
        <el-option
          v-for="lang in supportedLanguages"
          :key="lang.value"
          :label="lang.label"
          :value="lang.value"
        />
      </el-select>
    </div>

    <!-- 代码编辑器 -->
    <div class="editor-container">
      <MonacoEditor
        v-model:value="code"
        :language="selectedLanguage"
        :theme="isDarkMode ? 'vs-dark' : 'vs'"
        :options="editorOptions"
        class="code-editor"
      />
    </div>

    <!-- 测试用例 -->
    <div class="test-cases" v-if="problem.samples">
      <el-collapse v-model="activeNames">
        <el-collapse-item v-for="(sample, index) in problem.samples" :key="index" :name="index">
          <template #title>
            <span class="sample-title">示例 {{ index + 1 }}</span>
          </template>
          <div class="sample-content">
            <div class="sample-input">
              <div class="sample-label">输入：</div>
              <pre>{{ sample.input }}</pre>
            </div>
            <div class="sample-output">
              <div class="sample-label">输出：</div>
              <pre>{{ sample.output }}</pre>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleTest" :loading="isRunning">
        运行代码
      </el-button>
      <el-button type="success" @click="handleSubmit" :loading="isSubmitting" :disabled="!code">
        提交代码
      </el-button>
    </div>

    <!-- 运行结果 -->
    <div v-if="testResult" class="test-result">
      <el-alert
        :title="testResult.status"
        :type="testResult.success ? 'success' : 'error'"
        :description="testResult.message"
        show-icon
      />
      <div v-if="testResult.output" class="result-output">
        <pre>{{ testResult.output }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTheme } from '../../composables/useTheme'
import type { Problem } from '../../api/problem'
import MonacoEditor from '@/components/MonacoEditor.vue'

const props = defineProps<{
  problem: Problem
}>()

const emit = defineEmits<{
  (e: 'submit', answer: { language: string; code: string }): void
}>()

// 主题
const { isDarkMode } = useTheme()

// 编程语言选项
const supportedLanguages = [
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Python', value: 'python' },
  { label: 'Java', value: 'java' },
  { label: 'C++', value: 'cpp' },
  { label: 'Go', value: 'go' }
]

// 编辑器配置
const editorOptions = {
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  fontSize: 14,
  lineNumbers: 'on',
  roundedSelection: false,
  scrollBars: 'vertical',
  automaticLayout: true
}

// 响应式数据
const selectedLanguage = ref(supportedLanguages[0].value)
const code = ref('')
const activeNames = ref([0])
const isRunning = ref(false)
const isSubmitting = ref(false)
const testResult = ref<{
  status: string
  success: boolean
  message: string
  output?: string
} | null>(null)

// 运行代码
const handleTest = async () => {
  if (!code.value) return
  
  isRunning.value = true
  testResult.value = null
  
  try {
    // TODO: 实现代码运行逻辑
    testResult.value = {
      status: '运行成功',
      success: true,
      message: '测试用例通过',
      output: '输出结果...'
    }
  } catch (error) {
    testResult.value = {
      status: '运行失败',
      success: false,
      message: error instanceof Error ? error.message : '未知错误'
    }
  } finally {
    isRunning.value = false
  }
}

// 提交代码
const handleSubmit = async () => {
  if (!code.value) return
  
  isSubmitting.value = true
  
  try {
    emit('submit', {
      language: selectedLanguage.value,
      code: code.value
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.program-answer {
  padding: 20px;
}

.language-select {
  margin-bottom: 16px;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.code-editor {
  height: 400px;
}

.test-cases {
  margin: 16px 0;
}

.sample-title {
  font-size: 14px;
  font-weight: bold;
}

.sample-content {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.sample-input,
.sample-output {
  margin-bottom: 8px;
}

.sample-label {
  font-weight: bold;
  margin-bottom: 4px;
}

pre {
  margin: 0;
  padding: 8px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  font-family: monospace;
  white-space: pre-wrap;
}

.action-buttons {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.test-result {
  margin-top: 16px;
}

.result-output {
  margin-top: 12px;
}
</style> 