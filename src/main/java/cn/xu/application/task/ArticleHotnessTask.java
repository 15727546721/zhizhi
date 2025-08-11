package cn.xu.application.task;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleHotScorePolicy;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class ArticleHotnessTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // RedisTemplate，用于操作 Redis 数据库

    @Autowired
    private IArticleRepository articleRepository; // 文章仓储，用于从数据库获取文章信息

    /**
     * 定时任务：每小时更新一次文章的热度分数。
     * 使用 cron 表达式 "0 0 * * * ?" 每小时整点执行。
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时整点执行任务
    public void updateArticleHotScores() {
        // 获取 Redis 中所有文章的热度数据
        Set<ZSetOperations.TypedTuple<Object>> allArticles
                = redisTemplate.opsForZSet()
                .rangeWithScores(RedisKeyManager.articleHotRankKey(), 0, -1);

        // 如果 Redis 中存在文章数据，则继续处理
        if (allArticles != null) {

            // 遍历所有 Redis 中的文章数据，更新每篇文章的热度分数
            for (ZSetOperations.TypedTuple<Object> article : allArticles) {
                // 获取当前文章的 ID，并转为 Long 类型
                Long articleId = Long.parseLong((String) Objects.requireNonNull(article.getValue()));

                // 从数据库中获取文章实体
                ArticleEntity articleEntity = articleRepository.findById(articleId);

                // 如果文章存在，重新计算热度分数
                if (articleEntity != null) {
                    // 计算文章的热度分数，带时间衰减
                    double hotScore = ArticleHotScorePolicy.calculate(articleEntity.getLikeCount(),     // 点赞数
                            articleEntity.getCommentCount(),  // 评论数
                            articleEntity.getViewCount(),     // 浏览数
                            articleEntity.getCreateTime()     // 创建时间
                    );

                    // 更新 Redis 中该文章的热度分数
                    redisTemplate.opsForZSet().add(RedisKeyManager.articleHotRankKey(), articleId.toString(), hotScore);
                }
            }

            // 保证 Redis 中只保存前 1000 名文章的热度数据
            redisTemplate.opsForZSet().removeRange(RedisKeyManager.articleHotRankKey(), 1000, -1);

            // 输出日志，表示任务执行成功
            log.info("文章热度更新成功，保持前1000篇文章热度。");
        }
    }
}
