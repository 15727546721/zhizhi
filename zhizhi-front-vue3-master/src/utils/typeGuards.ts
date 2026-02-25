/**
 * TypeScript 类型守卫工具
 * 用于运行时类型检查和验证
 */

import type {
  UserDetail,
  UserBrief,
  PostDetail,
  PostItem,
  CommentDetail,
  ColumnDetail,
  ApiResponse,
  PageResponse
} from '@/types'

/**
 * 检查是否为有效的 API 响应
 */
export function isApiResponse<T>(value: unknown): value is ApiResponse<T> {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const response = value as Record<string, unknown>
  return (
    typeof response.code === 'number' &&
    (typeof response.info === 'string' || response.info === null) &&
    'data' in response
  )
}

/**
 * 检查是否为有效的分页响应
 */
export function isPageResponse<T>(value: unknown): value is PageResponse<T> {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const response = value as Record<string, unknown>
  return (
    typeof response.pageNo === 'number' &&
    typeof response.pageSize === 'number' &&
    typeof response.total === 'number' &&
    'data' in response
  )
}

/**
 * 检查是否为有效的用户详情
 */
export function isUserDetail(value: unknown): value is UserDetail {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const user = value as Record<string, unknown>
  return (
    typeof user.id === 'number' &&
    typeof user.username === 'string' &&
    typeof user.nickname === 'string' &&
    typeof user.avatar === 'string'
  )
}

/**
 * 检查是否为有效的用户简要信息
 */
export function isUserBrief(value: unknown): value is UserBrief {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const user = value as Record<string, unknown>
  return (
    typeof user.id === 'number' &&
    typeof user.username === 'string' &&
    typeof user.nickname === 'string' &&
    typeof user.avatar === 'string'
  )
}

/**
 * 检查是否为有效的帖子详情
 */
export function isPostDetail(value: unknown): value is PostDetail {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const post = value as Record<string, unknown>
  return (
    typeof post.id === 'number' &&
    typeof post.title === 'string' &&
    typeof post.content === 'string' &&
    typeof post.userId === 'number'
  )
}

/**
 * 检查是否为有效的帖子列表项
 */
export function isPostItem(value: unknown): value is PostItem {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const post = value as Record<string, unknown>
  return (
    typeof post.id === 'number' &&
    typeof post.title === 'string' &&
    typeof post.userId === 'number'
  )
}

/**
 * 检查是否为有效的评论详情
 */
export function isCommentDetail(value: unknown): value is CommentDetail {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const comment = value as Record<string, unknown>
  return (
    typeof comment.id === 'number' &&
    typeof comment.content === 'string' &&
    typeof comment.postId === 'number' &&
    typeof comment.userId === 'number'
  )
}

/**
 * 检查是否为有效的专栏详情
 */
export function isColumnDetail(value: unknown): value is ColumnDetail {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  
  const column = value as Record<string, unknown>
  return (
    typeof column.id === 'number' &&
    typeof column.name === 'string' &&
    typeof column.userId === 'number'
  )
}

/**
 * 检查是否为数组
 */
export function isArray<T>(value: unknown, itemGuard?: (item: unknown) => item is T): value is T[] {
  if (!Array.isArray(value)) {
    return false
  }
  
  if (itemGuard) {
    return value.every(item => itemGuard(item))
  }
  
  return true
}

/**
 * 检查是否为非空值
 */
export function isNotNull<T>(value: T | null | undefined): value is T {
  return value !== null && value !== undefined
}

/**
 * 检查是否为有效的 ID
 */
export function isValidId(value: unknown): value is number {
  return typeof value === 'number' && value > 0 && Number.isInteger(value)
}

/**
 * 检查是否为有效的时间字符串
 */
export function isValidTimeString(value: unknown): value is string {
  if (typeof value !== 'string') {
    return false
  }
  
  const date = new Date(value)
  return !isNaN(date.getTime())
}

/**
 * 安全的类型断言
 * 如果类型检查失败，返回默认值
 */
export function assertType<T>(
  value: unknown,
  guard: (value: unknown) => value is T,
  defaultValue: T
): T {
  return guard(value) ? value : defaultValue
}

/**
 * 验证 API 响应并提取数据
 */
export function validateApiResponse<T>(
  response: unknown,
  dataGuard?: (data: unknown) => data is T
): T | null {
  // 如果响应是字符串，尝试解析 JSON
  if (typeof response === 'string') {
    try {
      response = JSON.parse(response)
    } catch (e) {
      console.error('[Type Guard] Failed to parse JSON response')
      return null
    }
  }
  
  // 如果响应本身就是标准 API 响应格式
  if (isApiResponse(response)) {
    if (response.code !== 20000) {
      console.warn('[Type Guard] API error:', response.info)
      return null
    }
    
    if (dataGuard && !dataGuard(response.data)) {
      console.error('[Type Guard] Invalid response data type')
      return null
    }
    
    return response.data as T
  }
  
  // 如果响应被包装在对象中（例如 axios 响应）
  if (response && typeof response === 'object' && 'data' in response) {
    let wrappedResponse = (response as any).data
    
    // 如果 data 是字符串，尝试解析
    if (typeof wrappedResponse === 'string') {
      try {
        wrappedResponse = JSON.parse(wrappedResponse)
      } catch (e) {
        console.error('[Type Guard] Failed to parse JSON data')
        return null
      }
    }
    
    if (isApiResponse(wrappedResponse)) {
      if (wrappedResponse.code !== 20000) {
        console.warn('[Type Guard] API error:', wrappedResponse.info)
        return null
      }
      
      if (dataGuard && !dataGuard(wrappedResponse.data)) {
        console.error('[Type Guard] Invalid response data type')
        return null
      }
      
      return wrappedResponse.data as T
    }
  }
  
  // 调试：打印响应结构以便排查
  if (response && typeof response === 'object') {
    const keys = Object.keys(response)
    console.error('[Type Guard] Invalid API response format. Keys:', keys, 'Has code:', 'code' in response, 'Has data:', 'data' in response, 'Has info:', 'info' in response)
  } else {
    console.error('[Type Guard] Invalid API response format. Type:', typeof response)
  }
  return null
}

/**
 * 验证分页响应并提取数据
 */
export function validatePageResponse<T>(
  response: unknown,
  dataGuard?: (data: unknown) => data is T
): PageResponse<T> | null {
  if (!isApiResponse(response)) {
    console.error('[Type Guard] Invalid API response:', response)
    return null
  }
  
  if (response.code !== 20000) {
    console.warn('[Type Guard] API error:', response.info)
    return null
  }
  
  if (!isPageResponse(response.data)) {
    console.error('[Type Guard] Invalid page response:', response.data)
    return null
  }
  
  if (dataGuard && !dataGuard(response.data.data)) {
    console.error('[Type Guard] Invalid page data:', response.data.data)
    return null
  }
  
  return response.data as PageResponse<T>
}

/**
 * 创建类型守卫工厂
 */
export function createTypeGuard<T>(
  validator: (value: unknown) => boolean
): (value: unknown) => value is T {
  return (value: unknown): value is T => validator(value)
}

/**
 * 组合多个类型守卫
 */
export function combineGuards<T>(
  ...guards: Array<(value: unknown) => boolean>
): (value: unknown) => value is T {
  return (value: unknown): value is T => {
    return guards.every(guard => guard(value))
  }
}
