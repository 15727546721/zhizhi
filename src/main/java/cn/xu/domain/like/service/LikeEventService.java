package cn.xu.domain.like.service;

import cn.xu.domain.like.event.LikeEventPublisher;
import cn.xu.domain.like.model.LikeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 点赞事件服务
 * 处理点赞相关的事件发布和处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeEventService {
    
    private final LikeEventPublisher likeEventPublisher;
    
    /**
     * 发布点赞事件
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param isLike 是否点赞（true为点赞，false为取消点赞）
     */
    public void publishLikeEvent(Long userId, Long targetId, LikeType type, boolean isLike) {
        try {
            likeEventPublisher.publish(userId, targetId, type, isLike);
            log.info("发布点赞事件成功 - userId: {}, targetId: {}, type: {}, isLike: {}", 
                userId, targetId, type, isLike
            );
        } catch (Exception e) {
            log.error("发布点赞事件失败 - userId: {}, targetId: {}, type: {}, isLike: {}", 
                userId, targetId, type, isLike, 
                e
            );
        }
    }
}