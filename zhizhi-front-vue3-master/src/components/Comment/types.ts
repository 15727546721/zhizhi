/**
 * 评论组件类型定义
 */

// 评论用户信息
export interface CommentUser {
  id?: number
  nickname?: string
  username?: string
  avatar?: string
}

// 评论图片
export interface CommentImage {
  url: string
  name?: string
}

// @提及的用户
export interface MentionUser {
  userId: number
  nickname?: string
  username?: string
}

// 评论/回复项
export interface CommentItem {
  id: number
  userId: number
  targetType?: number
  targetId?: number
  parentId?: number
  content: string
  createTime: string
  updateTime?: string
  formattedTime?: string
  likeCount: number
  replyCount?: number
  nickname?: string
  avatar?: string
  username?: string
  user?: CommentUser
  replyUser?: CommentUser
  replyUserId?: number
  imageUrls: string[]
  isLiked: boolean
  isAuthorLiked: boolean
  isHot?: boolean
  hotScore?: number
  isAuthor?: boolean
  children?: CommentItem[]
  likeLoading?: boolean
}

// 回复目标
export interface ReplyingTo {
  comment: CommentItem
  parentComment?: CommentItem | null
  parentId: number
}

// 排序选项
export interface SortOption {
  label: string
  value: string
}

// 对话链项
export interface ConversationItem {
  id: number
  userId: number
  content: string
  createTime: string
  formattedTime?: string
  nickname?: string
  avatar?: string
  imageUrls?: string[]
}

// Emoji 项
export interface EmojiItem {
  i: string
  [key: string]: any
}

// 当前用户信息
export interface CurrentUserInfo {
  id: number
  name: string
  nickname: string
  username: string
  avatar: string
  isAdmin: boolean
}

// 评论组件 Props
export interface CommentProps {
  postId: string | number
  authorId?: string | number
  isQuestion?: boolean
}

// 评论组件 Emits
export interface CommentEmits {
  (e: 'update:comments', comments: CommentItem[]): void
  (e: 'comment-count-change', count: number): void
}
