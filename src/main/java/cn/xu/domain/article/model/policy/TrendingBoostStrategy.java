package cn.xu.domain.article.model.policy;


import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 短时间爆发型热度
 * 适合做“近24小时热门文章”，给短时间点赞/评论爆发内容加权。
 */
public class TrendingBoostStrategy implements ArticleHotScoreStrategy {

    @Override
    public double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        double baseScore = likeCount * 3 + commentCount * 5 + viewCount * 0.5;

        double boost = 1.0;
        if (hoursSincePublished < 3) {
            boost = 1.5;  // 刚发布3小时内内容，热度乘以1.5
        } else if (hoursSincePublished < 6) {
            boost = 1.2;
        }

        double decay = Math.pow(hoursSincePublished + 2, 1.5);

        return (baseScore * boost) / decay;
    }
}

