import request from '@/utils/request'

/**
 * 专栏管理API
 */

// 获取专栏列表
export function getColumnList(params: {
  page?: number
  size?: number
  keyword?: string
  status?: number
  isRecommended?: number
  userId?: number
}) {
  return request({
    url: '/api/system/column/list',
    method: 'get',
    params
  })
}

// 获取专栏统计数据
export function getColumnStatistics() {
  return request({
    url: '/api/system/column/statistics',
    method: 'get'
  })
}

// 删除专栏
export function deleteColumn(id: number) {
  return request({
    url: `/api/system/column/${id}/delete`,
    method: 'post'
  })
}

// 归档专栏
export function archiveColumn(id: number) {
  return request({
    url: `/api/system/column/${id}/archive`,
    method: 'post'
  })
}

// 设置推荐
export function setRecommend(id: number, isRecommended: number) {
  return request({
    url: `/api/system/column/${id}/recommend`,
    method: 'post',
    params: { isRecommended }
  })
}

// 批量删除专栏
export function batchDeleteColumns(ids: number[]) {
  return request({
    url: '/api/system/column/batch-delete',
    method: 'post',
    data: ids
  })
}
