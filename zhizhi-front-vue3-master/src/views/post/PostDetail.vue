<template>
  <div class="post-detail-page">
    <div class="content-layout">
      <!-- 左侧悬浮操作栏 -->
      <div class="left-sidebar">
        <LeftSidebarActions 
          v-if="!loading && post"
          :post="post || {}"
          :is-liking="isLiking"
          @like="handleLike"
          @favorite="handleFavorite"
          @report="handleReport"
          @share="handleShare"
          @share-success="handleShareSuccess"
          @scroll-to-comments="scrollToComments"
        />
      </div>
      
      <!-- 主内容区 -->
      <div class="main-content-wrapper">
        <!-- 加载状态骨架屏 -->
        <div v-if="loading" class="post-skeleton">
          <div class="skeleton-title"></div>
          <div class="skeleton-meta">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-info">
              <div class="skeleton-name"></div>
              <div class="skeleton-date"></div>
            </div>
          </div>
          <div class="skeleton-content">
            <div class="skeleton-line"></div>
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>
        
        <!-- 帖子内容 -->
        <div v-else-if="post" class="post-container">
          <h1 class="post-title">
            {{ post.title || '无标题' }}
            <el-tag 
              v-if="post.isFeatured" 
              class="featured-tag" 
              type="danger" 
              size="small"
              effect="dark"
            >
              精选
            </el-tag>
          </h1>
          <div class="post-meta">
            <div class="author-info" @click="goToAuthorProfile" :style="{ cursor: 'pointer' }">
              <el-avatar :size="40" :src="author.avatar" />
              <div class="author-detail">
                <div class="author-name">{{ author.name }}</div>
                <div class="publish-info">
                  <span>{{ post.createTime || '' }}</span>
                  <span class="dot">·</span>
                  <span>阅读 {{ post.viewCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div class="post-tags" v-if="post.tags && post.tags.length > 0">
              <el-tag
                v-for="tag in post.tags"
                :key="tag.id || tag.name || tag"
                size="small"
                effect="plain"
                class="tag-item"
              >
                {{ tag.name || tag }}
              </el-tag>
            </div>
          </div>
          <MdPreview :id="id" :modelValue="post.content || ''" />

          <!-- 专栏信息卡片 -->
          <ColumnInfoCard
            v-if="postColumns.length > 0"
            :columns="postColumns"
            :current-post-id="postId"
            :show-navigation="true"
            class="column-info-section"
          />
        </div>
        
        <!-- 评论区 -->
        <div id="comments-section" class="comments-section" v-if="!loading && post">
          <Comment 
            :post-id="postId"
            :author-id="author.id"
            @comment-count-change="handleCommentCountChange"
          />
        </div>
      </div>
      
      <!-- 右侧信息栏 --> 
      <div class="right-sidebar">
        <RightSidebar 
          v-if="!loading && post"
          :post="post || {}"
          :related-posts="relatedPosts"
          :is-following-loading="isFollowingLoading"
          :show-catalog="showCatalog"
          @follow="followAuthor"
          @send-message="sendMessage"
          @go-to-related-post="goToRelatedPost"
        >
          <template #catalog>
            <MdCatalog :editorId="id" :scrollElement="scrollElement" />
          </template>
        </RightSidebar>
      </div>
    </div>
    
    <!-- 举报弹窗 -->
    <ReportDialog ref="reportDialogRef" />
    
    <!-- 分享二维码弹窗 -->
    <ShareQrDialog ref="shareQrDialogRef" />
    
    <!-- 收藏夹选择对话框 -->
    <FolderSelectDialog 
      v-model="showFolderDialog" 
      @confirm="handleFolderConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Check, Plus, Message } from '@element-plus/icons-vue'
import { getPostDetail, increaseViewCount, likePost, unlikePost } from '@/api/post'
import { followUser, unfollowUser, isFollowing } from '@/api/follow'
import { getRelatedPosts } from '@/api/home'
import favoritesApi, { FavoriteType } from '@/api/favorites' // 导入正确的收藏API
import Comment from '@/components/Comment/index.vue'
import LeftSidebarActions from './components/LeftSidebarActions.vue'
import RightSidebar from './components/RightSidebar.vue'
import ReportDialog from '@/components/ReportDialog.vue'
import ShareQrDialog from '@/components/ShareQrDialog.vue'
import FolderSelectDialog from '@/components/FolderSelectDialog.vue'
import ColumnInfoCard from '@/components/column/ColumnInfoCard.vue'
import { MdCatalog, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/module/user'
import { ReportTargetType } from '@/api/report'
import type { ColumnVO } from '@/types/column'
import * as columnApi from '@/api/column'

// 类型定义
interface PostAuthor {
  id: number | null
  nickname?: string
  username?: string
  avatar?: string
  description?: string
  likeCount?: number
  fansCount?: number
}

interface PostTag {
  id?: number
  name: string
}

interface PostData {
  id: number
  title?: string
  content?: string
  createTime?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  favoriteCount?: number
  shareCount?: number
  isLiked?: boolean
  isFavorited?: boolean
  isFollowed?: boolean
  isFeatured?: boolean
  author?: PostAuthor
  user?: PostAuthor
  tags?: PostTag[]
}

interface RelatedPost {
  id: number
  title: string
  viewCount: number
  commentCount: number
}

interface AuthorInfo {
  id: number | null
  name: string
  avatar: string
  description: string
  likeCount: number
  fansCount: number
}

defineOptions({
  name: 'PostDetailPage'
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const id = 'preview-only'
const scrollElement = ref<HTMLElement | null>(null)

onMounted(() => {
  scrollElement.value = document.documentElement
})

// 响应式数据
const post = ref<PostData | null>(null)
const loading = ref(false)
const relatedPosts = ref<RelatedPost[]>([])
const postColumns = ref<ColumnVO[]>([])
const isFollowingLoading = ref(false)
const isLiking = ref(false)

// 计算属性
const postId = computed(() => route.params.id as string)

// 作者信息
const author = computed((): AuthorInfo => {
  const authorData = post.value?.author || post.value?.user
  return {
    id: authorData?.id || null,
    name: authorData?.nickname || authorData?.username || '未知作者',
    avatar: authorData?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    description: authorData?.description || '',
    likeCount: authorData?.likeCount || 0,
    fansCount: authorData?.fansCount || 0
  }
})

// 是否显示目录（可以根据具体内容决定是否显示）
const showCatalog = computed((): boolean => {
  // 检查是否有内容可以生成目录
  if (!post.value || !post.value.content) {
    return false;
  }
  
  // 如果内容不为空，则显示目录
  return !!post.value.content;
})

// 方法
const loadPostDetail = async (): Promise<void> => {
  if (!postId.value) return
  
  loading.value = true
  try {
    const response = await getPostDetail(postId.value)
    
    // 直接使用后端返回的完整数据
    if (response && response.data) {
      post.value = response.data
      
      // 加载相关推荐
      await loadRelatedPosts()
      
      // 加载专栏信息
      await loadPostColumns()
    }
  } catch (error) {
    // 加载失败，根据错误类型进行不同处理
    if (error.response?.status === 404) {
      ElMessage.error('帖子不存在或已删除')
      router.push('/')
    } else if (error.response?.status === 403) {
      ElMessage.error('无权限查看该帖子')
      router.push('/')
    } else {
      ElMessage.error('加载帖子详情失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载帖子所属的专栏
const loadPostColumns = async (): Promise<void> => {
  if (!postId.value) return
  
  try {
    const response = await columnApi.getPostColumns(Number(postId.value)) as any
    if (response.code === 20000 && response.data) {
      postColumns.value = response.data
    }
  } catch (error) {
    // 不影响主流程,静默失败
  }
}

const loadRelatedPosts = async (): Promise<void> => {
  // 如果没有帖子数据，直接返回
  if (!post.value || !post.value.id) {
    relatedPosts.value = []
    return
  }
  
  try {
    // 调用API获取相关推荐帖子（不再使用type参数）
    const response = await getRelatedPosts({
      excludeId: post.value.id,
      page: 1,
      size: 5
    })
    
    // 处理响应数据 - 支持 PageResponse 结构
    if (response && response.data && response.data.data && Array.isArray(response.data.data)) {
      // 处理新的PageResponse结构
      relatedPosts.value = response.data.data.map(item => {
        // 确保item和item.post存在
        if (!item || !item.post) {
          return null
        }
        return {
          id: item.post.id,
          title: item.post.titleValue || (item.post.title && (item.post.title.title || item.post.title)) || '无标题',
          viewCount: item.post.viewCount || 0,
          commentCount: item.post.commentCount || 0
        }
      }).filter(item => item && item.id) // 过滤掉无效数据
    } else if (response && response.data && Array.isArray(response.data)) {
      // 容错：直接返回数组的情况
      relatedPosts.value = response.data.map(item => {
        // 确保item和item.post存在
        if (!item || !item.post) {
          return null
        }
        return {
          id: item.post.id,
          title: item.post.titleValue || (item.post.title && (item.post.title.title || item.post.title)) || '无标题',
          viewCount: item.post.viewCount || 0,
          commentCount: item.post.commentCount || 0
        }
      }).filter(item => item && item.id) // 过滤掉无效数据
    } else {
      // 如果没有获取到相关推荐，清空数组
      relatedPosts.value = []
    }
  } catch (error) {
    // 出错时清空推荐列表
    relatedPosts.value = []
  }
}

// 处理点赞 - 添加防重复点击机制
const handleLike = async (): Promise<void> => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  if (!post.value) return

  // 防止重复点击
  if (isLiking.value) {
    return
  }

  // 保存原始状态用于错误恢复（必须在try块外定义，以便在catch中使用）
  const originalIsLiked = Boolean(post.value.isLiked)
  const originalLikeCount = post.value.likeCount || 0

  try {
    isLiking.value = true
    
    if (originalIsLiked) {
      // 取消点赞
      await unlikePost(post.value.id)
      // 直接更新post中的状态
      post.value.isLiked = false
      post.value.likeCount = Math.max(0, originalLikeCount - 1)
      ElMessage.success('已取消点赞')
    } else {
      // 点赞
      await likePost(post.value.id)
      // 直接更新post中的状态
      post.value.isLiked = true
      post.value.likeCount = originalLikeCount + 1
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    // 检查是否是重复操作的业务异常
    if (error.response && error.response.data && error.response.data.info && 
        (error.response.data.info.includes('请勿重复操作') || error.response.data.info.includes('尚未点赞'))) {
      // 对于重复操作的提示，恢复到原始状态
      post.value.isLiked = originalIsLiked
      post.value.likeCount = originalLikeCount
      ElMessage.warning(error.response.data.info)
    } else {
      // 其他错误情况，显示错误信息并恢复状态
      post.value.isLiked = originalIsLiked
      post.value.likeCount = originalLikeCount
      ElMessage.error('操作失败')
    }
  } finally {
    isLiking.value = false
  }
}

// 收藏夹选择对话框
const showFolderDialog = ref(false)

// 处理收藏
const handleFavorite = async (): Promise<void> => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }

  if (!post.value) return

  // 如果已收藏，直接取消收藏
  if (post.value.isFavorited) {
    await doUnfavorite()
    return
  }

  // 未收藏，显示收藏夹选择对话框
  showFolderDialog.value = true
}

// 确认收藏到指定收藏夹
const handleFolderConfirm = async (folderId: number) => {
  await doFavorite(folderId)
}

// 执行收藏操作
const doFavorite = async (folderId?: number): Promise<void> => {
  if (!post.value) return
  
  const originalIsFavorited = Boolean(post.value.isFavorited)
  const originalFavoriteCount = post.value.favoriteCount || 0

  try {
    const favoriteParams = {
      targetId: post.value.id,
      targetType: FavoriteType.POST,
      folderId
    }
    
    await favoritesApi.addFavorite(favoriteParams)
    
    // 更新post中的状态
    post.value.isFavorited = true
    post.value.favoriteCount = originalFavoriteCount + 1
    ElMessage.success('收藏成功')
  } catch (error) {
    // 检查是否是重复操作的业务异常
    if (error.response && error.response.data && error.response.data.info && 
        error.response.data.info.includes('请勿重复操作')) {
      post.value.isFavorited = true
      ElMessage.warning(error.response.data.info)
    } else {
      // 其他错误情况，恢复状态
      post.value.isFavorited = originalIsFavorited
      post.value.favoriteCount = originalFavoriteCount
      ElMessage.error('操作失败')
    }
  }
}

// 执行取消收藏操作
const doUnfavorite = async (): Promise<void> => {
  if (!post.value) return
  
  const originalIsFavorited = Boolean(post.value.isFavorited)
  const originalFavoriteCount = post.value.favoriteCount || 0

  try {
    const favoriteParams = {
      targetId: post.value.id,
      targetType: FavoriteType.POST
    }
    
    await favoritesApi.removeFavorite(favoriteParams)
    
    // 更新post中的状态
    post.value.isFavorited = false
    post.value.favoriteCount = Math.max(0, originalFavoriteCount - 1)
    ElMessage.success('已取消收藏')
  } catch (error) {
    // 检查是否是重复操作的业务异常
    if (error.response && error.response.data && error.response.data.info && 
        error.response.data.info.includes('尚未收藏')) {
      post.value.isFavorited = false
      ElMessage.warning(error.response.data.info)
    } else {
      // 其他错误情况，恢复状态
      post.value.isFavorited = originalIsFavorited
      post.value.favoriteCount = originalFavoriteCount
      ElMessage.error('操作失败')
    }
  }
}

// 滚动到评论区
const scrollToComments = (): void => {
  // 实现滚动到评论区的逻辑
  const commentsSection = document.getElementById('comments-section')
  if (commentsSection) {
    commentsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

// 举报弹窗引用
const reportDialogRef = ref<InstanceType<typeof ReportDialog> | null>(null)

// 分享二维码弹窗引用
const shareQrDialogRef = ref<InstanceType<typeof ShareQrDialog> | null>(null)

// 处理举报
const handleReport = (): void => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  if (!post.value) return
  
  // 打开举报弹窗
  reportDialogRef.value?.open(
    ReportTargetType.POST,
    post.value.id,
    post.value.title || '无标题'
  )
}

// 处理分享（微信二维码分享）
const handleShare = (type: string): void => {
  if (type === 'wechat') {
    const url = `${window.location.origin}/post/${post.value?.id}`
    const title = post.value?.title || '知知社区'
    shareQrDialogRef.value?.open(url, title)
  }
}

// 处理分享成功，更新分享数
const handleShareSuccess = (): void => {
  if (post.value) {
    post.value.shareCount = (post.value.shareCount || 0) + 1
  }
}

// 关注/取消关注作者
const followAuthor = async (): Promise<void> => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  
  // 检查帖子和作者信息是否存在
  if (!post.value || !post.value.author || !post.value.author.id) {
    ElMessage.error('作者信息不可用')
    return
  }
  
  // 防止重复点击
  if (isFollowingLoading.value) {
    return
  }
  
  const authorId = post.value.author.id
  const currentFollowState = Boolean(post.value.isFollowed)

  isFollowingLoading.value = true
  try {
    if (currentFollowState) {
      // 当前是已关注状态，执行取消关注
      await unfollowUser(authorId)
      
      // 强制触发响应式更新
      post.value = {
        ...post.value,
        isFollowed: false,
        author: {
          ...post.value.author,
          fansCount: Math.max(0, (post.value.author.fansCount || 0) - 1)
        }
      }
      ElMessage.success('已取消关注')
    } else {
      // 当前是未关注状态，执行关注
      await followUser(authorId)
      
      // 强制触发响应式更新
      post.value = {
        ...post.value,
        isFollowed: true,
        author: {
          ...post.value.author,
          fansCount: (post.value.author.fansCount || 0) + 1
        }
      }
      ElMessage.success('关注成功')
    }
  } catch (error) {
    // 关注失败
  } finally {
    isFollowingLoading.value = false
  }
}

// 发送私信
const sendMessage = (): void => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  
  const authorId = post.value?.author?.id
  if (!authorId) {
    ElMessage.error('无法获取作者信息')
    return
  }
  
  router.push({
    path: '/private-messages',
    query: { userId: authorId }
  })
}

// 跳转到作者主页
const goToAuthorProfile = (): void => {
  if (author.value.id) {
    router.push({ name: 'Profile', params: { userId: author.value.id } })
  }
}

const goToRelatedPost = (relatedPostId: number): void => {
  // 使用replace而不是push，避免在浏览器历史中留下太多记录
  router.replace(`/post/${relatedPostId}`)
}

const handleCommentCountChange = (count: number): void => {
  // 评论数变化后的处理
  if (post.value) {
    post.value.commentCount = count
  }
}

// 监听路由变化（包括刷新页面时）
watch(() => route.params.id, (newId: string | string[], oldId: string | string[] | undefined) => {
  // 当路由参数变化时，重新加载数据
  // 注意：刷新页面时 newId 和 oldId 可能相同，但需要重新加载数据
  if (newId) {
    // 清空之前的数据，避免显示旧数据
    post.value = null
    relatedPosts.value = []
    loadPostDetail()
  }
}, { immediate: false })

// 生命周期
onMounted(() => {
  // 确保在组件挂载时加载数据
  if (postId.value) {
    loadPostDetail()
  } else {
    ElMessage.error('帖子ID无效')
    router.push('/')
  }
})
</script>

<style scoped>
.post-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - var(--header-height));
  box-sizing: border-box;
}

.content-layout {
  display: flex;
  gap: 24px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  align-items: flex-start;
}

.left-sidebar { 
  position: sticky;
  top: calc(var(--header-height) + var(--announcement-bar-height) + 20px);
  width: 60px;
  flex-shrink: 0;
}

.main-content-wrapper {
  flex: 1;
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}

.right-sidebar {
  position: sticky;
  top: calc(var(--header-height) + var(--announcement-bar-height) + 20px);
  width: 300px;
  flex-shrink: 0;
}

.comments-section {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #eee;
}

/* 骨架屏样式 */
.post-skeleton {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.skeleton-title {
  height: 32px;
  width: 70%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 24px;
}

.skeleton-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
}

.skeleton-info {
  flex: 1;
}

.skeleton-name {
  height: 16px;
  width: 100px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-date {
  height: 14px;
  width: 150px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-line {
  height: 16px;
  width: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 响应式设计 */
@media screen and (max-width: 1100px) {
  .right-sidebar {
    display: none;
  }
}

@media screen and (max-width: 768px) {
  .left-sidebar {
    display: none;
  }
  
  .post-detail-page {
    padding: 12px;
  }
}

/* 帖子内容样式 */
.post-container {
  background: #fff;
  padding: 32px;
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.post-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1d;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.featured-tag {
  margin-left: 12px;
  vertical-align: middle;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.publish-info {
  font-size: 14px;
  color: #8a919f;
}

.dot {
  margin: 0 6px;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  border-radius: 4px;
}
</style>


/* 专栏信息区域 */
.column-info-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid #ebeef5;
}
