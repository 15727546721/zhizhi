import request from '@/utils/request'
import { cachedRequest, clearCacheByPrefix } from '@/utils/cache'
import { CACHE_TTL } from '@/constants'
import type { ApiResponse, Post, PageResponse } from '@/types'

interface HomePostParams {
  type?: string
  tagId?: number
  sort?: string
  page?: number
  size?: number
  forceRefresh?: boolean
  excludeId?: number
}

export function getFollowingPosts(params: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/home/following',
    method: 'get',
    params: { page: params.page || 1, size: params.size || 10 }
  })
}

export function getPosts(params: HomePostParams): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/home/posts',
    method: 'get',
    params: {
      type: params.type,
      tagId: params.tagId,
      sort: params.sort || 'latest',
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

export function getFeaturedPosts(params: HomePostParams): Promise<ApiResponse<PageResponse<Post>>> {
  const requestFn = () =>
    request({
      url: '/home/featured',
      method: 'get',
      params: { tagId: params.tagId, page: params.page || 1, size: params.size || 10 }
    })

  if (params.page === 1 && !params.tagId) {
    return cachedRequest('home:featured', requestFn, {
      ttl: CACHE_TTL.MEDIUM,
      forceRefresh: params.forceRefresh,
      params: { page: params.page, size: params.size }
    })
  }
  return requestFn()
}

export function getHotPosts(params: HomePostParams): Promise<ApiResponse<PageResponse<Post>>> {
  const requestFn = () =>
    request({
      url: '/home/hot',
      method: 'get',
      params: { tagId: params.tagId, page: params.page || 1, size: params.size || 10 }
    })

  if (params.page === 1 && !params.tagId) {
    return cachedRequest('home:hot', requestFn, {
      ttl: CACHE_TTL.MEDIUM,
      forceRefresh: params.forceRefresh,
      params: { page: params.page, size: params.size }
    })
  }
  return requestFn()
}

export function getRelatedPosts(params: HomePostParams): Promise<ApiResponse<Post[]>> {
  return request({
    url: '/home/related',
    method: 'get',
    params: {
      type: params.type,
      excludeId: params.excludeId,
      page: params.page || 1,
      size: params.size || 5
    }
  })
}

export function clearHomeCache(): void {
  clearCacheByPrefix('home:')
}

export const SortType = {
  LATEST: 'latest',
  HOTTEST: 'hottest',
  COMMENTS: 'comments',
  LIKES: 'likes',
  FAVORITES: 'favorites'
} as const
