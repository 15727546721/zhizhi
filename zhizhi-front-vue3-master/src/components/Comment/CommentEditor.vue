<template>
  <div v-if="userStore.isAuthenticated" class="comment-editor">
    <UserAvatar :size="40" :src="currentUser.avatar" :username="currentUser.username" :nickname="currentUser.nickname" />
    <div class="editor-wrapper">
      <div v-if="images.length > 0" class="comment-image-preview">
        <div v-for="(image, index) in images" :key="index" class="preview-item">
          <el-image 
            :src="image.url" 
            fit="cover" 
            :preview-src-list="images.map(img => img.url)"
          />
          <div class="preview-remove" @click="removeImage(index)">
            <el-icon><Close /></el-icon>
          </div>
        </div>
      </div>
      <MentionInput
        v-model="content"
        type="textarea"
        :rows="3"
        placeholder="输入评论，@可以提及用户（Enter 发送，Shift + Enter 换行）"
        resize="none"
        @enter="handleSubmit"
        @mention="handleMention"
      />
      <div class="editor-footer">
        <div class="editor-tools">
          <EmojiSelector v-model="showEmojiPicker" @select="addEmoji" />
          <el-upload
            class="upload-btn"
            action="/api/upload/image"
            :before-upload="beforeImageUpload"
            :file-list="[]"
            :limit="maxImages - images.length"
            :show-file-list="false"
            multiple
          >
            <el-icon><Picture /></el-icon>
          </el-upload>
          <span class="image-count-hint">{{ images.length }}/{{ maxImages }}</span>
        </div>
        <el-button type="primary" :disabled="!content.trim() && images.length === 0" @click="handleSubmit">
          发表评论
        </el-button>
      </div>
    </div>
  </div>
  <div v-else class="not-login-tip">
    <el-button type="primary" @click="handleLogin">登录后发表评论</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElButton } from 'element-plus'
import { Close, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import UserAvatar from '@/components/UserAvatar.vue'
import EmojiSelector from '@/components/EmojiSelector/index.vue'
import MentionInput from '@/components/MentionInput.vue'
import { uploadFiles, deleteFiles } from '@/api/file'
import type { CommentImage, MentionUser, EmojiItem } from './types'

const emit = defineEmits<{
  (e: 'submit', data: { content: string; imageUrls: string[]; mentionUserIds: number[] }): void
}>()

const userStore = useUserStore()

const currentUser = computed(() => {
  const userInfo = userStore.userInfo || {} as any
  return {
    id: userInfo.id || 0,
    name: userInfo.nickname || userInfo.username || '未登录用户',
    nickname: userInfo.nickname || userInfo.username || '未登录用户',
    username: userInfo.username || '',
    avatar: userInfo.avatar || 'https://placeholder.co/100',
    isAdmin: userInfo.isAdmin || false
  }
})

const content = ref('')
const images = ref<CommentImage[]>([])
const mentionedUsers = ref<MentionUser[]>([])
const showEmojiPicker = ref(false)
const maxImages = 9

const handleMention = (mention: MentionUser) => {
  if (!mentionedUsers.value.find(u => u.userId === mention.userId)) {
    mentionedUsers.value.push(mention)
  }
}

const addEmoji = (emoji: EmojiItem) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  content.value += emoji.i
}

const beforeImageUpload = async (file: File): Promise<boolean> => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return false
  }

  if (images.value.length >= maxImages) {
    ElMessage.error(`最多只能上传${maxImages}张图片!`)
    return false
  }

  const validTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/bmp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  if (file.size / 1024 / 1024 >= 10) {
    ElMessage.error('上传图片大小不能超过10MB!')
    return false
  }

  try {
    const response = await uploadFiles([file]) as any
    if (response.code === 20000 && response.data?.length > 0) {
      images.value.push({ url: response.data[0], name: file.name })
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(response.info || response.message || '图片上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '图片上传失败，请重试')
  }

  return false
}

const removeImage = async (index: number) => {
  const imageToDelete = images.value[index]
  if (imageToDelete?.url && !imageToDelete.url.startsWith('data:')) {
    try {
      await deleteFiles([imageToDelete.url])
    } catch (error) {
      console.error('删除图片失败:', error)
    }
  }
  images.value.splice(index, 1)
}

const handleSubmit = (event?: KeyboardEvent) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  if (event && event.shiftKey) return
  if (!content.value.trim() && images.value.length === 0) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  emit('submit', {
    content: content.value,
    imageUrls: images.value.map(img => img.url),
    mentionUserIds: mentionedUsers.value.map(u => u.userId)
  })

  // 清空状态
  content.value = ''
  images.value = []
  mentionedUsers.value = []
}

const handleLogin = () => {
  window.dispatchEvent(new CustomEvent('show-login-dialog'))
}
</script>

<style scoped>
.comment-editor {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.editor-wrapper {
  flex: 1;
  min-width: 0;
}

.editor-wrapper :deep(.el-textarea__inner) {
  padding: 12px 16px;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
}

.editor-wrapper :deep(.el-textarea__inner:focus) {
  background: #fff;
}

.editor-footer {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.editor-tools {
  position: relative;
  display: flex;
  gap: 8px;
  align-items: center;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
}

.image-count-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.comment-image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.preview-item {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
}

.preview-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.preview-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.preview-item:hover .preview-remove {
  opacity: 1;
}

.not-login-tip {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  margin-bottom: 24px;
}
</style>
