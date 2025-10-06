package cn.xu.domain.business;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.post.event.PostCreatedEvent;
import cn.xu.domain.post.event.PostDeletedEvent;
import cn.xu.domain.user.event.UserRegisteredEvent;
import cn.xu.domain.user.service.UserPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据一致性服务
 * 统一处理跨领域的数据一致性问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataConsistencyService {
    
    private final UserPointService userPointService;
    
    /**
     * 处理用户注册后的数据一致性
     */
    public void handleUserRegistration(UserRegisteredEvent event) {
        try {
            log.info("处理用户注册数据一致性: userId={}", event.getUserId());
            // 初始化用户相关数据
            // 例如：初始化用户积分、创建用户配置等
        } catch (Exception e) {
            log.error("处理用户注册数据一致性失败: userId={}", event.getUserId(), e);
        }
    }
    
    /**
     * 处理帖子创建后的数据一致性
     */
    public void handlePostCreation(PostCreatedEvent event) {
        try {
            log.info("处理帖子创建数据一致性: postId={}, userId={}", event.getPostId(), event.getUserId());
            // 更新用户积分（发布帖子增加10积分）
            userPointService.addUserPoints(event.getUserId(), 10);
        } catch (Exception e) {
            log.error("处理帖子创建数据一致性失败: postId={}, userId={}", event.getPostId(), event.getUserId(), e);
        }
    }
    
    /**
     * 处理帖子删除后的数据一致性
     */
    public void handlePostDeletion(PostDeletedEvent event) {
        try {
            log.info("处理帖子删除数据一致性: postId={}", event.getPostId());
            // 可以在这里处理帖子删除后的相关数据清理
        } catch (Exception e) {
            log.error("处理帖子删除数据一致性失败: postId={}", event.getPostId(), e);
        }
    }
    
    /**
     * 处理评论创建后的数据一致性
     */
    public void handleCommentCreation(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建数据一致性: commentId={}, userId={}", event.getCommentId(), event.getUserId());
            // 更新用户积分（发表评论增加1积分）
            userPointService.addUserPoints(event.getUserId(), 1);
        } catch (Exception e) {
            log.error("处理评论创建数据一致性失败: commentId={}, userId={}", event.getCommentId(), event.getUserId(), e);
        }
    }
    
    /**
     * 处理评论删除后的数据一致性
     */
    public void handleCommentDeletion(CommentDeletedEvent event) {
        try {
            log.info("处理评论删除数据一致性: commentId={}", event.getCommentId());
            // 可以在这里处理评论删除后的相关数据清理
        } catch (Exception e) {
            log.error("处理评论删除数据一致性失败: commentId={}", event.getCommentId(), e);
        }
    }
    
    /**
     * 处理点赞后的数据一致性
     */
    public void handleLikeOperation(LikeEvent event) {
        try {
            log.info("处理点赞数据一致性: userId={}, targetId={}, type={}, status={}", 
                    event.getUserId(), event.getTargetId(), event.getType(), event.getStatus());
            
            if (event.getStatus()) {
                // 点赞增加1积分
                userPointService.addUserPoints(event.getUserId(), 1);
            } else {
                // 取消点赞减少1积分
                userPointService.deductUserPoints(event.getUserId(), 1);
            }
        } catch (Exception e) {
            log.error("处理点赞数据一致性失败: userId={}, targetId={}, type={}", 
                     event.getUserId(), event.getTargetId(), event.getType(), e);
        }
    }
}