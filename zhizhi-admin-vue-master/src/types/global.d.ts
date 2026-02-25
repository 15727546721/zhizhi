/** 分页查询参数 */
export interface PageQuery {
  pageNo: number;
  pageSize: number;
}

/** 分页返回结果 */
export interface PageResult<T> {
  /** 当前页码 */
  pageNum: number;
  /** 每页数量 */
  pageSize: number;
  /** 总记录数 */
  total: number;
  /** 总页数 */
  pages: number;
  /** 列表数据 */
  records: T[];
  /** 是否有下一页 */
  hasNext: boolean;
  /** 是否有上一页 */
  hasPrevious: boolean;
} 