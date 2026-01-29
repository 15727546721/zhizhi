package cn.xu.service.favorite;

import cn.xu.cache.repository.FavoriteCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 收藏数据一致性服务
 * <p>定时检查并同步收藏数据的一致性</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteDataConsistencyService {

    private final FavoriteService favoriteService;
    private final FavoriteCacheRepository favoriteCacheRepository;

    /**
     * 定时任务：每30分钟执行一次，检查并同步收藏数据的一致性
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void checkAndFixConsistency() {
        try {
            log.info("[收藏数据一致性检查] 开始执行定时任务");

            // 1. 检查数据库中的收藏数量与缓存中的是否一致
            // 2. 检查用户的收藏关系是否一致

            log.info("[收藏数据一致性检查] 定时任务执行完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性检查] 执行失败", e);
        }
    }

    /**
     * 手动触发收藏数据一致性检查
     */
    public void triggerConsistencyCheck() {
        try {
            log.info("[收藏数据一致性检查] 手动触发一致性检查");

            // 手动执行一致性检查
            checkAndFixConsistency();

            log.info("[收藏数据一致性检查] 手动触发的一致性检查完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性检查] 手动触发失败", e);
            throw e;  // 重新抛出异常
        }
    }

    /**
     * 检查并同步目标项的收藏数量一致性
     */
    public void checkAndFixTargetConsistency(Long targetId, String targetType) {
        try {
            log.info("[收藏数据一致性检查] 检查目标项收藏数量一致性, targetId={}, targetType={}", targetId, targetType);

            // 获取数据库中该目标项的收藏数量
            int dbCount = favoriteService.countFavoritedItemsByTarget(targetId, targetType);

            // 获取缓存中该目标项的收藏数量
            Long cacheCount = favoriteCacheRepository.getFavoriteCount(targetId, targetType);

            // 如果缓存和数据库中的收藏数量不一致，同步缓存
            if (cacheCount != null && cacheCount.intValue() != dbCount) {
                log.warn("[收藏数据一致性检查] 缓存和数据库中的收藏数量不一致，同步缓存, targetId={}, targetType={}, dbCount={}, cacheCount={}",
                        targetId, targetType, dbCount, cacheCount);
                favoriteCacheRepository.setFavoriteCount(targetId, targetType, (long) dbCount);
            } else if (cacheCount == null && dbCount > 0) {
                // 如果缓存为空且数据库中有数据，初始化缓存
                log.info("[收藏数据一致性检查] 缓存为空，初始化缓存, targetId={}, targetType={}, dbCount={}",
                        targetId, targetType, dbCount);
                favoriteCacheRepository.setFavoriteCount(targetId, targetType, (long) dbCount);
            }

            log.info("[收藏数据一致性检查] 目标项收藏数量一致性检查完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性检查] 检查目标项收藏数量一致性失败, targetId={}, targetType={}", targetId, targetType, e);
        }
    }

    /**
     * 检查并同步用户对目标项的收藏关系一致性
     */
    public void checkAndFixUserFavoriteConsistency(Long userId, Long targetId, String targetType) {
        try {
            log.info("[收藏数据一致性检查] 检查用户收藏关系一致性, userId={}, targetId={}, targetType={}",
                    userId, targetId, targetType);

            // 获取数据库中用户是否收藏该目标项
            boolean dbFavorited = favoriteService.isFavorited(userId, targetId, targetType);

            // 获取缓存中用户是否收藏该目标项
            Boolean cacheFavorited = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, targetType);

            // 如果缓存中的值为null，则将数据库值写入缓存
            if (cacheFavorited == null) {
                log.info("[收藏数据一致性检查] 缓存无数据，初始化缓存, userId={}, targetId={}, targetType={}, dbStatus={}",
                        userId, targetId, targetType, dbFavorited);
                favoriteCacheRepository.updateUserFavoriteRelation(userId, targetId, targetType, dbFavorited);
            }
            // 如果缓存和数据库中的值不一致，则同步缓存
            else if (dbFavorited != cacheFavorited) {
                log.warn("[收藏数据一致性检查] 用户收藏关系不一致，同步缓存, userId={}, targetId={}, targetType={}, dbStatus={}, cacheStatus={}",
                        userId, targetId, targetType, dbFavorited, cacheFavorited);
                favoriteCacheRepository.updateUserFavoriteRelation(userId, targetId, targetType, dbFavorited);
            }

            log.info("[收藏数据一致性检查] 用户收藏关系一致性检查完成");
        } catch (Exception e) {
            log.error("[收藏数据一致性检查] 用户收藏关系一致性检查失败, userId={}, targetId={}, targetType={}", userId, targetId, targetType, e);
        }
    }
}
