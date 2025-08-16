package cn.xu.application.task;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.infrastructure.persistent.repository.ArticleRepository;
import cn.xu.infrastructure.persistent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisSyncService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_VIEW_COUNT_KEY_PREFIX = "article:view:count:";
    private static final String ARTICLE_LIKE_COUNT_KEY_PREFIX = "article:like:count:";
    private static final String ARTICLE_COLLECT_COUNT_KEY_PREFIX = "article:collect:count:";
    private static final String ARTICLE_COMMENT_COUNT_KEY_PREFIX = "article:comment:count:";

    // 定时任务，定时同步 Redis 数据到 MySQL
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void syncArticleCounts() {
        List<ArticleEntity> articles = articleRepository.findAll();

        for (ArticleEntity article : articles) {
            if (article.getStatus() == null || article.getStatus() == ArticleStatus.DRAFT) {
                continue;
            }

            Long articleId = article.getId();
            long viewCount = getRedisCount(ARTICLE_VIEW_COUNT_KEY_PREFIX, articleId);
            long likeCount = getRedisCount(ARTICLE_LIKE_COUNT_KEY_PREFIX, articleId);
            long collectCount = getRedisCount(ARTICLE_COLLECT_COUNT_KEY_PREFIX, articleId);
            long commentCount = getRedisCount(ARTICLE_COMMENT_COUNT_KEY_PREFIX, articleId);

            // 更新数据库中的文章计数
            article.setViewCount(viewCount);
            article.setLikeCount(likeCount);
            article.setCollectCount(collectCount);
            article.setCommentCount(commentCount);

            articleRepository.save(article);
        }
    }

    // 获取 Redis 中的计数值，如果 Redis 中没有，则返回 0
    private long getRedisCount(String keyPrefix, Long articleId) {
        String key = keyPrefix + articleId;
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        return count != null ? count : 0;
    }
}

