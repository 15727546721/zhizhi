import { ref, computed } from 'vue'
import { getFollowingList, getFollowersList, getUserFollowingList, getUserFollowersList } from '@/api/follow'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/module/user'

/**
 * 用户关注/粉丝数据管理 Composable
 * @param {string|number} userId - 用户ID
 * @param {string} type - 类型：'following' | 'followers'
 * @param {boolean} isOwnProfile - 是否自己的主页
 */
export function useUserFollow(userId, type = 'following', isOwnProfile = false) {
  const userStore = useUserStore()
  const list = ref([])
  const loading = ref(false)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const error = ref(null)

  const hasMore = computed(() => {
    return list.value.length < total.value
  })

  // 加载列表
  const loadList = async (reset = false) => {
    if (loading.value) return

    try {
      loading.value = true
      error.value = null

      if (reset) {
        pageNum.value = 1
        list.value = []
      }

      // 获取实际的 userId 值（支持响应式引用）
      const actualUserId = typeof userId === 'object' && userId.value !== undefined 
        ? userId.value 
        : userId
      
      // 判断是否自己的主页
      const currentUserId = userStore.userInfo?.id
      const isOwn = isOwnProfile || (currentUserId && actualUserId && currentUserId.toString() === actualUserId.toString())
      
      // 根据是否自己的主页调用不同的API
      let response
      if (isOwn) {
        // 获取自己的关注/粉丝列表
        const apiCall = type === 'following' ? getFollowingList : getFollowersList
        response = await apiCall({
          page: pageNum.value,
          size: pageSize.value
        })
      } else {
        // 获取指定用户的关注/粉丝列表
        const apiCall = type === 'following' ? getUserFollowingList : getUserFollowersList
        response = await apiCall(actualUserId, {
          page: pageNum.value,
          size: pageSize.value
        })
      }

      if (response.code === 20000) {
        let newList = []
        let totalCount = 0

        // 处理不同的响应数据结构
        if (Array.isArray(response.data)) {
          newList = response.data
          totalCount = response.data.length
        } else if (response.data && response.data.list) {
          newList = response.data.list
          totalCount = response.data.total || response.data.list.length
        }

        // 转换数据格式
        const formattedList = newList.map(item => {
          // 根据类型获取用户信息
          const user = type === 'following' 
            ? (item.followed || item.followedUser || item)
            : (item.follower || item.followerUser || item)

          return {
            id: user.id || item.id,
            userId: user.userId || user.id,
            nickname: user.nickname || user.name || user.username || '匿名用户',
            avatar: user.avatar || user.avatarUrl,
            description: user.description || user.bio || '',
            followTime: item.createTime || item.create_time || item.followTime,
            isFollowing: item.isFollowing !== undefined ? item.isFollowing : false,
            // 保留原始数据
            raw: item
          }
        })

        if (reset) {
          list.value = formattedList
        } else {
          list.value.push(...formattedList)
        }
        total.value = totalCount
        if (!reset) {
          pageNum.value++
        }
      } else {
        error.value = response.info || `获取${type === 'following' ? '关注' : '粉丝'}列表失败`
        ElMessage.error(error.value)
      }
    } catch (err) {
      error.value = err.message || `获取${type === 'following' ? '关注' : '粉丝'}列表失败`
      ElMessage.error(error.value)
    } finally {
      loading.value = false
    }
  }

  // 加载更多
  const loadMore = () => {
    if (hasMore.value && !loading.value) {
      loadList(false)
    }
  }

  // 刷新
  const refresh = () => {
    loadList(true)
  }

  return {
    list,
    loading,
    error,
    hasMore,
    total,
    loadList,
    loadMore,
    refresh
  }
}

