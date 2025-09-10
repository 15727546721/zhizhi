package cn.xu.domain.article.model.policy;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Reddit 爆文算法
 * 适合做“热榜”、“首页爆文”场景，优点是时间敏感度强、排序稳定。
 */
public class RedditLikeArticleHotScoreStrategy implements ArticleHotScoreStrategy {

    private static final long REFERENCE_EPOCH = 1134028003L; // Reddit 原始时间戳
    private static final long TIME_DIVISOR = 45000L; // 控制时间权重

    @Override
    public double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        long baseScore = (likeCount * 3 + commentCount * 5 + (long)(viewCount * 0.5));
        if (baseScore <= 0) baseScore = 1; // 避免 log(0)

        long secondsSinceEpoch = publishTime.toEpochSecond(ZoneOffset.UTC);
        long timeDiff = secondsSinceEpoch - REFERENCE_EPOCH;

        return Math.log10(baseScore) + (double) timeDiff / TIME_DIVISOR;
    }
}

