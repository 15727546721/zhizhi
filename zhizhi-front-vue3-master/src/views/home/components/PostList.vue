<template>
  <div 
    ref="postListContainer"
    class="post-list-container"
  >
    <div
      v-for="(post, index) in posts"
      :key="getPostId(post) || index"
      class="post-item"
      @click.stop="goToPostDetail(getPostId(post))"
    >
      <div class="post-main">
        <div class="post-info">
          <h3 class="post-title">
            {{ getPostTitle(post) }}
            <el-tag 
              v-if="getPostIsFeatured(post)" 
              class="featured-tag" 
              type="danger" 
              size="small"
              effect="dark"
            >
              精选
            </el-tag>
          </h3>
          <p class="post-desc">{{ getPostDescription(post) }}</p>
          <div class="post-footer">
            <div class="post-meta">
              <span class="author">{{ getPostAuthor(post) }}</span>
              <span class="dot">·</span>
              <span class="time">{{ getPostTime(post) }}</span>
              <span class="dot">·</span>
              <div class="post-stats">
                <span class="stat-item">
                  <el-icon><View/></el-icon>
                  {{ getPostViewCount(post) }}
                </span>
                <span class="stat-item">
                  <el-icon><Star/></el-icon>
                  {{ getPostLikeCount(post) }}
                </span>
              </div>
            </div>
            <div class="post-tags">
              <el-tag
                v-for="(tag, tagIndex) in getPostTags(post)"
                :key="tagIndex"
                size="small"
                class="post-tag"
                :effect="'plain'"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="post-cover-wrapper" :class="{ 'no-cover': !getPostCoverUrl(post) }">
          <div v-if="getPostCoverUrl(post)" class="post-cover">
            <el-image :src="getPostCoverUrl(post)" fit="cover" />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 加载状态提示 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <!-- 没有更多数据提示 -->
    <div v-if="noMore && posts.length > 0" class="no-more-container">
      没有更多内容了，已经到底了
    </div>
    
    <!-- 没有数据提示 -->
    <div v-if="posts.length === 0 && !loading" class="no-data-container">
      暂无数据
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { useUserStore } from '@/stores/module/user'
import { ElMessage } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import { 
  getPostList, 
  SortType,
  getPostsByCursor
} from '@/api/post'
import { 
  getFeaturedPosts,
  getPosts,
  getFollowingPosts,
  getHotPosts
} from '@/api/home'
import { validateApiResponse } from '@/utils/typeGuards'
import type { PageResponse } from '@/types/api'

// 解构出需要的图标组件
const { 
  Picture, 
  Link, 
  PriceTag, 
  View, 
  ChatDotRound, 
  Like, 
  LikeFilled, 
  Star, 
  StarFilled, 
  Share,
  Document,
  ChatLineRound,
  QuestionFilled,
  Tickets,
  Loading
} = Icons

defineOptions({
  name: 'PostList'
})

interface Props {
  categoryId?: number
  type?: string
  activeTab?: string
  tagId?: number
}

interface PostItem {
  id?: number
  postItem?: Record<string, unknown>
  title?: string | { value?: string; title?: string }
  titleValue?: string
  description?: string
  content?: unknown
  coverUrl?: string
  cover?: string
  nickname?: string
  author?: { nickname?: string }
  user?: { nickname?: string }
  publishTime?: string
  createTime?: string
  updateTime?: string
  viewCount?: number
  likeCount?: number
  isFeatured?: boolean
  is_featured?: boolean
  tagNameList?: string[]
  tags?: Array<{ name?: string } | string>
}

// 响应类型定义
interface PostListResponse {
  pageNo?: number
  pageSize?: number
  total?: number
  data?: PostItem[]
}

interface CursorPostListResponse {
  data?: PostItem[]
  nextCursor?: string | null
  hasMore?: boolean
  pageSize?: number
  total?: number
}

const props = withDefaults(defineProps<Props>(), {
  categoryId: undefined,
  type: 'all',
  activeTab: 'recommend',
  tagId: undefined
})

const emits = defineEmits<{
  'update:activeTab': [value: string]
}>()

const router = useRouter()
const userStore = useUserStore()

// 状态管理
const posts = ref<PostItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const previewVisible = ref(false)
const previewImageUrl = ref('')

// 游标分页状态
const nextCursor = ref<string | null>(null)
const hasMore = ref(true)
const useCursorPagination = ref(true) // 是否使用游标分页

const noMore = computed(() => {
  if (useCursorPagination.value) {
    return !hasMore.value && posts.value.length > 0
  }
  return posts.value.length >= total.value && total.value > 0
})
const postListContainer = ref<HTMLDivElement | null>(null)
let scrollHandler: (() => void) | null = null

// 获取内容预览文本
const getContentPreview = (content: unknown): string => {
  if (content && typeof content === 'object') {
    const obj = content as Record<string, unknown>
    if (obj.plainText && typeof obj.plainText === 'string') {
      return obj.plainText.substring(0, 150) + (obj.plainText.length > 150 ? '...' : '')
    }
    if (obj.value && typeof obj.value === 'string') {
      return obj.value.substring(0, 150) + (obj.value.length > 150 ? '...' : '')
    }
    if (obj.formattedContent && typeof obj.formattedContent === 'string') {
      return obj.formattedContent.substring(0, 150) + (obj.formattedContent.length > 150 ? '...' : '')
    }
    const str = JSON.stringify(content)
    return str.substring(0, 150) + (str.length > 150 ? '...' : '')
  }
  
  if (typeof content === 'string') {
    return content.substring(0, 150) + (content.length > 150 ? '...' : '')
  }
  
  if (content === null || content === undefined) {
    return ''
  }
  
  const str = String(content)
  return str.substring(0, 150) + (str.length > 150 ? '...' : '')
}

const formatTime = (time: string | undefined): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

// 加载帖子列表
const loadPosts = async (page = 1, type: string | null = null) => {
  // 游标分页模式
  if (useCursorPagination.value && (props.activeTab === 'recommend' || props.activeTab === 'latest' || props.activeTab === 'hot')) {
    await loadPostsByCursor(page === 1)
    return
  }

  // 传统分页模式
  if (noMore.value && page > 1) return

  if (props.activeTab === 'following' && !userStore.isAuthenticated) {
    ElMessage.warning('请先登录以查看关注内容')
    emits('update:activeTab', 'recommend')
    return
  }

  loading.value = true
  try {
    let response;

    switch (props.activeTab) {
          case 'featured':
            response = await getFeaturedPosts({
              page: page,
              size: pageSize.value,
              categoryId: props.categoryId,
              tagId: props.tagId
            })
            break
          case 'recommend':
            response = await getPosts({
              type: type || props.type,
              sort: 'latest',
              page: page,
              size: pageSize.value,
              categoryId: props.categoryId,
              tagId: props.tagId
            })
            break
          case 'latest':
            response = await getPosts({
              type: type || props.type,
              sort: 'latest',
              page: page,
              size: pageSize.value,
              categoryId: props.categoryId,
              tagId: props.tagId
            })
            break
          case 'hot':
            response = await getHotPosts({
              page: page,
              size: pageSize.value,
              tagId: props.tagId
            })
            break
          case 'following':
            response = await getFollowingPosts({
              page: page,
              size: pageSize.value,
              categoryId: props.categoryId,
              tagId: props.tagId
            })
            break
          default:
            response = await getPosts({
              type: type || props.type,
              sort: 'latest',
              page: page,
              size: pageSize.value,
              categoryId: props.categoryId,
              tagId: props.tagId
            })
        }

    if (response && response.data) {
      const data = validateApiResponse<PostListResponse>(response)
      if (!data) {
        if (page === 1) {
          posts.value = []
          total.value = 0
        }
        return
      }
      
      if (data.pageNo !== undefined && data.pageSize !== undefined && data.total !== undefined) {
        const returnedData = data.data || []
        const returnedDataLength = returnedData.length
        
        if (page === 1) {
          posts.value = returnedData
          currentPage.value = data.pageNo
        } else {
          posts.value = [...posts.value, ...returnedData]
          currentPage.value = data.pageNo
        }

        pageSize.value = data.pageSize
        
        if (returnedDataLength > 0 && returnedDataLength < data.pageSize) {
          if (page === 1) {
            total.value = returnedDataLength
          } else {
            total.value = posts.value.length
          }
        } else if (data.total !== undefined && data.total !== null && data.total > 0) {
          total.value = data.total
        } else {
          if (returnedDataLength > 0) {
            total.value = returnedDataLength
          } else {
            total.value = 0
          }
        }
      } else {
        if (data != undefined && data.length !== 0) {
          if (page === 1) {
            posts.value = data || []
            currentPage.value = 1
          } else {
            posts.value = [...posts.value, ...data]
            currentPage.value = page
          }
          
          const returnedDataLength = data ? data.length : 0
          if (returnedDataLength < pageSize.value) {
            total.value = posts.value.length
          } else {
            total.value = posts.value.length
          }
        } else {
          if (page === 1) {
            posts.value = []
            total.value = 0
          } else {
            total.value = posts.value.length
          }
        }
      }
    } else {
      if (page === 1) {
        posts.value = []
        total.value = 0
      }
    }
  } catch (error: any) {
    if (props.activeTab === 'following' && error.response && error.response.status === 401) {
      ElMessage.warning('请先登录以查看关注内容')
      emits('update:activeTab', 'recommend')
    } else {
      if (page === 1) {
        posts.value = []
        total.value = 0
      }
    }
  } finally {
    loading.value = false
  }
}

// 游标分页加载帖子（使用游标提升性能）
const loadPostsByCursor = async (isFirstPage = false) => {
  if (!hasMore.value && !isFirstPage) return

  loading.value = true
  try {
    const sortBy = props.activeTab === 'hot' ? 'hot' : 'latest'
    
    const response = await getPostsByCursor({
      cursor: isFirstPage ? undefined : (nextCursor.value || undefined),
      pageSize: pageSize.value,
      sortBy: sortBy,
      tagId: props.tagId
    })

    if (response && response.data) {
      const cursorResponse = validateApiResponse<CursorPostListResponse>(response)
      if (!cursorResponse) {
        if (isFirstPage) {
          posts.value = []
          total.value = 0
        }
        hasMore.value = false
        return
      }
      
      const returnedData = cursorResponse.data || []
      
      if (isFirstPage) {
        posts.value = returnedData
        if (cursorResponse.total !== undefined && cursorResponse.total !== null) {
          total.value = cursorResponse.total
        }
      } else {
        posts.value = [...posts.value, ...returnedData]
      }
      
      nextCursor.value = cursorResponse.nextCursor || null
      hasMore.value = cursorResponse.hasMore === true
      
      if (cursorResponse.pageSize) {
        pageSize.value = cursorResponse.pageSize
      }
    } else {
      if (isFirstPage) {
        posts.value = []
        total.value = 0
      }
      hasMore.value = false
    }
  } catch (error) {
    if (isFirstPage) {
      posts.value = []
      total.value = 0
    }
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 加载更多帖子（无限滚动）
const loadMorePosts = async () => {
  if (loading.value || noMore.value) return

  if (useCursorPagination.value && (props.activeTab === 'recommend' || props.activeTab === 'latest' || props.activeTab === 'hot')) {
    if (!hasMore.value) return
    await loadPostsByCursor(false)
    return
  }

  if (posts.value.length >= total.value) {
    return
  }

  await loadPosts(currentPage.value + 1, props.type)
}

// 检查是否需要加载更多
const checkScroll = () => {
  if (!scrollHandler) return

  const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight

  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMorePosts()
  }
}

// 跳转到帖子详情
const goToPostDetail = (postId: number | null) => {
  if (!postId) return
  router.push(`/post/${postId}`)
}

// ==================== 数据提取辅助函数 ====================

const getPostId = (post: PostItem): number | null => {
  if (!post) return null
  const item = post.postItem || post
  return (item as PostItem).id || null
}

const getPostTitle = (post: PostItem): string => {
  if (!post) return '无标题'
  const item = (post.postItem || post) as PostItem
  if (typeof item.title === 'string') return item.title
  if (item.title && typeof item.title === 'object') {
    if ('value' in item.title && item.title.value) return item.title.value
    if ('title' in item.title && item.title.title) return item.title.title
  }
  return item.titleValue || '无标题'
}

const getPostDescription = (post: PostItem): string => {
  if (!post) return ''
  const item = (post.postItem || post) as PostItem
  return item.description || getContentPreview(item.content) || ''
}

const getPostCoverUrl = (post: PostItem): string | null => {
  if (!post) return null
  const item = (post.postItem || post) as PostItem
  return item.coverUrl || item.cover || null
}

const getPostAuthor = (post: PostItem): string => {
  if (!post) return '匿名用户'
  const item = (post.postItem || post) as PostItem
  return item.nickname || item.author?.nickname || item.user?.nickname || '匿名用户'
}

const getPostTime = (post: PostItem): string => {
  if (!post) return ''
  const item = (post.postItem || post) as PostItem
  const time = item.publishTime || item.createTime || item.updateTime
  return formatTime(time)
}

const getPostViewCount = (post: PostItem): number => {
  if (!post) return 0
  const item = (post.postItem || post) as PostItem
  return item.viewCount || 0
}

const getPostLikeCount = (post: PostItem): number => {
  if (!post) return 0
  const item = (post.postItem || post) as PostItem
  return item.likeCount || 0
}

const getPostIsFeatured = (post: PostItem): boolean => {
  if (!post) return false
  const item = (post.postItem || post) as PostItem
  return item.isFeatured || item.is_featured || false
}

const getPostTags = (post: PostItem): string[] => {
  if (!post) return []
  const item = (post.postItem || post) as PostItem
  if (item.tagNameList && Array.isArray(item.tagNameList)) {
    return item.tagNameList
  }
  if (item.tags && Array.isArray(item.tags)) {
    return item.tags.map((t) => {
      if (typeof t === 'string') return t
      if (t && typeof t === 'object' && 'name' in t) return t.name || ''
      return ''
    }).filter(Boolean)
  }
  return []
}

// ==================== 生命周期 ====================

watch(() => props.activeTab, (newTab, oldTab) => {
  if (newTab !== oldTab) {
    currentPage.value = 1
    total.value = 0
    posts.value = []
    nextCursor.value = null
    hasMore.value = true
    loadPosts(1)
  }
})

watch(() => props.tagId, (newId, oldId) => {
  if (newId !== oldId) {
    currentPage.value = 1
    total.value = 0
    posts.value = []
    nextCursor.value = null
    hasMore.value = true
    loadPosts(1)
  }
})

onMounted(() => {
  loadPosts(1)
  scrollHandler = checkScroll
  window.addEventListener('scroll', scrollHandler)
})

onUnmounted(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler)
    scrollHandler = null
  }
})

onBeforeRouteLeave(() => {
  if (scrollHandler) {
    window.removeEventListener('scroll', scrollHandler)
    scrollHandler = null
  }
})

// 刷新方法供父组件调用
const refresh = () => {
  currentPage.value = 1
  total.value = 0
  posts.value = []
  nextCursor.value = null
  hasMore.value = true
  loadPosts(1)
}

defineExpose({
  refresh
})
</script>

<style scoped>
.post-list-container {
  background: #fff;
  border-radius: 4px;
}

.post-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.post-item:last-child {
  border-bottom: none;
}

.post-item:hover {
  background-color: #f8f9fa;
}

.post-main {
  display: flex;
  gap: 16px;
  height: auto;
  min-height: 80px;
  padding: 0;
  overflow: hidden;
}

.post-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.post-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  word-wrap: break-word;
}

/* 精选帖子标记样式 */
.featured-tag {
  margin-left: 8px;
  vertical-align: top;
}

.post-desc {
  margin: 0;
  font-size: 13px;
  color: #515767;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  word-wrap: break-word;
  margin-bottom: 4px;
  white-space: normal;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: auto;
  margin-top: auto;
  padding: 2px 0;
  min-height: 20px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8a919f;
  font-size: 13px;
  line-height: 1;
  flex-wrap: wrap;
}

.post-stats {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  line-height: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8a919f;
  font-size: 13px;
  cursor: pointer;
}

.stat-item:hover {
  color: #409eff;
}

.stat-item.active {
  color: #409eff;
}

.stat-item .el-icon {
  font-size: 14px;
}

.dot {
  color: #8a919f;
}

.post-cover-wrapper {
  flex: 0 0 120px;
  height: 80px;
}

.post-cover-wrapper.no-cover {
  flex: 0 0 0px;
  height: 0px;
  overflow: hidden;
}

.post-cover {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.post-cover :deep(.el-image) {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.post-cover:hover :deep(.el-image) {
  transform: scale(1.05);
}

.post-tags {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.post-tag {
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 18px;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
  border-color: transparent;
  color: #409eff;
}

.post-tag:hover {
  background: rgba(64, 158, 255, 0.2);
}

.post-topic {
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  line-height: 18px;
  border-radius: 4px;
  margin-right: 4px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}

.post-topic :deep(.el-tag__content) {
  display: inline-flex;
  align-items: center;
}



/* 加载状态样式 */
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 10px;
  color: #8a919f;
}

/* 没有更多数据样式 */
.no-more-container {
  text-align: center;
  padding: 20px;
  color: #8a919f;
  font-size: 14px;
}

/* 没有数据样式 */
.no-data-container {
  text-align: center;
  padding: 40px 20px;
  color: #8a919f;
  font-size: 14px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .post-item {
    padding: 10px 12px;
  }

  .post-main {
    min-height: 60px;
  }

  .post-cover-wrapper {
    flex: 0 0 100px;
    height: 70px;
  }

  .post-title {
    font-size: 15px;
    margin-bottom: 4px;
  }

  .post-desc {
    font-size: 12px;
    -webkit-line-clamp: 1;
  }

  .post-meta {
    font-size: 12px;
  }

  .post-tag {
    height: 18px;
    padding: 0 4px;
    font-size: 11px;
    line-height: 16px;
  }
}

@media screen and (max-width: 640px) {
  .post-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .post-meta {
    gap: 4px;
    width: 100%;
  }

  .post-stats {
    gap: 12px;
  }

  .post-tags {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>