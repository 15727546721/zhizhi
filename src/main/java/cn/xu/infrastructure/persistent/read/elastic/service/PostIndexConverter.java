package cn.xu.infrastructure.persistent.read.elastic.service;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostHotScorePolicy;
import cn.xu.infrastructure.persistent.read.elastic.model.PostIndex;

import java.time.LocalDateTime;

public class PostIndexConverter {

    public static PostIndex from(PostEntity post) {
        PostIndex index = new PostIndex();
        index.setId(post.getId());
        index.setTitle(post.getTitleValue());
        index.setDescription(post.getDescription());
        index.setCoverUrl(post.getCoverUrl());
        index.setUserId(post.getUserId());
        index.setCategoryId(post.getCategoryId());
        index.setType(post.getType() != null ? post.getType().getCode() : null);
        index.setViewCount(post.getViewCount());
        index.setFavoriteCount(post.getFavoriteCount());
        index.setCommentCount(post.getCommentCount());
        index.setLikeCount(post.getLikeCount());
        index.setShareCount(post.getShareCount() != null ? post.getShareCount() : 0L);
        index.setIsFeatured(post.getIsFeatured() != null ? post.getIsFeatured() : false);
        // 使用publishTime，如果publishTime为null则使用createTime（兼容旧数据）
        index.setPublishTime(post.getPublishTime() != null ? post.getPublishTime() : post.getCreateTime());
        index.setUpdateTime(post.getUpdateTime());

        // 使用publishTime计算热度，如果publishTime为null则使用createTime
        LocalDateTime timeForHotScore = post.getPublishTime() != null ? post.getPublishTime() : post.getCreateTime();
        double hotScore = PostHotScorePolicy.calculate(
                post.getLikeCount() != null ? post.getLikeCount() : 0L,
                post.getCommentCount() != null ? post.getCommentCount() : 0L,
                post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L,
                timeForHotScore
        );

        index.setHotScore(hotScore);
        return index;
    }
}