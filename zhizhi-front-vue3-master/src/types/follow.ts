import type { UserBrief } from './user'

/**
 * 关注状态
 */
export type FollowStatus = 'FOLLOWING' | 'NOT_FOLLOWING' | 'MUTUAL'

/**
 * 关注信息
 */
export interface FollowInfo {
  userId: number
  targetUserId: number
  status: FollowStatus
  createdAt: string
}

/**
 * 关注列表项
 */
export interface FollowListItem {
  user: UserBrief
  followStatus: FollowStatus
  createdAt: string
}

/**
 * 关注列表查询参数
 */
export interface FollowListParams {
  userId: number
  pageNo?: number
  pageSize?: number
}
