package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.infrastructure.persistent.po.Post;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子转换器
 * 负责领域实体与持久化对象之间的转换
 */
@Component
public class PostConverter {

    /**
     * 将PostEntity转换为Post PO对象
     */
    public static Post toDataObject(PostEntity postEntity) {
        if (postEntity == null) {
            return null;
        }
        
        return Post.builder()
                .id(postEntity.getId())
                .userId(postEntity.getUserId())
                .categoryId(postEntity.getCategoryId())
                .status(postEntity.getStatusCode())
                .title(postEntity.getTitleValue())
                .description(postEntity.getDescription())
                .content(postEntity.getContentValue())
                .coverUrl(postEntity.getCoverUrl())
                .type(postEntity.getType() != null ? postEntity.getType().toString() : null)
                .isFeatured(postEntity.isFeatured() ? 1 : 0)
                .acceptedAnswerId(postEntity.getAcceptedAnswerId()) // 添加acceptedAnswerId字段
                .viewCount(postEntity.getViewCount() != null ? postEntity.getViewCount().longValue() : 0L)
                .likeCount(postEntity.getLikeCount() != null ? postEntity.getLikeCount().longValue() : 0L)
                .commentCount(postEntity.getCommentCount() != null ? postEntity.getCommentCount().longValue() : 0L)
                .collectCount(postEntity.getCollectCount() != null ? postEntity.getCollectCount().longValue() : 0L)
                .publishTime(postEntity.getPublishTime())
                .createTime(postEntity.getCreateTime())
                .updateTime(postEntity.getUpdateTime())
                .build();
    }

    /**
     * 将Post PO对象转换为PostEntity
     */
    public static PostEntity toDomainEntity(Post post) {
        if (post == null) {
            return null;
        }
        
        return PostEntity.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .categoryId(post.getCategoryId())
                .status(PostStatus.fromCode(post.getStatus()))
                .title(post.getTitle() != null ? new PostTitle(post.getTitle()) : null)
                .description(post.getDescription())
                .content(post.getContent() != null ? new PostContent(post.getContent()) : null)
                .coverUrl(post.getCoverUrl())
                .type(post.getType() != null ? PostType.fromCode(post.getType()) : null)
                .isFeatured(post.getIsFeatured() != null && post.getIsFeatured() == 1)
                .acceptedAnswerId(post.getAcceptedAnswerId()) // 添加acceptedAnswerId字段
                .viewCount(post.getViewCount() != null ? post.getViewCount().longValue() : 0L)
                .likeCount(post.getLikeCount() != null ? post.getLikeCount().longValue() : 0L)
                .commentCount(post.getCommentCount() != null ? post.getCommentCount().longValue() : 0L)
                .collectCount(post.getCollectCount() != null ? post.getCollectCount().longValue() : 0L)
                .publishTime(post.getPublishTime())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }

    /**
     * 批量将Post PO对象列表转换为PostEntity列表
     */
    public static List<PostEntity> toDomainEntities(List<Post> posts) {
        if (posts == null) {
            return null;
        }
        
        return posts.stream()
                .map(PostConverter::toDomainEntity)
                .collect(Collectors.toList());
    }
}