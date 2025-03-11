<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO, type ProblemQueryRequest } from '@/api/problem'

const router = useRouter()
const loading = ref(false)
const problemList = ref<ProblemVO[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<ProblemQueryRequest>({
  current: 1,
  pageSize: 10,
  searchText: '',
  type: '',
  difficulty: '',
  jobType: '',
  tags: [],
  status: ''
})

// 计算属性：是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return queryParams.searchText || 
         queryParams.type || 
         queryParams.difficulty || 
         queryParams.jobType || 
         (queryParams.tags && queryParams.tags.length > 0) || 
         queryParams.status
})

// 获取筛选条件文本
const getActiveFilterTitle = () => {
  const parts: string[] = []
  
  if (queryParams.type) {
    parts.push(`题目类型: ${getTypeText(queryParams.type)}`)
  }
  
  if (queryParams.difficulty) {
    parts.push(`难度: ${getDifficultyText(queryParams.difficulty)}`)
  }
  
  if (queryParams.jobType) {
    parts.push(`岗位类型: ${queryParams.jobType}`)
  }
  
  if (queryParams.tags && queryParams.tags.length > 0) {
    parts.push(`标签: ${queryParams.tags.join(', ')}`)
  }
  
  if (queryParams.status) {
    parts.push(`状态: ${getStatusText(queryParams.status)}`)
  }
  
  if (queryParams.searchText) {
    parts.push(`关键词: ${queryParams.searchText}`)
  }
  
  return parts.length ? `当前筛选: ${parts.join(' | ')}` : '已应用筛选条件'
}

// 获取题目类型文本
const getTypeText = (type: string): string => {
  const map: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return map[type] || type
}

// 获取状态文本
const getStatusText = (status: string): string => {
  const map: Record<string, string> = {
    'UNSOLVED': '未解决',
    'ATTEMPTED': '尝试过',
    'SOLVED': '已解决'
  }
  return map[status] || '未知'
}

// 获取难度文本
const getDifficultyText = (difficulty: string): string => {
  const map: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难',
    '简单': '简单',
    '中等': '中等',
    '困难': '困难'
  }
  return map[difficulty] || difficulty
}

// 格式化日期
const formatDate = (dateString: string): string => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 获取时间间隔
const getTimeAgo = (dateString: string): string => {
  if (!dateString) return ''
  
  const now = new Date()
  const past = new Date(dateString)
  const diffMs = now.getTime() - past.getTime()
  
  // 转换为秒
  const diffSec = Math.floor(diffMs / 1000)
  
  if (diffSec < 60) return `${diffSec}秒前`
  
  // 转换为分钟
  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin}分钟前`
  
  // 转换为小时
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  
  // 转换为天
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 30) return `${diffDay}天前`
  
  // 转换为月
  const diffMonth = Math.floor(diffDay / 30)
  if (diffMonth < 12) return `${diffMonth}个月前`
  
  // 转换为年
  const diffYear = Math.floor(diffMonth / 12)
  return `${diffYear}年前`
}

// 处理查询参数变化
const handleParamChange = (label: string, value: any) => {
  if (!value) return
  ElMessage.success(`已选择${label}: ${value}`)
}

// 处理标签参数变化
const handleTagsChange = (tags: string[]) => {
  if (!tags.length) return
  ElMessage.success(`已选择${tags.length}个标签: ${tags.join(', ')}`)
}

// 执行查询
const handleQuery = () => {
  loadProblemList()
}

// 重置查询参数
const resetQuery = () => {
  Object.assign(queryParams, {
    searchText: '',
    type: '',
    difficulty: '',
    jobType: '',
    tags: [],
    status: ''
  })
  loadProblemList()
}

// 加载题目列表
const loadProblemList = async () => {
  loading.value = true
  try {
    // 过滤掉空值参数
    const params: Record<string, any> = { ...queryParams }
    Object.keys(params).forEach(key => {
      const value = params[key]
      if (value === '' || value === null || value === undefined || 
         (Array.isArray(value) && value.length === 0)) {
        delete params[key]
      }
    })
    
    console.log('查询参数:', params)
    const res = await problemApi.getProblemList(params)
    
    if (res.code === 0) {
      // 数据处理：确保每条记录有正确的字段格式
      problemList.value = (res.data.records || []).map((item: any, index: number) => {
        const problem = { ...item } as ProblemVO & { status?: string, displayId?: number }
        
        // 设置显示ID从1开始
        const currentPage = queryParams.current || 1;
        const pageSize = queryParams.pageSize || 10;
        problem.displayId = index + 1 + (currentPage - 1) * pageSize;
        
        // 确保tags始终是数组
        if (!problem.tags) problem.tags = []
        
        return problem
      })
      
      total.value = res.data.total || 0
      
      // 如果查询结果为空但有查询参数，提示用户
      if (problemList.value.length === 0 && Object.keys(params).length > 2) {
        ElMessage.warning('未找到符合条件的题目，请尝试调整查询条件')
      }
    } else {
      ElMessage.error(res.message || '获取题目列表失败')
    }
  } catch (error) {
    console.error('加载题目列表异常:', error)
    ElMessage.error('获取题目列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取表格行的样式类名
const getRowClassName = ({ row }: { row: ProblemVO }) => {
  if (row.difficulty === 'EASY' || row.difficulty === '简单') {
    return 'easy-row'
  } else if (row.difficulty === 'MEDIUM' || row.difficulty === '中等') {
    return 'medium-row'
  } else if (row.difficulty === 'HARD' || row.difficulty === '困难') {
    return 'hard-row'
  }
  return ''
}

// 处理分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.current = 1
  loadProblemList()
}

// 处理当前页变化
const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadProblemList()
}

// 进入题目详情页
const handleViewProblem = (row: ProblemVO) => {
  // 根据题目类型导航到不同的详情页
  if (row.type === 'CHOICE') {
    router.push(`/problem/choice/${row.id}`)
  } else if (row.type === 'JUDGE') {
    router.push(`/problem/judge/${row.id}`)
  } else if (row.type === 'PROGRAM') {
    router.push(`/problem/program/${row.id}`)
  } else {
    // 默认导航到通用详情页
    router.push(`/problem/${row.id}`)
  }
}

// 页面加载时自动获取题目列表
onMounted(() => {
  loadProblemList()
})
</script>

<template>
  <div class="problems-container">
    <el-header class="header">
      <div class="logo">
        <h1>在线判题系统</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text type="primary">题目列表</el-button>
        <el-button text @click="router.push('/submissions')">提交记录</el-button>
        <el-button text @click="router.push('/profile')">个人中心</el-button>
      </div>
    </el-header>
    
    <el-main class="main">
      <!-- 全局条件显示 -->
      <div v-if="hasActiveFilters" class="global-filter-container">
        <el-alert
          :title="getActiveFilterTitle()"
          type="success"
          :closable="false"
          show-icon
        />
      </div>
      
      <!-- 搜索栏 -->
      <el-card class="search-card">
        <el-form :inline="true" :model="queryParams" @submit.prevent>
          <el-form-item label="关键词">
            <el-input v-model="queryParams.searchText" placeholder="题目标题/内容关键词" clearable @change="handleParamChange('关键词', $event)">
              <template #prefix v-if="queryParams.searchText">
                <el-icon class="el-input__icon"><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="题目类型">
            <el-select v-model="queryParams.type" placeholder="选择题目类型" clearable @change="handleParamChange('题目类型', $event)">
              <template #prefix v-if="queryParams.type">
                <el-tag size="small" type="success" class="select-prefix-tag">{{ getTypeText(queryParams.type) }}</el-tag>
              </template>
              <el-option label="选择题" value="CHOICE" />
              <el-option label="判断题" value="JUDGE" />
              <el-option label="编程题" value="PROGRAM" />
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类型">
            <el-select v-model="queryParams.jobType" placeholder="选择岗位类型" clearable @change="handleParamChange('岗位类型', $event)">
              <template #prefix v-if="queryParams.jobType">
                <el-tag size="small" type="success" class="select-prefix-tag">{{ queryParams.jobType }}</el-tag>
              </template>
              <el-option label="前端开发" value="前端开发" />
              <el-option label="后端开发" value="后端开发" />
              <el-option label="全栈开发" value="全栈开发" />
              <el-option label="移动开发" value="移动开发" />
              <el-option label="算法工程师" value="算法工程师" />
              <el-option label="测试工程师" value="测试工程师" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select 
              v-model="queryParams.tags" 
              multiple 
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择标签" 
              clearable
              style="min-width: 200px;"
              @change="handleTagsChange"
            >
              <template #prefix v-if="queryParams.tags && queryParams.tags.length > 0">
                <div class="tag-prefix-container">
                  <el-tag 
                    v-for="(tag, index) in queryParams.tags.slice(0, 1)" 
                    :key="index" 
                    size="small" 
                    type="success" 
                    class="select-prefix-tag"
                  >
                    {{ tag }}
                  </el-tag>
                  <span v-if="queryParams.tags.length > 1" class="tag-more">+{{ queryParams.tags.length - 1 }}</span>
                </div>
              </template>
              <el-option label="数组" value="数组" />
              <el-option label="链表" value="链表" />
              <el-option label="字符串" value="字符串" />
              <el-option label="动态规划" value="动态规划" />
              <el-option label="贪心" value="贪心" />
              <el-option label="二叉树" value="二叉树" />
              <el-option label="哈希表" value="哈希表" />
              <el-option label="排序" value="排序" />
              <el-option label="Java" value="Java" />
              <el-option label="JavaScript" value="JavaScript" />
              <el-option label="Python" value="Python" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="queryParams.difficulty" placeholder="选择难度" clearable @change="handleParamChange('难度', $event)">
              <template #prefix v-if="queryParams.difficulty">
                <el-tag size="small" type="success" class="select-prefix-tag">{{ getDifficultyText(queryParams.difficulty) }}</el-tag>
              </template>
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="选择状态" clearable @change="handleParamChange('状态', $event)">
              <template #prefix v-if="queryParams.status">
                <el-tag size="small" type="success" class="select-prefix-tag">{{ getStatusText(queryParams.status) }}</el-tag>
              </template>
              <el-option label="未解决" value="UNSOLVED" />
              <el-option label="尝试过" value="ATTEMPTED" />
              <el-option label="已解决" value="SOLVED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 题目列表 -->
      <el-card class="problem-list-card">
        <el-table 
          :data="problemList" 
          stripe 
          border 
          v-loading="loading"
          :row-class-name="getRowClassName"
        >
          <el-table-column prop="displayId" label="序号" width="80" />
          <el-table-column prop="title" label="题目标题" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <a href="javascript:;" class="problem-title" @click="handleViewProblem(row)">{{ row.title }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="row.type === 'CHOICE' ? 'success' : row.type === 'JUDGE' ? 'warning' : 'primary'">
                {{ getTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="jobType" label="岗位类型" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.jobType || '通用' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="80">
            <template #default="{ row }">
              <el-tag 
                :type="(row.difficulty === '简单' || row.difficulty === 'EASY') ? 'success' : 
                       (row.difficulty === '中等' || row.difficulty === 'MEDIUM') ? 'warning' : 'danger'">
                {{ getDifficultyText(row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="tags" label="标签" min-width="180">
            <template #default="{ row }">
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag" 
                size="small" 
                class="tag-item">
                {{ tag }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag 
                v-if="row.status"
                :type="row.status === 'SOLVED' ? 'success' : row.status === 'ATTEMPTED' ? 'warning' : 'info'">
                {{ getStatusText(row.status) }}
              </el-tag>
              <el-tag v-else type="info">未解决</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                link
                @click="handleViewProblem(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<style scoped>
.problems-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.logo h1 {
  font-size: 20px;
  color: var(--primary-color);
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 16px;
}

.main {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-color);
}

.global-filter-container {
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.problem-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.tag-prefix-container {
  display: flex;
  align-items: center;
}

.tag-more {
  margin-left: 5px;
  font-size: 12px;
  color: #409eff;
}

.select-prefix-tag {
  margin-right: 5px;
}

.problem-title {
  color: #409eff;
  text-decoration: none;
}

.problem-title:hover {
  text-decoration: underline;
}

/* 题目难度行样式 */
:deep(.easy-row) {
  background-color: rgba(103, 194, 58, 0.1) !important;
}

:deep(.easy-row:hover) {
  background-color: rgba(103, 194, 58, 0.2) !important;
}

:deep(.medium-row) {
  background-color: rgba(230, 162, 60, 0.1) !important;
}

:deep(.medium-row:hover) {
  background-color: rgba(230, 162, 60, 0.2) !important;
}

:deep(.hard-row) {
  background-color: rgba(245, 108, 108, 0.1) !important;
}

:deep(.hard-row:hover) {
  background-color: rgba(245, 108, 108, 0.2) !important;
}
</style> 