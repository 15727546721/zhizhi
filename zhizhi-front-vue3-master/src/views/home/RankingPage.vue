<template>
  <div class="ranking-page">
    <div class="page-container">
      <div class="page-header">
        <h1 class="page-title">排行榜</h1>
        <div class="header-actions">
          <el-button 
            :type="activeTab === 'post' ? 'primary' : 'default'" 
            @click="handleTabChange('post')"
            class="tab-button"
            round
          >
            帖子榜
          </el-button>
          <el-button 
            :type="activeTab === 'author' ? 'primary' : 'default'" 
            @click="handleTabChange('author')"
            class="tab-button"
            round
          >
            作者榜
          </el-button>
          <el-button 
            :type="activeTab === 'tag' ? 'primary' : 'default'" 
            @click="handleTabChange('tag')"
            class="tab-button"
            round
          >
            标签榜
          </el-button>
        </div>
      </div>

      <div class="content-layout">
        <!-- 主内容区域 -->
        <div class="main-content">
          <!-- 筛选区域 -->
          <div class="filter-section">
            <div class="filter-group">
              <span class="filter-label">维度：</span>
              <el-radio-group v-if="activeTab === 'post'" v-model="postSort" size="small" @change="loadRankingData">
                <el-radio-button value="hot">热度</el-radio-button>
                <el-radio-button value="likes">点赞</el-radio-button>
                <el-radio-button value="favorites">收藏</el-radio-button>
                <el-radio-button value="comments">评论</el-radio-button>
                <el-radio-button value="views">浏览</el-radio-button>
                <el-radio-button value="latest">最新</el-radio-button>
              </el-radio-group>
              <el-radio-group v-else-if="activeTab === 'author'" v-model="userSort" size="small" @change="loadRankingData">
                <el-radio-button value="comprehensive">综合</el-radio-button>
                <el-radio-button value="fans">粉丝</el-radio-button>
                <el-radio-button value="likes">获赞</el-radio-button>
                <el-radio-button value="posts">发帖</el-radio-button>
              </el-radio-group>
              <el-radio-group v-else-if="activeTab === 'tag'" v-model="tagSort" size="small" @change="loadRankingData">
                <el-radio-button value="count">使用量</el-radio-button>
                <el-radio-button value="hot">热度</el-radio-button>
              </el-radio-group>
            </div>
            
            <div class="filter-group" v-if="activeTab === 'post' || activeTab === 'tag'">
              <span class="filter-label">时间：</span>
              <el-radio-group v-model="timeRange" size="small" @change="loadRankingData">
                <el-radio-button value="week">周榜</el-radio-button>
                <el-radio-button value="month">月榜</el-radio-button>
                <el-radio-button value="all">总榜</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 骨架屏 -->
          <div v-if="loading" class="skeleton-list">
            <div v-for="i in 5" :key="i" class="ranking-item skeleton-item">
              <el-skeleton :rows="3" animated />
            </div>
          </div>

          <!-- 帖子列表 -->
          <div v-else-if="activeTab === 'post'" class="ranking-list">
            <div 
              v-for="(post, index) in postList" 
              :key="post.id" 
              class="ranking-item"
              @click="goToPostDetail(post.id)"
            >
              <div class="ranking-index" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <div class="ranking-content">
                <h3 class="post-title">{{ post.title }}</h3>
                <div class="post-meta">
                  <img :src="post.avatar || '/default-avatar.png'" class="post-author-avatar" />
                  <span class="author">{{ post.author }}</span>
                  <span class="dot">·</span>
                  <span class="time">{{ formatTime(post.createTime) }}</span>
                </div>
                <div class="post-stats">
                  <span class="stat-item" :class="{ 'highlight': postSort === 'views' }">
                    <el-icon><View /></el-icon> {{ formatNumber(post.viewCount) }}
                  </span>
                  <span class="stat-item" :class="{ 'highlight': postSort === 'likes' }">
                    <CustomIcon name="thumb-up" :size="14" /> {{ formatNumber(post.likeCount) }}
                  </span>
                  <span class="stat-item" :class="{ 'highlight': postSort === 'favorites' }">
                    <el-icon><Star /></el-icon> {{ formatNumber(post.favoriteCount) }}
                  </span>
                  <span class="stat-item" :class="{ 'highlight': postSort === 'comments' }">
                    <el-icon><ChatDotRound /></el-icon> {{ formatNumber(post.commentCount) }}
                  </span>
                </div>
              </div>
              <div v-if="postSort === 'hot'" class="hot-score">
                <span class="fire-emoji">🔥</span>
                <span class="heat-value">{{ Math.round(post.score || 0) }}</span>
              </div>
            </div>
            <el-empty v-if="postList.length === 0" description="暂无数据" />
          </div>

          <!-- 作者列表 -->
          <div v-else-if="activeTab === 'author'" class="ranking-list">
            <div 
              v-for="(user, index) in userList" 
              :key="user.id" 
              class="ranking-item author-item"
              @click="goToUserProfile(user.id)"
            >
              <div class="ranking-index" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <img :src="user.avatar || defaultAvatar" :alt="user.name" class="user-avatar">
              <div class="ranking-content">
                <div class="user-header">
                  <h3 class="user-name">{{ user.name }}</h3>
                  <el-tag size="small" type="info" v-if="index < 3">TOP {{ index + 1 }}</el-tag>
                </div>
                <p class="user-desc">{{ user.description }}</p>
                <div class="user-stats">
                  <span class="stat-item" :class="{ 'highlight': userSort === 'posts' }">
                    <el-icon><Document /></el-icon> 帖子 {{ formatNumber(user.postCount) }}
                  </span>
                  <span class="stat-item" :class="{ 'highlight': userSort === 'likes' }">
                    <CustomIcon name="thumb-up" :size="14" /> 获赞 {{ formatNumber(user.likeCount) }}
                  </span>
                  <span class="stat-item" :class="{ 'highlight': userSort === 'fans' }">
                    <el-icon><User /></el-icon> 粉丝 {{ formatNumber(user.fansCount) }}
                  </span>
                </div>
              </div>
              <div class="action-btn" @click.stop v-if="!isCurrentUser(user.id)">
                <el-button 
                  :type="user.isFollowing ? 'info' : 'primary'" 
                  size="small" 
                  :plain="!user.isFollowing"
                  @click="handleFollow(user)"
                  :loading="user.followLoading"
                >
                  {{ user.isFollowing ? '已关注' : '关注' }}
                </el-button>
              </div>
              <div class="action-btn" v-else>
                <el-tag type="success" size="small">我自己</el-tag>
              </div>
            </div>
             <el-empty v-if="userList.length === 0" description="暂无数据" />
          </div>

          <!-- 标签列表 -->
          <div v-else-if="activeTab === 'tag'" class="ranking-list">
            <div 
              v-for="(tag, index) in tagList" 
              :key="tag.tagId" 
              class="ranking-item tag-ranking-item"
            >
              <div class="ranking-index" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
              <div class="ranking-content">
                <div class="tag-header">
                  <el-tag size="large" effect="dark" class="tag-name">{{ tag.name }}</el-tag>
                </div>
                <p class="tag-desc">{{ tag.description || '暂无描述' }}</p>
                <div class="tag-stats">
                  <span class="stat-item highlight">
                    <el-icon><Collection /></el-icon> 使用 {{ formatNumber(tag.usageCount) }}
                  </span>
                </div>
              </div>
            </div>
            <el-empty v-if="tagList.length === 0" description="暂无数据" />
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="right-sidebar">
          <div class="sidebar-widget">
            <h3 class="widget-title">榜单说明</h3>
            <div class="widget-content">
              <p>榜单根据以下维度综合计算：</p>
              <ul>
                <li>浏览量</li>
                <li>点赞数</li>
                <li>收藏数</li>
                <li>评论数</li>
                <li>发布时间</li>
              </ul>
              <p>数据每日更新，反映社区最热门的内容。</p>
            </div>
          </div>
          
          <div class="sidebar-widget" v-if="activeTab !== 'tag'">
            <h3 class="widget-title">热门标签</h3>
            <div class="tags-container">
              <el-tooltip
                v-for="tag in hotTags" 
                :key="tag.id"
                :content="tag.name"
                :disabled="tag.name.length <= 10"
                placement="top"
              >
                <el-tag 
                  size="small" 
                  type="info"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  View, 
  Star, 
  Collection,
  Document,
  User,
  ChatDotRound
} from '@element-plus/icons-vue'
import CustomIcon from '@/components/CustomIcon/index.vue'
import { ElMessage } from 'element-plus'
import { getPostRanking, getUserRanking, getTagRanking } from '@/api/ranking'
import { getHotTags } from '@/api/tag'
import { followUser, unfollowUser } from '@/api/follow'
import { useUserStore } from '@/stores/module/user'
import type { Tag } from '@/types'
import { validateApiResponse } from '@/utils/typeGuards'

defineOptions({
  name: 'RankingPage'
})

interface RankingPost {
  id: number
  title: string
  author: string
  avatar?: string
  createTime: string
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  score: number
}

interface RankingUser {
  id: number
  name: string
  avatar: string
  description: string
  postCount: number
  likeCount: number
  fansCount: number
  isFollowing: boolean
  followLoading: boolean
}

interface RankingTag {
  tagId: number
  name: string
  description?: string
  usageCount: number
}

// API 响应类型
interface PostRankingItem {
  postId: number
  title: string
  nickname: string
  avatar: string
  createTime: string
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  score: number
}

interface UserRankingItem {
  userId: number
  nickname: string
  username: string
  avatar: string
  description: string
  postCount: number
  likeCount: number
  fansCount: number
}

const router = useRouter()
const userStore = useUserStore()

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 响应式数据
const activeTab = ref<'post' | 'author' | 'tag'>('post')
const timeRange = ref('week')
const postSort = ref('hot')
const userSort = ref('comprehensive')
const tagSort = ref('count')
const postList = ref<RankingPost[]>([])
const userList = ref<RankingUser[]>([])
const tagList = ref<RankingTag[]>([])
const hotTags = ref<Tag[]>([])
const loading = ref(false)

// 模拟数据 (简化，主要依赖后端API)
const mockHotPosts: RankingPost[] = []
const mockAuthors: RankingUser[] = []
const mockTags: RankingTag[] = []
const mockHotTags: Tag[] = [
  { id: 1, name: 'Vue' },
  { id: 2, name: 'React' },
  { id: 3, name: 'JavaScript' },
  { id: 4, name: 'TypeScript' },
  { id: 5, name: 'Node.js' }
]

// 切换主Tab
const handleTabChange = (tab: 'post' | 'author' | 'tag') => {
  activeTab.value = tab
  loadRankingData()
}

// 获取排名样式
const getRankClass = (index: number): string => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

// 格式化数字
const formatNumber = (num: number | undefined): string => {
  if (!num) return '0'
  if (num > 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return String(num)
}

// 计算热度条宽度（基于列表中最高分数的比例）
const getHeatWidth = (score: number): string => {
  if (!score || postList.value.length === 0) return '0%'
  const maxScore = Math.max(...postList.value.map(p => p.score || 0))
  if (maxScore === 0) return '0%'
  const percentage = Math.round((score / maxScore) * 100)
  return `${Math.max(10, percentage)}%`  // 最小10%，保证可见
}

// 加载数据
const loadRankingData = async () => {
  loading.value = true
  
  try {
    if (activeTab.value === 'post') {
      // 加载帖子榜
      const res = await getPostRanking(timeRange.value, postSort.value, 20)
      const data = validateApiResponse<PostRankingItem[]>(res)
      
      if (data) {
        postList.value = data.map(item => ({
          id: item.postId,
          title: item.title || '无标题',
          author: item.nickname || '匿名用户',
          avatar: item.avatar,
          createTime: item.createTime,
          viewCount: item.viewCount || 0,
          likeCount: item.likeCount || 0,
          favoriteCount: item.favoriteCount || 0,
          commentCount: item.commentCount || 0,
          score: item.score || 0
        }))
      } else {
        postList.value = mockHotPosts
      }
    } else if (activeTab.value === 'author') {
      // 加载作者榜
      const res = await getUserRanking(userSort.value, 20)
      const data = validateApiResponse<UserRankingItem[]>(res)
      
      if (data) {
        userList.value = data.map(item => ({
          id: item.userId,
          name: item.nickname || item.username || '匿名用户',
          avatar: item.avatar || '/default-avatar.png',
          description: item.description || '这个人很懒，什么都没写~',
          postCount: item.postCount || 0,
          likeCount: item.likeCount || 0,
          fansCount: item.fansCount || 0,
          isFollowing: false,
          followLoading: false
        }))
      } else {
        userList.value = mockAuthors
      }
    } else if (activeTab.value === 'tag') {
      // 加载标签榜
      const res = await getTagRanking(tagSort.value, timeRange.value, 20)
      const data = validateApiResponse<RankingTag[]>(res)
      
      if (data) {
        tagList.value = data
      } else {
        tagList.value = mockTags
      }
    }
  } catch (error) {
    ElMessage.error('加载榜单数据失败')
  } finally {
    loading.value = false
  }
}

// 加载热门标签
const loadHotTags = async () => {
  try {
    const tagsResponse = await getHotTags('all', 10)
    if (tagsResponse && tagsResponse.data) {
      hotTags.value = tagsResponse.data
    } else {
      hotTags.value = mockHotTags
    }
  } catch (error) {
    hotTags.value = mockHotTags
  }
}

const goToPostDetail = (postId: number) => {
  if (!postId) return
  router.push(`/post/${postId}`)
}

const goToUserProfile = (userId: number) => {
  if (!userId) return
  router.push(`/user/${userId}`)
}

// 判断是否是当前登录用户
const isCurrentUser = (userId: number): boolean => {
  return userStore.isLoggedIn && userStore.userInfo?.id === userId
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
  
  return date.toLocaleDateString()
}

// 关注/取消关注
const handleFollow = async (user: RankingUser) => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  
  // 不能关注自己
  if (user.id === userStore.userInfo?.id) {
    ElMessage.warning('不能关注自己')
    return
  }
  
  user.followLoading = true
  try {
    if (user.isFollowing) {
      // 取消关注
      const res = await unfollowUser(user.id)
      if (res && res.code === 20000) {
        user.isFollowing = false
        user.fansCount = Math.max(0, (user.fansCount || 0) - 1)
        ElMessage.success('已取消关注')
      }
    } else {
      // 关注
      const res = await followUser(user.id)
      if (res && res.code === 20000) {
        user.isFollowing = true
        user.fansCount = (user.fansCount || 0) + 1
        ElMessage.success('关注成功')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  } finally {
    user.followLoading = false
  }
}

// 生命周期
onMounted(() => {
  loadRankingData()
  loadHotTags()
})
</script>

<style scoped>
.ranking-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.tab-button {
  padding: 10px 24px;
  font-size: 16px;
  font-weight: 500;
}

.content-layout {
  display: flex;
  gap: 20px;
}

.main-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  min-width: 0; /* 防止子元素溢出 */
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 48px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  display: flex;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  background: #fff;
  position: relative;
}

.ranking-item:hover {
  background: #f9fafc;
  border-color: #e4e7ed;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.ranking-index {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #909399;
  background: #f0f2f5;
  border-radius: 8px;
  flex-shrink: 0;
}

.ranking-index.rank-1 {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
  color: #fff;
  font-size: 20px;
}

.ranking-index.rank-2 {
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  color: #fff;
  font-size: 20px;
}

.ranking-index.rank-3 {
  background: linear-gradient(120deg, #f6d365 0%, #fda085 100%);
  color: #fff;
  font-size: 20px;
}

.ranking-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.post-title:hover {
  color: #409eff;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}

.post-author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.dot {
  color: #c0c4cc;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #909399;
  transition: color 0.2s;
}

.stat-item.highlight {
  color: #f56c6c;
  font-weight: 500;
}

.stat-item .el-icon {
  font-size: 14px;
}

.hot-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 60px;
}

.fire-emoji {
  font-size: 24px;
  line-height: 1;
}

.heat-value {
  font-size: 14px;
  font-weight: 700;
  color: #ff6b35;
}

.sidebar-widget {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.widget-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.widget-content p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.widget-content ul {
  margin: 10px 0;
  padding-left: 20px;
  color: #606266;
  font-size: 14px;
}

.widget-content li {
  margin-bottom: 4px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 作者列表样式 */
.author-item {
  align-items: center;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.user-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-desc {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 400px;
}

.user-stats {
  display: flex;
  gap: 16px;
}

.action-btn {
  align-self: center;
}

/* 标签榜样式 */
.tag-ranking-item {
  align-items: flex-start;
}

.tag-header {
  margin-bottom: 4px;
}

.tag-desc {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-stats {
  display: flex;
}

/* 侧边栏 */
.right-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  flex-shrink: 0;
}

/* 骨架屏间距 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 响应式 */
@media screen and (max-width: 992px) {
  .content-layout {
    flex-direction: column;
  }
  
  .right-sidebar {
    width: 100%;
    flex-direction: row;
    overflow-x: auto;
  }
  
  .sidebar-widget {
    flex: 1;
    min-width: 280px;
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 16px;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .filter-section {
    gap: 12px;
  }
  
  .filter-group {
    flex-wrap: wrap;
  }
  
  .ranking-item {
    padding: 16px;
    gap: 12px;
  }
  
  .ranking-index {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .ranking-index.rank-1,
  .ranking-index.rank-2,
  .ranking-index.rank-3 {
    font-size: 16px;
  }
  
  .user-avatar {
    width: 48px;
    height: 48px;
  }
  
  .post-stats,
  .user-stats {
    gap: 16px;
    flex-wrap: wrap;
  }
  
  .hot-score {
    display: none; /* 移动端隐藏热度分，节省空间 */
  }
}
</style>