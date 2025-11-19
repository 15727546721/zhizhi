package cn.xu.domain.like.event;

import cn.xu.domain.comment.service.HotScoreService;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.domain.post.service.IPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 点赞事件监听器
 * 使用Spring Event机制异步处理点赞相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventListener {

    private final ILikeAggregateRepository likeAggregateRepository;
    private final IPostService postService;
    private final HotScoreService hotScoreService;

    /**
     * 处理点赞事件
     */
    @Async
    @EventListener
    public void onLikeEvent(LikeEvent event) {
        log.info("处理点赞事件: {}", event);
        try {
            if (event.getStatus()) {
                handleLike(event);
            } else {
                handleUnlike(event);
            }
        } catch (Exception e) {
            log.error("处理点赞事件失败: userId={}, targetId={}, type={}", 
                     event.getUserId(), event.getTargetId(), event.getType(), e);
        }
    }

    private void handleLike(LikeEvent event) {
        // 直接操作聚合根仓储，避免循环调用
        Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(
            event.getUserId(), event.getType(), event.getTargetId());
        
        boolean shouldUpdate = false;
        
        if (existingLikeOpt.isPresent()) {
            // 更新现有点赞
            LikeAggregate likeAggregate = existingLikeOpt.get();
            if (!likeAggregate.isLiked()) {
                likeAggregate.like();
                likeAggregateRepository.update(likeAggregate);
                shouldUpdate = true;
                log.info("更新现有点赞记录为已点赞状态: userId={}, targetId={}, type={}", 
                        event.getUserId(), event.getTargetId(), event.getType());
            }
        } else {
            // 创建新点赞
            LikeAggregate likeAggregate = LikeAggregate.create(
                event.getUserId(), event.getTargetId(), event.getType());
            likeAggregateRepository.save(likeAggregate);
            shouldUpdate = true;
            log.info("创建新点赞记录: userId={}, targetId={}, type={}", 
                    event.getUserId(), event.getTargetId(), event.getType());
        }

        // 根据点赞类型更新热度分数
        if (shouldUpdate) {
            if (event.getType().isPost()) {
                postService.updatePostHotScore(event.getTargetId());
                log.info("更新帖子热度分数: postId={}", event.getTargetId());
            } else if (event.getType().isComment()) {
                hotScoreService.updateHotScore(event.getTargetId());
                log.info("更新评论热度分数: commentId={}", event.getTargetId());
            }
        }

        log.info("点赞处理完成: userId={}, targetId={}, type={}", 
                event.getUserId(), event.getTargetId(), event.getType());
    }

    private void handleUnlike(LikeEvent event) {
        // 直接操作聚合根仓储，避免循环调用
        Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(
            event.getUserId(), event.getType(), event.getTargetId());
        
        boolean shouldUpdate = false;
        
        if (existingLikeOpt.isPresent()) {
            LikeAggregate likeAggregate = existingLikeOpt.get();
            if (likeAggregate.isLiked()) {
                likeAggregate.unlike();
                likeAggregateRepository.update(likeAggregate);
                shouldUpdate = true;
                log.info("更新现有点赞记录为未点赞状态: userId={}, targetId={}, type={}", 
                        event.getUserId(), event.getTargetId(), event.getType());
            }
        }

        // 根据点赞类型更新热度分数
        if (shouldUpdate) {
            if (event.getType().isPost()) {
                postService.updatePostHotScore(event.getTargetId());
                log.info("更新帖子热度分数: postId={}", event.getTargetId());
            } else if (event.getType().isComment()) {
                hotScoreService.updateHotScore(event.getTargetId());
                log.info("更新评论热度分数: commentId={}", event.getTargetId());
            }
        }

        log.info("取消点赞处理完成: userId={}, targetId={}, type={}", 
                event.getUserId(), event.getTargetId(), event.getType());
    }
}