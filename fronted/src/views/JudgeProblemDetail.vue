<template>
  <div class="judge-problem-container">
    <el-row :gutter="20" justify="center">
      <el-col :xs="24" :sm="22" :md="20" :lg="18" :xl="16">
        <!-- 题目基本信息 -->
        <el-card class="problem-card">
          <template #header>
            <div class="problem-header">
              <div class="problem-title">
                <h2>{{ problem?.title }}</h2>
                <div class="problem-meta">
                  <el-tag type="info" size="small">判断题</el-tag>
                  <el-tag :type="getDifficultyType(problem?.difficulty)" size="small" class="ml-2">{{ problem?.difficulty }}</el-tag>
                  <span class="problem-stats">通过率: {{ problem?.acceptCount && problem?.submitCount ? Math.round((problem.acceptCount / problem.submitCount) * 100) + '%' : '0%' }}</span>
                </div>
              </div>
            </div>
          </template>
          
          <!-- 题目内容 -->
          <div class="problem-content markdown-body" v-html="renderMarkdown(problem?.content || '')"></div>
          
          <!-- 提交区域 -->
          <div class="submit-area">
            <h3>提交答案</h3>
            <el-radio-group v-model="selectedAnswer" class="judge-options">
              <el-radio :label="true" class="judge-option">
                <div class="option-content">
                  <el-icon><Select /></el-icon> 正确
                </div>
              </el-radio>
              <el-radio :label="false" class="judge-option">
                <div class="option-content">
                  <el-icon><Close /></el-icon> 错误
                </div>
              </el-radio>
            </el-radio-group>
            
            <div class="submit-btn">
              <el-button type="primary" @click="submitAnswer">提交答案</el-button>
            </div>
          </div>
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
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'ACCEPTED' ? 'success' : 'danger'">
                    {{ scope.row.status === 'ACCEPTED' ? '正确' : '错误' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="answer" label="提交答案">
                <template #default="scope">
                  {{ scope.row.answer === true ? '正确' : '错误' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <el-empty v-else description="暂无提交记录" />
        </el-card>
        
        <!-- 讨论区 -->
        <el-card class="discussion-card">
          <template #header>
            <div class="card-header">
              <h3>题目讨论</h3>
            </div>
          </template>
          <div class="discussion-placeholder">
            <el-empty description="讨论功能即将上线，敬请期待！">
              <template #description>
                <p>讨论功能即将上线，敬请期待！</p>
                <p class="text-muted">分享你的解题思路和技巧</p>
              </template>
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Select, Close } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO } from '@/api/problem'
import { submissionApi } from '@/api/submission'
// @ts-ignore
import MarkdownIt from 'markdown-it'

// 定义提交结果类型
interface SubmissionVO {
  id?: number;
  createTime?: string;
  status?: string;
  answer?: boolean | string;
  problemId?: number;
}

// 定义简化的题目类型用于相关题目
interface SimpleProblemVO {
  id: number;
  title: string;
  difficulty: string;
  content?: string;
  tags?: string[];
  type?: string;
}

// 题目数据
const problem = ref<ProblemVO | null>(null)
// 选中的答案 (true 或 false)
const selectedAnswer = ref<boolean | null>(null)
// 路由工具
const router = useRouter()
const route = useRoute()
const relatedProblems = ref<SimpleProblemVO[]>([])

// 提交历史
const submissionHistory = ref<SubmissionVO[]>([])

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
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
      { id: 5, title: 'Java线程安全问题判断', difficulty: '简单', content: '', tags: [], type: 'JUDGE' },
      { id: 6, title: 'JVM垃圾回收机制原理', difficulty: '中等', content: '', tags: [], type: 'JUDGE' },
      { id: 7, title: 'Spring事务传播机制正确性判断', difficulty: '困难', content: '', tags: [], type: 'JUDGE' }
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
    // const res = await submissionApi.getSubmissionHistory(problem.value.id)
    // if (res.code === 0 && res.data) {
    //   submissionHistory.value = res.data
    // }
    
    // 模拟数据
    submissionHistory.value = [
      { id: 2001, createTime: '2023-06-15T15:30:00', status: 'ACCEPTED', answer: true },
      { id: 2002, createTime: '2023-06-14T14:20:00', status: 'WRONG_ANSWER', answer: false }
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
const submitAnswer = async () => {
  if (selectedAnswer.value === null) {
    ElMessage.warning('请选择一个答案')
    return
  }
  
  if (!problem.value?.id) {
    ElMessage.error('题目信息不完整')
    return
  }
  
  try {
    const res = await submissionApi.submitJudge({
      problemId: problem.value.id,
      answer: selectedAnswer.value
    })
    
    if (res.code === 0 && res.data) {
      ElMessage.success('提交成功')
      // 可以跳转到提交结果页面或显示结果
      router.push(`/submissions/${res.data.id}`)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交答案异常:', error)
    ElMessage.error('提交失败，请稍后重试')
  }
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
</script>

<style scoped>
.judge-problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-card, .related-problems-card, .submission-history-card, .discussion-card {
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

.submit-area {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.judge-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 15px;
}

.judge-option {
  height: 50px;
  padding: 0 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.judge-option:hover {
  background-color: #f5f7fa;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.submit-btn {
  margin-top: 30px;
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
}

.text-muted {
  color: #909399;
  font-size: 14px;
}

.discussion-placeholder {
  padding: 30px 0;
}
</style>