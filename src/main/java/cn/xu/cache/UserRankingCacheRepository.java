package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户排行榜缓存仓储
 * 使用Redis ZSet实现用户排行榜，支持多种排序维度
 * 
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码
 * 
 * @author zhizhi
 * @since 2025-11-23
 */
@Slf4j
@Repository
public class UserRankingCacheRepository extends BaseCacheRepository {
    
    // TTL配置
    private static final int DEFAULT_CACHE_TTL = 300; // 5分钟（排行榜更新频率较高）
    private static final int EMPTY_RESULT_TTL = 60;   // 1分钟（空结果）

    /**
     * 获取用户排行榜ID列表
     * @param sortType 排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)
     * @param start 开始位置（从0开始）
     * @param end 结束位置（-1表示到最后）
     * @return 用户ID列表
     */
    public List<Long> getUserRankingIds(String sortType, int start, int end) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            // 使用ZREVRANGE获取降序排列的用户ID（分数高的在前）
            Set<Object> userIds = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
            
            if (userIds != null && !userIds.isEmpty()) {
                return userIds.stream()
                        .map(this::convertToLong)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("[缓存] 获取用户排行榜ID失败 - key: {}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存用户排行榜
     * @param sortType 排序类型
     * @param userScores 用户ID和分数的映射
     */
    public void cacheUserRanking(String sortType, Map<Long, Double> userScores) {
        if (userScores == null || userScores.isEmpty()) {
            cacheEmptyResult(sortType);
            return;
        }

        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            deleteCache(redisKey); // 先清空旧数据
            
            // 批量插入新数据
            userScores.forEach((userId, score) -> {
                if (score != null && userId != null) {
                    redisTemplate.opsForZSet().add(redisKey, userId.toString(), score);
                }
            });
            
            expire(redisKey, DEFAULT_CACHE_TTL);
            
            log.debug("缓存用户排行榜成功: key={}, size={}", redisKey, userScores.size());
        } catch (Exception e) {
            log.error("[缓存] 缓存用户排行榜失败 - key: {}", redisKey, e);
        }
    }

    /**
     * 更新单个用户的排行榜分数
     * @param sortType 排序类型
     * @param userId 用户ID
     * @param score 分数
     */
    public void updateUserRankingScore(String sortType, Long userId, double score) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            redisTemplate.opsForZSet().add(redisKey, userId.toString(), score);
            log.debug("更新用户排行榜分数成功: key={}, userId={}, score={}", redisKey, userId, score);
        } catch (Exception e) {
            log.error("[缓存] 更新排行榜分数失败 - key: {}, userId: {}", redisKey, userId, e);
        }
    }

    /**
     * 批量更新用户排行榜分数
     * @param sortType 排序类型
     * @param userScores 用户ID和分数的映射
     */
    public void batchUpdateUserRanking(String sortType, Map<Long, Double> userScores) {
        if (userScores == null || userScores.isEmpty()) {
            return;
        }

        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            // 批量更新排行榜分数
            userScores.forEach((userId, score) -> {
                if (score != null && userId != null) {
                    redisTemplate.opsForZSet().add(redisKey, userId.toString(), score);
                }
            });
            
            log.debug("批量更新用户排行榜成功: key={}, size={}", redisKey, userScores.size());
        } catch (Exception e) {
            log.error("[缓存] 批量更新排行榜失败 - key: {}", redisKey, e);
        }
    }

    /**
     * 从排行榜中移除用户
     * @param sortType 排序类型
     * @param userId 用户ID
     */
    public void removeFromRanking(String sortType, Long userId) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            redisTemplate.opsForZSet().remove(redisKey, userId.toString());
            log.debug("从排行榜移除用户成功: key={}, userId={}", redisKey, userId);
        } catch (Exception e) {
            log.error("[缓存] 移除排行榜用户失败 - key: {}, userId: {}", redisKey, userId, e);
        }
    }

    /**
     * 获取用户的排行榜分数
     * @param sortType 排序类型
     * @param userId 用户ID
     * @return 分数，不存在返回null
     */
    public Double getUserRankingScore(String sortType, Long userId) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            return redisTemplate.opsForZSet().score(redisKey, userId.toString());
        } catch (Exception e) {
            log.error("[缓存] 获取排行榜分数失败 - key: {}, userId: {}", redisKey, userId, e);
            return null;
        }
    }

    /**
     * 获取用户的排名（从1开始）
     * @param sortType 排序类型
     * @param userId 用户ID
     * @return 排名，不存在返回-1
     */
    public Long getUserRank(String sortType, Long userId) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            // ZREVRANK返回的是从0开始的排名，需要+1
            Long rank = redisTemplate.opsForZSet().reverseRank(redisKey, userId.toString());
            return rank != null ? rank + 1 : -1L;
        } catch (Exception e) {
            log.error("[缓存] 获取用户排名失败 - key: {}, userId: {}", redisKey, userId, e);
            return -1L;
        }
    }

    /**
     * 获取排行榜总数量
     * @param sortType 排序类型
     * @return 排行榜总数量
     */
    public Long getRankingSize(String sortType) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            return redisTemplate.opsForZSet().size(redisKey);
        } catch (Exception e) {
            log.error("[缓存] 获取排行榜总数量失败 - key: {}", redisKey, e);
            return 0L;
        }
    }

    /**
     * 检查排行榜是否存在
     * @param sortType 排序类型
     * @return 是否存在
     */
    public boolean exists(String sortType) {
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        return hasKey(redisKey);
    }

    /**
     * 缓存空结果，防止缓存穿透
     * @param sortType 排序类型
     */
    public void cacheEmptyResult(String sortType) {
        String redisKey = RedisKeyManager.userRankingKey(sortType) + ":empty";
        setValue(redisKey, "1", EMPTY_RESULT_TTL);
    }

    /**
     * 检查是否为空结果缓存
     * @param sortType 排序类型
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached(String sortType) {
        String redisKey = RedisKeyManager.userRankingKey(sortType) + ":empty";
        return hasKey(redisKey);
    }

    /**
     * 清理无效的缓存数据
     * @param sortType 排序类型
     * @param invalidIds 无效的用户ID列表
     */
    public void cleanupInvalidCacheData(String sortType, List<Long> invalidIds) {
        if (invalidIds == null || invalidIds.isEmpty()) {
            return;
        }
        
        String redisKey = RedisKeyManager.userRankingKey(sortType);
        
        try {
            // 批量删除无效的ID
            for (Long invalidId : invalidIds) {
                redisTemplate.opsForZSet().remove(redisKey, invalidId.toString());
            }
            
            log.info("[缓存] 清理无效数据成功 - key: {}, count: {}", redisKey, invalidIds.size());
        } catch (Exception e) {
            log.error("[缓存] 清理无效数据失败 - key: {}", redisKey, e);
        }
    }

}

