package cn.xu.infrastructure.repository.impl;

import cn.xu.domain.like.model.Like;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import cn.xu.infrastructure.persistent.repository.LikeRepository;
import cn.xu.infrastructure.repository.redis.IRedisLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组合仓储实现
 * 使用Redis作为缓存，MySQL作为持久化存储
 */
@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class CompositeLikeRepository implements ILikeRepository {

    private final IRedisLikeRepository redisLikeRepository;
    private final LikeRepository mysqlLikeRepository;

    // 批量同步的大小限制
    private static final int BATCH_SIZE = 1000;

    @Override
    public void save(Like like) {
        // 参数校验
        validateLike(like);

        // 检查点赞类型是否可用
        like.getType().checkEnabled();

        // 如果是点赞操作，检查点赞数是否超出限制
        if (like.isLiked()) {
            Long currentCount = getLikeCount(like.getTargetId(), like.getType());
            like.getType().checkLikeCount(currentCount);
        }

        try {
            // 先写入MySQL
            mysqlLikeRepository.save(like);

            // 再更新Redis
            if (like.isLiked()) {
                redisLikeRepository.saveLike(like.getUserId(), like.getTargetId(), like.getType());
            } else {
                redisLikeRepository.removeLike(like.getUserId(), like.getTargetId(), like.getType());
            }

            log.info("保存点赞记录成功: {}", like);
        } catch (Exception e) {
            log.error("保存点赞记录失败: {}, error: {}", like, e.getMessage());
            throw new RuntimeException("保存点赞记录失败", e);
        }
    }

    @Override
    public Long getLikeCount(Long targetId, LikeType type) {
        // 参数校验
        validateTargetId(targetId);
        validateLikeType(type);

        try {
            // 优先从Redis获取
            Long count = redisLikeRepository.getLikeCount(targetId, type);
            if (count == null || count == 0) {
                // Redis中没有数据，从MySQL获取
                count = mysqlLikeRepository.getLikeCount(targetId, type);
                // 如果MySQL中有数据，则同步到Redis
                if (count > 0) {
                    syncMySQLDataToRedis(targetId, type);
                }
            }
            return count;
        } catch (Exception e) {
            log.error("获取点赞数失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, LikeType type) {
        // 参数校验
        validateUserId(userId);
        validateTargetId(targetId);
        validateLikeType(type);

        try {
            // 优先从Redis查询
            boolean liked = redisLikeRepository.hasLiked(userId, targetId, type);
            if (!liked) {
                // Redis中没有，查询MySQL
                liked = mysqlLikeRepository.isLiked(userId, targetId, type);
                // 如果MySQL中是点赞状态，则同步到Redis
                if (liked) {
                    redisLikeRepository.saveLike(userId, targetId, type);
                }
            }
            return liked;
        } catch (Exception e) {
            log.error("查询点赞状态失败: userId={}, targetId={}, type={}, error={}",
                    userId, targetId, type, e.getMessage());
            return false;
        }
    }

    @Override
    public void delete(Long userId, Long targetId, LikeType type) {
        // 参数校验
        validateUserId(userId);
        validateTargetId(targetId);
        validateLikeType(type);

        try {
            // 先更新MySQL
            mysqlLikeRepository.delete(userId, targetId, type);
            // 再删除Redis中的数据
            redisLikeRepository.removeLike(userId, targetId, type);

            log.info("删除点赞记录成功: userId={}, targetId={}, type={}",
                    userId, targetId, type);
        } catch (Exception e) {
            log.error("删除点赞记录失败: userId={}, targetId={}, type={}, error={}",
                    userId, targetId, type, e.getMessage());
            throw new RuntimeException("删除点赞记录失败", e);
        }
    }

    @Override
    public Set<Long> getLikedUserIds(Long targetId, LikeType type) {
        try {
            // 优先从Redis获取
            Set<Long> userIds = redisLikeRepository.getLikedUserIds(targetId, type);
            if (userIds == null || userIds.isEmpty()) {
                // Redis中没有数据，从MySQL获取
                userIds = mysqlLikeRepository.getLikedUserIds(targetId, type);
                // 如果MySQL中有数据，则同步到Redis
                if (userIds != null && !userIds.isEmpty()) {
                    syncMySQLDataToRedis(targetId, type);
                }
            }
            return userIds;
        } catch (Exception e) {
            log.error("获取点赞用户列表失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
            return null;
        }
    }

    @Override
    public Set<Like> getPageByType(LikeType type, Integer offset, Integer limit) {
        try {
            return mysqlLikeRepository.getPageByType(type, offset, limit);
        } catch (Exception e) {
            log.error("分页获取点赞记录失败: type={}, offset={}, limit={}, error={}",
                    type, offset, limit, e.getMessage());
            return null;
        }
    }

    @Override
    public Long countByType(LikeType type) {
        try {
            return mysqlLikeRepository.countByType(type);
        } catch (Exception e) {
            log.error("获取点赞记录总数失败: type={}, error={}", type, e.getMessage());
            return 0L;
        }
    }

    @Override
    public void syncToCache(Long targetId, LikeType type) {
        try {
            syncMySQLDataToRedis(targetId, type);
        } catch (Exception e) {
            log.error("同步点赞数据到缓存失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
        }
    }

    @Override
    public void cleanExpiredCache() {
        // 缓存永久保存，不需要清理过期缓存
        log.debug("缓存永久保存，不需要清理过期缓存");
    }

    /**
     * 定时将MySQL数据同步到Redis
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncMySQLDataToRedis() {
        log.info("开始同步MySQL点赞数据到Redis...");
        int totalCount = 0;

        try {
            for (LikeType type : LikeType.values()) {
                if (!type.isEnabled()) {
                    continue;
                }

                totalCount += syncTypeData(type);
            }

            log.info("同步MySQL点赞数据到Redis完成，共同步{}条记录", totalCount);
        } catch (Exception e) {
            log.error("同步MySQL点赞数据到Redis失败: {}", e.getMessage());
        }
    }

    /**
     * 同步单个目标的MySQL数据到Redis
     */
    private void syncMySQLDataToRedis(Long targetId, LikeType type) {
        try {
            // 获取MySQL中的点赞用户列表
            Set<Long> likedUserIds = mysqlLikeRepository.getLikedUserIds(targetId, type);
            if (likedUserIds == null || likedUserIds.isEmpty()) {
                return;
            }

            // 清理旧的缓存数据
            redisLikeRepository.cleanCache(targetId, type);

            // 批量添加到Redis
            for (Long userId : likedUserIds) {
                redisLikeRepository.saveLike(userId, targetId, type);
            }

            log.info("同步MySQL数据到Redis成功: targetId={}, type={}, count={}",
                    targetId, type, likedUserIds.size());
        } catch (Exception e) {
            log.error("同步MySQL数据到Redis失败: targetId={}, type={}, error={}",
                    targetId, type, e.getMessage());
        }
    }

    /**
     * 同步指定类型的数据
     */
    private int syncTypeData(LikeType type) {
        int totalCount = 0;
        int offset = 0;

        try {
            // 获取该类型的总记录数
            Long total = mysqlLikeRepository.countByType(type);
            if (total == null || total == 0) {
                return 0;
            }

            // 分批同步数据
            while (offset < total) {
                Set<Like> records = mysqlLikeRepository.getPageByType(type, offset, BATCH_SIZE);
                if (records == null || records.isEmpty()) {
                    break;
                }

                // 按目标ID分组处理
                Map<Long, Set<Long>> targetUserMap = records.stream()
                        .collect(Collectors.groupingBy(
                                Like::getTargetId,
                                Collectors.mapping(Like::getUserId, Collectors.toSet())
                        ));

                // 批量同步到Redis
                for (Map.Entry<Long, Set<Long>> entry : targetUserMap.entrySet()) {
                    Long targetId = entry.getKey();
                    Set<Long> userIds = entry.getValue();

                    // 清理旧的缓存数据
                    redisLikeRepository.cleanCache(targetId, type);

                    // 批量添加到Redis
                    for (Long userId : userIds) {
                        redisLikeRepository.saveLike(userId, targetId, type);
                    }

                    totalCount += userIds.size();
                }

                offset += records.size();
                log.info("同步{}类型数据进度: {}/{}", type.getDescription(), offset, total);
            }

            log.info("同步{}类型数据完成，共同步{}条记录", type.getDescription(), totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("同步{}类型数据失败: {}", type.getDescription(), e.getMessage());
            return totalCount;
        }
    }

    private void validateLike(Like like) {
        if (like == null) {
            throw new IllegalArgumentException("点赞记录不能为空");
        }
        validateUserId(like.getUserId());
        validateTargetId(like.getTargetId());
        validateLikeType(like.getType());
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不合法");
        }
    }

    private void validateTargetId(Long targetId) {
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("目标ID不合法");
        }
    }

    private void validateLikeType(LikeType type) {
        if (type == null) {
            throw new IllegalArgumentException("点赞类型不能为空");
        }
    }
} 