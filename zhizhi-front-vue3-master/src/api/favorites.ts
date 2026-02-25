import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types'

export const FavoriteType = {
  POST: 'POST',
  COMMENT: 'COMMENT'
} as const

type FavoriteTypeValue = (typeof FavoriteType)[keyof typeof FavoriteType]

interface FavoriteCheckItem {
  targetId: number
  targetType: string
}

interface FavoriteCheckResult {
  targetId: number
  targetType: string
  isFavorited: boolean
}

// 收藏夹类型
export interface FavoriteFolder {
  id: number
  name: string
  description?: string
  isPublic: boolean
  isDefault: boolean
  itemCount: number
  coverUrl?: string
  createTime: string
}

export interface CreateFolderRequest {
  name: string
  description?: string
  isPublic?: boolean
}

export interface UpdateFolderRequest {
  name?: string
  description?: string
  isPublic?: boolean
}

const favoritesApi = {
  checkFavorite: (params: { targetId: number; targetType?: string; type?: string }): Promise<ApiResponse<boolean>> => {
    const requestParams: Record<string, unknown> = { ...params }
    if (requestParams.type && !requestParams.targetType) {
      requestParams.targetType = (requestParams.type as string).toUpperCase()
      delete requestParams.type
    }
    return request({ url: '/favorites/check', method: 'get', params: requestParams })
  },

  checkFavoriteBatch: (data: FavoriteCheckItem[]): Promise<ApiResponse<FavoriteCheckResult[]>> => {
    return request({ url: '/favorites/check/batch', method: 'post', data })
  },

  addFavorite: (data: { targetId: number; targetType: string; folderId?: number }): Promise<ApiResponse> => {
    return request({ url: '/favorites/favorite', method: 'post', data })
  },

  removeFavorite: (data: { targetId: number; targetType: string }): Promise<ApiResponse> => {
    return request({ url: '/favorites/unfavorite', method: 'post', data })
  },

  getMyFavorites: (data: {
    pageNo: number
    pageSize: number
    type?: string
  }): Promise<ApiResponse<PageResponse<unknown>>> => {
    return request({ url: '/favorites/my', method: 'post', data })
  },

  batchRemoveFavorites: (data: { items: FavoriteCheckItem[] }): Promise<ApiResponse> => {
    return request({ url: '/favorites/batch-remove', method: 'post', data })
  }
}

// 收藏夹 API
export const folderApi = {
  // 获取我的收藏夹列表
  getMyFolders: (): Promise<ApiResponse<FavoriteFolder[]>> => {
    return request({ url: '/favorite-folders/my', method: 'get' })
  },

  // 获取用户公开的收藏夹
  getUserPublicFolders: (userId: number): Promise<ApiResponse<FavoriteFolder[]>> => {
    return request({ url: `/favorite-folders/user/${userId}`, method: 'get' })
  },

  // 获取收藏夹详情
  getFolderDetail: (folderId: number): Promise<ApiResponse<FavoriteFolder>> => {
    return request({ url: `/favorite-folders/${folderId}`, method: 'get' })
  },

  // 创建收藏夹
  createFolder: (data: CreateFolderRequest): Promise<ApiResponse<FavoriteFolder>> => {
    return request({ url: '/favorite-folders', method: 'post', data })
  },

  // 更新收藏夹
  updateFolder: (folderId: number, data: UpdateFolderRequest): Promise<ApiResponse> => {
    return request({ url: `/favorite-folders/${folderId}`, method: 'put', data })
  },

  // 删除收藏夹
  deleteFolder: (folderId: number): Promise<ApiResponse> => {
    return request({ url: `/favorite-folders/${folderId}`, method: 'delete' })
  },

  // 获取收藏夹中的收藏内容
  getFolderFavorites: (folderId: number, data: { pageNo: number; pageSize: number; type?: string }): Promise<ApiResponse<PageResponse<unknown>>> => {
    return request({ url: `/favorite-folders/${folderId}/favorites`, method: 'post', data })
  },

  // 迁移收藏夹内容
  moveFolderContents: (sourceFolderId: number, targetFolderId: number): Promise<ApiResponse<number>> => {
    return request({ url: `/favorite-folders/${sourceFolderId}/move-to/${targetFolderId}`, method: 'post' })
  },

  // 合并收藏夹
  mergeFolders: (sourceFolderId: number, targetFolderId: number): Promise<ApiResponse<number>> => {
    return request({ url: `/favorite-folders/${sourceFolderId}/merge-to/${targetFolderId}`, method: 'post' })
  }
}

export const addFavorite = (targetId: number, type: FavoriteTypeValue = FavoriteType.POST, folderId?: number): Promise<ApiResponse> => {
  return favoritesApi.addFavorite({ targetId, targetType: type, folderId })
}

export const removeFavorite = (targetId: number, type: FavoriteTypeValue = FavoriteType.POST): Promise<ApiResponse> => {
  return favoritesApi.removeFavorite({ targetId, targetType: type })
}

export const checkFavorite = (targetId: number, type: FavoriteTypeValue = FavoriteType.POST): Promise<ApiResponse<boolean>> => {
  const targetType = typeof type === 'string' ? type.toUpperCase() : type
  return favoritesApi.checkFavorite({ targetId, targetType })
}

export const checkFavoriteBatch = favoritesApi.checkFavoriteBatch
export const getMyFavorites = favoritesApi.getMyFavorites
export const batchRemoveFavorites = favoritesApi.batchRemoveFavorites

export default favoritesApi
