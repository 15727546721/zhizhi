import request from '@/utils/request'
import type { ApiResponse, PageResponse, Notification } from '@/types'

interface NotificationParams {
  type?: string | number
  pageNo?: number
  pageSize?: number
}

// 通知查询
export function getNotifications(params: NotificationParams): Promise<ApiResponse<PageResponse<Notification>>> {
  return request({ url: '/notifications', method: 'get', params })
}

export function getUnreadNotificationCount(type?: number): Promise<ApiResponse<number>> {
  return request({
    url: '/notifications/unread/count',
    method: 'get',
    params: type !== undefined ? { type } : {}
  })
}

export function getUnreadCountByType(): Promise<ApiResponse<Record<number, number>>> {
  return request({ url: '/notifications/unread/count-by-type', method: 'get' })
}

// 已读操作
export function markNotificationAsRead(notificationId: number): Promise<ApiResponse> {
  return request({ url: `/notifications/${notificationId}/read`, method: 'post' })
}

export function markAllNotificationsAsRead(type?: number): Promise<ApiResponse> {
  return request({
    url: '/notifications/read-all',
    method: 'post',
    params: type !== undefined ? { type } : {}
  })
}

// 删除操作
export function deleteNotification(notificationId: number): Promise<ApiResponse> {
  return request({ url: `/notifications/${notificationId}/delete`, method: 'post' })
}

export function deleteNotifications(notificationIds: number[]): Promise<ApiResponse> {
  return request({ url: '/notifications/batch/delete', method: 'post', data: { ids: notificationIds } })
}

// 通知类型常量
export const NotificationType = {
  SYSTEM: 0,
  LIKE: 1,
  FAVORITE: 2,
  COMMENT: 3,
  REPLY: 4,
  FOLLOW: 5,
  MENTION: 6
} as const

export const NotificationTypeName: Record<number, string> = {
  [NotificationType.SYSTEM]: '系统通知',
  [NotificationType.LIKE]: '点赞',
  [NotificationType.FAVORITE]: '收藏',
  [NotificationType.COMMENT]: '评论',
  [NotificationType.REPLY]: '回复',
  [NotificationType.FOLLOW]: '关注',
  [NotificationType.MENTION]: '@提及'
}

// 便捷方法
export function getReplyNotifications(params: NotificationParams): Promise<ApiResponse<PageResponse<Notification>>> {
  return getNotifications({ ...params, type: `${NotificationType.COMMENT},${NotificationType.REPLY}` })
}

export function getMentionNotifications(params: NotificationParams): Promise<ApiResponse<PageResponse<Notification>>> {
  return getNotifications({ ...params, type: NotificationType.MENTION })
}

export function getLikeNotifications(params: NotificationParams): Promise<ApiResponse<PageResponse<Notification>>> {
  return getNotifications({ ...params, type: `${NotificationType.LIKE},${NotificationType.FAVORITE}` })
}

export function getFollowNotifications(params: NotificationParams): Promise<ApiResponse<PageResponse<Notification>>> {
  return getNotifications({ ...params, type: NotificationType.FOLLOW })
}
