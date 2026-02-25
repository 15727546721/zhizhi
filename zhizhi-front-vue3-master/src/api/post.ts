import request from '@/utils/request'
import { like, unlike, LikeType } from '@/api/like'
import type { ApiResponse, Post, PageResponse, CursorResponse, Tag } from '@/types'

export const PostStatus = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  DELETED: 'DELETED'
} as const

export const SortType = {
  LATEST: 'latest',
  HOTTEST: 'hottest',
  COMMENTS: 'comments',
  LIKES: 'likes',
  FAVORITES: 'favorites'
} as const

interface PostCreateData {
  title: string
  content: string
  description?: string
  coverUrl?: string
  tagIds?: number[]
  status?: string
}

interface PostListParams {
  pageNo?: number
  pageSize?: number
  type?: string
  tagId?: number
  sortBy?: string
  keyword?: string
  status?: string
}

interface PostSearchParams {
  keyword: string
  pageNo?: number
  pageSize?: number
  types?: string[]
  timeRange?: string
  sortOption?: string
}

interface CursorParams {
  cursor?: string | null
  pageSize?: number
  sortBy?: string
  tagId?: number | null
}

export function createPost(data: PostCreateData): Promise<ApiResponse<Post>> {
  return request({ url: '/post/create', method: 'post', data })
}

export function updatePost(postId: number, data: PostCreateData): Promise<ApiResponse<Post>> {
  return request({ url: '/post/publish', method: 'post', data: { ...data, id: postId } })
}

export function getTags(): Promise<ApiResponse<Tag[]>> {
  return request({ url: '/tag/list', method: 'get' })
}

export function saveDraft(data: PostCreateData): Promise<ApiResponse<Post>> {
  return request({
    url: '/post/saveDraft',
    method: 'post',
    data: { ...data, status: PostStatus.DRAFT }
  })
}

export function deleteDraft(id: number): Promise<ApiResponse> {
  return request({ url: `/post/draft/${id}/delete`, method: 'post' })
}


export function getPostDraftList(params: { pageNo?: number; pageSize?: number } = {}): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/post/drafts',
    method: 'post',
    data: { pageNo: params.pageNo || 1, pageSize: params.pageSize || 10 }
  })
}

export function likePost(postId: number): Promise<ApiResponse> {
  return like({ targetId: postId, type: LikeType.POST })
}

export function unlikePost(postId: number): Promise<ApiResponse> {
  return unlike({ targetId: postId, type: LikeType.POST })
}

export function checkPostLikeStatus(postId: number): Promise<ApiResponse<boolean>> {
  return request({
    url: '/likes/status',
    method: 'get',
    params: { targetId: postId, type: LikeType.POST }
  })
}

export function getPostLikeCount(postId: number): Promise<ApiResponse<{ count: number }>> {
  return request({
    url: '/likes/count',
    method: 'get',
    params: { targetId: postId, type: LikeType.POST }
  })
}

export function publishPost(postId: number): Promise<ApiResponse> {
  return request({ url: '/post/publish', method: 'post', data: { postId } })
}

export function publishDraft(data: PostCreateData): Promise<ApiResponse<Post>> {
  return request({
    url: '/post/publish',
    method: 'post',
    data: { ...data, status: PostStatus.PUBLISHED }
  })
}

export function getPostDetail(postId: number): Promise<ApiResponse<Post>> {
  return request({ url: `/post/${postId}`, method: 'get' })
}

export function getPostList(params: PostListParams): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/post/page',
    method: 'post',
    data: {
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      type: params.type,
      tagId: params.tagId,
      sortBy: params.sortBy,
      keyword: params.keyword
    }
  })
}


export function searchPosts(params: PostSearchParams): Promise<ApiResponse<PageResponse<Post>>> {
  const requestParams: Record<string, unknown> = {
    keyword: params.keyword,
    page: params.pageNo || 1,
    size: params.pageSize || 10
  }
  if (params.types && params.types.length > 0) {
    requestParams.types = params.types.join(',')
  }
  if (params.timeRange && params.timeRange !== 'all') {
    requestParams.timeRange = params.timeRange
  }
  if (params.sortOption && params.sortOption !== 'time') {
    requestParams.sortOption = params.sortOption
  }
  return request({ url: '/post/search', method: 'get', params: requestParams })
}

export function getSearchSuggestions(params: { keyword?: string; limit?: number } = {}): Promise<ApiResponse<string[]>> {
  return request({
    url: '/post/search/suggestions',
    method: 'get',
    params: { keyword: params.keyword || '', limit: params.limit || 10 }
  })
}

export function getHotKeywords(params: { limit?: number } = {}): Promise<ApiResponse<string[]>> {
  return request({
    url: '/post/search/hot-keywords',
    method: 'get',
    params: { limit: params.limit || 10 }
  })
}

export function uploadPostCover(file: File): Promise<ApiResponse<{ url: string }>> {
  const formData = new FormData()
  formData.append('files', file)
  return request({ url: '/file/upload', method: 'post', data: formData })
}

export function deletePost(postId: number): Promise<ApiResponse> {
  return request({ url: `/post/${postId}/delete`, method: 'post' })
}

export function getMyPosts(params: PostListParams): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/post/my',
    method: 'post',
    data: {
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      status: params.status,
      keyword: params.keyword || null
    }
  })
}

export function getUserPosts(userId: number, params: PostListParams): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: `/post/user/${userId}`,
    method: 'post',
    data: {
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 10,
      status: params.status || 'PUBLISHED',
      type: params.type
    }
  })
}

export function getHotPosts(params: { pageNo?: number; pageSize?: number }): Promise<ApiResponse<PageResponse<Post>>> {
  return request({
    url: '/post/hot',
    method: 'post',
    data: { pageNo: params.pageNo || 1, pageSize: params.pageSize || 10 }
  })
}

export function increaseViewCount(postId: number): Promise<ApiResponse> {
  return request({ url: `/post/view?postId=${postId}`, method: 'get' })
}

export function getFavoriteRanking(params: { limit?: number }): Promise<ApiResponse<Post[]>> {
  return request({
    url: '/post/favorite/ranking',
    method: 'get',
    params: { limit: params.limit || 10 }
  })
}

export function getPostsByCursor(params: CursorParams = {}): Promise<ApiResponse<CursorResponse<Post>>> {
  return request({
    url: '/post/cursor',
    method: 'post',
    data: {
      cursor: params.cursor || null,
      pageSize: params.pageSize || 10,
      sortBy: params.sortBy || 'latest',
      tagId: params.tagId || null
    }
  })
}
