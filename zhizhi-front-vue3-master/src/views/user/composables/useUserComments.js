import { ref, computed } from 'vue'
import { getUserComments } from '@/api/comment'
import { ElMessage } from 'element-plus'

/**
 * 用户评论数据管理 Composable
 * @param {string|number} userId - 用户ID
 */
export function useUserComments(userId) {
  const comments = ref([])
  const loading = ref(false)
  const pageNo = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const error = ref(null)

  const hasMore = computed(() => {
    return comments.value.length < total.value
  })

  // 获取实际的 userId 值（支持响应式引用和函数）
  const getUserId = () => {
    // 如果是函数，调用它获取值
    if (typeof userId === 'function') {
      return userId()
    }
    // 如果是 ref 或 computed，获取其 value
    if (typeof userId === 'object' && userId !== null && 'value' in userId) {
      return userId.value
    }
    // 否则直接返回
    return userId
  }

  // 加载评论列表
  const loadComments = async (reset = false) => {
    if (loading.value) return

    try {
      loading.value = true
      error.value = null

      if (reset) {
        pageNo.value = 1
        comments.value = []
      }

      const actualUserId = getUserId()
      if (!actualUserId) {
        error.value = '用户ID不能为空'
        return
      }

      const response = await getUserComments(actualUserId, {
        page: pageNo.value,
        size: pageSize.value
      })

      if (response.code === 20000) {
        const responseData = response.data
        let newComments = []
        let totalCount = 0

        // 后端返回格式：PageResponse<List<UserCommentItemVO>>
        if (responseData && responseData.data && Array.isArray(responseData.data)) {
          newComments = responseData.data
          totalCount = responseData.total || 0
        } else if (Array.isArray(responseData)) {
          newComments = responseData
          totalCount = responseData.length
        }

        if (reset) {
          comments.value = newComments
        } else {
          comments.value.push(...newComments)
        }
        total.value = totalCount
        if (!reset) {
          pageNo.value++
        }
      } else {
        error.value = response.info || '获取评论列表失败'
        ElMessage.error(error.value)
      }
    } catch (err) {
      error.value = err.message || '获取评论列表失败'
      ElMessage.error('获取评论列表失败')
    } finally {
      loading.value = false
    }
  }

  // 加载更多
  const loadMore = () => {
    if (hasMore.value && !loading.value) {
      loadComments(false)
    }
  }

  // 刷新
  const refresh = () => {
    loadComments(true)
  }

  return {
    comments,
    loading,
    error,
    hasMore,
    total,
    loadComments,
    loadMore,
    refresh
  }
}

