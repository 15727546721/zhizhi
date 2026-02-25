<template>
  <div class="comment-item">
    <UserHoverCard :user-id="comment.userId">
      <UserAvatar :size="40" :src="comment.avatar" :username="comment.username" :nickname="comment.nickname" />
    </UserHoverCard>
    <div class="comment-content">
      <!-- 评论信息 -->
      <div class="comment-info">
        <span class="author-name">
          {{ comment.nickname || '未知用户' }}
          <el-tag v-if="comment.isAuthor" size="small" type="primary">作者</el-tag>
          <el-tag v-if="comment.isHot" size="small" type="danger">热门</el-tag>
        </span>
      </div>
      <div class="comment-text">{{ comment.content }}</div>

      <!-- 评论图片 -->
      <div v-if="comment.imageUrls && comment.imageUrls.length > 0" 
           class="comment-images" 
           :class="{ 'single-image': comment.imageUrls.length === 1, 'grid-images': comment.imageUrls.length > 1 }">
        <div v-for="(imageUrl, index) in comment.imageUrls" 
             :key="index" 
             class="comment-image-item"
             v-show="index < 2">
          <el-image 
            :src="imageUrl" 
            :preview-src-list="comment.imageUrls" 
            :initial-index="index"
            :preview-teleported="true"
            :hide-on-click-modal="true"
            fit="cover"
            loading="lazy"
          />
          <div v-if="index === 1 && comment.imageUrls.length > 2" class="more-images-overlay">
            +{{ comment.imageUrls.length - 2 }}
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="comment-actions">
        <div class="action-group">
          <span class="comment-time">{{ comment.formattedTime }}</span>
          <div class="action-item" @click="handleLike" :class="{ 'disabled': !isAuthenticated }">
            <CustomIcon name="thumb-up" :active="comment.isLiked" :size="16" />
            <span class="action-text">{{ comment.likeCount > 0 ? comment.likeCount : '点赞' }}</span>
            <el-tag v-if="comment.isAuthorLiked" size="small" type="warning" style="margin-left: 4px;">
              <el-icon><Star /></el-icon>
              作者点赞
            </el-tag>
          </div>
          <div class="action-item" @click="handleReply" :class="{ 'disabled': !isAuthenticated }">
            <el-icon><ChatDotRound /></el-icon>
            <span class="action-text">回复</span>
          </div>
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

      <!-- 回复编辑器插槽 -->
      <slot name="editor"></slot>

      <!-- 回复列表插槽 -->
      <slot name="replies"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElTag, ElDropdown, ElDropdownMenu, ElDropdownItem, ElImage } from 'element-plus'
import { ChatDotRound, More, Warning, Delete, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import CustomIcon from '@/components/CustomIcon/index.vue'
import UserHoverCard from '@/components/UserHoverCard/index.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import type { CommentItem } from './types'

const props = defineProps<{
  comment: CommentItem
  authorId?: number | string
}>()

const emit = defineEmits<{
  (e: 'like', comment: CommentItem): void
  (e: 'reply', comment: CommentItem): void
  (e: 'delete', comment: CommentItem): void
  (e: 'report', comment: CommentItem): void
}>()

const userStore = useUserStore()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const currentUserId = computed(() => Number(userStore.userInfo?.id || 0))

const canDelete = computed(() => {
  if (!isAuthenticated.value) return false
  return currentUserId.value === Number(props.comment.userId)
})

const canReport = computed(() => {
  if (!isAuthenticated.value) return false
  return currentUserId.value !== Number(props.comment.userId)
})

const canOperate = computed(() => canDelete.value || canReport.value)

const handleLike = () => emit('like', props.comment)
const handleReply = () => emit('reply', props.comment)
const handleDelete = () => emit('delete', props.comment)
const handleReport = () => emit('report', props.comment)
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 16px;
  padding: 24px 0;
  border-bottom: 1px solid #f0f2f5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-info {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 12px;
}

.author-name {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #252933;
  transition: color 0.2s;
}

.author-name:hover {
  color: #1890ff;
}

.author-name :deep(.el-tag) {
  display: inline-block;
  height: 20px;
  line-height: 18px;
  margin: 0 0 0 4px;
  padding: 0 6px;
  border-radius: 2px;
  font-size: 12px;
  transform: scale(0.9);
}

.comment-time {
  font-size: 13px;
  color: #8a919f;
}

.comment-text {
  font-size: 15px;
  line-height: 1.6;
  color: #252933;
  margin: 8px 0 12px;
  word-break: break-word;
}

/* 评论图片样式 - 统一固定尺寸，防止长图破坏布局 */
.comment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

/* 图片项统一样式 */
.comment-image-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  cursor: zoom-in;
  background-color: #f5f5f5;
}

/* 单张图片稍大一些 */
.comment-images.single-image .comment-image-item {
  width: 200px;
  height: 200px;
}

/* 多张图片保持较小尺寸 */
.comment-images.grid-images .comment-image-item {
  width: 110px;
  height: 110px;
}

.more-images-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  pointer-events: none;
}

/* 图片填充容器，裁剪显示 */
.comment-image-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.comment-image-item :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-image-item:hover :deep(.el-image) {
  transform: scale(1.05);
  transition: transform 0.3s;
}

/* 操作区样式 */
.comment-actions {
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

@media screen and (max-width: 768px) {
  .comment-item {
    padding: 16px 0;
  }

  .action-group {
    gap: 8px;
  }

  .action-item {
    padding: 4px;
  }
}
</style>
