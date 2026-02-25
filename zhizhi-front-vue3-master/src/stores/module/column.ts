/**
 * 专栏状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  ColumnVO,
  ColumnDetailVO,
  ColumnCreateDTO,
  ColumnUpdateDTO,
  PostSortDTO,
} from '@/types/column'
import * as columnApi from '@/api/column'
import { ElMessage } from 'element-plus'

export const useColumnStore = defineStore('column', () => {
  // 状态
  const myColumns = ref<ColumnVO[]>([])
  const currentColumn = ref<ColumnDetailVO | null>(null)
  const subscriptions = ref<ColumnVO[]>([])
  const loading = ref(false)

  // 计算属性
  const myColumnsCount = computed(() => myColumns.value.length)
  const canCreateColumn = computed(() => myColumnsCount.value < 10)

  // Actions

  /**
   * 获取我的专栏列表
   */
  const fetchMyColumns = async (userId: number): Promise<void> => {
    try {
      loading.value = true
      const res = await columnApi.getUserColumns(userId) as any
      
      if (res.code === 20000 && res.data) {
        myColumns.value = res.data
      }
    } catch (error) {
      ElMessage.error('获取专栏列表失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取专栏详情
   */
  const fetchColumnDetail = async (id: number): Promise<ColumnDetailVO | undefined> => {
    try {
      loading.value = true
      const res = await columnApi.getColumnDetail(id) as any
      if (res.code === 20000 && res.data) {
        currentColumn.value = res.data
        return res.data
      }
    } catch (error) {
      ElMessage.error('获取专栏详情失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建专栏
   */
  const createColumn = async (data: ColumnCreateDTO): Promise<any> => {
    try {
      if (!canCreateColumn.value) {
        ElMessage.warning('专栏数量已达上限(10个)')
        return null
      }

      loading.value = true
      const res = await columnApi.createColumn(data) as any
      
      if (res.code === 20000 && res.data) {
        ElMessage.success('专栏创建成功')
        return res.data
      } else {
        ElMessage.error(res.info || '创建专栏失败')
        return null
      }
    } catch (error: any) {
      ElMessage.error(error.message || '创建专栏失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新专栏
   */
  const updateColumn = async (id: number, data: ColumnUpdateDTO): Promise<void> => {
    try {
      loading.value = true
      const res = await columnApi.updateColumn(id, data) as any
      if (res.code === 20000) {
        ElMessage.success('专栏更新成功')
        // 更新本地缓存
        const index = myColumns.value.findIndex((c) => c.id === id)
        if (index !== -1) {
          myColumns.value[index] = { ...myColumns.value[index], ...data }
        }
        if (currentColumn.value && currentColumn.value.id === id) {
          currentColumn.value = { ...currentColumn.value, ...data }
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '更新专栏失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除专栏
   */
  const deleteColumn = async (id: number): Promise<void> => {
    try {
      loading.value = true
      const res = await columnApi.deleteColumn(id) as any
      if (res.code === 20000) {
        ElMessage.success('专栏删除成功')
        // 从本地缓存移除
        myColumns.value = myColumns.value.filter((c) => c.id !== id)
        if (currentColumn.value && currentColumn.value.id === id) {
          currentColumn.value = null
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除专栏失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 订阅专栏
   */
  const subscribeColumn = async (columnId: number): Promise<void> => {
    try {
      const res = await columnApi.subscribeColumn(columnId) as any
      if (res.code === 20000) {
        ElMessage.success('订阅成功')
        // 更新订阅状态
        updateSubscribeStatus(columnId, true)
      }
    } catch (error: any) {
      ElMessage.error(error.message || '订阅失败')
      throw error
    }
  }

  /**
   * 取消订阅专栏
   */
  const unsubscribeColumn = async (columnId: number): Promise<void> => {
    try {
      const res = await columnApi.unsubscribeColumn(columnId) as any
      if (res.code === 20000) {
        ElMessage.success('取消订阅成功')
        // 更新订阅状态
        updateSubscribeStatus(columnId, false)
      }
    } catch (error: any) {
      ElMessage.error(error.message || '取消订阅失败')
      throw error
    }
  }

  /**
   * 添加文章到专栏
   */
  const addPostToColumn = async (columnId: number, postId: number, sort?: number): Promise<void> => {
    try {
      const res = await columnApi.addPostToColumn(columnId, postId, sort) as any
      if (res.code === 20000) {
        ElMessage.success('文章添加成功')
        // 更新专栏文章数
        const column = myColumns.value.find((c) => c.id === columnId)
        if (column) {
          column.postCount++
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '添加文章失败')
      throw error
    }
  }

  /**
   * 从专栏移除文章
   */
  const removePostFromColumn = async (columnId: number, postId: number): Promise<void> => {
    try {
      const res = await columnApi.removePostFromColumn(columnId, postId) as any
      if (res.code === 20000) {
        ElMessage.success('文章移除成功')
        // 更新专栏文章数
        const column = myColumns.value.find((c) => c.id === columnId)
        if (column && column.postCount > 0) {
          column.postCount--
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '移除文章失败')
      throw error
    }
  }

  /**
   * 批量调整文章顺序
   */
  const updatePostsSort = async (columnId: number, sortList: PostSortDTO[]): Promise<void> => {
    try {
      const res = await columnApi.updatePostsSort(columnId, sortList) as any
      if (res.code === 20000) {
        ElMessage.success('排序更新成功')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '更新排序失败')
      throw error
    }
  }

  /**
   * 更新订阅状态(本地)
   */
  const updateSubscribeStatus = (columnId: number, isSubscribed: boolean) => {
    // 更新我的专栏列表
    const column = myColumns.value.find((c) => c.id === columnId)
    if (column) {
      column.isSubscribed = isSubscribed
      column.subscribeCount += isSubscribed ? 1 : -1
    }
    // 更新当前专栏
    if (currentColumn.value && currentColumn.value.id === columnId) {
      currentColumn.value.isSubscribed = isSubscribed
      currentColumn.value.subscribeCount += isSubscribed ? 1 : -1
    }
  }

  /**
   * 清空当前专栏
   */
  const clearCurrentColumn = () => {
    currentColumn.value = null
  }

  /**
   * 重置状态
   */
  const reset = () => {
    myColumns.value = []
    currentColumn.value = null
    subscriptions.value = []
    loading.value = false
  }

  return {
    // 状态
    myColumns,
    currentColumn,
    subscriptions,
    loading,
    // 计算属性
    myColumnsCount,
    canCreateColumn,
    // Actions
    fetchMyColumns,
    fetchColumnDetail,
    createColumn,
    updateColumn,
    deleteColumn,
    subscribeColumn,
    unsubscribeColumn,
    addPostToColumn,
    removePostFromColumn,
    updatePostsSort,
    updateSubscribeStatus,
    clearCurrentColumn,
    reset,
  }
})
