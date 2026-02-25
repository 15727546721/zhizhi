<template>
  <div class="message-detail">
    <div v-if="currentUserId" class="message-detail-content">
      <MessageHeader
        :user-id="currentUserId"
        :user-name="conversation?.userName"
        :avatar="conversation?.userAvatar"
        :has-blocked="hasBlocked"
        :show-follow-button="showFollowButton"
        @go-profile="$emit('goProfile')"
        @block="$emit('block')"
        @unblock="$emit('unblock')"
      />

      <MessageList
        ref="messageListRef"
        :messages="messages"
        :current-user-id="currentUserIdNum"
        :current-user-avatar="currentUserAvatar"
        :current-user-nickname="currentUserNickname"
        :loading="messageLoading"
        :has-more="hasMore"
        :dm-permission="dmPermission"
        @load-more="$emit('loadMore')"
        @message-withdrawn="$emit('messageWithdrawn', $event)"
      />

      <MessageInput
        ref="messageInputRef"
        :can-send="dmPermission.canSend"
        :permission-reason="dmPermission.reason"
        :sending="sending"
        @send="$emit('send', $event)"
        @upload-image="$emit('uploadImage', $event)"
      />
    </div>
    <div v-else class="message-placeholder">
      <el-icon :size="48" color="#ddd"><Message /></el-icon>
      <p>选择对话开始聊天</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Message } from '@element-plus/icons-vue'
import type { Conversation, PrivateMessage, DMPermission } from '@/types'
import MessageHeader from './MessageHeader.vue'
import MessageList from './MessageList.vue'
import MessageInput from './MessageInput.vue'

defineOptions({ name: 'MessageDetail' })

interface Props {
  currentUserId: number | null
  conversation: Conversation | null
  messages: PrivateMessage[]
  currentUserIdNum: number
  currentUserAvatar?: string
  currentUserNickname?: string
  messageLoading?: boolean
  hasMore?: boolean
  hasBlocked?: boolean
  sending?: boolean
  dmPermission: DMPermission
  showFollowButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentUserAvatar: '',
  currentUserNickname: '',
  messageLoading: false,
  hasMore: true,
  hasBlocked: false,
  sending: false,
  showFollowButton: true
})

defineEmits<{
  goProfile: []
  block: []
  unblock: []
  loadMore: []
  send: [content: string]
  uploadImage: [file: File]
  messageWithdrawn: [messageId: number]
}>()

const messageListRef = ref<InstanceType<typeof MessageList> | null>(null)
const messageInputRef = ref<InstanceType<typeof MessageInput> | null>(null)

// 暴露方法
defineExpose({
  scrollToBottom: () => messageListRef.value?.scrollToBottom(),
  focusInput: () => messageInputRef.value?.focus()
})
</script>

<style scoped>
.message-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.message-detail-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.message-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
}

.message-placeholder p {
  margin-top: 12px;
  font-size: 14px;
}
</style>
