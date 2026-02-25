import request from "@/utils/request";
import { AxiosPromise } from "axios";

// ==================== 类型定义 ====================

/** 反馈类型 */
export const FeedbackType = {
  BUG: 0,
  SUGGESTION: 1,
  CONTENT: 2,
  OTHER: 3
};

export const FeedbackTypeOptions = [
  { value: 0, label: "Bug问题" },
  { value: 1, label: "功能建议" },
  { value: 2, label: "内容问题" },
  { value: 3, label: "其他" }
];

/** 反馈状态 */
export const FeedbackStatus = {
  PENDING: 0,
  PROCESSING: 1,
  RESOLVED: 2,
  CLOSED: 3
};

export const FeedbackStatusOptions = [
  { value: 0, label: "待处理", type: "warning" },
  { value: 1, label: "处理中", type: "primary" },
  { value: 2, label: "已解决", type: "success" },
  { value: 3, label: "已关闭", type: "info" }
];

// ==================== API ====================

/**
 * 获取反馈列表
 */
export function getFeedBackPage(queryParams?: any): AxiosPromise<any> {
  return request({
    url: "/api/system/feedback/list",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取反馈详情
 */
export function getFeedbackDetail(id: number): AxiosPromise<any> {
  return request({
    url: "/api/system/feedback/detail",
    method: "get",
    params: { id },
  });
}

/**
 * 回复反馈
 */
export function replyFeedback(data: { id: number; reply: string }): AxiosPromise<any> {
  return request({
    url: "/api/system/feedback/reply",
    method: "post",
    data,
  });
}

/**
 * 修改反馈状态
 */
export function updateFeedbackStatus(data: { id: number; status: number }): AxiosPromise<any> {
  return request({
    url: "/api/system/feedback/status",
    method: "post",
    data,
  });
}

/**
 * 删除反馈
 */
export function deleteFeedBack(ids: number[]): AxiosPromise<any> {
  return request({
    url: "/api/system/feedback/delete",
    method: "post",
    data: { ids },
  });
}
