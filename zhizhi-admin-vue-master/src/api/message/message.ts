import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { PageResponse } from "./types";

/** 系统消息VO */
export interface SystemMessageVO {
  id: number;
  title: string;
  content: string;
  type: number;          // 0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注 6-@提及
  isRead: number;        // 0-未读 1-已读
  senderId: number | null;
  senderType: number;    // 0-系统 1-用户
  receiverId: number;
  businessType: number;  // 0-系统 1-帖子 2-评论 3-用户
  businessId: number | null;
  createTime: string;
  readTime: string | null;
}

/** 消息查询参数 */
export interface MessageQuery {
  pageNo: number;
  pageSize: number;
  type?: number;
  isRead?: number;
}

/**
 * 获取系统消息列表
 */
export function getMessagePage(params: MessageQuery): AxiosPromise<PageResponse<SystemMessageVO[]>> {
  return request({
    url: "/api/system/message/list",
    method: "get",
    params,
  });
}

/**
 * 删除系统消息
 */
export function deleteMessage(ids: number[]): AxiosPromise<void> {
  return request({
    url: "/api/system/message/delete",
    method: "post",
    data: ids,
  });
}

/** 发送通知请求 */
export interface SendNotificationRequest {
  title: string;
  content: string;
  receiverIds: number[];
}

/**
 * 发送系统通知
 */
export function sendNotification(data: SendNotificationRequest): AxiosPromise<void> {
  return request({
    url: "/api/system/message/send",
    method: "post",
    data,
  });
}

/** 用户简要信息 */
export interface UserSimpleVO {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
}

/**
 * 搜索用户（用于发送通知时选择用户）
 */
export function searchUsersForMessage(keyword: string): AxiosPromise<UserSimpleVO[]> {
  return request({
    url: "/api/system/message/user/search",
    method: "get",
    params: { keyword },
  });
}
