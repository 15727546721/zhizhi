/**
 * API 错误类型
 */
export interface ApiError {
  silent?: boolean
  message: string
  code?: number
  response?: {
    status: number
    data: {
      code: number
      info: string
      data?: unknown
    }
  }
}

/**
 * 类型守卫：判断是否为 ApiError
 */
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error
  )
}

/**
 * 类型守卫：判断是否为静默错误
 */
export function isSilentError(error: unknown): boolean {
  return isApiError(error) && error.silent === true
}
