/**
 * 全局常量配置
 */

// ==================== 类型定义 ====================

interface StatusConfig {
  code: number
  key: string
  label: string
  type: 'success' | 'info' | 'warning' | 'danger'
}

interface SortOption {
  value: string
  label: string
}

interface TypeConfig {
  value: string
  label: string
}

interface LikeTypeConfig {
  code: number
  label: string
}

// ==================== 帖子相关 ====================

/**
 * 帖子状态配置
 */
export const POST_STATUS: Record<string, StatusConfig> = {
  PUBLISHED: { code: 1, key: 'published', label: '已发布', type: 'success' },
  DRAFT: { code: 0, key: 'draft', label: '草稿', type: 'info' },
  PENDING: { code: 2, key: 'pending', label: '待审核', type: 'warning' },
  DELETED: { code: -1, key: 'deleted', label: '已删除', type: 'danger' }
}

/**
 * 帖子排序选项
 */
export const POST_SORT_OPTIONS: Record<string, SortOption> = {
  LATEST: { value: 'latest', label: '最新' },
  HOT: { value: 'hot', label: '热门' },
  COMMENTS: { value: 'comments', label: '评论最多' },
  LIKES: { value: 'likes', label: '点赞最多' },
  FAVORITES: { value: 'favorites', label: '收藏最多' }
}

/**
 * 帖子类型
 */
export const POST_TYPES: Record<string, TypeConfig> = {
  POST: { value: 'POST', label: '帖子' },
  ARTICLE: { value: 'ARTICLE', label: '文章' },
  DISCUSSION: { value: 'DISCUSSION', label: '讨论' },
  QUESTION: { value: 'QUESTION', label: '问答' }
}

// ==================== 分页相关 ====================

/**
 * 默认分页配置
 */
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100] as const,
  MAX_SIZE: 100
} as const

// ==================== 缓存相关 ====================

/**
 * 缓存TTL配置（毫秒）
 */
export const CACHE_TTL = {
  SHORT: 1 * 60 * 1000,      // 1分钟
  MEDIUM: 5 * 60 * 1000,     // 5分钟
  LONG: 30 * 60 * 1000,      // 30分钟
  HOUR: 60 * 60 * 1000,      // 1小时
  DAY: 24 * 60 * 60 * 1000   // 1天
} as const

// ==================== 交互相关 ====================

/**
 * 点赞类型
 */
export const LIKE_TYPES: Record<string, LikeTypeConfig> = {
  POST: { code: 1, label: '帖子' },
  COMMENT: { code: 2, label: '评论' }
}

/**
 * 收藏类型
 */
export const FAVORITE_TYPES = {
  POST: 'POST',
  COMMENT: 'COMMENT'
} as const

// ==================== 消息相关 ====================

/**
 * 消息类型
 */
export const MESSAGE_TYPES: Record<string, TypeConfig> = {
  SYSTEM: { value: 'SYSTEM', label: '系统消息' },
  LIKE: { value: 'LIKE', label: '点赞' },
  COMMENT: { value: 'COMMENT', label: '评论' },
  FOLLOW: { value: 'FOLLOW', label: '关注' },
  REPLY: { value: 'REPLY', label: '回复' }
}

// ==================== 工具函数 ====================

/**
 * 根据状态码获取状态配置
 */
export const getPostStatusByCode = (code: number): StatusConfig => {
  return Object.values(POST_STATUS).find(s => s.code === code) || POST_STATUS.DRAFT
}

/**
 * 根据状态键获取状态配置
 */
export const getPostStatusByKey = (key: string): StatusConfig => {
  return Object.values(POST_STATUS).find(s => s.key === key) || POST_STATUS.DRAFT
}

export default {
  POST_STATUS,
  POST_SORT_OPTIONS,
  POST_TYPES,
  PAGINATION,
  CACHE_TTL,
  LIKE_TYPES,
  FAVORITE_TYPES,
  MESSAGE_TYPES,
  getPostStatusByCode,
  getPostStatusByKey
}
