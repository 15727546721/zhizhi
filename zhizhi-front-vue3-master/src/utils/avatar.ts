/**
 * 头像工具函数
 */

export interface UIAvatarOptions {
  name?: string
  size?: number
  background?: string
  color?: string
  bold?: boolean
  length?: number
  rounded?: boolean
  format?: string
}

/**
 * 默认头像URL
 */
export const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

/**
 * 检查头像URL是否有效
 */
export function isValidAvatarUrl(url: string | null | undefined): boolean {
  if (!url || typeof url !== 'string') return false
  const trimmed = url.trim()
  if (!trimmed) return false
  return trimmed.startsWith('http://') ||
    trimmed.startsWith('https://') ||
    trimmed.startsWith('data:image/') ||
    trimmed.startsWith('/')
}

/**
 * 获取头像URL，带fallback
 */
export function getAvatarUrl(
  avatarUrl: string | null | undefined,
  username = '',
  fallbackType: 'default' | 'dicebear' | 'boring' | 'ui-avatars' = 'dicebear'
): string {
  if (isValidAvatarUrl(avatarUrl)) {
    return avatarUrl!
  }

  switch (fallbackType) {
    case 'dicebear':
      return generateDiceBearAvatar(username)
    case 'boring':
      return generateBoringAvatar(username)
    case 'ui-avatars':
      return generateUIAvatar(username)
    case 'default':
    default:
      return DEFAULT_AVATAR
  }
}

/**
 * 生成 DiceBear 头像
 */
export function generateDiceBearAvatar(seed: string, style = 'initials'): string {
  const validSeed = seed || 'guest'
  const params = new URLSearchParams({ seed: validSeed })
  return `https://api.dicebear.com/7.x/${style}/svg?${params.toString()}`
}

/**
 * 生成 BoringAvatars 几何头像
 */
export function generateBoringAvatar(name: string, variant = 'beam'): string {
  const validName = encodeURIComponent(name || 'guest')
  const size = 80
  const colors = '264653,2a9d8f,e9c46a,f4a261,e76f51'
  return `https://source.boringavatars.com/${variant}/${size}/${validName}?colors=${colors}`
}


/**
 * 生成 UI Avatars 字母头像
 */
export function generateUIAvatar(name: string, options: UIAvatarOptions = {}): string {
  const defaults: UIAvatarOptions = {
    name: name || 'Guest',
    size: 80,
    background: '667eea',
    color: 'fff',
    bold: true,
    length: 1,
    rounded: true,
    format: 'svg'
  }

  const config = { ...defaults, ...options }
  const params = new URLSearchParams()
  Object.entries(config).forEach(([key, value]) => {
    params.set(key, String(value))
  })

  return `https://ui-avatars.com/api/?${params.toString()}`
}

/**
 * 获取用户名首字母
 */
export function getInitials(nickname?: string, username?: string): string {
  const name = nickname || username || '未知'

  if (/[\u4e00-\u9fa5]/.test(name)) {
    return name.charAt(0)
  }

  const words = name.trim().split(/\s+/)
  if (words.length >= 2) {
    return (words[0].charAt(0) + words[1].charAt(0)).toUpperCase()
  }

  return name.charAt(0).toUpperCase()
}

/**
 * 预加载头像
 */
export function preloadAvatar(url: string): Promise<boolean> {
  return new Promise((resolve) => {
    if (!url) {
      resolve(false)
      return
    }

    const img = new Image()
    img.onload = () => resolve(true)
    img.onerror = () => resolve(false)
    img.src = url
  })
}

/**
 * 获取头像背景色
 */
export function getAvatarColor(username: string): [string, string] {
  const colorPairs: [string, string][] = [
    ['#667eea', '#764ba2'],
    ['#f093fb', '#f5576c'],
    ['#4facfe', '#00f2fe'],
    ['#43e97b', '#38f9d7'],
    ['#fa709a', '#fee140'],
    ['#30cfd0', '#330867'],
    ['#a8edea', '#fed6e3'],
    ['#ff9a9e', '#fecfef']
  ]

  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash)
  }

  const index = Math.abs(hash) % colorPairs.length
  return colorPairs[index]
}

export default {
  DEFAULT_AVATAR,
  isValidAvatarUrl,
  getAvatarUrl,
  generateDiceBearAvatar,
  generateBoringAvatar,
  generateUIAvatar,
  getInitials,
  preloadAvatar,
  getAvatarColor
}
