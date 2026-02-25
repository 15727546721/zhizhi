/**
 * 用户信息
 */
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email?: string
  phone?: string
  bio?: string
  gender?: number
  birthday?: string
  location?: string
  website?: string
  followersCount: number
  followingCount: number
  postsCount: number
  likesCount: number
  createdAt: string
  updatedAt: string
}

/**
 * 用户简要信息（用于列表展示）
 */
export interface UserBrief {
  id: number
  username: string
  nickname: string
  avatar: string
  bio?: string
}

/**
 * 登录凭证
 */
export interface LoginCredentials {
  email: string
  password: string
}

/**
 * 验证码登录凭证
 */
export interface CodeLoginCredentials {
  email: string
  verifyCode: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  user: User
  userInfo?: User
}

/**
 * 登录结果
 */
export interface LoginResult {
  success: boolean
  error?: string
  remainingAttempts?: number
}

/**
 * 用户更新请求
 */
export interface UserUpdateRequest {
  nickname?: string
  avatar?: string
  bio?: string
  gender?: number
  birthday?: string
  location?: string
  website?: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
