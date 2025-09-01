package cn.xu.domain.article.model.policy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 默认文章热度计算策略：
 * - 点赞 * 3
 * - 评论 * 5
 * - 浏览 * 0.5
 * - 时间衰减：越新越高热度，越久越低
 */
public class DefaultArticleHotScoreStrategy implements ArticleHotScoreStrategy {

    @Override
    public double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        // 计算距离发布时间的小时数
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        // 基础热度值（行为权重）
        double baseScore = likeCount * 3 + commentCount * 5 + viewCount * 0.5;

        // 时间衰减因子，避免除以0
        double decayFactor = Math.pow(hoursSincePublished + 2, 1.5);

        return baseScore / decayFactor;
    }
}
