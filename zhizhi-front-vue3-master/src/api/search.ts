import request from '@/utils/request'
import type { ApiResponse, Post, User, Tag } from '@/types'

interface AggregateSearchResult {
  posts: Post[]
  users: User[]
  tags: Tag[]
}

export function aggregateSearch(params: {
  keyword: string
  postLimit?: number
  userLimit?: number
  tagLimit?: number
}): Promise<ApiResponse<AggregateSearchResult>> {
  return request({
    url: '/search/aggregate',
    method: 'get',
    params: {
      keyword: params.keyword,
      postLimit: params.postLimit || 10,
      userLimit: params.userLimit || 10,
      tagLimit: params.tagLimit || 10
    }
  })
}

export function searchPosts(params: { keyword: string; limit?: number }): Promise<ApiResponse<Post[]>> {
  return request({
    url: '/search/posts',
    method: 'get',
    params: { keyword: params.keyword, limit: params.limit || 20 }
  })
}

export function searchUsers(params: { keyword: string; limit?: number }): Promise<ApiResponse<User[]>> {
  return request({
    url: '/search/users',
    method: 'get',
    params: { keyword: params.keyword, limit: params.limit || 20 }
  })
}

export function searchTags(params: { keyword: string; limit?: number }): Promise<ApiResponse<Tag[]>> {
  return request({
    url: '/search/tags',
    method: 'get',
    params: { keyword: params.keyword, limit: params.limit || 20 }
  })
}

export const SearchType = {
  ALL: 'all',
  POSTS: 'posts',
  USERS: 'users',
  TAGS: 'tags'
} as const

export function getSearchHistory(): Promise<ApiResponse<string[]>> {
  return request({ url: '/search/history', method: 'get' })
}

export function deleteSearchHistory(keyword: string): Promise<ApiResponse> {
  return request({ url: '/search/history/delete', method: 'post', params: { keyword } })
}

export function clearSearchHistory(): Promise<ApiResponse> {
  return request({ url: '/search/history/clear', method: 'post' })
}

export function getHotWords(): Promise<ApiResponse<string[]>> {
  return request({ url: '/search/hot', method: 'get' })
}

export function getSearchSuggestions(prefix: string): Promise<ApiResponse<string[]>> {
  return request({ url: '/search/suggest', method: 'get', params: { prefix } })
}
