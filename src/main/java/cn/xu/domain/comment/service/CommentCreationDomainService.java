package cn.xu.domain.comment.service;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentEventPublisher;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.policy.HotScoreStrategy;
import cn.xu.domain.comment.model.policy.HotScoreStrategyFactory;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 评论创建领域服务
 * 负责评论创建相关的业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCreationDomainService {

    private final ICommentRepository commentRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final IUserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HotScoreStrategyFactory hotScoreStrategyFactory;

    /**
     * 创建评论
     * @param event 评论创建事件
     * @return 评论ID
     */
    public Long createComment(CommentCreatedEvent event) {
        // 1. 验证用户权限
        UserEntity currentUser = validateUser();
        
        // 2. 构建评论实体
        CommentEntity comment = buildCommentEntity(event, currentUser.getId());
        
        // 3. 保存评论
        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);
        
        // 4. 更新缓存
        updateCacheAfterCreation(comment, event);
        
        // 5. 发布事件
        event.setCommentId(commentId);
        commentEventPublisher.publishCommentCreatedEvent(event);
        
        log.info("评论创建成功 - commentId: {}, userId: {}, targetType: {}, targetId: {}", 
                commentId, currentUser.getId(), event.getTargetType(), event.getTargetId());
        
        return commentId;
    }

    /**
     * 验证用户是否有权限创建评论
     */
    private UserEntity validateUser() {
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        
        UserEntity currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        return currentUser;
    }

    /**
     * 构建评论实体
     */
    private CommentEntity buildCommentEntity(CommentCreatedEvent event, Long userId) {
        HotScoreStrategy strategy = hotScoreStrategyFactory.getStrategy();
        double score = hotScoreStrategyFactory.calculateInitialScore(strategy, LocalDateTime.now());

        return CommentEntity.builder()
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .parentId(event.getParentId())
                .userId(userId)
                .content(new cn.xu.domain.comment.model.valueobject.CommentContent(event.getContent()))
                .imageUrls(event.getImageUrls())
                .replyUserId(event.getReplyUserId())
                .likeCount(0L)
                .replyCount(0L)
                .hotScore(score)
                .isHot(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建评论后更新缓存
     */
    private void updateCacheAfterCreation(CommentEntity comment, CommentCreatedEvent event) {
        try {
            // 更新评论计数
            updateCommentCount(comment, event);
            
            // 更新热度排行缓存
            updateHotRankCache(comment, event);
            
            log.debug("评论创建后缓存更新成功 - commentId: {}", comment.getId());
        } catch (Exception e) {
            log.error("评论创建后缓存更新失败 - commentId: {}", comment.getId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 更新评论计数
     */
    private void updateCommentCount(CommentEntity comment, CommentCreatedEvent event) {
        if (comment.getParentId() != null) {
            // 子评论：更新父评论的回复计数
            redisTemplate.opsForValue().increment(
                    RedisKeyManager.commentCountKey(CommentType.COMMENT.getValue(), comment.getParentId()), 1);
        }
        
        // 更新目标对象的评论计数
        redisTemplate.opsForValue().increment(
                RedisKeyManager.commentCountKey(event.getTargetType(), event.getTargetId()), 1);
    }

    /**
     * 更新热度排行缓存
     */
    private void updateHotRankCache(CommentEntity comment, CommentCreatedEvent event) {
        if (comment.getParentId() == null) {
            // 一级评论：写入主ZSet
            redisTemplate.opsForZSet().add(
                    RedisKeyManager.commentHotRankKey(CommentType.valueOf(event.getTargetType()), event.getTargetId()),
                    comment.getId(),
                    comment.getHotScore()
            );
        } else {
            // 二级回复：写入对应的回复ZSet
            redisTemplate.opsForZSet().add(
                    RedisKeyManager.replyHotRankKey(CommentType.valueOf(event.getTargetType()), 
                            event.getTargetId(), comment.getParentId()),
                    comment.getId(),
                    comment.getHotScore()
            );
        }
    }
}