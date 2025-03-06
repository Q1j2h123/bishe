<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="userAccount">
          <el-input v-model="loginForm.userAccount" placeholder="账号">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="userPassword">
          <el-input v-model="loginForm.userPassword" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  userAccount: '',
  userPassword: ''
})

const rules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        console.log('开始登录请求...')
        const response = await userApi.login(loginForm)
        console.log('登录响应:', response)
        
        if (response && response.token && response.userInfo) {
          console.log('设置token:', response.token)
          userStore.setToken(response.token)
          console.log('设置用户信息:', response.userInfo)
          userStore.setUserInfo(response.userInfo)
          
          console.log('准备跳转...')
          const redirect = route.query.redirect as string
          const targetPath = redirect || '/home'
          console.log('目标路径:', targetPath)
          
          try {
            await router.push(targetPath)
            console.log('路由跳转成功')
          } catch (routerError) {
            console.error('路由跳转失败:', routerError)
            ElMessage.error('页面跳转失败')
          }
        } else {
          console.error('登录响应数据异常:', response)
          ElMessage.error('登录失败，服务器响应异常')
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查账号密码')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/home')
  }
})
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}
</style> 