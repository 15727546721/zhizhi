export interface CommentDTO {
  id: number;
  type: number;
  targetId: number;
  parentId: number | null;
  userId: number;
  nickname: string;
  avatar: string | null;
  replyUserId: number | null;
  content: string;
  likeCount: number;
  replyCount: number;
  createTime: string;
}

/**
 * 评论查询参数
 */
export interface CommentQuery {
  /** 页码 */
  pageNo: number;
  /** 每页显示条数 */
  pageSize: number;
  /** 评论类型 */
  type?: number;
  /** 用户ID */
  userId?: number;
}

export interface PageResponse<T> {
  pageNo: number;
  pageSize: number;
  total: number;
  data: T;
}

/**
 * 评论回复DTO（与后端 SysCommentVO 一致）
 */
export interface CommentReplyDTO {
  /** 评论ID */
  id: number;
  /** 评论内容 */
  content: string;
  /** 评论用户ID */
  userId: number;
  /** 评论用户昵称 */
  nickname: string;
  /** 评论用户头像 */
  avatar: string | null;
  /** 被回复用户ID */
  replyUserId: number;
  /** 被回复用户昵称 */
  replyNickname: string;
  /** 被回复用户头像 */
  replyAvatar: string | null;
  /** 评论时间 */
  createTime: string;
}