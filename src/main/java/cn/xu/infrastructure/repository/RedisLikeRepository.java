package cn.xu.infrastructure.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis点赞仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisLikeRepository implements ILikeRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LIKE_KEY_PREFIX = "like:";
    private static final String LIKE_COUNT_KEY_PREFIX = "like:count:";
    private static final long CACHE_EXPIRE_DAYS = 30;

    @Override
    public void save(Like like) {
        String likeKey = getLikeKey(like.getUserId(), like.getTargetId(), like.getType());
        String countKey = getLikeCountKey(like.getTargetId(), like.getType());

        try {
            if (like.isLiked()) {
                // 保存点赞记录
                redisTemplate.opsForValue().set(likeKey, true, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                // 增加点赞计数
                redisTemplate.opsForValue().increment(countKey);
            } else {
                // 删除点赞记录
                redisTemplate.delete(likeKey);
                // 减少点赞计数
                Object countObj = redisTemplate.opsForValue().get(countKey);
                long count = 0;
                if (countObj != null) {
                    count = countObj instanceof Long ? (Long) countObj : ((Integer) countObj).longValue();
                }
                if (count > 0) {
                    redisTemplate.opsForValue().decrement(countKey);
                }
            }
            // 设置计数的过期时间
            redisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("保存点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            String countKey = getLikeCountKey(targetId, type);
            Object countObj = redisTemplate.opsForValue().get(countKey);
            if (countObj == null) {
                return 0L;
            }
            return countObj instanceof Long ? (Long) countObj : ((Integer) countObj).longValue();
        } catch (Exception e) {
            log.error("获取点赞数量失败: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        try {
            String key = getLikeKey(userId, targetId, type);
            Object value = redisTemplate.opsForValue().get(key);
            return value != null && (Boolean) value;
        } catch (Exception e) {
            log.error("获取点赞状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void batchUpdateLikeCount(Map<String, Long> likeCounts) {
        try {
            likeCounts.forEach((key, count) -> {
                redisTemplate.opsForValue().set(key, count);
                redisTemplate.expire(key, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            });
        } catch (Exception e) {
            log.error("批量更新点赞数失败: {}", e.getMessage());
            throw new RuntimeException("批量更新点赞数失败", e);
        }
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        try {
            String likeKey = getLikeKey(userId, targetId, type);
            String countKey = getLikeCountKey(targetId, type);

            // 删除点赞记录
            redisTemplate.delete(likeKey);

            // 减少点赞计数
            Object countObj = redisTemplate.opsForValue().get(countKey);
            long count = 0;
            if (countObj != null) {
                count = countObj instanceof Long ? (Long) countObj : ((Integer) countObj).longValue();
            }
            if (count > 0) {
                redisTemplate.opsForValue().decrement(countKey);
            }
        } catch (Exception e) {
            log.error("删除点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("删除点赞记录失败", e);
        }
    }

    private String getLikeKey(Long userId, Long targetId, LikeType type) {
        return LIKE_KEY_PREFIX + type.name() + ":" + targetId + ":" + userId;
    }

    private String getLikeCountKey(Long targetId, LikeType type) {
        return LIKE_COUNT_KEY_PREFIX + type.name() + ":" + targetId;
    }
} 