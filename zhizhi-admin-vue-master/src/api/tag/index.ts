import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { TagForm, TagPageVO, TagQuery } from "./types";

/**
 * 获取标签分页数据
 *
 * @param queryParams
 */
export function getTagPage(queryParams?: TagQuery): AxiosPromise<TagPageVO> {
  return request({
    url: "/api/system/tag/list",
    method: "get",
    params: queryParams,
  });
}

/**
 * 修改标签
 *
 * @param data
 */
export function updateTag(data?: TagForm): AxiosPromise<any> {
  return request({
    url: "/api/system/tag/update",
    method: "post",
    data,
  });
}

/**
 * 添加标签
 *
 * @param data
 */
export function addTag(data?: TagForm): AxiosPromise<any> {
  return request({
    url: "/api/system/tag/add",
    method: "post",
    data,
  });
}

/**
 * 删除标签
 *
 * @param data
 */
export function deleteTag(data?: Number[]): AxiosPromise<any> {
  return request({
    url: "/api/system/tag/delete",
    method: "post",
    data,
  });
}

/**
 * 置顶标签
 *
 * @param data
 */
export function topTag(tagId?: Number): AxiosPromise<any> {
  return request({
    url: "/api/system/tag/top",
    method: "get",
    params: {
      id: tagId,
    },
  });
}
