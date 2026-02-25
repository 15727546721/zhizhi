/**
 * 工具函数统一导出
 */

// 请求工具
export { default as request } from './request'

// 头像工具
export * from './avatar'

// 缓存工具
export * from './cache'

// 事件总线
export * from './eventBus'

// 收藏工具
export * from './favoriteUtils'

// 时间工具
export * from './time'

// 验证工具
export * from './validation'

// WebSocket
export * from './websocket'

/**
 * 浏览器版本检测
 */
interface BrowserInfo {
  browser: string
  version: string
}

export const browserMatch = (): BrowserInfo => {
  const userAgent = navigator.userAgent
  const rMsie = /(msie\s|trident.*rv:)([\w.]+)/
  const rEdge = /(edg)\/([\w.]+)/
  const rFirefox = /(firefox)\/([\w.]+)/
  const rOpera = /(opera).+version\/([\w.]+)/
  const rChrome = /(chrome)\/([\w.]+)/
  const rSafari = /version\/([\w.]+).*(safari)/
  const ua = userAgent.toLowerCase()

  let match = rMsie.exec(ua)
  if (match !== null) {
    return { browser: 'IE', version: match[2] || '0' }
  }

  match = rEdge.exec(ua)
  if (match !== null) {
    return { browser: 'Edge', version: match[2] || '0' }
  }

  match = rFirefox.exec(ua)
  if (match !== null) {
    return { browser: match[1] || '', version: match[2] || '0' }
  }

  match = rOpera.exec(ua)
  if (match !== null) {
    return { browser: match[1] || '', version: match[2] || '0' }
  }

  match = rChrome.exec(ua)
  if (match !== null) {
    return { browser: match[1] || '', version: match[2] || '0' }
  }

  match = rSafari.exec(ua)
  if (match !== null) {
    return { browser: match[2] || '', version: match[1] || '0' }
  }

  return { browser: '', version: '0' }
}
