/**
 * 点赞类型枚举
 */
export enum LikeType {
  POST = 'POST',
  COMMENT = 'COMMENT'
}

/**
 * 点赞请求
 */
export interface LikeRequest {
  targetId: number
  type: LikeType
}

/**
 * 点赞状态响应
 */
export interface LikeStatus {
  isLiked: boolean
  likeCount: number
}
