<template>
  <div class="users-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号">
          <el-input v-model="searchForm.userAccount" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-select v-model="searchForm.createTimeOrder" placeholder="请选择排序" clearable>
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userAccount" label="账号" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="userRole" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.userRole === 'admin' ? 'danger' : row.userRole === 'banned' ? 'info' : 'success'">
              {{ row.userRole === 'admin' ? '管理员' : row.userRole === 'banned' ? '已封禁' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" link @click="handleView(row)">
                查看
              </el-button>
              <el-button type="danger" link @click="handleResetPassword(row)">
                重置密码
              </el-button>
              <!-- 管理员不能被封禁，已封禁用户显示解封按钮，其他用户显示封禁按钮 -->
              <template v-if="row.userRole !== 'admin'">
                <el-button 
                  v-if="row.userRole === 'banned'" 
                  type="success" 
                  link 
                  @click="handleUnban(row)"
                >
                  解封
                </el-button>
                <el-button 
                  v-else 
                  type="warning" 
                  link 
                  @click="handleBan(row)"
                >
                  封禁
                </el-button>
              </template>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="用户详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账号">{{ currentUser?.userAccount }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser?.userName }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="currentUser?.userRole === 'admin' ? 'danger' : currentUser?.userRole === 'banned' ? 'info' : 'success'">
            {{ currentUser?.userRole === 'admin' ? '管理员' : currentUser?.userRole === 'banned' ? '已封禁' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ formatDate(currentUser?.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="提交次数">{{ currentUser?.submissionCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="通过次数">{{ currentUser?.acceptedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="解题数">{{ currentUser?.totalSolvedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="通过率">{{ (currentUser?.acceptanceRate || 0).toFixed(2) }}%</el-descriptions-item>
        <el-descriptions-item label="最近通过题目" :span="2">
          {{ currentUser?.lastAcceptedProblem || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 修改角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="修改用户角色"
      width="400px"
    >
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="当前角色">
          <el-tag :type="currentUser?.userRole === 'admin' ? 'danger' : currentUser?.userRole === 'banned' ? 'info' : 'success'">
            {{ currentUser?.userRole === 'admin' ? '管理员' : currentUser?.userRole === 'banned' ? '已封禁' : '普通用户' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="roleForm.newRole">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
            <el-option label="封禁用户" value="banned" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRole" :loading="updating">
          确认
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 封禁用户对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      title="封禁用户"
      width="400px"
    >
      <el-form :model="banForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.userName }} ({{ currentUser?.userAccount }})</span>
        </el-form-item>
        <el-form-item label="封禁原因" prop="reason">
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入封禁原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBan" :loading="updating">
          确认封禁
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/user'
import type { UserListVO, UserManageVO } from '@/types/api'

// 搜索表单
const searchForm = reactive({
  userAccount: '',
  userName: '',
  createTimeOrder: 'desc' as 'asc' | 'desc'
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载状态
const loading = ref(false)
const updating = ref(false)

// 用户列表
const userList = ref<UserListVO[]>([])

// 对话框控制
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const banDialogVisible = ref(false)
const currentUser = ref<UserManageVO | null>(null)

// 角色表单
const roleForm = reactive({
  newRole: ''
})

// 封禁表单
const banForm = reactive({
  reason: ''
})

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await userApi.getUserList({
      current: currentPage.value,
      pageSize: pageSize.value,
      userAccount: searchForm.userAccount || undefined,
      userName: searchForm.userName || undefined,
      createTimeOrder: searchForm.createTimeOrder
    })
    
    console.log('API响应数据:', res)
    
    if (res.code === 0 && res.data) {
      if (!res.data.records) {
        console.error('响应数据中缺少 records 字段:', res.data)
        ElMessage.warning('响应数据格式不正确')
        return
      }
      userList.value = res.data.records
      total.value = res.data.total || 0
    } else {
      console.error('API响应异常:', res)
      userList.value = []
      total.value = 0
      ElMessage.warning(res.message || '获取用户列表失败')
    }
  } catch (error: any) {
    console.error('API请求异常:', error)
    userList.value = []
    total.value = 0
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 查看用户详情
const handleView = async (row: UserListVO) => {
  try {
    const res = await userApi.getUserManageDetail(row.id)
    console.log('用户详情数据:', res)
    
    if (res.code === 0 && res.data) {
      currentUser.value = res.data
      dialogVisible.value = true
    } else {
      ElMessage.warning(res.message || '获取用户详情失败')
    }
  } catch (error: any) {
    console.error('获取用户详情失败:', error)
    ElMessage.error(error.message || '获取用户详情失败')
  }
}

// 修改角色
const handleEditRole = (row: UserListVO) => {
  currentUser.value = row as any
  roleForm.newRole = row.userRole || 'user'
  roleDialogVisible.value = true
}

// 更新角色
const handleUpdateRole = async () => {
  if (!currentUser.value) {
    ElMessage.warning('当前用户信息不存在')
    return
  }
  
  if (!roleForm.newRole) {
    ElMessage.warning('请选择新角色')
    return
  }

  // 如果角色没有变化，直接关闭对话框
  if (currentUser.value.userRole === roleForm.newRole) {
    roleDialogVisible.value = false
    return
  }
  
  updating.value = true
  try {
    console.log('正在更新用户角色:', {
      userId: currentUser.value.id,
      currentRole: currentUser.value.userRole,
      newRole: roleForm.newRole
    })
    
    await userApi.updateUserRole(currentUser.value.id, roleForm.newRole)
    ElMessage.success('修改角色成功')
    roleDialogVisible.value = false
    loadUserList()
  } catch (error: any) {
    console.error('修改角色失败:', error)
    ElMessage.error(error.message || '修改角色失败')
  } finally {
    updating.value = false
  }
}

// 重置密码
const handleResetPassword = async (row: UserListVO) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '提示', {
      type: 'warning'
    })
    
    await userApi.resetUserPassword(row.id)
    ElMessage.success('重置密码成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '重置密码失败')
    }
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.userAccount = ''
  searchForm.userName = ''
  searchForm.createTimeOrder = 'desc'
  handleSearch()
}

// 刷新数据
const refreshData = () => {
  loadUserList()
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadUserList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadUserList()
}

// 在 script setup 部分添加日期格式化函数
const formatDate = (date: string | undefined | null) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 封禁用户
const handleBan = (row: UserListVO) => {
  currentUser.value = row as any
  banForm.reason = ''  // 重置封禁原因
  banDialogVisible.value = true
}

// 确认封禁
const confirmBan = async () => {
  if (!currentUser.value) {
    ElMessage.warning('当前用户信息不存在')
    return
  }
  
  if (!banForm.reason) {
    ElMessage.warning('请输入封禁原因')
    return
  }

  updating.value = true
  try {
    console.log('正在封禁用户:', {
      userId: currentUser.value.id,
      reason: banForm.reason
    })
    
    await userApi.banUser(currentUser.value.id, banForm.reason)
    ElMessage.success('封禁用户成功')
    banDialogVisible.value = false
    loadUserList()
  } catch (error: any) {
    console.error('封禁用户失败:', error)
    ElMessage.error(error.message || '封禁用户失败')
  } finally {
    updating.value = false
  }
}

// 解封用户
const handleUnban = async (row: UserListVO) => {
  try {
    await ElMessageBox.confirm('确定要解封该用户吗？', '提示', {
      type: 'warning'
    })
    
    await userApi.unbanUser(row.id)
    ElMessage.success('解封用户成功')
    loadUserList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解封用户失败')
    }
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.users-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
