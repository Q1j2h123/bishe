<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px">
        <div class="sidebar-header">
          <div class="text-logo">Admin</div>
          <h3>刷题平台</h3>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :router="true"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>控制面板</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/problems">
            <el-icon><Document /></el-icon>
            <span>题目管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/import">
            <el-icon><Upload /></el-icon>
            <span>批量导入题目</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
    
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-left">
            <el-breadcrumb>
              <el-breadcrumb-item>刷题平台</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userStore.currentUser?.userName }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Document, 
  User, 
  Odometer, 
  ArrowDown,
  Upload,
  Monitor
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 获取当前页面标题
const currentPageTitle = computed(() => {
  return route.meta.title || '页面'
})

// 处理用户命令
const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/admin/login')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #bfcbd9;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 20px 16px;
}

.sidebar-header .text-logo {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #f56c6c, #c45656);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-right: 10px;
  font-size: 14px;
  font-weight: bold;
}

.sidebar-header h3 {
  margin: 0;
  color: #fff;
  font-size: 16px;
}

.el-header {
  background-color: #fff;
  color: #333;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.header-left, .header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
