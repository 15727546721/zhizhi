package cn.xu.domain.comment.model.policy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 默认评论热度策略：
 * - 点赞 + 回复 * 2 + 时间权重（衰减）
 * - 针对评论72小时内的热度计算适配
 */
public class SimpleHotScoreStrategy implements HotScoreStrategy {

    private static final int DECAY_HOURS = 72;

    @Override
    public double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        // 距离现在的小时数
        long hoursSincePublish = Duration.between(createTime, LocalDateTime.now()).toHours();

        // 时间衰减因子（72小时内，值越小越衰减）
        double timeDecay = Math.max(0, 1 - (double) hoursSincePublish / DECAY_HOURS);

        // 时间衰减权重，最大5分
        double timeWeight = timeDecay * 5;

        // 回复权重提升
        long replyWeight = replyCount * 2;

        return likeCount + replyWeight + timeWeight;
    }
}
