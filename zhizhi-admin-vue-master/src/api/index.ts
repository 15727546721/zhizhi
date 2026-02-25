import request from "@/utils/request";
import { AxiosPromise } from "axios";

/**
 * 获取首页统计数据
 */
export function getDashboardBottomStatistics() {
  return request({
    url: "/api/system/home/getDashboardBottomStatistics",
    method: "get",
  });
}

/**
 * 获取首页统计数据
 */
export function getDashboardTopStatistics() {
  return request({
    url: "/api/system/home/getDashboardTopStatistics",
    method: "get",
  });
}

/**
 * 获取服务监控数据
 */
export function getSystemInfo() {
  return request({
    url: "/api/system/home/systemInfo",
    method: "get",
  });
}

/**
 * 获取缓存监控数据
 */
export function cacheInfo() {
  return request({
    url: "/api/system/home/cache",
    method: "get",
  });
}
