import { ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import type { Conversation, PrivateMessage, DMPermission } from '@/types'
import {
  getConversationList,
  getOrCreateConversation,
  getMessages,
  sendMessage,
  markAsRead,
  checkDMPermission
} from '@/api/message'
import { blockUser, unblockUser, isBlocked as checkBlockStatus } from '@/api/block'
import { uploadImage } from '@/api/file'
import { onPrivateMessage, offPrivateMessage, type PrivateMessageData } from '@/utils/websocket'

export const MAX_MESSAGE_LENGTH = 500
export const PAGE_SIZE = 30
export const CONVERSATION_PAGE_SIZE = 20

export function usePrivateMessage() {
  const userStore = useUserStore()
  const currentUserId_computed = computed(() => userStore.userInfo?.id)

  // 状态
  const conversations = ref<Conversation[]>([])
  const messages = ref<PrivateMessage[]>([])
  const currentUserId = ref<number | null>(null)
  const currentConversation = ref<Conversation | null>(null)
  const conversationLoading = ref(false)
  const messageLoading = ref(false)
  const sending = ref(false)
  const hasMore = ref(true)
  const hasBlockedOther = ref(false)
  const dmPermission = ref<DMPermission>({ canSend: true, isGreeting: false, reason: '' })
  
  // 会话分页状态
  const conversationPage = ref(1)
  const conversationTotal = ref(0)
  const hasMoreConversations = ref(true)

  // 加载会话列表
  const loadConversations = async (append = false) => {
    try {
      conversationLoading.value = true
      const page = append ? conversationPage.value + 1 : 1
      const res = await getConversationList({ page, size: CONVERSATION_PAGE_SIZE })
      if (res.code === 20000 && res.data) {
        const data = res.data
        if (append) {
          conversations.value = [...conversations.value, ...data.records]
        } else {
          conversations.value = data.records || []
        }
        conversationPage.value = page
        conversationTotal.value = data.total || 0
        hasMoreConversations.value = data.hasMore ?? (page * CONVERSATION_PAGE_SIZE < data.total)
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    } finally {
      conversationLoading.value = false
    }
  }
  
  // 加载更多会话
  const loadMoreConversations = async () => {
    if (!hasMoreConversations.value || conversationLoading.value) return
    await loadConversations(true)
  }

  // 选择会话
  const selectConversation = async (conversation: Conversation, scrollCallback?: () => void) => {
    currentUserId.value = conversation.userId
    currentConversation.value = conversation
    messages.value = []
    hasMore.value = true

    await loadMessages()
    await markMessagesAsRead()
    await checkIfBlockedOther()
    await checkDMPermissionStatus()

    if (scrollCallback) {
      nextTick(scrollCallback)
    }
  }

  // 加载消息
  const loadMessages = async (append = false) => {
    if (!currentUserId.value) return

    try {
      messageLoading.value = true
      const offset = append ? messages.value.length : 0
      const res = await getMessages(currentUserId.value, { 
        page: Math.floor(offset / PAGE_SIZE) + 1, 
        size: PAGE_SIZE 
      })

      if (res.code === 20000) {
        const newMessages = (res.data as any)?.records || res.data || []
        if (append) {
          messages.value = [...newMessages, ...messages.value]
        } else {
          messages.value = newMessages
        }
        hasMore.value = newMessages.length >= PAGE_SIZE
      }
    } catch (error) {
      console.error('加载消息失败:', error)
    } finally {
      messageLoading.value = false
    }
  }

  // 发送消息
  const handleSendMessage = async (content: string, scrollCallback?: () => void) => {
    if (!content.trim() || sending.value || !dmPermission.value.canSend) return false
    if (!currentUserId.value) return false

    if (content.length > MAX_MESSAGE_LENGTH) {
      ElMessage.warning(`消息长度不能超过${MAX_MESSAGE_LENGTH}字`)
      return false
    }

    try {
      sending.value = true
      const res = await sendMessage(currentUserId.value, content.trim())

      if (res.code === 20000) {
        await loadMessages()
        await loadConversations()
        if (scrollCallback) nextTick(scrollCallback)

        if (dmPermission.value.isGreeting) {
          dmPermission.value = { canSend: false, isGreeting: false, reason: '等待对方回复' }
        }
        return true
      } else {
        ElMessage.error((res as any).message || '发送失败')
        return false
      }
    } catch (error) {
      console.error('发送失败:', error)
      return false
    } finally {
      sending.value = false
    }
  }

  // 发送图片（使用 JSON 格式，与后端保持一致）
  const handleSendImage = async (file: File, scrollCallback?: () => void) => {
    if (!currentUserId.value) return false

    try {
      sending.value = true
      const res = await uploadImage(file)
      if (res.code === 20000 && res.data?.url) {
        // 使用 JSON 格式，与后端 PrivateMessageService 保持一致
        const imageContent = JSON.stringify({ type: 'image', url: res.data.url })
        await sendMessage(currentUserId.value, imageContent)
        await loadMessages()
        await loadConversations()
        if (scrollCallback) nextTick(scrollCallback)
        return true
      }
      return false
    } catch (error) {
      console.error('图片发送失败:', error)
      return false
    } finally {
      sending.value = false
    }
  }

  // 标记已读
  const markMessagesAsRead = async () => {
    if (!currentUserId.value) return
    try {
      await markAsRead(currentUserId.value)
      const conv = conversations.value.find(c => c.userId === currentUserId.value)
      if (conv) conv.unreadCount = 0
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 检查是否屏蔽
  const checkIfBlockedOther = async () => {
    if (!currentUserId.value) return
    try {
      const res = await checkBlockStatus(currentUserId.value)
      hasBlockedOther.value = res.code === 20000 && res.data === true
    } catch (error) {
      hasBlockedOther.value = false
    }
  }

  // 检查私信权限
  const checkDMPermissionStatus = async () => {
    // 严格检查 currentUserId 是否为有效数字
    if (!currentUserId.value || typeof currentUserId.value !== 'number') {
      console.warn('[私信] checkDMPermissionStatus: currentUserId 无效', currentUserId.value)
      return
    }
    try {
      const res = await checkDMPermission(currentUserId.value)
      if (res.code === 20000) {
        dmPermission.value = res.data || { canSend: true, isGreeting: false, reason: '' }
      }
    } catch (error) {
      dmPermission.value = { canSend: true, isGreeting: false, reason: '' }
    }
  }

  // 屏蔽/取消屏蔽用户
  const handleBlockUser = async () => {
    if (!currentUserId.value) return
    try {
      await ElMessageBox.confirm('确定要屏蔽该用户吗？', '提示', { type: 'warning' })
      const res = await blockUser(currentUserId.value)
      if (res.code === 20000) {
        hasBlockedOther.value = true
        ElMessage.success('已屏蔽')
      }
    } catch (error) {
      if (error !== 'cancel') console.error('屏蔽操作失败:', error)
    }
  }

  const handleUnblockUser = async () => {
    if (!currentUserId.value) return
    try {
      const res = await unblockUser(currentUserId.value)
      if (res.code === 20000) {
        hasBlockedOther.value = false
        ElMessage.success('已取消屏蔽')
      }
    } catch (error) {
      console.error('取消屏蔽失败:', error)
    }
  }

  // 通过用户ID打开会话
  const openConversationByUserId = async (targetId: number, scrollCallback?: () => void) => {
    let conv = conversations.value.find(c => c.userId === targetId)
    if (!conv) {
      const res = await getOrCreateConversation(targetId)
      if (res.code === 20000) {
        await loadConversations()
        conv = conversations.value.find(c => c.userId === targetId)
      }
    }
    if (conv) {
      await selectConversation(conv, scrollCallback)
    }
  }

  // WebSocket 消息处理
  const handleWebSocketMessage = (data: PrivateMessageData, scrollCallback?: () => void) => {
    if (data.type === 'private_message' && data.senderId) {
      updateConversationFromWs(data)
      if (currentUserId.value === data.senderId) {
        messages.value.push({
          messageId: data.messageId || Date.now(),
          senderId: data.senderId,
          receiverId: currentUserId_computed.value || 0,
          senderNickname: data.senderName || '',
          senderAvatar: data.senderAvatar || '',
          content: data.content || '',
          createTime: data.timestamp || new Date().toISOString(),
          status: 1
        } as PrivateMessage)
        if (scrollCallback) nextTick(scrollCallback)
        markMessagesAsRead()
      }
    }
  }

  const updateConversationFromWs = (data: PrivateMessageData) => {
    const senderId = data.senderId
    if (!senderId) return
    
    const existingIndex = conversations.value.findIndex(c => c.userId === senderId)

    if (existingIndex >= 0) {
      const conv = conversations.value[existingIndex]
      conv.lastMessage = data.content
      conv.lastMessageTime = data.timestamp || new Date().toISOString()
      if (currentUserId.value !== senderId) {
        conv.unreadCount = (conv.unreadCount || 0) + 1
      }
      conversations.value.splice(existingIndex, 1)
      conversations.value.unshift(conv)
    } else {
      conversations.value.unshift({
        conversationId: 0,
        recipientId: 0,
        recipientName: '',
        recipientAvatar: '',
        userId: senderId,
        userName: data.senderName || '',
        userAvatar: data.senderAvatar || '',
        lastMessage: data.content || '',
        lastMessageTime: data.timestamp || new Date().toISOString(),
        unreadCount: 1
      })
    }
  }

  // 处理消息撤回
  const handleMessageWithdrawn = (messageId: number) => {
    const index = messages.value.findIndex(m => m.messageId === messageId)
    if (index >= 0) {
      // 更新消息状态为已撤回
      messages.value[index] = {
        ...messages.value[index],
        status: 4,
        content: '[消息已撤回]'
      }
    }
  }

  // 注册/注销 WebSocket 监听
  const registerWebSocket = (scrollCallback?: () => void) => {
    const handler = (data: PrivateMessageData) => handleWebSocketMessage(data, scrollCallback)
    onPrivateMessage(handler)
    return () => offPrivateMessage(handler)
  }

  return {
    // 状态
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
    // 会话分页状态
    conversationPage,
    conversationTotal,
    hasMoreConversations,
    // 方法
    loadConversations,
    loadMoreConversations,
    selectConversation,
    loadMessages,
    handleSendMessage,
    handleSendImage,
    markMessagesAsRead,
    handleBlockUser,
    handleUnblockUser,
    openConversationByUserId,
    handleMessageWithdrawn,
    registerWebSocket
  }
}
