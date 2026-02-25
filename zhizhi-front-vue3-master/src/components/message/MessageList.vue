<template>
  <div
    ref="listRef"
    v-loading="loading"
    class="message-list"
    @scroll="handleScroll"
  >
    <!-- 权限提示 -->
    <div v-if="dmPermission.isGreeting" class="greeting-notice">
      你们暂未互关，对方回复或互关后可无限制发送消息
    </div>
    <div v-else-if="!dmPermission.canSend && dmPermission.reason" class="greeting-notice">
      {{ dmPermission.reason }}
    </div>

    <div v-if="messages.length === 0 && !loading" class="empty-messages">
      <el-empty description="暂无消息" :image-size="80" />
    </div>
    <div v-else class="messages">
      <div v-if="!hasMore && messages.length > 0" class="system-tip">没有更多消息了</div>

      <template v-for="(message, index) in messages" :key="message.messageId">
        <div v-if="shouldShowDateSeparator(message, index)" class="date-separator">
          {{ formatDateSeparator(message.createTime) }}
        </div>

        <MessageItem
          :message="message"
          :current-user-id="currentUserId"
          :current-user-avatar="currentUserAvatar"
          :current-user-nickname="currentUserNickname"
          @withdrawn="handleMessageWithdrawn"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { PrivateMessage, DMPermission } from '@/types'
import MessageItem from './MessageItem.vue'

defineOptions({ name: 'MessageList' })

interface Props {
  messages: PrivateMessage[]
  currentUserId: number
  currentUserAvatar?: string
  currentUserNickname?: string
  loading?: boolean
  hasMore?: boolean
  dmPermission: DMPermission
}

const props = withDefaults(defineProps<Props>(), {
  currentUserAvatar: '',
  currentUserNickname: '',
  loading: false,
  hasMore: true
})

const emit = defineEmits<{
  loadMore: []
  messageWithdrawn: [messageId: number]
}>()

const listRef = ref<HTMLDivElement | null>(null)

// 处理消息撤回
const handleMessageWithdrawn = (messageId: number) => {
  emit('messageWithdrawn', messageId)
}

// 滚动到底部
const scrollToBottom = () => {
  if (listRef.value) {
    listRef.value.scrollTop = listRef.value.scrollHeight
  }
}

// 滚动处理
const handleScroll = () => {
  if (!listRef.value || props.loading || !props.hasMore) return
  if (listRef.value.scrollTop < 50) {
    emit('loadMore')
  }
}

// 日期分隔线
const shouldShowDateSeparator = (message: PrivateMessage, index: number): boolean => {
  if (index === 0) return true
  const prev = props.messages[index - 1]
  const prevDate = new Date(prev.createTime).toDateString()
  const currDate = new Date(message.createTime).toDateString()
  return prevDate !== currDate
}

const formatDateSeparator = (time: string): string => {
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const isYesterday = date.toDateString() === new Date(now.getTime() - 86400000).toDateString()

  if (isToday) return '今天'
  if (isYesterday) return '昨天'
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}

// 暴露方法给父组件
defineExpose({ scrollToBottom })

// 监听消息变化自动滚动
watch(() => props.messages.length, (newLen, oldLen) => {
  if (newLen > oldLen) {
    scrollToBottom()
  }
}, { flush: 'post' })
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
  min-height: 0;
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.greeting-notice {
  text-align: center;
  padding: 8px 12px;
  background: #fff8e6;
  color: #fa8c16;
  font-size: 12px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.system-tip {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 8px;
}

.date-separator {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 8px 0;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 200px;
}
</style>
