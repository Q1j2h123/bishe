<template>
  <div class="profile-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>个人主页</h2>
          <el-button @click="router.push('/problems')">返回刷题</el-button>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="user-info-card">
              <template #header>
                <div class="card-header">
                  <span>个人信息</span>
                  <el-button link type="primary" @click="handleEditProfile">
                    编辑资料
                  </el-button>
                </div>
              </template>
              <div class="user-info">
                <el-avatar :size="100" :src="userStore.currentUser?.userAvatar" />
                <h3>{{ userStore.currentUser?.userName }}</h3>
                <p>{{ userStore.currentUser?.userAccount }}</p>
                <p>加入时间：{{ formatDate(userStore.currentUser?.createTime) }}</p>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="16">
            <el-card class="statistics-card">
              <template #header>
                <div class="card-header">
                  <span>做题统计</span>
                </div>
              </template>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.totalProblems }}</div>
                    <div class="stat-label">总题数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.solvedProblems }}</div>
                    <div class="stat-label">已解决</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number">{{ statistics.correctRate }}%</div>
                    <div class="stat-label">正确率</div>
                  </div>
                </el-col>
              </el-row>
            </el-card>
            
            <el-card class="recent-problems-card">
              <template #header>
                <div class="card-header">
                  <span>最近做题记录</span>
                </div>
              </template>
              <el-table :data="recentProblems" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="题号" width="80" />
                <el-table-column prop="title" label="题目" />
                <el-table-column prop="difficulty" label="难度" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getDifficultyType(row.difficulty)">
                      {{ row.difficulty }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 2 ? 'success' : 'danger'">
                      {{ row.status === 2 ? '通过' : '未通过' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="提交时间" width="180">
                  <template #default="{ row }">
                    {{ formatDate(row.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑资料"
      width="500px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px">
        <el-form-item label="昵称" prop="userName">
          <el-input v-model="editForm.userName" />
        </el-form-item>
        <el-form-item label="头像" prop="userAvatar">
          <el-input v-model="editForm.userAvatar" />
        </el-form-item>
        <el-form-item label="简介" prop="userProfile">
          <el-input
            v-model="editForm.userProfile"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProfile" :loading="saving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'
import { questionApi } from '../api/question'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editFormRef = ref()

const statistics = ref({
  totalProblems: 0,
  solvedProblems: 0,
  correctRate: 0
})

const recentProblems = ref([])

const editForm = reactive({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

const rules = {
  userName: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const getDifficultyType = (difficulty: string) => {
  const types: Record<string, string> = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty] || 'info'
}

const formatDate = (date: string | undefined) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const fetchStatistics = async () => {
  try {
    // TODO: 实现获取统计数据的API
    statistics.value = {
      totalProblems: 100,
      solvedProblems: 45,
      correctRate: 85
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

const fetchRecentProblems = async () => {
  try {
    loading.value = true
    // TODO: 实现获取最近做题记录的API
    recentProblems.value = [
      {
        id: 1,
        title: '两数之和',
        difficulty: '简单',
        status: 2,
        createTime: '2024-03-20 14:30:00'
      },
      {
        id: 2,
        title: '有效的括号',
        difficulty: '中等',
        status: 3,
        createTime: '2024-03-19 16:45:00'
      },
      {
        id: 3,
        title: '二叉树的最大深度',
        difficulty: '简单',
        status: 2,
        createTime: '2024-03-18 09:15:00'
      }
    ]
  } catch (error) {
    console.error('获取最近做题记录失败:', error)
    ElMessage.error('获取最近做题记录失败')
  } finally {
    loading.value = false
  }
}

const handleEditProfile = () => {
  if (!userStore.currentUser) return
  editForm.userName = userStore.currentUser.userName
  editForm.userAvatar = userStore.currentUser.userAvatar || ''
  editForm.userProfile = userStore.currentUser.userProfile || ''
  dialogVisible.value = true
}

const handleSaveProfile = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        saving.value = true
        await userApi.updateUserInfo(editForm)
        await userStore.getCurrentUser()
        ElMessage.success('保存成功')
        dialogVisible.value = false
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

onMounted(() => {
  fetchStatistics()
  fetchRecentProblems()
})
</script>

<style scoped>
.profile-container {
  height: 100vh;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  text-align: center;
  padding: 20px 0;
}

.user-info h3 {
  margin: 10px 0;
}

.user-info p {
  color: #666;
  margin: 5px 0;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  color: #666;
  margin-top: 5px;
}

.recent-problems-card {
  margin-top: 20px;
}
</style> 