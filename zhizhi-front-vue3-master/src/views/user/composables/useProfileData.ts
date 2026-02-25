import { ref, computed, watch, type Ref, type ComputedRef } from 'vue'
import { getUserProfile } from '@/api/user'
import { useUserStore } from '@/stores/module/user'
import type { ApiResponse, UserInfo, UserStats, UserProfileResponse, UserProfile } from '@/types'
import { validateApiResponse } from '@/utils/typeGuards'

/**
 * useProfileData 返回类型
 */
export interface UseProfileDataReturn {
  userInfo: Ref<UserInfo | null>
  stats: Ref<UserStats>
  loading: Ref<boolean>
  error: Ref<string | null>
  isOwnProfile: ComputedRef<boolean>
  isFollowing: Ref<boolean>
  isFollowedBy: Ref<boolean>
  refresh: () => Promise<void>
}

/**
 * 用户主页数据管理 Composable
 * @param userId - 用户ID（响应式）
 */
export function useProfileData(userId: Ref<string | number | undefined>): UseProfileDataReturn {
  const userStore = useUserStore()
  
  // 状态
  const userInfo = ref<UserInfo | null>(null)
  const stats = ref<UserStats>({
    postCount: 0,
    followCount: 0,
    fansCount: 0,
    likeCount: 0,
    commentCount: 0,
    collectionCount: 0
  })
  const loading = ref(false)
  const error = ref<string | null>(null)
  const isFollowing = ref(false)
  const isFollowedBy = ref(false)
  
  // 计算属性：是否是自己的主页
  const isOwnProfile = computed(() => {
    const currentUserId = userStore.userInfo?.id
    return !!currentUserId && String(currentUserId) === String(userId.value)
  })
  
  // 加载用户数据
  const loadUserData = async (): Promise<void> => {
    if (!userId.value) return
    
    loading.value = true
    error.value = null
    
    try {
      const response = await getUserProfile(Number(userId.value))
      
      // 使用类型守卫验证响应
      const profileData = validateApiResponse<UserProfile>(response)
      
      if (profileData) {
        // 设置用户信息 - 映射增强类型到现有 UserInfo 结构
        userInfo.value = {
          id: profileData.basicInfo.id,
          username: profileData.basicInfo.username,
          nickname: profileData.basicInfo.nickname,
          avatar: profileData.basicInfo.avatar,
          gender: profileData.basicInfo.gender,
          birthday: profileData.basicInfo.birthday,
          description: profileData.basicInfo.bio, // bio → description
          region: profileData.basicInfo.location  // location → region
        }
        
        // 设置统计数据 - 直接使用类型化的数据
        stats.value = {
          postCount: profileData.stats.postCount,
          followCount: profileData.stats.followCount,
          fansCount: profileData.stats.fansCount,
          likeCount: profileData.stats.likeCount,
          commentCount: profileData.stats.commentCount,
          collectionCount: profileData.stats.favoriteCount
        }
        
        // 设置关注状态
        isFollowing.value = profileData.isFollowing
        isFollowedBy.value = profileData.isFollowedBy
      } else {
        error.value = '加载失败：数据格式错误'
      }
    } catch (err) {
      error.value = (err as Error).message || '加载失败'
    } finally {
      loading.value = false
    }
  }
  
  // 刷新数据
  const refresh = async (): Promise<void> => {
    await loadUserData()
  }
  
  // 监听 userId 变化
  watch(userId, (newId) => {
    if (newId) {
      loadUserData()
    }
  }, { immediate: true })
  
  return {
    userInfo,
    stats,
    loading,
    error,
    isOwnProfile,
    isFollowing,
    isFollowedBy,
    refresh
  }
}
