<template>
  <div class="problem-detail-container">
    <!-- 根据题目类型动态加载不同的详情页组件 -->
    <ChoiceProblemDetail v-if="problem?.type === 'CHOICE'" :problemId="Number(route.params.id || 0)" />
    <JudgeProblemDetail v-else-if="problem?.type === 'JUDGE'" :problemId="Number(route.params.id || 0)" />
    <ProgramProblemDetail v-else-if="problem?.type === 'PROGRAM'" :problemId="Number(route.params.id || 0)" />
    
    <!-- 加载中状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 加载失败状态 -->
    <el-result 
      v-else
      icon="error"
      title="题目加载失败"
      sub-title="无法加载题目信息，请稍后再试"
    >
      <template #extra>
        <el-button type="primary" @click="loadProblem">重新加载</el-button>
        <el-button @click="router.push('/problems')">返回题目列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { problemApi, type ProblemVO } from '@/api/problem'
import ChoiceProblemDetail from './ChoiceProblemDetail.vue'
import JudgeProblemDetail from './JudgeProblemDetail.vue'
import ProgramProblemDetail from './ProgramProblemDetail.vue'
import { useAutoSave } from '@/hooks/useAutoSave'

// 题目数据
const problem = ref<ProblemVO | null>(null)
// 加载状态
const loading = ref<boolean>(true)

// 路由工具
const router = useRouter()
const route = useRoute()

// 定义必要的变量
const userAnswer = ref<string>('')
const codeContent = ref<string>('')
const selectedLanguage = ref<string>('java')

// 加载题目数据
const loadProblem = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('题目ID不能为空')
    router.push('/problems')
    return
  }
  
  loading.value = true
  try {
    const res = await problemApi.getProblemDetail(Number(id))
    
    if (res.code === 0 && res.data) {
      problem.value = res.data
      
      // 根据题目类型初始化对应的草稿功能
      if (problem.value.type === 'PROGRAM') {
        setupProgramDraft()
      } else {
        setupChoiceJudgeDraft()
      }
    } else {
      ElMessage.error(res.message || '获取题目详情失败')
    }
  } catch (error) {
    console.error('加载题目详情异常:', error)
    ElMessage.error('获取题目详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 选择题或判断题的自动保存示例
const setupChoiceJudgeDraft = () => {
  if (!problem.value) return
  
  const { 
    content: savedAnswer, 
    isDirty, 
    lastSaved,
    save
  } = useAutoSave(
    problem.value.id || 0,
    problem.value.type,
    userAnswer.value,
    {
      onSaved: () => {
        console.log('草稿已保存')
      },
      onLoaded: (content) => {
        userAnswer.value = content
        ElMessage.info('已加载上次的答案')
      }
    }
  )
  
  // 将自动保存内容与组件状态双向绑定
  watch(userAnswer, (newValue) => {
    savedAnswer.value = newValue
  })
  
  // 在组件卸载前保存
  onBeforeUnmount(() => {
    save()
  })
}

// 编程题的自动保存示例
const setupProgramDraft = () => {
  if (!problem.value) return
  
  const { 
    content: savedCode, 
    language: savedLanguage,
    isDirty, 
    lastSaved,
    save,
    setLanguage
  } = useAutoSave(
    problem.value.id || 0,
    'PROGRAM',
    codeContent.value,
    {
      onSaved: () => {
        console.log('代码草稿已保存')
      },
      onLoaded: (content) => {
        codeContent.value = content
        ElMessage.info('已加载上次编写的代码')
      }
    }
  )
  
  // 设置初始语言
  setLanguage(selectedLanguage.value)
  
  // 监听代码变化
  watch(codeContent, (newValue) => {
    savedCode.value = newValue
  })
  
  // 监听语言变化
  watch(selectedLanguage, (newValue) => {
    setLanguage(newValue)
  })
  
  // 在组件卸载前保存
  onBeforeUnmount(() => {
    save()
  })
}

// 页面加载时获取题目
onMounted(() => {
  loadProblem()
})
</script>

<style scoped>
.problem-detail-container {
  padding: 20px;
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>