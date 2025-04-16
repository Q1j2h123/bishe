<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-content">
        <div class="logo-area">
          <div class="text-logo">Code</div>
          <h1 class="site-title">刷题平台</h1>
        </div>
        
        <h2 class="welcome-text">创建账号</h2>
        <p class="subtitle">加入我们，开始编程挑战</p>
        
        <el-form
          ref="formRef"
          :model="registerForm"
          :rules="rules"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="userAccount">
            <el-input
              v-model="registerForm.userAccount"
              placeholder="请输入账号"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="userName">
            <el-input
              v-model="registerForm.userName"
              placeholder="请输入用户名"
              :prefix-icon="Avatar"
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="userPassword">
            <el-input
              v-model="registerForm.userPassword"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="checkPassword">
            <el-input
              v-model="registerForm.checkPassword"
              type="password"
              placeholder="请确认密码"
              :prefix-icon="Lock"
              show-password
              clearable
              size="large"
            />
          </el-form-item>
          
          <!-- <div class="agreement">
            <el-checkbox>我已阅读并同意<a href="javascript:void(0)" class="link-text">用户协议</a>和<a href="javascript:void(0)" class="link-text">隐私政策</a></el-checkbox>
          </div> -->
          
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
            size="large"
          >
            注册账号
          </el-button>
          
          <div class="login-link">
            已有账号？ <a href="javascript:void(0)" @click="goToLogin" class="link-text">立即登录</a>
          </div>
        </el-form>
        
        <div class="decoration-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
      
      <div class="register-image">
        <img src="https://img.freepik.com/free-vector/programmer-work-with-working-day-symbols-flat-illustration_1284-60322.jpg" alt="编程插图" />
        <div class="image-overlay">
          <div class="slogan">
            <h2>开启编程新旅程</h2>
            <p>与志同道合的人一起成长</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Avatar, Monitor } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import type { RegisterParams } from '@/types/api'

const router = useRouter()

// 注册表单
const registerForm = reactive<RegisterParams>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: ''
})

// 表单验证规则
const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    if (registerForm.checkPassword !== '') {
      formRef.value?.validateField('checkPassword', () => {})
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能小于4位', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
})

const formRef = ref<FormInstance>()
const loading = ref(false)

// 处理注册
const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    await userApi.register(registerForm)
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.register-card {
  display: flex;
  width: 900px;
  max-width: 90%;
  min-height: 600px;
  background-color: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
}

.register-content {
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

/* 新图标样式 */
.logo-icon {
  font-size: 24px;
  padding: 10px;
  color: #409EFF;
  background-color: #ecf5ff;
  border-radius: 12px;
  margin-right: 12px;
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

.register-form {
  width: 100%;
  max-width: 340px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  height: 48px;
  transition: all 0.3s;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.agreement {
  margin-bottom: 24px;
  font-size: 14px;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
  background: #409EFF;
  border: none;
  transition: transform 0.3s, background 0.3s;
}

.register-button:hover {
  transform: translateY(-2px);
  background: #66b1ff;
}

.login-link {
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

.register-image {
  flex: 1.2;
  position: relative;
  overflow: hidden;
  display: none;
}

.register-image img {
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
  .register-image {
    display: block;
  }
}
</style> 