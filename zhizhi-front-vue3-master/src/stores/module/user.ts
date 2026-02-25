import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, loginWithCode } from '@/api/auth'
import { getUserInfo as fetchUserInfoApi } from '@/api/user'
import type { User, LoginCredentials, CodeLoginCredentials, LoginResult } from '@/types/user'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value && !!userInfo.value)
  const getUserAuth = computed(() => isAuthenticated.value)
  const getUserInfo = computed(() => userInfo.value)
  const userId = computed(() => userInfo.value?.id || null)

  /**
   * 登录
   */
  const loginAction = async (credentials: LoginCredentials): Promise<LoginResult> => {
    try {
      const response = await login(credentials)
      if (response.code === 20000) {
        const data = response.data
        token.value = data.token
        userInfo.value = data.user || data.userInfo || data
        return { success: true }
      }

      return {
        success: false,
        error: response.info || '登录失败',
        remainingAttempts: response.data?.remainingAttempts
      }
    } catch (error: any) {
      const errorMsg = error.response?.data?.info || error.message || '登录失败'
      const remaining = error.response?.data?.data?.remainingAttempts
      return {
        success: false,
        error: errorMsg,
        remainingAttempts: remaining
      }
    }
  }

  /**
   * 验证码登录
   */
  const loginWithCodeAction = async (credentials: CodeLoginCredentials): Promise<LoginResult> => {
    try {
      const response = await loginWithCode(credentials)
      if (response.code === 20000) {
        const data = response.data
        token.value = data.token
        userInfo.value = data.user || data.userInfo || data
        return { success: true }
      }

      return {
        success: false,
        error: response.info || '登录失败'
      }
    } catch (error: any) {
      const errorMsg = error.response?.data?.info || error.message || '登录失败'
      return {
        success: false,
        error: errorMsg
      }
    }
  }

  /**
   * 退出登录
   */
  const logoutAction = async (): Promise<boolean> => {
    try {
      clearUserState()
      return true
    } catch (error) {
      return false
    }
  }

  /**
   * 清除用户状态
   */
  const clearUserState = () => {
    token.value = ''
    userInfo.value = null
  }

  /**
   * 更新用户信息
   */
  const updateUserInfo = (data: Partial<User>) => {
    if (userInfo.value && data) {
      userInfo.value = { ...userInfo.value, ...data }
    }
  }

  /**
   * 获取最新用户信息
   */
  const fetchUserInfo = async (): Promise<User | null> => {
    if (!userInfo.value?.id) return null

    try {
      const response = await fetchUserInfoApi(userInfo.value.id)
      if (response.code === 20000) {
        userInfo.value = response.data
        return userInfo.value
      }
    } catch (error) {
      // 静默处理错误
    }
    return null
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    getUserAuth,
    getUserInfo,
    userId,
    loginAction,
    loginWithCodeAction,
    logoutAction,
    clearUserState,
    updateUserInfo,
    fetchUserInfo
  }
}, {
  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: ['token', 'userInfo']
  }
})
