package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleHotScorePolicy;
import cn.xu.infrastructure.persistent.read.elastic.model.ArticleIndex;

public class ArticleIndexConverter {

    public static ArticleIndex from(ArticleEntity article) {
        ArticleIndex index = new ArticleIndex();
        index.setId(article.getId());
        index.setTitle(article.getTitleValue());
        index.setDescription(article.getDescription());
        index.setCoverUrl(article.getCoverUrl());
        index.setUserId(article.getUserId());
        index.setCategoryId(article.getCategoryId());
        index.setViewCount(article.getViewCount());
        index.setCollectCount(article.getCollectCount());
        index.setCommentCount(article.getCommentCount());
        index.setLikeCount(article.getLikeCount());
        index.setPublishTime(article.getCreateTime());
        index.setUpdateTime(article.getUpdateTime());

        double hotScore = ArticleHotScorePolicy.calculate(
                article.getLikeCount(), article.getCollectCount(),
                article.getCommentCount(), article.getCreateTime());

        index.setHotScore(hotScore);
        return index;
    }
}
