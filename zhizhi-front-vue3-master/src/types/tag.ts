/**
 * 标签信息
 */
export interface Tag {
  id: number
  name: string
  description?: string
  icon?: string
  color?: string
  postCount?: number
  usageCount?: number
  isRecommended?: number | boolean
  sort?: number
  createTime?: string
  updateTime?: string
  createdAt?: string
}
