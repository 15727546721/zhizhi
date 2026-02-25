<template>
  <div class="reply-item">
    <UserHoverCard :user-id="reply.userId">
      <UserAvatar :size="32" :src="reply.user?.avatar || reply.avatar" :username="reply.user?.username || reply.username" :nickname="reply.user?.nickname || reply.nickname" />
    </UserHoverCard>
    <div class="reply-content">
      <div class="reply-info">
        <span class="author-name">
          {{ reply.user?.nickname || reply.nickname || '未知用户' }}
          <el-tag v-if="isAuthor" size="small" type="primary">作者</el-tag>
          <el-tag v-if="reply.isHot" size="small" type="danger">热门</el-tag>
        </span>
        <template v-if="reply.replyUserId">
          <span class="reply-to">回复</span>
          <el-text class="mx-1" type="primary">
            @{{ reply.replyUser?.nickname || reply.replyUser?.username || '未知用户' }}
          </el-text>
        </template>
      </div>
      <div class="reply-text">{{ reply.content }}</div>
      
      <!-- 回复图片 -->
      <div v-if="reply.imageUrls && reply.imageUrls.length > 0" class="reply-images">
        <div v-for="(imageUrl, index) in reply.imageUrls" :key="index" class="reply-image-item">
          <el-image 
            :src="imageUrl" 
            :preview-src-list="reply.imageUrls"
            :initial-index="index"
            :preview-teleported="true"
            :hide-on-click-modal="true"
            fit="cover"
            loading="lazy"
          />
        </div>
      </div>

      <!-- 操作区 -->
      <div class="reply-actions">
        <div class="action-group">
          <span class="comment-time">{{ reply.formattedTime }}</span>
          <div class="action-item" @click="handleLike" :class="{ 'disabled': !isAuthenticated }">
            <CustomIcon name="thumb-up" :active="reply.isLiked" :size="16" />
            <span class="action-text">{{ reply.likeCount > 0 ? reply.likeCount : '点赞' }}</span>
            <el-tag v-if="reply.isAuthorLiked" size="small" type="warning" style="margin-left: 4px;">
              <el-icon><Star /></el-icon>
              作者点赞
            </el-tag>
          </div>
          <div class="action-item" @click="handleReply" :class="{ 'disabled': !isAuthenticated }">
            <el-icon><ChatDotRound /></el-icon>
            <span class="action-text">回复</span>
          </div>
          <div v-if="reply.replyUserId" class="action-item view-conversation" @click="handleViewConversation">
            <span class="action-text">查看对话</span>
          </div>
          <el-dropdown trigger="hover" class="more-dropdown" v-if="canOperate">
            <div class="action-item">
              <el-icon><More /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="dropdown-menu">
                <el-dropdown-item v-if="canDelete" @click="handleDelete" class="danger">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-dropdown-item>
                <el-dropdown-item v-if="canReport" @click="handleReport">
                  <el-icon><Warning /></el-icon>
                  <span>举报</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 回复编辑器插槽 -->
      <slot name="editor"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElTag, ElText, ElDropdown, ElDropdownMenu, ElDropdownItem, ElImage } from 'element-plus'
import { ChatDotRound, More, Warning, Delete, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import CustomIcon from '@/components/CustomIcon/index.vue'
import UserHoverCard from '@/components/UserHoverCard/index.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import type { CommentItem } from './types'

const props = defineProps<{
  reply: CommentItem
  authorId?: number | string
}>()

const emit = defineEmits<{
  (e: 'like', reply: CommentItem): void
  (e: 'reply', reply: CommentItem): void
  (e: 'delete', reply: CommentItem): void
  (e: 'report', reply: CommentItem): void
  (e: 'view-conversation', reply: CommentItem): void
}>()

const userStore = useUserStore()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const currentUserId = computed(() => Number(userStore.userInfo?.id || 0))

const isAuthor = computed(() => Number(props.reply.userId) === Number(props.authorId))

const canDelete = computed(() => {
  if (!isAuthenticated.value) return false
  return currentUserId.value === Number(props.reply.userId)
})

const canReport = computed(() => {
  if (!isAuthenticated.value) return false
  return currentUserId.value !== Number(props.reply.userId)
})

const canOperate = computed(() => canDelete.value || canReport.value)

const handleLike = () => emit('like', props.reply)
const handleReply = () => emit('reply', props.reply)
const handleDelete = () => emit('delete', props.reply)
const handleReport = () => emit('report', props.reply)
const handleViewConversation = () => emit('view-conversation', props.reply)
</script>

<style scoped>
.reply-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
}

.reply-item:not(:last-child) {
  border-bottom: 1px dashed #e5e6eb;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.author-name {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #252933;
}

.author-name :deep(.el-tag) {
  height: 20px;
  line-height: 18px;
  padding: 0 6px;
  border-radius: 2px;
  font-size: 12px;
  transform: scale(0.9);
}

.reply-to {
  font-size: 14px;
  color: #8a919f;
  margin: 0 4px;
}

.reply-text {
  font-size: 14px;
  line-height: 1.6;
  color: #515767;
  margin: 6px 0;
  word-break: break-word;
}

.reply-images {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.reply-image-item {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  cursor: zoom-in;
  background-color: #f5f5f5;
}

.reply-image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
  transition: transform 0.2s;
}

.reply-image-item :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reply-image-item:hover :deep(.el-image) {
  transform: scale(1.05);
}

.reply-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
  color: #8a919f;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-item:hover {
  color: #1890ff;
}

.action-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comment-time {
  font-size: 13px;
  color: #8a919f;
}

.view-conversation {
  color: #1890ff;
}

.view-conversation:hover {
  color: #40a9ff;
}
</style>
