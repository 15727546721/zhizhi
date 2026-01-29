package cn.xu.elasticsearch.converter;

import cn.xu.elasticsearch.model.PostIndex;
import cn.xu.model.entity.Post;
import cn.xu.service.post.PostHotScorePolicy;

import java.time.LocalDateTime;

/**
 * 帖子索引转换器
 * <p>负责 Post 实体与 PostIndex 索引模型之间的转换</p>
 */
public class PostIndexConverter {

    /**
     * 将 Post 实体转换为 PostIndex 索引模型
     */
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
        index.setIsFeatured(post.isFeaturedPost());
        index.setPublishTime(post.getCreateTime());
        index.setUpdateTime(post.getUpdateTime());

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

    /**
     * 将 PostIndex 索引模型转换为 Post 实体
     */
    public static Post toPost(PostIndex index) {
        return Post.builder()
                .id(index.getId())
                .title(index.getTitle())
                .description(index.getDescription())
                .coverUrl(index.getCoverUrl())
                .userId(index.getUserId())
                .viewCount(index.getViewCount() != null ? index.getViewCount() : 0L)
                .favoriteCount(index.getFavoriteCount() != null ? index.getFavoriteCount() : 0L)
                .commentCount(index.getCommentCount() != null ? index.getCommentCount() : 0L)
                .likeCount(index.getLikeCount() != null ? index.getLikeCount() : 0L)
                .shareCount(index.getShareCount() != null ? index.getShareCount() : 0L)
                .isFeatured(index.getIsFeatured() != null && index.getIsFeatured() ? 1 : 0)
                .createTime(index.getPublishTime())
                .updateTime(index.getUpdateTime() != null ? index.getUpdateTime() : index.getPublishTime())
                .status(Post.STATUS_PUBLISHED)
                .build();
    }
}
