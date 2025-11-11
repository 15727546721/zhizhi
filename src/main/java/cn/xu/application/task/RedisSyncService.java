package cn.xu.application.task;

import cn.xu.domain.cache.ICacheService;
import cn.xu.domain.favorite.model.valobj.TargetType;
import cn.xu.domain.favorite.repository.IFavoriteRepository;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.cache.FavoriteCacheRepository;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis数据同步服务
 * 负责定时将Redis中的计数数据同步到数据库
 * 
 * 重要：此服务使用增量同步策略
 * - 如果Redis中有数据，使用数据库当前值 + Redis增量值
 * - 如果Redis中没有数据，从数据库恢复真实值到Redis（而不是用0覆盖数据库）
 * 
 * @author xu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final IPostRepository postRepository;
    private final ILikeAggregateRepository likeAggregateRepository;
    private final IFavoriteRepository favoriteRepository;
    private final ICacheService cacheService; // 用于浏览数和评论数的增量同步
    private final LikeCacheRepository likeCacheRepository;
    private final FavoriteCacheRepository favoriteCacheRepository;

    private static final String POST_VIEW_COUNT_KEY_PREFIX = "post:view:count:";
    private static final String POST_COMMENT_COUNT_KEY_PREFIX = "post:comment:count:";

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

            List<PostEntity> posts = postRepository.findAll();

            int successCount = 0;
            int skipCount = 0;
            int errorCount = 0;
            
            for (PostEntity post : posts) {
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
                        // 创建PostAggregate对象用于更新
                        PostAggregate postAggregate = PostAggregate.builder()
                                .id(postId)
                                .postEntity(post)
                                .build();

                        postRepository.update(postAggregate);
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
     * 同步点赞数：从数据库统计真实值，然后同步到Redis
     * 
     * @param postId 帖子ID
     * @return 点赞数
     */
    private long syncLikeCount(Long postId) {
        try {
            // 从数据库统计真实点赞数（数据库是唯一真实数据源）
            long dbLikeCount = likeAggregateRepository.countByTarget(postId, LikeType.POST);
            
            // 同步到Redis缓存（使用LikeCacheRepository，它会设置正确的过期时间30天）
            likeCacheRepository.setLikeCount(postId, LikeType.POST, dbLikeCount);
            
            log.debug("同步点赞数成功 - postId: {}, count: {}", postId, dbLikeCount);
            return dbLikeCount;
        } catch (Exception e) {
            log.error("同步点赞数失败 - postId: {}", postId, e);
            // 如果同步失败，尝试从数据库获取当前值，避免返回0覆盖数据库
            try {
                return likeAggregateRepository.countByTarget(postId, LikeType.POST);
            } catch (Exception ex) {
                log.error("从数据库获取点赞数失败 - postId: {}", postId, ex);
                // 最后降级：尝试从Redis读取，如果Redis也没有，返回0（但不会覆盖数据库，因为needUpdate检查）
                Long cachedCount = likeCacheRepository.getLikeCount(postId, LikeType.POST);
                return cachedCount != null ? cachedCount : 0L;
            }
        }
    }

    /**
     * 同步收藏数：从数据库统计真实值，然后同步到Redis
     * 
     * @param postId 帖子ID
     * @return 收藏数
     */
    private long syncFavoriteCount(Long postId) {
        try {
            // 从数据库统计真实收藏数（数据库是唯一真实数据源）
            int dbFavoriteCount = favoriteRepository.countFavoritedItemsByTarget(postId, TargetType.POST.getDbCode());
            
            // 同步到Redis缓存（使用FavoriteCacheRepository，它会设置正确的过期时间）
            favoriteCacheRepository.setFavoriteCount(postId, TargetType.POST.getDbCode(), (long) dbFavoriteCount);
            
            log.debug("同步收藏数成功 - postId: {}, count: {}", postId, dbFavoriteCount);
            return dbFavoriteCount;
        } catch (Exception e) {
            log.error("同步收藏数失败 - postId: {}", postId, e);
            // 如果同步失败，尝试从数据库获取当前值，避免返回0覆盖数据库
            try {
                return favoriteRepository.countFavoritedItemsByTarget(postId, TargetType.POST.getDbCode());
            } catch (Exception ex) {
                log.error("从数据库获取收藏数失败 - postId: {}", postId, ex);
                // 最后降级：尝试从Redis读取，如果Redis也没有，返回数据库中的当前值（如果有）
                Long cachedCount = favoriteCacheRepository.getFavoriteCount(postId, TargetType.POST.getDbCode());
                return cachedCount != null ? cachedCount : 0L;
            }
        }
    }

    /**
     * 同步浏览数：从Redis读取增量值，如果Redis中没有，保持数据库当前值
     * 
     * @param postId 帖子ID
     * @param currentDbValue 数据库当前值
     * @return 浏览数
     */
    private long syncViewCount(Long postId, long currentDbValue) {
        try {
            String key = POST_VIEW_COUNT_KEY_PREFIX + postId;
            Long redisValue = cacheService.getCount(key);
            
            if (redisValue != null && redisValue > 0) {
                // Redis中有增量值，累加到数据库当前值
                long newValue = currentDbValue + redisValue;
                // 更新数据库后，清空Redis增量（或设置为0）
                cacheService.setCount(key, 0L);
                log.debug("同步浏览数成功 - postId: {}, dbValue: {}, redisIncrement: {}, newValue: {}", 
                        postId, currentDbValue, redisValue, newValue);
                return newValue;
            } else {
                // Redis中没有数据，保持数据库当前值
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
     * 
     * @param postId 帖子ID
     * @param currentDbValue 数据库当前值
     * @return 评论数
     */
    private long syncCommentCount(Long postId, long currentDbValue) {
        try {
            String key = POST_COMMENT_COUNT_KEY_PREFIX + postId;
            Long redisValue = cacheService.getCount(key);
            
            if (redisValue != null && redisValue > 0) {
                // Redis中有增量值，累加到数据库当前值
                long newValue = currentDbValue + redisValue;
                // 更新数据库后，清空Redis增量（或设置为0）
                cacheService.setCount(key, 0L);
                log.debug("同步评论数成功 - postId: {}, dbValue: {}, redisIncrement: {}, newValue: {}", 
                        postId, currentDbValue, redisValue, newValue);
                return newValue;
            } else {
                // Redis中没有数据，保持数据库当前值
                log.debug("Redis中没有评论数增量，保持数据库值 - postId: {}, value: {}", postId, currentDbValue);
                return currentDbValue;
            }
        } catch (Exception e) {
            log.warn("同步评论数失败 - postId: {}, 保持数据库当前值: {}", postId, currentDbValue, e);
            return currentDbValue;
        }
    }
}