package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentContent;
import cn.xu.infrastructure.persistent.po.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论实体转换器（防腐层）
 * 负责领域实体与持久化对象之间的转换，遵循DDD原则
 */
@Slf4j
@Component
public class CommentConverter {

    /**
     * 领域实体转换为持久化对象
     * @param entity 评论领域实体
     * @return 持久化对象
     */
    public Comment toDataObject(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Comment.CommentBuilder builder = Comment.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .userId(entity.getUserId())
                .replyUserId(entity.getReplyUserId())
                .content(entity.getContent() != null ? entity.getContent().getValue() : null)
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount() : 0L)
                .replyCount(entity.getReplyCount() != null ? entity.getReplyCount() : 0L)
                .hotScore(entity.getHotScore() != 0 ? BigDecimal.valueOf(entity.getHotScore()) : BigDecimal.ZERO)
                .createTime(entity.getCreateTime() != null ? entity.getCreateTime() : LocalDateTime.now())
                .updateTime(entity.getUpdateTime() != null ? entity.getUpdateTime() : LocalDateTime.now());

        // 处理图片URL列表转换
        List<String> imageUrls = entity.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String imageUrlStr = String.join(",", imageUrls);
            builder.imageUrl(imageUrlStr);
        }

        return builder.build();
    }

    /**
     * 持久化对象转换为领域实体
     * @param dataObject 持久化对象
     * @return 评论领域实体
     */
    public CommentEntity toDomainEntity(Comment dataObject) {
        if (dataObject == null) {
            return null;
        }
        
        // 解析图片URL字符串为列表
        List<String> imageUrls = parseImageUrls(dataObject.getImageUrl());
        
        // 处理可能为空或无效的评论内容
        CommentContent commentContent = null;
        if (dataObject.getContent() != null && !dataObject.getContent().trim().isEmpty()) {
            try {
                commentContent = new CommentContent(dataObject.getContent());
            } catch (Exception e) {
                // 如果内容验证失败，记录日志并使用默认内容
                log.warn("评论内容验证失败，ID: {}, 内容: {}, 使用默认内容", dataObject.getId(), dataObject.getContent());
                try {
                    commentContent = new CommentContent("默认评论内容");
                } catch (Exception ex) {
                    // 如果默认内容也失败，设置为null
                    log.error("创建默认评论内容失败，ID: {}", dataObject.getId());
                    commentContent = null;
                }
            }
        }
        
        return CommentEntity.builder()
                .id(dataObject.getId())
                .userId(dataObject.getUserId())
                .targetType(dataObject.getTargetType())
                .targetId(dataObject.getTargetId())
                .content(commentContent)
                .parentId(dataObject.getParentId())
                .replyUserId(dataObject.getReplyUserId())
                .likeCount(dataObject.getLikeCount() != null ? dataObject.getLikeCount() : 0L)
                .replyCount(dataObject.getReplyCount() != null ? dataObject.getReplyCount() : 0L)
                .hotScore(dataObject.getHotScore() != null ? dataObject.getHotScore().doubleValue() : 0.0)
                .imageUrls(imageUrls)
                .createTime(dataObject.getCreateTime())
                .updateTime(dataObject.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象为领域实体
     * @param dataObjects 持久化对象列表
     * @return 领域实体列表
     */
    public List<CommentEntity> toDomainEntities(List<Comment> dataObjects) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return new ArrayList<>();
        }
        
        return dataObjects.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 解析图片URL字符串为列表
     * @param imageUrlStr 逗号分隔的URL字符串
     * @return URL列表
     */
    private List<String> parseImageUrls(String imageUrlStr) {
        List<String> imageUrls = new ArrayList<>();
        if (imageUrlStr != null && !imageUrlStr.trim().isEmpty()) {
            String[] urls = imageUrlStr.split(",");
            for (String url : urls) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    imageUrls.add(trimmedUrl);
                }
            }
        }
        return imageUrls;
    }
}