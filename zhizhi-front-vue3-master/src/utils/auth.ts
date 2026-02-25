import { useUserStore } from '@/stores/module/user'
import { ElMessage } from 'element-plus'

/**
 * 检查用户是否已登录
 * 未登录时显示登录弹窗并提示用户
 * 
 * @param message - 自定义提示消息，默认为 "请先登录"
 * @returns 是否已登录
 * 
 * @example
 * ```ts
 * const handleLike = () => {
 *   if (!requireAuth('登录后才能点赞哦')) return
 *   // 继续点赞逻辑
 * }
 * ```
 */
export function requireAuth(message = '请先登录'): boolean {
  const userStore = useUserStore()
  
  if (!userStore.isAuthenticated) {
    ElMessage.warning(message)
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return false
  }
  
  return true
}

/**
 * 静默检查用户是否已登录
 * 不显示任何提示
 * 
 * @returns 是否已登录
 * 
 * @example
 * ```ts
 * const showEditButton = computed(() => {
 *   return isAuthenticated() && isOwner.value
 * })
 * ```
 */
export function isAuthenticated(): boolean {
  const userStore = useUserStore()
  return userStore.isAuthenticated
}

/**
 * 获取当前登录用户ID
 * 
 * @returns 用户ID，未登录返回 null
 */
export function getCurrentUserId(): number | null {
  const userStore = useUserStore()
  return userStore.userId
}

/**
 * 显示登录弹窗
 * 不显示任何提示消息
 */
export function showLoginDialog(): void {
  window.dispatchEvent(new CustomEvent('show-login-dialog'))
}
