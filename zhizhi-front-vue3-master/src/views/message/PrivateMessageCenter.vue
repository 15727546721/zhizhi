<template>
  <div class="private-message-center">
    <div class="message-container">
      <!-- 左侧对话列表 -->
      <div class="conversation-list">
        <div class="conversation-header">
          <el-input
            v-model="conversationSearchKeyword"
            placeholder="搜索"
            :prefix-icon="Search"
            clearable
          />
        </div>
        <div v-loading="conversationLoading" class="conversation-content" @scroll="handleConversationScroll">
          <div
            v-for="conversation in filteredConversations"
            :key="conversation.userId"
            class="conversation-item"
            :class="{ active: currentUserId === conversation.userId }"
            @click="selectConversation(conversation)"
          >
            <UserAvatar :size="44" :src="conversation.userAvatar" :nickname="conversation.userName" />
            <div class="conversation-info">
              <div class="conversation-name-row">
                <span class="conversation-name">{{ conversation.userName }}</span>
                <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
              </div>
              <div class="conversation-preview-row">
                <span class="conversation-preview">{{ conversation.lastMessage || '暂无消息' }}</span>
                <span v-if="conversation.unreadCount > 0" class="unread-badge">
                  {{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}
                </span>
              </div>
            </div>
          </div>
          <div v-if="filteredConversations.length === 0 && !conversationLoading" class="empty-state">
            <el-empty :description="conversationSearchKeyword ? '未找到相关对话' : '暂无对话'" :image-size="80" />
          </div>
        </div>
      </div>

      <!-- 右侧消息详情 -->
      <div class="message-detail">
        <div v-if="currentUserId" class="message-detail-content">
          <!-- 消息头部 -->
          <div class="message-header">
            <div class="message-header-left">
              <UserAvatar 
                :size="36" 
                :src="currentConversation?.userAvatar" 
                :nickname="currentConversation?.userName"
                custom-class="header-avatar"
                @click="goToUserProfile"
              />
              <span class="message-header-name" @click="goToUserProfile">{{ currentConversation?.userName || '用户' }}</span>
            </div>
            <div class="message-header-right">
              <!-- 消息搜索 -->
              <el-popover
                v-model:visible="showMessageSearch"
                placement="bottom-end"
                :width="320"
                trigger="click"
              >
                <template #reference>
                  <el-button :icon="Search" circle size="small" title="搜索消息" />
                </template>
                <div class="message-search-panel">
                  <el-input
                    v-model="messageSearchKeyword"
                    placeholder="搜索消息内容"
                    :prefix-icon="Search"
                    clearable
                    @keyup.enter="handleMessageSearch"
                  />
                  <div v-if="searchResults.length > 0" class="search-results">
                    <div
                      v-for="result in searchResults"
                      :key="result.messageId"
                      class="search-result-item"
                      @click="jumpToMessage(result)"
                    >
                      <div class="result-content">{{ result.content }}</div>
                      <div class="result-time">{{ formatMessageTime(result.createTime) }}</div>
                    </div>
                  </div>
                  <div v-else-if="messageSearchKeyword && searchResults.length === 0" class="no-results">
                    未找到相关消息
                  </div>
                </div>
              </el-popover>
              
              <FollowButton 
                v-if="currentUserId && currentUserId !== currentUserId_computed"
                :user-id="currentUserId"
                size="small"
              />
              <el-dropdown trigger="click" @command="handleUserAction">
                <el-button :icon="More" circle size="small" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="!hasBlockedOther" command="block">
                      屏蔽用户
                    </el-dropdown-item>
                    <el-dropdown-item v-else command="unblock">
                      取消屏蔽
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            ref="messageListRef"
            v-loading="messageLoading"
            class="message-list"
            @scroll="handleScroll"
          >
            <!-- 权限提示：打招呼消息状态 -->
            <div v-if="dmPermission.isGreeting" class="greeting-notice">
              你们暂未互关，对方回复或互关后可无限制发送消息
            </div>
            <!-- 权限提示：已发送过消息，等待回复 -->
            <div v-else-if="!dmPermission.canSend && dmPermission.reason" class="greeting-notice">
              {{ dmPermission.reason }}
            </div>
            
            <div v-if="messages.length === 0 && !messageLoading" class="empty-messages">
              <el-empty description="暂无消息" :image-size="100" />
            </div>
            <div v-else class="messages">
              <!-- 没有更多消息提示 -->
              <div v-if="!hasMore && messages.length > 0" class="system-tip">
                没有更多消息了
              </div>
              
              <template v-for="(message, index) in messages" :key="message.messageId">
                <!-- 日期分隔线 -->
                <div v-if="shouldShowDateSeparator(message, index)" class="date-separator">
                  {{ formatDateSeparator(message.createTime) }}
                </div>
                
                <!-- 消息项 -->
                <div
                  class="message-item"
                  :class="{ 
                    'message-sent': message.senderId === currentUserId_computed, 
                    'message-received': message.senderId !== currentUserId_computed 
                  }"
                  @mouseenter="hoveredMessageId = message.messageId"
                  @mouseleave="hoveredMessageId = null"
                >
                  <!-- 对方头像（左侧，显示对方发送的消息） -->
                  <UserAvatar
                    v-if="message.senderId !== currentUserId_computed"
                    :size="40"
                    :src="message.senderAvatar"
                    :nickname="message.senderNickname || currentConversation?.userName"
                    custom-class="message-avatar"
                  />
                  
                  <!-- 消息内容 -->
                  <div class="message-content-wrapper">
                    <!-- 消息状态图标（发送方消息，显示在气泡左上角） -->
                    <el-tooltip
                      v-if="message.senderId === currentUserId_computed && (message.status === 2 || message.status === 3)"
                      :content="message.status === 2 ? '对方主动回复或关注你之前，最多发送1条消息' : '对方已屏蔽，消息不可见'"
                      placement="top"
                    >
                      <span class="message-status-icon">
                        <el-icon color="#ff4d4f"><Warning /></el-icon>
                      </span>
                    </el-tooltip>
                    
                    <div 
                      class="message-bubble"
                      :class="{ 
                        'message-bubble--image': parseMessageContent(message.content).type === 'image',
                        'message-bubble--withdrawn': message.status === 4
                      }"
                    >
                      <template v-if="message.status === 4">
                        <span class="message-text withdrawn-text">消息已撤回</span>
                      </template>
                      <template v-else-if="parseMessageContent(message.content).type === 'image'">
                        <el-image
                          class="message-image"
                          :src="parseMessageContent(message.content).url"
                          :preview-src-list="[parseMessageContent(message.content).url]"
                          fit="contain"
                        />
                      </template>
                      <template v-else>
                        <span class="message-text">{{ message.content }}</span>
                      </template>
                    </div>
                    
                    <!-- 撤回按钮（仅自己发送的消息，2分钟内可撤回） -->
                    <div 
                      v-if="message.senderId === currentUserId_computed && 
                             message.status !== 4 && 
                             canWithdraw(message) &&
                             hoveredMessageId === message.messageId"
                      class="message-actions"
                    >
                      <el-button
                        size="small"
                        text
                        @click="handleWithdrawMessage(message)"
                      >
                        撤回
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 我的头像（右侧，显示我发送的消息） -->
                  <UserAvatar
                    v-if="message.senderId === currentUserId_computed"
                    :size="40"
                    :src="currentUserStore.userInfo?.avatar"
                    :username="currentUserStore.userInfo?.username"
                    :nickname="currentUserStore.userInfo?.nickname"
                    custom-class="message-avatar"
                  />
                </div>
              </template>
            </div>
          </div>

          <!-- 消息输入框 -->
          <div class="message-input-area">
            <div class="message-input-tools">
              <EmojiSelector v-model="showEmojiPicker" icon="😊" text="表情" @select="addEmoji" />
              <el-upload
                :show-file-list="false"
                :before-upload="beforeUploadImage"
                :http-request="doUploadImage"
                :disabled="!dmPermission.canSend"
              >
                <div class="tool-item" :class="{ disabled: !dmPermission.canSend }">
                  <el-icon><Picture /></el-icon>
                  <span>图片</span>
                </div>
              </el-upload>
            </div>
            
            <el-input
              ref="messageInputRef"
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :maxlength="MAX_MESSAGE_LENGTH"
              show-word-limit
              placeholder="输入聊天内容"
              :disabled="sending"
              resize="none"
              class="custom-textarea"
              @keydown.enter.shift.exact.prevent
              @keydown.enter.exact.prevent="handleSendMessage"
            />
            
            <div class="message-input-footer">
              <!-- 权限提示 -->
              <span v-if="!dmPermission.canSend" class="permission-tip">
                {{ dmPermission.reason || '无法发送' }}
              </span>
              <span v-else class="send-hint">按 Enter 发送消息</span>
              <el-button 
                type="primary" 
                :loading="sending"
                :disabled="!dmPermission.canSend || !inputMessage.trim()"
                @click="handleSendMessage"
                class="send-button"
              >
                发 送
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="message-placeholder">
          <div class="empty-wrapper">
            <el-icon :size="64" color="#e0e0e0"><Message /></el-icon>
            <p>暂时还没有消息或还未选中联系人</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { More, Warning, Promotion, Search, Picture, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import {
  getConversationList,
  getOrCreateConversation,
  getMessages,
  sendMessage,
  markAsRead,
  checkDMPermission,
  withdrawMessage,
  searchMessages,
  MessageStatus
} from '@/api/message'
import {
  blockUser,
  unblockUser,
  isBlocked as checkBlockStatus
} from '@/api/block'
import { uploadImage } from '@/api/file'
import { onPrivateMessage, offPrivateMessage, isWebSocketConnected } from '@/utils/websocket'
import { eventBus, EVENT_REFRESH_MESSAGE_COUNT } from '@/utils/eventBus'
import EmojiSelector from '@/components/EmojiSelector/index.vue'
import FollowButton from '@/components/FollowButton/index.vue'
import UserAvatar from '@/components/UserAvatar.vue'

defineOptions({
  name: 'PrivateMessageCenter'
})

// 跳转到用户主页
const goToUserProfile = () => {
  if (currentUserId.value) {
    router.push(`/user/${currentUserId.value}`)
  }
}

const currentUserStore = useUserStore()
const route = useRoute()
const router = useRouter()
// 默认头像（用于通知和虚拟会话）
const defaultAvatar = ''

// 获取当前用户ID
const currentUserId_computed = computed(() => currentUserStore.userInfo?.id)

// 对话列表
const conversations = ref([])
const conversationSearchKeyword = ref('')
const conversationLoading = ref(false)
const currentUserId = ref(null)
const currentConversation = ref(null)
const hasBlockedOther = ref(false) // 当前用户是否屏蔽了对方

// 会话列表分页状态
const conversationPage = ref(1)
const conversationPageSize = ref(20)
const conversationTotal = ref(0)
const hasMoreConversations = ref(true)

// 私信权限状态
const dmPermission = ref({
  canSend: true,      // 是否可以发送私信
  isGreeting: false,  // 是否是打招呼消息（首次私信）
  reason: ''          // 不可发送的原因
})

// 过滤后的对话列表
const filteredConversations = computed(() => {
  if (!conversationSearchKeyword.value) {
    return conversations.value
  }
  const keyword = conversationSearchKeyword.value.toLowerCase()
  return conversations.value.filter(c => 
    (c.userName && c.userName.toLowerCase().includes(keyword)) || 
    (c.lastMessage && c.lastMessage.toLowerCase().includes(keyword))
  )
})

// 消息列表
const messages = ref([])
const messageLoading = ref(false)
const messageListRef = ref(null)
const pageNo = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 消息输入
const inputMessage = ref('')
const sending = ref(false)
const messageInputRef = ref(null)
const showEmojiPicker = ref(false)
const MAX_MESSAGE_LENGTH = 1000  // 与后端保持一致
const lastSendTime = ref(0)  // 防重复发送

// 消息搜索
const showMessageSearch = ref(false)
const messageSearchKeyword = ref('')
const searchResults = ref([])

// 消息悬停状态（用于显示撤回按钮）
const hoveredMessageId = ref(null)

// 解析消息内容，支持图片(JSON)与文本
const parseMessageContent = (content) => {
  if (!content) return { type: 'text', text: '' }
  try {
    const obj = JSON.parse(content)
    if (obj && obj.type === 'image' && obj.url) {
      return { type: 'image', url: obj.url, width: obj.width, height: obj.height }
    }
  } catch (e) {
    // 不是 JSON 格式，当作普通文本处理
  }
  return { type: 'text', text: content }
}

// 加载对话列表
const loadConversations = async (append = false) => {
  try {
    conversationLoading.value = true
    const page = append ? conversationPage.value + 1 : 1
    const res = await getConversationList({
      page,
      size: conversationPageSize.value
    })
    if (res.code === 20000 && res.data) {
      // 后端返回的是 ConversationPageVO 结构
      const pageData = res.data
      const newConversations = pageData.records || []
      
      if (append) {
        conversations.value = [...conversations.value, ...newConversations]
      } else {
        conversations.value = newConversations
      }
      
      conversationPage.value = page
      conversationTotal.value = pageData.total || 0
      hasMoreConversations.value = pageData.hasMore ?? false
      
      // 检查路由参数，如果有userId参数，自动打开该用户的对话
      if (!append) {
        const targetUserId = route.query.userId
        if (targetUserId) {
          const userId = Number(targetUserId)
          // 查找是否已有该用户的对话
        let existingConversation = conversations.value.find(c => c.userId === userId)
        
        if (!existingConversation) {
          // 如果没有对话，调用后端API创建对话
          try {
            const createRes = await getOrCreateConversation(userId)
            if (createRes.code === 20000 && createRes.data) {
              // 创建成功，添加到列表顶部
              existingConversation = createRes.data
              conversations.value.unshift(existingConversation)
            }
          } catch (error) {
            ElMessage.error('创建对话失败')
          }
        }
        
        // 选中对话
        if (existingConversation) {
          await selectConversation(existingConversation)
        }
        
        // 清除路由参数
        router.replace({ path: route.path, query: {} })
      }
      }
      // 不自动选中任何会话，让用户自己选择
    }
  } catch (error) {
    // 加载失败
  } finally {
    conversationLoading.value = false
  }
}

// 加载更多会话
const loadMoreConversations = async () => {
  if (!hasMoreConversations.value || conversationLoading.value) return
  await loadConversations(true)
}

// 发送图片：上传前校验
const beforeUploadImage = (file) => {
  const isImage = file?.type?.startsWith('image/')
  const isLt5M = file?.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// 发送图片：自定义上传并发送消息
const doUploadImage = async (options) => {
  const { file, onError, onSuccess } = options || {}
  try {
    if (!currentUserId.value) throw new Error('未选择会话')
    const uploadRes = await uploadImage(file)
    // 处理不同返回结构：{code, data:[url]} 或 {code, data: url}
    let url = ''
    if (uploadRes) {
      const d = uploadRes.data ?? uploadRes?.data?.data
      if (Array.isArray(d)) {
        url = d[0] || ''
      } else if (typeof d === 'string') {
        url = d
      }
    }
    if (!url) throw new Error('获取图片URL失败')

    const payload = JSON.stringify({ type: 'image', url })
    const res = await sendMessage(currentUserId.value, payload)
    if (res.code === 20000) {
      // 重新加载消息与对话
      pageNo.value = 1
      hasMore.value = true
      await loadMessages()
      if (res.data?.message) {
        if (res.data.status !== 1) {
          ElMessage.warning(res.data.message)
        } else {
          ElMessage.success(res.data.message)
        }
      }
      await loadConversations()
      await nextTick()
      focusInput()
    }
    onSuccess && onSuccess({}, file)
  } catch (e) {
    onError && onError(e)
  }
}

// 表情选择
const addEmoji = (emoji) => {
  inputMessage.value = (inputMessage.value || '') + emoji.i
  nextTick(() => {
    focusInput()
  })
}

// 选择对话
const selectConversation = async (conversation) => {
  currentUserId.value = conversation.userId
  currentConversation.value = conversation
  pageNo.value = 1
  hasMore.value = true
  messages.value = []
  
  // 加载消息
  await loadMessages()
  await markMessagesAsRead()
  
  // 检查当前用户是否屏蔽了对方（用于菜单显示）
  await checkIfBlockedOther()
  
  // 检查私信权限（后端统一处理所有权限逻辑，包括屏蔽检查）
  try {
    if (conversation.userId && typeof conversation.userId === 'number') {
      const permissionRes = await checkDMPermission(conversation.userId)
      if (permissionRes.code === 20000 && permissionRes.data) {
        dmPermission.value = {
          canSend: permissionRes.data.canSend ?? true,
          isGreeting: permissionRes.data.isGreeting ?? false,
          reason: permissionRes.data.reason || ''
        }
      } else {
        // 默认有权限
        dmPermission.value = { canSend: true, isGreeting: false, reason: '' }
      }
    } else {
      dmPermission.value = { canSend: true, isGreeting: false, reason: '' }
    }
  } catch (error) {
    // 默认有权限
    dmPermission.value = { canSend: true, isGreeting: false, reason: '' }
  }
}

// 加载消息列表
const loadMessages = async (append = false) => {
  if (!currentUserId.value || messageLoading.value) {
    return
  }

  if (!append && !hasMore.value && messages.value.length > 0) {
    return
  }

  try {
    messageLoading.value = true
    const res = await getMessages(currentUserId.value, {
      page: pageNo.value,
      size: pageSize.value
    })
    if (res.code === 20000) {
      const newMessages = res.data || []
      // 后端返回 ASC 排序（最早→最新）
      if (append) {
        // 加载历史消息，插入到前面
        const oldScrollHeight = messageListRef.value?.scrollHeight || 0
        messages.value = [...newMessages, ...messages.value]
        // 保持滚动位置
        await nextTick()
        if (messageListRef.value) {
          const newScrollHeight = messageListRef.value.scrollHeight
          messageListRef.value.scrollTop = newScrollHeight - oldScrollHeight
        }
      } else {
        // 加载最新消息
        messages.value = newMessages
        // 滚动到底部
        await nextTick()
        scrollToBottom()
      }
      hasMore.value = newMessages.length === pageSize.value
    }
  } catch (error) {
    // 加载失败
  } finally {
    messageLoading.value = false
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value || !currentUserId.value) {
    return
  }

  // 检查权限
  if (!dmPermission.value.canSend) {
    ElMessage.warning(dmPermission.value.reason || '无法发送私信')
    return
  }
  
  // 消息长度检查
  if (inputMessage.value.length > MAX_MESSAGE_LENGTH) {
    ElMessage.warning(`消息内容不能超过${MAX_MESSAGE_LENGTH}字`)
    return
  }
  
  // 防重复发送（1秒内不能重复发送）
  const now = Date.now()
  if (now - lastSendTime.value < 1000) {
    ElMessage.warning('发送太快，请稍后再试')
    return
  }
  lastSendTime.value = now

  try {
    sending.value = true
    const wasGreeting = dmPermission.value.isGreeting // 记录是否是打招呼消息
    const res = await sendMessage(currentUserId.value, inputMessage.value.trim())
    if (res.code === 20000) {
      inputMessage.value = ''
      // 重新加载消息
      pageNo.value = 1
      hasMore.value = true
      await loadMessages()
      
      // 如果是打招呼消息发送成功，更新权限状态（等待对方回复）
      if (wasGreeting) {
        dmPermission.value = {
          canSend: false,
          isGreeting: false,
          reason: '已发送消息，等待对方回复后可继续发送'
        }
        ElMessage.success('消息发送成功，请等待对方回复')
      } else {
        // 显示提示信息
        if (res.data?.message) {
          if (res.data.status !== 1) {
            ElMessage.warning(res.data.message)
          } else {
            ElMessage.success(res.data.message)
          }
        }
      }
      
      // 刷新对话列表
      await loadConversations()
      
      // 重新设置焦点到输入框（如果仍有权限）
      if (dmPermission.value.canSend) {
        focusInput()
      }
    }
  } catch (error) {
    // 发送失败
  } finally {
    sending.value = false
  }
}

// 设置焦点到输入框
const focusInput = () => {
  nextTick(() => {
    if (!messageInputRef.value) return
    
    // Element Plus el-input 组件获取焦点
    try {
      // 方式1: 使用组件暴露的 focus 方法
      if (typeof messageInputRef.value.focus === 'function') {
        messageInputRef.value.focus()
        return
      }
      // 方式2: 直接获取 textarea DOM 元素
      const el = messageInputRef.value.$el || messageInputRef.value
      const textarea = el.querySelector?.('textarea') || el
      if (textarea && typeof textarea.focus === 'function') {
        textarea.focus()
      }
    } catch (e) {
      // 设置焦点失败
    }
  })
}

// 标记为已读
const markMessagesAsRead = async () => {
  if (!currentUserId.value) {
    return
  }

  try {
    await markAsRead(currentUserId.value)
    // 更新对话列表中的未读数量
    const conversation = conversations.value.find(c => c.userId === currentUserId.value)
    if (conversation) {
      conversation.unreadCount = 0
    }
    // 通知顶部导航栏刷新私信未读数
    eventBus.emit(EVENT_REFRESH_MESSAGE_COUNT)
  } catch (error) {
    // 标记已读失败
  }
}

// 检查当前用户是否屏蔽了对方
const checkIfBlockedOther = async () => {
  try {
    if (!currentUserId.value) return
    const res = await checkBlockStatus(currentUserId.value)
    if (res && res.code === 20000) {
      // 后端返回 res.data 直接是布尔值
      hasBlockedOther.value = res.data === true
    } else {
      hasBlockedOther.value = false
    }
  } catch (e) {
    hasBlockedOther.value = false
  }
}

// 处理用户操作
const handleUserAction = async (command) => {
  if (!currentUserId.value) {
    return
  }

  try {
    if (command === 'block') {
      await ElMessageBox.confirm('确定要屏蔽该用户吗？屏蔽后您将无法收到该用户的消息。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await blockUser(currentUserId.value)
      ElMessage.success('已屏蔽该用户')
      hasBlockedOther.value = true
      // 更新权限状态
      dmPermission.value = { canSend: false, isGreeting: false, reason: '你已屏蔽对方，无法发送消息' }
    } else if (command === 'unblock') {
      await unblockUser(currentUserId.value)
      ElMessage.success('已取消屏蔽')
      hasBlockedOther.value = false
      // 重新检查权限（取消屏蔽后需要重新判断是否可以发送）
      if (currentUserId.value && typeof currentUserId.value === 'number') {
        const permissionRes = await checkDMPermission(currentUserId.value)
        if (permissionRes.code === 20000 && permissionRes.data) {
          dmPermission.value = {
            canSend: permissionRes.data.canSend ?? true,
            isGreeting: permissionRes.data.isGreeting ?? false,
            reason: permissionRes.data.reason || ''
          }
        }
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 滚动处理
const handleScroll = (e) => {
  const { scrollTop } = e.target
  // 当滚动到顶部附近时，加载更多历史消息
  if (scrollTop <= 50 && hasMore.value && !messageLoading.value) {
    pageNo.value++
    loadMessages(true)
  }
}

// 会话列表滚动处理
const handleConversationScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  // 距离底部50px时触发加载更多
  if (scrollHeight - scrollTop - clientHeight < 50 && hasMoreConversations.value && !conversationLoading.value) {
    loadMoreConversations()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 格式化时间（相对时间）
const formatTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    
    if (seconds < 60) {
      return '刚刚'
    } else if (minutes < 60) {
      return `${minutes}分钟前`
    } else if (hours < 24) {
      return `${hours}小时前`
    } else if (days < 7) {
      return `${days}天前`
    } else {
      return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    }
  } catch (error) {
    return time
  }
}

// 格式化消息时间
const formatMessageTime = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days === 1) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days < 7) {
      return days + '天前'
    } else {
      return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }) + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  } catch (error) {
    return time
  }
}

// 判断是否显示日期分隔线（与上一条消息间隔超过5分钟，或者是第一条消息）
const shouldShowDateSeparator = (message, index) => {
  if (index === 0) return true
  
  const prevMessage = messages.value[index - 1]
  if (!prevMessage) return true
  
  const currentTime = new Date(message.createTime).getTime()
  const prevTime = new Date(prevMessage.createTime).getTime()
  
  // 间隔超过5分钟显示时间
  return (currentTime - prevTime) > 5 * 60 * 1000
}

// 格式化日期分隔线
const formatDateSeparator = (time) => {
  if (!time) return ''
  try {
    const date = new Date(time)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const messageDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
    const diffDays = Math.floor((today - messageDay) / (1000 * 60 * 60 * 24))
    
    const timeStr = date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    
    if (diffDays === 0) {
      return timeStr
    } else if (diffDays === 1) {
      return `昨天 ${timeStr}`
    } else if (date.getFullYear() === now.getFullYear()) {
      return `${date.getMonth() + 1}-${date.getDate()} ${timeStr}`
    } else {
      return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${timeStr}`
    }
  } catch (error) {
    return time
  }
}

// 监听当前用户ID变化
watch(currentUserId, (newVal, oldVal) => {
  if (newVal && newVal !== oldVal) {
    pageNo.value = 1
    hasMore.value = true
    messages.value = []
  }
})

// 判断消息是否可以撤回（2分钟内）
const canWithdraw = (message) => {
  if (!message || !message.createTime) return false
  const createTime = new Date(message.createTime).getTime()
  const now = Date.now()
  const diff = now - createTime
  return diff < 2 * 60 * 1000 // 2分钟 = 120秒
}

// 撤回消息
const handleWithdrawMessage = async (message) => {
  try {
    await ElMessageBox.confirm('确定要撤回这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await withdrawMessage(message.messageId)
    if (res.code === 20000) {
      // 更新本地消息状态
      const index = messages.value.findIndex(m => m.messageId === message.messageId)
      if (index >= 0) {
        messages.value[index].status = 4 // 已撤回
        messages.value[index].content = '消息已撤回'
      }
      ElMessage.success('消息已撤回')
    } else {
      ElMessage.error(res.message || '撤回失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回失败')
    }
  }
}

// 搜索消息
const handleMessageSearch = async () => {
  if (!messageSearchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    const res = await searchMessages(messageSearchKeyword.value.trim(), { page: 1, size: 20 })
    if (res.code === 20000) {
      // 过滤出当前会话的消息
      searchResults.value = (res.data || []).filter(msg => 
        (msg.senderId === currentUserId.value && msg.receiverId === currentUserId_computed.value) ||
        (msg.senderId === currentUserId_computed.value && msg.receiverId === currentUserId.value)
      )
    }
  } catch (error) {
    // 搜索失败
  }
}

// 跳转到搜索结果消息
const jumpToMessage = (message) => {
  showMessageSearch.value = false
  messageSearchKeyword.value = ''
  searchResults.value = []
  
  // 查找消息是否在当前列表中
  const index = messages.value.findIndex(m => m.messageId === message.messageId)
  if (index >= 0) {
    // 消息在列表中，滚动到该消息
    nextTick(() => {
      const messageElements = document.querySelectorAll('.message-item')
      if (messageElements[index]) {
        messageElements[index].scrollIntoView({ behavior: 'smooth', block: 'center' })
        // 高亮显示
        messageElements[index].classList.add('highlight')
        setTimeout(() => {
          messageElements[index].classList.remove('highlight')
        }, 2000)
      }
    })
  } else {
    // 消息不在列表中，重新加载消息列表
    ElMessage.info('正在加载历史消息...')
    pageNo.value = 1
    hasMore.value = true
    loadMessages()
  }
}

// 监听搜索关键词变化
watch(messageSearchKeyword, (newVal) => {
  if (!newVal.trim()) {
    searchResults.value = []
  }
})

// 轮询定时器（仅在WebSocket断线时使用）
let pollTimer = null
const POLL_INTERVAL = 30000 // 30秒轮询一次

// 启动轮询（降级方案）
const startPolling = () => {
  // 只有在WebSocket未连接时才启动轮询
  if (isWebSocketConnected()) {
    return
  }
  
  if (pollTimer) {
    clearInterval(pollTimer)
  }
  
  pollTimer = setInterval(() => {
    if (currentUserId_computed.value && !isWebSocketConnected()) {
      // 刷新对话列表（检查新消息）
      loadConversations()
      // 如果当前有打开的对话，也刷新消息列表
      if (currentUserId.value) {
        loadMessages()
      }
    } else if (isWebSocketConnected()) {
      // WebSocket已恢复连接，停止轮询
      stopPolling()
    }
  }, POLL_INTERVAL)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 请求浏览器通知权限
const requestNotificationPermission = async () => {
  if ('Notification' in window && Notification.permission === 'default') {
    try {
      await Notification.requestPermission()
    } catch (error) {
      // 请求通知权限失败
    }
  }
}

// 显示浏览器通知
const showNotification = (title, body, icon) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    try {
      const notification = new Notification(title, {
        body,
        icon: icon || defaultAvatar,
        tag: 'private-message',
        requireInteraction: false
      })
      
      // 点击通知时聚焦窗口
      notification.onclick = () => {
        window.focus()
        notification.close()
      }
      
      // 5秒后自动关闭
      setTimeout(() => {
        notification.close()
      }, 5000)
    } catch (error) {
      // 显示通知失败
    }
  }
}

// 检查新消息并显示通知
const checkNewMessages = async () => {
  if (!currentUserId_computed.value) {
    return
  }
  
  try {
    const res = await getConversationList({
      page: 1,
      size: 50
    })
    
    if (res.code === 20000) {
      const newConversations = res.data || []
      // 检查是否有新的未读消息
      newConversations.forEach(newConv => {
        const oldConv = conversations.value.find(c => c.userId === newConv.userId)
        if (!oldConv || newConv.unreadCount > oldConv.unreadCount) {
          // 有新消息，显示通知
          if (newConv.unreadCount > 0 && (!oldConv || newConv.unreadCount > oldConv.unreadCount)) {
            showNotification(
              '新私信',
              `${newConv.userName}: ${newConv.lastMessage || '您有一条新消息'}`,
              newConv.userAvatar || defaultAvatar
            )
          }
        }
      })
      
      conversations.value = newConversations
    }
  } catch (error) {
    // 检查新消息失败
  }
}

// 页面可见性变化处理函数
const handleVisibilityChange = () => {
  if (document.hidden) {
    // 页面隐藏时，如果WebSocket未连接，降低轮询频率（60秒）
    if (!isWebSocketConnected()) {
      stopPolling()
      pollTimer = setInterval(() => {
        if (currentUserId_computed.value && !isWebSocketConnected()) {
          checkNewMessages()
        }
      }, 60000)
    }
  } else {
    // 页面可见时，恢复正常轮询频率（30秒）或依赖WebSocket
    if (!isWebSocketConnected()) {
      startPolling()
    }
  }
}

// ==================== WebSocket实时消息处理 ====================

/**
 * 处理WebSocket收到的私信消息
 */
const handleWebSocketPrivateMessage = (data) => {
  if (data.type === 'private_message') {
    const senderId = data.senderId
    
    // 更新会话列表
    updateConversationFromWs(data)
    
    // 如果是当前正在聊天的对象发来的消息，实时更新消息列表
    if (currentUserId.value === senderId) {
      // 添加新消息到列表末尾
      const newMessage = {
        messageId: data.messageId,
        senderId: senderId,
        senderNickname: data.senderName,
        senderAvatar: data.senderAvatar,
        content: data.content,
        createTime: data.timestamp || new Date().toISOString(),
        status: 1
      }
      messages.value.push(newMessage)
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      
      // 标记已读
      markMessagesAsRead()
    }
  } else if (data.type === 'message_read') {
    // 对方已读消息，可以更新消息状态
  } else if (data.type === 'message_withdrawn') {
    // 消息被撤回
    const index = messages.value.findIndex(m => m.messageId === data.messageId)
    if (index >= 0) {
      messages.value[index].status = 4
      messages.value[index].content = '消息已撤回'
    }
  }
}

/**
 * 根据WebSocket消息更新会话列表
 */
const updateConversationFromWs = (data) => {
  const senderId = data.senderId
  const existingIndex = conversations.value.findIndex(c => c.userId === senderId)
  
  if (existingIndex >= 0) {
    // 更新已有会话
    const conv = conversations.value[existingIndex]
    conv.lastMessage = data.content
    conv.lastMessageTime = data.timestamp || new Date().toISOString()
    
    // 如果不是当前聊天对象，增加未读数
    if (currentUserId.value !== senderId) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
    }
    
    // 将此会话移到列表顶部
    conversations.value.splice(existingIndex, 1)
    conversations.value.unshift(conv)
  } else {
    // 新会话，添加到顶部
    const newConv = {
      userId: senderId,
      userName: data.senderName,
      userAvatar: data.senderAvatar,
      lastMessage: data.content,
      lastMessageTime: data.timestamp || new Date().toISOString(),
      unreadCount: 1
    }
    conversations.value.unshift(newConv)
  }
}

// 初始化
onMounted(async () => {
  await requestNotificationPermission()
  await loadConversations()
  
  // 优先使用WebSocket，仅在WebSocket未连接时启动轮询降级
  if (!isWebSocketConnected()) {
    startPolling()
  }
  
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 注册WebSocket私信监听
  onPrivateMessage(handleWebSocketPrivateMessage)
})

// 组件卸载时清理
onUnmounted(() => {
  stopPolling()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  
  // 移除WebSocket私信监听
  offPrivateMessage(handleWebSocketPrivateMessage)
})
</script>

<style scoped>
.private-message-center {
  height: calc(100vh - 60px);
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.message-container {
  width: 1200px;
  max-width: 95%;
  height: 100%;
  display: flex;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

/* ==================== 左侧对话列表 ==================== */
.conversation-list {
  width: 280px;
  background-color: #fff;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.conversation-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-header :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
  border-radius: 4px;
}

.conversation-header :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #409eff inset;
}

.conversation-content {
  flex: 1;
  overflow-y: auto;
}

/* 美化滚动条 */
.conversation-content::-webkit-scrollbar,
.message-list::-webkit-scrollbar {
  width: 6px;
}

.conversation-content::-webkit-scrollbar-thumb,
.message-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.conversation-content::-webkit-scrollbar-thumb:hover,
.message-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.conversation-content::-webkit-scrollbar-track,
.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  position: relative;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #e6f7ff;
  border-left-color: #1890ff;
}

.conversation-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.conversation-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conversation-name {
  font-weight: 500;
  color: #333;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  flex-shrink: 0;
}

.conversation-preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.conversation-preview {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-badge {
  background-color: #ff4d4f;
  color: #fff;
  font-size: 12px;
  padding: 0 6px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  min-width: 18px;
  text-align: center;
  flex-shrink: 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 40px;
  color: #909399;
}

/* ==================== 右侧消息详情 ==================== */
.message-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background-color: #fff;
}

.message-detail-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-header {
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.message-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  cursor: pointer;
}

.message-header-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}

.message-header-name:hover {
  color: #409eff;
}

.message-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 消息搜索面板 */
.message-search-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.result-content {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-time {
  font-size: 12px;
  color: #999;
}

.no-results {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background-color: #fff;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 系统提示（居中显示） */
.system-tip {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 10px 0;
}

.system-tip.warning {
  color: #fa8c16;
}

/* 权限提示 */
.greeting-notice {
  text-align: center;
  padding: 10px 0;
  font-size: 12px;
  color: #999;
}

/* 日期分隔线 */
.date-separator {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 10px 0;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
  position: relative;
  transition: background-color 0.3s;
}

.message-item.highlight {
  background-color: #fff3cd;
  padding: 8px;
  border-radius: 8px;
  margin: 0 -8px 16px -8px;
}

.message-item.message-sent {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}

/* 消息内容包装器 */
.message-content-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 60%;
  position: relative;
}

.message-sent .message-content-wrapper {
  align-items: flex-end;
}

/* 消息状态图标 */
.message-status-icon {
  position: absolute;
  top: -8px;
  left: -8px;
  z-index: 10;
  display: flex;
  align-items: center;
  cursor: pointer;
  background: #fff;
  border-radius: 50%;
  padding: 2px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.message-sent .message-status-icon {
  left: auto;
  right: -8px;
}

.message-status-icon .el-icon {
  font-size: 16px;
}

/* 消息气泡 */
.message-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-word;
  position: relative;
}

.message-sent .message-bubble {
  background-color: #f2f3f5; /* 浅灰色背景 */
  color: #333;
  border-bottom-right-radius: 2px;
}

.message-received .message-bubble {
  background-color: #fff;
  color: #333;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 2px;
}

/* 已撤回消息样式 */
.message-bubble--withdrawn {
  background-color: #f5f5f5 !important;
  border: 1px dashed #d9d9d9 !important;
  font-style: italic;
}

.withdrawn-text {
  color: #999 !important;
}

/* 图片消息特殊样式 */
.message-bubble--image {
  padding: 4px !important;
  background-color: transparent !important;
  border: none !important;
}

.message-sent .message-bubble--image {
  background-color: transparent !important;
}

.message-received .message-bubble--image {
  background-color: transparent !important;
  border: none !important;
}

.message-text {
  white-space: pre-wrap;
}

.message-image {
  max-width: 300px;
  max-height: 300px;
  border-radius: 8px;
  display: block;
  cursor: pointer;
}

/* 消息操作按钮 */
.message-actions {
  position: absolute;
  top: 0;
  display: flex;
  gap: 4px;
  opacity: 0.9;
}

.message-sent .message-actions {
  right: 100%;
  margin-right: 8px;
}

.message-received .message-actions {
  left: 100%;
  margin-left: 8px;
}

.message-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

/* ==================== 输入区域 ==================== */
.message-input-area {
  border-top: 1px solid #f0f0f0;
  padding: 16px 24px;
  background-color: #fff;
}

.message-input-tools {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 12px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  transition: color 0.2s;
}

.tool-item:hover {
  color: #409eff;
}

.tool-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.tool-item.disabled:hover {
  color: #c0c4cc;
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none;
  padding: 0;
  border: none;
  background: transparent;
  resize: none;
  font-size: 14px;
  color: #333;
  min-height: 60px !important;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.custom-textarea :deep(.el-textarea__inner::placeholder) {
  color: #c0c4cc;
}

.message-input-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
}

.send-hint {
  font-size: 12px;
  color: #909399;
}

.permission-tip {
  font-size: 12px;
  color: #e6a23c;
}

.send-button {
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.send-button:disabled {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
}

/* 占位区域 */
.message-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #fff;
}

.empty-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #909399;
  font-size: 14px;
}
</style>

