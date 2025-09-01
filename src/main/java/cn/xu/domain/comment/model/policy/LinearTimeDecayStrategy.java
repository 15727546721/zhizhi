package cn.xu.domain.comment.model.policy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 线性时间衰减
 */
public class LinearTimeDecayStrategy implements HotScoreStrategy {

    @Override
    public double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        long hoursSincePublish = Duration.between(createTime, LocalDateTime.now()).toHours();
        double baseScore = likeCount + replyCount * 2;
        double decay = Math.max(1.0, hoursSincePublish / 24.0); // 每24小时衰减1倍

        return baseScore / decay;
    }
}
