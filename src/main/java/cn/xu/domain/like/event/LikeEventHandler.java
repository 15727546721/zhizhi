package cn.xu.domain.like.event;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.repository.ILikeRepository;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 点赞事件处理器
 */
@Slf4j
@Component
public class LikeEventHandler implements EventHandler<LikeEvent> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ILikeRepository likeRepository;

    // Redis key 前缀
    private static final String LOCK_KEY_PREFIX = "like:lock:";
    private static final String LIKE_COUNT_KEY = "like:count:";
    private static final String USER_SET_KEY = "like:users:";
    
    // 时间常量
    private static final long LOCK_WAIT_TIME = 3000L;
    private static final long LOCK_LEASE_TIME = 5000L;
    private static final long CACHE_EXPIRE_DAYS = 7;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        if (event == null || event.getTargetId() == null || event.getUserId() == null || event.getType() == null) {
            log.error("无效的点赞事件: {}", event);
            return;
        }

        String lockKey = LOCK_KEY_PREFIX + event.getType().name().toLowerCase() + ":" + event.getTargetId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取分布式锁
            if (!lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS)) {
                log.warn("获取{}[{}]的锁失败，事件处理稍后重试", event.getType().getDescription(), event.getTargetId());
                return;
            }

            // 更新Redis中的点赞数量
            updateRedisLikeCount(event);
            
            // 更新MySQL中的点赞关系记录
            updateMySQLLikeRelation(event);

        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void updateRedisLikeCount(LikeEvent event) {
        String countKey = LIKE_COUNT_KEY + event.getType().name().toLowerCase() + ":" + event.getTargetId();
        String userSetKey = USER_SET_KEY + event.getType().name().toLowerCase() + ":" + event.getTargetId();

        try {
            String userId = String.valueOf(event.getUserId());
            
            if (event.isLiked()) {
                // 尝试将用户ID添加到Set中，如果添加成功说明之前没有点赞
                Boolean added = redisTemplate.opsForSet().add(userSetKey, userId) == 1;
                if (added) {
                    // 增加点赞计数
                    redisTemplate.opsForValue().increment(countKey, 1L);
                    log.info("用户[{}]点赞了{}[{}]", 
                            event.getUserId(), event.getType().getDescription(), event.getTargetId());
                } else {
                    log.info("用户[{}]已经点赞过{}[{}]", 
                            event.getUserId(), event.getType().getDescription(), event.getTargetId());
                }
            } else {
                // 尝试从Set中移除用户ID，如果移除成功说明之前确实点赞了
                Boolean removed = redisTemplate.opsForSet().remove(userSetKey, userId) == 1;
                if (removed) {
                    // 减少点赞计数，确保不会小于0
                    Object currentValue = redisTemplate.opsForValue().get(countKey);
                    Long currentCount = convertToLong(currentValue);
                    if (currentCount > 0) {
                        redisTemplate.opsForValue().decrement(countKey, 1L);
                    }
                    log.info("用户[{}]取消点赞了{}[{}]", 
                            event.getUserId(), event.getType().getDescription(), event.getTargetId());
                } else {
                    log.info("用户[{}]没有点赞过{}[{}]", 
                            event.getUserId(), event.getType().getDescription(), event.getTargetId());
                }
            }
            
            // 设置过期时间
            redisTemplate.expire(countKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
            redisTemplate.expire(userSetKey, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("更新Redis点赞数失败: {}", e.getMessage());
            throw new RuntimeException("更新Redis点赞数失败", e);
        }
    }

    private void updateMySQLLikeRelation(LikeEvent event) {
        try {
            // 使用工厂方法创建Like实例
            Like like = Like.create(event.getUserId(), event.getTargetId(), event.getType());
            
            // 根据事件状态设置点赞状态
            if (!event.isLiked()) {
                like.cancel();
            }
            
            // 保存点赞关系到MySQL
            likeRepository.save(like);
            
        } catch (Exception e) {
            log.error("更新MySQL点赞关系失败: {}", e.getMessage());
            throw new RuntimeException("更新MySQL点赞关系失败", e);
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