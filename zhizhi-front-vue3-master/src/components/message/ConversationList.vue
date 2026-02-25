<template>
  <div class="conversation-list">
    <div class="conversation-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话或消息"
        :prefix-icon="Search"
        clearable
        @input="handleSearchInput"
      />
      <!-- 搜索模式切换 -->
      <div v-if="searchKeyword" class="search-mode-tabs">
        <span 
          class="search-tab" 
          :class="{ active: searchMode === 'conversation' }"
          @click="searchMode = 'conversation'"
        >会话</span>
        <span 
          class="search-tab" 
          :class="{ active: searchMode === 'message' }"
          @click="handleSearchMessages"
        >消息</span>
      </div>
    </div>
    <div v-loading="loading || searchLoading" class="conversation-content" @scroll="handleScroll">
      <!-- 消息搜索结果 -->
      <template v-if="searchKeyword && searchMode === 'message'">
        <div v-if="searchResults.length > 0" class="search-results">
          <div class="conversation-group-header">
            <span>搜索结果</span>
            <span class="result-count">{{ searchResults.length }}条</span>
          </div>
          <div
            v-for="msg in searchResults"
            :key="msg.messageId"
            class="search-result-item"
            @click="$emit('searchResultClick', msg)"
          >
            <UserAvatar :size="36" :src="msg.senderAvatar" :nickname="msg.senderNickname" />
            <div class="search-result-info">
              <div class="search-result-name">{{ msg.senderNickname }}</div>
              <div class="search-result-content" v-html="highlightKeyword(msg.content)"></div>
              <div class="search-result-time">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-empty description="未找到相关消息" :image-size="60" />
        </div>
      </template>
      
      <!-- 会话列表 -->
      <template v-else>
        <!-- 消息请求分组 -->
        <template v-if="messageRequests.length > 0">
          <div class="conversation-group-header">
            <span>消息请求</span>
            <span class="request-count">{{ messageRequests.length }}</span>
          </div>
          <div
            v-for="conversation in messageRequests"
            :key="conversation.userId"
            class="conversation-item message-request"
            :class="{ active: activeUserId === conversation.userId }"
            @click="$emit('select', conversation)"
          >
            <UserAvatar :size="40" :src="conversation.userAvatar" :nickname="conversation.userName" />
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
        </template>
        
        <!-- 正常对话分组 -->
        <template v-if="normalConversations.length > 0">
          <div v-if="messageRequests.length > 0" class="conversation-group-header">
            <span>对话</span>
          </div>
          <div
            v-for="conversation in normalConversations"
            :key="conversation.userId"
            class="conversation-item"
            :class="{ active: activeUserId === conversation.userId }"
            @click="$emit('select', conversation)"
          >
            <UserAvatar :size="40" :src="conversation.userAvatar" :nickname="conversation.userName" />
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
        </template>
        
        <!-- 加载更多 -->
        <div v-if="hasMore && filteredConversations.length > 0" class="load-more">
          <span v-if="loading">加载中...</span>
          <span v-else class="load-more-text" @click="$emit('loadMore')">加载更多</span>
        </div>
        
        <div v-if="filteredConversations.length === 0 && !loading" class="empty-state">
          <el-empty :description="searchKeyword ? '未找到相关对话' : '暂无对话'" :image-size="60" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { Conversation, PrivateMessage } from '@/types'
import UserAvatar from '@/components/UserAvatar.vue'
import { searchMessages } from '@/api/message'

defineOptions({ name: 'ConversationList' })

interface Props {
  conversations: Conversation[]
  activeUserId: number | null
  loading?: boolean
  hasMore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  hasMore: false
})

const emit = defineEmits<{
  select: [conversation: Conversation]
  loadMore: []
  searchResultClick: [message: PrivateMessage]
}>()

const searchKeyword = ref('')
const searchMode = ref<'conversation' | 'message'>('conversation')
const searchResults = ref<PrivateMessage[]>([])
const searchLoading = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const filteredConversations = computed(() => {
  if (!searchKeyword.value || searchMode.value === 'message') return props.conversations
  const keyword = searchKeyword.value.toLowerCase()
  return props.conversations.filter(c =>
    c.userName?.toLowerCase().includes(keyword) ||
    c.lastMessage?.toLowerCase().includes(keyword)
  )
})

// 消息请求（陌生人发来的）
const messageRequests = computed(() => {
  return filteredConversations.value.filter(c => c.isMessageRequest)
})

// 正常对话
const normalConversations = computed(() => {
  return filteredConversations.value.filter(c => !c.isMessageRequest)
})

// 搜索输入防抖
const handleSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!searchKeyword.value) {
    searchMode.value = 'conversation'
    searchResults.value = []
    return
  }
}

// 搜索消息
const handleSearchMessages = async () => {
  searchMode.value = 'message'
  if (!searchKeyword.value.trim()) return
  
  searchLoading.value = true
  try {
    const res = await searchMessages(searchKeyword.value.trim())
    if (res.code === 20000) {
      searchResults.value = res.data || []
    }
  } catch (error) {
    console.error('搜索消息失败:', error)
  } finally {
    searchLoading.value = false
  }
}

// 高亮关键词
const highlightKeyword = (content: string): string => {
  if (!content || !searchKeyword.value) return content
  const keyword = searchKeyword.value
  const regex = new RegExp(`(${keyword})`, 'gi')
  return content.replace(regex, '<mark>$1</mark>')
}

// 清空搜索时重置
watch(searchKeyword, (val) => {
  if (!val) {
    searchMode.value = 'conversation'
    searchResults.value = []
  }
})

// 滚动加载更多
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  const { scrollTop, scrollHeight, clientHeight } = target
  // 距离底部 50px 时触发加载
  if (scrollHeight - scrollTop - clientHeight < 50 && props.hasMore && !props.loading) {
    emit('loadMore')
  }
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.conversation-list {
  width: 260px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.conversation-header {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 搜索模式切换 */
.search-mode-tabs {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.search-tab {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  padding-bottom: 4px;
}

.search-tab.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
}

.search-tab:hover {
  color: #409eff;
}

/* 搜索结果样式 */
.search-results {
  padding: 0;
}

.result-count {
  font-size: 12px;
  color: #909399;
}

.search-result-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:hover {
  background: #f5f5f5;
}

.search-result-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.search-result-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.search-result-content {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-result-content :deep(mark) {
  background: #ffe58f;
  padding: 0 2px;
}

.search-result-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.conversation-content {
  flex: 1;
  overflow-y: auto;
}

.conversation-content::-webkit-scrollbar {
  width: 6px;
}

.conversation-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.conversation-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #e6f7ff;
}

.conversation-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.conversation-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.conversation-preview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-preview {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.unread-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 200px;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 12px;
  font-size: 12px;
  color: #909399;
}

.load-more-text {
  cursor: pointer;
  color: #409eff;
}

.load-more-text:hover {
  text-decoration: underline;
}

/* 消息请求分组样式 */
.conversation-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.request-count {
  background: #ff4d4f;
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}

.conversation-item.message-request {
  background: #fffbe6;
}

.conversation-item.message-request:hover {
  background: #fff1b8;
}

.conversation-item.message-request.active {
  background: #ffe58f;
}
</style>
