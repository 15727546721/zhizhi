import type { UserBrief } from './user'

/**
 * 评论信息
 */
export interface Comment {
  id: number
  content: string
  postId: number
  authorId: number
  author: UserBrief
  parentId: number | null
  rootId: number | null
  replyTo: UserBrief | null
  likeCount: number
  replyCount: number
  isLiked: boolean
  children: Comment[]
  createdAt: string
  updatedAt: string
}

/**
 * 评论创建请求
 */
export interface CommentCreateRequest {
  postId: number
  content: string
  parentId?: number | null
  rootId?: number | null
  replyToUserId?: number | null
}

/**
 * 评论列表查询参数
 */
export interface CommentListParams {
  postId: number
  pageNo?: number
  pageSize?: number
  sortBy?: 'latest' | 'hottest'
}

/**
 * 子评论查询参数
 */
export interface ReplyListParams {
  rootId: number
  pageNo?: number
  pageSize?: number
}
