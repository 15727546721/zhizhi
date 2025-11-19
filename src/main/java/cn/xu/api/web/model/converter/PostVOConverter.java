package cn.xu.api.web.model.converter;

import cn.xu.api.web.model.vo.post.PostDetailResponse;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 帖子VO转换器，负责将领域对象转换为前端响应VO
 */
@Component
public class PostVOConverter {
    
    @Resource
    private UserVOConverter userVOConverter;

    /**
     * 将PostEntity转换为PostDetailResponse
     */
    public PostDetailResponse convertToPostDetailResponse(PostEntity post,
                                             UserEntity author, 
                                             String categoryName, 
                                             List<TagEntity> tags, 
                                             List<Long> topicIds,
                                             List<cn.xu.domain.post.model.entity.TopicEntity> topics,
                                             Long acceptedAnswerId,
                                             boolean isLiked, 
                                             boolean isFavorited, 
                                             boolean isAuthor, 
                                             boolean isFollowed) {
        if (post == null) {
            return null;
        }

        // 构建最终的PostDetailResponse
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle().getValue())
                .type(post.getType().getCode())
                .content(post.getContent().getValue())
                .author(userVOConverter.convertToUserResponse(author))  // 使用UserVOConverter转换作者信息
                .coverUrl(post.getCoverUrl())
                .categoryName(categoryName)
                .tags(tags != null ? tags : Collections.emptyList())
                .topics(topics != null ? topics : Collections.emptyList())
                .topicIds(topicIds != null ? topicIds : Collections.emptyList())
                .acceptedAnswerId(acceptedAnswerId)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .favoriteCount(post.getFavoriteCount())
                .shareCount(post.getShareCount())
                .status(post.getStatus().getCode())
                .isFeatured(post.isFeatured())
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .isAuthor(isAuthor)
                .isFollowed(isFollowed)
                .build();
    }
}