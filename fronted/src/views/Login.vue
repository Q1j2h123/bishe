<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-content">
        <div class="logo-area">
  <div class="text-logo">code</div>
  <h1 class="site-title">刷题平台</h1>
</div>
        
        <h2 class="welcome-text">欢迎回来</h2>
        <p class="subtitle">登录您的账号，继续编程之旅</p>
        
        <!-- 添加错误信息显示 -->
        <el-alert
          v-if="loginError"
          :title="loginError"
          type="error"
          :closable="true"
          show-icon
          style="margin-bottom: 20px;"
        />
        
        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="userAccount">
            <el-input
              v-model="loginForm.userAccount"
              placeholder="请输入账号"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="userPassword">
            <el-input
              v-model="loginForm.userPassword"
              :type="passwordVisible ? 'text' : 'password'"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              size="large"
            />
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox>记住我</el-checkbox>
            <a href="javascript:void(0)" class="link-text">忘记密码？</a>
          </div>
          
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
            size="large"
          >
            登录
          </el-button>
          
          <div class="register-link">
            还没有账号？ <a href="javascript:void(0)" @click="goToRegister" class="link-text">立即注册</a>
          </div>
        </el-form>
        
        <div class="decoration-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
      
      <div class="login-image">
        <img src="https://img.freepik.com/free-vector/code-typing-concept-illustration_114360-3581.jpg" alt="编程插图" />
        <div class="image-overlay">
          <div class="slogan">
            <h2>提升编程能力</h2>
            <p>在这里找到成长的乐趣</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Monitor } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 添加登录错误信息状态
const loginError = ref('')

// 登录表单
const loginForm = reactive({
  userAccount: '',
  userPassword: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能小于4位', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
})

const formRef = ref<FormInstance>()
const loading = ref(false)
const passwordVisible = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    // 清除之前的错误
    loginError.value = ''
    console.log('开始登录请求...')
    
    // 确保先清理可能存在的旧登录信息，防止封禁用户信息残留
    userStore.logout()
    
    await userStore.login(loginForm)
    
    ElMessage.success('登录成功')
    
    // 如果有重定向，则跳转到重定向页面，否则跳转到首页
    const redirectPath = route.query.redirect as string || '/home'
    router.push(redirectPath)
  } catch (error: any) {
    console.error('登录失败:', error)
    // 设置错误信息，优先显示错误消息，如果没有则显示通用错误
    loginError.value = error.message || '登录失败，请稍后重试'
    // 特别处理封禁信息，使用更醒目的显示方式
    if (error.message && error.message.includes('已被封禁')) {
      loginError.value = error.message
      // 使用console.log记录详细的错误信息，帮助调试
      console.warn('用户被封禁:', error.message)
      // 确保封禁用户不会保留登录状态
      userStore.logout()
    }
  } finally {
    loading.value = false
  }
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-card {
  display: flex;
  width: 900px;
  max-width: 90%;
  min-height: 550px;
  background-color: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
}

.login-content {
  flex: 1;
  padding: 40px;
  position: relative;
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

/* 文字Logo样式 */
.text-logo {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409EFF, #1677ff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  margin-right: 18px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
  font-size: 24px;
  font-weight: bold;
  font-family: 'Monaco', 'Courier New', monospace;
}

.text-logo:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.site-title {
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.welcome-text {
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin: 30px 0 10px 0;
}

.subtitle {
  font-size: 15px;
  color: #909399;
  margin: 0 0 30px 0;
}

.login-form {
  width: 100%;
  max-width: 340px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: 48px;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

/* 添加错误信息样式 */
.el-alert {
  border-radius: 8px;
  margin-bottom: 20px;
}

.el-alert :deep(.el-alert__title) {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap; /* 允许换行显示 */
}

/* 添加对封禁信息的特殊样式 */
.el-alert.el-alert--error :deep(.el-alert__title) {
  font-weight: bold; /* 加粗显示 */
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
  background: #409EFF;
  border: none;
  transition: transform 0.3s, background 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  background: #66b1ff;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #606266;
}

.link-text {
  color: #409EFF;
  text-decoration: none;
  transition: color 0.3s;
}

.link-text:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.decoration-dots {
  position: absolute;
  bottom: 30px;
  right: 30px;
  display: flex;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e0e7ff;
  margin-right: 8px;
}

.dot:nth-child(2) {
  background: #c0d1ff;
}

.dot:nth-child(3) {
  background: #a0bbff;
}

.login-image {
  flex: 1.2;
  position: relative;
  overflow: hidden;
  display: none;
}

.login-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(64, 158, 255, 0.7), rgba(100, 180, 255, 0.8));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  text-align: center;
}

.slogan h2 {
  font-size: 28px;
  margin-bottom: 10px;
}

.slogan p {
  font-size: 16px;
  opacity: 0.9;
}

@media (min-width: 768px) {
  .login-image {
    display: block;
  }
}
</style> 