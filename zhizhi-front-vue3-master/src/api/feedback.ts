import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types'

interface Feedback {
  id: number
  type: number
  title: string
  content: string
  status: number
  createdAt: string
}

export const FeedbackType = { BUG: 0, SUGGESTION: 1, CONTENT: 2, OTHER: 3 } as const
export const FeedbackTypeOptions = [
  { value: 0, label: 'Bug问题' },
  { value: 1, label: '功能建议' },
  { value: 2, label: '内容问题' },
  { value: 3, label: '其他' }
]

export const FeedbackStatus = { PENDING: 0, PROCESSING: 1, RESOLVED: 2, CLOSED: 3 } as const
export const FeedbackStatusOptions = [
  { value: 0, label: '待处理', type: 'warning' },
  { value: 1, label: '处理中', type: 'primary' },
  { value: 2, label: '已解决', type: 'success' },
  { value: 3, label: '已关闭', type: 'info' }
]

export function submitFeedback(data: {
  type: number
  title: string
  content: string
  images?: string
  contact?: string
}): Promise<ApiResponse> {
  return request({ url: '/feedback/submit', method: 'post', data })
}

export function getMyFeedbackList(params: { pageNo?: number; pageSize?: number } = {}): Promise<ApiResponse<PageResponse<Feedback>>> {
  return request({
    url: '/feedback/my',
    method: 'get',
    params: { pageNo: params.pageNo || 1, pageSize: params.pageSize || 10 }
  })
}

export function getFeedbackDetail(id: number): Promise<ApiResponse<Feedback>> {
  return request({ url: '/feedback/detail', method: 'get', params: { id } })
}

export function getTypeName(type: number): string {
  const option = FeedbackTypeOptions.find((o) => o.value === type)
  return option ? option.label : '未知'
}

export function getStatusInfo(status: number): { label: string; type: string } {
  const option = FeedbackStatusOptions.find((o) => o.value === status)
  return option || { label: '未知', type: 'info' }
}
