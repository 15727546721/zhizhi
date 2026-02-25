import request from "@/utils/request";
import { AxiosPromise } from "axios";
import { CommentDTO, CommentQuery, PageResponse, CommentReplyDTO } from "./types";

/**
 * 获取一级评论列表（分页）
 */
export function getComments(params: CommentQuery): AxiosPromise<PageResponse<CommentDTO[]>> {
  return request({
    url: "/api/system/comment/list",
    method: "get",
    params,
  });
}

/**
 * 获取二级评论列表（指定父评论的回复）
 */
export function getReplies(parentId: number, params: { pageNo: number; pageSize: number }): AxiosPromise<CommentReplyDTO[]> {
  return request({
    url: `/api/system/comment/replies/${parentId}`,
    method: "get",
    params: {
      pageNum: params.pageNo,
      pageSize: params.pageSize
    },
  });
}

/**
 * 删除评论（管理员）
 */
export function deleteComment(id: number): AxiosPromise<void> {
  return request({
    url: `/api/system/comment/delete/${id}`,
    method: "post",
  });
}
