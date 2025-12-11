package cn.xu.task;

import cn.xu.cache.RedisKeyManager;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.post.PostHotScorePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子热度定时更新任务
 * 
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostHotnessTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;

    /**
     * 定时任务：每小时更新一次帖子的热度分数
     * 使用批量查询优化性能，避免 N+1 查询问题
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updatePostHotScores() {
        try {
            // 获取 Redis 中所有帖子的热度数据
            Set<ZSetOperations.TypedTuple<Object>> allPosts = redisTemplate.opsForZSet()
                    .rangeWithScores(RedisKeyManager.postHotRankKey(), 0, -1);

            if (allPosts == null || allPosts.isEmpty()) {
                log.debug("Redis 中没有帖子热度数据，跳过更新");
                return;
            }

            // 1. 收集所有帖子ID
            List<Long> postIds = allPosts.stream()
                    .map(tuple -> {
                        try {
                            return Long.parseLong((String) Objects.requireNonNull(tuple.getValue()));
                        } catch (Exception e) {
                            log.warn("解析帖子ID失败: {}", tuple.getValue());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (postIds.isEmpty()) {
                log.warn("没有有效的帖子ID");
                return;
            }

            // 2. 批量查询帖子（解决 N+1 问题）
            List<Post> posts = postMapper.findByIds(postIds);
            if (posts == null || posts.isEmpty()) {
                log.warn("批量查询帖子结果为空");
                return;
            }

            // 3. 构建 ID -> Post 的映射，便于快速查找
            Map<Long, Post> postMap = posts.stream()
                    .collect(Collectors.toMap(Post::getId, post -> post));

            // 4. 批量更新热度分数
            int updateCount = 0;
            for (Long postId : postIds) {
                Post post = postMap.get(postId);
                if (post != null) {
                    // 计算帖子的热度分数，带时间衰减
                    double hotScore = PostHotScorePolicy.calculate(
                            post.getLikeCount() != null ? post.getLikeCount() : 0L,
                            post.getCommentCount() != null ? post.getCommentCount() : 0L,
                            post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L,
                            post.getCreateTime()
                    );

                    // 更新 Redis 中该帖子的热度分数
                    redisTemplate.opsForZSet().add(RedisKeyManager.postHotRankKey(), postId.toString(), hotScore);
                    updateCount++;
                }
            }

            // 5. 保证 Redis 中只保存前1000名帖子的热度数据
            redisTemplate.opsForZSet().removeRange(RedisKeyManager.postHotRankKey(), 1000, -1);

            log.info("帖子热度更新成功，更新数量 {}/{}，保留前1000篇帖子热度", updateCount, postIds.size());
        } catch (Exception e) {
            log.error("帖子热度更新失败", e);
        }
    }
}