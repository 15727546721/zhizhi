package cn.xu.infrastructure.repository;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class CompositeLikeRepository implements ILikeRepository {

    private final RedisLikeRepository redisLikeRepository;
    private final LikeRepository mysqlLikeRepository;
    
    // 用于存储待同步到MySQL的数据
    private final ConcurrentHashMap<String, Long> pendingSync = new ConcurrentHashMap<>();

    @Override
    public void save(Like like) {
        // 先写入Redis
        redisLikeRepository.save(like);
        
        // 添加到待同步队列
        String key = like.getType().name().toLowerCase() + ":" + like.getTargetId();
        pendingSync.put(key, redisLikeRepository.getLikeCount(like.getTargetId(), like.getType()));
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        // 优先从Redis获取
        Long count = redisLikeRepository.getLikeCount(targetId, type);
        if (count == null || count == 0) {
            // Redis中没有数据，从MySQL获取
            count = mysqlLikeRepository.getLikeCount(targetId, type);
            // 将MySQL中的数据写入Redis
            if (count > 0) {
                Map<String, Long> countMap = new HashMap<>();
                countMap.put(type.name().toLowerCase() + ":" + targetId, count);
                redisLikeRepository.batchUpdateLikeCount(countMap);
            }
        }
        return count;
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        // 优先从Redis查询
        boolean liked = redisLikeRepository.isLiked(userId, targetId, type);
        if (!liked) {
            // Redis中没有，查询MySQL
            liked = mysqlLikeRepository.isLiked(userId, targetId, type);
        }
        return liked;
    }

    @Override
    public void batchUpdateLikeCount(Map<String, Long> likeCounts) {
        // 更新Redis
        redisLikeRepository.batchUpdateLikeCount(likeCounts);
        // 更新MySQL
        mysqlLikeRepository.batchUpdateLikeCount(likeCounts);
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        // 先删除Redis中的数据
        redisLikeRepository.delete(userId, targetId, type);
        // 再删除MySQL中的数据
        mysqlLikeRepository.delete(userId, targetId, type);
    }

    /**
     * 定时同步Redis数据到MySQL
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void syncToMySQL() {
        try {
            if (!pendingSync.isEmpty()) {
                Map<String, Long> currentBatch = new ConcurrentHashMap<>(pendingSync);
                pendingSync.clear();
                
                // 批量更新MySQL
                mysqlLikeRepository.batchUpdateLikeCount(currentBatch);
                log.info("同步点赞数据到MySQL成功，同步数量: {}", currentBatch.size());
            }
        } catch (Exception e) {
            log.error("同步点赞数据到MySQL失败: {}", e.getMessage());
        }
    }
} 