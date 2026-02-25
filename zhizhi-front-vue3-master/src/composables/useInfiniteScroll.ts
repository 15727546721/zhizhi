import { ref, onMounted, onUnmounted, type Ref } from 'vue'

interface InfiniteScrollOptions {
  loadMore: () => Promise<boolean | void>
  threshold?: number
  container?: HTMLElement | null
}

interface InfiniteScrollReturn {
  loading: Ref<boolean>
  noMore: Ref<boolean>
  reset: () => void
}

/**
 * 无限滚动 Hook
 */
export function useInfiniteScroll(options: InfiniteScrollOptions): InfiniteScrollReturn {
  const { loadMore, threshold = 100, container = null } = options

  const loading = ref(false)
  const noMore = ref(false)

  let scrollHandler: (() => Promise<void>) | null = null

  const checkScroll = async () => {
    if (loading.value || noMore.value) return

    let scrollTop: number, windowHeight: number, documentHeight: number

    if (container) {
      scrollTop = container.scrollTop
      windowHeight = container.clientHeight
      documentHeight = container.scrollHeight
    } else {
      scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
      windowHeight = window.innerHeight
      documentHeight = document.documentElement.scrollHeight
    }

    if (scrollTop + windowHeight >= documentHeight - threshold) {
      loading.value = true
      try {
        const hasMore = await loadMore()
        if (hasMore === false) {
          noMore.value = true
        }
      } catch (error) {
        // 加载失败
      } finally {
        loading.value = false
      }
    }
  }

  const reset = () => {
    loading.value = false
    noMore.value = false
  }

  onMounted(() => {
    scrollHandler = checkScroll
    const target = container || window
    target.addEventListener('scroll', scrollHandler as EventListener)
  })

  onUnmounted(() => {
    if (scrollHandler) {
      const target = container || window
      target.removeEventListener('scroll', scrollHandler as EventListener)
      scrollHandler = null
    }
  })

  return { loading, noMore, reset }
}

interface CursorPaginationOptions<T> {
  fetchFn: (params: { cursor: string | null; pageSize: number }) => Promise<any>
  pageSize?: number
}

interface CursorPaginationReturn<T> {
  data: Ref<T[]>
  loading: Ref<boolean>
  hasMore: Ref<boolean>
  cursor: Ref<string | null>
  total: Ref<number | null>
  loadMore: (isRefresh?: boolean) => Promise<boolean>
  reset: () => void
  refresh: () => Promise<boolean>
}

/**
 * 游标分页 Hook
 */
export function useCursorPagination<T = any>(options: CursorPaginationOptions<T>): CursorPaginationReturn<T> {
  const { fetchFn, pageSize = 10 } = options

  const data = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const hasMore = ref(true)
  const cursor = ref<string | null>(null)
  const total = ref<number | null>(null)

  const loadMore = async (isRefresh = false): Promise<boolean> => {
    if (loading.value || (!hasMore.value && !isRefresh)) return false

    loading.value = true

    try {
      const response = await fetchFn({
        cursor: isRefresh ? null : cursor.value,
        pageSize
      })

      if (response && response.data) {
        const result = response.data
        const newData = result.data || []

        if (isRefresh) {
          data.value = newData
          total.value = result.total ?? null
        } else {
          data.value = [...data.value, ...newData]
        }

        cursor.value = result.nextCursor
        hasMore.value = result.hasMore === true

        return hasMore.value
      }

      return false
    } catch (error) {
      return false
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    data.value = []
    loading.value = false
    hasMore.value = true
    cursor.value = null
    total.value = null
  }

  const refresh = () => {
    reset()
    return loadMore(true)
  }

  return { data, loading, hasMore, cursor, total, loadMore, reset, refresh }
}
