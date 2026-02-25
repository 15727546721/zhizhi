/**
 * 请求缓存工具
 */

interface CacheItem<T> {
  value: T
  expireAt: number
}

interface CachedRequestOptions {
  ttl?: number
  forceRefresh?: boolean
  params?: Record<string, unknown>
}

// 内存缓存
const memoryCache = new Map<string, CacheItem<unknown>>()

// 进行中的请求
const pendingRequests = new Map<string, Promise<unknown>>()

// 默认过期时间 5 分钟
const DEFAULT_TTL = 5 * 60 * 1000

/**
 * 生成缓存键
 */
const generateCacheKey = (key: string, params: Record<string, unknown> = {}): string => {
  const paramStr = JSON.stringify(params)
  return `${key}:${paramStr}`
}

/**
 * 设置缓存
 */
export const setCache = <T>(key: string, value: T, ttl = DEFAULT_TTL): void => {
  memoryCache.set(key, {
    value,
    expireAt: Date.now() + ttl
  })
}

/**
 * 获取缓存
 */
export const getCache = <T>(key: string): T | null => {
  const cached = memoryCache.get(key) as CacheItem<T> | undefined
  if (!cached) return null

  if (Date.now() > cached.expireAt) {
    memoryCache.delete(key)
    return null
  }

  return cached.value
}

/**
 * 删除缓存
 */
export const removeCache = (key: string): void => {
  memoryCache.delete(key)
}

/**
 * 清除所有缓存
 */
export const clearAllCache = (): void => {
  memoryCache.clear()
}

/**
 * 清除匹配前缀的缓存
 */
export const clearCacheByPrefix = (prefix: string): void => {
  for (const key of memoryCache.keys()) {
    if (key.startsWith(prefix)) {
      memoryCache.delete(key)
    }
  }
}


/**
 * 带缓存的请求包装器
 */
export const cachedRequest = async <T>(
  cacheKey: string,
  requestFn: () => Promise<T>,
  options: CachedRequestOptions = {}
): Promise<T> => {
  const { ttl = DEFAULT_TTL, forceRefresh = false, params = {} } = options
  const fullKey = generateCacheKey(cacheKey, params)

  if (!forceRefresh) {
    const cached = getCache<T>(fullKey)
    if (cached !== null) {
      return cached
    }
  }

  if (pendingRequests.has(fullKey)) {
    return pendingRequests.get(fullKey) as Promise<T>
  }

  const requestPromise = requestFn()
    .then((result) => {
      setCache(fullKey, result, ttl)
      return result
    })
    .finally(() => {
      pendingRequests.delete(fullKey)
    })

  pendingRequests.set(fullKey, requestPromise)
  return requestPromise
}

/**
 * 防抖请求
 */
export const debounceRequest = <T, Args extends unknown[]>(
  fn: (...args: Args) => Promise<T>,
  delay = 300
): ((...args: Args) => Promise<T>) => {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (this: unknown, ...args: Args): Promise<T> {
    if (timer) clearTimeout(timer)
    return new Promise((resolve, reject) => {
      timer = setTimeout(() => {
        fn.apply(this, args).then(resolve).catch(reject)
      }, delay)
    })
  }
}

/**
 * 节流请求
 */
export const throttleRequest = <T, Args extends unknown[]>(
  fn: (...args: Args) => Promise<T>,
  interval = 1000
): ((...args: Args) => Promise<T | null>) => {
  let lastTime = 0
  let pendingPromise: Promise<T> | null = null

  return function (this: unknown, ...args: Args): Promise<T | null> {
    const now = Date.now()

    if (now - lastTime >= interval) {
      lastTime = now
      pendingPromise = fn.apply(this, args)
      return pendingPromise
    }

    return pendingPromise || Promise.resolve(null)
  }
}

export default {
  setCache,
  getCache,
  removeCache,
  clearAllCache,
  clearCacheByPrefix,
  cachedRequest,
  debounceRequest,
  throttleRequest
}
