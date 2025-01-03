package cn.xu.domain.like.service.impl;

import cn.xu.domain.like.command.LikeCommand;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeService;
import cn.xu.domain.like.service.LikeDomainService;
import com.lmax.disruptor.RingBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * 点赞服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeDomainService likeDomainService;
    
    @Qualifier("domainLikeThreadPool")
    private final ExecutorService likeThreadPool;
    
    private final RingBuffer<LikeEvent> likeEventRingBuffer;

    @Override
    public void like(Long userId, Long targetId, LikeType type) {
        // 参数校验
        validateParams(userId, targetId, type);

        LikeCommand command = LikeCommand.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .build();

        likeThreadPool.execute(() -> {
            try {
                LikeEvent event = likeDomainService.handleLike(command);
                if (event != null) {
                    publishEvent(event);
                    log.info("用户[{}]点赞了{}[{}]", userId, type.getDescription(), targetId);
                }
            } catch (Exception e) {
                log.error("点赞{}[{}]失败: {}", type.getDescription(), targetId, e.getMessage());
            }
        });
    }

    @Override
    public void unlike(Long userId, Long targetId, LikeType type) {
        // 参数校验
        validateParams(userId, targetId, type);

        LikeCommand command = LikeCommand.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .build();

        likeThreadPool.execute(() -> {
            try {
                LikeEvent event = likeDomainService.handleUnlike(command);
                if (event != null) {
                    publishEvent(event);
                    log.info("用户[{}]取消点赞了{}[{}]", userId, type.getDescription(), targetId);
                }
            } catch (Exception e) {
                log.error("取消点赞{}[{}]失败: {}", type.getDescription(), targetId, e.getMessage());
            }
        });
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        if (targetId == null || type == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return likeDomainService.getLikeCount(targetId, type);
    }

    @Override
    public Boolean isLiked(Long userId, Long targetId, LikeType type) {
        validateParams(userId, targetId, type);
        return likeDomainService.isLiked(userId, targetId, type);
    }

    /**
     * 使用Disruptor发布事件
     */
    private void publishEvent(LikeEvent event) {
        try {
            // 获取下一个序列号
            long sequence = likeEventRingBuffer.next();
            try {
                // 获取该序列号对应的事件对象
                LikeEvent likeEvent = likeEventRingBuffer.get(sequence);
                // 设置事件属性
                likeEvent.setUserId(event.getUserId());
                likeEvent.setTargetId(event.getTargetId());
                likeEvent.setType(event.getType());
                likeEvent.setLiked(event.isLiked());
                likeEvent.setOccurredTime(event.getOccurredTime());
            } finally {
                // 发布事件
                likeEventRingBuffer.publish(sequence);
            }
            log.debug("点赞事件发布成功：userId={}, targetId={}, type={}, liked={}", 
                    event.getUserId(), event.getTargetId(), event.getType(), event.isLiked());
        } catch (Exception e) {
            log.error("点赞事件发布失败：{}", e.getMessage());
            throw new RuntimeException("点赞事件发布失败", e);
        }
    }

    private void validateParams(Long userId, Long targetId, LikeType type) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("点赞类型不能为空");
        }
    }
} 