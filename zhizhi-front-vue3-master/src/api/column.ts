/**
 * 专栏相关API接口
 */
import request from '@/utils/request'
import type {
  Column,
  ColumnVO,
  ColumnDetailVO,
  ColumnPostVO,
  ColumnCreateDTO,
  ColumnUpdateDTO,
  PostSortDTO,
  BatchSortRequest,
  ColumnSquareQuery,
  ColumnSearchQuery,
  PageResult,
} from '@/types/column'

/**
 * 创建专栏
 */
export const createColumn = (data: ColumnCreateDTO) => {
  return request<number>({
    url: '/columns',
    method: 'POST',
    data,
  })
}

/**
 * 更新专栏
 */
export const updateColumn = (id: number, data: ColumnUpdateDTO) => {
  return request<void>({
    url: `/columns/${id}`,
    method: 'PUT',
    data,
  })
}

/**
 * 删除专栏
 */
export const deleteColumn = (id: number) => {
  return request<void>({
    url: `/columns/${id}`,
    method: 'DELETE',
  })
}

/**
 * 获取专栏详情
 */
export const getColumnDetail = (id: number) => {
  return request<ColumnDetailVO>({
    url: `/columns/${id}`,
    method: 'GET',
  })
}

/**
 * 获取用户的专栏列表
 */
export const getUserColumns = (userId: number) => {
  return request<ColumnVO[]>({
    url: `/columns/user/${userId}`,
    method: 'GET',
  })
}

/**
 * 添加文章到专栏
 */
export const addPostToColumn = (columnId: number, postId: number, sort?: number) => {
  return request<void>({
    url: `/columns/${columnId}/posts/${postId}`,
    method: 'POST',
    params: { sort },
  })
}

/**
 * 从专栏移除文章
 */
export const removePostFromColumn = (columnId: number, postId: number) => {
  return request<void>({
    url: `/columns/${columnId}/posts/${postId}`,
    method: 'DELETE',
  })
}

/**
 * 批量调整文章顺序
 */
export const updatePostsSort = (columnId: number, sortList: PostSortDTO[]) => {
  const data: BatchSortRequest = { sorts: sortList }
  return request<void>({
    url: `/columns/${columnId}/posts/sort`,
    method: 'PUT',
    data,
  })
}

/**
 * 获取专栏的文章列表
 */
export const getColumnPosts = (columnId: number, page = 1, size = 20) => {
  return request<PageResult<ColumnPostVO>>({
    url: `/columns/${columnId}/posts`,
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 订阅专栏
 */
export const subscribeColumn = (columnId: number) => {
  return request<void>({
    url: `/columns/${columnId}/subscribe`,
    method: 'POST',
  })
}

/**
 * 取消订阅专栏
 */
export const unsubscribeColumn = (columnId: number) => {
  return request<void>({
    url: `/columns/${columnId}/subscribe`,
    method: 'DELETE',
  })
}

/**
 * 专栏广场
 */
export const getColumnSquare = (params: ColumnSquareQuery) => {
  return request<PageResult<ColumnVO>>({
    url: '/columns/square',
    method: 'GET',
    params: {
      sortType: params.sortType || 'latest',
      page: params.page || 1,
      size: params.size || 20,
    },
  })
}

/**
 * 搜索专栏
 */
export const searchColumns = (params: ColumnSearchQuery) => {
  return request<PageResult<ColumnVO>>({
    url: '/columns/search',
    method: 'GET',
    params: {
      keyword: params.keyword,
      page: params.page || 1,
      size: params.size || 20,
    },
  })
}

/**
 * 获取推荐专栏
 */
export const getRecommendedColumns = (limit = 10) => {
  return request<ColumnVO[]>({
    url: '/columns/recommended',
    method: 'GET',
    params: { limit },
  })
}

/**
 * 获取文章所属的专栏列表
 */
export const getPostColumns = (postId: number) => {
  return request<ColumnVO[]>({
    url: `/columns/post/${postId}`,
    method: 'GET',
  })
}

/**
 * 获取文章在专栏中的导航信息
 */
export const getPostNavigation = (columnId: number, postId: number) => {
  return request<{
    previousPostId: number | null
    previousPostTitle: string | null
    nextPostId: number | null
    nextPostTitle: string | null
  }>({
    url: `/columns/${columnId}/posts/${postId}/navigation`,
    method: 'GET',
  })
}

/**
 * 获取用户订阅的专栏列表
 */
export const getUserSubscriptions = (page = 1, size = 20) => {
  return request<PageResult<ColumnVO>>({
    url: '/columns/subscriptions',
    method: 'GET',
    params: { page, size },
  })
}
