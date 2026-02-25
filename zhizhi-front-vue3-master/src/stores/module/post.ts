import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPostsByCursor, getMyPosts } from '@/api/post'
import { validateApiResponse } from '@/utils/typeGuards'
import type { Post } from '@/types/post'
import type { PageResponse } from '@/types/api'

interface StatusConfig {
  type: string
  label: string
  code: number
}

interface SortOption {
  label: string
  value: string
}

/**
 * 帖子状态管理
 */
export const usePostStore = defineStore('post', () => {
  const STATUS_CONFIG: Record<string, StatusConfig> = {
    published: { type: 'success', label: '已发布', code: 1 },
    draft: { type: 'info', label: '草稿', code: 0 },
    pending: { type: 'warning', label: '待审核', code: 2 },
    deleted: { type: 'danger', label: '已删除', code: -1 }
  }

  const SORT_OPTIONS: Record<string, SortOption> = {
    latest: { label: '最新', value: 'latest' },
    hot: { label: '热门', value: 'hot' },
    comments: { label: '评论最多', value: 'comments' },
    likes: { label: '点赞最多', value: 'likes' }
  }

  const draftCount = ref(0)
  const draftCountCacheTime = ref(0)
  const DRAFT_CACHE_TTL = 5 * 60 * 1000

  const hotPostsCache = ref<Post[]>([])
  const hotPostsCacheTime = ref(0)
  const HOT_POSTS_CACHE_TTL = 10 * 60 * 1000

  const isDraftCountCacheValid = computed(() => {
    return Date.now() - draftCountCacheTime.value < DRAFT_CACHE_TTL
  })

  const isHotPostsCacheValid = computed(() => {
    return Date.now() - hotPostsCacheTime.value < HOT_POSTS_CACHE_TTL
  })

  const getStatusKeyByCode = (code: number): string => {
    for (const [key, config] of Object.entries(STATUS_CONFIG)) {
      if (config.code === code) return key
    }
    return 'draft'
  }

  const getStatusTagType = (status: string): string => {
    return STATUS_CONFIG[status]?.type || 'info'
  }

  const getStatusLabel = (status: string): string => {
    return STATUS_CONFIG[status]?.label || '未知'
  }

  const getStatusCode = (status: string): number => {
    return STATUS_CONFIG[status]?.code ?? 0
  }

  const transformStatus = (statusCode: number): string => {
    return getStatusKeyByCode(statusCode)
  }

  const fetchDraftCount = async (forceRefresh = false): Promise<number> => {
    if (!forceRefresh && isDraftCountCacheValid.value && draftCount.value > 0) {
      return draftCount.value
    }

    try {
      const response = await getMyPosts({
        pageNo: 1,
        pageSize: 1,
        status: 'DRAFT'
      })
      
      const data = validateApiResponse<PageResponse<Post>>(response)
      if (data && data.total !== undefined) {
        draftCount.value = data.total
        draftCountCacheTime.value = Date.now()
      }
    } catch (error) {
      // 获取失败
    }
    return draftCount.value
  }

  const updateDraftCount = (delta: number) => {
    draftCount.value = Math.max(0, draftCount.value + delta)
  }

  const clearDraftCountCache = () => {
    draftCountCacheTime.value = 0
  }

  const fetchHotPosts = async (forceRefresh = false): Promise<Post[]> => {
    if (!forceRefresh && isHotPostsCacheValid.value && hotPostsCache.value.length > 0) {
      return hotPostsCache.value
    }

    try {
      const response = await getPostsByCursor({
        pageSize: 10,
        sortBy: 'hot'
      })
      
      interface CursorResponse {
        data?: Post[]
        nextCursor?: string | null
        hasMore?: boolean
      }
      
      const data = validateApiResponse<CursorResponse>(response)
      if (data && data.data) {
        hotPostsCache.value = data.data
        hotPostsCacheTime.value = Date.now()
      }
    } catch (error) {
      // 获取失败
    }
    return hotPostsCache.value
  }

  const clearAllCache = () => {
    draftCountCacheTime.value = 0
    hotPostsCacheTime.value = 0
    hotPostsCache.value = []
  }

  return {
    STATUS_CONFIG,
    SORT_OPTIONS,
    draftCount,
    hotPostsCache,
    isDraftCountCacheValid,
    isHotPostsCacheValid,
    getStatusKeyByCode,
    getStatusTagType,
    getStatusLabel,
    getStatusCode,
    transformStatus,
    fetchDraftCount,
    updateDraftCount,
    clearDraftCountCache,
    fetchHotPosts,
    clearAllCache
  }
})
