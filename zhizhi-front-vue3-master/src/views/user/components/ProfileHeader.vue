<template>
  <div class="profile-header">
    <div class="user-basic">
      <div class="avatar-wrapper">
        <!-- 自己的主页：可上传头像 -->
        <el-upload
          v-if="isOwnProfile"
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="handleAvatarUpload"
          accept="image/*"
        >
          <el-avatar 
            :size="80" 
            :src="userInfo?.avatar || 'default-avatar.png'" 
            class="avatar-clickable"
            @click.stop="handleAvatarClick"
          />
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
        </el-upload>
        <!-- 他人主页：点击预览 -->
        <div v-else class="avatar-view-wrapper">
          <el-avatar 
            :size="80" 
            :src="userInfo?.avatar || 'default-avatar.png'" 
            class="avatar-clickable"
            @click="handleAvatarClick"
          />
          <div class="avatar-preview-hint" v-if="userInfo?.avatar">
            <el-icon><ZoomIn /></el-icon>
            <span>点击查看大图</span>
          </div>
        </div>
        <span class="gender-icon" :class="{ female: userInfo?.gender === 2 }">
          <el-icon><Female v-if="userInfo?.gender === 2"/><Male v-else/></el-icon>
        </span>
      </div>
      <div class="user-info">
        <h2 class="username">{{ userInfo?.nickname || userInfo?.name || userInfo?.username || '暂无昵称' }}</h2>
        <div v-if="userInfo?.description" class="user-description">
          {{ userInfo.description }}
        </div>
      </div>
      <div class="action-buttons">
        <template v-if="isOwnProfile">
          <el-button type="primary" @click="handleEditProfile">修改资料</el-button>
          <el-button @click="handleSettings">
            <el-icon><Setting /></el-icon>
          </el-button>
        </template>
        <template v-else>
          <el-button 
            v-if="!isAuthenticated"
            type="primary"
            @click="handleLogin"
          >
            登录后关注
          </el-button>
          <template v-else>
            <el-button 
              :type="followButtonType"
              @click="handleFollow"
              :loading="followLoading"
              :disabled="followLoading"
            >
              {{ followButtonText }}
            </el-button>
            <el-button @click="handleSendMessage">
              <el-icon><ChatDotRound /></el-icon>
              私信
            </el-button>
          </template>
        </template>
      </div>
    </div>
    
    <!-- 头像预览 -->
    <el-image-viewer
      v-if="avatarPreviewVisible && userInfo?.avatar"
      :url-list="[userInfo.avatar]"
      :initial-index="0"
      :hide-on-click-modal="true"
      :teleported="true"
      :z-index="2001"
      @close="avatarPreviewVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElImageViewer } from 'element-plus'
import { Female, Male, Setting, Camera, ZoomIn, ChatDotRound } from '@element-plus/icons-vue'
import { uploadAvatar } from '@/api/user'
import type { UserInfo, ApiResponse } from '@/types'

const props = defineProps<{
  userInfo: UserInfo | null
  isOwnProfile: boolean
  isAuthenticated: boolean
  isFollowing: boolean
  isFollowedBy: boolean
  followLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'follow'): void
  (e: 'avatar-updated', url: string): void
}>()

const router = useRouter()
const avatarPreviewVisible = ref(false)
const avatarUploading = ref(false)

// 关注按钮文本
const followButtonText = computed(() => {
  if (props.isFollowing && props.isFollowedBy) return '互相关注'
  if (props.isFollowing) return '已关注'
  if (props.isFollowedBy) return '回关'
  return '关注'
})

// 关注按钮类型
const followButtonType = computed<'default' | 'primary'>(() => props.isFollowing ? 'default' : 'primary')

const handleEditProfile = () => {
  router.push('/user/edit')
}

const handleSettings = () => {
  router.push('/user/settings')
}

const handleLogin = () => {
  ElMessage.info('请先登录')
  window.dispatchEvent(new CustomEvent('show-login-dialog'))
}

const handleSendMessage = () => {
  if (!props.userInfo?.id) return
  router.push({ path: '/private-messages', query: { userId: String(props.userInfo.id) } })
}

const handleFollow = () => {
  emit('follow')
}

const handleAvatarClick = (event?: MouseEvent) => {
  if (avatarUploading.value) return
  if (props.userInfo?.avatar && props.userInfo.avatar !== 'default-avatar.png') {
    event?.stopPropagation()
    avatarPreviewVisible.value = true
  }
}

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

const handleAvatarUpload = async (options: { file: File }) => {
  try {
    avatarUploading.value = true
    const response = await uploadAvatar(options.file) as ApiResponse<string>
    
    if (response.code === 20000 && response.data) {
      ElMessage.success('头像上传成功!')
      emit('avatar-updated', response.data)
    } else {
      ElMessage.error(response.info || '头像上传失败')
    }
  } catch (error) {
    // 上传失败
  } finally {
    avatarUploading.value = false
  }
}
</script>

<style scoped>
.profile-header {
  background: linear-gradient(to right, #fff, #f8faff);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.profile-header:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.user-basic {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  margin-bottom: 8px;
}

.avatar-view-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-wrapper :deep(.el-avatar) {
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
}

.avatar-clickable {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.avatar-clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
  margin-top: 4px;
}

.avatar-view-wrapper .avatar-preview-hint {
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s ease, bottom 0.3s ease;
  pointer-events: none;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 10;
}

.avatar-view-wrapper:hover .avatar-preview-hint {
  opacity: 1;
  bottom: -35px;
}

.gender-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: 3px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.gender-icon.female {
  background: linear-gradient(135deg, #ff69b4, #ff8da1);
}

.gender-icon:hover {
  transform: scale(1.1);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-top: 8px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons :deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-buttons :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--el-color-primary), #409eff);
  border: none;
  padding: 10px 24px;
}

.action-buttons :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

@media screen and (max-width: 768px) {
  .profile-header {
    padding: 24px;
  }

  .user-basic {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
  }

  .action-buttons {
    width: 100%;
    justify-content: center;
  }
}
</style>
