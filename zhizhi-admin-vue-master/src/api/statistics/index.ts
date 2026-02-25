import request from "@/utils/request";
import { AxiosPromise } from "axios";

/** 仪表盘统计数据 */
export interface DashboardStats {
  totalPosts: number;
  totalUsers: number;
  totalComments: number;
  totalTags: number;
  todayPosts: number;
  todayUsers: number;
  todayComments: number;
  yesterdayPosts: number;
  yesterdayComments: number;
  pendingReports: number;
  pendingFeedbacks: number;
  onlineUserCount: number;
}

/** 热门帖子 */
export interface HotPostVO {
  id: number;
  title: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  authorName: string;
  yesterdayViewCount: number;
  growthRate: number; // 环比增长率
}

/** 趋势数据 */
export interface TrendData {
  dates: string[];
  postCounts: number[];
  userCounts: number[];
  commentCounts: number[];
}

/** UV/PV统计数据 */
export interface VisitStats {
  todayUV: number;
  yesterdayUV: number;
  totalUV: number;
  todayPV: number;
  yesterdayPV: number;
  totalPV: number;
}

/** UV/PV趋势数据 */
export interface VisitTrendData {
  dates: string[];
  uvCounts: number[];
  pvCounts: number[];
}

/**
 * 获取仪表盘统计数据
 */
export function getDashboardStats(): AxiosPromise<DashboardStats> {
  return request({
    url: "/api/system/statistics/dashboard",
    method: "get",
  });
}

/**
 * 获取数据趋势
 */
export function getTrend(days: number = 7): AxiosPromise<TrendData> {
  return request({
    url: "/api/system/statistics/trend",
    method: "get",
    params: { days },
  });
}

/**
 * 获取热门帖子排行
 */
export function getHotPosts(limit: number = 10): AxiosPromise<HotPostVO[]> {
  return request({
    url: "/api/system/statistics/hot-posts",
    method: "get",
    params: { limit },
  });
}

/**
 * 获取活跃用户排行
 */
export function getActiveUsers(limit: number = 10): AxiosPromise<any[]> {
  return request({
    url: "/api/system/statistics/active-users",
    method: "get",
    params: { limit },
  });
}

/**
 * 获取热门标签排行
 */
export function getHotTags(limit: number = 10): AxiosPromise<any[]> {
  return request({
    url: "/api/system/statistics/hot-tags",
    method: "get",
    params: { limit },
  });
}

/**
 * 获取内容类型分布
 */
export function getContentDistribution(): AxiosPromise<any[]> {
  return request({
    url: "/api/system/statistics/content-distribution",
    method: "get",
  });
}

/**
 * 获取UV/PV统计数据
 */
export function getVisitStats(): AxiosPromise<VisitStats> {
  return request({
    url: "/api/system/statistics/visit",
    method: "get",
  });
}

/**
 * 获取UV/PV趋势数据
 */
export function getVisitTrend(days: number = 7): AxiosPromise<VisitTrendData> {
  return request({
    url: "/api/system/statistics/visit-trend",
    method: "get",
    params: { days },
  });
}
