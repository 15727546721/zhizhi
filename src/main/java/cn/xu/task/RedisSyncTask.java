package cn.xu.task;

import cn.xu.cache.FavoriteCacheRepository;
import cn.xu.cache.LikeCacheRepository;
import cn.xu.cache.RedisKeyManager;
import cn.xu.cache.core.RedisOperations;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.enums.favorite.TargetType;
import cn.xu.repository.FavoriteRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Redis数据同步任务
 * 负责定时将Redis中的计数数据同步到数据库
 * 
 * 重要：此任务使用增量同步策略
 * - 如果Redis中有数据，使用数据库当前值 + Redis增量值
 * - 如果Redis中没有数据，从数据库恢复真实值到Redis（而不是用0覆盖数据库）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSyncTask {

    private final PostMapper postMapper;
    private final FavoriteRepository favoriteRepository;
    private final RedisOperations redisOperations;
    private final LikeCacheRepository likeCacheRepository;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final LikeService likeDomainService;

    /**
     * 定时任务：定时同步 Redis 数据到 MySQL
     * 每小时执行一次，将Redis中的帖子计数更新到数据库
     * 
     * 同步策略：
     * 1. 对于点赞数和收藏数：从数据库统计真实值，然后同步到Redis（确保数据一致性）
     * 2. 对于浏览数和评论数：从Redis读取增量值，累加到数据库当前值
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncPostCounts() {
        try {
            log.info("开始同步Redis数据到数据库");

            List<Post> posts = postMapper.findAll();

            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;
            
            for (Post post : posts) {
                if (post.getStatus() == null || "DRAFT".equals(post.getStatus())) {
                    skipCount++;
                    continue;
                }

                try {
                    Long postId = post.getId();
                    
                    // 1. 点赞数：从数据库统计真实值（因为数据库是唯一真实数据源）
                    long likeCount = syncLikeCount(postId);
                    
                    // 2. 收藏数：从数据库统计真实值（因为数据库是唯一真实数据源）
                    long favoriteCount = syncFavoriteCount(postId);
                    
                    // 3. 浏览数：从Redis读取增量值，如果Redis中没有，保持数据库当前值
                    long viewCount = syncViewCount(postId, post.getViewCount() != null ? post.getViewCount() : 0L);
                    
                    // 4. 评论数：从Redis读取增量值，如果Redis中没有，保持数据库当前值
                    long commentCount = syncCommentCount(postId, post.getCommentCount() != null ? post.getCommentCount() : 0L);

                    // 只有当值发生变化时才更新数据库
                    boolean needUpdate = false;
                    if (post.getLikeCount() == null || !post.getLikeCount().equals(likeCount)) {
                        post.setLikeCount(likeCount);
                        needUpdate = true;
                    }
                    if (post.getFavoriteCount() == null || !post.getFavoriteCount().equals(favoriteCount)) {
                        post.setFavoriteCount(favoriteCount);
                        needUpdate = true;
                    }
                    if (post.getViewCount() == null || !post.getViewCount().equals(viewCount)) {
                        post.setViewCount(viewCount);
                        needUpdate = true;
                    }
                    if (post.getCommentCount() == null || !post.getCommentCount().equals(commentCount)) {
                        post.setCommentCount(commentCount);
                        needUpdate = true;
                    }
                    
                    if (needUpdate) {
                        postMapper.updateCounts(postId, likeCount, favoriteCount, viewCount, commentCount);
                        log.debug("同步帖子计数成功 - postId: {}, like: {}, favorite: {}, view: {}, comment: {}", 
                                postId, likeCount, favoriteCount, viewCount, commentCount);
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    log.error("同步帖子计数失败 - postId: {}", post.getId(), e);
                    errorCount++;
                }
            }

            log.info("Redis数据同步完成，成功: {}, 跳过: {}, 失败: {}", successCount, skipCount, errorCount);
        } catch (Exception e) {
            log.error("Redis数据同步失败", e);
        }
    }

    /**
     * 手动触发同步（供管理后台调用）
     */
    public void syncAll() {
        syncPostCounts();
    }

    /**
     * 同步点赞数：从数据库统计真实值，然后同步到Redis
     */
    private long syncLikeCount(Long postId) {
        try {
            Long dbLikeCount = likeDomainService.getLikeCount(postId, Like.LikeType.POST.getCode());
            likeCacheRepository.setLikeCount(postId, Like.LikeType.POST, dbLikeCount);
            log.debug("同步点赞数成功 - postId: {}, count: {}", postId, dbLikeCount);
            return dbLikeCount;
        } catch (Exception e) {
            log.error("同步点赞数失败 - postId: {}", postId, e);
            Long cachedCount = likeCacheRepository.getLikeCount(postId, Like.LikeType.POST);
            return cachedCount != null ? cachedCount : 0L;
        }
    }

    /**
     * 同步收藏数：从数据库统计真实值，然后同步到Redis
     */
    private long syncFavoriteCount(Long postId) {
        try {
            int dbFavoriteCount = favoriteRepository.countFavoritedItemsByTarget(postId, TargetType.POST.getDbCode());
            favoriteCacheRepository.setFavoriteCount(postId, TargetType.POST.getDbCode(), (long) dbFavoriteCount);
            log.debug("同步收藏数成功 - postId: {}, count: {}", postId, dbFavoriteCount);
            return dbFavoriteCount;
        } catch (Exception e) {
            log.error("同步收藏数失败 - postId: {}", postId, e);
            try {
                return favoriteRepository.countFavoritedItemsByTarget(postId, TargetType.POST.getDbCode());
            } catch (Exception ex) {
                log.error("从数据库获取收藏数失败 - postId: {}", postId, ex);
                Long cachedCount = favoriteCacheRepository.getFavoriteCount(postId, TargetType.POST.getDbCode());
                return cachedCount != null ? cachedCount : 0L;
            }
        }
    }

    /**
     * 同步浏览数：从Redis读取增量值，如果Redis中没有，保持数据库当前值
     */
    private long syncViewCount(Long postId, long currentDbValue) {
        try {
            String key = RedisKeyManager.postViewCountKey(postId);
            Long redisValue = convertToLong(redisOperations.get(key));
            
            if (redisValue != null && redisValue > 0) {
                long newValue = currentDbValue + redisValue;
                redisOperations.set(key, 0L);
                log.debug("同步浏览数成功 - postId: {}, dbValue: {}, redisIncrement: {}, newValue: {}", 
                        postId, currentDbValue, redisValue, newValue);
                return newValue;
            } else {
                log.debug("Redis中没有浏览数增量，保持数据库值 - postId: {}, value: {}", postId, currentDbValue);
                return currentDbValue;
            }
        } catch (Exception e) {
            log.warn("同步浏览数失败 - postId: {}, 保持数据库当前值: {}", postId, currentDbValue, e);
            return currentDbValue;
        }
    }

    /**
     * 同步评论数：从Redis读取增量值，如果Redis中没有，保持数据库当前值
     */
    private long syncCommentCount(Long postId, long currentDbValue) {
        try {
            String key = RedisKeyManager.postCommentCountKey(postId);
            Long redisValue = convertToLong(redisOperations.get(key));
            
            if (redisValue != null && redisValue > 0) {
                long newValue = currentDbValue + redisValue;
                redisOperations.set(key, 0L);
                log.debug("同步评论数成功 - postId: {}, dbValue: {}, redisIncrement: {}, newValue: {}", 
                        postId, currentDbValue, redisValue, newValue);
                return newValue;
            } else {
                log.debug("Redis中没有评论数增量，保持数据库值 - postId: {}, value: {}", postId, currentDbValue);
                return currentDbValue;
            }
        } catch (Exception e) {
            log.warn("同步评论数失败 - postId: {}, 保持数据库当前值: {}", postId, currentDbValue, e);
            return currentDbValue;
        }
    }

    /**
     * 将Object转换为Long
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
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
                return null;
            }
        }
        return null;
    }
}
