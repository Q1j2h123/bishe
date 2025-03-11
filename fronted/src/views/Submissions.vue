<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="submissions-container">
    <el-header class="header">
      <div class="logo">
        <h1>在线判题系统</h1>
      </div>
      <div class="nav-links">
        <el-button text @click="router.push('/home')">首页</el-button>
        <el-button text @click="router.push('/problems')">题目列表</el-button>
        <el-button text type="primary">提交记录</el-button>
        <el-button text @click="router.push('/profile')">个人中心</el-button>
      </div>
    </el-header>
    
    <el-main class="main">
      <el-card>
        <template #header>
          <div class="card-header">
            <h2>提交记录</h2>
            <div class="header-actions">
              <span>开发中，敬请期待...</span>
            </div>
          </div>
        </template>
        
        <div class="submissions-placeholder">
          <el-empty description="提交记录功能开发中，敬请期待..." />
        </div>
      </el-card>
    </el-main>
  </div>
</template>

<style scoped>
.submissions-container {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.submissions-placeholder {
  padding: 40px 0;
}
</style> 