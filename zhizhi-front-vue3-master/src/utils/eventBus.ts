/**
 * 简单的事件总线
 */

type EventCallback<T = unknown> = (data: T) => void

const events: Record<string, EventCallback[]> = {}

export const eventBus = {
  /**
   * 监听事件
   */
  on<T = unknown>(event: string, callback: EventCallback<T>): void {
    if (!events[event]) {
      events[event] = []
    }
    events[event].push(callback as EventCallback)
  },

  /**
   * 移除监听
   */
  off<T = unknown>(event: string, callback?: EventCallback<T>): void {
    if (!events[event]) return
    if (callback) {
      events[event] = events[event].filter((cb) => cb !== callback)
    } else {
      delete events[event]
    }
  },

  /**
   * 触发事件
   */
  emit<T = unknown>(event: string, data?: T): void {
    if (!events[event]) return
    events[event].forEach((callback) => callback(data))
  }
}

// 事件名称常量
export const EVENT_REFRESH_UNREAD_COUNT = 'refresh-unread-count'
export const EVENT_REFRESH_NOTIFICATION_COUNT = 'refresh-notification-count'
export const EVENT_REFRESH_MESSAGE_COUNT = 'refresh-message-count'
