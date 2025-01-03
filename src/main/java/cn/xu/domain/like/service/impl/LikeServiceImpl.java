package cn.xu.domain.like.service.impl;

import cn.xu.domain.like.command.LikeCommand;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeDomainService;
import cn.xu.domain.like.service.LikeService;
import com.lmax.disruptor.RingBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 点赞应用服务实现
 * 负责协调领域服务和事件发布
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeDomainService likeDomainService;
    private final RingBuffer<LikeEvent> likeEventRingBuffer;

    @Override
    @Transactional
    public void like(LikeCommand command) {
        // 参数校验
        validateCommand(command);

        // 调用领域服务处理点赞
        boolean success = likeDomainService.like(command.getUserId(), command.getTargetId(), command.getType());

        if (success) {
            // 发布点赞事件
            publishLikeEvent(command, true);
            log.info("用户[{}]点赞了{}[{}]",
                    command.getUserId(), command.getType().getDescription(), command.getTargetId());
        }
    }

    @Override
    @Transactional
    public void unlike(LikeCommand command) {
        // 参数校验
        validateCommand(command);

        // 调用领域服务处理取消点赞
        boolean success = likeDomainService.unlike(command.getUserId(), command.getTargetId(), command.getType());

        if (success) {
            // 发布取消点赞事件
            publishLikeEvent(command, false);
            log.info("用户[{}]取消点赞了{}[{}]",
                    command.getUserId(), command.getType().getDescription(), command.getTargetId());
        }
    }

    @Override
    public Long getLikeCount(Long targetId, String type) {
        validateTargetId(targetId);
        return likeDomainService.getLikeCount(targetId, LikeType.valueOf(type.toUpperCase()));
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, String type) {
        validateUserId(userId);
        validateTargetId(targetId);
        return likeDomainService.isLiked(userId, targetId, LikeType.valueOf(type.toUpperCase()));
    }

    /**
     * 发布点赞事件
     */
    private void publishLikeEvent(LikeCommand command, boolean liked) {
        long sequence = likeEventRingBuffer.next();
        try {
            LikeEvent event = likeEventRingBuffer.get(sequence);
            event.setUserId(command.getUserId());
            event.setTargetId(command.getTargetId());
            event.setType(command.getType());
            event.setLiked(liked);
            event.setOccurredTime(LocalDateTime.now());
        } finally {
            likeEventRingBuffer.publish(sequence);
        }
    }

    private void validateCommand(LikeCommand command) {
        validateUserId(command.getUserId());
        validateTargetId(command.getTargetId());
        if (command.getType() == null) {
            throw new IllegalArgumentException("点赞类型不能为空");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }

    private void validateTargetId(Long targetId) {
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("目标ID不合法");
        }
    }
} 