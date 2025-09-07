package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleContent;
import cn.xu.domain.article.model.valobj.ArticleTitle;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.infrastructure.persistent.po.Article;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 */
@Component
public class ArticleConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 文章领域实体
     * @return 文章持久化对象
     */
    public Article toDataObject(ArticleEntity entity) {
        if (entity == null) {
            return null;
        }

        return Article.builder()
                .id(entity.getId())
                .title(entity.getTitle() != null ? entity.getTitle().getValue() : null)
                .description(entity.getDescription())
                .content(entity.getContent() != null ? entity.getContent().getValue() : null)
                .coverUrl(entity.getCoverUrl())
                .userId(entity.getUserId())
                .categoryId(entity.getCategoryId())
                .viewCount(entity.getViewCount() != null ? entity.getViewCount() : 0L)
                .collectCount(entity.getCollectCount() != null ? entity.getCollectCount() : 0L)
                .commentCount(entity.getCommentCount() != null ? entity.getCommentCount() : 0L)
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount() : 0L)
                .status(entity.getStatus() != null ? entity.getStatus().getCode() : 0)
                .publishTime(entity.getPublishTime())
                .createTime(entity.getCreateTime() != null ? entity.getCreateTime() : LocalDateTime.now())
                .updateTime(entity.getUpdateTime() != null ? entity.getUpdateTime() : LocalDateTime.now())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param dataObject 文章持久化对象
     * @return 文章领域实体
     */
    public ArticleEntity toDomainEntity(Article dataObject) {
        if (dataObject == null) {
            return null;
        }

        return ArticleEntity.builder()
                .id(dataObject.getId())
                .title(dataObject.getTitle() != null ? new ArticleTitle(dataObject.getTitle()) : null)
                .description(dataObject.getDescription())
                .content(dataObject.getContent() != null ? new ArticleContent(dataObject.getContent()) : null)
                .coverUrl(dataObject.getCoverUrl())
                .userId(dataObject.getUserId())
                .categoryId(dataObject.getCategoryId())
                .viewCount(dataObject.getViewCount() != null ? dataObject.getViewCount() : 0L)
                .collectCount(dataObject.getCollectCount() != null ? dataObject.getCollectCount() : 0L)
                .commentCount(dataObject.getCommentCount() != null ? dataObject.getCommentCount() : 0L)
                .likeCount(dataObject.getLikeCount() != null ? dataObject.getLikeCount() : 0L)
                .status(dataObject.getStatus() != null ? ArticleStatus.fromCode(dataObject.getStatus()) : ArticleStatus.DRAFT)
                .publishTime(dataObject.getPublishTime())
                .createTime(dataObject.getCreateTime())
                .updateTime(dataObject.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象列表转换为领域实体列表
     *
     * @param dataObjects 持久化对象列表
     * @return 领域实体列表
     */
    public List<ArticleEntity> toDomainEntities(List<Article> dataObjects) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return new ArrayList<>();
        }

        return dataObjects.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将领域实体列表转换为持久化对象列表
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<Article> toDataObjects(List<ArticleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }

    /**
     * 更新持久化对象的部分字段（来自领域实体）
     *
     * @param target 目标持久化对象
     * @param source 源领域实体
     * @return 更新后的持久化对象
     */
    public Article updateDataObject(Article target, ArticleEntity source) {
        if (target == null || source == null) {
            return target;
        }

        if (source.getTitle() != null) {
            target.setTitle(source.getTitle().getValue());
        }

        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }

        if (source.getContent() != null) {
            target.setContent(source.getContent().getValue());
        }

        if (source.getCoverUrl() != null) {
            target.setCoverUrl(source.getCoverUrl());
        }

        if (source.getCategoryId() != null) {
            target.setCategoryId(source.getCategoryId());
        }

        if (source.getViewCount() != null) {
            target.setViewCount(source.getViewCount());
        }

        if (source.getCollectCount() != null) {
            target.setCollectCount(source.getCollectCount());
        }

        if (source.getCommentCount() != null) {
            target.setCommentCount(source.getCommentCount());
        }

        if (source.getLikeCount() != null) {
            target.setLikeCount(source.getLikeCount());
        }

        if (source.getStatus() != null) {
            target.setStatus(source.getStatus().getCode());
        }

        if (source.getPublishTime() != null) {
            target.setPublishTime(source.getPublishTime());
        }

        // 始终将更新时间设置为当前时间
        target.setUpdateTime(LocalDateTime.now());

        return target;
    }
}