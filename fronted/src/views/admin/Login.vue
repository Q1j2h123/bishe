<template>
    <div class="admin-login-container">
      <div class="admin-login-card">
        <div class="admin-login-content">
          <div class="logo-area">
            <div class="text-logo">Admin</div>
            <h1 class="site-title">刷题平台管理系统</h1>
          </div>
          
          <h2 class="welcome-text">管理员登录</h2>
          <p class="subtitle">请登录管理员账号进行系统管理</p>
          
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
                placeholder="请输入管理员账号"
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
            
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
              size="large"
            >
              管理员登录
            </el-button>
          </el-form>
          
          <div class="decoration-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
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
  import { User, Lock } from '@element-plus/icons-vue'
  import { useUserStore } from '@/stores/user'
  
  const router = useRouter()
  const userStore = useUserStore()
  
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
      
      const result = await userStore.login(loginForm)
      
      // 检查用户角色是否为管理员
      if (userStore.currentUser?.userRole !== 'admin') {
        ElMessage.error('您不是管理员，无法访问管理系统')
        userStore.logout()
        loading.value = false
        return
      }
      
      ElMessage.success('管理员登录成功')
      router.push('/admin/dashboard')
    } catch (error: any) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  </script>
  
  <style scoped>
  .admin-login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f0f2f5;
    padding: 20px;
  }
  
  .admin-login-card {
    width: 500px;
    max-width: 90%;
    background-color: white;
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
  }
  
  .admin-login-content {
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
  
  .text-logo {
    width: 70px;
    height: 70px;
    background: linear-gradient(135deg, #f56c6c, #c45656);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
    margin-right: 18px;
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
    transition: all 0.3s ease;
    font-size: 20px;
    font-weight: bold;
    font-family: 'Monaco', 'Courier New', monospace;
  }
  
  .text-logo:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
  }
  
  /* 其他样式保持与普通登录页一致，但可以修改颜色为红色系突出管理员身份 */
  .login-button {
    background: linear-gradient(135deg, #f56c6c, #c45656);
    border: none;
  }
  
  .login-button:hover {
    background: #f78989;
  }
  
  /* 省略其他相同样式... */
  </style>