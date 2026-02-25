import request from '@/utils/request'
import type { ApiResponse, PageResponse, User } from '@/types'

export function blockUser(userId: number): Promise<ApiResponse> {
  return request({ url: `/blocks/${userId}`, method: 'post' })
}

export function unblockUser(userId: number): Promise<ApiResponse> {
  return request({ url: `/blocks/unblock/${userId}`, method: 'post' })
}

export function isBlocked(userId: number): Promise<ApiResponse<boolean>> {
  return request({ url: `/blocks/status/${userId}`, method: 'get' })
}

export function getBlockList(params: { pageNo?: number; pageSize?: number } = {}): Promise<ApiResponse<PageResponse<User>>> {
  return request({
    url: '/blocks',
    method: 'get',
    params: { pageNo: params.pageNo || 1, pageSize: params.pageSize || 20 }
  })
}

export function getBlockCount(): Promise<ApiResponse<number>> {
  return request({ url: '/blocks/count', method: 'get' })
}
