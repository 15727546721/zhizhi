import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 获取分页数据
 *
 * @param queryParams
 */
export function getJobPage(queryParams?: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/list",
    method: "get",
    params: queryParams,
  });
}

/**
 * 添加
 *
 * @param data
 */
export function addJob(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/add",
    method: "post",
    data,
  });
}

/**
 * 修改
 *
 * @param data
 */
export function updateJob(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/update",
    method: "post",
    data,
  });
}

/**
 * 删除
 *
 * @param data
 */
export function deleteJob(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/delete",
    method: "post",
    data,
  });
}

/**
 * 改变状态
 *
 * @param data
 */
export function changeStatus(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/change",
    method: "POST",
    data,
  });
}

/**
 * 立即执行
 *
 * @param data
 */
export function runJob(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/run",
    method: "POST",
    data,
  });
}

/**
 * 任务详情
 *
 * @param id
 */
export function infoJob(id: any): AxiosPromise<any> {
  return request({
    url: "/api/system/job/info",
    method: "get",
    params: {
      jobId: id,
    },
  });
}
