/**
 * 增强的类型定义
 * 确保与后端 VO 完全匹配
 */

import type { Tag } from './tag'

/**
 * 用户详情（完整信息）
 * 对应后端: UserDetailVO.java
 */
export interface UserDetail {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string | null
  bio: string | null
  gender: number | null
  birthday: string | null
  location: string | null
  website: string | null
  // 统计信息
  followCount: number
  fansCount: number
  postCount: number
  likeCount: number
  commentCount: number
  // 时间信息
  createTime: string
  updateTime: string | null
  lastLoginTime: string | null
}

/**
 * 用户简要信息
 * 对应后端: UserBriefVO.java
 */
export interface UserBrief {
  id: number
  username: string
  nickname: string
  avatar: string
}

/**
 * 用户个人资料
 * 对应后端: UserProfileVO.java
 */
export interface UserProfile {
  basicInfo: {
    id: number
    username: string
    nickname: string
    avatar: string
    bio: string | null
    gender: number | null
    birthday: string | null
    location: string | null
    website: string | null
  }
  stats: {
    followCount: number
    fansCount: number
    postCount: number
    likeCount: number
    commentCount: number
    favoriteCount: number
  }
  isOwnProfile: boolean
  isFollowing: boolean
  isFollowedBy: boolean
  isBlocked: boolean
}

/**
 * 帖子详情（完整信息）
 * 对应后端: PostDetailVO.java
 */
export interface PostDetail {
  // 基本信息
  id: number
  title: string
  description: string
  content: string
  coverUrl: string | null
  // 作者信息
  userId: number
  userName: string
  userAvatar: string
  // 状态信息
  status: number
  // 统计信息
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  shareCount: number
  // 用户交互状态
  isLiked: boolean
  isFavorited: boolean
  isFollowingAuthor: boolean
  // 标签
  tags: Tag[]
  // 专栏信息（如果属于专栏）
  columnId: number | null
  columnName: string | null
  // 时间信息
  createTime: string
  updateTime: string | null
}

/**
 * 帖子列表项（简要信息）
 * 对应后端: PostItemVO.java
 */
export interface PostItem {
  id: number
  title: string
  description: string
  coverUrl: string | null
  userId: number
  userName: string
  userAvatar: string
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  favoriteCount: number
  tags: Tag[]
  createTime: string
}

/**
 * 评论详情
 * 对应后端: CommentVO.java
 */
export interface CommentDetail {
  id: number
  content: string
  imageUrls: string[] | null
  // 关联信息
  postId: number
  userId: number
  userName: string
  userAvatar: string
  // 回复关系
  parentId: number | null
  rootId: number | null
  replyToUserId: number | null
  replyToUserName: string | null
  // 统计信息
  likeCount: number
  replyCount: number
  // 用户交互状态
  isLiked: boolean
  isAuthorLiked: boolean
  // 热度信息
  hotScore: number | null
  isHot: boolean
  // 子评论
  children: CommentDetail[]
  // 时间信息
  createTime: string
  updateTime: string | null
}

/**
 * 专栏详情
 * 对应后端: ColumnDetailVO.java
 */
export interface ColumnDetail {
  id: number
  userId: number
  userName: string
  userAvatar: string
  name: string
  description: string
  coverUrl: string | null
  status: number
  postCount: number
  subscribeCount: number
  isSubscribed: boolean
  isOwner: boolean
  lastPostTime: string | null
  createTime: string
  updateTime: string | null
  // 最近文章
  recentPosts: ColumnPostItem[]
}

/**
 * 专栏文章项
 * 对应后端: ColumnPostVO.java
 */
export interface ColumnPostItem {
  postId: number
  title: string
  description: string | null
  coverUrl: string | null
  viewCount: number
  likeCount: number
  commentCount: number
  sort: number
  createTime: string
}

/**
 * 专栏文章导航
 * 对应后端: ColumnPostNavigationVO.java
 */
export interface ColumnPostNavigation {
  previousPostId: number | null
  previousPostTitle: string | null
  nextPostId: number | null
  nextPostTitle: string | null
}

/**
 * 收藏夹详情
 * 对应后端: FavoriteFolderVO.java
 */
export interface FavoriteFolderDetail {
  id: number
  userId: number
  name: string
  description: string | null
  coverUrl: string | null
  isDefault: boolean
  isPublic: boolean
  itemCount: number
  createTime: string
  updateTime: string | null
}

/**
 * 私信详情
 * 对应后端: PrivateMessageVO.java
 */
export interface PrivateMessageDetail {
  messageId: number
  senderId: number
  senderName: string
  senderAvatar: string
  receiverId: number
  receiverName: string
  receiverAvatar: string
  content: string
  messageType: number
  isRead: boolean
  isWithdrawn: boolean
  createTime: string
}

/**
 * 会话列表项
 * 对应后端: ConversationListVO.java
 */
export interface ConversationItem {
  conversationId: string
  otherUserId: number
  otherUserName: string
  otherUserAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
  isOnline: boolean
}

/**
 * 通知详情
 * 对应后端: NotificationVO.java
 */
export interface NotificationDetail {
  id: number
  userId: number
  type: number
  title: string
  content: string
  relatedId: number | null
  relatedType: string | null
  isRead: boolean
  createTime: string
}

/**
 * 关注信息
 * 对应后端: FollowUserVO.java
 */
export interface FollowUser {
  userId: number
  userName: string
  userAvatar: string
  userBio: string | null
  isFollowing: boolean
  isFollowedBy: boolean
  followTime: string | null
}

/**
 * 点赞状态
 * 对应后端: LikeStatusVO.java
 */
export interface LikeStatus {
  targetId: number
  targetType: number
  isLiked: boolean
  likeCount: number
}

/**
 * 标签详情
 * 对应后端: TagVO.java
 */
export interface TagDetail {
  id: number
  name: string
  description: string | null
  usageCount: number
  postCount: number
  followCount: number
  isFollowing: boolean
  createTime: string
}

/**
 * 搜索结果
 * 对应后端: PostSearchVO.java
 */
export interface SearchResult {
  post: PostItem
  highlightTitle: string | null
  highlightContent: string | null
  score: number
}

/**
 * 用户设置
 * 对应后端: UserSettingsVO.java
 */
export interface UserSettings {
  privacy: {
    profileVisibility: number
    showEmail: boolean
    showPhone: boolean
  }
  notification: {
    emailNotification: boolean
    systemNotification: boolean
    commentNotification: boolean
    likeNotification: boolean
    followNotification: boolean
  }
  message: {
    allowStrangerMessage: boolean
    autoReply: boolean
    autoReplyContent: string | null
  }
}

/**
 * 举报信息
 * 对应后端: ReportVO.java
 */
export interface ReportDetail {
  id: number
  reporterId: number
  reporterName: string
  targetId: number
  targetType: number
  targetTitle: string | null
  reason: string
  description: string | null
  status: number
  handleResult: string | null
  createTime: string
  handleTime: string | null
}

/**
 * 分享结果
 * 对应后端: ShareResultVO.java
 */
export interface ShareResult {
  shareId: number
  shareUrl: string
  qrCodeUrl: string | null
  shareCount: number
}

/**
 * 系统配置
 * 对应后端: SystemConfigVO.java
 */
export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  configType: string
  description: string | null
  createTime: string
  updateTime: string | null
}

/**
 * 用户排行榜项
 * 对应后端: UserRankingVO.java
 */
export interface UserRanking {
  rank: number
  userId: number
  userName: string
  userAvatar: string
  score: number
  postCount: number
  likeCount: number
  followCount: number
}

/**
 * 首页数据
 * 对应后端: HomeDataVO.java
 */
export interface HomeData {
  hotPosts: PostItem[]
  recommendPosts: PostItem[]
  latestPosts: PostItem[]
  hotTags: TagDetail[]
  hotUsers: UserBrief[]
}
