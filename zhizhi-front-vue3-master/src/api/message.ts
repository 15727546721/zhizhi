import request from '@/utils/request'
import type { ApiResponse, PageResponse, PrivateMessage, Conversation } from '@/types'

interface PageParams {
  page?: number
  size?: number
}

// 会话分页响应
interface ConversationPageResponse {
  records: Conversation[]
  total: number
  page: number
  size: number
  hasMore: boolean
}

// 会话相关
export function getConversationList(params: PageParams = {}): Promise<ApiResponse<ConversationPageResponse>> {
  return request({
    url: '/messages/conversations',
    method: 'get',
    params: { page: params.page || 1, size: params.size || 20 }
  })
}

export function getOrCreateConversation(targetUserId: number): Promise<ApiResponse<Conversation>> {
  return request({ url: `/messages/conversations/${targetUserId}`, method: 'post' })
}

export function deleteConversation(userId: number): Promise<ApiResponse> {
  return request({ url: `/messages/conversations/${userId}/delete`, method: 'post' })
}

// 消息相关
export function getMessages(userId: number, params: PageParams = {}): Promise<ApiResponse<PageResponse<PrivateMessage>>> {
  return request({
    url: `/messages/conversations/${userId}/messages`,
    method: 'get',
    params: { page: params.page || 1, size: params.size || 50 }
  })
}

export function sendMessage(receiverId: number, content: string): Promise<ApiResponse<{ messageId: number; status: number }>> {
  return request({ url: '/messages', method: 'post', data: { receiverId, content } })
}

export function sendImage(receiverId: number, imageUrl: string): Promise<ApiResponse> {
  return request({ url: '/messages/image', method: 'post', data: { receiverId, imageUrl } })
}

export function markAsRead(userId: number): Promise<ApiResponse> {
  return request({ url: `/messages/conversations/${userId}/read`, method: 'post' })
}

export function deleteMessage(messageId: number): Promise<ApiResponse> {
  return request({ url: `/messages/${messageId}/delete`, method: 'post' })
}

// 撤回消息（2分钟内可撤回）
export function withdrawMessage(messageId: number): Promise<ApiResponse> {
  return request({ url: `/messages/${messageId}/withdraw`, method: 'post' })
}

// 搜索消息
export function searchMessages(keyword: string, params: PageParams = {}): Promise<ApiResponse<PrivateMessage[]>> {
  return request({
    url: '/messages/search',
    method: 'get',
    params: { keyword, page: params.page || 1, size: params.size || 20 }
  })
}


// 统计相关
export function getTotalUnreadCount(): Promise<ApiResponse<number>> {
  return request({ url: '/messages/unread-count', method: 'get' })
}

export function checkDMPermission(targetUserId: number): Promise<ApiResponse<{
  canSend: boolean
  isGreeting: boolean
  reason?: string
}>> {
  return request({ url: `/messages/can-send/${targetUserId}`, method: 'get' })
}

export function getUnreadCount(userId: number): Promise<ApiResponse<number>> {
  return request({ url: `/messages/conversations/${userId}/unread-count`, method: 'get' })
}

// 消息状态
export const MessageStatus = {
  DELIVERED: 1,
  PENDING: 2,
  BLOCKED: 3,
  WITHDRAWN: 4
} as const

export function getMessageStatusText(status: number): string {
  const statusMap: Record<number, string> = {
    1: '已送达',
    2: '待对方回复',
    3: '发送失败',
    4: '已撤回'
  }
  return statusMap[status] || '未知'
}

// 关系类型
export const RelationType = {
  STRANGER: 0,
  FOLLOWING: 1,
  MUTUAL: 2
} as const

export function getRelationText(relationType: number): string {
  const relationMap: Record<number, string> = {
    0: '',
    1: '已关注',
    2: '互相关注'
  }
  return relationMap[relationType] || ''
}

// 消息设置
export function getUserMessageSettings(): Promise<ApiResponse> {
  return request({ url: '/user/settings/message', method: 'get' })
}

export function updateUserMessageSettings(settings: Record<string, unknown>): Promise<ApiResponse> {
  return request({ url: '/user/settings/message', method: 'post', data: settings })
}
