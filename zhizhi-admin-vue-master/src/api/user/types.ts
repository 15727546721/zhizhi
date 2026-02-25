/**
 * 登录用户信息
 */
export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  email: string;
  phone?: string;
  gender?: number;
  location?: string;
  website?: string;
  birthday?: string;
  description?: string;
  status: number;
  userType: number; // 1:普通用户 2:官方账号 3:管理员
  followCount?: number;
  fansCount?: number;
  likeCount?: number;
  postCount?: number;
  commentCount?: number;
  lastLoginTime?: string;
  lastLoginIp?: string;
  createTime: string;
  updateTime?: string;
  roles?: string[];
  perms?: string[];
}

/**
 * 用户查询对象类型
 */
export interface UserQuery extends PageQuery {
  keywords?: string;
  status?: number;
  userType?: number;
  startTime?: string;
  endTime?: string;
}

/**
 * 用户分页对象
 */
export interface UserPageVO {
  id?: number;
  username?: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  gender?: number;
  location?: string;
  description?: string;
  status?: number;
  userType?: number; // 1:普通用户 2:官方账号 3:管理员
  followCount?: number;
  fansCount?: number;
  likeCount?: number;
  postCount?: number;
  commentCount?: number;
  lastLoginTime?: string;
  createTime?: Date;
  roleNames?: string;
}

/**
 * 用户表单类型
 */
export interface UserForm {
  id?: number;
  username?: string;
  password?: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  gender?: number;
  location?: string;
  description?: string;
  status?: number; // 1:正常 0:禁用
  userType?: number; // 1:普通用户 2:官方账号 3:管理员
  roleIds?: number[];
}

/**
 * 修改密码请求参数
 */
export interface PasswordData {
  /**
   * 旧密码
   */
  oldPassword: string;
  /**
   * 新密码
   */
  newPassword: string;

  confirmPassword: string;
}

export interface UserInfoResponse {
  code: number;
  data: UserInfo;
  info: string;
}

/**
 * 更新用户信息的请求参数类型
 */
export interface UpdateUserInfoParams {
  nickname?: string;
  email?: string;
  avatar?: string;
}
