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
    private static final String PROCESSED_EVENT_KEY = "like:processed:";
    
    // 时间常量
    private static final long LOCK_WAIT_TIME = 3000L;
    private static final long LOCK_LEASE_TIME = 5000L;
    private static final long EVENT_EXPIRE_TIME = 3600L;

    @Override
    public void onEvent(LikeEvent event, long sequence, boolean endOfBatch) {
        if (event == null || event.getTargetId() == null || event.getUserId() == null || event.getType() == null) {
            log.error("无效的点赞事件: {}", event);
            return;
        }

        // 生成事件唯一标识
        String eventId = generateEventId(event);
        // 检查事件是否已处理
        if (isEventProcessed(eventId)) {
            log.info("事件已处理，跳过: {}", eventId);
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

            // 更新MySQL中的点赞关系记录
            updateMySQLLikeRelation(event);
            
            // 标记事件为已处理
            markEventAsProcessed(eventId);

        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
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
     * 生成事件唯一标识
     */
    private String generateEventId(LikeEvent event) {
        return String.format("%s:%d:%d:%d:%b",
                PROCESSED_EVENT_KEY,
                event.getUserId(),
                event.getTargetId(),
                event.getType().getCode(),
                event.isLiked());
    }

    /**
     * 检查事件是否已处理
     */
    private boolean isEventProcessed(String eventId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(eventId));
    }

    /**
     * 标记事件为已处理
     */
    private void markEventAsProcessed(String eventId) {
        redisTemplate.opsForValue().set(eventId, "1", EVENT_EXPIRE_TIME, TimeUnit.SECONDS);
    }
} 