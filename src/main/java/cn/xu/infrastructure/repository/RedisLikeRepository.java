package cn.xu.infrastructure.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    // Lua脚本
    private DefaultRedisScript<Long> saveLikeScript;
    private DefaultRedisScript<Long> removeLikeScript;

    @PostConstruct
    public void init() {
        // 初始化保存点赞的Lua脚本
        saveLikeScript = new DefaultRedisScript<>();
        saveLikeScript.setResultType(Long.class);
        saveLikeScript.setScriptText(
                "local count_key = KEYS[1] " +
                        "local user_key = KEYS[2] " +
                        "local user_id = ARGV[1] " +
                        "if redis.call('SADD', user_key, user_id) == 1 then " +
                        "  local count = redis.call('INCR', count_key) " +
                        "  return count " +
                        "else " +
                        "  return -1 " +
                        "end"
        );

        // 初始化移除点赞的Lua脚本
        removeLikeScript = new DefaultRedisScript<>();
        removeLikeScript.setResultType(Long.class);
        removeLikeScript.setScriptText(
                "local count_key = KEYS[1] " +
                        "local user_key = KEYS[2] " +
                        "local user_id = ARGV[1] " +
                        "if redis.call('SREM', user_key, user_id) == 1 then " +
                        "  local count = redis.call('DECR', count_key) " +
                        "  if count < 0 then " +
                        "    redis.call('SET', count_key, 0) " +
                        "    return 0 " +
                        "  end " +
                        "  return count " +
                        "else " +
                        "  return -1 " +
                        "end"
        );
    }

    @Override
    public void save(Like like) {
        try {
            String countKey = buildCountKey(like.getType(), like.getTargetId());
            String userKey = buildUserKey(like.getType(), like.getTargetId());

            if (like.isLiked()) {
                // 执行Lua脚本添加点赞
                Long result = redisTemplate.execute(
                        saveLikeScript,
                        Arrays.asList(countKey, userKey),
                        String.valueOf(like.getUserId())
                );

                if (result != null && result >= 0) {
                    // 设置过期时间
                    redisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                    redisTemplate.expire(userKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
                    log.info("Redis保存点赞记录成功: {}", like);
                } else {
                    log.info("Redis用户已点赞: {}", like);
                }
            } else {
                // 执行Lua脚本移除点赞
                Long result = redisTemplate.execute(
                        removeLikeScript,
                        Arrays.asList(countKey, userKey),
                        String.valueOf(like.getUserId())
                );

                if (result != null && result >= 0) {
                    log.info("Redis移除点赞记录成功: {}", like);
                } else {
                    log.info("Redis用户未点赞: {}", like);
                }
            }
        } catch (Exception e) {
            log.error("Redis保存点赞记录失败: {}, error: {}", like, e.getMessage());
            throw new RuntimeException("Redis保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        try {
            String countKey = buildCountKey(type, targetId);
            Object count = redisTemplate.opsForValue().get(countKey);
            return convertToLong(count);
        } catch (Exception e) {
            log.error("Redis获取点赞数量失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        try {
            String userKey = buildUserKey(type, targetId);
            return Boolean.TRUE.equals(
                    redisTemplate.opsForSet().isMember(userKey, String.valueOf(userId))
            );
        } catch (Exception e) {
            log.error("Redis检查点赞状态失败: userId={}, targetId={}, type={}, error={}",
                    userId, targetId, type, e.getMessage());
            return false;
        }
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        try {
            String countKey = buildCountKey(type, targetId);
            String userKey = buildUserKey(type, targetId);

            // 执行Lua脚本移除点赞
            Long result = redisTemplate.execute(
                    removeLikeScript,
                    Arrays.asList(countKey, userKey),
                    String.valueOf(userId)
            );

            if (result != null && result >= 0) {
                log.info("Redis删除点赞记录成功: userId={}, targetId={}, type={}",
                        userId, targetId, type);
            } else {
                log.info("Redis用户未点赞: userId={}, targetId={}, type={}",
                        userId, targetId, type);
            }
        } catch (Exception e) {
            log.error("Redis删除点赞记录失败: userId={}, targetId={}, type={}, error={}",
                    userId, targetId, type, e.getMessage());
            throw new RuntimeException("Redis删除点赞记录失败", e);
        }
    }

    @Override
    public Set<Long> getLikedUserIds(Long targetId, LikeType type) {
        try {
            String userKey = buildUserKey(type, targetId);
            Set<Object> members = redisTemplate.opsForSet().members(userKey);
            if (members == null || members.isEmpty()) {
                return Collections.emptySet();
            }
            return members.stream()
                    .map(String::valueOf)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Redis获取点赞用户列表失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public Set<Like> getPageByType(LikeType type, Integer offset, Integer limit) {
        // Redis不支持分页查询，返回空集合
        return Collections.emptySet();
    }

    @Override
    public Long countByType(LikeType type) {
        // Redis不支持统计总数，返回0
        return 0L;
    }

    @Override
    public void syncToCache(Long targetId, LikeType type) {
        // 由CompositeLikeRepository负责同步，这里不需要实现
        log.debug("Redis不需要主动同步缓存");
    }

    @Override
    public void cleanExpiredCache() {
        // 由Redis自动过期机制处理，这里不需要实现
        log.debug("Redis通过TTL自动处理缓存过期");
    }

    private String buildCountKey(LikeType type, Long targetId) {
        return LIKE_COUNT_KEY + type.name().toLowerCase() + ":" + targetId;
    }

    private String buildUserKey(LikeType type, Long targetId) {
        return USER_LIKE_KEY + type.name().toLowerCase() + ":" + targetId;
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