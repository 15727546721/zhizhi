<template>
  <div class="reply-editor" :data-comment-id="replyTo.id">
    <div v-if="images.length > 0" class="reply-image-preview">
      <div v-for="(image, index) in images" :key="index" class="preview-item">
        <el-image :src="image.url" fit="cover" />
        <div class="preview-remove" @click="removeImage(index)">
          <el-icon><Close /></el-icon>
        </div>
      </div>
    </div>
    
    <el-input
      v-model="content"
      type="textarea"
      :rows="2"
      :placeholder="`回复 ${replyTo.user?.nickname || replyTo.user?.username || replyTo.nickname || '未知用户'}`"
      resize="none"
    />
    <div class="reply-editor-footer">
      <div class="reply-tools">
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
      <div class="reply-buttons">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :disabled="!content.trim() && images.length === 0" @click="handleSubmit">
          发表回复
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElButton, ElInput } from 'element-plus'
import { Close, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import EmojiSelector from '@/components/EmojiSelector/index.vue'
import { uploadFiles, deleteFiles } from '@/api/file'
import type { CommentItem, CommentImage, EmojiItem } from './types'

const props = defineProps<{
  replyTo: CommentItem
  parentId: number
}>()

const emit = defineEmits<{
  (e: 'submit', data: { content: string; imageUrls: string[]; parentId: number; replyUserId: number }): void
  (e: 'cancel'): void
}>()

const userStore = useUserStore()

const content = ref('')
const images = ref<CommentImage[]>([])
const showEmojiPicker = ref(false)
const maxImages = 9

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

const handleSubmit = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  if (!content.value.trim() && images.value.length === 0) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  emit('submit', {
    content: content.value,
    imageUrls: images.value.map(img => img.url),
    parentId: props.parentId,
    replyUserId: props.replyTo.userId
  })

  content.value = ''
  images.value = []
}

const handleCancel = () => {
  content.value = ''
  images.value = []
  emit('cancel')
}
</script>

<style scoped>
.reply-editor {
  margin-top: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
}

.reply-editor-footer {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.reply-tools {
  position: relative;
  display: flex;
  gap: 8px;
  align-items: center;
}

.reply-buttons {
  display: flex;
  gap: 8px;
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

.reply-image-preview {
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
</style>
