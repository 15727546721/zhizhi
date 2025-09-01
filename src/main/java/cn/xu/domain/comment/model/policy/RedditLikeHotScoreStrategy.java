package cn.xu.domain.comment.model.policy;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Reddit爆文算法
 */
public class RedditLikeHotScoreStrategy implements HotScoreStrategy {

    private static final long REFERENCE_EPOCH = 1134028003L; // Reddit 时间基准
    private static final long TIME_DIVISOR = 45000L; // 控制时间权重

    @Override
    public double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        long score = likeCount + replyCount;
        if (score <= 0) score = 1;

        long secondsSinceEpoch = createTime.toEpochSecond(ZoneOffset.UTC);
        long timeDiff = secondsSinceEpoch - REFERENCE_EPOCH;

        return Math.log10(score) + (double) timeDiff / TIME_DIVISOR;
    }
}
