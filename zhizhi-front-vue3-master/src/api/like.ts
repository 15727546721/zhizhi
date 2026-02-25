import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types'

/**
 * 点赞类型枚举
 */
export const LikeType = {
  POST: '1',
  ESSAY: '2',
  COMMENT: '3'
} as const

export type LikeTypeValue = (typeof LikeType)[keyof typeof LikeType]

interface LikeData {
  targetId: number
  type: LikeTypeValue
}

interface LikeCountResponse {
  count: number
  liked: boolean
}

export function like(data: LikeData): Promise<ApiResponse> {
  return request({
    url: '/likes/like',
    method: 'post',
    data: {
      targetId: data.targetId,
      type: data.type
    }
  })
}

export function unlike(data: LikeData): Promise<ApiResponse> {
  return request({
    url: '/likes/unlike',
    method: 'post',
    data: {
      targetId: data.targetId,
      type: data.type
    }
  })
}

export function checkLike(params: LikeData): Promise<ApiResponse<boolean>> {
  return request({
    url: '/likes/status',
    method: 'get',
    params: {
      targetId: params.targetId,
      type: params.type
    }
  })
}

export function getLikeCount(params: {
  type: LikeTypeValue
  targetId: number
  userId?: number
}): Promise<ApiResponse<LikeCountResponse>> {
  return request({
    url: '/likes/count',
    method: 'get',
    params
  })
}

export function getUserLikes(
  userId: number,
  params: { page?: number; size?: number } = {}
): Promise<ApiResponse<PageResponse<unknown>>> {
  return request({
    url: `/likes/user/${userId}`,
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 10
    }
  })
}
