import request from '@/utils/request'
import { LikeType } from '@/api/like'
import type { ApiResponse, Comment, PageResponse } from '@/types'

export const CommentType = {
  POST: 1,
  ESSAY: 2,
  COMMENT: 3
} as const

interface CommentCreateData {
  type?: number
  targetId: number
  content: string
  parentId?: number | null
  replyUserId?: number | null
  imageUrls?: string[]
  mentionUserIds?: number[]
}

interface CommentListParams {
  targetType: number
  targetId: number
  pageNo?: number
  pageSize?: number
  sortType?: 'HOT' | 'TIME' | 'NEW'
}

export function getCommentPage(data: CommentListParams): Promise<ApiResponse<PageResponse<Comment>>> {
  return request({ url: '/comment/list', method: 'post', data })
}

export function getReplyPage(data: {
  parentId: number
  pageNo?: number
  pageSize?: number
}): Promise<ApiResponse<PageResponse<Comment>>> {
  return request({ url: '/comment/reply/list', method: 'post', data })
}

export function createComment(data: CommentCreateData): Promise<ApiResponse<Comment>> {
  return request({ url: '/comment/add', method: 'post', data })
}

export function replyComment(data: CommentCreateData): Promise<ApiResponse<Comment>> {
  return request({ url: '/comment/reply', method: 'post', data })
}

export function deleteComment(commentId: number): Promise<ApiResponse> {
  return request({ url: `/comment/delete/${commentId}`, method: 'post' })
}

export function reportComment(
  targetId: number,
  commentId: number,
  reason: string,
  type = CommentType.POST
): Promise<ApiResponse> {
  return request({
    url: '/comment/report',
    method: 'post',
    data: { targetId, commentId, reason, type }
  })
}

export function likeComment(commentId: number): Promise<ApiResponse> {
  return request({
    url: '/likes/like',
    method: 'post',
    data: { targetId: commentId, type: LikeType.COMMENT }
  })
}

export function unlikeComment(commentId: number): Promise<ApiResponse> {
  return request({
    url: '/likes/unlike',
    method: 'post',
    data: { targetId: commentId, type: LikeType.COMMENT }
  })
}

export function getUserComments(
  userId: number,
  params: { page?: number; size?: number } = {}
): Promise<ApiResponse<PageResponse<Comment>>> {
  return request({
    url: `/comment/user/${userId}`,
    method: 'get',
    params: { page: params.page || 1, size: params.size || 10 }
  })
}

export function getConversationChain(replyId: number): Promise<ApiResponse<Comment[]>> {
  return request({ url: `/comment/conversation/${replyId}`, method: 'get' })
}
