<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { 
  problemApi, 
  getProblemsWithStatus, 
  type ProblemVO, 
  type ProblemWithStatusVO, 
  type ProblemQueryRequest,
  type ProblemQueryRequestExt
} from '@/api/problem'
import { problemStatusApi } from '@/api/problem-status'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 题目列表和加载状态
const loading = ref(false)
const problemList = ref<ProblemWithStatusVO[]>([])
const total = ref(0)
const allTags = ref<string[]>([]) // 所有标签列表

// 下拉框选项定义
const problemTypes = [
  { value: 'CHOICE', label: '选择题' },
  { value: 'JUDGE', label: '判断题' },
  { value: 'PROGRAM', label: '编程题' }
]

const positionTypes = [
  { value: '全栈开发', label: '全栈开发' },
  { value: '前端开发', label: '前端开发' },
  { value: '后端开发', label: '后端开发' },
  { value: '数据库', label: '数据库' },
  { value: '算法', label: '算法' },
  { value: '系统设计', label: '系统设计' }
]

const difficultyOptions = [
  { value: 'EASY', label: '简单' },
  { value: 'MEDIUM', label: '中等' },
  { value: 'HARD', label: '困难' }
]

const statusOptions = [
  { value: 'UNSOLVED', label: '未解决' },
  { value: 'ATTEMPTED', label: '尝试过' },
  { value: 'SOLVED', label: '已解决' }
]

// 查询参数
const queryParams = reactive<ProblemQueryRequest>({
  current: 1,
  pageSize: 10,
  searchText: '',
  type: '',
  difficulty: '',
  jobType: '',
  tags: [], // 将在处理时转换为单个标签
  status: '',
  tag: '' // 添加单个标签字段
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

// 监听路由参数变化，当有时间戳参数或强制刷新参数时强制刷新
watch(() => route.query, (newQuery, oldQuery) => {
  console.log('路由参数变化:', newQuery);
  
  // 检查是否有强制刷新标记或时间戳
  const shouldForceRefresh = 
    newQuery.forceRefresh === 'true' || 
    newQuery.t !== oldQuery.t || 
    !oldQuery.t;  // 首次加载
  
  if (shouldForceRefresh) {
    console.log('检测到强制刷新参数，正在执行彻底刷新');
    
    // 彻底清除所有状态相关缓存
    try {
      localStorage.removeItem('userProblemStatuses');
      localStorage.removeItem('statusCacheTime');
      localStorage.removeItem('problem_status_cache');
      console.log('已清除所有状态缓存');
    } catch (e) {
      console.error('清除缓存失败:', e);
    }
    
    // 重置页码到第一页
    queryParams.current = 1;
    
    // 强制重新加载题目列表，包括状态
    loadProblemList(true);
  } else {
    // 仅当查询参数发生实质性变化时才重新加载
    const hasQueryChange = 
      newQuery.searchText !== oldQuery.searchText ||
      newQuery.type !== oldQuery.type ||
      newQuery.difficulty !== oldQuery.difficulty ||
      newQuery.jobType !== oldQuery.jobType ||
      newQuery.status !== oldQuery.status ||
      JSON.stringify(newQuery.tags) !== JSON.stringify(oldQuery.tags);
      
    if (hasQueryChange) {
      console.log('查询参数变化，重新加载列表');
      loadProblemList(false);
    }
  }
}, { deep: true });

// 页面加载时触发一次加载
onMounted(() => {
  console.log('Problems组件已挂载，初始化加载题目列表和标签');
  
  // 从路由参数中初始化查询参数
  if (route.query.current) {
    queryParams.current = Number(route.query.current);
  }
  
  if (route.query.pageSize) {
    queryParams.pageSize = Number(route.query.pageSize);
  }
  
  if (route.query.type) {
    queryParams.type = String(route.query.type);
  }
  
  if (route.query.difficulty) {
    queryParams.difficulty = String(route.query.difficulty);
  }
  
  // 处理状态参数
  if (route.query.status) {
    queryParams.status = String(route.query.status);
    console.log('初始化状态参数:', queryParams.status);
  } else if (route.query.userStatus) {
    // 如果只有userStatus参数，使用它作为status参数
    queryParams.status = String(route.query.userStatus);
    console.log('从userStatus初始化状态参数:', queryParams.status);
    
    // 同步到URL
    const initialQuery = { ...route.query, status: queryParams.status };
    router.replace({
      path: '/problems',
      query: initialQuery
    });
  }
  
  if (route.query.searchText) {
    queryParams.searchText = String(route.query.searchText);
  }
  
  // 处理单标签
  if (route.query.tag) {
    queryParams.tag = String(route.query.tag);
  }
  
  // 打印日志
  console.log('初始化查询参数:', queryParams);
  
  // 检查强制刷新参数
  const forceRefresh = route.query.forceRefresh === 'true';
  console.log('初始强制刷新参数:', forceRefresh);
  
  // 加载标签和题目列表
  loadAllTags();
  loadProblemList(forceRefresh);
  
  // 设置路由监听
  setupRouteWatcher();
});

// 处理查询参数变化
const handleParamChange = (label: string, value: any) => {
  console.log(`已选择${label}: ${value}`);
  queryParams.current = 1; // 重置到第一页
  
  // 特殊处理状态字段
  const query: Record<string, any> = {
    ...route.query,
    [label.toLowerCase().replace(/\s/g, '')]: value || undefined,
    current: '1' // 重置页码
  };
  
  // 如果是状态字段，同时也设置userStatus参数
  if (label.toLowerCase() === 'status') {
    query.userStatus = value || undefined;
    console.log('添加userStatus查询参数:', value);
  }
  
  // 不要在这里直接调用loadProblemList
  // 路由监听器会检测到参数变化并自动加载
  router.push({
    path: '/problems',
    query
  });
}

// 处理标签参数变化（修改为单选）
const handleTagChange = (tag: string) => {
  console.log(`已选择标签: ${tag}`);
  queryParams.current = 1; // 重置到第一页
  queryParams.tag = tag; // 设置单个标签
  
  // 从现有查询参数复制，保留其他筛选条件
  const newQuery: Record<string, any> = { ...route.query };
  
  // 更新标签相关参数
  newQuery.tag = tag || undefined; // 使用单个标签
  newQuery.tags = undefined; // 不再使用多标签数组
  newQuery.current = '1'; // 重置页码
  
  // 确保状态筛选参数被保留
  if (queryParams.status) {
    newQuery.status = queryParams.status;
    newQuery.userStatus = queryParams.status;
    console.log('标签变化，保留状态筛选参数:', queryParams.status);
  }
  
  // 直接触发查询
  router.push({
    path: '/problems',
    query: newQuery
  });
}

// 处理搜索框输入变化
const handleSearchChange = () => {
  queryParams.current = 1; // 重置到第一页
  
  // 从现有查询参数复制，保留其他筛选条件
  const newQuery: Record<string, any> = { ...route.query };
  
  // 更新搜索相关参数
  newQuery.searchText = queryParams.searchText || undefined;
  newQuery.current = '1'; // 重置页码
  
  // 确保状态筛选参数被保留
  if (queryParams.status) {
    newQuery.status = queryParams.status;
    newQuery.userStatus = queryParams.status;
    console.log('搜索变化，保留状态筛选参数:', queryParams.status);
  }
  
  // 通过路由变化触发加载
  router.push({
    path: '/problems',
    query: newQuery
  });
}

// 执行查询
const handleQuery = () => {
  queryParams.current = 1; // 重置到第一页
  
  // 使用路由参数触发加载
  const query: Record<string, any> = {};
  
  if (queryParams.searchText) query.searchText = queryParams.searchText;
  if (queryParams.type) query.type = queryParams.type;
  if (queryParams.difficulty) query.difficulty = queryParams.difficulty;
  if (queryParams.jobType) query.jobType = queryParams.jobType;
  if (queryParams.status) {
    query.status = queryParams.status;
    // 同时设置userStatus参数
    query.userStatus = queryParams.status;
  }
  if (queryParams.tag) query.tag = queryParams.tag;
  
  query.current = '1'; // 重置页码
  
  router.push({
    path: '/problems',
    query
  });
}

// 重置查询参数
const resetQuery = () => {
  Object.assign(queryParams, {
    current: 1,
    searchText: '',
    type: '',
    difficulty: '',
    jobType: '',
    tags: [],
    status: ''
  });
  
  // 清除查询参数，保留时间戳
  const { t } = route.query;
  router.push({
    path: '/problems',
    query: { 
      t, 
      forceRefresh: 'true' // 强制刷新，确保清除缓存
    }
  });
  
  // 手动清除筛选缓存
  try {
    localStorage.removeItem('userProblemStatuses');
    localStorage.removeItem('statusCacheTime');
    localStorage.removeItem('problem_status_cache');
    localStorage.removeItem('currentProblemPage');
    console.log('已手动清除所有缓存');
  } catch (e) {
    console.error('清除缓存失败:', e);
  }
}

// 处理分页变化
const handlePageChange = (page: number) => {
  console.log('页码变化:', page);
  
  // 更新当前页码参数
  queryParams.current = page;
  console.log('更新页码后的查询参数:', JSON.stringify(queryParams));
  
  // 强制清除本地存储的前一页数据
  try {
    localStorage.removeItem('currentProblemPage');
    console.log('清除了当前页面缓存');
  } catch (e) {
    console.error('清除缓存失败:', e);
  }
  
  // 从route.query复制所有查询参数，保留当前筛选条件
  const newQuery: Record<string, any> = { ...route.query };
  
  // 更新或添加分页相关参数
  newQuery.current = String(page);
  newQuery.t = Date.now().toString(); // 添加时间戳，确保每次都是新的URL
  newQuery.forceRefresh = 'true'; // 始终强制刷新，确保获取正确数据
  
  // 确保状态筛选参数被保留
  if (queryParams.status) {
    newQuery.status = queryParams.status;
    newQuery.userStatus = queryParams.status; // 同时设置userStatus确保前端筛选正常工作
    console.log('保留状态筛选参数:', queryParams.status);
  }
  
  // 添加强制刷新标志以确保状态正确
  router.push({
    path: '/problems',
    query: newQuery
  });
}

// 处理页大小变化
const handleSizeChange = (size: number) => {
  console.log('每页数量变化:', size);
  queryParams.pageSize = size;
  queryParams.current = 1; // 重置到第一页
  
  // 从route.query复制所有查询参数，保留当前筛选条件
  const newQuery: Record<string, any> = { ...route.query };
  
  // 更新或添加分页相关参数
  newQuery.pageSize = String(size);
  newQuery.current = '1';
  newQuery.t = Date.now().toString(); // 时间戳
  newQuery.forceRefresh = 'true'; // 强制刷新
  
  // 确保状态筛选参数被保留
  if (queryParams.status) {
    newQuery.status = queryParams.status;
    newQuery.userStatus = queryParams.status; // 同时设置userStatus确保前端筛选正常工作
    console.log('保留状态筛选参数:', queryParams.status);
  }
  
  // 通过路由变化触发重新加载
  router.push({
    path: '/problems',
    query: newQuery
  });
}

// 加载题目列表
const loadProblemList = async (forceRefresh = false) => {
  console.log('开始加载题目列表, forceRefresh:', forceRefresh);
  console.log('当前查询参数:', JSON.stringify(queryParams));
  loading.value = true;
  
  try {
    // 确保当前页码正确设置
    const currentPage = Number(route.query.current) || queryParams.current || 1;
    queryParams.current = currentPage;
    
    // 清晰记录当前页码和每页数量
    console.log('加载参数 - 页码:', queryParams.current, '每页数量:', queryParams.pageSize);
    
    // 获取题目列表（带状态）
    // 使用类型断言绕过TypeScript类型检查
    const params: any = {
      ...queryParams,
      forceRefresh: true // 始终强制刷新，确保每次翻页都获取新数据
    };
    
    // 重要修改：将status参数转换为后端API期望的userStatus字段
    if (params.status) {
      params.userStatus = params.status; // 添加userStatus作为实际状态筛选字段
      console.log('状态查询参数已转换:', params.status, '->', params.userStatus);
    }
    
    console.log('最终请求参数:', JSON.stringify(params));
    const response = await getProblemsWithStatus(params);
    console.log('获取题目带状态列表响应:', response);
    
    if (response.code === 0) {
      // 打印详细日志
      console.log('获取到题目总数:', response.data.total);
      console.log('当前页码:', queryParams.current);
      console.log('每页数量:', queryParams.pageSize);
      
      // 清空当前列表，然后重新设置
      problemList.value = [];
      problemList.value = response.data.records || [];
      total.value = response.data.total || 0;
      
      // 遍历每个题目，记录详细信息
      problemList.value.forEach((problem, index) => {
        console.log('处理题目Item:', problem);
        const { id, title, type, difficulty, userStatus } = problem;
        console.log(`题目 ${id} [${type}] "${title}" 难度:${difficulty} 状态:${userStatus || 'UNSOLVED'}`);
        
        // 添加显示序号
        const currentPage = queryParams.current || 1;
        const pageSize = queryParams.pageSize || 10;
        problem.displayId = index + 1 + (currentPage - 1) * pageSize;
      });
      
      // 在页面加载成功后更新路由中的页码，以便于刷新后保持在同一页
      const currentPage = queryParams.current || 1;
      const currentSize = queryParams.pageSize || 10;
      
      // 仅当路由中的页码与当前页码不同时更新
      if (route.query.current !== String(currentPage) || route.query.pageSize !== String(currentSize)) {
        console.log('更新路由页码参数:', currentPage, currentSize);
        router.replace({
          path: '/problems',
          query: {
            ...route.query,
            current: String(currentPage),
            pageSize: String(currentSize)
          }
        });
      }
    } else {
      ElMessage.error('获取题目列表失败');
    }
  } catch (error) {
    console.error('加载题目列表异常:', error);
    ElMessage.error('加载题目列表失败');
  } finally {
    loading.value = false;
  }
};

// 增强的路由监听函数
const setupRouteWatcher = () => {
  console.log('设置路由监听器');
  
  // 在组件挂载后的onMounted中已经调用了初始化加载，这里不需要重复
  // 但我们需要监听路由变化
  watch(
    () => route.query,
    (newQuery, oldQuery) => {
      console.log('路由查询参数变化:', {
        new: newQuery,
        old: oldQuery
      });
      
      // 判断是否需要强制刷新
      const forceRefresh = newQuery.forceRefresh === 'true';
      console.log('forceRefresh 参数值:', newQuery.forceRefresh, '转换后:', forceRefresh);
      
      // 检查页码和筛选参数是否变化
      const currentChanged = newQuery.current !== oldQuery.current;
      const pageSizeChanged = newQuery.pageSize !== oldQuery.pageSize;
      const filterChanged = newQuery.type !== oldQuery.type || 
                          newQuery.difficulty !== oldQuery.difficulty || 
                          newQuery.status !== oldQuery.status ||
                          newQuery.userStatus !== oldQuery.userStatus ||
                          newQuery.searchText !== oldQuery.searchText ||
                          newQuery.tag !== oldQuery.tag;
      
      console.log('参数变化情况:', {
        currentChanged,
        pageSizeChanged,
        filterChanged
      });
      
      if (currentChanged || pageSizeChanged || filterChanged || forceRefresh) {
        console.log('需要重新加载数据');
        // 更新本地查询参数
        if (newQuery.current) {
          queryParams.current = Number(newQuery.current);
          console.log('从路由参数更新页码:', queryParams.current);
        }
        
        if (newQuery.pageSize) {
          queryParams.pageSize = Number(newQuery.pageSize);
        }
        
        if (newQuery.type) {
          queryParams.type = String(newQuery.type);
        }
        
        if (newQuery.difficulty) {
          queryParams.difficulty = String(newQuery.difficulty);
        }
        
        if (newQuery.status) {
          queryParams.status = String(newQuery.status);
          console.log('从路由参数更新状态:', queryParams.status);
          
          // 确保userStatus和status保持同步
          if (newQuery.userStatus !== newQuery.status) {
            console.log('状态参数不一致，同步userStatus参数');
            
            // 更新URL中的userStatus参数，但不触发新的导航
            const updatedQuery = { ...route.query, userStatus: newQuery.status };
            router.replace({
              path: '/problems',
              query: updatedQuery
            });
          }
        }
        
        // 处理userStatus参数，可能来自前端筛选
        if (newQuery.userStatus && !newQuery.status) {
          queryParams.status = String(newQuery.userStatus);
          console.log('从userStatus参数更新状态:', queryParams.status);
          
          // 同步status参数
          const updatedQuery = { ...route.query, status: newQuery.userStatus };
          router.replace({
            path: '/problems',
            query: updatedQuery
          });
        }
        
        if (newQuery.searchText) {
          queryParams.searchText = String(newQuery.searchText);
        }
        
        // 处理单标签
        if (newQuery.tag) {
          queryParams.tag = String(newQuery.tag);
        } else {
          queryParams.tag = '';
        }
        
        // 清除任何可能的页面缓存
        try {
          localStorage.removeItem('currentProblemPage');
        } catch (e) {
          console.error('清除页面缓存失败:', e);
        }
        
        // 调用加载函数，显式传递强制刷新参数
        loadProblemList(true); // 始终强制刷新
      }
    },
    {
      deep: true, // 深度监听对象属性变化
      immediate: false // 不立即触发，让onMounted中的初始加载处理
    }
  );
  
  // 监听浏览器回退/前进操作
  window.addEventListener('popstate', () => {
    console.log('检测到浏览器导航操作（前进/后退），强制刷新数据');
    // 使用参数 true 强制刷新
    loadProblemList(true);
  });
};

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

// 标签选项
const tagOptions = ref<string[]>([])

// 标签数据加载
const loadTags = async () => {
  try {
    const res = await problemApi.getAllTags()
    if (res.code === 0 && res.data) {
      // 清理标签数据，确保没有引号和括号
      tagOptions.value = res.data.map(tag => {
        // 清理标签，去除可能的引号和括号
        return String(tag).replace(/["'\[\]{}]/g, '').trim()
      })
    }
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('标签加载失败')
  }
}

// 格式化状态
const formatStatus = (status: string | undefined): string => {
  console.log('格式化状态:', status);
  if (!status) {
    return '未解决'; // 默认为未解决
  }
  
  switch (status) {
    case 'SOLVED':
      return '已解决'
    case 'ATTEMPTED':
      return '尝试过'
    case 'UNSOLVED':
    default:
      return '未解决'
  }
}

// 获取状态标签类型
const getStatusType = (status: string | undefined): string => {
  console.log('获取状态类型:', status);
  if (!status) {
    return 'info'; // 无状态时使用info类型
  }
  
  switch (status) {
    case 'SOLVED':
      return 'success'
    case 'ATTEMPTED':
      return 'warning'
    case 'UNSOLVED':
    default:
      return 'info'
  }
}

// 加载所有标签
const loadAllTags = async () => {
  try {
    const response = await problemApi.getAllTags();
    
    if (response.code === 0 && response.data) {
      // 清理标签，去除双引号和括号
      allTags.value = response.data.map(tag => {
        return String(tag).replace(/["'\[\]{}]/g, '').trim();
      });
      console.log('获取到的所有标签:', allTags.value);
    } else {
      ElMessage.warning('获取标签列表失败');
    }
  } catch (error) {
    console.error('加载标签异常:', error);
  }
};

// 进入题目详情页
const handleViewProblem = (row: ProblemVO) => {
  // 根据题目类型导航到不同的详情页
  if (row.type === 'CHOICE') {
    router.push(`/problem/choice/${row.id}`);
  } else if (row.type === 'JUDGE') {
    router.push(`/problem/judge/${row.id}`);
  } else if (row.type === 'PROGRAM') {
    router.push(`/problem/program/${row.id}`);
  } else {
    // 默认导航到通用详情页
    router.push(`/problem/${row.id}`);
  }
};

// 只保留要在defineExpose中暴露的方法
defineExpose({
  loadProblemList
})
</script>

<template>
  <div class="problems-container">
    <el-header class="header">
      <div class="logo" @click="router.push('/home')">
        <h1>面试刷题平台</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text type="primary">题目列表</el-button>
        <el-button text @click="router.push('/my-submissions')">提交记录</el-button>
        <el-button text @click="router.push('/user-center')">个人中心</el-button>
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
      <el-card class="filter-card">
        <el-form :model="queryParams" inline>
          <el-form-item label="关键词">
            <el-input v-model="queryParams.searchText" placeholder="题目标题/内容关键词" clearable @keyup.enter="handleSearchChange" @clear="handleSearchChange"></el-input>
          </el-form-item>
          
          <el-form-item label="题目类型">
            <el-select v-model="queryParams.type" placeholder="请选择" clearable @change="handleParamChange('type', queryParams.type)">
              <el-option v-for="item in problemTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="岗位类型">
            <el-select v-model="queryParams.jobType" placeholder="请选择" clearable @change="handleParamChange('jobType', queryParams.jobType)">
              <el-option v-for="item in positionTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="标签">
            <el-select v-model="queryParams.tag" placeholder="选择标签" clearable @change="handleTagChange">
              <el-option v-for="tag in allTags" :key="tag" :label="tag" :value="tag"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="难度">
            <el-select v-model="queryParams.difficulty" placeholder="请选择" clearable @change="handleParamChange('difficulty', queryParams.difficulty)">
              <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择" clearable @change="handleParamChange('status', queryParams.status)">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button @click="resetQuery">重置筛选</el-button>
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
          <el-table-column prop="tags" label="标签" min-width="200">
            <template #default="{ row }">
              <div v-if="row.tags && row.tags.length > 0" class="tag-list">
                <el-tag
                  v-for="tag in row.tags"
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
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row.userStatus)" 
                effect="plain"
              >
                {{ formatStatus(row.userStatus) }}
                <span v-if="!row.userStatus" class="debug-info">[未登录]</span>
              </el-tag>
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
            @current-change="handlePageChange"
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

.logo {
  cursor: pointer;
  transition: color 0.3s;
}

.logo:hover {
  color: #66b1ff;
}

.logo h1 {
  margin: 0;
  font-size: 20px;
  color: #409EFF;
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

.filter-card {
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
  margin: 2px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-option {
  margin-right: 0;
  font-weight: normal;
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

/* 调试信息样式 */
.debug-info {
  color: #ff4949;
  font-size: 12px;
  margin-left: 4px;
}
</style> 