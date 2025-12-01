package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子缓存仓储
 * 处理帖子相关的缓存操作
 * 
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码
 * 
 * @author zhizhi
 * @since 2025-11-23
 */
@Slf4j
@Repository
public class PostCacheRepository extends BaseCacheRepository {
    
    // TTL配置
    private static final int DEFAULT_CACHE_TTL = 1800; // 30分钟
    private static final int EMPTY_RESULT_TTL = 60;    // 1分钟（空结果）

    /**
     * 获取热门帖子ID列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 帖子ID列表
     */
    public List<Long> getHotPostIds(int start, int end) {
        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            Set<Object> postIds = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
            
            if (postIds != null && !postIds.isEmpty()) {
                return postIds.stream()
                        .map(this::convertToLong)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("[缓存] 获取热门帖子ID失败 - key: {}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存热门帖子排行
     * @param postScores 帖子ID和分数的映射
     */
    public void cacheHotPostRank(Map<Long, Double> postScores) {
        if (postScores == null || postScores.isEmpty()) {
            cacheEmptyResult();
            return;
        }

        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            deleteCache(redisKey); // 先清空旧数据
            
            // 批量插入新数据
            postScores.forEach((postId, score) -> {
                if (score != null && postId != null) {
                    redisTemplate.opsForZSet().add(redisKey, postId.toString(), score);
                }
            });
            
            expire(redisKey, DEFAULT_CACHE_TTL);
            
            log.debug("缓存热门帖子排行成功: key={}, size={}", redisKey, postScores.size());
        } catch (Exception e) {
            log.error("[缓存] 缓存热门帖子排行失败 - key: {}", redisKey, e);
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        setValue(redisKey, "1", EMPTY_RESULT_TTL);
    }

    /**
     * 检查是否为空结果缓存
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        return hasKey(redisKey);
    }

    /**
     * 清理无效的缓存数据
     * @param invalidIds 无效的帖子ID列表
     */
    public void cleanupInvalidCacheData(List<Long> invalidIds) {
        if (invalidIds == null || invalidIds.isEmpty()) {
            return;
        }
        
        String redisKey = RedisKeyManager.postHotRankKey();
        
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

    /**
     * 获取帖子的热度分数
     * @param postId 帖子ID
     * @return 热度分数，不存在返回null
     */
    public Double getHotScore(Long postId) {
        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            return redisTemplate.opsForZSet().score(redisKey, postId.toString());
        } catch (Exception e) {
            log.error("[缓存] 获取热度分数失败 - key: {}, postId: {}", redisKey, postId, e);
            return null;
        }
    }

    /**
     * 获取热度排行的总数量
     * @return 排行总数量
     */
    public Long getHotRankSize() {
        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            return redisTemplate.opsForZSet().size(redisKey);
        } catch (Exception e) {
            log.error("[缓存] 获取热度排行总数量失败 - key: {}", redisKey, e);
            return 0L;
        }
    }
}