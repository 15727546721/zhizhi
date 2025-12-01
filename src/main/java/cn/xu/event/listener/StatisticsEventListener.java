package cn.xu.event.listener;

import cn.xu.event.comment.CommentEvent;
import cn.xu.event.follow.FollowEvent;
import cn.xu.event.like.LikeEvent;
import cn.xu.event.post.PostEvent;
import cn.xu.event.user.UserRegisteredEvent;
import cn.xu.model.entity.PostStatistics;
import cn.xu.model.entity.UserStatistics;
import cn.xu.repository.impl.PostStatisticsRepository;
import cn.xu.repository.impl.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 统计数据事件监听器
 * 监听各种业务事件，实时更新统计表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsEventListener {
    
    private final PostStatisticsRepository postStatisticsRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    
    // ========== 帖子相关事件 ==========
    
    /**
     * 监听帖子事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onPostEvent(PostEvent event) {
        try {
            if (event.getEventType() == PostEvent.PostEventType.CREATED) {
                // 1. 创建帖子统计记录
                PostStatistics stats = PostStatistics.createNew(event.getPostId());
                postStatisticsRepository.save(stats);
                
                // 2. 增加用户发帖数
                userStatisticsRepository.incrementPostCount(event.getUserId());
                
                log.info("帖子统计已创建: postId={}, userId={}", event.getPostId(), event.getUserId());
            } else if (event.getEventType() == PostEvent.PostEventType.DELETED) {
                // 1. 删除帖子统计记录
                postStatisticsRepository.deleteByPostId(event.getPostId());
                
                // 2. 减少用户发帖数
                if (event.getUserId() != null) {
                    userStatisticsRepository.decrementPostCount(event.getUserId());
                }
                
                log.info("帖子统计已删除: postId={}", event.getPostId());
            }
        } catch (Exception e) {
            log.error("处理帖子事件失败: postId={}, eventType={}", event.getPostId(), event.getEventType(), e);
        }
    }
    
    
    // ========== 评论相关事件 ==========
    
    /**
     * 监听评论事件
     * targetType: 1=帖子 2=随笔 等
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onCommentEvent(CommentEvent event) {
        try {
            if (event.getAction() == CommentEvent.CommentAction.CREATED) {
                // 1. 如果是帖子评论(targetType=1)，增加帖子评论数
                if (event.getTargetType() != null && event.getTargetType() == 1) {
                    postStatisticsRepository.incrementCommentCount(event.getTargetId());
                }
                
                // 2. 增加用户评论数
                if (event.getUserId() != null) {
                    userStatisticsRepository.incrementCommentCount(event.getUserId());
                }
                
                log.info("评论统计已更新: commentId={}, targetType={}, targetId={}", 
                    event.getCommentId(), event.getTargetType(), event.getTargetId());
            } else if (event.getAction() == CommentEvent.CommentAction.DELETED) {
                // 如果是帖子评论(targetType=1)，减少帖子评论数
                if (event.getTargetType() != null && event.getTargetType() == 1) {
                    postStatisticsRepository.decrementCommentCount(event.getTargetId());
                }
                
                log.info("评论统计已更新（删除）: commentId={}, targetType={}, targetId={}", 
                    event.getCommentId(), event.getTargetType(), event.getTargetId());
            }
        } catch (Exception e) {
            log.error("处理评论事件失败: commentId={}, action={}", event.getCommentId(), event.getAction(), e);
        }
    }
    
    
    // ========== 点赞相关事件 ==========
    
    /**
     * 监听点赞事件
     * LikeType是枚举类型：POST, COMMENT等
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onLike(LikeEvent event) {
        try {
            // 只处理帖子点赞 (LikeType.POST)
            if (event.getType() != null && "POST".equals(event.getType().name())) {
                if (event.getStatus() != null && event.getStatus()) {
                    // 点赞
                    postStatisticsRepository.incrementLikeCount(event.getTargetId());
                } else {
                    // 取消点赞
                    postStatisticsRepository.decrementLikeCount(event.getTargetId());
                }
                
                log.info("点赞统计已更新: targetId={}, status={}", 
                    event.getTargetId(), event.getStatus());
            }
        } catch (Exception e) {
            log.error("处理点赞事件失败: targetId={}", event.getTargetId(), e);
        }
    }
    
    // ========== 收藏相关事件 ==========
    // 注意：项目中暂时没有FavoriteEvent，如果需要监听收藏事件
    // 请先在favorite模块创建FavoriteEvent，然后取消下面代码的注释
    
    /*
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onFavorite(FavoriteEvent event) {
        try {
            if (event.getStatus() == 1) {
                postStatisticsRepository.incrementFavoriteCount(event.getTargetId());
            } else {
                postStatisticsRepository.decrementFavoriteCount(event.getTargetId());
            }
        } catch (Exception e) {
            log.error("处理收藏事件失败", e);
        }
    }
    */
    
    // ========== 关注相关事件 ==========
    
    /**
     * 监听关注事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onFollow(FollowEvent event) {
        try {
            // FollowStatus.FOLLOWING = 关注, FollowStatus.UNFOLLOWED = 取消关注
            if (event.getStatus() != null && event.getStatus() == 1) {
                // 关注
                userStatisticsRepository.incrementFollowCount(event.getFollowerId());
                userStatisticsRepository.incrementFansCount(event.getFolloweeId());
            } else {
                // 取消关注
                userStatisticsRepository.decrementFollowCount(event.getFollowerId());
                userStatisticsRepository.decrementFansCount(event.getFolloweeId());
            }
            
            log.info("关注统计已更新: followerId={}, followeeId={}, status={}", 
                event.getFollowerId(), event.getFolloweeId(), event.getStatus());
        } catch (Exception e) {
            log.error("处理关注事件失败: followerId={}", event.getFollowerId(), e);
        }
    }
    
    // ========== 用户相关事件 ==========
    
    /**
     * 监听用户注册事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            // 创建用户统计记录
            UserStatistics stats = UserStatistics.createNew(event.getUserId());
            userStatisticsRepository.save(stats);
            
            log.info("用户统计已创建: userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("处理用户注册事件失败: userId={}", event.getUserId(), e);
        }
    }
}
