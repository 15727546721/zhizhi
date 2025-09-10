package cn.xu.domain.article.model.policy;


import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 线性时间衰减
 * 适合对时间更温和、不过度倾斜新内容的场景。
 */
public class LinearDecayStrategy implements ArticleHotScoreStrategy {

    @Override
    public double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        double baseScore = likeCount * 3 + commentCount * 5 + viewCount * 0.5;

        double decay = Math.max(1.0, hoursSincePublished / 24.0); // 每24小时衰减一倍

        return baseScore / decay;
    }
}

