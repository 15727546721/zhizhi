package cn.xu.domain.comment.model.valueobject;


import java.time.Duration;
import java.time.LocalDateTime;

public class HotScorePolicy {

    // 热度 = 点赞数 + 回复数 * 0.7 + 时间衰减因子
    public static double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        long hoursSincePublish = Duration.between(createTime, LocalDateTime.now()).toHours();
        double timeDecay = Math.max(0, 1 - hoursSincePublish / 72.0); // 72小时衰减
        return likeCount + replyCount * 0.7 + timeDecay * 5;
    }
}
