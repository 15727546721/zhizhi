package cn.xu.infrastructure.service;

import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.infrastructure.cache.FavoriteCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 收藏数据一致性服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteDataConsistencyService {

    private final IFavoriteService favoriteService;
    private final FavoriteCacheRepository favoriteCacheRepository;

    /**
     * 定时任务：检查并修复收藏数据一致性
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void checkAndFixConsistency() {
        try {
            log.info("[收藏数据一致性] 开始执行定时检查任务");
            
            // 这里可以实现具体的一致性检查逻辑
            // 1. 检查缓存中的收藏数与数据库是否一致
            // 2. 检查用户收藏关系是否一致
            
            log.info("[收藏数据一致性] 定时检查任务执行完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性] 定时检查任务执行失败", e);
        }
    }

    /**
     * 手动触发数据一致性检查
     */
    public void triggerConsistencyCheck() {
        try {
            log.info("[收藏数据一致性] 开始执行手动触发的检查任务");
            
            // 实现与定时任务相同的逻辑
            checkAndFixConsistency();
            
            log.info("[收藏数据一致性] 手动触发的检查任务执行完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性] 手动触发的检查任务执行失败", e);
            throw e;
        }
    }

    /**
     * 检查并修复指定目标的收藏数一致性
     */
    public void checkAndFixTargetConsistency(Long targetId, String targetType) {
        try {
            log.info("[收藏数据一致性] 开始检查目标对象收藏数一致性，targetId={}, targetType={}", targetId, targetType);
            
            // 从数据库获取真实收藏数
            int dbCount = favoriteService.countFavoritedItemsByTarget(targetId, targetType);
            
            // 从缓存获取收藏数
            Long cacheCount = favoriteCacheRepository.getFavoriteCount(targetId, targetType);
            
            // 如果缓存存在且与数据库不一致，则修复缓存
            if (cacheCount != null && cacheCount.intValue() != dbCount) {
                log.warn("[收藏数据一致性] 发现不一致数据，修复收藏数缓存，targetId={}, targetType={}, dbCount={}, cacheCount={}", 
                        targetId, targetType, dbCount, cacheCount);
                favoriteCacheRepository.setFavoriteCount(targetId, targetType, (long) dbCount);
            } else if (cacheCount == null && dbCount > 0) {
                // 如果缓存不存在但数据库有记录，初始化缓存
                log.info("[收藏数据一致性] 缓存不存在，初始化收藏数缓存，targetId={}, targetType={}, dbCount={}", 
                        targetId, targetType, dbCount);
                favoriteCacheRepository.setFavoriteCount(targetId, targetType, (long) dbCount);
            }
            
            log.info("[收藏数据一致性] 目标对象收藏数一致性检查完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性] 目标对象收藏数一致性检查失败，targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    /**
     * 检查并修复用户收藏关系一致性
     */
    public void checkAndFixUserFavoriteConsistency(Long userId, Long targetId, String targetType) {
        try {
            log.info("[收藏数据一致性] 开始检查用户收藏关系一致性，userId={}, targetId={}, targetType={}", 
                    userId, targetId, targetType);
            
            // 从数据库检查收藏状态
            boolean dbFavorited = favoriteService.isFavorited(userId, targetId, targetType);
            
            // 从缓存检查收藏状态
            Boolean cacheFavorited = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, targetType);
            
            // 确保处理null值情况
            if (cacheFavorited == null) {
                log.info("[收藏数据一致性] 缓存查询失败或不存在，根据数据库状态初始化缓存，userId={}, targetId={}, targetType={}, dbStatus={}", 
                        userId, targetId, targetType, dbFavorited);
                favoriteCacheRepository.updateUserFavoriteRelation(userId, targetId, targetType, dbFavorited);
            } 
            // 如果不一致，修复缓存
            else if (dbFavorited != cacheFavorited) {
                log.warn("[收藏数据一致性] 发现不一致数据，修复用户收藏关系缓存，userId={}, targetId={}, targetType={}, dbStatus={}, cacheStatus={}", 
                        userId, targetId, targetType, dbFavorited, cacheFavorited);
                favoriteCacheRepository.updateUserFavoriteRelation(userId, targetId, targetType, dbFavorited);
            }
            
            log.info("[收藏数据一致性] 用户收藏关系一致性检查完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性] 用户收藏关系一致性检查失败，userId={}, targetId={}, targetType={}", 
                    userId, targetId, targetType, e);
        }
    }
}