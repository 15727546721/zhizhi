package cn.xu.domain.comment.model.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CommentImageEntity {
    Long id;
    Long commentId;
    String imageUrl;
    Integer sortOrder;
    LocalDateTime createTime;
}