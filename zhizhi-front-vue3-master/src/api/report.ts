import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types'

interface Report {
  id: number
  targetType: number
  targetId: number
  reason: number
  description?: string
  status: number
  createdAt: string
}

export const ReportTargetType = { POST: 1, COMMENT: 2, USER: 3 } as const
export const ReportReason = { SPAM: 1, ILLEGAL: 2, PORN: 3, ATTACK: 4, PLAGIARISM: 5, OTHER: 6 } as const
export const ReportReasonOptions = [
  { value: 1, label: '垃圾广告' },
  { value: 2, label: '违法违规' },
  { value: 3, label: '色情低俗' },
  { value: 4, label: '人身攻击' },
  { value: 5, label: '抄袭侵权' },
  { value: 6, label: '其他' }
]
export const ReportStatus = { PENDING: 0, APPROVED: 1, REJECTED: 2, IGNORED: 3 } as const

export function submitReport(data: {
  targetType: number
  targetId: number
  reason: number
  description?: string
  evidenceUrls?: string[]
}): Promise<ApiResponse> {
  return request({ url: '/reports', method: 'post', data })
}

export function getMyReports(params: { page?: number; size?: number } = {}): Promise<ApiResponse<PageResponse<Report>>> {
  return request({ url: '/reports/mine', method: 'get', params: { page: params.page || 1, size: params.size || 10 } })
}

export function getReportDetail(reportId: number): Promise<ApiResponse<Report>> {
  return request({ url: `/reports/${reportId}`, method: 'get' })
}

export function reportPost(postId: number, reason: number, description = ''): Promise<ApiResponse> {
  return submitReport({ targetType: ReportTargetType.POST, targetId: postId, reason, description })
}

export function reportComment(commentId: number, reason: number, description = ''): Promise<ApiResponse> {
  return submitReport({ targetType: ReportTargetType.COMMENT, targetId: commentId, reason, description })
}

export function reportUser(userId: number, reason: number, description = ''): Promise<ApiResponse> {
  return submitReport({ targetType: ReportTargetType.USER, targetId: userId, reason, description })
}

export function getReasonName(reason: number): string {
  const option = ReportReasonOptions.find((o) => o.value === reason)
  return option ? option.label : '未知'
}

export function getStatusName(status: number): string {
  const statusMap: Record<number, string> = { 0: '待处理', 1: '已通过', 2: '已驳回', 3: '已忽略' }
  return statusMap[status] || '未知'
}

export function getStatusType(status: number): string {
  const typeMap: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return typeMap[status] || 'info'
}
