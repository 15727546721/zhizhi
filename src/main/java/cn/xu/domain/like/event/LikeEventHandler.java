package cn.xu.domain.like.event;

import cn.xu.domain.like.repository.ILikeRepository;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final String LOCK_KEY_PREFIX = "like:lock:";
    private static final long LOCK_WAIT_TIME = 3000L;
    private static final long LOCK_LEASE_TIME = 5000L;

    // 用于存储待同步的点赞数据
    private final Map<String, Long> pendingLikeCounts = new ConcurrentHashMap<>();
    // 上次同步时间
    private volatile long lastSyncTime = System.currentTimeMillis();
    // 同步间隔（毫秒）
    private static final long SYNC_INTERVAL = 5000;
    // 批量同步阈值
    private static final int BATCH_SYNC_THRESHOLD = 100;

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

            handleLikeEvent(event);
            
            // 检查是否需要同步数据到数据库
            checkAndSyncToDatabase(endOfBatch);

        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void handleLikeEvent(LikeEvent event) {
        // 构建Redis key
        String countKey = String.format("%s:like:%d:count", 
                event.getType().name().toLowerCase(), event.getTargetId());

        try {
            // 更新点赞计数
            Long currentCount = redisTemplate.opsForValue().get(countKey) != null ? 
                    (Long) redisTemplate.opsForValue().get(countKey) : 0L;
            
            Long newCount = event.isLiked() ? currentCount + 1 : Math.max(0, currentCount - 1);
            
            // 更新Redis中的计数
            redisTemplate.opsForValue().set(countKey, newCount);

            // 添加到待同步队列
            pendingLikeCounts.put(countKey, newCount);
            
            log.info("{}[{}]点赞数更新为: {}", 
                    event.getType().getDescription(), event.getTargetId(), newCount);

        } catch (Exception e) {
            log.error("处理点赞事件失败: {}", e.getMessage());
            // 发生异常时回滚Redis操作
            if (event.isLiked()) {
                Long currentCount = (Long) redisTemplate.opsForValue().get(countKey);
                if (currentCount != null && currentCount > 0) {
                    redisTemplate.opsForValue().decrement(countKey);
                }
            } else {
                Long currentCount = (Long) redisTemplate.opsForValue().get(countKey);
                if (currentCount != null) {
                    redisTemplate.opsForValue().increment(countKey);
                }
            }
            throw new RuntimeException("处理点赞事件失败", e);
        }
    }

    private void checkAndSyncToDatabase(boolean endOfBatch) {
        long currentTime = System.currentTimeMillis();
        boolean shouldSync = endOfBatch && (
                pendingLikeCounts.size() >= BATCH_SYNC_THRESHOLD ||
                        (currentTime - lastSyncTime) >= SYNC_INTERVAL
        );

        if (shouldSync && !pendingLikeCounts.isEmpty()) {
            try {
                // 批量更新数据库
                likeRepository.batchUpdateLikeCount(pendingLikeCounts);
                log.info("同步{}个目标的点赞数到数据库", pendingLikeCounts.size());

                // 清空待同步队列
                pendingLikeCounts.clear();
                // 更新同步时间
                lastSyncTime = currentTime;

            } catch (Exception e) {
                log.error("同步点赞数据到数据库失败: {}", e.getMessage());
            }
        }
    }
} 