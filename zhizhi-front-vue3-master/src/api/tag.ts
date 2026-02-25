import request from '@/utils/request'
import type { ApiResponse, Tag, Post, PageResponse } from '@/types'

export function getTagList(): Promise<ApiResponse<Tag[]>> {
  return request({ url: '/tag/list', method: 'get' })
}

export function getHotTags(timeRange = 'all', limit = 10): Promise<ApiResponse<Tag[]>> {
  return request({ url: '/tag/hot', method: 'get', params: { timeRange, limit } })
}

export function searchTags(keyword: string): Promise<ApiResponse<Tag[]>> {
  return request({ url: '/tag/search', method: 'get', params: { keyword } })
}

export function getTagPosts(tagId: number, pageNo = 1, pageSize = 20): Promise<ApiResponse<PageResponse<Post>>> {
  return request({ url: `/tag/${tagId}/posts`, method: 'get', params: { pageNo, pageSize } })
}

export function getTagStats(tagId: number): Promise<ApiResponse<{ postCount: number }>> {
  return request({ url: `/tag/${tagId}/stats`, method: 'get' })
}

export const getTagPage = getTagList
