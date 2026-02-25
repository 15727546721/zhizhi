import request from '@/utils/request'
import type { ApiResponse } from '@/types'

interface UploadResult {
  url: string
  urls?: string[]
}

export function uploadFile(file: File): Promise<ApiResponse<UploadResult>> {
  const formData = new FormData()
  formData.append('files', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadFiles(files: File[]): Promise<ApiResponse<UploadResult>> {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteFiles(fileUrls: string[]): Promise<ApiResponse> {
  return request({ url: '/file/deleteBatch', method: 'post', data: { fileUrls } })
}

export function uploadImage(file: File): Promise<ApiResponse<UploadResult>> {
  if (!file.type.startsWith('image/')) {
    return Promise.reject(new Error('只能上传图片文件'))
  }
  if (file.size > 5 * 1024 * 1024) {
    return Promise.reject(new Error('图片大小不能超过5MB'))
  }
  return uploadFile(file)
}

export function uploadImages(files: File[]): Promise<ApiResponse<UploadResult>> {
  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      return Promise.reject(new Error('只能上传图片文件'))
    }
    if (file.size > 5 * 1024 * 1024) {
      return Promise.reject(new Error('图片大小不能超过5MB'))
    }
  }
  return uploadFiles(files)
}
