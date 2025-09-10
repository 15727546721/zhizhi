package cn.xu.domain.comment.repository;


import cn.xu.domain.comment.model.entity.CommentImageEntity;
import cn.xu.infrastructure.persistent.po.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    // 保存评论（返回带ID的聚合）
    Comment save(Comment comment);

    // 根据ID查找评论（包含图片）
    Optional<Comment> findById(Long id);

    // 批量查询评论（基础信息）
    List<Comment> findByIds(List<Long> ids);

    // 保存图片列表
    void saveImages(List<CommentImageEntity> images);

    // 根据评论ID查询图片
    List<CommentImageEntity> findImagesByCommentId(Long commentId);

}
