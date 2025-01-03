package cn.xu.infrastructure.repository.redis;

import cn.xu.domain.like.model.LikeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis点赞仓储实现
 * 使用Redis Set存储用户点赞记录，通过Set的大小获取点赞数
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisLikeRepositoryImpl implements IRedisLikeRepository {

    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis key 前缀 - 使用单个SET存储用户ID
    private static final String USER_SET_KEY = "like:users:";

    @Override
    public void saveLike(Long userId, Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            Long result = redisTemplate.opsForSet().add(key, String.valueOf(userId));
            
            if (result != null && result > 0) {
                log.info("用户点赞成功: userId={}, targetId={}, type={}", userId, targetId, type);
            } else {
                log.info("用户已点赞: userId={}, targetId={}, type={}", userId, targetId, type);
            }
        } catch (Exception e) {
            log.error("Redis保存点赞失败: userId={}, targetId={}, type={}, error={}", 
                    userId, targetId, type, e.getMessage());
            throw new RuntimeException("保存点赞失败", e);
        }
    }

    @Override
    public void removeLike(Long userId, Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            Long result = redisTemplate.opsForSet().remove(key, String.valueOf(userId));
            
            if (result != null && result > 0) {
                log.info("取消点赞成功: userId={}, targetId={}, type={}", userId, targetId, type);
            } else {
                log.info("用户未点赞: userId={}, targetId={}, type={}", userId, targetId, type);
            }
        } catch (Exception e) {
            log.error("Redis删除点赞失败: userId={}, targetId={}, type={}, error={}", 
                    userId, targetId, type, e.getMessage());
            throw new RuntimeException("删除点赞失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("Redis获取点赞数失败: targetId={}, type={}, error={}", 
                    targetId, type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean hasLiked(Long userId, Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            Boolean result = redisTemplate.opsForSet().isMember(key, String.valueOf(userId));
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis查询点赞状态失败: userId={}, targetId={}, type={}, error={}", 
                    userId, targetId, type, e.getMessage());
            return false;
        }
    }

    @Override
    public Set<Long> getLikedUserIds(Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            Set<String> members = redisTemplate.opsForSet().members(key);
            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }
            return members.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Redis获取点赞用户列表失败: targetId={}, type={}, error={}", 
                    targetId, type, e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public void cleanCache(Long targetId, LikeType type) {
        try {
            String key = buildKey(type, targetId);
            redisTemplate.delete(key);
            log.info("清除点赞数据成功: targetId={}, type={}", targetId, type);
        } catch (Exception e) {
            log.error("Redis清除点赞数据失败: targetId={}, type={}, error={}", 
                    targetId, type, e.getMessage());
            throw new RuntimeException("清除点赞数据失败", e);
        }
    }

    @Override
    public void batchUpdateLikeCount(String key, Long count) {
        // 不再需要单独更新点赞数，因为点赞数是通过Set size动态获取的
        log.info("批量更新点赞数已废弃，点赞数通过Set size动态获取");
    }

    /**
     * 构建Redis key
     * 格式：like:users:{type}:{targetId}
     */
    private String buildKey(LikeType type, Long targetId) {
        return USER_SET_KEY + type.name().toLowerCase() + ":" + targetId;
    }
} 