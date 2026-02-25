/**
 * 分页信息
 */
export interface Pagination {
  pageNo: number
  pageSize: number
  total: number
  pages: number
}

/**
 * 排序方向
 */
export type SortOrder = 'asc' | 'desc'

/**
 * 通用 ID 类型
 */
export type ID = number | string

/**
 * 可空类型
 */
export type Nullable<T> = T | null

/**
 * 可选类型
 */
export type Optional<T> = T | undefined

/**
 * 键值对
 */
export interface KeyValue<T = string> {
  key: string
  value: T
}

/**
 * 选项类型（用于下拉框等）
 */
export interface Option<T = string | number> {
  label: string
  value: T
  disabled?: boolean
}
