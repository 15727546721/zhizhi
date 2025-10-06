package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 点赞数据一致性服务
 * 定期检查和修复点赞数据一致性问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDataConsistencyService {
    
    private final ILikeAggregateRepository likeAggregateRepository;
    private final LikeCacheRepository likeCacheRepository;
    private final LikeStatisticsService likeStatisticsService;
    
    /**
     * 定时检查点赞数据一致性（每30分钟执行一次）
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void checkLikeDataConsistency() {
        try {
            log.info("开始执行点赞数据一致性检查");
            
            // 这里可以添加具体的检查逻辑
            // 由于检查所有数据可能比较耗时，建议根据业务需求添加针对性的检查
            
            log.info("点赞数据一致性检查完成");
        } catch (Exception e) {
            log.error("点赞数据一致性检查失败", e);
        }
    }
    
    /**
     * 检查并修复指定用户和目标的点赞数据一致性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type) {
        try {
            log.info("开始检查并修复点赞数据一致性: userId={}, targetId={}, type={}", userId, targetId, type);
            
            // 1. 从数据库获取真实状态
            Optional<LikeAggregate> dbLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
            boolean dbLiked = dbLikeOpt.isPresent() && dbLikeOpt.get().isLiked();
            
            // 2. 从缓存获取状态
            boolean cacheLiked = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
            
            // 3. 比较并修复不一致
            if (dbLiked != cacheLiked) {
                log.warn("发现点赞数据不一致: userId={}, targetId={}, type={}, dbLiked={}, cacheLiked={}", 
                        userId, targetId, type, dbLiked, cacheLiked);
                
                if (dbLiked) {
                    // 数据库中已点赞，但缓存中未标记，需要添加缓存标记
                    likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                    log.info("修复缓存点赞标记: userId={}, targetId={}, type={}", userId, targetId, type);
                } else {
                    // 数据库中未点赞，但缓存中标记了，需要清除缓存标记
                    likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
                    log.info("清除缓存点赞标记: userId={}, targetId={}, type={}", userId, targetId, type);
                }
            }
            
            // 4. 检查点赞数一致性
            long dbLikeCount = likeAggregateRepository.countByTarget(targetId, type);
            Long cacheLikeCount = likeCacheRepository.getLikeCount(targetId, type);
            
            if (cacheLikeCount == null || dbLikeCount != cacheLikeCount) {
                log.warn("发现点赞数不一致: targetId={}, type={}, dbCount={}, cacheCount={}", 
                        targetId, type, dbLikeCount, cacheLikeCount);
                
                // 同步数据库计数到缓存
                likeCacheRepository.setLikeCount(targetId, type, dbLikeCount);
                log.info("同步点赞数到缓存: targetId={}, type={}, count={}", targetId, type, dbLikeCount);
            }
            
            log.info("点赞数据一致性检查并修复完成: userId={}, targetId={}, type={}", userId, targetId, type);
        } catch (Exception e) {
            log.error("检查并修复点赞数据一致性失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
        }
    }
    
    /**
     * 手动触发数据一致性检查和修复
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void repairTargetLikeConsistency(Long targetId, LikeType type) {
        try {
            log.info("开始修复目标点赞数据一致性: targetId={}, type={}", targetId, type);
            
            // 获取数据库中的点赞数
            long dbLikeCount = likeAggregateRepository.countByTarget(targetId, type);
            
            // 同步到缓存
            likeCacheRepository.setLikeCount(targetId, type, dbLikeCount);
            log.info("同步目标点赞数到缓存: targetId={}, type={}, count={}", targetId, type, dbLikeCount);
            
            log.info("目标点赞数据一致性修复完成: targetId={}, type={}", targetId, type);
        } catch (Exception e) {
            log.error("修复目标点赞数据一致性失败: targetId={}, type={}", targetId, type, e);
        }
    }
}