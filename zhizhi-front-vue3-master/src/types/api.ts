/**
 * API 响应相关类型定义
 */

/**
 * API 响应基础结构
 */
export interface ApiResponse<T = unknown> {
  code: number
  info: string | null
  message?: string
  data: T
}

/**
 * API 错误
 */
export interface ApiError {
  silent?: boolean
  message: string
  code?: number
}

/**
 * 分页响应数据
 */
export interface PageResponse<T> {
  pageNo: number
  pageSize: number
  total: number
  data: T
}

/**
 * 列表响应数据（旧版本兼容）
 */
export interface ListResponse<T> {
  list: T
  total: number
  page?: number
  size?: number
}

/**
 * 上传响应
 */
export interface UploadResponse {
  code: number
  data: string[]
  info?: string
}

/**
 * 表单验证规则
 */
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  validator?: (rule: FormRule, value: unknown, callback: (error?: Error) => void) => void
  min?: number
  max?: number
  type?: string
  pattern?: RegExp
}

/**
 * Element Plus 表单实例
 */
export interface FormInstance {
  validate: () => Promise<boolean>
  validateField: (prop: string) => Promise<boolean>
  resetFields: () => void
  clearValidate: (props?: string | string[]) => void
}

/**
 * 收藏夹项目
 */
export interface FavoriteItem {
  id: number
  postItem?: {
    id: number
    title: string
    description: string
    coverUrl: string
    viewCount: number
    likeCount: number
    commentCount: number
    createTime: string
  }
  // 兼容旧数据结构
  title?: string
  description?: string
  coverUrl?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  createTime?: string
}

/**
 * 收藏夹响应
 */
export interface FavoriteResponse {
  list: FavoriteItem[]
  total: number
}

/**
 * 对话链响应项
 */
export interface ConversationChainItem {
  id: number
  content: string
  userId: number
  userName: string
  userAvatar: string
  createTime: string
  formattedTime?: string
}
