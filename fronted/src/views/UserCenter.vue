<template>
  <div class="user-center-container">
    <!-- 顶部用户信息区域 -->
    <el-card class="user-info-card">
      <div class="user-info-content">
        <div class="user-avatar">
          <el-avatar 
            :size="80" 
            :src="userInfo.avatar ? userInfo.avatar + '?t=' + new Date().getTime() : defaultAvatar"
            @error="() => { console.error('头像加载失败:', userInfo.avatar); userInfo.avatar = defaultAvatar; }"
          ></el-avatar>
        </div>
        <div class="user-details">
          <h2 class="user-name">{{ userInfo.username || '用户名' }}</h2>
          <p class="user-profile">{{ userInfo.profile || '这个人很懒，还没有写简介' }}</p>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalSolved || 0 }}</div>
              <div class="stat-label">已解决</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalSubmissions || 0 }}</div>
              <div class="stat-label">总提交数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.correctRate || '0%' }}</div>
              <div class="stat-label">正确率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.streak || 0 }}</div>
              <div class="stat-label">连续天数</div>
            </div>
          </div>
        </div>
        <div class="user-actions">
          <el-button type="primary" plain @click="showEditProfile">编辑资料</el-button>
        </div>
      </div>
    </el-card>

    <!-- 编辑个人资料对话框 -->
    <el-dialog
      v-model="profileDialogVisible"
      title="编辑个人资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            action="/api/user/upload/avatar"
            :headers="{
              Authorization: `Bearer ${userStore.token}`
            }"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="个人简介" prop="profile">
          <el-input
            v-model="editForm.profile"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
          ></el-input>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input v-model="userInfo.createTime" disabled></el-input>
        </el-form-item>
        <el-form-item label="用户角色">
          <el-input v-model="userInfo.userRole" disabled></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditProfile">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 中部数据统计区域 -->
    <div class="statistics-area">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
          <el-card class="statistic-card">
            <template #header>
              <div class="card-header">
                <h3>总体进度</h3>
              </div>
            </template>
            <div class="progress-chart" id="overall-progress-chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
          <el-card class="statistic-card">
            <template #header>
              <div class="card-header">
                <h3>题型分布</h3>
              </div>
            </template>
            <div class="type-chart" id="problem-type-chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
          <el-card class="statistic-card">
            <template #header>
              <div class="card-header">
                <h3>难度分布</h3>
              </div>
            </template>
            <div class="difficulty-chart" id="difficulty-chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部标签页区域 -->
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="错题本" name="errorBook">
          <div class="error-book-container">
            <div class="filter-bar">
              <el-form :inline="true" :model="errorFilters" class="filter-form">
                <el-form-item label="题目类型">
                  <el-select v-model="errorFilters.type" placeholder="全部">
                    <el-option label="全部" value=""></el-option>
                    <el-option label="选择题" value="CHOICE"></el-option>
                    <el-option label="判断题" value="JUDGE"></el-option>
                    <el-option label="编程题" value="PROGRAM"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="难度">
                  <el-select v-model="errorFilters.difficulty" placeholder="全部">
                    <el-option label="全部" value=""></el-option>
                    <el-option label="简单" value="简单"></el-option>
                    <el-option label="中等" value="中等"></el-option>
                    <el-option label="困难" value="困难"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadErrorProblems">筛选</el-button>
                  <el-button @click="resetFilters">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <div v-if="loadingErrors" class="loading-container">
              <el-skeleton :rows="5" animated></el-skeleton>
            </div>
            <div v-else-if="errorProblems.length === 0" class="empty-container">
              <el-empty description="暂无错题记录"></el-empty>
            </div>
            <el-table v-else :data="errorProblems" style="width: 100%" border>
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column prop="title" label="题目" min-width="200">
                <template #default="scope">
                  <router-link :to="`/problem/${scope.row.type.toLowerCase()}/${scope.row.id}`" class="problem-link">
                    {{ scope.row.title }}
                  </router-link>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="100">
                <template #default="scope">
                  <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeText(scope.row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="difficulty" label="难度" width="100">
                <template #default="scope">
                  <el-tag :type="getDifficultyType(scope.row.difficulty)">{{ scope.row.difficulty }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastErrorTime" label="最近错误时间" width="180"></el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="goToProblem(scope.row)">重新做题</el-button>
                  <el-button type="success" plain size="small" @click="markAsMastered(scope.row.id)">
                    标记已掌握
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="errorPage.current"
                :page-size="errorPage.size"
                layout="prev, pager, next, total"
                :total="errorPage.total"
                @current-change="handleErrorPageChange"
              ></el-pagination>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { userStatsApi } from '@/api/userStats'
import type { ErrorProblem, UserStatistics } from '@/api/userStats'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadProps } from 'element-plus'

// 注册echarts组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  PieChart,
  BarChart,
  CanvasRenderer
])

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户基本信息
const userInfo = ref({
  username: '',
  profile: '',
  avatar: '',
  createTime: '',
  userRole: ''
})

// 统计数据
const statistics = ref<UserStatistics>({
  totalSolved: 0,
  totalSubmissions: 0,
  correctRate: '0%',
  streak: 0,
  typeCounts: {
    choice: { total: 0, solved: 0 },
    judge: { total: 0, solved: 0 },
    program: { total: 0, solved: 0 }
  },
  difficultyCounts: {
    easy: { total: 0, solved: 0 },
    medium: { total: 0, solved: 0 },
    hard: { total: 0, solved: 0 }
  }
})

// 标签页控制
const activeTab = ref('errorBook')

// 错题本数据和分页
const errorProblems = ref<ErrorProblem[]>([])
const loadingErrors = ref(false)
const errorPage = ref({
  current: 1,
  size: 10,
  total: 0
})

// 错题本筛选条件
const errorFilters = ref({
  type: '',
  difficulty: '',
  keyword: ''
})

// 编辑个人资料相关
const profileDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref({
  username: '',
  profile: '',
  avatar: ''
})

const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  profile: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 获取用户信息
const loadUserInfo = async () => {
  try {
    if (!userStore.token) {
      router.push('/login')
      return
    }

    // 从userStore中获取用户信息
    const user = userStore.currentUser
    // 格式化时间
    const formatTime = (timeStr: string) => {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      })
    }
    
    userInfo.value = {
      username: user?.userName || '用户',
      profile: user?.userProfile || '',
      avatar: user?.userAvatar || defaultAvatar,
      createTime: formatTime(user?.createTime || ''),
      userRole: user?.userRole || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 获取用户统计数据
const loadStatistics = async () => {
  try {
    const res = await userStatsApi.getUserStatistics()
    if (res.code === 0 && res.data) {
      statistics.value = res.data
    } else {
      console.warn('获取统计数据返回错误:', res.message)
      // 使用默认数据
      statistics.value = {
        totalSolved: 0,
        totalSubmissions: 0,
        correctRate: '0%',
        streak: 0,
        typeCounts: {
          choice: { total: 0, solved: 0 },
          judge: { total: 0, solved: 0 },
          program: { total: 0, solved: 0 }
        },
        difficultyCounts: {
          easy: { total: 0, solved: 0 },
          medium: { total: 0, solved: 0 },
          hard: { total: 0, solved: 0 }
        }
      }
    }

    // 加载完成后初始化图表
    initCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 获取错题本数据
const loadErrorProblems = async () => {
  loadingErrors.value = true
  try {
    const params = {
      ...errorFilters.value,
      current: errorPage.value.current,
      size: errorPage.value.size
    }

    const res = await userStatsApi.getErrorProblems(params)
    if (res.code === 0 && res.data) {
      errorProblems.value = res.data.records
      errorPage.value.total = res.data.total
    } else {
      console.warn('获取错题本返回错误:', res.message)
      ElMessage.warning(res.message || '获取错题本失败')
      errorProblems.value = []
      errorPage.value.total = 0
    }
  } catch (error) {
    console.error('加载错题本失败:', error)
    ElMessage.error('加载错题本失败')
    errorProblems.value = []
    errorPage.value.total = 0
  } finally {
    loadingErrors.value = false
  }
}

// 初始化图表
const initCharts = () => {
  // 使用延时确保DOM完全加载
  setTimeout(() => {
    try {
      // 检查DOM元素是否存在
      if (!document.getElementById('overall-progress-chart') || 
          !document.getElementById('problem-type-chart') || 
          !document.getElementById('difficulty-chart')) {
        console.warn('图表容器未找到，将在50ms后重试初始化')
        setTimeout(initCharts, 50) // 延迟重试
        return
      }

      console.log('开始初始化图表')
      
      // 总体进度图表
      const overallChart = echarts.init(document.getElementById('overall-progress-chart'))
      const totalProblems = statistics.value.typeCounts.choice.total + 
                          statistics.value.typeCounts.judge.total + 
                          statistics.value.typeCounts.program.total
      const solvedProblems = statistics.value.typeCounts.choice.solved + 
                           statistics.value.typeCounts.judge.solved + 
                           statistics.value.typeCounts.program.solved
      
      overallChart.setOption({
        title: {
          text: '刷题进度',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['已完成', '未完成']
        },
        series: [
          {
            name: '刷题进度',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
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
            data: [
              { value: solvedProblems, name: '已完成' },
              { value: totalProblems - solvedProblems, name: '未完成' }
            ]
          }
        ]
      })
      console.log('总体进度图表初始化完成')

      // 题型分布图表
      const typeChart = echarts.init(document.getElementById('problem-type-chart'))
      typeChart.setOption({
        title: {
          text: '题目类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['选择题', '判断题', '编程题']
        },
        series: [
          {
            name: '题型分布',
            type: 'pie',
            radius: '50%',
            data: [
              { value: statistics.value.typeCounts.choice.total, name: '选择题' },
              { value: statistics.value.typeCounts.judge.total, name: '判断题' },
              { value: statistics.value.typeCounts.program.total, name: '编程题' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
      console.log('题型分布图表初始化完成')

      // 难度分布图表
      const diffChart = echarts.init(document.getElementById('difficulty-chart'))
      diffChart.setOption({
        title: {
          text: '难度分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['已完成', '未完成'],
          top: 'bottom'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['简单', '中等', '困难']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '已完成',
            type: 'bar',
            stack: 'total',
            label: {
              show: true
            },
            emphasis: {
              focus: 'series'
            },
            data: [
              statistics.value.difficultyCounts.easy.solved,
              statistics.value.difficultyCounts.medium.solved,
              statistics.value.difficultyCounts.hard.solved
            ]
          },
          {
            name: '未完成',
            type: 'bar',
            stack: 'total',
            label: {
              show: true
            },
            emphasis: {
              focus: 'series'
            },
            data: [
              statistics.value.difficultyCounts.easy.total - statistics.value.difficultyCounts.easy.solved,
              statistics.value.difficultyCounts.medium.total - statistics.value.difficultyCounts.medium.solved,
              statistics.value.difficultyCounts.hard.total - statistics.value.difficultyCounts.hard.solved
            ]
          }
        ]
      })
      console.log('难度分布图表初始化完成')

      // 添加窗口大小变化的事件监听，以便调整图表大小
      window.addEventListener('resize', () => {
        overallChart.resize()
        typeChart.resize()
        diffChart.resize()
      })
    } catch (error) {
      console.error('图表初始化失败:', error)
    }
  }, 0)
}

// 跳转到题目页面
const goToProblem = (problem: ErrorProblem) => {
  router.push(`/problem/${problem.type.toLowerCase()}/${problem.id}`)
}

// 标记题目为已掌握
const markAsMastered = (problemId: number) => {
  ElMessageBox.confirm('确定将此题标记为已掌握吗？', '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await userStatsApi.markProblemAsMastered(problemId)
        if (res.code === 0 && res.data) {
          ElMessage.success('已标记为掌握')
          // 重新加载错题本数据
          errorProblems.value = errorProblems.value.filter(item => item.id !== problemId)
          if (errorProblems.value.length === 0 && errorPage.value.current > 1) {
            errorPage.value.current--
          }
          loadErrorProblems()
          // 更新统计数据
          loadStatistics()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        console.error('标记已掌握失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 错题本分页处理
const handleErrorPageChange = (page: number) => {
  errorPage.value.current = page
  loadErrorProblems()
}

// 重置筛选条件
const resetFilters = () => {
  errorFilters.value = {
    type: '',
    difficulty: '',
    keyword: ''
  }
  loadErrorProblems()
}

// 监听筛选条件变化
watch(
  [() => errorFilters.value.type, () => errorFilters.value.difficulty],
  () => {
    if (activeTab.value === 'errorBook') {
      errorPage.value.current = 1 // 重置为第一页
      loadErrorProblems()
    }
  }
)

// 监听标签页变化
watch(
  activeTab,
  (newVal) => {
    // 当切换到错题本标签时，加载数据
    if (newVal === 'errorBook') {
      loadErrorProblems()
    }
  },
  // 立即执行一次
  { immediate: true }
)

// 显示编辑个人资料对话框
const showEditProfile = () => {
  editForm.value = {
    username: userInfo.value.username,
    profile: userInfo.value.profile,
    avatar: userInfo.value.avatar
  }
  profileDialogVisible.value = true
}

// 头像上传前的验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }

  return isJpgOrPng && isLt2M
}

// 头像上传成功的回调
const handleAvatarSuccess: UploadProps['onSuccess'] = async (response) => {
  if (response.code === 0) {
    const avatarUrl = response.data;
    console.log('头像上传成功，URL:', avatarUrl);
    
    // 立即更新表单中的头像
    editForm.value.avatar = avatarUrl;
    
    // 立即更新界面上显示的头像
    userInfo.value.avatar = avatarUrl;
    
    // 更新用户信息
    try {
      const res = await userStore.updateUserInfo({
        userAvatar: avatarUrl
      });
      
      if (res.code === 0) {
        ElMessage.success('头像更新成功');
      } else {
        ElMessage.error(res.message || '头像更新失败');
        console.error('更新头像失败，响应:', res);
      }
    } catch (error) {
      console.error('更新头像失败:', error);
      ElMessage.error('头像更新失败，请稍后重试');
    }
  } else {
    console.error('头像上传失败，响应:', response);
    ElMessage.error(response.message || '头像上传失败');
  }
}

// 提交编辑个人资料
const submitEditProfile = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await userStore.updateUserInfo({
          userName: editForm.value.username,
          userProfile: editForm.value.profile,
          userAvatar: editForm.value.avatar
        })
        
        if (res.code === 0) {
          ElMessage.success('个人资料更新成功')
          profileDialogVisible.value = false
          // 重新加载用户信息
          loadUserInfo()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } catch (error) {
        console.error('更新个人资料失败:', error)
        ElMessage.error('更新失败，请稍后重试')
      }
    }
  })
}

// 获取题目类型对应的标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'CHOICE': 'success',
    'JUDGE': 'warning',
    'PROGRAM': 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取题目类型对应的文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'PROGRAM': '编程题'
  }
  return typeMap[type] || '未知'
}

// 获取难度对应的标签类型
const getDifficultyType = (difficulty: string) => {
  const diffMap: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return diffMap[difficulty] || 'info'
}

// 页面加载时获取用户数据
onMounted(async () => {
  if (userStore.token) {
    // 确保先获取最新的用户信息
    await userStore.getCurrentUser()
    // 然后加载用户信息和统计数据
    loadUserInfo()
    loadStatistics()
    
    // 如果默认标签是错题本，自动加载错题数据
    if (activeTab.value === 'errorBook') {
      loadErrorProblems()
    }
  } else {
    router.push('/login')
  }
})
</script>

<style scoped>
.user-center-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-info-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.user-avatar {
  margin-right: 30px;
}

.user-details {
  flex: 1;
  min-width: 200px;
}

.user-name {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.user-profile {
  color: #666;
  margin: 0 0 15px 0;
  font-size: 14px;
  line-height: 1.5;
  max-width: 500px;
  white-space: pre-wrap;
  word-break: break-all;
}

.user-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  margin-top: 10px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.user-actions {
  margin-left: 20px;
}

.statistics-area {
  margin-bottom: 20px;
}

.statistic-card {
  height: 350px;
  margin-bottom: 20px;
}

.progress-chart,
.type-chart,
.difficulty-chart {
  height: 280px;
}

.tabs-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.filter-bar {
  margin-bottom: 15px;
}

.loading-container,
.empty-container {
  padding: 20px;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.problem-link {
  color: #409EFF;
  text-decoration: none;
}

.problem-link:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-info-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .user-stats {
    justify-content: center;
  }

  .user-actions {
    margin-left: 0;
    margin-top: 15px;
  }

  .filter-form {
    display: flex;
    flex-direction: column;
  }
}

.avatar-uploader {
  text-align: center;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  display: block;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

/* 图表样式 */
.statistic-card {
  margin-bottom: 20px;
  height: 100%;
}

.progress-chart,
.type-chart,
.difficulty-chart {
  height: 300px;
  width: 100%;
}
</style> 