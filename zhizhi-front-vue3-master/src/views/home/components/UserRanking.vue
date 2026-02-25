<template>
  <div class="ranking-card">
    <div class="ranking-header">
      <div class="ranking-title-wrapper">
        <el-icon class="ranking-icon"><user /></el-icon>
        <h3 class="ranking-title">用户榜</h3>
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
      <!-- 加载状态 -->
      <div v-if="loading && rankingList.length === 0" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <!-- 帖子列表 - 保持显示，避免闪烁 -->
      <template v-else-if="rankingList.length > 0">
        <div 
          v-for="(user, index) in rankingList" 
          :key="user.id" 
          class="ranking-item"
          @click="goToUserProfile(user.id)"
        >
          <span class="ranking-index" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
          <div class="ranking-content">
            <div class="user-info">
              <UserAvatar :size="40" :src="user.avatar || defaultAvatar" :username="user.username" :nickname="getUserName(user)" />
              <div class="user-details">
                <h4 class="user-name">{{ getUserName(user) }}</h4>
                <p class="user-desc">{{ user.description || '暂无描述' }}</p>
              </div>
            </div>
            <div class="user-stats">
              <span>帖子数: {{ user.postCount || 0 }}</span>
              <span>粉丝数: {{ user.fansCount || 0 }}</span>
            </div>
          </div>
        </div>
      </template>
      
      <!-- 空状态 - 只在非加载且无数据时显示 -->
      <div v-else-if="!loading" class="empty-container">
        <span>暂无用户数据</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User, ArrowRight, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserRanking } from '@/api/ranking'
import UserAvatar from '@/components/UserAvatar.vue'

defineOptions({
  name: 'UserRanking'
})

interface RankingUser {
  id: number
  nickname?: string
  name?: string
  username?: string
  avatar?: string
  description?: string
  fansCount: number
  followCount: number
  likeCount: number
  postCount: number
  rank: number
  score: number
}

interface UserRankingItem {
  userId?: number
  id?: number
  nickname?: string
  name?: string
  username?: string
  avatar?: string
  description?: string
  fansCount?: number
  followCount?: number
  likeCount?: number
  postCount?: number
  rank?: number
  score?: number
}

const router = useRouter()

const timeRange = ref('week')
const rankingList = ref<RankingUser[]>([])
const loading = ref(false)
// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
// 数据缓存，避免重复加载
const dataCache = ref<Map<string, RankingUser[]>>(new Map())

// 获取用户名称的辅助函数（处理不同的字段名）
const getUserName = (user: RankingUser): string => {
  return user.nickname || user.name || user.username || '匿名用户'
}

// 获取用户排行榜数据
const loadUserRanking = async (showLoading = true) => {
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
    // 使用排行榜API - getUserRanking(type, limit)
    const response = await getUserRanking('comprehensive', 5)

    // 处理响应数据 - 后端返回结构: { code, data: [UserRankingVO] }
    // UserRankingVO: { rank, userId, username, nickname, avatar, description, fansCount, likeCount, postCount, score }
    if (response && response.data) {
      let users = Array.isArray(response.data) ? response.data : []
      
      // 转换数据格式，处理字段映射
      const newList: RankingUser[] = users.map((item: UserRankingItem) => {
        return {
          id: item.userId || item.id || 0,
          nickname: item.nickname,
          name: item.name || item.nickname,
          username: item.username,
          avatar: item.avatar,
          description: item.description,
          fansCount: item.fansCount || 0,
          followCount: item.followCount || 0,
          likeCount: item.likeCount || 0,
          postCount: item.postCount || 0,
          rank: item.rank || 0,
          score: item.score || 0
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

// 跳转到用户主页
const goToUserProfile = (userId: number) => {
  if (!userId) return
  router.push(`/user/${userId}`)
}

// 监听时间范围变化
watch(timeRange, () => {
  loadUserRanking(true)
})

// 初始化加载
onMounted(() => {
  loadUserRanking(true)
})
</script>

<style scoped>
.ranking-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-top: 20px;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-desc {
  margin: 0;
  font-size: 12px;
  color: #8a919f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8a919f;
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