/**
 * 用户主页相关类型定义
 */

/**
 * 用户基本信息
 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  email?: string
  phone?: string
  gender?: number
  birthday?: string
  description?: string
  region?: string
  userType?: number
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户统计数据
 */
export interface UserStats {
  postCount: number
  followCount: number
  fansCount: number
  likeCount: number
  commentCount?: number
  collectionCount?: number
}

/**
 * 用户主页数据响应
 */
export interface UserProfileResponse {
  basicInfo: UserInfo
  stats: UserStats
  isFollowing: boolean
  isFollowedBy: boolean
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 用户资料更新请求
 */
export interface UserProfileUpdateRequest {
  nickname?: string
  avatar?: string
  gender?: number
  phone?: string
  region?: string
  birthday?: string
  description?: string
}
