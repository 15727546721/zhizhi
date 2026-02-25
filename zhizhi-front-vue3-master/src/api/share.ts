import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export const SharePlatform = {
  COPY: 'copy',
  WEIBO: 'weibo',
  QQ: 'qq',
  WECHAT: 'wechat',
  OTHER: 'other'
} as const

type SharePlatformValue = (typeof SharePlatform)[keyof typeof SharePlatform]

export function sharePost(data: {
  postId: number
  platform?: SharePlatformValue
}): Promise<ApiResponse> {
  return request({
    url: '/share',
    method: 'post',
    data: { postId: data.postId, platform: data.platform || SharePlatform.OTHER }
  })
}

export function getShareStats(postId: number): Promise<ApiResponse<{ count: number }>> {
  return request({ url: `/share/stats/${postId}`, method: 'get' })
}

export function checkShared(postId: number, userId: number): Promise<ApiResponse<boolean>> {
  return request({ url: `/share/check/${postId}`, method: 'get', params: { userId } })
}
