package cn.xu.domain.comment.event;

import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.HotScoreService;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.post.service.IPostService;
import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 评论事件监听处理器
 * 使用Disruptor事件监听器统一处理评论相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final ICommentRepository commentRepository;
    private final HotScoreService hotScoreService;
    private final IPostService postService;
    private final IPostRepository postRepository;

    /**
     * 处理评论创建事件（使用Disruptor监听器）
     */
    @DisruptorListener(eventType = "CommentCreatedEvent", priority = 10)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建事件: {}", event);

            // 1. 更新目标对象的评论计数
            updateTargetCommentCount(CommentType.valueOf(event.getTargetType()), event.getTargetId(), 1);

            // 2. 更新热度分数
            hotScoreService.updateHotScore(event.getCommentId());

            // 3. 更新帖子的评论数和热度分数
            updatePostCommentCount(event.getTargetId(), true);
            postService.updatePostHotScore(event.getTargetId());

            log.info("评论创建事件处理完成: commentId={}", event.getCommentId());
        } catch (Exception e) {
            log.error("处理评论创建事件失败: commentId={}", event.getCommentId(), e);
        }
    }

    /**
     * 处理评论点赞事件
     */
    @DisruptorListener(eventType = "CommentLikedEvent", priority = 10)
    public void handleCommentLikedEvent(CommentLikedEvent event) {
        try {
            log.info("处理评论点赞事件: {}", event);

            // 1. 更新热度分数
            hotScoreService.updateHotScore(event.getCommentId());

            log.info("评论点赞事件处理完成: commentId={}", event.getCommentId());
        } catch (Exception e) {
            log.error("处理评论点赞事件失败: commentId={}", event.getCommentId(), e);
        }
    }

    /**
     * 处理评论删除事件
     */
    @DisruptorListener(eventType = "CommentDeletedEvent", priority = 10)
    public void handleCommentDeletedEvent(CommentDeletedEvent event) {
        try {
            log.info("处理评论删除事件: {}", event);

            // 1. 更新目标对象的评论计数
            updateTargetCommentCount(CommentType.valueOf(event.getTargetType()), event.getTargetId(), -1);

            // 2. 从热度排行中移除评论
            hotScoreService.removeHotScore(
                event.getCommentId(), 
                event.getTargetType(), 
                event.getTargetId(), 
                event.isRootComment() ? null : event.getParentId()
            );

            // 3. 更新帖子的评论数和热度分数
            updatePostCommentCount(event.getTargetId(), false);
            postService.updatePostHotScore(event.getTargetId());

            log.info("评论删除事件处理完成: commentId={}", event.getCommentId());
        } catch (Exception e) {
            log.error("处理评论删除事件失败: commentId={}", event.getCommentId(), e);
        }
    }

    private void updateTargetCommentCount(CommentType type, Long targetId, int increment) {
        // 这里可以实现更新目标对象评论计数的逻辑
        // 目前简化处理，实际项目中可能需要根据type调用不同的服务
        log.info("更新目标对象评论计数: type={}, targetId={}, increment={}", type, targetId, increment);
    }
    
    /**
     * 更新帖子评论数
     * 
     * @param postId 帖子ID
     * @param isIncrease 是否增加
     */
    private void updatePostCommentCount(Long postId, boolean isIncrease) {
        try {
            // 获取帖子聚合根
            Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
            if (postAggregateOpt.isPresent()) {
                PostAggregate postAggregate = postAggregateOpt.get();
                
                // 更新评论数
                if (isIncrease) {
                    postAggregate.increaseCommentCount();
                } else {
                    postAggregate.decreaseCommentCount();
                }
                
                // 保存更新
                postRepository.update(postAggregate);
                
                log.info("帖子评论数更新成功: postId={}, isIncrease={}", postId, isIncrease);
            }
        } catch (Exception e) {
            log.error("更新帖子评论数失败: postId={}, isIncrease={}", postId, isIncrease, e);
        }
    }
}