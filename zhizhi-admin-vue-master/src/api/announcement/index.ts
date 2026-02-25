import request from "@/utils/request";
import { AxiosPromise } from "axios";

/** 公告VO */
export interface AnnouncementVO {
  id: number;
  title: string;
  content: string;
  type: number;        // 0-普通 1-活动 2-系统 3-更新
  status: number;      // 0-草稿 1-已发布 2-已下架
  isTop: number;       // 0-否 1-是
  publisherId: number | null;
  publishTime: string | null;
  createTime: string;
  updateTime: string;
}

/** 公告查询参数 */
export interface AnnouncementQuery {
  pageNo: number;
  pageSize: number;
  title?: string;
  type?: number;
  status?: number;
}

/** 公告表单 */
export interface AnnouncementForm {
  id?: number;
  title: string;
  content: string;
  type: number;
  status: number;
  isTop: number;
}

/** 分页响应 */
export interface PageResponse<T> {
  pageNo: number;
  pageSize: number;
  total: number;
  data: T;
}

/**
 * 获取公告分页列表
 */
export function getAnnouncementPage(params: AnnouncementQuery): AxiosPromise<PageResponse<AnnouncementVO[]>> {
  return request({
    url: "/api/system/announcements",
    method: "get",
    params,
  });
}

/**
 * 获取公告详情
 */
export function getAnnouncementDetail(id: number): AxiosPromise<AnnouncementVO> {
  return request({
    url: `/api/system/announcements/${id}`,
    method: "get",
  });
}

/**
 * 新增公告
 */
export function addAnnouncement(data: AnnouncementForm): AxiosPromise<void> {
  return request({
    url: "/api/system/announcements",
    method: "post",
    data,
  });
}

/**
 * 更新公告
 */
export function updateAnnouncement(data: AnnouncementForm): AxiosPromise<void> {
  return request({
    url: `/api/system/announcements/${data.id}/update`,
    method: "post",
    data,
  });
}

/**
 * 删除公告（支持批量）
 */
export function deleteAnnouncement(ids: number[]): AxiosPromise<void> {
  return request({
    url: "/api/system/announcements/batch-delete",
    method: "post",
    data: ids,
  });
}

/**
 * 发布公告
 */
export function publishAnnouncement(id: number): AxiosPromise<void> {
  return request({
    url: `/api/system/announcements/${id}/publish`,
    method: "post",
  });
}

/**
 * 下架公告
 */
export function unpublishAnnouncement(id: number): AxiosPromise<void> {
  return request({
    url: `/api/system/announcements/${id}/offline`,
    method: "post",
  });
}

/**
 * 置顶/取消置顶公告
 */
export function toggleTopAnnouncement(id: number, isTop: number): AxiosPromise<void> {
  return request({
    url: `/api/system/announcements/${id}/top`,
    method: "post",
    params: { isTop },
  });
}
