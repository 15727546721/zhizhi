import request from "@/utils/request";
import { AxiosPromise } from "axios";

/** 文件信息VO */
export interface FileVO {
  fileName: string;
  fileUrl: string;
  fileSize: number;
  fileSizeStr: string;
  fileType: string;
  lastModified: string;
}

/** 文件查询参数 */
export interface FileQuery {
  pageNo: number;
  pageSize: number;
  prefix?: string;
}

/**
 * 获取文件列表
 */
export function getFileList(params: FileQuery): AxiosPromise<any> {
  return request({
    url: "/api/system/file/list",
    method: "get",
    params,
  });
}

/**
 * 上传文件
 */
export function upload(data: any) {
  return request({
    url: "/api/system/file/upload",
    method: "POST",
    headers: { "Content-Type": "multipart/form-data" },
    data,
  });
}

// 别名导出，兼容旧代码
export const uploadFile = upload;

/**
 * 删除文件
 * @param fileUrl 文件URL
 */
export function deleteFile(fileUrl: string) {
  return request({
    url: "/api/system/file/delete",
    method: "POST",
    data: fileUrl,
    headers: { "Content-Type": "application/json" },
  });
}

// 别名导出，兼容旧代码
export const delBatchFile = deleteFile;

/**
 * 批量删除文件
 * @param fileUrls 文件URL列表
 */
export function batchDeleteFiles(fileUrls: string[]): AxiosPromise<void> {
  return request({
    url: "/api/system/file/batchDelete",
    method: "post",
    data: fileUrls,
  });
}

/**
 * 下载文件
 * @param fileName 文件名
 */
export function downloadFile(fileName: string) {
  return request({
    url: `/api/system/file/download/${encodeURIComponent(fileName)}`,
    method: "GET",
    responseType: "blob",
  });
}
