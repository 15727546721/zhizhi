<template>
  <el-card class="edit-profile-container">
    <h2 class="title">修改信息</h2>
    <el-form 
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="profile-form"
      @submit.prevent="submitForm"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          placeholder="最多 12 个字"
          maxlength="12"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="头像" prop="avatar">
        <el-upload
          class="avatar-uploader"
          action="#"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="handleAvatarUpload"
          accept="image/*"
        >
          <el-avatar :size="80" :src="form.avatar || defaultAvatar" />
          <div class="avatar-hint">点击更换头像</div>
        </el-upload>
        <span v-if="avatarUploading" class="upload-status">上传中...</span>
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="简介" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请填写个人简介"
          :rows="4"
          show-word-limit
          maxlength="200"
        />
      </el-form-item>

      <el-form-item class="form-actions">
        <el-button type="primary" native-type="submit" :loading="submitting">提交</el-button>
        <el-button @click="cancelEdit">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import { updateUserProfile, uploadAvatar } from '@/api/user'
import { validateApiResponse } from '@/utils/typeGuards'

// 类型定义
interface ProfileForm {
  nickname: string
  avatar: string
  gender: number
  description: string
}

interface UploadOptions {
  file: File
}

// API 响应类型
interface UploadAvatarResponse {
  url: string
}

interface UpdateProfileResponse {
  id: number
  nickname: string
  avatar: string
  gender: number
  description: string
}

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance | null>(null)

const userId = userStore.getUserInfo?.id
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const form = ref<ProfileForm>({
  nickname: '',
  avatar: '',
  gender: 1, // 1-男，2-女
  description: ''
})

const avatarUploading = ref<boolean>(false)
const submitting = ref<boolean>(false)

// 表单验证规则
const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 12, message: '长度在 2 到 12 个字符', trigger: 'blur' }
  ]
}

const userInfo = ref<any>({})

// 在组件挂载时根据 userId 获取用户信息
onMounted(async () => {
  if (!userStore.isAuthenticated) {
    router.push('/')
    return
  }
  
  // 直接获取用户信息
  userInfo.value = userStore.getUserInfo;
  
  // 转换性别：字符串转数字（'男' -> 1, '女' -> 2）
  let genderValue = 1;
  if (userInfo.value.gender === '女' || userInfo.value.gender === 2) {
    genderValue = 2;
  } else if (userInfo.value.gender === '男' || userInfo.value.gender === 1) {
    genderValue = 1;
  }
  
  form.value = {
    nickname: userInfo.value.nickname || '',
    avatar: userInfo.value.avatar || '',
    gender: genderValue,
    description: userInfo.value.description || ''
  }
})

// 头像上传前校验
const beforeAvatarUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 头像上传
const handleAvatarUpload = async (options: UploadOptions): Promise<void> => {
  const file = options.file
  if (!file) return

  try {
    avatarUploading.value = true
    const response = await uploadAvatar(file)
    const data = validateApiResponse<UploadAvatarResponse>(response)
    
    if (data) {
      form.value.avatar = data.url
      ElMessage.success('头像上传成功!')
    }
  } catch (error) {
    // 上传失败
  } finally {
    avatarUploading.value = false
  }
}

const submitForm = async (): Promise<void> => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 只传递可修改的字段，不传递用户ID（后端从 token获取，防止越权）
    const profileData = {
      nickname: form.value.nickname,
      avatar: form.value.avatar,
      gender: form.value.gender,
      description: form.value.description
    }
    
    const response = await updateUserProfile(profileData)
    const data = validateApiResponse<UpdateProfileResponse>(response)
    
    if (data) {
      ElMessage.success('信息已更新！')
      
      // 更新用户信息到 store
      userStore.updateUserInfo(data)
      
      // 跳转到个人主页
      router.push({
        path: `/user/${userId}`
      })
    } else {
      // 如果没有返回数据，则主动刷新
      try {
        await userStore.fetchUserInfo()
        
        // 跳转到个人主页
        router.push({
          path: `/user/${userId}`
        })
      } catch (error) {
        ElMessage.error(response.info || '更新失败，请重试')
      }
    }
  } catch (error) {
    // 更新失败
  } finally {
    submitting.value = false
  }
}

const cancelEdit = (): void => {
  router.push(`/user/${userId}`)
}
</script>

<style scoped>
.edit-profile-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 32px;
  border-radius: 16px;
  background-color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #2c3e50;
  text-align: center;
  padding-bottom: 16px;
}

.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader .el-avatar {
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
}

.avatar-uploader .el-avatar:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.el-radio-group) {
  display: flex;
  align-items: center;
  gap: 32px;
}

:deep(.el-radio) {
  margin-right: 0;
  height: 32px;
  display: flex;
  align-items: center;
}

.avatar-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.upload-status {
  margin-left: 12px;
  color: #409eff;
  font-size: 14px;
}
</style>