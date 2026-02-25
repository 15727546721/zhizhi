<template>
  <div class="ranking-card">
    <div class="ranking-header">
      <div class="ranking-title-wrapper">
        <el-icon class="ranking-icon"><document /></el-icon>
        <h3 class="ranking-title">热门帖子</h3>
        <el-button link size="small" class="more-btn" @click="viewMore">
          <span>更多</span>
          <el-icon><arrow-right /></el-icon>
        </el-button>
      </div>
      <el-tabs v-model="timeRange" class="ranking-tabs">
        <el-tab-pane label="7天" name="week"/>
        <el-tab-pane label="30天" name="month"/>
      </el-tabs>
    </div>

    <div class="ranking-list" :class="{ 'is-loading': loading }">
      <!-- 帖子列表 - 保持显示，避免闪烁 -->
      <template v-if="rankingList.length > 0">
        <div v-for="(post, index) in rankingList" :key="post.id" class="ranking-item" @click="goToPost(post.id)">
          <span class="ranking-index" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
          <div class="ranking-content">
            <h4 class="ranking-post-title">{{ post.title }}</h4>
            <div class="ranking-post-stats">
              <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
              <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
              <span><el-icon><Collection /></el-icon> {{ post.favoriteCount || 0 }}</span>
            </div>
          </div>
        </div>
      </template>
      
      <!-- 空状态 - 只在非加载且无数据时显示 -->
      <div v-else-if="!loading" class="empty-container">
        <span>暂无热门帖子</span>
      </div>
      
      <!-- 首次加载状态 -->
      <div v-else class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Document, ArrowRight, View, Star, Collection, Loading } from '@element-plus/icons-vue'
import { getPostRanking } from '@/api/ranking'
import { ElMessage } from 'element-plus'

defineOptions({
  name: 'PostRanking'
})

interface RankingPost {
  id: number
  title: string
  viewCount: number
  likeCount: number
  favoriteCount: number
}

interface PostRankingItem {
  postId?: number
  id?: number
  title?: string | { value?: string; title?: string }
  titleValue?: string
  viewCount?: number
  likeCount?: number
  favoriteCount?: number
}

const router = useRouter()

const timeRange = ref('week')
const rankingList = ref<RankingPost[]>([])
const loading = ref(false)
// 数据缓存，避免重复加载
const dataCache = ref<Map<string, RankingPost[]>>(new Map())

// 获取标题文本的辅助函数
const getPostTitle = (post: PostRankingItem): string => {
  if (!post) return '无标题'
  
  // 处理不同的title格式
  if (typeof post.title === 'string') {
    return post.title
  }
  
  if (post.title && typeof post.title === 'object') {
    return post.title.value || post.title.title || post.titleValue || '无标题'
  }
  
  // 如果title是值对象，尝试获取value
  if (post.titleValue) {
    return post.titleValue
  }
  
  return '无标题'
}

// 获取热门帖子数据
const loadHotPosts = async (showLoading = true) => {
  const cacheKey = timeRange.value
  
  // 如果已有缓存且列表不为空，且不是强制显示loading，则静默更新
  if (dataCache.value.has(cacheKey) && rankingList.value.length > 0 && !showLoading) {
    // 静默更新模式：不显示loading，保持当前显示
    showLoading = false
  }
  
  if (showLoading) {
    loading.value = true
  }
  
  try {
    // 使用排行榜API，支持时间范围筛选
    const response = await getPostRanking(timeRange.value, 'hot', 5)

    // 处理响应数据 - 后端返回结构: { code, data: [PostRankingVO] }
    // PostRankingVO: { rank, postId, title, viewCount, likeCount, commentCount, favoriteCount, ... }
    if (response && response.data) {
      let posts = Array.isArray(response.data) ? response.data : []
      
      // 转换数据格式
      const newList: RankingPost[] = posts.map((item: PostRankingItem) => {
        return {
          id: item.postId || item.id || 0,
          title: (typeof item.title === 'string' ? item.title : getPostTitle(item)) || '无标题',
          viewCount: item.viewCount || 0,
          likeCount: item.likeCount || 0,
          favoriteCount: item.favoriteCount || 0
        }
      }).filter(item => item.id) // 过滤掉无效数据
      
      // 检查数据是否有变化，避免不必要的更新
      const cachedList = dataCache.value.get(cacheKey)
      const hasChanged = !cachedList || 
        cachedList.length !== newList.length ||
        cachedList.some((item, index) => item.id !== newList[index]?.id)
      
      // 只有在数据真正变化时才更新显示
      if (hasChanged || showLoading) {
        rankingList.value = newList
      }
      
      // 更新缓存
      dataCache.value.set(cacheKey, newList)
    } else {
      // 响应数据为空
      if (showLoading) {
        rankingList.value = []
      }
    }
  } catch (error) {
    if (showLoading) {
      rankingList.value = []
    }
  } finally {
    loading.value = false
  }
}

// 查看更多 - 跳转到排行榜页面
const viewMore = () => {
  router.push('/ranking')
}

// 跳转到帖子详情
const goToPost = (postId: number) => {
  if (!postId) return
  router.push(`/post/${postId}`)
}

// 监听时间范围变化
watch(timeRange, () => {
  loadHotPosts(true)
})

// 初始化加载
onMounted(() => {
  loadHotPosts(true)
})
</script>

<style scoped>
.ranking-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  border: 1px solid #f0f0f0;
}

.ranking-header {
  margin-bottom: 20px;
}

.ranking-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.ranking-icon {
  width: 24px;
  height: 24px;
  margin-right: 8px;
  color: #1890ff;
}

.ranking-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.more-btn {
  color: #8a919f;
  padding: 2px 0;
  font-size: 14px;
  border: none;
  background: none;
  transition: color 0.3s;
}

.more-btn:hover {
  color: #1890ff;
  background: none;
}

.more-btn .el-icon {
  margin-left: 2px;
  font-size: 14px;
}

.ranking-tabs :deep(.el-tabs__header) {
  margin: 10px 0 0;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px; /* 保持最小高度，避免布局抖动 */
  transition: opacity 0.2s ease;
}

.ranking-list.is-loading {
  opacity: 0.7;
}

.ranking-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.ranking-item:hover {
  background-color: #f5f5f5;
}

.ranking-index {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #8a919f;
  font-weight: 500;
}

.ranking-index.top-three {
  color: #ff6b6b;
  font-weight: 600;
}

.ranking-content {
  flex: 1;
  min-width: 0;
}

.ranking-post-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: normal;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.ranking-post-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #8a919f;
}

.ranking-post-stats .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #8a919f;
  font-size: 14px;
  gap: 8px;
}

.loading-container .el-icon {
  font-size: 16px;
}

.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #8a919f;
  font-size: 14px;
}
</style>