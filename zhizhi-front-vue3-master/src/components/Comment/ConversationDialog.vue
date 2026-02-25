<template>
  <el-dialog
    v-model="visible"
    title="查看对话"
    width="500px"
    :close-on-click-modal="true"
    class="conversation-dialog"
    @close="handleClose"
  >
    <div v-if="loading" class="conversation-loading">
      <Loading class="loading-icon" />
      <span>加载中...</span>
    </div>
    <div v-else-if="chain.length === 0" class="conversation-empty">
      <span>暂无对话内容</span>
    </div>
    <div v-else class="conversation-chain">
      <div v-for="(item, index) in chain" :key="item.id" class="conversation-item">
        <div class="conversation-line" v-if="index < chain.length - 1"></div>
        <div class="conversation-avatar">
          <UserAvatar :size="36" :src="item.avatar" :nickname="item.nickname" />
        </div>
        <div class="conversation-content">
          <div class="conversation-header">
            <span class="conversation-name">{{ item.nickname || '未知用户' }}</span>
            <el-tag v-if="isAuthor(item.userId)" size="small" type="primary">作者</el-tag>
            <span class="conversation-time">{{ item.formattedTime }}</span>
          </div>
          <div class="conversation-text">{{ item.content }}</div>
          <div v-if="item.imageUrls && item.imageUrls.length > 0" class="conversation-images">
            <el-image 
              v-for="(url, imgIdx) in item.imageUrls.slice(0, 3)" 
              :key="imgIdx"
              :src="url" 
              :preview-src-list="item.imageUrls"
              fit="cover"
              class="conversation-img"
            />
            <span v-if="item.imageUrls.length > 3" class="more-img">+{{ item.imageUrls.length - 3 }}</span>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElDialog, ElButton, ElTag, ElImage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { getConversationChain } from '@/api/comment'
import { formatTime } from '@/utils/time'
import type { ConversationItem } from './types'
import type { ConversationChainItem } from '@/types/api'

const props = defineProps<{
  modelValue: boolean
  replyId: number | null
  authorId?: number | string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = ref(props.modelValue)
const loading = ref(false)
const chain = ref<ConversationItem[]>([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.replyId) {
    fetchConversation()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const fetchConversation = async () => {
  if (!props.replyId) return
  
  loading.value = true
  try {
    const response = await getConversationChain(props.replyId)
    if (response.code === 20000 && response.data) {
      chain.value = response.data.map((item: ConversationChainItem) => ({
        ...item,
        formattedTime: formatTime(item.createTime)
      }))
    }
  } catch (error) {
    console.error('获取对话链失败:', error)
    ElMessage.error('获取对话链失败')
    visible.value = false
  } finally {
    loading.value = false
  }
}

const isAuthor = (userId: number | string | undefined): boolean => {
  return Number(userId) === Number(props.authorId)
}

const handleClose = () => {
  visible.value = false
  chain.value = []
}
</script>

<style scoped>
.conversation-chain {
  max-height: 400px;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  padding: 12px 0;
  position: relative;
}

.conversation-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.conversation-line {
  position: absolute;
  left: 18px;
  top: 48px;
  bottom: -12px;
  width: 2px;
  background: #e8e8e8;
}

.conversation-avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.conversation-name {
  font-weight: 500;
  color: #333;
}

.conversation-time {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}

.conversation-text {
  color: #666;
  line-height: 1.6;
  word-break: break-word;
}

.conversation-images {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.conversation-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.more-img {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 4px;
  font-size: 14px;
}

.conversation-loading,
.conversation-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
}

.conversation-loading .loading-icon {
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
