import request from "@/utils/request";
import type { FileResponse } from "./types";

/**
 * 文件上传
 * @param file 文件对象
 * @param fileName 文件名(可选)
 * @returns
 */
export function uploadFile(file: File, fileName?: string) {
  const formData = new FormData();
  formData.append("files", file);
  if (fileName) {
    formData.append("fileName", fileName);
  }
  return request<FileResponse>({
    url: "/api/system/file/upload",
    method: "post",
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 删除文件
 * @param fileUrl 文件URL
 * @returns
 */
export function deleteFile(fileUrl: string) {
  return request<FileResponse>({
    url: "/api/system/file/delete",
    method: "post",
    data: fileUrl,
  });
}
