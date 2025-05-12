<template>
  <div class="program-form">
    <el-divider>编程题信息</el-divider>
    
    <!-- 函数相关信息 -->
    <el-form-item label="函数名称" prop="functionName" required>
      <el-input
        v-model="modelValue.functionName"
        placeholder="请输入函数名称，如 twoSum"
      />
    </el-form-item>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="参数类型" prop="paramTypes" required>
          <el-select 
            v-model="modelValue.paramTypes" 
            multiple 
            placeholder="请选择参数类型"
            style="width: 100%"
          >
            <el-option label="int" value="int" />
            <el-option label="int[]" value="int[]" />
            <el-option label="String" value="String" />
            <el-option label="String[]" value="String[]" />
            <el-option label="double" value="double" />
            <el-option label="boolean" value="boolean" />
            <el-option label="char" value="char" />
            <el-option label="long" value="long" />
            <el-option label="ListNode" value="ListNode" />
            <el-option label="TreeNode" value="TreeNode" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="返回值类型" prop="returnType" required>
          <el-select 
            v-model="modelValue.returnType" 
            placeholder="请选择返回值类型"
            style="width: 100%"
          >
            <el-option label="int" value="int" />
            <el-option label="int[]" value="int[]" />
            <el-option label="String" value="String" />
            <el-option label="String[]" value="String[]" />
            <el-option label="double" value="double" />
            <el-option label="boolean" value="boolean" />
            <el-option label="char" value="char" />
            <el-option label="void" value="void" />
            <el-option label="long" value="long" />
            <el-option label="ListNode" value="ListNode" />
            <el-option label="TreeNode" value="TreeNode" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="时间限制(ms)" prop="timeLimit">
          <el-input-number
            v-model="modelValue.timeLimit"
            :min="100"
            :max="10000"
            :step="100"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="内存限制(MB)" prop="memoryLimit">
          <el-input-number
            v-model="modelValue.memoryLimit"
            :min="16"
            :max="1024"
            :step="16"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>
    
    <!-- 测试用例 -->
    <el-divider>测试用例</el-divider>
    
    <div class="test-cases-container">
      <div class="test-cases-header">
        <h3>测试用例</h3>
        <el-button type="primary" size="small" @click="addTestCase">
          添加测试用例
        </el-button>
      </div>
      
      <div 
        v-for="(testCase, index) in modelValue.testCases" 
        :key="index"
        class="test-case-item"
      >
        <div class="test-case-header">
          <span>测试用例 {{ index + 1 }}</span>
          <el-button 
            type="danger" 
            size="small"
            @click="removeTestCase(index)"
          >
            删除
          </el-button>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="'输入'" :prop="`testCases.${index}.input`">
              <el-input
                v-model="testCase.input"
                type="textarea"
                :rows="4"
                placeholder="请输入测试用例输入"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item :label="'期望输出'" :prop="`testCases.${index}.output`">
              <el-input
                v-model="testCase.output"
                type="textarea"
                :rows="4"
                placeholder="请输入测试用例期望输出"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 代码模板 -->
    <el-divider>代码模板</el-divider>
    
    <div class="code-templates-container">
      <div class="code-templates-header">
        <h3>代码模板</h3>
        <el-button type="primary" size="small" @click="addLanguageTemplate">
          添加语言模板
        </el-button>
      </div>
      
      <div v-if="!modelValue.templates || Object.keys(modelValue.templates).length === 0" class="empty-templates">
        请添加至少一种语言的代码模板
      </div>
      
      <div 
        v-for="lang in Object.keys(modelValue.templates || {})"
        :key="lang"
        class="template-item"
      >
        <div class="template-header">
          <span>{{ languageLabels[lang as string] || lang }}</span>
          <el-button 
            type="danger" 
            size="small"
            @click="removeLanguageTemplate(lang as string)"
          >
            删除
          </el-button>
        </div>
        
        <el-form-item :label="`${languageLabels[lang as string] || lang} 代码模板`" :prop="`templates.${lang}`">
          <el-input
            v-model="modelValue.templates[lang as string]"
            type="textarea"
            :rows="10"
            placeholder="请输入代码模板"
            :spellcheck="false"
            style="font-family: monospace;"
          />
        </el-form-item>
      </div>
    </div>
    
    <!-- 标准答案 -->
    <el-divider>标准答案</el-divider>
    
    <div class="standard-solutions-container">
      <div class="standard-solutions-header">
        <h3>标准答案</h3>
        <el-button type="primary" size="small" @click="addLanguageSolution">
          添加语言答案
        </el-button>
      </div>
      
      <div v-if="!modelValue.standardSolution || Object.keys(modelValue.standardSolution).length === 0" class="empty-solutions">
        请添加至少一种语言的标准答案
      </div>
      
      <div 
        v-for="([lang, code]) in Object.entries(modelValue.standardSolution || {})" 
        :key="lang"
        class="solution-item"
      >
        <div class="solution-header">
          <span>{{ languageLabels[lang as string] || lang }}</span>
          <el-button 
            type="danger" 
            size="small"
            @click="removeLanguageSolution(lang as string)"
          >
            删除
          </el-button>
        </div>
        
        <el-form-item :label="`${languageLabels[lang as string] || lang} 标准答案`" :prop="`standardSolution.${lang}`">
          <el-input
            v-model="modelValue.standardSolution[lang as string]"
            type="textarea"
            :rows="10"
            placeholder="请输入标准答案代码"
            :spellcheck="false"
            style="font-family: monospace;"
          />
        </el-form-item>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { reactive } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})
// 添加语言模板
const addLanguageTemplate = () => {
  // 显示选择语言的对话框
  ElMessageBox.prompt('请输入语言标识 (如 java, python, cpp, javascript, go)', '添加语言模板', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[a-z0-9_]+$/,
    inputErrorMessage: '语言标识只能包含小写字母、数字和下划线'
  }).then(({ value }) => {
    if (props.modelValue.templates?.[value]) {
      ElMessage.warning(`${value} 模板已存在`)
      return
    }
    
    // 使用Vue响应式API确保界面更新
    const newTemplates = {...(props.modelValue.templates || {})}
    newTemplates[value] = getDefaultTemplate(value, props.modelValue.functionName, props.modelValue.paramTypes, props.modelValue.returnType)
    
    // 替换整个对象以确保Vue检测到变化
    props.modelValue.templates = newTemplates
    
    console.log('添加了模板:', value, props.modelValue.templates)
    ElMessage.success(`添加 ${value} 模板成功`)
  }).catch(() => {
    // 用户取消
  })
}

// 添加语言标准答案
const addLanguageSolution = () => {
  // 显示选择语言的对话框
  ElMessageBox.prompt('请输入语言标识 (如 java, python, cpp, javascript, go)', '添加语言标准答案', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[a-z0-9_]+$/,
    inputErrorMessage: '语言标识只能包含小写字母、数字和下划线'
  }).then(({ value }) => {
    if (props.modelValue.standardSolution?.[value]) {
      ElMessage.warning(`${value} 标准答案已存在`)
      return
    }
    
    // 使用Vue响应式API确保界面更新
    const newSolution = {...(props.modelValue.standardSolution || {})}
    newSolution[value] = getDefaultTemplate(value, props.modelValue.functionName, props.modelValue.paramTypes, props.modelValue.returnType)
    
    // 替换整个对象以确保Vue检测到变化
    props.modelValue.standardSolution = newSolution
    
    console.log('添加了标准答案:', value, props.modelValue.standardSolution)
    ElMessage.success(`添加 ${value} 标准答案成功`)
  }).catch(() => {
    // 用户取消
  })
}
// 语言标签映射
const languageLabels: Record<string, string> = reactive({
  'java': 'Java',
  'python': 'Python',
  'cpp': 'C++',
  'javascript': 'JavaScript',
  'go': 'Go',
})

// 添加测试用例
const addTestCase = () => {
  if (!props.modelValue.testCases) {
    props.modelValue.testCases = []
  }
  props.modelValue.testCases.push({ input: '', output: '' })
}

// 删除测试用例
const removeTestCase = (index: number) => {
  if (props.modelValue.testCases.length > 1) {
    props.modelValue.testCases.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个测试用例')
  }
}

// 初始化模板和标准答案
if (!props.modelValue.templates) {
  props.modelValue.templates = {}
}

if (!props.modelValue.standardSolution) {
  props.modelValue.standardSolution = {}
}

// 确保至少有一个测试用例
if (!props.modelValue.testCases || props.modelValue.testCases.length === 0) {
  props.modelValue.testCases = [{ input: '', output: '' }]
}

// 设置默认的时间和内存限制
if (!props.modelValue.timeLimit) {
  props.modelValue.timeLimit = 1000
}

if (!props.modelValue.memoryLimit) {
  props.modelValue.memoryLimit = 256
}

// 删除语言模板
const removeLanguageTemplate = (lang: string) => {
  if (props.modelValue.templates) {
    const newTemplates = {...props.modelValue.templates}
    delete newTemplates[lang]
    props.modelValue.templates = newTemplates
    console.log('删除了模板:', lang, props.modelValue.templates)
  }
}

// 删除语言标准答案
const removeLanguageSolution = (lang: string) => {
  if (props.modelValue.standardSolution) {
    const newSolution = {...props.modelValue.standardSolution}
    delete newSolution[lang]
    props.modelValue.standardSolution = newSolution
    console.log('删除了标准答案:', lang, props.modelValue.standardSolution)
  }
}

// 根据语言和函数信息生成默认模板
const getDefaultTemplate = (language: string, functionName: string, paramTypes: string[], returnType: string) => {
  // 如果函数名称为空，使用默认函数名
  const fn = functionName || 'solution'
  
  // 如果参数类型为空，使用默认参数类型
  const params = paramTypes && paramTypes.length > 0 
    ? paramTypes 
    : ['int']
  
  // 如果返回类型为空，使用默认返回类型
  const ret = returnType || 'int'
  
  switch (language) {
    case 'java':
      return `public class Solution {\n    public ${ret} ${fn}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {\n        // 请实现此函数\n        return ${getDefaultReturnValue(ret)};\n    }\n}`
    case 'python':
      return `class Solution:\n    def ${fn}(self, ${params.map((_, index) => `param${index + 1}`).join(', ')}):\n        # 请实现此函数\n        return ${getDefaultReturnValue(ret)}`
    case 'cpp':
      return `class Solution {\npublic:\n    ${ret} ${fn}(${params.map((type, index) => `${type} param${index + 1}`).join(', ')}) {\n        // 请实现此函数\n        return ${getDefaultReturnValue(ret)};\n    }\n};`
    case 'javascript':
      return `/**\n * @param {${params.map(p => {
        if (p === 'int' || p === 'long' || p === 'double') return 'number';
        if (p === 'int[]') return 'number[]';
        if (p === 'String') return 'string';
        if (p === 'String[]') return 'string[]';
        if (p === 'boolean') return 'boolean';
        return p;
      }).join(', ')}}\n * @return {${
        ret === 'int' || ret === 'long' || ret === 'double' ? 'number' :
        ret === 'int[]' ? 'number[]' :
        ret === 'String' ? 'string' :
        ret === 'String[]' ? 'string[]' :
        ret === 'boolean' ? 'boolean' :
        ret
      }}\n */\nvar ${fn} = function(${params.map((_, index) => `param${index + 1}`).join(', ')}) {\n    // 请实现此函数\n    return ${getDefaultReturnValue(ret)};\n};`
    case 'go':
      return `func ${fn}(${params.map((type, index) => {
        let goType = type;
        if (type === 'int[]') goType = '[]int';
        if (type === 'String') goType = 'string';
        if (type === 'String[]') goType = '[]string';
        return `param${index + 1} ${goType}`;
      }).join(', ')}) ${
        ret === 'int[]' ? '[]int' :
        ret === 'String' ? 'string' :
        ret === 'String[]' ? '[]string' :
        ret
      } {\n    // 请实现此函数\n    return ${getDefaultReturnValue(ret)}\n}`
    default:
      return `// 请在此实现 ${fn} 函数\n// 参数: ${params.join(', ')}\n// 返回值: ${ret}`
  }
}

// 根据返回类型生成默认返回值
const getDefaultReturnValue = (returnType: string) => {
  switch (returnType) {
    case 'int':
    case 'long':
    case 'double':
      return '0'
    case 'int[]':
      return '[]'
    case 'String':
      return '""'
    case 'String[]':
      return '[]'
    case 'boolean':
      return 'false'
    case 'char':
      return "''"
    case 'void':
      return ''
    case 'ListNode':
    case 'TreeNode':
      return 'null'
    default:
      return 'null'
  }
}
</script>

<style scoped>
.test-cases-container, .code-templates-container, .standard-solutions-container {
  margin-top: 20px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px;
  background-color: #f8f9fa;
}

.test-cases-header, .code-templates-header, .standard-solutions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.test-case-item, .template-item, .solution-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e0e0e0;
}

.test-case-item:last-child, .template-item:last-child, .solution-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.test-case-header, .template-header, .solution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.empty-templates, .empty-solutions {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>