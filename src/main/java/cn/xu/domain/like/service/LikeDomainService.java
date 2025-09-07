package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeEntity;
import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 点赞领域服务
 * 处理点赞相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDomainService {
    
    private final ILikeRepository likeRepository;
    private final LikeEventService likeEventService;
    private final LikeStatisticsService likeStatisticsService;
    private final LikeHotDataService likeHotDataService;
    
    /**
     * 执行点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void doLike(Long userId, Long targetId, LikeType type) {
        // 1. 检查用户是否已经点赞
        LikeEntity existingLike = likeRepository.findByUserIdAndTypeAndTargetId(userId, type, targetId);
        
        if (existingLike == null) {
            // 2. 用户没有点赞，创建新记录
            LikeEntity likeEntity = LikeEntity.createLike(userId, targetId, type);
            likeRepository.saveLike(likeEntity);
            
            // 3. 发布点赞事件
            likeEventService.publishLikeEvent(likeEntity, true);
        } else if (existingLike.getStatus() == LikeStatus.UNLIKED) {
            // 3. 已取消点赞，则重新点赞
            existingLike.doLike();
            likeRepository.saveLike(existingLike);
            
            // 4. 发布点赞事件
            likeEventService.publishLikeEvent(existingLike, true);
        } else {
            log.warn("[点赞操作] 用户已点赞，无需重复操作 - userId: {}, targetId: {}", userId, targetId);
            throw new BusinessException("您已点赞，请勿重复操作");
        }
        
        // 5. 更新统计信息
        updateLikeStatistics(targetId, type, 1L);
    }
    
    /**
     * 取消点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void cancelLike(Long userId, Long targetId, LikeType type) {
        // 1. 查找已点赞的记录
        LikeEntity likeEntity = likeRepository.findByUserIdAndTypeAndTargetId(userId, type, targetId);
        if (likeEntity == null) {
            log.warn("[取消点赞] 找不到点赞记录 - userId: {}, targetId: {}", userId, targetId);
            throw new BusinessException("您尚未点赞，无法取消");
        }
        
        // 2. 取消点赞
        likeEntity.cancelLike();
        likeRepository.saveLike(likeEntity);
        
        // 3. 发布取消点赞事件
        likeEventService.publishLikeEvent(likeEntity, false);
        
        // 4. 更新统计信息
        updateLikeStatistics(targetId, type, -1L);
    }
    
    /**
     * 检查用户是否点赞某个目标
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 是否已点赞
     */
    public boolean checkLikeStatus(Long userId, Long targetId, LikeType type) {
        LikeEntity likeEntity = likeRepository.findByUserIdAndTypeAndTargetId(userId, type, targetId);
        return likeEntity != null && likeEntity.isLiked();
    }
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        return likeStatisticsService.getLikeCount(targetId, type);
    }
    
    /**
     * 更新点赞统计信息
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param delta 变化量
     */
    private void updateLikeStatistics(Long targetId, LikeType type, Long delta) {
        try {
            // 更新热点数据
            Long currentCount = likeStatisticsService.getLikeCount(targetId, type);
            likeHotDataService.updateHotData(targetId, type, currentCount + delta);
        } catch (Exception e) {
            log.error("更新点赞统计信息失败 - targetId: {}, type: {}, delta: {}", targetId, type, delta, e);
        }
    }
}