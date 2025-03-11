<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import type { UserInfo } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref<FormInstance>()

// 用户信息表单
const userForm = reactive<Partial<UserInfo>>({
  id: undefined,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
  createTime: ''
})

// 加载用户信息
const loadUserInfo = () => {
  if (!userStore.currentUser) {
    router.push('/login')
    return
  }
  
  // 复制用户信息
  Object.assign(userForm, userStore.currentUser)
}

// 保存用户信息
const saveUserInfo = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 只更新允许的字段
    const updateData = {
      userName: userForm.userName,
      userAvatar: userForm.userAvatar,
      userProfile: userForm.userProfile
    }
    
    await userApi.updateUserInfo(updateData)
    
    // 更新本地存储的用户信息
    if (userStore.currentUser) {
      userStore.setUserInfo({
        ...userStore.currentUser,
        ...updateData
      })
    }
    
    ElMessage.success('保存成功')
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="profile-container">
    <el-header class="header">
      <div class="logo">
        <h1>在线判题系统</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text @click="router.push('/problems')">题目列表</el-button>
        <el-button text @click="router.push('/submissions')">提交记录</el-button>
      </div>
    </el-header>
    
    <el-main class="main">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h2>个人信息</h2>
          </div>
        </template>
        
        <el-form
          ref="formRef"
          :model="userForm"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="账号">
            <el-input v-model="userForm.userAccount" disabled />
          </el-form-item>
          
          <el-form-item label="用户名">
            <el-input v-model="userForm.userName" />
          </el-form-item>
          
          <el-form-item label="头像">
            <el-input v-model="userForm.userAvatar" placeholder="请输入头像URL" />
            <div class="avatar-preview" v-if="userForm.userAvatar">
              <el-avatar :size="80" :src="userForm.userAvatar" />
            </div>
          </el-form-item>
          
          <el-form-item label="个人简介">
            <el-input
              v-model="userForm.userProfile"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
            />
          </el-form-item>
          
          <el-form-item label="用户角色">
            <el-tag v-if="userForm.userRole === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else-if="userForm.userRole === 'user'" type="success">普通用户</el-tag>
            <el-tag v-else-if="userForm.userRole === 'ban'" type="info">已封禁</el-tag>
          </el-form-item>
          
          <el-form-item label="注册时间">
            <span>{{ new Date(userForm.createTime).toLocaleString() }}</span>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveUserInfo" :loading="loading">保存修改</el-button>
            <el-button @click="router.push('/home')">返回首页</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </div>
</template>

<style scoped>
.profile-container {
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

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.profile-form {
  margin-top: 20px;
}

.avatar-preview {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}
</style> 