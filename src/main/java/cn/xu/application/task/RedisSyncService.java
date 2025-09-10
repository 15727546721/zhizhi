package cn.xu.application.task;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.repository.IArticleRepository;
import cn.xu.domain.cache.ICacheService;
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

    private final IArticleRepository articleRepository;
    private final IUserRepository userRepository;
    private final ICacheService cacheService;  // 使用领域层接口

    private static final String ARTICLE_VIEW_COUNT_KEY_PREFIX = "article:view:count:";
    private static final String ARTICLE_LIKE_COUNT_KEY_PREFIX = "article:like:count:";
    private static final String ARTICLE_COLLECT_COUNT_KEY_PREFIX = "article:collect:count:";
    private static final String ARTICLE_COMMENT_COUNT_KEY_PREFIX = "article:comment:count:";

    /**
     * 定时任务：定时同步 Redis 数据到 MySQL
     * 每小时执行一次，将Redis中的文章计数更新到数据库
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncArticleCounts() {
        try {
            log.info("开始同步Redis数据到数据库");
            
            List<ArticleEntity> articles = articleRepository.findAll();
            
            int successCount = 0;
            for (ArticleEntity article : articles) {
                if (article.getStatus() == null || article.getStatus() == ArticleStatus.DRAFT) {
                    continue;
                }
                
                try {
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
                    
                    articleRepository.update(article);
                    successCount++;
                } catch (Exception e) {
                    log.error("同步文章计数失败 - articleId: {}", article.getId(), e);
                }
            }
            
            log.info("Redis数据同步完成，成功同步 {} 篇文章", successCount);
        } catch (Exception e) {
            log.error("Redis数据同步失败", e);
        }
    }

    /**
     * 获取 Redis 中的计数值，如果 Redis 中没有，则返回 0
     * 
     * @param keyPrefix 键值前缀
     * @param articleId 文章ID
     * @return 计数值
     */
    private long getRedisCount(String keyPrefix, Long articleId) {
        try {
            String key = keyPrefix + articleId;
            Long count = cacheService.getCount(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("获取Redis计数失败 - key: {}{}", keyPrefix, articleId, e);
            return 0;
        }
    }
}