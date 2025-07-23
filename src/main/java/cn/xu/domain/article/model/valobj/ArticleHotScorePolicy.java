package cn.xu.domain.article.model.valobj;

import java.time.LocalDateTime;

public class ArticleHotScorePolicy {

    /**
     * 热度计算示例公式
     * @param likeCount 点赞数
     * @param collectCount 收藏数
     * @param commentCount 评论数
     * @param publishTime 发布时间
     * @return 热度得分
     */
    public static double calculate(long likeCount, long collectCount, long commentCount, LocalDateTime publishTime) {
        // 基础分：点赞 * 2 + 收藏 * 3 + 评论 * 5
        double baseScore = likeCount * 2 + collectCount * 3 + commentCount * 5;

        // 时间衰减，当前时间 - 发布时间，单位小时
        long hoursSincePublish = java.time.Duration.between(publishTime, LocalDateTime.now()).toHours();

        // 衰减函数：比如每小时衰减0.01（指数衰减）
        double decayFactor = Math.exp(-0.01 * hoursSincePublish);

        return baseScore * decayFactor;
    }
}

