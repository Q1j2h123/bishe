<template>
  <div class="user-submissions-container">
    <el-header class="header">
      <div class="logo" @click="router.push('/home')">
        <h1>面试刷题平台</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text @click="router.push('/problems')">题目列表</el-button>
        <el-button text type="primary">提交记录</el-button>
        <el-button text @click="router.push('/user-center')">个人中心</el-button>
      </div>
    </el-header>

    <el-card class="submissions-card">
      <template #header>
        <div class="card-header">
          <div class="title-container">
            <h2>我的提交记录</h2>
          </div>
          <div class="filter-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目标题"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            
            <el-select v-model="typeFilter" placeholder="题目类型" @change="handleFilterChange" clearable class="filter-item">
              <el-option label="全部类型" value="" />
              <el-option label="选择题" value="CHOICE" />
              <el-option label="判断题" value="JUDGE" />
              <el-option label="编程题" value="PROGRAM" />
            </el-select>
            
            <el-select v-model="statusFilter" placeholder="状态" @change="handleFilterChange" clearable class="filter-item">
              <el-option label="全部状态" value="" />
              <el-option label="正确" value="ACCEPTED" />
              <el-option label="错误" value="WRONG_ANSWER" />
              <el-option label="编译错误" value="COMPILE_ERROR" />
              <el-option label="运行错误" value="RUNTIME_ERROR" />
              <el-option label="超时" value="TIME_LIMIT_EXCEEDED" />
              <el-option label="内存超限" value="MEMORY_LIMIT_EXCEEDED" />
              <el-option label="待测评" value="PENDING" />
              <el-option label="系统错误" value="SYSTEM_ERROR" />
            </el-select>
            
            <el-select v-model="difficultyFilter" placeholder="难度" @change="handleFilterChange" clearable class="filter-item">
              <el-option label="全部难度" value="" />
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
            
            <el-select v-model="jobTypeFilter" placeholder="岗位类型" @change="handleFilterChange" clearable class="filter-item">
              <el-option label="全部岗位" value="" />
              <el-option 
                v-for="option in jobTypeOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value"
              >
                {{ option.label }}
              </el-option>
            </el-select>

            <el-select 
              v-model="tagFilter" 
              placeholder="标签" 
              @change="handleFilterChange" 
              clearable 
              filterable 
              class="filter-item"
            >
              <el-option 
                v-for="option in tagOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value" 
              >
                <el-tag size="small" effect="light" class="tag-option">{{ option.label }}</el-tag>
              </el-option>
            </el-select>
            
            <el-button type="primary" size="small" plain @click="resetFilters" class="reset-button">
              重置筛选
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="submissions-table">
        <el-table
          v-loading="loading"
          :data="submissions"
          style="width: 100%"
          :empty-text="loading ? '加载中...' : '暂无提交记录'"
          stripe
          border
        >
          <el-table-column prop="id" label="ID" width="80" />
          
          <el-table-column prop="problemTitle" label="题目" min-width="200">
            <template #default="{ row }">
              <router-link :to="`/problem/${row.problemId}`" class="problem-link">
                {{ row.problemTitle }}
              </router-link>
            </template>
          </el-table-column>
          
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getTypeTagType(row.type)" 
                size="small" 
                effect="plain"
              >
                {{ formatType(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="{ row }">
              <el-tag 
                :type="getDifficultyTagType(row.difficulty)" 
                size="small"
              >
                {{ formatDifficulty(row.difficulty) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="jobType" label="岗位类型" width="120">
            <template #default="{ row }">
              <span v-if="row.jobType">{{ formatJobType(row.jobType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="tags" label="标签" width="200">
            <template #default="{ row }">
              <div v-if="row.tags && row.tags.length > 0" class="tags-container">
                <el-tag
                  v-for="tag in formatTags(row.tags)"
                  :key="tag"
                  size="small"
                  effect="light"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="150">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusTagType(row.status)" 
                size="small"
              >
                {{ formatStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="提交详情" width="180">
            <template #default="{ row }">
              <div v-if="row.type === 'PROGRAM'">
                <div v-if="row.executeTime" class="execution-info">
                  <el-tooltip content="执行时间" placement="top">
                    <span><el-icon><Timer /></el-icon> {{ row.executeTime }} ms</span>
                  </el-tooltip>
                </div>
                <div v-if="row.memoryUsage" class="execution-info">
                  <el-tooltip content="内存使用" placement="top">
                    <span><el-icon><Monitor /></el-icon> {{ row.memoryUsage }} MB</span>
                  </el-tooltip>
                </div>
                <div v-if="row.language" class="execution-info">
                  <el-tooltip content="编程语言" placement="top">
                    <span><el-icon><Document /></el-icon> {{ row.language }}</span>
                  </el-tooltip>
                </div>
                <div v-if="!row.executeTime && !row.memoryUsage && !row.language" class="execution-info">
                  <el-tooltip content="查看完整信息" placement="top">
                    <span><el-icon><InfoFilled /></el-icon> 点击"查看详情"获取更多信息</span>
                  </el-tooltip>
                </div>
              </div>
              <div v-else-if="row.type === 'CHOICE' || row.type === 'JUDGE'">
                <div v-if="row.answer" class="answer-info">
                  <el-tooltip content="您的答案" placement="top">
                    <span><el-icon><ChatDotRound /></el-icon> {{ formatAnswer(row) }}</span>
                  </el-tooltip>
                </div>
                <div v-if="!row.answer" class="answer-info">
                  <el-tooltip content="查看完整信息" placement="top">
                    <span><el-icon><InfoFilled /></el-icon> 点击"查看详情"获取更多信息</span>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="submissionTime" label="提交时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.submissionTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                link
                @click="viewSubmissionDetail(row.id)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Timer, Monitor, Document, ChatDotRound, InfoFilled } from '@element-plus/icons-vue'
import { submissionApi, type SubmissionVO, type SubmissionQueryRequest } from '@/api/submission'
import { problemApi } from '@/api/problem'

// 路由
const router = useRouter()

// 表格数据
const submissions = ref<SubmissionVO[]>([])
const loading = ref(false)
const total = ref(0)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 筛选参数
const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const difficultyFilter = ref('')
const jobTypeFilter = ref('')
const tagFilter = ref('')
const tagOptions = ref<{label: string, value: string}[]>([
  { label: '全部标签', value: '' }
])
const jobTypeOptions = ref<{label: string, value: string}[]>([])

// 获取所有岗位类型
const loadAllJobTypes = async () => {
  try {
    const res = await problemApi.getAllJobTypes()
    if (res.code === 0 && res.data) {
      // 转换后端返回的岗位类型数据为前端格式
      jobTypeOptions.value = res.data.map(jobType => ({
        label: formatJobType(jobType),
        value: jobType
      }));
    }
  } catch (error) {
    console.error('加载岗位类型列表失败:', error)
  }
}

// 加载提交记录数据
const loadSubmissions = async () => {
  loading.value = true
  
  try {
    // 构建请求参数，过滤掉undefined和空字符串值
    const params: SubmissionQueryRequest = {
      current: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 只添加有效的筛选条件
    if (typeFilter.value) {
      params.type = typeFilter.value;
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value;
    }
    
    if (difficultyFilter.value) {
      params.difficulty = difficultyFilter.value;
    }
    
    if (jobTypeFilter.value) {
      params.jobType = jobTypeFilter.value;
    }
    
    if (tagFilter.value) {
      // 使用tag参数
      params.tag = tagFilter.value;
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }
    
    console.log('搜索参数:', params);
    
    const res = await submissionApi.getMySubmissionList(params);
    
    if (res.code === 0) {
      submissions.value = res.data.records;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.message || '加载提交记录失败');
    }
  } catch (error: any) {
    console.error('加载提交记录异常:', error);
    
    // 添加详细错误信息输出
    if (error.response) {
      console.error('错误响应状态:', error.response.status);
      console.error('错误响应数据:', error.response.data);
    }
    
    ElMessage.error('加载提交记录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 获取所有标签
const loadAllTags = async () => {
  try {
    const res = await problemApi.getAllTags()
    if (res.code === 0 && res.data) {
      // 清理标签数据，确保没有引号和括号
      const cleanedTags = res.data.map(tag => 
        String(tag).replace(/["'\[\]{}]/g, '').trim()
      )
      
      tagOptions.value = [
        { label: '全部标签', value: '' }, 
        ...cleanedTags.map(tag => ({
          label: tag,
          value: tag
        }))
      ]
    }
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 格式化类型
const formatType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return typeMap[type] || type
}

// 获取类型标签样式
const getTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    'CHOICE': 'success',
    'JUDGE': 'warning',
    'PROGRAM': 'info'
  }
  return typeMap[type] || ''
}

// 格式化难度
const formatDifficulty = (difficulty: string): string => {
  const difficultyMap: Record<string, string> = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难',
    '简单': '简单',
    '中等': '中等',
    '困难': '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

// 获取难度标签样式颜色
const getDifficultyTagType = (difficulty: string): string => {
  const difficultyMap: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return difficultyMap[difficulty] || 'info'
}

// 格式化岗位类型
const formatJobType = (jobType: string): string => {
  const jobTypeMap: Record<string, string> = {
    'FRONTEND': '前端',
    'BACKEND': '后端',
    'ALGORITHM': '算法',
    'DATABASE': '数据库',
    'SYSTEM_DESIGN': '系统设计'
  }
  return jobTypeMap[jobType] || jobType
}

// 格式化状态
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': '评测中',
    'ACCEPTED': '正确',
    'CORRECT': '正确',
    'WRONG': '错误',
    'WRONG_ANSWER': '错误',
    'COMPILE_ERROR': '编译错误',
    'RUNTIME_ERROR': '运行错误',
    'TIME_LIMIT_EXCEEDED': '超时',
    'MEMORY_LIMIT_EXCEEDED': '内存超限',
    'SYSTEM_ERROR': '系统错误'
  }
  return statusMap[status] || status
}

// 获取状态标签样式
const getStatusTagType = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING': 'info',
    'ACCEPTED': 'success',
    'CORRECT': 'success',
    'WRONG_ANSWER': 'danger',
    'COMPILE_ERROR': 'warning',
    'RUNTIME_ERROR': 'danger',
    'TIME_LIMIT_EXCEEDED': 'warning',
    'MEMORY_LIMIT_EXCEEDED': 'warning'
  }
  return statusMap[status] || 'info'
}

// 格式化答案
const formatAnswer = (submission: SubmissionVO): string => {
  if (submission.type === 'JUDGE') {
    return submission.answer === 'true' ? '正确' : '错误'
  }
  return String(submission.answer)
}

// 格式化日期时间
const formatDateTime = (dateTime: string | undefined): string => {
  if (!dateTime) return ''
  
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 查看提交详情
const viewSubmissionDetail = (submissionId: number) => {
  router.push(`/submission/${submissionId}`)
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
  loadSubmissions()
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1 // 重置到第一页
  
  // 记录当前选择的条件，提供用户反馈
  const selectedFilters = [];
  
  if (typeFilter.value) {
    selectedFilters.push(`题目类型: ${formatType(typeFilter.value)}`);
  }
  
  if (statusFilter.value) {
    selectedFilters.push(`状态: ${formatStatus(statusFilter.value)}`);
  }
  
  if (difficultyFilter.value) {
    selectedFilters.push(`难度: ${formatDifficulty(difficultyFilter.value)}`);
  }
  
  if (jobTypeFilter.value) {
    selectedFilters.push(`岗位: ${formatJobType(jobTypeFilter.value)}`);
  }
  
  if (tagFilter.value) {
    selectedFilters.push(`标签: ${formatSingleTag(tagFilter.value)}`);
  }
  
  if (selectedFilters.length > 0) {
    ElMessage.success(`筛选条件: ${selectedFilters.join(', ')}`);
  }
  
  loadSubmissions();
}

// 添加重置筛选条件的功能
const resetFilters = () => {
  searchKeyword.value = '';
  typeFilter.value = '';
  statusFilter.value = '';
  difficultyFilter.value = '';
  jobTypeFilter.value = '';
  tagFilter.value = '';
  currentPage.value = 1;
  
  ElMessage.info('已重置所有筛选条件');
  loadSubmissions();
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadSubmissions()
}

// 处理每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
 // currentPage.value = 1 // 重置到第一页
  loadSubmissions()
}

// 格式化单个标签，去除引号和中括号
const formatSingleTag = (tag: string): string => {
  if (!tag) return '';
  
  // 如果是对象，尝试转换为字符串
  if (typeof tag === 'object') {
    try {
      return String(tag);
    } catch (e) {
      return '';
    }
  }
  
  // 直接使用正则替换所有引号和括号
  return String(tag).replace(/["'\[\]{}]/g, '').trim();
}

// 格式化标签，处理带引号和中括号的标签
const formatTags = (tags: string[] | string): string[] => {
  if (!tags) return [];
  
  try {
    // 如果是字符串，先尝试解析JSON
    if (typeof tags === 'string') {
      try {
        // 尝试解析JSON字符串
        const parsed = JSON.parse(tags);
        if (Array.isArray(parsed)) {
          return parsed.map(tag => formatSingleTag(tag));
        }
        // 如果不是数组，可能是逗号分隔的字符串
        return tags.split(',').map(tag => formatSingleTag(tag));
      } catch (e) {
        // 解析失败，按逗号分隔处理
        return tags.split(',').map(tag => formatSingleTag(tag));
      }
    }
    
    // 如果已经是数组
    if (Array.isArray(tags)) {
      return tags.map(tag => formatSingleTag(tag));
    }
  } catch (error) {
    console.error('标签格式化错误:', error);
  }
  
  return [];
}

// 页面加载时获取数据
onMounted(() => {
  loadSubmissions()
  loadAllTags()
  loadAllJobTypes()
})
</script>

<style scoped>
.user-submissions-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.submissions-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.title-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
  white-space: nowrap;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  flex: 1;
  justify-content: flex-start;
}

.reset-button {
  margin-left: auto;
}

.search-input {
  width: 180px;
}

.filter-item {
  width: 120px;
}

.submissions-table {
  margin-top: 16px;
}

.problem-link {
  color: #409EFF;
  text-decoration: none;
}

.problem-link:hover {
  text-decoration: underline;
}

.execution-info, .answer-info {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 13px;
  color: #606266;
}

.execution-info .el-icon, .answer-info .el-icon {
  margin-right: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.tag-item {
  margin: 2px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-option {
  margin-right: 0;
  font-weight: normal;
}

/* 添加导航栏样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.logo {
  margin: 0;
  font-size: 20px;
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.logo:hover {
  color: #66b1ff;
}

.logo h1 {
  margin: 0;
  font-size: 20px;
}

.nav-links {
  display: flex;
  gap: 10px;
}
</style> 