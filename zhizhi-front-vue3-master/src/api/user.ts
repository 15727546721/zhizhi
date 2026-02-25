import request from '@/utils/request'
import type { ApiResponse, User, PageResponse } from '@/types'

interface UserProfile {
  nickname?: string
  avatar?: string
  gender?: number
  phone?: string
  region?: string
  birthday?: string
  description?: string
}

interface UserRankingParams {
  timeRange?: 'week' | 'month'
  sortType?: 'fans' | 'likes' | 'posts' | 'comprehensive'
  limit?: number
  page?: number
  size?: number
}

interface PrivacySettings {
  profileVisibility?: number
  showOnlineStatus?: boolean
}

interface NotificationSettings {
  emailNotification?: boolean
  browserNotification?: boolean
  soundNotification?: boolean
}

export function getUserInfo(userId: number): Promise<ApiResponse<User>> {
  return request({
    url: `/user/info/${userId}`,
    method: 'get'
  })
}

export function updateUser(user: Partial<User>): Promise<ApiResponse> {
  return request({
    url: '/user/update',
    method: 'post',
    data: user
  })
}

export function updateUserProfile(profile: UserProfile): Promise<ApiResponse> {
  return request({
    url: '/user/profile/update',
    method: 'post',
    data: profile
  })
}

export function getUserRanking(params: UserRankingParams): Promise<ApiResponse<PageResponse<User>>> {
  return request({
    url: '/user/ranking',
    method: 'get',
    params: {
      timeRange: params.timeRange || 'week',
      sortType: params.sortType || 'fans',
      limit: params.limit || 10,
      page: params.page || 1,
      size: params.size || 10
    }
  })
}


export function getUserProfile(userId: number): Promise<ApiResponse> {
  return request({
    url: `/user/profile/${userId}`,
    method: 'get'
  })
}

export function uploadAvatar(file: File): Promise<ApiResponse<{ url: string }>> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}

export function changePassword(data: {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}): Promise<ApiResponse> {
  return request({
    url: '/user/change-password',
    method: 'post',
    data
  })
}

export function getUserSettings(): Promise<ApiResponse> {
  return request({
    url: '/user/settings',
    method: 'get'
  })
}

export function updatePrivacySettings(settings: PrivacySettings): Promise<ApiResponse> {
  return request({
    url: '/user/settings/privacy',
    method: 'post',
    data: settings
  })
}

export function updateNotificationSettings(settings: NotificationSettings): Promise<ApiResponse> {
  return request({
    url: '/user/settings/notification',
    method: 'post',
    data: settings
  })
}

export function sendEmailVerify(email: string): Promise<ApiResponse> {
  return request({
    url: '/user/settings/email/send-verify',
    method: 'post',
    data: { email }
  })
}

export function verifyEmail(token: string): Promise<ApiResponse> {
  return request({
    url: '/user/settings/email/verify',
    method: 'post',
    data: { token }
  })
}

export function deleteAccount(password: string): Promise<ApiResponse> {
  return request({
    url: '/user/delete-account',
    method: 'post',
    data: { password }
  })
}

export function searchUsers(keyword: string, limit = 10): Promise<ApiResponse<User[]>> {
  return request({
    url: '/user/search',
    method: 'get',
    params: { keyword, limit }
  })
}
