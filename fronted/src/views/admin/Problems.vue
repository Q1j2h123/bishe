<template>
  <div class="problem-manage">
    <div class="page-header">
      <h2>题目管理</h2>
      <div class="operation-btns">
        <el-dropdown @command="handleAddProblem" split-button type="primary">
          <span>添加题目</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="CHOICE">选择题</el-dropdown-item>
              <el-dropdown-item command="JUDGE">判断题</el-dropdown-item>
              <el-dropdown-item command="PROGRAM">编程题</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 全局条件显示 -->
    <div v-if="hasActiveFilters" class="global-filter-container">
      <el-alert
        :title="getActiveFilterTitle()"
        type="success"
        :closable="false"
        show-icon
      />
    </div>
    
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
    
    <el-card class="problem-list-card">
      <el-table 
        :data="problemList" 
        stripe 
        border 
        v-loading="loading"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="题目标题" min-width="180" show-overflow-tooltip />
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
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            <div class="time-display">
              <div>{{ formatDate(row.createTime) }}</div>
              <div class="time-ago">{{ getTimeAgo(row.createTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)">
              删除
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
    
    <!-- 删除确认框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除题目 "{{ selectedProblem?.title }}" 吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleting">
            确定删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { problemApi, type ProblemVO, type ProblemQueryRequest } from '@/api/problem'

interface FilterItem {
  key: string;
  label: string;
  value: string | string[];
}

const router = useRouter()
const loading = ref(false)
const deleting = ref(false)
const problemList = ref<ProblemVO[]>([])
const total = ref(0)
const deleteDialogVisible = ref(false)
const selectedProblem = ref<ProblemVO | null>(null)
const activeFilters = ref<FilterItem[]>([])

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

// 根据题目难度返回行类名
const getRowClassName = ({ row }: { row: ProblemVO }) => {
  // 将后端返回的EASY, MEDIUM, HARD映射到对应的样式类
  if (row.difficulty === '简单' || row.difficulty === 'EASY') return 'easy-row'
  if (row.difficulty === '中等' || row.difficulty === 'MEDIUM') return 'medium-row'
  if (row.difficulty === '困难' || row.difficulty === 'HARD') return 'hard-row'
  return ''
}

// 格式化日期为年月日
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return '-' // 无效日期
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } catch {
    return '-'
  }
}

// 计算相对时间（几天前等）
const getTimeAgo = (dateStr?: string) => {
  if (!dateStr) return ''
  
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return '' // 无效日期
    
    const now = new Date()
    const diffTime = Math.abs(now.getTime() - date.getTime())
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) {
      const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
      if (diffHours === 0) {
        const diffMinutes = Math.floor(diffTime / (1000 * 60))
        return `${diffMinutes} 分钟前`
      }
      return `${diffHours} 小时前`
    } else if (diffDays < 30) {
      return `${diffDays} 天前`
    } else if (diffDays < 365) {
      const diffMonths = Math.floor(diffDays / 30)
      return `${diffMonths} 个月前`
    } else {
      const diffYears = Math.floor(diffDays / 365)
      return `${diffYears} 年前`
    }
  } catch {
    return ''
  }
}

// 判断是否有活跃的过滤条件
const hasActiveFilters = computed(() => {
  return Boolean(
    queryParams.searchText || 
    queryParams.type || 
    queryParams.difficulty || 
    queryParams.jobType || 
    (queryParams.tags && queryParams.tags.length > 0) || 
    queryParams.status
  )
})

// 获取当前活跃的过滤条件标题
const getActiveFilterTitle = () => {
  const parts: string[] = []
  
  if (queryParams.type) {
    parts.push(`题目类型: ${getTypeText(queryParams.type)}`)
  }
  
  if (queryParams.difficulty) {
    parts.push(`难度: ${queryParams.difficulty}`)
  }
  
  if (queryParams.jobType) {
    parts.push(`岗位类型: ${queryParams.jobType}`)
  }
  
  if (queryParams.tags && queryParams.tags.length) {
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

// 处理普通参数变化
const handleParamChange = (label: string, value: any) => {
  if (!value) return
  
  // 更新查询参数已在v-model中进行
  
  // 显示选中内容
  ElMessage.success(`已选择${label}: ${value}`)
}

// 处理标签参数变化
const handleTagsChange = (tags: string[]) => {
  if (!tags.length) return
  
  // 显示选中内容
  ElMessage.success(`已选择${tags.length}个标签: ${tags.join(', ')}`)
}

// 加载题目列表
const loadProblemList = async () => {
  loading.value = true
  try {
    // 过滤掉空值参数，避免不必要的查询条件
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
      problemList.value = (res.data.records || []).map((item: any) => {
        const problem = { ...item } as ProblemVO & { status?: string }
        
        // 确保tags始终是数组
        if (!problem.tags) problem.tags = []
        
        // 不需要标准化难度值，由getDifficultyText处理显示
        // 统一使用英文标准值: EASY, MEDIUM, HARD
        
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
    console.error('加载题目列表失败:', error)
    ElMessage.error('加载题目列表失败，请检查网络连接或刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.current = 1
  loadProblemList()
}

// 重置查询
const resetQuery = () => {
  queryParams.searchText = ''
  queryParams.type = ''
  queryParams.difficulty = ''
  queryParams.jobType = ''
  queryParams.tags = []
  queryParams.status = ''
  queryParams.current = 1
  loadProblemList()
  ElMessage.info('已重置所有筛选条件')
}

// 分页处理
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  loadProblemList()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadProblemList()
}

// 添加题目
const handleAddProblem = (type: string) => {
  const typeMapping: Record<string, string> = {
    'CHOICE': 'choice',
    'JUDGE': 'judge',
    'PROGRAM': 'program'
  }
  const routeType = typeMapping[type] || type.toLowerCase()
  router.push(`/admin/problem/add/${routeType}`)
}

// 编辑题目
const handleEdit = (row: ProblemVO) => {
  const typeMapping: Record<string, string> = {
    'CHOICE': 'choice',
    'JUDGE': 'judge',
    'PROGRAM': 'program'
  }
  const routeType = typeMapping[row.type] || row.type.toLowerCase()
  router.push(`/admin/problem/edit/${routeType}/${row.id}`)
}

// 删除题目
const handleDelete = (row: ProblemVO) => {
  selectedProblem.value = row
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!selectedProblem.value) return
  
  deleting.value = true
  const problem = selectedProblem.value
  const id = problem.id as number
  
  try {
    let res = undefined
    switch (problem.type) {
      case 'CHOICE':
        res = await problemApi.deleteChoiceProblem(id)
        break
      case 'JUDGE':
        res = await problemApi.deleteJudgeProblem(id)
        break
      case 'PROGRAM':
        res = await problemApi.deleteprogramProblem(id)
        break
    }
    
    if (res && res.code === 0) {
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      loadProblemList()
    } else if (res) {
      ElMessage.error(res.message || '删除失败')
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除题目失败:', error)
    ElMessage.error('删除题目失败')
  } finally {
    deleting.value = false
    deleteDialogVisible.value = false
  }
}

// 加载初始数据
onMounted(() => {
  loadProblemList()
})
</script>

<style scoped>
.problem-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.global-filter-container {
  margin-bottom: 15px;
}

.search-card {
  margin-bottom: 20px;
}

.select-prefix-tag {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.time-display {
  display: flex;
  flex-direction: column;
}

.time-ago {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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
