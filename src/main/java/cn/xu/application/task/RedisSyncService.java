package cn.xu.application.task;

import cn.xu.domain.cache.ICacheService;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis数据同步服务
 * 负责定时将Redis中的计数数据同步到数据库
 * 
 * @author xu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final ICacheService cacheService;  // 使用领域层接口

    private static final String POST_VIEW_COUNT_KEY_PREFIX = "post:view:count:";
    private static final String POST_LIKE_COUNT_KEY_PREFIX = "post:like:count:";
    private static final String POST_COLLECT_COUNT_KEY_PREFIX = "post:collect:count:";
    private static final String POST_COMMENT_COUNT_KEY_PREFIX = "post:comment:count:";

    /**
     * 定时任务：定时同步 Redis 数据到 MySQL
     * 每小时执行一次，将Redis中的帖子计数更新到数据库
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncPostCounts() {
        try {
            log.info("开始同步Redis数据到数据库");

            List<PostEntity> posts = postRepository.findAll();

            int successCount = 0;
            for (PostEntity post : posts) {
                if (post.getStatus() == null || "DRAFT".equals(post.getStatus())) {
                    continue;
                }

                try {
                    Long postId = post.getId();
                    long viewCount = getRedisCount(POST_VIEW_COUNT_KEY_PREFIX, postId);
                    long likeCount = getRedisCount(POST_LIKE_COUNT_KEY_PREFIX, postId);
                    long collectCount = getRedisCount(POST_COLLECT_COUNT_KEY_PREFIX, postId);
                    long commentCount = getRedisCount(POST_COMMENT_COUNT_KEY_PREFIX, postId);

                    // 更新数据库中的帖子计数
                    post.setViewCount(viewCount);
                    post.setLikeCount(likeCount);
                    post.setFavoriteCount(collectCount);
                    post.setCommentCount(commentCount);

                    // 创建PostAggregate对象用于更新
                    PostAggregate postAggregate = PostAggregate.builder()
                            .id(postId)
                            .postEntity(post)
                            .build();

                    postRepository.update(postAggregate);
                    successCount++;
                } catch (Exception e) {
                    log.error("同步帖子计数失败 - postId: {}", post.getId(), e);
                }
            }

            log.info("Redis数据同步完成，成功同步 {} 篇帖子", successCount);
        } catch (Exception e) {
            log.error("Redis数据同步失败", e);
        }
    }

    /**
     * 获取 Redis 中的计数值，如果 Redis 中没有，则返回 0
     *
     * @param keyPrefix 键值前缀
     * @param postId    帖子ID
     * @return 计数值
     */
    private long getRedisCount(String keyPrefix, Long postId) {
        try {
            String key = keyPrefix + postId;
            Long count = cacheService.getCount(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("获取Redis计数失败 - key: {}{}", keyPrefix, postId, e);
            return 0;
        }
    }
}