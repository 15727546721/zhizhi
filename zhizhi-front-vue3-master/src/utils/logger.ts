/**
 * 统一日志管理工具
 * 
 * 特性：
 * - 开发环境显示所有日志
 * - 生产环境只显示 warn 和 error
 * - 支持日志分类和格式化
 */

const isDev = import.meta.env.DEV
const isDebugEnabled = import.meta.env.VITE_ENABLE_DEBUG_LOG === 'true'

// 日志级别
enum LogLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR'
}

// 日志颜色
const colors = {
  [LogLevel.DEBUG]: '#9E9E9E',
  [LogLevel.INFO]: '#2196F3',
  [LogLevel.WARN]: '#FF9800',
  [LogLevel.ERROR]: '#F44336'
}

/**
 * 格式化日志输出
 */
function formatLog(level: LogLevel, category: string, ...args: any[]) {
  const timestamp = new Date().toLocaleTimeString()
  const prefix = `[${timestamp}] [${level}] [${category}]`
  
  if (isDev) {
    console.log(
      `%c${prefix}`,
      `color: ${colors[level]}; font-weight: bold;`,
      ...args
    )
  } else {
    console.log(prefix, ...args)
  }
}

/**
 * 日志工具类
 */
export class Logger {
  private category: string

  constructor(category: string) {
    this.category = category
  }

  /**
   * 调试日志（仅开发环境）
   */
  debug(...args: any[]) {
    if (isDev || isDebugEnabled) {
      formatLog(LogLevel.DEBUG, this.category, ...args)
    }
  }

  /**
   * 信息日志（仅开发环境）
   */
  info(...args: any[]) {
    if (isDev) {
      formatLog(LogLevel.INFO, this.category, ...args)
    }
  }

  /**
   * 警告日志（所有环境）
   */
  warn(...args: any[]) {
    formatLog(LogLevel.WARN, this.category, ...args)
  }

  /**
   * 错误日志（所有环境）
   */
  error(...args: any[]) {
    formatLog(LogLevel.ERROR, this.category, ...args)
  }
}

/**
 * 创建日志实例
 * 
 * @example
 * const logger = createLogger('WebSocket')
 * logger.debug('连接成功')
 * logger.error('连接失败', error)
 */
export function createLogger(category: string): Logger {
  return new Logger(category)
}

/**
 * 默认日志实例
 */
export const logger = {
  debug: (...args: any[]) => {
    if (isDev || isDebugEnabled) {
      console.log('[DEBUG]', ...args)
    }
  },
  info: (...args: any[]) => {
    if (isDev) {
      console.log('[INFO]', ...args)
    }
  },
  warn: (...args: any[]) => {
    console.warn('[WARN]', ...args)
  },
  error: (...args: any[]) => {
    console.error('[ERROR]', ...args)
  }
}
