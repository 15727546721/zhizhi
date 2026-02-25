import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 修改
 *
 * @param data
 */
export function updateWebConfig(data: any): AxiosPromise<any> {
  return request({
    url: "/api/system/webConfig/update",
    method: "post",
    data,
  });
}

/**
 * 网站信息
 *
 */
export function getWebConfig(): AxiosPromise<any> {
  return request({
    url: "/api/system/webConfig/",
    method: "get",
  });
}
