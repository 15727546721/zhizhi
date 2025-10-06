package cn.xu.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子缓存仓储
 * 专门处理帖子相关的缓存操作，遵循DDD原则
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PostCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final int DEFAULT_CACHE_TTL = 1800; // 30分钟
    private static final int EMPTY_RESULT_TTL = 60;    // 1分钟

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
                        .map(this::safeParsePostId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从Redis获取热门帖子ID失败: key={}", redisKey, e);
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
            // 先清空旧数据
            redisTemplate.delete(redisKey);
            
            // 批量插入新数据
            postScores.forEach((postId, score) -> {
                if (score != null && postId != null) {
                    redisTemplate.opsForZSet().add(redisKey, postId.toString(), score);
                }
            });
            
            // 设置过期时间
            redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            
            log.debug("缓存热门帖子排行成功: key={}, size={}", redisKey, postScores.size());
        } catch (Exception e) {
            log.error("缓存热门帖子排行失败: key={}", redisKey, e);
        }
    }

    /**
     * 更新单个帖子的热度排行
     * @param postId 帖子ID
     * @param hotScore 热度分数
     */
    public void updateHotRank(Long postId, double hotScore) {
        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            redisTemplate.opsForZSet().add(redisKey, postId.toString(), hotScore);
            log.debug("更新帖子热度排行成功: key={}, postId={}, score={}", redisKey, postId, hotScore);
        } catch (Exception e) {
            log.error("更新帖子热度排行失败: key={}, postId={}, score={}", redisKey, postId, hotScore, e);
        }
    }

    /**
     * 批量更新帖子热度排行
     * @param postScores 帖子ID和分数的映射
     */
    public void batchUpdateHotRank(Map<Long, Double> postScores) {
        if (postScores == null || postScores.isEmpty()) {
            return;
        }

        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            // 批量更新热度排行
            postScores.forEach((postId, score) -> {
                if (score != null && postId != null) {
                    redisTemplate.opsForZSet().add(redisKey, postId.toString(), score);
                }
            });
            
            log.debug("批量更新帖子热度排行成功: key={}, size={}", redisKey, postScores.size());
        } catch (Exception e) {
            log.error("批量更新帖子热度排行失败: key={}", redisKey, e);
        }
    }

    /**
     * 从热度排行中移除帖子
     * @param postId 帖子ID
     */
    public void removeFromHotRank(Long postId) {
        String redisKey = RedisKeyManager.postHotRankKey();
        
        try {
            redisTemplate.opsForZSet().remove(redisKey, postId.toString());
            log.debug("从热度排行移除帖子成功: key={}, postId={}", redisKey, postId);
        } catch (Exception e) {
            log.error("从热度排行移除帖子失败: key={}, postId={}", redisKey, postId, e);
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        
        try {
            redisTemplate.opsForValue().set(redisKey, "1", EMPTY_RESULT_TTL, TimeUnit.SECONDS);
            log.debug("缓存空结果成功: key={}", redisKey);
        } catch (Exception e) {
            log.error("缓存空结果失败: key={}", redisKey, e);
        }
    }

    /**
     * 检查是否为空结果缓存
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("检查空结果缓存失败: key={}", redisKey, e);
            return false;
        }
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
            
            log.info("清理无效缓存数据成功: key={}, 清理数量={}", redisKey, invalidIds.size());
        } catch (Exception e) {
            log.error("清理无效缓存数据失败: key={}", redisKey, e);
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
            log.error("获取帖子热度分数失败: key={}, postId={}", redisKey, postId, e);
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
            log.error("获取热度排行总数量失败: key={}", redisKey, e);
            return 0L;
        }
    }

    /**
     * 安全解析帖子ID
     * @param id Redis中的ID对象
     * @return 解析后的Long类型ID，解析失败返回null
     */
    private Long safeParsePostId(Object id) {
        if (id == null) {
            return null;
        }
        
        try {
            return Long.parseLong(id.toString());
        } catch (NumberFormatException e) {
            log.warn("Redis中存在无效的帖子ID: {}", id);
            return null;
        }
    }
}