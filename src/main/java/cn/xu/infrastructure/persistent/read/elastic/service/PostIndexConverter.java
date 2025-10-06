package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostHotScorePolicy;
import cn.xu.infrastructure.persistent.read.elastic.model.PostIndex;

public class PostIndexConverter {

    public static PostIndex from(PostEntity post) {
        PostIndex index = new PostIndex();
        index.setId(post.getId());
        index.setTitle(post.getTitleValue());
        index.setDescription(post.getDescription());
        index.setCoverUrl(post.getCoverUrl());
        index.setUserId(post.getUserId());
        index.setCategoryId(post.getCategoryId());
        index.setViewCount(post.getViewCount());
        index.setCollectCount(post.getCollectCount());
        index.setCommentCount(post.getCommentCount());
        index.setLikeCount(post.getLikeCount());
        index.setPublishTime(post.getCreateTime());
        index.setUpdateTime(post.getUpdateTime());

        double hotScore = PostHotScorePolicy.calculate(
                post.getLikeCount() != null ? post.getLikeCount() : 0L,
                post.getCommentCount() != null ? post.getCommentCount() : 0L,
                post.getViewCount() != null ? post.getViewCount() : 0L,
                post.getCreateTime()
        );

        index.setHotScore(hotScore);
        return index;
    }
}