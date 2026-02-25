import type { UserBrief } from './user'

/**
 * 通知类型
 */
export enum NotificationType {
  LIKE = 'LIKE',
  COMMENT = 'COMMENT',
  FOLLOW = 'FOLLOW',
  SYSTEM = 'SYSTEM',
  REPLY = 'REPLY',
  MENTION = 'MENTION'
}

/**
 * 通知信息
 */
export interface Notification {
  id: number
  type: NotificationType
  content: string
  sender: UserBrief | null
  targetId: number | null
  targetType: string | null
  isRead: boolean
  createdAt: string
}

/**
 * 私信消息状态
 */
export enum MessageStatus {
  DELIVERED = 1,
  PENDING = 2,
  BLOCKED = 3,
  WITHDRAWN = 4
}

/**
 * 私信信息
 */
export interface PrivateMessage {
  messageId: number
  senderId: number
  receiverId: number
  senderNickname?: string
  senderAvatar?: string
  receiverNickname?: string
  receiverAvatar?: string
  content: string
  status: number
  createTime: string
  updateTime?: string
  // 兼容旧字段
  id?: number
  sender?: UserBrief
  receiver?: UserBrief
  isRead?: boolean
  createdAt?: string
}

/**
 * 会话信息
 */
export interface Conversation {
  conversationId: number
  recipientId: number
  recipientName: string
  recipientAvatar: string
  userId: number
  userName: string
  userAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
  /** 关系类型: 0-陌生人 1-单向关注 2-互关 */
  relationType?: number
  /** 是否是消息请求（陌生人发来的首条消息） */
  isMessageRequest?: boolean
  // 兼容旧字段
  id?: number
  targetUser?: UserBrief
  updatedAt?: string
}

/**
 * 私信权限
 */
export interface DMPermission {
  canSend: boolean
  isGreeting: boolean
  reason?: string
}

/**
 * 发送私信请求
 */
export interface SendMessageRequest {
  receiverId: number
  content: string
}

/**
 * 消息列表查询参数
 */
export interface MessageListParams {
  conversationId?: number
  targetUserId?: number
  page?: number
  size?: number
  pageNo?: number
  pageSize?: number
}

/**
 * WebSocket 私信消息
 */
export interface WebSocketPrivateMessage {
  type: string
  messageId: number
  senderId: number
  senderName: string
  senderAvatar: string
  content: string
  timestamp?: string
}
