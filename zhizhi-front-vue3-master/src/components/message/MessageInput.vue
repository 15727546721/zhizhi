<template>
  <div class="message-input-area">
    <div class="message-input-tools">
      <EmojiSelector v-model="showEmojiPicker" icon="😊" text="表情" @select="addEmoji" />
      <el-upload
        :show-file-list="false"
        :before-upload="beforeUploadImage"
        :http-request="handleUploadImage"
        :disabled="!canSend"
      >
        <div class="tool-item" :class="{ disabled: !canSend }">
          <el-icon><Picture /></el-icon>
          <span>图片</span>
        </div>
      </el-upload>
    </div>

    <el-input
      ref="inputRef"
      v-model="inputMessage"
      type="textarea"
      :rows="2"
      :maxlength="maxLength"
      show-word-limit
      placeholder="输入聊天内容"
      :disabled="sending"
      resize="none"
      @keydown.enter.exact.prevent="handleSend"
    />

    <div class="message-input-footer">
      <span v-if="!canSend" class="permission-tip">{{ permissionReason || '无法发送' }}</span>
      <span v-else class="send-hint">按 Enter 发送</span>
      <el-button
        type="primary"
        :loading="sending"
        :disabled="!canSend || !inputMessage.trim()"
        size="small"
        @click="handleSend"
      >
        发 送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { InputInstance } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import EmojiSelector from '@/components/EmojiSelector/index.vue'

defineOptions({ name: 'MessageInput' })

interface Props {
  canSend?: boolean
  permissionReason?: string
  sending?: boolean
  maxLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  canSend: true,
  permissionReason: '',
  sending: false,
  maxLength: 500
})

const emit = defineEmits<{
  send: [content: string]
  uploadImage: [file: File]
}>()

const inputRef = ref<InputInstance | null>(null)
const inputMessage = ref('')
const showEmojiPicker = ref(false)

const handleSend = () => {
  if (!inputMessage.value.trim() || props.sending || !props.canSend) return
  emit('send', inputMessage.value.trim())
  inputMessage.value = ''
}

const addEmoji = (emoji: string) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
}

const beforeUploadImage = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('只能上传图片')
  if (!isLt5M) ElMessage.error('图片大小不能超过5MB')
  return isImage && isLt5M
}

const handleUploadImage = ({ file }: { file: File }) => {
  emit('uploadImage', file)
}

// 暴露方法
defineExpose({
  focus: () => inputRef.value?.focus(),
  clear: () => { inputMessage.value = '' }
})
</script>

<style scoped>
.message-input-area {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
}

.message-input-tools {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.tool-item:hover {
  color: #1890ff;
}

.tool-item.disabled {
  color: #ccc;
  cursor: not-allowed;
}

.message-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.send-hint,
.permission-tip {
  font-size: 12px;
  color: #999;
}

.permission-tip {
  color: #ff4d4f;
}
</style>
