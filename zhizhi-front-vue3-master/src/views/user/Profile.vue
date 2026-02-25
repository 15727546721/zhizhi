<template>
  <div class="profile-container">
    <!-- 加载骨架屏 -->
    <ProfileSkeleton v-if="loading && !error" />
    
    <!-- 错误提示 -->
    <ProfileError v-else-if="error && !loading" :error="error" @retry="refresh" />
    
    <!-- 主内容 -->
    <template v-else>
      <!-- 个人信息头部 -->
      <ProfileHeader
        :user-info="userInfo"
        :is-own-profile="isOwnProfile"
        :is-authenticated="userStore.isAuthenticated"
        :is-following="isFollowing"
        :is-followed-by="isFollowedBy"
        :follow-loading="followLoading"
        @follow="handleFollow"
        @avatar-updated="handleAvatarUpdated"
      />

      <!-- 数据统计 -->
      <div class="stats-wrapper">
        <ProfileStats
          :stats="stats"
          :is-own-profile="isOwnProfile"
          @stat-click="handleStatClick"
        />
      </div>

      <!-- 标签页内容 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="帖子" name="posts">
            <template #label>
              <span class="tab-label">
                帖子
                <el-badge :value="stats.postCount || 0" class="tab-badge" v-if="stats.postCount > 0"/>
              </span>
            </template>
            <PostsTab
              :key="`posts-${userId}`"
              :user-id="userId"
              :is-own-profile="isOwnProfile"
              @post-deleted="refresh"
            />
          </el-tab-pane>

          <el-tab-pane label="评论" name="comments">
            <template #label>
              <span class="tab-label">
                评论
                <el-badge :value="stats.commentCount || 0" class="tab-badge" v-if="stats.commentCount > 0"/>
              </span>
            </template>
            <CommentsTab :user-id="userId" />
          </el-tab-pane>

          <el-tab-pane v-if="isOwnProfile" label="点赞" name="likes">
            <template #label>
              <span class="tab-label">点赞</span>
            </template>
            <LikesTab :user-id="userId" />
          </el-tab-pane>

          <el-tab-pane label="关注" name="following">
            <template #label>
              <span class="tab-label">
                关注
                <el-badge :value="stats.followCount || 0" class="tab-badge" v-if="stats.followCount > 0"/>
              </span>
            </template>
            <FollowingTab :key="`following-${userId}`" :user-id="userId" :is-own-profile="isOwnProfile" />
          </el-tab-pane>

          <el-tab-pane label="粉丝" name="followers">
            <template #label>
              <span class="tab-label">
                粉丝
                <el-badge :value="stats.fansCount || 0" class="tab-badge" v-if="stats.fansCount > 0"/>
              </span>
            </template>
            <FollowersTab :key="`followers-${userId}`" :user-id="userId" :is-own-profile="isOwnProfile" />
          </el-tab-pane>

          <el-tab-pane label="收藏夹" name="favorites">
            <template #label>
              <span class="tab-label">收藏夹</span>
            </template>
            <FavoritesTab :key="`favorites-${userId}`" :user-id="userId" :is-own-profile="isOwnProfile" />
          </el-tab-pane>

          <el-tab-pane label="专栏" name="columns">
            <template #label>
              <span class="tab-label">专栏</span>
            </template>
            <ColumnsTab :key="`columns-${userId}`" :user-id="userId" :is-own-profile="isOwnProfile" />
          </el-tab-pane>

          <el-tab-pane label="资料" name="info">
            <ProfileInfoTab
              :user-info="userInfo"
              :is-own-profile="isOwnProfile"
              :loading="loading"
              @change-password="showPasswordDialog = true"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
    
    <!-- 修改密码对话框 -->
    <PasswordDialog v-model:visible="showPasswordDialog" />
  </div>
</template>

<script setup lang="ts" name="UserProfile">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/module/user'
import { useProfileData } from './composables/useProfileData'
import { followUser, unfollowUser } from '@/api/follow'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

// 组件
import ProfileSkeleton from './components/ProfileSkeleton.vue'
import ProfileError from './components/ProfileError.vue'
import ProfileHeader from './components/ProfileHeader.vue'
import ProfileStats from './components/ProfileStats.vue'
import ProfileInfoTab from './components/ProfileInfoTab.vue'
import PasswordDialog from './components/PasswordDialog.vue'
import PostsTab from './components/tabs/PostsTab.vue'
import FollowingTab from './components/tabs/FollowingTab.vue'
import FollowersTab from './components/tabs/FollowersTab.vue'
import CommentsTab from './components/tabs/CommentsTab.vue'
import LikesTab from './components/tabs/LikesTab.vue'
import FavoritesTab from './components/tabs/FavoritesTab.vue'
import ColumnsTab from './components/tabs/ColumnsTab.vue'

const route = useRoute()
const userStore = useUserStore()

const userId = computed(() => route.params.userId as string)

// 使用 composable 管理数据
const {
  userInfo,
  stats,
  loading,
  error,
  isOwnProfile,
  isFollowing,
  isFollowedBy,
  refresh
} = useProfileData(userId)

// 状态
const activeTab = ref('posts')
const followLoading = ref(false)
const showPasswordDialog = ref(false)
const isInitialized = ref(false)

// 处理统计点击
const handleStatClick = (statType: string) => {
  const tabMap: Record<string, string> = {
    'posts': 'posts',
    'likes': 'likes',
    'follows': 'following',
    'fans': 'followers'
  }
  const tabName = tabMap[statType]
  if (tabName) {
    if (tabName === 'likes' && !isOwnProfile.value) return
    activeTab.value = tabName
  }
}

// 处理关注
const handleFollow = async () => {
  if (!userInfo.value?.id || followLoading.value) return

  try {
    followLoading.value = true
    
    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      window.dispatchEvent(new CustomEvent('show-login-dialog'))
      return
    }
    
    const wasFollowing = isFollowing.value
    
    if (isFollowing.value) {
      isFollowing.value = false
      if (stats.value.fansCount > 0) stats.value.fansCount--
      
      const response = await unfollowUser(userInfo.value.id) as ApiResponse
      if (response.code === 20000) {
        ElMessage.success('取消关注成功')
        await refresh()
      } else {
        isFollowing.value = wasFollowing
        stats.value.fansCount++
        ElMessage.error(response.info || '取消关注失败')
      }
    } else {
      isFollowing.value = true
      stats.value.fansCount = (stats.value.fansCount || 0) + 1
      
      const response = await followUser(userInfo.value.id) as ApiResponse
      if (response.code === 20000) {
        ElMessage.success(isFollowedBy.value ? '互相关注成功' : '关注成功')
        await refresh()
      } else {
        isFollowing.value = wasFollowing
        if (stats.value.fansCount > 0) stats.value.fansCount--
        ElMessage.error(response.info || '关注失败')
      }
    }
  } catch (err) {
    const error = err as { response?: { status?: number } }
    if (error.response?.status === 401) {
      ElMessage.warning('请先登录')
      window.dispatchEvent(new CustomEvent('show-login-dialog'))
    }
  } finally {
    followLoading.value = false
  }
}

// 处理头像更新
const handleAvatarUpdated = (url: string) => {
  if (userInfo.value) userInfo.value.avatar = url
  if (userStore.userInfo) userStore.updateUserInfo({ avatar: url })
  refresh()
}

// 监听路由变化
watch(() => route.params.userId, (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    isInitialized.value = false
    activeTab.value = 'posts'
    refresh()
  }
}, { immediate: false })

// 监听用户资料更新事件
const handleProfileUpdated = (event: Event) => {
  const { userId: updatedUserId } = (event as CustomEvent).detail || {}
  if (updatedUserId && updatedUserId.toString() === userId.value?.toString()) {
    setTimeout(() => refresh(), 100)
  }
}

onMounted(() => {
  window.addEventListener('user-profile-updated', handleProfileUpdated)
  if (!isInitialized.value) {
    refresh().then(() => { isInitialized.value = true })
  }
})

onUnmounted(() => {
  window.removeEventListener('user-profile-updated', handleProfileUpdated)
})
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background-color: var(--el-bg-color-page);
}

.stats-wrapper {
  background: linear-gradient(to right, #fff, #f8faff);
  border-radius: 12px;
  padding: 0 32px 24px;
  margin-top: -24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.profile-main {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
}

.profile-tabs {
  padding: 0 32px;
  border-bottom: 1px solid #f0f2f5;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: none;
  padding: 8px 0;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--el-color-primary), #409eff);
}

.profile-tabs :deep(.el-tabs__item) {
  padding: 12px 24px;
  font-size: 16px;
  color: #666;
  transition: all 0.3s ease;
  height: auto;
  line-height: 1.5;
}

.profile-tabs :deep(.el-tabs__item:hover) {
  color: var(--el-color-primary);
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 600;
  color: var(--el-color-primary);
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  position: relative;
  padding: 0 4px;
}

.tab-badge :deep(.el-badge__content) {
  background-color: #f0f2f5;
  color: #8a919f;
  border: none;
  padding: 0 8px;
  height: 18px;
  line-height: 18px;
  border-radius: 9px;
  font-weight: normal;
  font-size: 12px;
  transform: translateY(-2px);
}

@media screen and (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .stats-wrapper {
    padding: 0 16px 16px;
  }

  .profile-tabs {
    padding: 0 16px;
  }

  .profile-tabs :deep(.el-tabs__item) {
    padding: 12px 16px;
    font-size: 14px;
  }
}
</style>
