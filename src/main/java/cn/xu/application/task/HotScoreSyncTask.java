package cn.xu.application.task;

import cn.xu.infrastructure.common.utils.ArticleHotScoreCacheHelper;
import cn.xu.infrastructure.persistent.po.Article;
import cn.xu.infrastructure.persistent.read.elastic.service.ArticleElasticService;
import cn.xu.infrastructure.persistent.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 定时任务同步热度到 Elasticsearch
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotScoreSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleRepository articleRepository;
    private final ArticleElasticService articleElasticService;
    private final ArticleHotScoreCacheHelper hotScoreHelper;

    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void syncToElastic() {
        Set<String> keys = redisTemplate.keys("article:hot:*");
        if (keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 解析文章ID，避免数组越界异常
                String[] parts = key.split(":");
                if (parts.length < 3) {
                    log.warn("Redis key格式异常: {}", key);
                    continue;
                }
                Long articleId = Long.parseLong(parts[2]);

                Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                if (map.isEmpty()) {
                    log.warn("热度缓存为空，跳过同步，key={}", key);
                    continue;
                }

                int like = parseIntSafe(map.get("like"));
                int collect = parseIntSafe(map.get("collect"));
                int comment = parseIntSafe(map.get("comment"));

                // 查询数据库文章实体
                Article article = articleRepository.findPoById(articleId);
                if (article == null) {
                    log.warn("文章不存在，跳过同步，articleId={}", articleId);
                    continue;
                }

                // 累加最新热度数值
                article.setLikeCount(article.getLikeCount() + like);
                article.setCollectCount(article.getCollectCount() + collect);
                article.setCommentCount(article.getCommentCount() + comment);

                // 同步到ES
                articleElasticService.indexArticle(article);

                // 清理缓存，避免重复同步
                hotScoreHelper.clearHotData(articleId);

                log.info("文章[{}]热度已同步至Elasticsearch", articleId);

            } catch (Exception e) {
                log.error("同步文章热度失败，key={}", key, e);
            }
        }
    }

    /**
     * 衰减热度 & 清理冷评论
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void decayHotComments() {
        Set<String> keys = redisTemplate.keys("comment:hot:zset:*");
        for (String zsetKey : keys) {
            Set<ZSetOperations.TypedTuple<Object>> hotComments = redisTemplate.opsForZSet()
                    .rangeWithScores(zsetKey, 0, -1);

            if (hotComments != null) {
                for (ZSetOperations.TypedTuple<Object> tuple : hotComments) {
                    double oldScore = tuple.getScore();
                    double newScore = oldScore * 0.98; // 衰减 2%
                    redisTemplate.opsForZSet().add(zsetKey, Objects.requireNonNull(tuple.getValue()), newScore);

                    // 清理过低的
                    if (newScore < 5) {
                        redisTemplate.opsForZSet().remove(zsetKey, tuple.getValue());
                    }
                }
            }
        }
    }

    /**
     * 安全转换整数，防止空指针和格式异常
     */
    private int parseIntSafe(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            log.warn("热度字段转换失败，值={}，默认返回0", obj);
            return 0;
        }
    }
}
