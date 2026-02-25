import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/module/user'
import router from '@/router'
import type { ApiResponse, ApiError } from '@/types'

// 扩展 Window 接口
declare global {
  interface Window {
    globalAbortController?: AbortController
  }
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000, // 请求超时时间：30秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 标记是否正在显示登录弹窗
let isLoginDialogShowing = false

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    let token: string | null = null
    try {
      const userStore = JSON.parse(localStorage.getItem('user-store') || '{}')
      token = userStore.token
    } catch {
      // 忽略解析错误
    }
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    if (config.data instanceof FormData && config.headers['Content-Type']) {
      delete config.headers['Content-Type']
    }

    if (!config.signal && window.globalAbortController) {
      config.signal = window.globalAbortController.signal
    }

    return config
  },
  (error) => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)


// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse

    if (res.code !== 20000) {
      // token 失效处理
      if ([30001, 30002, 30003, 30004, 30005].includes(res.code)) {
        if (!isLoginDialogShowing) {
          isLoginDialogShowing = true

          const userStore = useUserStore()
          userStore.clearUserState()

          if (router.currentRoute.value.path !== '/') {
            router.push('/')
          }

          window.dispatchEvent(new CustomEvent('show-login-dialog'))

          ElMessage({
            message: '登录已过期，请重新登录',
            type: 'warning',
            duration: 2000,
            onClose: () => {
              isLoginDialogShowing = false
            }
          })
        }
        return Promise.reject({ silent: true, message: '登录已过期' } as ApiError)
      }

      // 其他业务错误
      if (response.config.url !== '/oauth/wechat/is_login') {
        ElMessage.error(res.info || '请求失败')
      }
      return Promise.reject({ silent: true, message: res.info || 'Error' } as ApiError)
    }
    return res
  },
  (error) => {
    // 取消请求不处理
    if (error.name === 'AbortError' || error.code === 'ERR_CANCELED' || axios.isCancel(error)) {
      return Promise.reject({ silent: true } as ApiError)
    }

    // 网络/服务器错误
    if (error.response) {
      const responseData = error.response.data
      const errorMessage = responseData?.info || responseData?.message || `服务器错误 (${error.response.status})`
      console.error('Response Error:', error.response.status, responseData)
      if (!error.config?.url?.includes('/upload')) {
        ElMessage.error(errorMessage)
      }
    } else if (error.request) {
      console.error('No Response:', error.request)
      ElMessage.error('服务器无响应，请检查网络连接')
    } else {
      console.error('Request Error:', error.message)
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject({ silent: true, message: error.message } as ApiError)
  }
)

/**
 * 泛型请求函数
 */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.request(config)
}

export default service
