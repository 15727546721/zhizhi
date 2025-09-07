package cn.xu.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章缓存仓储
 * 专门处理文章相关的缓存操作，遵循DDD原则
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final int DEFAULT_CACHE_TTL = 1800; // 30分钟
    private static final int EMPTY_RESULT_TTL = 60;    // 1分钟

    /**
     * 获取热门文章ID列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 文章ID列表
     */
    public List<Long> getHotArticleIds(int start, int end) {
        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            Set<Object> articleIds = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
            
            if (articleIds != null && !articleIds.isEmpty()) {
                return articleIds.stream()
                        .map(this::safeParseArticleId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从Redis获取热门文章ID失败: key={}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存热门文章排行
     * @param articleScores 文章ID和分数的映射
     */
    public void cacheHotArticleRank(Map<Long, Double> articleScores) {
        if (articleScores == null || articleScores.isEmpty()) {
            cacheEmptyResult();
            return;
        }

        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            // 先清空旧数据
            redisTemplate.delete(redisKey);
            
            // 批量插入新数据
            articleScores.forEach((articleId, score) -> {
                if (score != null && articleId != null) {
                    redisTemplate.opsForZSet().add(redisKey, articleId.toString(), score);
                }
            });
            
            // 设置过期时间
            redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            
            log.debug("缓存热门文章排行成功: key={}, size={}", redisKey, articleScores.size());
        } catch (Exception e) {
            log.error("缓存热门文章排行失败: key={}", redisKey, e);
        }
    }

    /**
     * 更新单个文章的热度排行
     * @param articleId 文章ID
     * @param hotScore 热度分数
     */
    public void updateHotRank(Long articleId, double hotScore) {
        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            redisTemplate.opsForZSet().add(redisKey, articleId.toString(), hotScore);
            log.debug("更新文章热度排行成功: key={}, articleId={}, score={}", redisKey, articleId, hotScore);
        } catch (Exception e) {
            log.error("更新文章热度排行失败: key={}, articleId={}, score={}", redisKey, articleId, hotScore, e);
        }
    }

    /**
     * 批量更新文章热度排行
     * @param articleScores 文章ID和分数的映射
     */
    public void batchUpdateHotRank(Map<Long, Double> articleScores) {
        if (articleScores == null || articleScores.isEmpty()) {
            return;
        }

        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            // 批量更新热度排行
            articleScores.forEach((articleId, score) -> {
                if (score != null && articleId != null) {
                    redisTemplate.opsForZSet().add(redisKey, articleId.toString(), score);
                }
            });
            
            log.debug("批量更新文章热度排行成功: key={}, size={}", redisKey, articleScores.size());
        } catch (Exception e) {
            log.error("批量更新文章热度排行失败: key={}", redisKey, e);
        }
    }

    /**
     * 从热度排行中移除文章
     * @param articleId 文章ID
     */
    public void removeFromHotRank(Long articleId) {
        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            redisTemplate.opsForZSet().remove(redisKey, articleId.toString());
            log.debug("从热度排行移除文章成功: key={}, articleId={}", redisKey, articleId);
        } catch (Exception e) {
            log.error("从热度排行移除文章失败: key={}, articleId={}", redisKey, articleId, e);
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult() {
        String redisKey = RedisKeyManager.articleHotRankKey() + ":empty";
        
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
        String redisKey = RedisKeyManager.articleHotRankKey() + ":empty";
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("检查空结果缓存失败: key={}", redisKey, e);
            return false;
        }
    }

    /**
     * 清理无效的缓存数据
     * @param invalidIds 无效的文章ID列表
     */
    public void cleanupInvalidCacheData(List<Long> invalidIds) {
        if (invalidIds == null || invalidIds.isEmpty()) {
            return;
        }
        
        String redisKey = RedisKeyManager.articleHotRankKey();
        
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
     * 获取文章的热度分数
     * @param articleId 文章ID
     * @return 热度分数，不存在返回null
     */
    public Double getHotScore(Long articleId) {
        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            return redisTemplate.opsForZSet().score(redisKey, articleId.toString());
        } catch (Exception e) {
            log.error("获取文章热度分数失败: key={}, articleId={}", redisKey, articleId, e);
            return null;
        }
    }

    /**
     * 获取热度排行的总数量
     * @return 排行总数量
     */
    public Long getHotRankSize() {
        String redisKey = RedisKeyManager.articleHotRankKey();
        
        try {
            return redisTemplate.opsForZSet().size(redisKey);
        } catch (Exception e) {
            log.error("获取热度排行总数量失败: key={}", redisKey, e);
            return 0L;
        }
    }

    /**
     * 安全解析文章ID
     * @param id Redis中的ID对象
     * @return 解析后的Long类型ID，解析失败返回null
     */
    private Long safeParseArticleId(Object id) {
        if (id == null) {
            return null;
        }
        
        try {
            return Long.parseLong(id.toString());
        } catch (NumberFormatException e) {
            log.warn("Redis中存在无效的文章ID: {}", id);
            return null;
        }
    }
}