import type { UserBrief } from './user'
import type { Tag } from './tag'

/**
 * 帖子状态枚举
 */
export enum PostStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  DELETED = 'DELETED'
}

/**
 * 排序类型枚举
 */
export enum SortType {
  LATEST = 'latest',
  HOTTEST = 'hottest',
  COMMENTS = 'comments',
  LIKES = 'likes',
  FAVORITES = 'favorites'
}

/**
 * 帖子信息
 */
export interface Post {
  id: number
  title: string
  content: string
  description: string
  coverUrl: string
  authorId: number
  author: UserBrief
  status: PostStatus
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  shareCount: number
  tags: Tag[]
  isLiked: boolean
  isFavorited: boolean
  createdAt: string
  updatedAt: string
}

/**
 * 帖子简要信息（用于列表）
 */
export interface PostBrief {
  id: number
  title: string
  description: string
  coverUrl: string
  author: UserBrief
  viewCount: number
  likeCount: number
  commentCount: number
  tags: Tag[]
  createdAt: string
}

/**
 * 帖子创建请求
 */
export interface PostCreateRequest {
  title: string
  content: string
  description?: string
  coverUrl?: string
  tagIds?: number[]
  status?: PostStatus
}

/**
 * 帖子更新请求
 */
export interface PostUpdateRequest extends PostCreateRequest {
  id: number
}

/**
 * 帖子列表查询参数
 */
export interface PostListParams {
  pageNo?: number
  pageSize?: number
  type?: string
  tagId?: number
  sortBy?: SortType
  keyword?: string
  status?: PostStatus
}

/**
 * 游标分页查询参数
 */
export interface PostCursorParams {
  cursor?: string | null
  pageSize?: number
  sortBy?: string
  tagId?: number | null
}

/**
 * 搜索参数
 */
export interface PostSearchParams {
  keyword: string
  pageNo?: number
  pageSize?: number
  types?: string[]
  timeRange?: string
  sortOption?: string
}
