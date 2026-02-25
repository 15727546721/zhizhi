import request from "@/utils/request";

/**
 * 获取帖子分页数据
 */
export function getPostPage(queryParams?: any) {
  return request({
    url: "/api/system/post/list",
    method: "get",
    params: queryParams,
  });
}

/**
 * 获取帖子详情
 */
export function getPostInfo(id?: any) {
  return request({
    url: "/api/system/post/info/" + id,
    method: "get",
  });
}

/**
 * 删除帖子
 */
export function deletePost(data: any) {
  return request({
    url: "/api/system/post/delete",
    method: "post",
    data,
  });
}

/**
 * 添加帖子
 */
export function addPost(data: any) {
  return request({
    url: "/api/system/post/add",
    method: "post",
    data,
  });
}

/**
 * 修改帖子
 */
export function updatePost(data: any) {
  return request({
    url: "/api/system/post/update",
    method: "post",
    data,
  });
}

/**
 * 随机一张封面图
 */
export function randomCover() {
  return request({
    url: "/api/system/post/randomImg",
    method: "get",
  });
}

/**
 * 置顶帖子
 */
export function topPost(data: any) {
  return request({
    url: "/api/system/post/top",
    method: "post",
    data,
  });
}

/**
 * 加精/取消加精
 */
export function togglePostFeatured(data: any) {
  return request({
    url: "/api/system/post/featured",
    method: "post",
    data,
  });
}

/**
 * 发布或下架
 */
export function togglePostStatus(data: any) {
  return request({
    url: "/api/system/post/status",
    method: "post",
    data,
  });
}

// 兼容旧代码
export const getArticlePage = getPostPage;
export const getArticleInfo = getPostInfo;
export const deleteArticle = deletePost;
export const addArticle = addPost;
export const updateArticle = updatePost;
export const topArticle = topPost;
