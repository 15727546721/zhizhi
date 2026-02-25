import request from '@/utils/request'
import type { ApiResponse, User, Post, Tag } from '@/types'

// 用户排行榜
export function getUserRanking(type = 'comprehensive', limit = 20): Promise<ApiResponse<User[]>> {
  return request({ url: '/ranking/users', method: 'get', params: { type, limit } })
}

export function getFansRanking(limit = 20): Promise<ApiResponse<User[]>> {
  return request({ url: '/ranking/users/fans', method: 'get', params: { limit } })
}

export function getLikesRanking(limit = 20): Promise<ApiResponse<User[]>> {
  return request({ url: '/ranking/users/likes', method: 'get', params: { limit } })
}

export function getActiveRanking(limit = 20): Promise<ApiResponse<User[]>> {
  return request({ url: '/ranking/users/active', method: 'get', params: { limit } })
}

// 帖子排行榜
export function getPostRanking(period = 'week', sort = 'hot', limit = 20): Promise<ApiResponse<Post[]>> {
  return request({ url: '/ranking/posts', method: 'get', params: { period, sort, limit } })
}

export function getWeeklyPostRanking(sort = 'hot', limit = 20): Promise<ApiResponse<Post[]>> {
  return request({ url: '/ranking/posts/week', method: 'get', params: { sort, limit } })
}

export function getMonthlyPostRanking(sort = 'hot', limit = 20): Promise<ApiResponse<Post[]>> {
  return request({ url: '/ranking/posts/month', method: 'get', params: { sort, limit } })
}

// 标签排行榜
export function getTagRanking(sort = 'count', period = 'all', limit = 20): Promise<ApiResponse<Tag[]>> {
  return request({ url: '/ranking/tags', method: 'get', params: { sort, period, limit } })
}
