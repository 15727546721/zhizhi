import { ref, type Ref } from 'vue'
import favoritesApi from '@/api/favorites'
import { useUserStore } from '@/stores/module/user'

interface FavoriteItem {
  targetId: number | string
  targetType?: string
  type?: string
}

interface FavoriteStateManager {
  favoritesMap: Ref<Record<string, boolean>>
  loading: Ref<boolean>
  initializeFavorites: (items: FavoriteItem[]) => Promise<void>
  isFavorited: (targetId: number | string, type: string) => boolean
  updateFavoriteStatus: (targetId: number | string, type: string, favorited: boolean) => void
  clear: () => void
}

/**
 * 批量检查收藏状态
 */
export const checkFavoritesBatch = async (
  items: FavoriteItem[]
): Promise<Record<string, boolean>> => {
  const userStore = useUserStore()
  const favoritesMap: Record<string, boolean> = {}

  if (!userStore.isAuthenticated) {
    items.forEach((item) => {
      const key = `${item.targetType || item.type}_${item.targetId}`
      favoritesMap[key] = false
    })
    return favoritesMap
  }

  try {
    const checkItems = items.map((item) => ({
      targetId: item.targetId,
      targetType: item.targetType || item.type
    }))

    const res = await favoritesApi.checkFavoriteBatch(checkItems)

    if (res?.data && Array.isArray(res.data)) {
      res.data.forEach((item: { targetType: string; targetId: number; isFavorited: boolean }) => {
        const key = `${item.targetType}_${item.targetId}`
        favoritesMap[key] = item.isFavorited || false
      })
    }

    items.forEach((item) => {
      const key = `${item.targetType || item.type}_${item.targetId}`
      if (favoritesMap[key] === undefined) {
        favoritesMap[key] = false
      }
    })

    return favoritesMap
  } catch (error) {
    items.forEach((item) => {
      const key = `${item.targetType || item.type}_${item.targetId}`
      favoritesMap[key] = false
    })
    return favoritesMap
  }
}


/**
 * 创建响应式收藏状态管理器
 */
export const createFavoriteStateManager = (): FavoriteStateManager => {
  const favoritesMap = ref<Record<string, boolean>>({})
  const loading = ref(false)
  const userStore = useUserStore()

  const initializeFavorites = async (items: FavoriteItem[]): Promise<void> => {
    if (!userStore.isAuthenticated || items.length === 0) {
      favoritesMap.value = {}
      return
    }

    loading.value = true
    try {
      const newFavoritesMap = await checkFavoritesBatch(items)
      favoritesMap.value = { ...favoritesMap.value, ...newFavoritesMap }
    } finally {
      loading.value = false
    }
  }

  const isFavorited = (targetId: number | string, type: string): boolean => {
    if (!userStore.isAuthenticated) return false
    const key = `${type}_${targetId}`
    return favoritesMap.value[key] || false
  }

  const updateFavoriteStatus = (
    targetId: number | string,
    type: string,
    favorited: boolean
  ): void => {
    const key = `${type}_${targetId}`
    favoritesMap.value[key] = favorited
  }

  const clear = (): void => {
    favoritesMap.value = {}
  }

  return {
    favoritesMap,
    loading,
    initializeFavorites,
    isFavorited,
    updateFavoriteStatus,
    clear
  }
}

/**
 * 收藏工具函数
 */
export const FavoriteUtils = {
  normalizeType(type: string | undefined): string {
    if (!type) return 'post'
    return type.toLowerCase()
  },

  getTypeDisplayName(type: string): string {
    const typeMap: Record<string, string> = {
      post: '帖子',
      article: '文章',
      essay: '文章',
      comment: '评论'
    }
    return typeMap[this.normalizeType(type)] || '内容'
  }
}
