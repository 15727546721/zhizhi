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
    
    // Redis key 前缀
    private static final String LIKE_COUNT_KEY = "like:count:";  // 点赞数
    private static final String USER_LIKE_KEY = "like:user:";    // 用户点赞集合
    private static final int CACHE_EXPIRE_DAYS = 30;             // 缓存过期时间
    
    @Override
    public void save(Like like) {
        try {
            String countKey = LIKE_COUNT_KEY + like.getType().name().toLowerCase() + ":" + like.getTargetId();
            String userLikeKey = USER_LIKE_KEY + like.getType().name().toLowerCase() + ":" + like.getTargetId();
            
            if (like.isLiked()) {
                // 增加点赞数并确保返回Long类型
                Object result = redisTemplate.opsForValue().increment(countKey, 1L);
                Long count = convertToLong(result);
                if (count == null) {
                    redisTemplate.opsForValue().set(countKey, 1L);
                }
                redisTemplate.opsForSet().add(userLikeKey, String.valueOf(like.getUserId()));
            } else {
                // 减少点赞数并确保返回Long类型
                Object result = redisTemplate.opsForValue().decrement(countKey, 1L);
                Long count = convertToLong(result);
                if (count == null || count < 0) {
                    redisTemplate.opsForValue().set(countKey, 0L);
                }
                redisTemplate.opsForSet().remove(userLikeKey, String.valueOf(like.getUserId()));
            }
            
            // 设置过期时间
            redisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            redisTemplate.expire(userLikeKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.error("Redis保存点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("Redis保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            String countKey = LIKE_COUNT_KEY + type.name().toLowerCase() + ":" + targetId;
            Object count = redisTemplate.opsForValue().get(countKey);
            return convertToLong(count);
        } catch (Exception e) {
            log.error("Redis获取点赞数量失败: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        try {
            String userLikeKey = USER_LIKE_KEY + type.name().toLowerCase() + ":" + targetId;
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(userLikeKey, String.valueOf(userId)));
        } catch (Exception e) {
            log.error("Redis获取点赞状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void batchUpdateLikeCount(Map<String, Long> likeCounts) {
        try {
            likeCounts.forEach((key, count) -> {
                String[] parts = key.split(":");
                if (parts.length >= 2) {
                    String countKey = LIKE_COUNT_KEY + parts[0] + ":" + parts[1];
                    // 确保存储为Long类型
                    redisTemplate.opsForValue().set(countKey, count.longValue());
                    redisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                }
            });
        } catch (Exception e) {
            log.error("Redis批量更新点赞数失败: {}", e.getMessage());
            throw new RuntimeException("Redis批量更新点赞数失败", e);
        }
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        try {
            String countKey = LIKE_COUNT_KEY + type.name().toLowerCase() + ":" + targetId;
            String userLikeKey = USER_LIKE_KEY + type.name().toLowerCase() + ":" + targetId;
            
            Object result = redisTemplate.opsForValue().decrement(countKey, 1L);
            Long count = convertToLong(result);
            if (count == null || count < 0) {
                redisTemplate.opsForValue().set(countKey, 0L);
            }
            redisTemplate.opsForSet().remove(userLikeKey, String.valueOf(userId));
        } catch (Exception e) {
            log.error("Redis删除点赞记录失败: {}", e.getMessage());
            throw new RuntimeException("Redis删除点赞记录失败", e);
        }
    }

    /**
     * 将 Object 转换为 Long 类型
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
} 