/**
 * 帖子查询参数
 */
export interface PostQuery {
  pageNo: number;
  pageSize: number;
  title?: string;
  tagId?: number;
  status?: number; // 0:草稿 1:已发布 2:已删除
  userId?: number;
}

/**
 * 帖子分页数据（后台管理列表VO，不包含content字段）
 */
export interface PostPageVO {
  id: number;
  title: string;
  description: string;
  coverUrl: string;
  userId: number;
  nickname?: string;
  isFeatured: number; // 0:否 1:是
  viewCount: number;
  favoriteCount: number;
  commentCount: number;
  likeCount: number;
  shareCount: number;
  status: number; // 0:草稿 1:已发布 2:已删除
  tagNames?: string; // 标签名（逗号分隔）
  createTime: string;
  updateTime: string;
}

/**
 * 帖子分页结果
 */
export interface PostPageResult {
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
  records: PostPageVO[];
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * 帖子表单对象
 */
export interface PostForm {
  id?: number;
  title: string;
  content: string;
  description?: string;
  coverUrl?: string;
  isFeatured?: number;
  status?: number;
  tagIds?: number[];
}

// 别名，兼容旧代码
export type ArticleQuery = PostQuery;
export type ArticlePageVO = PostPageVO;
export type ArticlePageResult = PostPageResult;
export type ArticleForm = PostForm;
