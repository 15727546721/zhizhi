<template>
  <div class="private-message-panel">
    <ConversationList
      :conversations="conversations"
      :active-user-id="currentUserId"
      :loading="conversationLoading"
      :has-more="hasMoreConversations"
      @select="handleSelectConversation"
      @load-more="loadMoreConversations"
      @search-result-click="handleSearchResultClick"
    />

    <MessageDetail
      ref="messageDetailRef"
      :current-user-id="currentUserId"
      :conversation="currentConversation"
      :messages="messages"
      :current-user-id-num="currentUserId_computed || 0"
      :current-user-avatar="userStore.userInfo?.avatar"
      :current-user-nickname="userStore.userInfo?.nickname"
      :message-loading="messageLoading"
      :has-more="hasMore"
      :has-blocked="hasBlockedOther"
      :sending="sending"
      :dm-permission="dmPermission"
      :show-follow-button="currentUserId !== currentUserId_computed"
      @go-profile="goToUserProfile"
      @block="handleBlockUser"
      @unblock="handleUnblockUser"
      @load-more="loadMessages(true)"
      @send="handleSend"
      @upload-image="handleUploadImage"
      @message-withdrawn="handleMessageWithdrawn"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { Conversation, PrivateMessage } from '@/types'
import { usePrivateMessage } from './composables/usePrivateMessage'
import ConversationList from './ConversationList.vue'
import MessageDetail from './MessageDetail.vue'

defineOptions({ name: 'PrivateMessagePanel' })

interface Props {
  targetUserId?: number | string | null
}

const props = withDefaults(defineProps<Props>(), {
  targetUserId: null
})

const router = useRouter()

const {
  userStore,
  currentUserId_computed,
  conversations,
  messages,
  currentUserId,
  currentConversation,
  conversationLoading,
  messageLoading,
  sending,
  hasMore,
  hasBlockedOther,
  dmPermission,
  hasMoreConversations,
  loadConversations,
  loadMoreConversations,
  selectConversation,
  loadMessages,
  handleSendMessage,
  handleSendImage,
  handleBlockUser,
  handleUnblockUser,
  openConversationByUserId,
  handleMessageWithdrawn,
  registerWebSocket
} = usePrivateMessage()

// 跳转用户主页
const goToUserProfile = () => {
  if (currentUserId.value) {
    router.push(`/user/${currentUserId.value}`)
  }
}

// 选择会话
const handleSelectConversation = (conversation: Conversation) => {
  selectConversation(conversation)
}

// 发送消息
const handleSend = async (content: string) => {
  await handleSendMessage(content)
}

// 发送图片
const handleUploadImage = async (file: File) => {
  await handleSendImage(file)
}

// 搜索结果点击 - 打开对应会话
const handleSearchResultClick = async (message: PrivateMessage) => {
  // 确定对方用户ID
  const otherUserId = message.senderId === currentUserId_computed.value 
    ? message.receiverId 
    : message.senderId
  if (otherUserId) {
    await openConversationByUserId(otherUserId)
  }
}

// 初始化
let unregisterWs: (() => void) | null = null

onMounted(async () => {
  await loadConversations()
  unregisterWs = registerWebSocket()

  // 如果传入了目标用户ID，自动打开会话
  if (props.targetUserId) {
    await openConversationByUserId(Number(props.targetUserId))
  }
})

onUnmounted(() => {
  unregisterWs?.()
})

// 监听目标用户ID变化
watch(() => props.targetUserId, async (newVal) => {
  if (newVal) {
    await openConversationByUserId(Number(newVal))
  }
})
</script>

<style scoped>
.private-message-panel {
  display: flex;
  height: 100%;
  min-height: 500px;
  background: #fff;
}
</style>
