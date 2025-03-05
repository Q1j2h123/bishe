<template>
  <div class="problems-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>刷题平台</h2>
          <div class="user-info">
            <el-dropdown>
              <span class="user-dropdown">
                {{ userStore.currentUser?.userName || '未登录' }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">个人主页</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="filter-section">
          <el-form :inline="true" :model="filterForm">
            <el-form-item label="岗位">
              <el-select v-model="filterForm.job" placeholder="选择岗位" @change="handleFilter">
                <el-option
                  v-for="item in jobOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="题目类型">
              <el-select v-model="filterForm.type" placeholder="选择题目类型" @change="handleFilter">
                <el-option
                  v-for="item in typeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="标签">
              <el-select
                v-model="filterForm.tags"
                multiple
                placeholder="选择标签"
                style="width: 300px"
                @change="handleFilter"
              >
                <el-option
                  v-for="item in tagOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="problems-list">
          <el-table :data="problems" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="题号" width="80" />
            <el-table-column prop="title" label="题目" />
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="{ row }">
                <el-tag :type="getDifficultyType(row.difficulty)">
                  {{ row.difficulty }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="questionType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getQuestionTypeTag(row.questionType)">
                  {{ getQuestionTypeLabel(row.questionType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" width="200">
              <template #default="{ row }">
                <el-tag
                  v-for="tag in row.tags"
                  :key="tag"
                  size="small"
                  style="margin-right: 5px"
                >
                  {{ tag }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="startProblem(row)">
                  开始答题
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { questionApi } from '../api/question'
import { useUserStore } from '../stores/user'
import type { Question } from '../api/question'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const problems = ref<Question[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = reactive({
  job: '',
  type: '',
  tags: [] as string[]
})

const jobOptions = [
  { value: 'frontend', label: '前端开发' },
  { value: 'backend', label: '后端开发' },
  { value: 'data', label: '数据分析' },
  { value: 'algorithm', label: '算法工程师' }
]

const typeOptions = [
  { value: 'programming', label: '编程题' },
  { value: 'choice', label: '选择题' },
  { value: 'judgement', label: '判断题' }
]

const tagOptions = [
  { value: 'array', label: '数组' },
  { value: 'string', label: '字符串' },
  { value: 'stack', label: '栈' },
  { value: 'queue', label: '队列' },
  { value: 'tree', label: '树' },
  { value: 'graph', label: '图' },
  { value: 'dp', label: '动态规划' }
]

const getDifficultyType = (difficulty: string) => {
  const types: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}

const getQuestionTypeTag = (type: string) => {
  const types: Record<string, string> = {
    'programming': 'primary',
    'choice': 'success',
    'judgement': 'warning'
  }
  return types[type] || 'info'
}

const getQuestionTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'programming': '编程题',
    'choice': '选择题',
    'judgement': '判断题'
  }
  return labels[type] || type
}

const fetchProblems = async () => {
  try {
    loading.value = true
    const res = await questionApi.getQuestionList({
      current: currentPage.value,
      pageSize: pageSize.value,
      ...filterForm
    })
    problems.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  currentPage.value = 1
  fetchProblems()
}

const resetFilter = () => {
  filterForm.job = ''
  filterForm.type = ''
  filterForm.tags = []
  handleFilter()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchProblems()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProblems()
}

const startProblem = (problem: Question) => {
  // TODO: 实现开始答题逻辑
  console.log('开始答题：', problem)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchProblems()
})
</script>

<style scoped>
.problems-container {
  height: 100vh;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.problems-list {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 