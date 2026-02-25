/**
 * 标签VO（与后端Tag实体对齐）
 */
export interface TagPageVO {
  /**
   * 标签ID
   */
  id?: number;
  /**
   * 标签名
   */
  name?: string;
  /**
   * 标签描述
   */
  description?: string;
  /**
   * 使用次数（帖子数量）
   */
  usageCount?: number;
  /**
   * 是否推荐：0-否，1-是
   */
  isRecommended?: number;
  /**
   * 排序
   */
  sort?: number;
  /**
   * 创建时间
   */
  createTime?: string;
  /**
   * 更新时间
   */
  updateTime?: string;
}

/**
 * 查询对象类型
 */
export interface TagQuery extends PageQuery {
  name?: string;
}

/**
 * 表单请求参数（与后端TagRequest对齐）
 */
export interface TagForm {
  /**
   * 标签ID
   */
  id?: number;
  /**
   * 标签名
   */
  name: string;
  /**
   * 标签描述
   */
  description?: string;
  /**
   * 排序
   */
  sort?: number;
}
