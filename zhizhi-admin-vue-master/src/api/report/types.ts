/**
 * 举报相关类型定义
 */

// 举报目标类型
export enum ReportTargetType {
  POST = 1,     // 帖子
  COMMENT = 2,  // 评论
  USER = 3      // 用户
}

// 举报原因
export enum ReportReason {
  SPAM = 1,        // 垃圾广告
  ILLEGAL = 2,     // 违法违规
  PORN = 3,        // 色情低俗
  ATTACK = 4,      // 人身攻击
  PLAGIARISM = 5,  // 抄袭侵权
  OTHER = 6        // 其他
}

// 举报状态
export enum ReportStatus {
  PENDING = 0,   // 待处理
  APPROVED = 1,  // 已通过
  REJECTED = 2,  // 已驳回
  IGNORED = 3    // 已忽略
}

// 处罚措施
export enum HandleAction {
  NONE = 0,        // 无
  DELETE = 1,      // 删除内容
  WARN = 2,        // 警告
  BAN_7D = 3,      // 禁言7天
  BAN_30D = 4,     // 禁言30天
  BAN_FOREVER = 5  // 永久封号
}

// 举报查询参数
export interface ReportQueryParams {
  status?: number;
  targetType?: number;
  reason?: number;
  reporterId?: number;
  targetUserId?: number;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
}

// 举报详情
export interface ReportDetail {
  id: number;
  reporterId: number;
  reporterNickname: string;
  reporterAvatar: string;
  targetType: number;
  targetTypeName: string;
  targetId: number;
  targetContent: string;
  targetUserId: number;
  targetUserNickname: string;
  targetUserAvatar: string;
  reason: number;
  reasonName: string;
  description: string;
  evidenceUrls: string[];
  status: number;
  statusName: string;
  handlerId: number;
  handlerNickname: string;
  handleResult: string;
  handleAction: number;
  handleActionName: string;
  handleTime: string;
  createTime: string;
}

// 处理举报请求
export interface HandleReportRequest {
  status: number;      // 1-通过 2-驳回 3-忽略
  handleAction?: number;
  handleResult?: string;
}

// 举报统计
export interface ReportStats {
  total: number;
  pending: number;
  handled: number;
}

// 举报原因选项
export const ReasonOptions = [
  { value: 1, label: '垃圾广告' },
  { value: 2, label: '违法违规' },
  { value: 3, label: '色情低俗' },
  { value: 4, label: '人身攻击' },
  { value: 5, label: '抄袭侵权' },
  { value: 6, label: '其他' }
];

// 目标类型选项
export const TargetTypeOptions = [
  { value: 1, label: '帖子' },
  { value: 2, label: '评论' },
  { value: 3, label: '用户' }
];

// 状态选项
export const StatusOptions = [
  { value: 0, label: '待处理', type: 'warning' },
  { value: 1, label: '已通过', type: 'success' },
  { value: 2, label: '已驳回', type: 'danger' },
  { value: 3, label: '已忽略', type: 'info' }
];

// 处罚措施选项
export const ActionOptions = [
  { value: 0, label: '无' },
  { value: 1, label: '删除内容' },
  { value: 2, label: '警告' },
  { value: 3, label: '禁言7天' },
  { value: 4, label: '禁言30天' },
  { value: 5, label: '永久封号' }
];
