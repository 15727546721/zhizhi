package cn.xu.repository.read.elastic.service;

import cn.xu.model.entity.Post;
import cn.xu.repository.read.elastic.model.PostIndex;
import cn.xu.service.post.PostHotScorePolicy;

import java.time.LocalDateTime;

public class PostIndexConverter {

    public static PostIndex from(Post post) {
        PostIndex index = new PostIndex();
        index.setId(post.getId());
        index.setTitle(post.getTitle());
        index.setDescription(post.getDescription());
        index.setCoverUrl(post.getCoverUrl());
        index.setUserId(post.getUserId());
        index.setViewCount(post.getViewCount());
        index.setFavoriteCount(post.getFavoriteCount());
        index.setCommentCount(post.getCommentCount());
        index.setLikeCount(post.getLikeCount());
        index.setShareCount(post.getShareCount() != null ? post.getShareCount() : 0L);
        index.setIsFeatured(post.getIsFeatured() != null && post.getIsFeatured() == 1);
        // 使用createTime作为发布时间
        index.setPublishTime(post.getCreateTime());
        index.setUpdateTime(post.getUpdateTime());

        // 使用createTime计算热度
        LocalDateTime timeForHotScore = post.getCreateTime();
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