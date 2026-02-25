import request from "@/utils/request";
import type { ReportQueryParams, HandleReportRequest, ReportDetail, ReportStats } from "./types";

/**
 * 获取举报列表
 */
export function getReportList(params: ReportQueryParams) {
  return request({
    url: "/api/system/reports",
    method: "get",
    params,
  });
}

/**
 * 获取举报详情
 */
export function getReportDetail(id: number) {
  return request<ReportDetail>({
    url: `/api/system/reports/${id}`,
    method: "get",
  });
}

/**
 * 处理举报
 */
export function handleReport(id: number, data: HandleReportRequest) {
  return request({
    url: `/api/system/reports/${id}/handle`,
    method: "post",
    data,
  });
}

/**
 * 批量忽略举报
 */
export function batchIgnoreReports(ids: number[]) {
  return request({
    url: "/api/system/reports/batch-ignore",
    method: "post",
    data: ids,
  });
}

/**
 * 获取举报统计
 */
export function getReportStats() {
  return request<ReportStats>({
    url: "/api/system/reports/stats",
    method: "get",
  });
}
