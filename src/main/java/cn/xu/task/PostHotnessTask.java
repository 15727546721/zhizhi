package cn.xu.task;

import cn.xu.cache.RedisKeyManager;
import cn.xu.model.entity.Post;
import cn.xu.service.post.PostHotScorePolicy;
import cn.xu.service.post.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class PostHotnessTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // RedisTemplate，用于操作 Redis 数据库

    @Autowired
    private PostService postService; // 帖子服务，用于从数据库获取帖子信息

    /**
     * 定时任务：每小时更新一次帖子的热度分数。
     * 使用 cron 表达式 "0 0 * * * ?" 每小时整点执行。
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时整点执行任务
    public void updatePostHotScores() {
        // 获取 Redis 中所有帖子的热度数据
        Set<ZSetOperations.TypedTuple<Object>> allPosts
                = redisTemplate.opsForZSet()
                .rangeWithScores(RedisKeyManager.postHotRankKey(), 0, -1);

        // 如果 Redis 中存在帖子数据，则继续处理
        if (allPosts != null) {

            // 遍历所有 Redis 中的帖子数据，更新每篇帖子的热度分数
            for (ZSetOperations.TypedTuple<Object> tuple : allPosts) {
                // 获取当前帖子的 ID，并转为 Long 类型
                Long postId = Long.parseLong((String) Objects.requireNonNull(tuple.getValue()));

                // 从数据库中获取帖子实体
                Optional<Post> postOpt = postService.getPostById(postId);
                Post postEntity = postOpt.orElse(null);

                // 如果帖子存在，重新计算热度分数
                if (postEntity != null) {
                    // 计算帖子的热度分数，带时间衰减
                    double hotScore = PostHotScorePolicy.calculate(
                            postEntity.getLikeCount() != null ? postEntity.getLikeCount() : 0L,     // 点赞数
                            postEntity.getCommentCount() != null ? postEntity.getCommentCount() : 0L,  // 评论数
                            postEntity.getFavoriteCount() != null ? postEntity.getFavoriteCount() : 0L,  // 收藏数
                            postEntity.getCreateTime()     // 创建时间
                    );

                    // 更新 Redis 中该帖子的热度分数
                    redisTemplate.opsForZSet().add(RedisKeyManager.postHotRankKey(), postId.toString(), hotScore);
                }
            }

            // 保证 Redis 中只保存前 1000 名帖子的热度数据
            redisTemplate.opsForZSet().removeRange(RedisKeyManager.postHotRankKey(), 1000, -1);

            // 输出日志，表示任务执行成功
            log.info("帖子热度更新成功，保持前1000篇帖子热度。");
        }
    }
}