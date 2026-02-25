/**
 * 时间格式化工具
 */

/**
 * 格式化为相对时间
 */
export function formatRelativeTime(time: string | Date | null | undefined): string {
  if (!time) return ''

  const date = typeof time === 'string' ? new Date(time) : time
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else if (days < 30) {
    return `${Math.floor(days / 7)}周前`
  } else if (days < 365) {
    return `${Math.floor(days / 30)}个月前`
  } else {
    return `${Math.floor(days / 365)}年前`
  }
}

/**
 * 格式化为标准日期时间
 */
export function formatDateTime(
  time: string | Date | null | undefined,
  format = 'YYYY-MM-DD HH:mm'
): string {
  if (!time) return ''

  const date = typeof time === 'string' ? new Date(time) : time

  const year = String(date.getFullYear())
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化时间为友好显示（别名）
 */
export const formatTime = formatRelativeTime
