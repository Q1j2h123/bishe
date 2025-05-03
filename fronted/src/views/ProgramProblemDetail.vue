<template>
  <div class="program-problem-container">
    <div class="problem-layout">
      <!-- 左侧：题目详情区域 -->
      <div class="problem-info-panel">
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
          
          <!-- 运行限制信息 -->
          <div class="execution-limits" v-if="problem">
            <h3>执行限制</h3>
            <div class="limits-info">
              <el-tag type="info">时间限制: {{ problem.timeLimit || 1000 }} ms</el-tag>
              <el-tag type="info" class="ml-2">内存限制: {{ problem.memoryLimit || 256 }} MB</el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：代码编辑器区域 -->
      <div class="code-editor-panel">
        <el-card class="editor-card">
          <template #header>
            <div class="editor-header">
              <h3>代码编辑器</h3>
              <div class="language-selector">
                <span>选择语言:</span>
                <el-select 
                  v-model="selectedLanguage" 
                  placeholder="请选择编程语言"
                  @change="updateCodeTemplate"
                  size="small"
                >
                  <el-option
                    v-for="lang in availableLanguages"
                    :key="lang.value"
                    :label="lang.label"
                    :value="lang.value"
                  />
                </el-select>
              </div>
            </div>
          </template>
          
          <!-- 代码编辑器 -->
          <div class="editor-container">
            <md-editor
              v-model="code"
              :language="selectedLanguage"
              preview-theme="vuepress"
              code-theme="atom-one-dark"
              show-code-row-number
              :tab-width="2"
              toolbarsExclude="[
                'image', 'link', 'table', 'katex', 'html',
                'title', 'bold', 'italic', 'strikethrough', 
                'sub', 'sup', 'quote', 'unordered-list', 
                'ordered-list', 'toc', 'prettier', 'code-block',
                'clear', 'preview', 'fullscreen', 'github', 'save'
              ]"
              :toolbars="[
                'undo', 'redo', 'format'
              ]"
              :preview="false"
              :codeStyleReverse="false"
              :autoHeight="true"
              :autoFocus="true"
              @onSave="runCode"
              @onCtrlEnter="submitCode"
              placeholder="请在此处编写代码..."
              :style="{ fontSize: '14px' }"
              :theme="editorTheme"
            />
          </div>
          
          <!-- 输入格式提示 -->
          <div class="input-format-tip" v-if="selectedLanguage === 'java'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
            >
              <b>输入格式提示:</b> 测试用例输入使用<b>空格分隔</b>，例如"5 1 4 2 8"。请确保代码使用<code>split(" ")</code>而不是<code>split(",")</code>来处理输入。
            </el-alert>
          </div>
          
          <!-- 操作按钮 -->
          <div class="editor-actions">
            <el-button type="primary" @click="runCode">运行代码</el-button>
            <el-button type="success" @click="submitCode">提交代码</el-button>
            <el-button @click="resetCode">重置代码</el-button>
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
        </el-card>
      </div>
    </div>

    <!-- 新增标准答案和报错信息展示区域 -->
    <div class="result-info" v-if="resultInfo.show">
      <h3>结果信息</h3>
      <el-alert
        :type="resultInfo.type"
        :title="'评测结果'"
        :closable="false"
        show-icon
      >
        <div class="result-content markdown-body" v-html="renderMarkdown(resultInfo.content)"></div>
        <div class="result-actions" v-if="standardSolution && !showStandardSolution">
          <el-button size="small" type="primary" @click="askToShowStandardSolution">查看标准答案</el-button>
        </div>
      </el-alert>
    </div>

    <!-- 新增标准答案展示区域 -->
    <div class="standard-solution" v-if="showStandardSolution">
      <h3>标准答案参考</h3>
      <el-card>
        <pre class="solution-code">{{ standardSolution }}</pre>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineProps, watchEffect, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO } from '@/api/problem'
import { submissionApi, type ProgramSubmitRequest } from '@/api/submission'
import { codeRunApi, type CodeRunRequest } from '@/api/run-code'
import { problemStatusApi } from '@/api/problem-status'
// @ts-ignore
import MarkdownIt from 'markdown-it'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

// 调试模式 - 设置为false，隐藏调试面板
const debug = ref(false)

// 编辑器主题
const editorTheme = ref('dark')

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

// 新增在顶部变量定义区域
const standardSolution = ref<string>('');
const showStandardSolution = ref<boolean>(false);
const resultInfo = ref<{
  show: boolean;
  content: string;
  type: 'success' | 'error' | 'warning';
}>({
  show: false,
  content: '',
  type: 'warning'
});

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
      signature = `class Solution {
    public ${returnType} ${funcName}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {
        // 请实现此函数
        return ${returnType === 'int' ? '0' : returnType === 'boolean' ? 'false' : returnType === 'String' ? '""' : 'null'};
    }
}`
      break
    case 'python':
      signature = `def ${funcName}(${params.map((_, index) => `param${index + 1}`).join(', ')}):
    # 请实现此函数
    pass`
      break
    case 'c':
      signature = `${returnType} ${funcName}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {
    // 请实现C语言函数
    ${returnType !== 'void' ? (returnType === 'int' ? 'return 0;' : returnType === 'char*' || returnType.includes('*') ? 'return NULL;' : 'return 0;') : ''}
}`
      break
    case 'cpp':
      signature = `${returnType} ${funcName}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {
    // 请实现此函数
    ${returnType !== 'void' ? (returnType === 'int' ? 'return 0;' : returnType === 'bool' ? 'return false;' : returnType === 'string' || returnType.includes('string') ? 'return "";' : returnType.includes('vector') || returnType.includes('*') ? 'return {};' : 'return 0;') : ''}
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

// 辅助函数 - 修正代码中的输入分隔符
const correctInputDelimiter = (sourceCode: string, language: string): string => {
  if (language !== 'java' || !sourceCode) {
    return sourceCode;
  }
  
  // 检查是否已经包含正确的分隔符（空格分隔）
  if (sourceCode.includes("split(\" \")") || sourceCode.includes("split(\"\\\\s+\")")) {
    return sourceCode;
  }
  
  // 替换可能的错误分隔符（逗号分隔）为空格分隔
  let correctedCode = sourceCode.replace(
    /String\[\] input = scanner\.nextLine\(\)\.split\(","\);/g,
    'String[] input = scanner.nextLine().split(" ");'
  ).replace(
    /String\[\] input = scanner\.nextLine\(\)\.split\("\\\\,"\);/g,
    'String[] input = scanner.nextLine().split(" ");'
  );
  
  // 通用替换：捕获更多可能的分隔模式
  if (correctedCode === sourceCode) {
    correctedCode = sourceCode.replace(
      /\.split\(\s*"[,;]"\s*\)/g,
      '.split(" ")'
    );
  }
  
  // 检查是否包含main方法，如果不包含，添加带有输入处理的main方法
  if (!correctedCode.includes("public static void main(String[] args)")) {
    // 找到类的结束括号
    const classEndIndex = correctedCode.lastIndexOf('}');
    if (classEndIndex !== -1) {
      const mainMethod = ``;
      correctedCode = correctedCode.substring(0, classEndIndex) + mainMethod + correctedCode.substring(classEndIndex);
    }
  }
  
  return correctedCode;
};

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
  
  // 不再加载默认模板，而是设置为空字符串
  code.value = '';
  
  console.log(`已将${selectedLanguage.value}语言编辑器设置为空白`);
};

// 重置代码
const resetCode = () => {
  // 重置为空白
  code.value = '';
  
  ElMessage.info('已重置代码为空白');
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
  
  try {
    ElMessage.info('正在运行代码...');
    
    // 修正代码中的输入分隔符
    let codeToRun = correctInputDelimiter(code.value, selectedLanguage.value);
    
    // 如果进行了替换，通知用户并更新编辑器
    if (codeToRun !== code.value) {
      console.log('代码已自动修正以适应空格分隔的输入');
      code.value = codeToRun;
    }
    
    // 准备运行请求数据
    const runData: CodeRunRequest = {
      problemId: problem.value.id,
      language: selectedLanguage.value,
      code: codeToRun
    };
    
    console.log('运行代码数据:', runData);
    
    // 调用运行代码API
    const res = await codeRunApi.runCode(runData);
    
    if (res.code === 0 && res.data) {
      const result = res.data;
      
      runResults.value = {
        show: true,
        success: result.success,
        executionTime: result.executionTime,
        memoryUsage: result.memoryUsage,
        output: result.output,
        errorMessage: result.errorMessage
      };
      
      if (result.success) {
        ElMessage.success('代码运行成功');
      } else {
        ElMessage.error(result.errorMessage || '代码运行失败');
      }
    } else {
      runResults.value = {
        show: true,
        success: false,
        errorMessage: res.message || '系统错误，请稍后重试'
      };
      
      ElMessage.error(res.message || '运行失败，请稍后重试');
    }
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

// 处理轮询结果
const handlePollResult = async (result: any) => {
  try {
    // 获取服务器返回的状态
    const serverStatus = result.status;
    console.log('服务器返回的评测状态:', serverStatus);
    
    // 如果状态是ACCEPTED，设置为已解决，否则为已尝试
    const newStatus = serverStatus === 'ACCEPTED' ? 'SOLVED' : 'ATTEMPTED';
    
    // 先清除所有可能的缓存
    const cacheKeys = [
      'userProblemStatuses',
      'statusCacheTime',
      'problem_status_cache',
      'problem_list_cache',
      'currentProblemPage'
    ];
    
    for (const key of cacheKeys) {
      try {
        localStorage.removeItem(key);
        console.log(`清除缓存: ${key}`);
      } catch (e) {
        console.error(`清除缓存 ${key} 失败:`, e);
      }
    }
    
    // 非PENDING和JUDGING状态时更新状态
    if (serverStatus !== 'PENDING' && serverStatus !== 'JUDGING') {
      if (problem.value && problem.value.id) {
        const problemId = problem.value.id as number;
        
        // 直接调用强制更新API
        console.log(`通过API强制更新题目 ${problemId} 状态为 ${newStatus}`);
        try {
          const response = await problemStatusApi.forceUpdateStatus(problemId, newStatus);
          
          console.log('状态强制更新结果:', response);
          
          if (response.code === 0 && response.data) {
            console.log('状态更新成功');
          } else {
            console.warn('状态更新通过API失败，尝试使用本地缓存更新');
            
            // 尝试更新本地缓存
            const updateSuccess = updateLocalStatusCache(problemId, newStatus);
            console.log('本地缓存更新结果:', updateSuccess ? '成功' : '失败');
          }
        } catch (e) {
          console.error('通过API更新状态失败:', e);
          
          // 如果API调用失败，尝试更新本地缓存
          try {
            const updateSuccess = updateLocalStatusCache(problemId, newStatus);
            console.log('备用本地缓存更新结果:', updateSuccess ? '成功' : '失败');
          } catch (cacheError) {
            console.error('本地缓存更新也失败:', cacheError);
          }
        }
      }
    }

    // 获取详细提交信息，包括标准答案
    submissionApi.getProgramSubmissionDetail(result.id).then(detailRes => {
      if (detailRes.code === 0 && detailRes.data) {
        const detail = detailRes.data;
        
        // 获取标准答案并显示
        if (detail.standardSolution && selectedLanguage.value) {
          standardSolution.value = detail.standardSolution[selectedLanguage.value] || '';
          // 不自动显示标准答案
          showStandardSolution.value = false;
        }
        
        // 设置结果信息
        const langDisplay = languageDisplayNames[result.language] || result.language;
        let resultContent = '';
        let resultType: 'success' | 'error' | 'warning' = 'warning';
        
        if (serverStatus === 'ACCEPTED') {
          resultType = 'success';
          resultContent = `恭喜，您的代码通过了所有测试用例！\n` +
            `编程语言: ${langDisplay}\n` +
            `执行时间: ${result.executeTime || 0} ms\n` +
            `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB`;
        } else if (serverStatus === 'WRONG_ANSWER') {
          resultType = 'error';
          resultContent = `很遗憾，您的代码未通过所有测试用例。\n` +
            `编程语言: ${langDisplay}\n` +
            `通过测试用例：${detail.passedTestCases || 0}/${detail.totalTestCases || 0}\n` +
            `执行时间: ${result.executeTime || 0} ms\n` +
            `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB\n` +
            `${detail.testcaseResults || ''}`;
        } else if (serverStatus === 'COMPILE_ERROR') {
          resultType = 'warning';
          resultContent = `编译错误:\n` +
            `编程语言: ${langDisplay}\n` +
            `${detail.errorMessage || '未知编译错误'}`;
        } else if (serverStatus === 'RUNTIME_ERROR') {
          resultType = 'warning';
          resultContent = `运行时错误:\n` +
            `编程语言: ${langDisplay}\n` +
            `执行时间: ${result.executeTime || 0} ms\n` +
            `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB\n` +
            `${detail.errorMessage || '未知运行时错误'}`;
        } else if (serverStatus === 'TIME_LIMIT_EXCEEDED') {
          resultType = 'warning';
          resultContent = `您的代码执行超时，请优化算法提高效率。\n` +
            `编程语言: ${langDisplay}\n` +
            `时间限制: ${problem.value?.timeLimit || 1000} ms\n` +
            `实际耗时: ${result.executeTime || 0} ms\n` +
            `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB`;
        } else if (serverStatus === 'MEMORY_LIMIT_EXCEEDED') {
          resultType = 'warning';
          resultContent = `您的代码内存使用超出限制，请优化算法降低内存占用。\n` +
            `编程语言: ${langDisplay}\n` +
            `内存限制: ${problem.value?.memoryLimit || 256} MB\n` +
            `实际使用: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB\n` +
            `执行时间: ${result.executeTime || 0} ms`;
        } else {
          resultType = 'warning';
          resultContent = `评测结果: ${serverStatus || '未知状态'}\n` +
            `编程语言: ${langDisplay}\n` +
            `执行时间: ${result.executeTime || 0} ms\n` +
            `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB\n` +
            `${result.errorMessage || ''}`;
        }
        
        // 设置结果信息
        resultInfo.value = {
          show: true,
          content: resultContent,
          type: resultType
        };
      }
    });

    // 根据评测结果更新UI (保留原有的UI更新逻辑)
    if (serverStatus === 'ACCEPTED') {
      // 获取语言显示名称
      const langDisplay = languageDisplayNames[result.language] || result.language;
      
      ElMessageBox.confirm(
        `恭喜，您的代码通过了所有测试用例！\n` +
        `编程语言: ${langDisplay}\n` +
        `执行时间: ${result.executeTime || 0} ms\n` +
        `内存消耗: ${(result.memoryUsage ? result.memoryUsage / 1024 : 0).toFixed(2)} MB`, 
        '提交成功', 
        {
          confirmButtonText: '返回题目列表',
          cancelButtonText: '继续编写',
          type: 'success'
        }
      ).then(() => {
        returnToList();
      }).catch(() => {
        // 用户选择继续编写，询问是否查看标准答案
        // if (standardSolution.value) {
        //   ElMessageBox.confirm(
        //     '是否查看此题的标准答案？',
        //     '查看标准答案',
        //     {
        //       confirmButtonText: '查看',
        //       cancelButtonText: '不查看',
        //       type: 'info'
        //     }
        //   ).then(() => {
        //     // 用户选择查看标准答案
        //     showStandardSolution.value = true;
        //   }).catch(() => {
        //     // 用户选择不查看标准答案
        //     showStandardSolution.value = false;
        //   });
        // }
        
        // 清除缓存，确保状态一致性
        for (const key of cacheKeys) {
          try {
            localStorage.removeItem(key);
          } catch (e) {
            console.error(`清除缓存 ${key} 失败:`, e);
          }
        }
      });
    } else if (serverStatus === 'WRONG_ANSWER') {
      // 操作已经在上面的API调用中处理了，无需重复
    } else if (serverStatus === 'COMPILE_ERROR') {
      // 操作已经在上面的API调用中处理了，无需重复
    } else if (serverStatus === 'RUNTIME_ERROR') {
      // 操作已经在上面的API调用中处理了，无需重复
    } else if (serverStatus === 'TIME_LIMIT_EXCEEDED') {
      // 操作已经在上面的API调用中处理了，无需重复
    } else if (serverStatus === 'MEMORY_LIMIT_EXCEEDED') {
      // 操作已经在上面的API调用中处理了，无需重复
    } else {
      // 操作已经在上面的API调用中处理了，无需重复
    }
  } catch (error) {
    console.error('处理评测结果异常:', error);
    ElMessage.error('处理评测结果时发生错误，请刷新页面重试');
  }
}

// 添加返回列表函数
const returnToList = async () => {
  console.log('准备返回题目列表');
  
  // 清除所有缓存
  try {
    // 同时移除多个缓存项
    const cacheKeys = [
      'userProblemStatuses',
      'statusCacheTime',
      'problem_status_cache',
      'problem_list_cache',
      'currentProblemPage'
    ];
    
    for (const key of cacheKeys) {
      localStorage.removeItem(key);
      console.log(`已清除缓存: ${key}`);
    }
    
    console.log('所有缓存已清除');
  } catch (e) {
    console.error('清除缓存失败:', e);
  }
  
  console.log('开始导航到题目列表');
  
  // 导航到题目列表
  setTimeout(() => {
    // 使用query参数强制刷新列表页
    router.push({
      path: '/problems',
      query: { 
        t: Date.now().toString(), // 添加时间戳强制刷新
        forceRefresh: 'true',     // 添加强制刷新标记
        clearedCache: 'true'      // 额外标记，表示缓存已清除
      }
    });
  }, 200);
}

// 添加新方法：更新本地状态缓存
const updateLocalStatusCache = (problemId: number, status?: string) => {
  try {
    // 如果未提供状态，默认为SOLVED
    const newStatus = status || 'SOLVED';
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
    
    // 修正代码中的输入分隔符
    let codeToSubmit = correctInputDelimiter(code.value, selectedLanguage.value);
    
    // 如果进行了替换，通知用户
    if (codeToSubmit !== code.value) {
      console.log('代码已自动修正以适应空格分隔的输入');
      // 更新编辑器中的代码
      code.value = codeToSubmit;
    }
    
    // 准备提交请求
    const submitData: ProgramSubmitRequest = {
      problemId: problem.value.id,
      language: selectedLanguage.value,
      code: codeToSubmit
    };
    
    console.log('提交数据:', submitData);
    
    // 调用提交API
    const res = await submissionApi.submitProgram(submitData);
    
    if (res.code === 0) {
      // 获取提交ID
      const submissionId = res.data;
      
      if (submissionId) {
        // 存储当前提交的ID
        latestSubmissionId.value = submissionId;
        
        // 显示提交成功消息
        ElMessage.success('代码提交成功，正在评测...');
        
        // 开始轮询结果
        setTimeout(() => {
          pollSubmissionResult(submissionId);
        }, 1000);
      } else {
        ElMessage.warning('提交成功，但未返回评测ID');
      }
    } else {
      ElMessage.error(res.message || '提交失败');
    }
  } catch (error) {
    console.error('提交代码出错:', error);
    ElMessage.error('提交失败，请稍后重试');
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
      ElMessage.error(res.message || '获取评测结果失败')
    }
  } catch (error) {
    console.error('轮询提交结果出错:', error)
    ElMessage.error('获取评测结果失败，请稍后重试')
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
        // 将默认代码设置为空字符串，而不是加载模板
        code.value = '';
      } else {
        // 不再生成函数签名
        code.value = '';
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

// 监听语言选择变化，更新代码模板和编辑器语言
watchEffect(() => {
  if (selectedLanguage.value) {
    updateCodeTemplate();
  }
});

// 页面加载时不再主动调用loadProblem，由watchEffect触发
onMounted(() => {
  // watchEffect会自动触发加载
  console.log('ProgramProblemDetail组件已挂载，路由参数:', route.params)
})

// 添加一个询问是否查看标准答案的方法
const askToShowStandardSolution = () => {
  if (standardSolution.value) {
    // 直接显示标准答案，不再显示确认对话框
    showStandardSolution.value = true;
  } else {
    ElMessage.info('当前没有可用的标准答案');
  }
};
</script>

<style scoped>
.program-problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-layout {
  display: flex;
  gap: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.problem-info-panel {
  flex: 1;
  min-width: 0;
}

.code-editor-panel {
  flex: 1;
  min-width: 0;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

.problem-card, .editor-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 100%;
  overflow: auto;
}

.editor-card {
  display: flex;
  flex-direction: column;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
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

.language-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.editor-container {
  margin: 15px 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background-color: #1e1e1e;
  flex-grow: 1;
  height: 500px;
  min-height: 500px;
  position: relative;
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

.loading-content {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.input-format-tip {
  margin: 10px 0 20px;
}

.input-format-tip code {
  background-color: #f0f9eb;
  padding: 2px 5px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: #409EFF;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .problem-layout {
    flex-direction: column;
  }
  
  .code-editor-panel {
    position: relative;
    top: 0;
    max-height: none;
  }
}

.result-info {
  margin-top: 20px;
}

.result-info h3 {
  margin-bottom: 15px;
  color: #303133;
}

.result-content {
  white-space: normal;
  word-wrap: break-word;
}

.result-content :deep(h1), 
.result-content :deep(h2), 
.result-content :deep(h3),
.result-content :deep(h4), 
.result-content :deep(h5), 
.result-content :deep(h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
  color: #303133;
}

.result-content :deep(code) {
  background-color: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
  color: #333;
}

.result-content :deep(pre) {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  overflow-x: auto;
}

.result-content :deep(p) {
  margin: 0.5em 0;
}

.result-content :deep(ul), .result-content :deep(ol) {
  padding-left: 2em;
  margin: 0.5em 0;
}

.standard-solution {
  margin-top: 20px;
}

.standard-solution h3 {
  margin-bottom: 15px;
  color: #303133;
}

.solution-code {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 添加Markdown编辑器样式 */
:deep(.md-editor) {
  height: 100%;
  border: none;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
}

:deep(.md-editor-toolbar-wrapper) {
  background-color: #2a2a2a;
  border-bottom: 1px solid #3e3e3e;
}

:deep(.md-editor-content) {
  height: calc(100% - 36px);
}

:deep(.md-editor-content .cm-editor) {
  background-color: #1e1e1e;
  color: #d4d4d4;
}

:deep(.md-editor-content .cm-editor .cm-activeLine) {
  background-color: #2c323b;
}

:deep(.md-editor-content .cm-editor .cm-gutters) {
  background-color: #1e1e1e;
  border-right: 1px solid #3e3e3e;
}

:deep(.md-editor-content .cm-editor .cm-content) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  tab-size: 2;
}

:deep(.md-editor-content .cm-editor .cm-line) {
  padding-left: 8px;
}

:deep(.md-editor-content .cm-editor .cm-selectionBackground) {
  background-color: #264f78;
}

:deep(.md-editor-content .cm-editor .cm-cursor) {
  border-left: 2px solid #aeafad;
}

:deep(.md-editor-dark) {
  --md-bk-color: #1e1e1e;
  --md-border-color: #3e3e3e;
}

:deep(.md-editor-toolbar) {
  padding: 2px 4px;
}

:deep(.md-toolbar-item) {
  margin: 0 2px;
}

.result-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>