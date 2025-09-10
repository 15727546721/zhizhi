package cn.xu.application.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeDomainService;
import cn.xu.domain.like.service.LikeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 点赞应用服务
 * 协调领域服务完成点赞相关操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeApplicationService {
    
    private final LikeDomainService likeDomainService;
    private final LikeStatisticsService likeStatisticsService;
    
    /**
     * 执行点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param typeCode 点赞类型代码
     */
    public void doLike(Long userId, Long targetId, Integer typeCode) {
        LikeType type = LikeType.valueOf(typeCode);
        if (type == null) {
            throw new IllegalArgumentException("不支持的点赞类型: " + typeCode);
        }
        
        likeDomainService.doLike(userId, targetId, type);
    }
    
    /**
     * 取消点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param typeCode 点赞类型代码
     */
    public void cancelLike(Long userId, Long targetId, Integer typeCode) {
        LikeType type = LikeType.valueOf(typeCode);
        if (type == null) {
            throw new IllegalArgumentException("不支持的点赞类型: " + typeCode);
        }
        
        likeDomainService.cancelLike(userId, targetId, type);
    }
    
    /**
     * 检查用户是否点赞某个目标
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param typeCode 点赞类型代码
     * @return 是否已点赞
     */
    public boolean checkLikeStatus(Long userId, Long targetId, Integer typeCode) {
        LikeType type = LikeType.valueOf(typeCode);
        if (type == null) {
            throw new IllegalArgumentException("不支持的点赞类型: " + typeCode);
        }
        
        return likeDomainService.checkLikeStatus(userId, targetId, type);
    }
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param typeCode 点赞类型代码
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, Integer typeCode) {
        LikeType type = LikeType.valueOf(typeCode);
        if (type == null) {
            throw new IllegalArgumentException("不支持的点赞类型: " + typeCode);
        }
        
        return likeDomainService.getLikeCount(targetId, type);
    }
    
    /**
     * 获取用户点赞统计信息
     * 
     * @param userId 用户ID
     * @return 用户点赞统计信息
     */
    public LikeStatisticsService.UserLikeStatistics getUserLikeStatistics(Long userId) {
        return likeStatisticsService.getUserLikeStatistics(userId);
    }
}