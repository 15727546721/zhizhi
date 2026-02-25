/**
 * 角色查询参数
 */
export interface RoleQuery {
  pageNo: number;
  pageSize: number;
  name?: string;
}

/**
 * 角色分页对象
 */
export interface RolePageVO {
  id: number;
  name: string;
  code: string;
  remark: string;
  createTime?: number[];
  updateTime?: number[];
}

/**
 * 角色分页
 */
export interface RolePageResult {
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
  records: RolePageVO[];
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * 角色表单对象
 */
export interface RoleForm {
  id?: number;
  name: string;
  code: string;
  remark: string;
}
