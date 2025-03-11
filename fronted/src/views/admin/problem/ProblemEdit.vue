<template>
  <div class="problem-edit">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑' + typeName : '添加' + typeName }}</h2>
      <el-button @click="router.go(-1)">返回</el-button>
    </div>
    
    <el-card>
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        v-loading="loading"
      >
        <!-- 基本信息 -->
        <el-divider>基本信息</el-divider>
        
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入题目标题" />
        </el-form-item>
        
        <el-form-item label="题目描述" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入题目描述，支持Markdown格式"
          />
        </el-form-item>
        
        <el-form-item label="题目解析" prop="analysis">
          <el-input
            v-model="formData.analysis"
            type="textarea"
            :rows="4"
            placeholder="请输入题目解析，帮助学习者理解解题思路"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="formData.difficulty" placeholder="请选择题目难度" style="width: 100%">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                allow-create
                placeholder="请选择或创建标签"
                style="width: 100%"
              >
                <el-option label="数组" value="数组" />
                <el-option label="链表" value="链表" />
                <el-option label="字符串" value="字符串" />
                <el-option label="动态规划" value="动态规划" />
                <el-option label="贪心" value="贪心" />
                <el-option label="二叉树" value="二叉树" />
                <el-option label="哈希表" value="哈希表" />
                <el-option label="排序" value="排序" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="24">
            <el-form-item label="岗位类型" prop="jobType">
              <el-select v-model="formData.jobType" placeholder="请选择岗位类型" style="width: 100%">
                <el-option label="前端开发" value="前端开发" />
                <el-option label="后端开发" value="后端开发" />
                <el-option label="全栈开发" value="全栈开发" />
                <el-option label="算法工程师" value="算法工程师" />
                <el-option label="数据工程师" value="数据工程师" />
                <el-option label="移动开发" value="移动开发" />
                <el-option label="测试工程师" value="测试工程师" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 不同题型的特定内容 -->
        <template v-if="problemType === 'program' || problemType === 'program'">
          <program-form v-model="formData" />
        </template>
        
        <template v-else-if="problemType === 'choice'">
          <choice-form v-model="formData" />
        </template>
        
        <template v-else-if="problemType === 'judge'">
          <judge-form v-model="formData" />
        </template>
        
        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建题目' }}
          </el-button>
          <el-button @click="router.go(-1)">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { problemApi } from '@/api/problem'
import type { BaseResponse } from '@/types/response'
import programForm from './components/ProgramForm.vue'
import ChoiceForm from './components/ChoiceForm.vue'
import JudgeForm from './components/JudgeForm.vue'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

// 获取题目类型
const problemType = computed(() => route.params.type as string)

// 获取题目类型名称
const typeName = computed(() => {
  const map: Record<string, string> = {
    'program': '编程题',
    'choice': '选择题',
    'judge': '判断题'
  }
  return map[problemType.value] || '题目'
})

// 判断是编辑还是新增
const isEdit = computed(() => {
  return route.params.id !== undefined
})

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  title: '',
  content: '',
  difficulty: 'MEDIUM',
  tags: [] as string[],
  jobType: '全栈开发', // 添加岗位类型字段
  analysis: '', // 添加题目解析字段
  // 编程题相关字段
  testCases: [{ input: '', output: '' }],
  // 新增编程题相关字段
  functionName: '',
  paramTypes: [] as string[],
  returnType: '',
  templates: {} as Record<string, string>,
  standardSolution: {} as Record<string, string>,
  timeLimit: 1000,
  memoryLimit: 256,
  // 选择题相关字段
  options: [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ],
  // 判断题相关字段
  answer: true
})

// 表单校验规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入题目标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入题目描述', trigger: 'blur' }
  ],
  difficulty: [
    { required: true, message: '请选择题目难度', trigger: 'change' }
  ],
  tags: [
    { required: true, message: '请至少选择一个标签', trigger: 'change' }
  ],
  jobType: [
    { required: true, message: '请选择岗位类型', trigger: 'change' }
  ],
  analysis: [
    { required: true, message: '请输入题目解析', trigger: 'blur' }
  ]
})

// 获取题目详情
const getProblemDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await problemApi.getProblemDetail(id) as BaseResponse
    if (res.code === 0 && res.data) {
      // 复制后端返回的数据到表单
      Object.assign(formData, res.data)
      ElMessage.success('加载题目信息成功')
    } else {
      ElMessage.error(res.message || '获取题目详情失败')
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      ElMessage.error('请完善表单信息')
      console.error('表单验证失败')
      submitting.value = false
      return
    }
    
    console.log('准备提交数据:', formData)
    
    // 额外验证编程题特定字段
    if (problemType.value === 'program') {
      // 验证函数信息
      if (!formData.functionName) {
        ElMessage.error('请填写函数名称')
        submitting.value = false
        return
      }
      
      if (!formData.paramTypes || formData.paramTypes.length === 0) {
        ElMessage.error('请选择参数类型')
        submitting.value = false
        return
      }
      
      if (!formData.returnType) {
        ElMessage.error('请选择返回值类型')
        submitting.value = false
        return
      }
      
      // 验证测试用例
      if (!formData.testCases || formData.testCases.length === 0) {
        ElMessage.error('请添加至少一个测试用例')
        submitting.value = false
        return
      }
      
      // 验证每个测试用例都有输入和输出
      const invalidTestCase = formData.testCases.some(tc => !tc.input || !tc.output)
      if (invalidTestCase) {
        ElMessage.error('测试用例的输入和预期输出不能为空')
        submitting.value = false
        return
      }
      
      // 验证代码模板
      if (!formData.templates || Object.keys(formData.templates).length === 0) {
        ElMessage.error('请添加至少一种语言的代码模板')
        submitting.value = false
        return
      }
      
      // 验证标准答案
      if (!formData.standardSolution || Object.keys(formData.standardSolution).length === 0) {
        ElMessage.error('请添加至少一种语言的标准答案')
        submitting.value = false
        return
      }
    }
    
    // 检查标签是否为空数组
    if (!formData.tags || formData.tags.length === 0) {
      formData.tags = []; // 确保至少是空数组而非undefined
      ElMessage.error('请至少添加一个标签');
      submitting.value = false;
      return;
    }
    
    // 对选择题进行验证
    if (problemType.value === 'choice') {
      // 检查是否至少有一个正确选项
      const hasCorrectOption = formData.options.some(option => option.isCorrect);
      if (!hasCorrectOption) {
        ElMessage.error('请至少选择一个正确选项');
        submitting.value = false;
        return;
      }
      
      // 检查所有选项是否都有内容
      const hasEmptyOption = formData.options.some(option => !option.content.trim());
      if (hasEmptyOption) {
        ElMessage.error('所有选项都必须填写内容');
        submitting.value = false;
        return;
      }
      
      // 检查选项数量
      if (formData.options.length < 2) {
        ElMessage.error('选择题至少需要两个选项');
        submitting.value = false;
        return;
      }
    }
    
    submitting.value = true
    try {
      let res: BaseResponse | null = null
      
      if (isEdit.value) {
        // 编辑题目
        const id = Number(route.params.id)
        switch (problemType.value) {
          case 'choice':
            // 对数据进行深拷贝，避免Proxy对象序列化问题
            const editChoiceData = {
              id, // 编辑模式需要提供ID
              title: formData.title,
              content: formData.content,
              difficulty: formData.difficulty,
              tags: [...formData.tags],
              jobType: formData.jobType,
              analysis: formData.analysis,
              options: formData.options.map(opt => ({
                content: opt.content,
                isCorrect: opt.isCorrect
              })),
              // 提取正确答案，将所有正确选项的索引转换为字母（A, B, C, D...）并连接
              answer: formData.options
                .map((opt, index) => opt.isCorrect ? String.fromCharCode(65 + index) : '')
                .filter(Boolean)
                .join(','),
              type: 'CHOICE' as const
            };
            console.log('处理后的编辑选择题数据:', editChoiceData);
            
            try {
              // 检查选项JSON序列化是否正常
              const jsonString = JSON.stringify(editChoiceData);
              console.log('编辑选择题JSON数据:', jsonString);
              
              res = await problemApi.updateChoiceProblem(editChoiceData) as BaseResponse;
            } catch (error: any) {
              console.error('选择题数据序列化或发送失败:', error);
              
              // 显示更具体的错误信息
              if (error.response && error.response.data) {
                console.error('后端错误详情:', error.response.data);
                ElMessage.error(`提交选择题数据失败: ${error.response.data.message || '未知错误'}`);
              } else {
                ElMessage.error('提交选择题数据失败');
              }
              
              submitting.value = false;
              return;
            }
            break
          case 'judge':
            res = await problemApi.updateJudgeProblem({
              id,
              title: formData.title,
              content: formData.content,
              difficulty: formData.difficulty,
              tags: [...formData.tags],
              jobType: formData.jobType,
              analysis: formData.analysis,
              answer: formData.answer,
              type: 'JUDGE' as const
            }) as BaseResponse
            break
          case 'program':
            res = await problemApi.updateProgramProblem({
              id,
              title: formData.title,
              content: formData.content,
              difficulty: formData.difficulty,
              tags: [...formData.tags],
              jobType: formData.jobType,
              analysis: formData.analysis,
              testCases: formData.testCases.map(tc => ({...tc})),
              functionName: formData.functionName,
              paramTypes: [...formData.paramTypes],
              returnType: formData.returnType,
              templates: {...formData.templates},
              standardSolution: {...formData.standardSolution},
              timeLimit: formData.timeLimit,
              memoryLimit: formData.memoryLimit,
              type: 'PROGRAM' as const
            }) as BaseResponse
            break
        }
      } else {
        // 创建题目
        switch (problemType.value) {
          case 'choice':
            // 对数据进行深拷贝，避免Proxy对象序列化问题
            const choiceData = {
              title: formData.title,
              content: formData.content,
              difficulty: formData.difficulty,
              tags: [...formData.tags],
              jobType: formData.jobType,
              analysis: formData.analysis,
              options: formData.options.map(opt => ({
                content: opt.content,
                isCorrect: opt.isCorrect
              })),
              // 提取正确答案，将所有正确选项的索引转换为字母（A, B, C, D...）并连接
              answer: formData.options
                .map((opt, index) => opt.isCorrect ? String.fromCharCode(65 + index) : '')
                .filter(Boolean)
                .join(','),
              type: 'CHOICE' as const
            };
            console.log('处理后的选择题数据:', choiceData);
            
            try {
              // 检查选项JSON序列化是否正常
              const jsonString = JSON.stringify(choiceData);
              console.log('选择题JSON数据:', jsonString);
              
              res = await problemApi.addChoiceProblem(choiceData) as unknown as BaseResponse;
            } catch (error: any) {
              console.error('选择题数据序列化或发送失败:', error);
              
              // 显示更具体的错误信息
              if (error.response && error.response.data) {
                console.error('后端错误详情:', error.response.data);
                ElMessage.error(`提交选择题数据失败: ${error.response.data.message || '未知错误'}`);
              } else {
                ElMessage.error('提交选择题数据失败');
              }
              
              submitting.value = false;
              return;
            }
            break
          case 'judge':
            res = await problemApi.addJudgeProblem({
              title: formData.title,
              content: formData.content,
              difficulty: formData.difficulty,
              tags: [...formData.tags],
              jobType: formData.jobType,
              analysis: formData.analysis,
              answer: formData.answer,
              type: 'JUDGE' as const
            }) as BaseResponse
            break
          case 'program':
            try {
              // 设置长时间操作的标志
              submitting.value = true;
              ElMessage.info('正在处理编程题数据，请稍候...');
              
              // 检查网络连接
              if (!navigator.onLine) {
                ElMessage.error('网络连接已断开，请检查您的网络连接');
                submitting.value = false;
                return;
              }
              
              // 确保数据完整性
              if (!formData.functionName || !formData.returnType || 
                  !formData.paramTypes || formData.paramTypes.length === 0) {
                ElMessage.error('请完整填写函数信息');
                submitting.value = false;
                return;
              }
              
              if (!formData.testCases || formData.testCases.length === 0 ||
                  formData.testCases.some(tc => !tc.input || !tc.output)) {
                ElMessage.error('请至少添加一个完整的测试用例');
                submitting.value = false;
                return;
              }
              
              if (!formData.templates || Object.keys(formData.templates).length === 0) {
                ElMessage.error('请至少添加一种语言的代码模板');
                submitting.value = false;
                return;
              }
              
              if (!formData.standardSolution || Object.keys(formData.standardSolution).length === 0) {
                ElMessage.error('请至少添加一种语言的标准答案');
                submitting.value = false;
                return;
              }
              
              // 创建简化的请求数据对象，避免使用Proxy对象
              const requestData = {
                title: formData.title,
                content: formData.content,
                difficulty: formData.difficulty,
                tags: Array.isArray(formData.tags) ? [...formData.tags] : [],
                jobType: formData.jobType || '',
                analysis: formData.analysis || '',
                functionName: formData.functionName,
                paramTypes: Array.isArray(formData.paramTypes) ? [...formData.paramTypes] : [],
                returnType: formData.returnType,
                testCases: Array.isArray(formData.testCases) ? formData.testCases.map(tc => ({
                  input: tc.input || '',
                  output: tc.output || ''
                })) : [],
                templates: formData.templates ? {...formData.templates} : {},
                standardSolution: formData.standardSolution ? {...formData.standardSolution} : {},
                timeLimit: formData.timeLimit || 1000,
                memoryLimit: formData.memoryLimit || 256,
                type: 'PROGRAM' as const
              };
              
              // 检查数据大小
              const requestDataSize = JSON.stringify(requestData).length;
              const sizeInKB = Math.round(requestDataSize / 1024);
              console.log(`请求数据大小: ${sizeInKB}KB`);
              
              // 判断数据大小是否可能导致问题
              if (sizeInKB > 5000) {
                ElMessage.error(`请求数据过大(${sizeInKB}KB)，超过服务器限制，请减少代码模板或测试用例的大小`);
                submitting.value = false;
                return;
              }
              
              if (sizeInKB > 1000) {
                ElMessage.warning(`请求数据较大(${sizeInKB}KB)，可能需要较长处理时间，请耐心等待`);
              }
              
              // 显示处理进度
              const loadingMessage = ElMessage({
                type: 'info',
                message: '正在发送请求并等待服务器处理...',
                duration: 0
              });
              
              console.log('开始发送添加编程题请求...');
              
              // 使用简单直接的请求方式
              try {
                res = await problemApi.addProgramProblem(requestData);
                
                // 关闭加载消息
                loadingMessage.close();
                
                console.log('编程题请求发送完成，响应:', res);
                
                if (!res) {
                  throw new Error('未收到服务器响应');
                }
              } catch (requestError) {
                // 关闭加载消息
                loadingMessage.close();
                throw requestError; // 继续抛出异常让外层处理
              }
              
            } catch (programError: any) {
              console.error('添加编程题错误:', programError);
              
              // 打印更多错误信息以便调试
              console.error('错误类型:', typeof programError);
              console.error('错误字符串:', String(programError));
              if (programError instanceof Error) {
                console.error('错误消息:', programError.message);
                console.error('错误堆栈:', programError.stack);
              }
              
              // 显示更详细的错误信息
              if (programError.response) {
                const status = programError.response.status;
                const data = programError.response.data;
                
                console.error('请求错误HTTP状态码:', status);
                console.error('请求错误响应体:', data);
                
                if (status === 413) {
                  ElMessage.error('请求数据过大，请减少代码模板或测试用例的大小');
                } else if (status === 400) {
                  ElMessage.error(`请求参数错误: ${data?.message || '请检查输入字段'}`);
                } else if (status === 500) {
                  ElMessage.error(`服务器内部错误: ${data?.message || '请联系管理员'}`);
                } else if (data && data.message) {
                  ElMessage.error(`服务器错误: ${data.message}`);
                } else {
                  ElMessage.error(`服务器返回错误状态码: ${status}`);
                }
              } else if (programError.message && programError.message.includes('timeout')) {
                console.error('请求超时:', programError);
                ElMessage.error('请求超时，服务器处理时间过长。请尝试减少数据大小或稍后重试。');
              } else if (programError.message && programError.message.includes('Network Error')) {
                console.error('网络错误:', programError);
                ElMessage.error('网络连接错误，请检查您的网络连接并刷新页面重试。');
              } else if (programError.request) {
                console.error('请求已发送但没有收到响应:', programError.request);
                ElMessage.error('服务器未响应，请检查网络连接或服务器状态。可能是数据过大导致服务器处理超时。');
              } else if (programError.message) {
                console.error('请求错误消息:', programError.message);
                ElMessage.error(`错误: ${programError.message}`);
              } else {
                console.error('未知错误:', programError);
                ElMessage.error('添加编程题失败，请稍后重试。如问题持续存在，请联系管理员。');
              }
              
              // 建议用户采取的行动
              ElMessage.warning('建议：尝试减少代码示例和测试用例的大小，或将问题分为多个较小的问题。');
              
              // 重置提交状态
              submitting.value = false;
              return; // 直接返回，不进行后续处理
            }
            break
        }
      }
      
      if (res && res.code === 0) {
        ElMessage.success(isEdit.value ? '更新题目成功' : '创建题目成功')
        router.push('/admin/problems')
      } else if (res) {
        console.error('服务器返回错误:', res)
        ElMessage.error(res.message || '操作失败')
      } else {
        console.error('未收到服务器响应')
        ElMessage.error('操作失败')
      }
    } catch (error) {
      console.error('保存题目失败:', error)
      // 添加更详细的错误输出
      if (error instanceof Error) {
        console.error('错误信息:', error.message)
        console.error('错误堆栈:', error.stack)
        ElMessage.error(`保存题目失败: ${error.message}`)
      } else {
        ElMessage.error('保存题目失败，未知错误')
      }
    } finally {
      submitting.value = false
    }
  })
}

// 初始化
onMounted(async () => {
  const id = route.params.id
  if (id && !isNaN(Number(id))) {
    await getProblemDetail(Number(id))
  }
})
</script>

<style scoped>
.problem-edit {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
