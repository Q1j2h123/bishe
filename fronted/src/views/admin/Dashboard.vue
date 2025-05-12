<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>控制面板</h2>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <el-icon><Refresh /></el-icon> 刷新数据
      </el-button>
    </div>
    
    <!-- 关键指标统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>题目总数</span>
              <el-icon class="icon"><Document /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="stat-value">{{ stats.problemCount || 0 }}</div>
            <div class="stat-description">当前系统中的题目数量</div>
            <div class="stat-distribution">
              <el-tooltip content="选择题" placement="top">
                <div class="distribution-item">
                  <div class="item-label">选择</div>
                  <div class="item-value">{{ stats.problemDistribution?.choice || 0 }}</div>
                </div>
              </el-tooltip>
              <el-tooltip content="判断题" placement="top">
                <div class="distribution-item">
                  <div class="item-label">判断</div>
                  <div class="item-value">{{ stats.problemDistribution?.judge || 0 }}</div>
                </div>
              </el-tooltip>
              <el-tooltip content="编程题" placement="top">
                <div class="distribution-item">
                  <div class="item-label">编程</div>
                  <div class="item-value">{{ stats.problemDistribution?.program || 0 }}</div>
                </div>
              </el-tooltip>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
              <el-icon class="icon"><User /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-description">注册用户数量</div>
            <div class="stat-trends">
              <div class="trend-item">
                <div class="trend-label">今日活跃</div>
                <div class="trend-value">
                  {{ stats.todayActiveUsers || 0 }}
                  <span class="trend-icon up">
                    <el-icon><ArrowUp /></el-icon>
                  </span>
                </div>
              </div>
              <div class="trend-item">
                <div class="trend-label">本周新增</div>
                <div class="trend-value">
                  {{ stats.weeklyNewUsers || 0 }}
                  <span class="trend-icon up">
                    <el-icon><ArrowUp /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>提交次数</span>
              <el-icon class="icon"><Check /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="stat-value">{{ stats.submissionCount || 0 }}</div>
            <div class="stat-description">用户提交总次数</div>
            <div class="progress-bar-container">
              <div class="progress-label">
                通过率: {{ stats.passRate }}%
              </div>
              <el-progress 
                :percentage="stats.passRate"
                :format="() => ''"
                :stroke-width="8"
                status="success"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表和活动记录 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>题目分布</span>
              <el-radio-group v-model="chartView" size="small">
                <el-radio-button label="type">类型</el-radio-button>
                <el-radio-button label="difficulty">难度</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" v-loading="loading">
            <div id="problemDistributionChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>提交趋势 (近7天)</span>
            </div>
          </template>
          <div class="chart-container" v-loading="loading">
            <div id="submissionTrendChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>编程语言使用情况</span>
            </div>
          </template>
          <div class="chart-container" v-loading="loading">
            <div id="languageDistributionChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button size="small" text type="primary" @click="loadMoreActivities">
                查看更多
              </el-button>
            </div>
          </template>
          <div class="activity-list" v-loading="activitiesLoading">
            <el-empty v-if="activities.length === 0" description="暂无活动记录"></el-empty>
            <div v-else v-for="activity in activities" :key="activity.id" class="activity-item">
              <div class="activity-time">{{ formatTime(activity.time) }}</div>
              <div class="activity-content">
                <strong>{{ activity.username }}</strong> 
                {{ getActionText(activity.action) }} 
                {{ activity.targetType }} 
                <strong>{{ activity.targetName }}</strong>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { ref, reactive, onMounted, watch } from 'vue'
import { 
  Document, User, Check, Refresh, 
  ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import { dashboardApi } from '@/api/dashboard'
import type { 
  DashboardStats, ActivityRecord, 
  SubmissionStats 
} from '@/api/dashboard'
import { ElMessage } from 'element-plus'

// 加载状态
const loading = ref(false)
const activitiesLoading = ref(false)

// 图表视图
const chartView = ref('type')

// 统计数据
const stats = ref<DashboardStats>({
  problemCount: 0,
  userCount: 0,
  submissionCount: 0,
  passRate: 0,
  todayActiveUsers: 0,
  weeklyNewUsers: 0,
  problemDistribution: {
    choice: 0,
    judge: 0,
    program: 0
  },
  difficultyDistribution: {
    easy: 0,
    medium: 0,
    hard: 0
  }
})

// 提交统计
const submissionStats = ref<SubmissionStats>({
  totalSubmissions: 0,
  acceptedSubmissions: 0,
  timeDistribution: [],
  languageDistribution: []
})

// 活动记录
const activities = ref<ActivityRecord[]>([])
const activityLimit = ref(10)

// 图表实例
let problemChart: echarts.ECharts | null = null
let submissionChart: echarts.ECharts | null = null
let languageChart: echarts.ECharts | null = null

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 并行请求多个API
    const [statsRes, submissionRes] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getSubmissionStats()
    ])
    
    if (statsRes.code === 0 && statsRes.data) {
      stats.value = statsRes.data
    }
    
    if (submissionRes.code === 0 && submissionRes.data) {
      submissionStats.value = submissionRes.data
    }
    
    // 初始化图表
    initCharts()
  } catch (error) {
    console.error('加载控制面板数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载活动记录
const loadActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await dashboardApi.getRecentActivities(activityLimit.value)
    if (res.code === 0 && res.data) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('加载活动记录失败:', error)
    ElMessage.error('加载活动记录失败，请稍后重试')
  } finally {
    activitiesLoading.value = false
  }
}

// 加载更多活动
const loadMoreActivities = () => {
  activityLimit.value += 10
  loadActivities()
}

// 初始化图表
const initCharts = () => {
  setTimeout(() => {
    initProblemDistributionChart()
    initSubmissionTrendChart()
    initLanguageDistributionChart()
  }, 0)
}

// 初始化题目分布图表
const initProblemDistributionChart = () => {
  const chartDom = document.getElementById('problemDistributionChart')
  if (!chartDom) return
  
  problemChart = echarts.init(chartDom)
  updateProblemChart()
}

// 更新题目分布图表
const updateProblemChart = () => {
  if (!problemChart) return
  
  let data = []
  let title = ''
  
  if (chartView.value === 'type') {
    title = '题目类型分布'
    data = [
      { value: stats.value.problemDistribution.choice, name: '选择题' },
      { value: stats.value.problemDistribution.judge, name: '判断题' },
      { value: stats.value.problemDistribution.program, name: '编程题' }
    ]
  } else {
    title = '题目难度分布'
    data = [
      { value: stats.value.difficultyDistribution.easy, name: '简单' },
      { value: stats.value.difficultyDistribution.medium, name: '中等' },
      { value: stats.value.difficultyDistribution.hard, name: '困难' }
    ]
  }
  
  const option = {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: data.map(item => item.name)
    },
    series: [
      {
        name: '题目分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data,
        animationType: 'scale',
        animationEasing: 'elasticOut'
      }
    ],
    color: chartView.value === 'type' 
      ? ['#67C23A', '#409EFF', '#F56C6C'] 
      : ['#67C23A', '#E6A23C', '#F56C6C']
  }
  
  problemChart.setOption(option)
}

// 初始化提交趋势图表
const initSubmissionTrendChart = () => {
  const chartDom = document.getElementById('submissionTrendChart')
  if (!chartDom) return
  
  submissionChart = echarts.init(chartDom)
  
  const dates = submissionStats.value.timeDistribution.map(item => item.date)
  const counts = submissionStats.value.timeDistribution.map(item => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: dates,
        axisTick: {
          alignWithLabel: true
        }
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '提交数',
        type: 'bar',
        barWidth: '60%',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }
  
  submissionChart.setOption(option)
}

// 初始化语言分布图表
const initLanguageDistributionChart = () => {
  const chartDom = document.getElementById('languageDistributionChart')
  if (!chartDom) return
  
  languageChart = echarts.init(chartDom)
  
  const languages = submissionStats.value.languageDistribution.map(item => item.language)
  const counts = submissionStats.value.languageDistribution.map(item => item.count)
  const total = counts.reduce((a, b) => a + b, 0)
  
  if (languages.length === 0) {
    languageChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center'
      }
    })
    return
  }
  
  const option = {
    title: {
      text: '编程语言使用情况',
      left: 'center',
      top: 0
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        const percentage = ((params.value / total) * 100).toFixed(2)
        return `${params.name}<br/>提交次数: ${params.value}<br/>占比: ${percentage}%`
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: languages,
      type: 'scroll',
      pageIconSize: 12,
      pageTextStyle: {
        color: '#666'
      }
    },
    series: [
      {
        name: '编程语言',
        type: 'pie',
        radius: ['30%', '60%'],
        center: ['50%', '45%'],
        data: submissionStats.value.languageDistribution.map(item => ({
          value: item.count,
          name: item.language
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {d}%',
          fontSize: 12,
          color: '#666'
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 10
        },
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ],
    color: [
      '#409EFF', // 蓝色
      '#67C23A', // 绿色
      '#E6A23C', // 橙色
      '#F56C6C', // 红色
      '#909399', // 灰色
      '#36CBCB', // 青色
      '#FFA500', // 橙色
      '#FF69B4', // 粉色
      '#BA55D3', // 紫色
      '#CD853F'  // 棕色
    ]
  }
  
  languageChart.setOption(option)
}

// 监听图表视图变化
watch(chartView, () => {
  updateProblemChart()
})

// 刷新数据
const refreshData = () => {
  loadData()
  loadActivities()
}

// 日期格式化
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  const now = new Date()
  
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 一周内
  const daysAgo = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))
  if (daysAgo < 7) {
    return `${daysAgo}天前`
  }
  
  // 其他情况
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 获取操作文本
const getActionText = (action: string) => {
  const actionMap: Record<string, string> = {
    'add': '添加了',
    'update': '更新了',
    'delete': '删除了',
    'submit': '提交了',
    'login': '登录了',
    'register': '注册了'
  }
  return actionMap[action] || action
}

// 计算百分比
const calculatePercentage = (part: number, total: number) => {
  if (!total) return 0
  return Math.round((part / total) * 100)
}

// 监听窗口大小变化，重绘图表
window.addEventListener('resize', () => {
  problemChart?.resize()
  submissionChart?.resize()
  languageChart?.resize()
})

// 组件挂载时加载数据
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon {
  font-size: 20px;
  color: #409EFF;
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-distribution {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
}

.distribution-item {
  text-align: center;
  cursor: pointer;
}

.item-label {
  font-size: 12px;
  color: #909399;
}

.item-value {
  font-size: 16px;
  font-weight: bold;
  color: #606266;
}

.stat-trends {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
}

.trend-item {
  text-align: center;
}

.trend-label {
  font-size: 12px;
  color: #909399;
}

.trend-value {
  font-size: 16px;
  font-weight: bold;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-icon {
  font-size: 12px;
  margin-left: 5px;
}

.trend-icon.up {
  color: #67C23A;
}

.trend-icon.down {
  color: #F56C6C;
}

.progress-bar-container {
  margin-top: 15px;
}

.progress-label {
  text-align: left;
  font-size: 14px;
  margin-bottom: 5px;
  color: #606266;
}

.chart-row {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.chart {
  height: 100%;
  width: 100%;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-time {
  width: 100px;
  color: #909399;
  font-size: 13px;
}

.activity-content {
  flex: 1;
  color: #606266;
}
</style>
