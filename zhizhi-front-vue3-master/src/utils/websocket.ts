import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { useUserStore } from '@/stores/module/user'
import { ElNotification } from 'element-plus'
import { createLogger } from './logger'

// 创建 WebSocket 专用日志
const logger = createLogger('WebSocket')

type ListenerType = 'notification' | 'unreadCount' | 'privateMessage'
type ListenerCallback<T = unknown> = (data: T) => void

interface NotificationData {
  typeName?: string
  title?: string
  content?: string
}

/**
 * 私信 WebSocket 消息数据
 */
export interface PrivateMessageData {
  type?: string
  messageId?: number
  senderId?: number
  senderName?: string
  senderAvatar?: string
  content?: string
  timestamp?: string
}

interface UnreadCountData {
  count: number
}

/**
 * WebSocket服务
 */
class WebSocketService {
  private client: Client | null = null
  private connected = false
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private listeners: Record<ListenerType, ListenerCallback[]> = {
    notification: [],
    unreadCount: [],
    privateMessage: []
  }

  connect(): void {
    const userStore = useUserStore()

    if (!userStore.isAuthenticated) {
      logger.debug('用户未登录，跳过连接')
      return
    }

    if (this.connected) {
      console.log('[WebSocket] 已连接，跳过重复连接')
      return
    }

    const token = userStore.token
    if (!token) {
      console.log('[WebSocket] 缺少token，跳过连接')
      return
    }

    const wsUrl = '/api/ws'

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: { token },
      debug: (str: string) => {
        if (import.meta.env.DEV) {
          console.log('[WebSocket]', str)
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        this.connected = true
        this.reconnectAttempts = 0
        console.log('[WebSocket] 连接成功')
        this.subscribe()
      },

      onDisconnect: () => {
        this.connected = false
        console.log('[WebSocket] 连接断开')
      },

      onStompError: (frame) => {
        console.error('[WebSocket] STOMP错误:', frame.headers['message'])
        this.connected = false
      },

      onWebSocketError: (event) => {
        console.error('[WebSocket] 连接错误:', event)
        this.connected = false
      }
    })

    this.client.activate()
  }


  private subscribe(): void {
    if (!this.client || !this.connected) return

    this.client.subscribe('/user/queue/notifications', (message: IMessage) => {
      try {
        const notification = JSON.parse(message.body) as NotificationData
        console.log('[WebSocket] 收到通知:', notification)
        this.notifyListeners('notification', notification)
        this.showNotification(notification)
      } catch (e) {
        console.error('[WebSocket] 解析通知失败:', e)
      }
    })

    this.client.subscribe('/user/queue/unread-count', (message: IMessage) => {
      try {
        const data = JSON.parse(message.body) as UnreadCountData
        console.log('[WebSocket] 未读数更新:', data.count)
        this.notifyListeners('unreadCount', data.count)
      } catch (e) {
        console.error('[WebSocket] 解析未读数失败:', e)
      }
    })

    this.client.subscribe('/user/queue/private-message', (message: IMessage) => {
      try {
        const data = JSON.parse(message.body) as PrivateMessageData
        console.log('[WebSocket] 收到私信:', data)
        this.notifyListeners('privateMessage', data)
        if (data.type === 'private_message') {
          this.showPrivateMessage(data)
        }
      } catch (e) {
        console.error('[WebSocket] 解析私信失败:', e)
      }
    })
  }

  private showNotification(notification: NotificationData): void {
    ElNotification({
      title: notification.typeName || '新消息',
      message: notification.title || notification.content,
      type: 'info',
      duration: 5000,
      position: 'top-right'
    })
  }

  private showPrivateMessage(data: PrivateMessageData): void {
    ElNotification({
      title: data.senderName || '新私信',
      message: data.content || '发来一条消息',
      type: 'info',
      duration: 5000,
      position: 'top-right'
    })
  }

  addListener<T>(type: ListenerType, callback: ListenerCallback<T>): void {
    this.listeners[type].push(callback as ListenerCallback)
  }

  removeListener<T>(type: ListenerType, callback: ListenerCallback<T>): void {
    const index = this.listeners[type].indexOf(callback as ListenerCallback)
    if (index > -1) {
      this.listeners[type].splice(index, 1)
    }
  }

  private notifyListeners<T>(type: ListenerType, data: T): void {
    this.listeners[type].forEach((callback) => {
      try {
        callback(data)
      } catch (e) {
        console.error('[WebSocket] 监听器执行错误:', e)
      }
    })
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate()
      this.client = null
      this.connected = false
      console.log('[WebSocket] 已断开连接')
    }
  }

  isConnected(): boolean {
    return this.connected
  }
}

export const wsService = new WebSocketService()

export const connectWebSocket = (): void => wsService.connect()
export const disconnectWebSocket = (): void => wsService.disconnect()
export const isWebSocketConnected = (): boolean => wsService.isConnected()

export const onNotification = (callback: ListenerCallback<NotificationData>): void =>
  wsService.addListener('notification', callback)
export const offNotification = (callback: ListenerCallback<NotificationData>): void =>
  wsService.removeListener('notification', callback)

export const onUnreadCount = (callback: ListenerCallback<number>): void =>
  wsService.addListener('unreadCount', callback)
export const offUnreadCount = (callback: ListenerCallback<number>): void =>
  wsService.removeListener('unreadCount', callback)

export const onPrivateMessage = (callback: ListenerCallback<PrivateMessageData>): void =>
  wsService.addListener('privateMessage', callback)
export const offPrivateMessage = (callback: ListenerCallback<PrivateMessageData>): void =>
  wsService.removeListener('privateMessage', callback)
