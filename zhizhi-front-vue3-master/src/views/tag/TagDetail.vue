<template>
  <div class="tag-detail-page">
    <!-- 标签头部信息 -->
    <div v-loading="statsLoading" class="tag-header">
      <div class="tag-info">
        <h1 class="tag-title"># {{ tagStats?.tagName || '加载中...' }}</h1>
        <p class="tag-description">{{ tagStats?.description || '暂无描述' }}</p>
        <div class="tag-meta">
          <span class="meta-item">
            <el-icon><Document /></el-icon>
            {{ tagStats?.postCount || 0 }} 篇文章
          </span>
          <el-tag v-if="tagStats?.isRecommended" type="warning" size="small">推荐话题</el-tag>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="posts-section">
      <div class="section-header">
        <h2>相关文章</h2>
        <el-select v-model="sortBy" size="small" @change="handleSortChange">
          <el-option label="最新发布" value="latest" />
          <el-option label="最多浏览" value="views" />
          <el-option label="最多点赞" value="likes" />
        </el-select>
      </div>

      <div v-loading="postsLoading" class="posts-list">
        <div
          v-for="post in posts"
          :key="post.id"
          class="post-item"
          @click="goToPost(post.id)"
        >
          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-desc">{{ post.description || getContentPreview(post.content) }}</p>
            <div class="post-meta">
              <span class="author">{{ post.authorName || '匿名用户' }}</span>
              <span class="divider">·</span>
              <span class="time">{{ formatTime(post.createTime) }}</span>
              <span class="divider">·</span>
              <span class="stats">
                <el-icon><View /></el-icon> {{ post.viewCount || 0 }}
                <el-icon style="margin-left: 12px"><Star /></el-icon> {{ post.likeCount || 0 }}
              </span>
            </div>
          </div>
          <div v-if="post.coverUrl" class="post-cover">
            <img :src="post.coverUrl" :alt="post.title" />
          </div>
        </div>

        <el-empty v-if="!postsLoading && posts.length === 0" description="该标签下暂无文章" />

        <!-- 加载更多 -->
        <div v-if="hasMore && posts.length > 0" class="load-more">
          <el-button :loading="postsLoading" @click="loadMorePosts">加载更多</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, View, Star } from '@element-plus/icons-vue'
import { getTagPosts, getTagStats } from '@/api/tag'
import type { Post } from '@/types'

defineOptions({ name: 'TagDetail' })

interface TagStatsData {
  tagId: number
  tagName: string
  description: string
  postCount: number
  isRecommended: boolean
}

const route = useRoute()
const router = useRouter()

const tagId = ref<number>(0)
const tagStats = ref<TagStatsData | null>(null)
const posts = ref<Post[]>([])
const sortBy = ref('latest')
const page = ref(1)
const pageSize = 20
const hasMore = ref(true)
const statsLoading = ref(false)
const postsLoading = ref(false)

const loadTagStats = async () => {
  if (!tagId.value) return
  statsLoading.value = true
  try {
    const res = await getTagStats(tagId.value)
    if (res.code === 20000) {
      tagStats.value = res.data as TagStatsData
    }
  } catch (error) {
    // 加载失败
  } finally {
    statsLoading.value = false
  }
}

const loadPosts = async (append = false) => {
  if (!tagId.value) return
  postsLoading.value = true
  try {
    const res = await getTagPosts(tagId.value, page.value, pageSize)
    if (res.code === 20000 && res.data) {
      const newPosts = res.data.records || res.data.data || []
      if (append) {
        posts.value = [...posts.value, ...newPosts]
      } else {
        posts.value = newPosts
      }
      hasMore.value = newPosts.length >= pageSize
    }
  } catch (error) {
    // 加载失败
  } finally {
    postsLoading.value = false
  }
}

const loadMorePosts = () => {
  page.value++
  loadPosts(true)
}

const handleSortChange = () => {
  page.value = 1
  posts.value = []
  loadPosts()
}

const goToPost = (postId: number) => {
  router.push(`/post/${postId}`)
}

const getContentPreview = (content: string): string => {
  if (!content) return ''
  const text = content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ')
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

watch(
  () => route.params.tagId,
  (newId) => {
    if (newId) {
      tagId.value = Number(newId)
      page.value = 1
      posts.value = []
      loadTagStats()
      loadPosts()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (route.params.tagId) {
    tagId.value = Number(route.params.tagId)
    loadTagStats()
    loadPosts()
  }
})
</script>

<style scoped>
.tag-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.tag-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.tag-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px;
}

.tag-description {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 16px;
  line-height: 1.6;
}

.tag-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.posts-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.posts-list {
  min-height: 200px;
}

.post-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.post-item:hover {
  background: #fafafa;
  margin: 0 -20px;
  padding: 16px 20px;
}

.post-item:last-child {
  border-bottom: none;
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-title:hover {
  color: #409eff;
}

.post-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.divider {
  margin: 0 8px;
}

.stats {
  display: flex;
  align-items: center;
}

.post-cover {
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .tag-detail-page {
    padding: 16px;
  }
  
  .tag-header {
    padding: 20px;
  }
  
  .tag-title {
    font-size: 22px;
  }
  
  .post-cover {
    width: 80px;
    height: 60px;
  }
}
</style>
