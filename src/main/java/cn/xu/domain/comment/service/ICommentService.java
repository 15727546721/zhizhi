package cn.xu.domain.comment.service;

import cn.xu.api.web.model.dto.comment.CommentRequest;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author xuhongzu
 * @date 2024/03/16
 */
public interface ICommentService {
    /**
     * 保存评论（包括发表评论和回复评论）
     *
     * @param request 评论请求参数
     */
    void saveComment(CommentRequest request);

    /**
     * 分页获取评论列表
     *
     * @param type     评论类型
     * @param targetId 目标ID（文章ID或话题ID）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论列表（已构建好父子关系）
     */
    List<CommentEntity> getPagedComments(CommentType type, Long targetId, Integer pageNum, Integer pageSize);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 删除评论（管理员操作）
     * @param commentId
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    CommentEntity getCommentById(Long commentId);
}
