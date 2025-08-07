package cn.xu.domain.article.model.valobj;

import java.time.LocalDateTime;
import java.time.Duration;

public class ArticleHotScorePolicy {

    /**
     * 计算文章热度值
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    public static double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
//        hot_score = (点赞数 * 3 + 评论 * 5 + 浏览 * 0.5) / ((发布时间距今小时数 + 2)^1.5)

        // 时间差（小时）
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        // 基础热度值（可调权重）
        double baseScore = likeCount * 3 + commentCount * 5 + viewCount * 0.5;

        // 衰减系数，避免除以 0
        double decayFactor = Math.pow(hoursSincePublished + 2, 1.5);

        // 带时间衰减的热度值
        return baseScore / decayFactor;
    }
}
