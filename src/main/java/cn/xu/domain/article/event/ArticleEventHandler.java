package cn.xu.domain.article.event;

import cn.xu.domain.article.repository.IArticleRepository;
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
 * 文章事件处理器（文章消费者）
 */
@Slf4j
@Component
public class ArticleEventHandler implements EventHandler<ArticleEvent> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private IArticleRepository articleRepository;
    @Resource
    private ILikeRepository likeRepository;

    private static final String LOCK_KEY_PREFIX = "article:lock:";
    private static final long LOCK_WAIT_TIME = 3000L;
    private static final long LOCK_LEASE_TIME = 5000L;
    
    // 用于存储待同步的点赞数据
    private final Map<Long, Long> pendingLikeCounts = new ConcurrentHashMap<>();
    // 上次同步时间
    private volatile long lastSyncTime = System.currentTimeMillis();
    // 同步间隔（毫秒）
    private static final long SYNC_INTERVAL = 5000;
    // 批量同步阈值
    private static final int BATCH_SYNC_THRESHOLD = 100;

    @Override
    public void onEvent(ArticleEvent event, long sequence, boolean endOfBatch) {
        String lockKey = LOCK_KEY_PREFIX + event.getArticleId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取分布式锁
            if (!lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.MILLISECONDS)) {
                log.warn("获取文章{}的锁失败，事件处理稍后重试", event.getArticleId());
                return;
            }
            
            switch (event.getType()) {
                case LIKE:
                    handleLikeEvent(event);
                    break;
                case COMMENT:
                    handleCommentEvent(event);
                    break;
                default:
                    log.warn("未知的事件类型：{}", event.getType());
            }
            
            // 检查是否需要同步数据到数据库
            checkAndSyncToDatabase(endOfBatch);
            
        } catch (Exception e) {
            log.error("处理文章事件失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    private void handleLikeEvent(ArticleEvent event) {
        // 构建Redis key
        String countKey = String.format("article:like:%d:count", event.getArticleId());
        
        try {
            // 更新点赞计数
            Long newCount;
            if (event.isAdd()) {
                newCount = redisTemplate.opsForValue().increment(countKey);
            } else {
                newCount = redisTemplate.opsForValue().decrement(countKey);
            }
            
            // 更新数据库记录
            likeRepository.insertArticleLikeRecord(event);
            
            // 添加到待同步队列
            if (newCount != null) {
                pendingLikeCounts.put(event.getArticleId(), newCount);
            }
            
        } catch (Exception e) {
            log.error("处理点赞事件失败", e);
            // 发生异常时回滚Redis操作
            if (event.isAdd()) {
                redisTemplate.opsForValue().decrement(countKey);
            } else {
                redisTemplate.opsForValue().increment(countKey);
            }
            throw e;
        }
    }
    
    private void handleCommentEvent(ArticleEvent event) {
        // TODO: 实现评论事件处理逻辑
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
                articleRepository.batchUpdateArticleLikeCount(pendingLikeCounts);
                log.info("同步{}篇文章的点赞数到数据库", pendingLikeCounts.size());
                
                // 清空待同步队列
                pendingLikeCounts.clear();
                // 更新同步时间
                lastSyncTime = currentTime;
                
            } catch (Exception e) {
                log.error("同步点赞数据到数据库失败", e);
            }
        }
    }
}

