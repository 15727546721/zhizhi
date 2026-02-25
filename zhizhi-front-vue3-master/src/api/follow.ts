import request from '@/utils/request'
import type { ApiResponse, User, PageResponse } from '@/types'

interface PageParams {
  page?: number
  size?: number
}

export function followUser(followedId: number): Promise<ApiResponse> {
  return request({ url: `/follows/follow/${followedId}`, method: 'post' })
}

export function unfollowUser(followedId: number): Promise<ApiResponse> {
  return request({ url: `/follows/unfollow/${followedId}`, method: 'post' })
}

export function isFollowing(followedId: number): Promise<ApiResponse<boolean>> {
  return request({ url: `/follows/status/${followedId}`, method: 'get' })
}

export function getFollowingList(params: PageParams): Promise<ApiResponse<PageResponse<User>>> {
  return request({ url: '/follows/following', method: 'get', params })
}

export function getUserFollowingList(userId: number, params: PageParams): Promise<ApiResponse<PageResponse<User>>> {
  return request({ url: `/follows/following/${userId}`, method: 'get', params })
}

export function getFollowersList(params: PageParams): Promise<ApiResponse<PageResponse<User>>> {
  return request({ url: '/follows/followers', method: 'get', params })
}

export function getUserFollowersList(userId: number, params: PageParams): Promise<ApiResponse<PageResponse<User>>> {
  return request({ url: `/follows/followers/${userId}`, method: 'get', params })
}

export function getFollowingCount(): Promise<ApiResponse<number>> {
  return request({ url: '/follows/following/count', method: 'get' })
}

export function getFollowersCount(): Promise<ApiResponse<number>> {
  return request({ url: '/follows/followers/count', method: 'get' })
}

export function getUserFollowingCount(userId: number): Promise<ApiResponse<number>> {
  return request({ url: `/follows/following/count/${userId}`, method: 'get' })
}

export function getUserFollowersCount(userId: number): Promise<ApiResponse<number>> {
  return request({ url: `/follows/followers/count/${userId}`, method: 'get' })
}

export function searchFollowingUsers(keyword = '', limit = 10): Promise<ApiResponse<User[]>> {
  return request({ url: '/follows/following/search', method: 'get', params: { keyword, limit } })
}
