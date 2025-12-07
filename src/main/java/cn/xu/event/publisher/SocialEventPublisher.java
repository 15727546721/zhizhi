package cn.xu.event.publisher;

import cn.xu.event.core.BaseEvent.EventAction;
import cn.xu.event.core.EventBus;
import cn.xu.event.events.CommentEvent;
import cn.xu.event.events.FavoriteEvent;
import cn.xu.event.events.FollowEvent;
import cn.xu.event.events.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 社交事件发布器
 * 
 * <p>负责发布社交类事件：
 * <ul>
 *   <li>评论事件</li>
 *   <li>点赞事件</li>
 *   <li>关注事件</li>
 *   <li>收藏事件</li>
 * </ul>
 *
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocialEventPublisher {
    
    private final EventBus eventBus;
    
    // ==================== 评论事件 ====================
    
    /**
     * 发布评论创建事件
     */
    public void publishCommentCreated(Long userId, Long postId, Long commentId, String content) {
        eventBus.publish(CommentEvent.builder()
                .operatorId(userId)
                .action(EventAction.CREATE)
                .postId(postId)
                .commentId(commentId)
                .content(content)
                .rootComment(true)
                .build());
    }
    
    /**
     * 发布回复评论事件
     */
    public void publishReplyCreated(Long userId, Long postId, Long commentId, Long parentId, 
                                    Long replyUserId, String content) {
        eventBus.publish(CommentEvent.builder()
                .operatorId(userId)
                .action(EventAction.CREATE)
                .postId(postId)
                .commentId(commentId)
                .parentId(parentId)
                .replyUserId(replyUserId)
                .content(content)
                .rootComment(false)
                .build());
    }
    
    /**
     * 发布评论删除事件
     */
    public void publishCommentDeleted(Long userId, Long postId, Long commentId) {
        eventBus.publish(CommentEvent.builder()
                .operatorId(userId)
                .action(EventAction.DELETE)
                .postId(postId)
                .commentId(commentId)
                .build());
    }
    
    // ==================== 点赞事件 ====================
    
    /**
     * 发布帖子点赞事件
     */
    public void publishPostLiked(Long userId, Long postId) {
        eventBus.publish(LikeEvent.likePost(userId, postId));
    }
    
    /**
     * 发布帖子取消点赞事件
     */
    public void publishPostUnliked(Long userId, Long postId) {
        eventBus.publish(LikeEvent.unlikePost(userId, postId));
    }
    
    /**
     * 发布评论点赞事件
     */
    public void publishCommentLiked(Long userId, Long commentId) {
        eventBus.publish(LikeEvent.likeComment(userId, commentId));
    }
    
    /**
     * 发布评论取消点赞事件
     */
    public void publishCommentUnliked(Long userId, Long commentId) {
        eventBus.publish(LikeEvent.unlikeComment(userId, commentId));
    }
    
    // ==================== 关注事件 ====================
    
    /**
     * 发布关注事件
     */
    public void publishFollowed(Long followerId, Long followeeId) {
        eventBus.publish(FollowEvent.follow(followerId, followeeId));
    }
    
    /**
     * 发布取消关注事件
     */
    public void publishUnfollowed(Long followerId, Long followeeId) {
        eventBus.publish(FollowEvent.unfollow(followerId, followeeId));
    }
    
    // ==================== 收藏事件 ====================
    
    /**
     * 发布收藏事件
     */
    public void publishFavorited(Long userId, Long postId) {
        eventBus.publish(FavoriteEvent.favorite(userId, postId));
    }
    
    /**
     * 发布取消收藏事件
     */
    public void publishUnfavorited(Long userId, Long postId) {
        eventBus.publish(FavoriteEvent.unfavorite(userId, postId));
    }
}
