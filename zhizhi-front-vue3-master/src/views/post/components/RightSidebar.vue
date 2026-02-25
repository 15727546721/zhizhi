<template>
  <div class="sidebar">
    <!-- 作者信息卡片 -->
    <div class="author-card" v-if="post">
      <div class="author-header" @click="goToAuthorProfile" :style="{ cursor: authorInfo.id ? 'pointer' : 'default' }">
        <div class="avatar-wrapper">
          <UserAvatar 
            :src="authorInfo.avatar"
            :username="props.post?.author?.username"
            :nickname="authorInfo.name"
            :size="64"
          />
          <el-tag size="small" type="info" effect="plain">作者</el-tag>
        </div>
        <div class="author-info">
          <h3>{{ authorInfo.name }}</h3>
          <p>{{ authorInfo.description }}</p>
        </div>
      </div>
      
      <div class="author-stats">
        <div class="stat-item">
          <div class="stat-value">{{ authorInfo.likeCount }}</div>
          <div class="stat-label">获赞</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ authorInfo.fansCount }}</div>
          <div class="stat-label">粉丝</div>
        </div>
      </div>

      <!-- 仅在不是作者且作者信息存在时显示关注和私信按钮 -->
      <UserActionButtons
        v-if="!isAuthor && hasValidAuthor"
        :is-following="isFollowing"
        :follow-loading="isFollowingLoading"
        @follow="followAuthor"
        @message="sendMessage"
      />
      
      <!-- 作者信息不可用提示 -->
      <div v-if="!hasValidAuthor" class="author-unavailable">
        <el-alert
          title="作者信息不可用"
          type="info"
          :closable="false"
          show-icon
        >
          该用户可能已被删除或封禁
        </el-alert>
      </div>
    </div>

    <!-- 文章目录 -->
    <div class="post-catalog" style="margin-top: 24px;" v-if="showCatalog">
      <h3 class="catalog-title">目录</h3>
      <slot name="catalog"></slot>
    </div>

    <!-- 相关推荐 -->
    <div class="recommendations" v-if="relatedPosts.length > 0" style="margin-top: 24px;">
      <h3 class="sidebar-title">相关推荐</h3>
      <div class="recommend-list">
        <div 
          v-for="relatedPost in relatedPosts" 
          :key="relatedPost.id"
          class="recommend-item"
          @click="goToRelatedPost(relatedPost.id)"
        >
          <h4 class="recommend-title">{{ relatedPost.title }}</h4>
          <div class="recommend-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ relatedPost.viewCount || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ relatedPost.commentCount || 0 }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElAlert } from 'element-plus'
import { View, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/module/user'
import UserActionButtons from '@/components/UserActionButtons.vue'
import UserAvatar from '@/components/UserAvatar.vue'

// 类型定义
interface PostAuthor {
  id?: number | null
  nickname?: string
  username?: string
  name?: string
  avatar?: string
  description?: string
  likeCount?: number
  fansCount?: number
}

interface PostData {
  author?: PostAuthor | null
  isFollowed?: boolean
}

interface RelatedPost {
  id: number
  title: string
  viewCount?: number
  commentCount?: number
}

interface Props {
  post: PostData
  relatedPosts?: RelatedPost[]
  isFollowingLoading?: boolean
  showCatalog?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  relatedPosts: () => [],
  isFollowingLoading: false,
  showCatalog: false
})

const emit = defineEmits<{
  follow: []
  sendMessage: []
  goToRelatedPost: [id: number]
}>()

const route = useRoute()
const router = useRouter()

// 计算属性：处理作者信息，容错author为null的情况
const authorInfo = computed(() => {
  const author = props.post?.author

  // 即使author为null/undefined，也返回默认的未知作者信息
  return {
    id: author?.id || null,
    name: author?.nickname || author?.username || author?.name || '未知作者',
    avatar: author?.avatar || '',
    description: author?.description || '该用户信息已不可用',
    likeCount: author?.likeCount || 0,
    fansCount: author?.fansCount || 0
  }
})

// 计算属性：作者信息是否有效（不是null）
const hasValidAuthor = computed(() => {
  return props.post?.author != null
})

// 计算属性：是否关注了作者
const isFollowing = computed(() => {
  const result = props.post && props.post.isFollowed
  return result
})

// 计算属性：是否为作者（通过比较当前用户ID和帖子作者ID）
const isAuthor = computed(() => {
  const userStore = useUserStore()
  if (!userStore.isAuthenticated || !userStore.userId || !props.post?.author?.id) {
    return false
  }
  // 统一转为字符串比较，避免类型不一致问题
  return String(userStore.userId) === String(props.post.author.id)
})

// 方法
const followAuthor = async (): Promise<void> => {
  // 检查用户是否登录
  const userStore = useUserStore()
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  emit('follow')
}

const sendMessage = (): void => {
  // 检查用户是否登录
  const userStore = useUserStore()
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  emit('sendMessage')
}

const goToRelatedPost = (relatedPostId: number): void => {
  emit('goToRelatedPost', relatedPostId)
}

// 跳转到作者主页
const goToAuthorProfile = (): void => {
  if (authorInfo.value.id) {
    router.push({ name: 'Profile', params: { userId: authorInfo.value.id } })
  }
}
</script>

<style scoped>
/* 右侧信息栏 */
.sidebar {
  width: 300px;
}

.author-card,
.post-catalog,
.recommendations {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.author-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar-wrapper :deep(.el-tag) {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  height: 18px;
  padding: 0 6px;
  font-size: 11px;
  border: 1px solid #fff;
  border-radius: 9px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  background: var(--el-color-info-light-9);
}

.author-header .author-info h3 {
  margin: 8px 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.author-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.author-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 20px 0;
  margin: 0;
  border-top: 1px solid var(--el-border-color-lighter);
}

.stat-item {
  text-align: center;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: var(--el-color-primary-light-8);
  transform: translateY(-2px);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-color-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-top: 4px;
}

.catalog-title,
.sidebar-title {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}


.author-unavailable :deep(.el-alert) {
  padding: 12px;
}

.author-unavailable :deep(.el-alert__title) {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.author-unavailable :deep(.el-alert__description) {
  font-size: 12px;
  margin-top: 4px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommend-item {
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recommend-item:hover {
  background-color: #f5f5f5;
}

.recommend-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommend-meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 响应式布局 */
@media screen and (max-width: 992px) {
  .sidebar {
    width: 100%;
    position: static;
  }
  
  .author-card,
  .post-catalog,
  .recommendations {
    margin-bottom: 15px;
  }
}

@media screen and (max-width: 768px) {
  .author-header {
    flex-direction: column;
    text-align: center;
  }
  
  .action-buttons {
    justify-content: center;
  }
}
</style>