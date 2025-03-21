<template>
    <div class="profile-container">
      <el-card class="profile-card">
        <div class="profile-header">
          <h2>个人资料</h2>
        </div>
        
        <div class="profile-content">
          <div class="avatar-section">
            <el-avatar 
              :size="100" 
              :src="userInfo.avatar ? userInfo.avatar + '?t=' + new Date().getTime() : defaultAvatar"
              @error="() => { console.error('头像加载失败:', userInfo.avatar); userInfo.avatar = defaultAvatar; }"
            ></el-avatar>
            <el-button type="primary" plain class="mt-3" @click="showEditProfile">编辑资料</el-button>
          </div>
  
          <div class="info-section">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
              <el-descriptions-item label="个人简介">{{ userInfo.profile || '这个人很懒，还没有写简介' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ userInfo.createTime }}</el-descriptions-item>
              <el-descriptions-item label="用户角色">{{ userInfo.userRole }}</el-descriptions-item>
            </el-descriptions>
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
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import { Plus } from '@element-plus/icons-vue'
  import type { FormInstance, UploadProps } from 'element-plus'
  
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
  
  // 编辑表单相关
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
        router.push('/admin/login')
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
      const avatarUrl = response.data
      console.log('头像上传成功，URL:', avatarUrl)
      
      editForm.value.avatar = avatarUrl
      userInfo.value.avatar = avatarUrl
      
      try {
        const res = await userStore.updateUserInfo({
          userAvatar: avatarUrl
        })
        
        if (res.code === 0) {
          ElMessage.success('头像更新成功')
        } else {
          ElMessage.error(res.message || '头像更新失败')
        }
      } catch (error) {
        console.error('更新头像失败:', error)
        ElMessage.error('头像更新失败，请稍后重试')
      }
    } else {
      ElMessage.error(response.message || '头像上传失败')
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
  
  // 页面加载时获取用户数据
  onMounted(async () => {
    if (userStore.token) {
      await userStore.getCurrentUser()
      loadUserInfo()
    } else {
      router.push('/admin/login')
    }
  })
  </script>
  
  <style scoped>
  .profile-container {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
  }
  
  .profile-card {
    margin-bottom: 20px;
  }
  
  .profile-header {
    margin-bottom: 20px;
  }
  
  .profile-content {
    display: flex;
    gap: 40px;
  }
  
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  .info-section {
    flex: 1;
  }
  
  .mt-3 {
    margin-top: 12px;
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
  
  @media (max-width: 768px) {
    .profile-content {
      flex-direction: column;
      align-items: center;
    }
  
    .info-section {
      width: 100%;
    }
  }
  </style>