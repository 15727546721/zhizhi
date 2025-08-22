package cn.xu.domain.comment.service;

import cn.xu.api.web.model.dto.comment.FindReplyReq;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.api.web.model.dto.comment.FindCommentReq;
import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.user.model.entity.UserEntity;

import java.util.List;


/**
 * 评论服务接口
 */
public interface ICommentService {
    /**
     * 保存评论（包括发表评论和回复评论）
     *
     * @param request 评论请求参数
     */
    Long saveComment(CommentCreatedEvent request);

    /**
     * 获取评论列表（带预览）
     *
     * @param request
     * @return
     */
    List<CommentEntity> findCommentListWithPreview(FindCommentReq request);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 删除评论（管理员操作）
     *
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

    /**
     * 获取评论的子评论列表
     */
    List<CommentEntity> findChildCommentList(FindReplyReq request);
}
