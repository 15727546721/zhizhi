/**
 * 专栏相关类型定义
 */

// 专栏状态枚举
export enum ColumnStatus {
  DRAFT = 0, // 草稿
  PUBLISHED = 1, // 已发布
  ARCHIVED = 2, // 已归档
}

// 专栏基础信息
export interface Column {
  id: number
  userId: number
  name: string
  description?: string
  coverUrl?: string
  status: ColumnStatus
  postCount: number
  subscribeCount: number
  isRecommended: number
  sort: number
  lastPostTime?: string
  createTime: string
  updateTime: string
}

// 专栏VO(视图对象)
export interface ColumnVO {
  id: number
  userId: number
  userName: string
  userAvatar: string
  name: string
  description?: string
  coverUrl?: string
  status: ColumnStatus
  postCount: number
  subscribeCount: number
  isSubscribed: boolean
  lastPostTime?: string
  createTime: string
}

// 专栏详情VO
export interface ColumnDetailVO extends ColumnVO {
  recentPosts: ColumnPostVO[]
  isOwner: boolean
}

// 专栏文章VO
export interface ColumnPostVO {
  postId: number
  title: string
  description?: string
  coverUrl?: string
  viewCount: number
  likeCount: number
  commentCount: number
  sort: number
  createTime: string
  // 导航信息
  previousPostId?: number
  previousPostTitle?: string
  nextPostId?: number
  nextPostTitle?: string
}

// 专栏订阅信息
export interface ColumnSubscription {
  id: number
  userId: number
  columnId: number
  status: number
  createTime: string
  updateTime: string
}

// 创建专栏DTO
export interface ColumnCreateDTO {
  name: string
  description?: string
  coverUrl?: string
  status: ColumnStatus
}

// 更新专栏DTO
export interface ColumnUpdateDTO {
  name?: string
  description?: string
  coverUrl?: string
  status?: ColumnStatus
}

// 文章排序DTO
export interface PostSortDTO {
  postId: number
  sort: number
}

// 批量排序请求
export interface BatchSortRequest {
  sorts: PostSortDTO[]
}

// 专栏广场查询参数
export interface ColumnSquareQuery {
  sortType?: 'latest' | 'hot' | 'subscribe'
  page?: number
  size?: number
}

// 专栏搜索查询参数
export interface ColumnSearchQuery {
  keyword: string
  page?: number
  size?: number
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  hasMore: boolean
}
