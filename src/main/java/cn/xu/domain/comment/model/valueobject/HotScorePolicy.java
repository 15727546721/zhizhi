package cn.xu.domain.comment.model.valueobject;


import java.time.Duration;
import java.time.LocalDateTime;

public class HotScorePolicy {
    // 热度 = 点赞数 + 回复数 * 2 + 时间衰减因子 + 权重
    public static double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        // 计算距今的小时数
        long hoursSincePublish = Duration.between(createTime, LocalDateTime.now()).toHours();

        // 时间衰减因子，72小时内的内容衰减较少，72小时后的衰减逐渐增大
        double timeDecay = Math.max(0, 1 - hoursSincePublish / 72.0);

        // 时间衰减权重（防止时间过长的帖子热度过低）
        double timeWeight = timeDecay * 5;

        // 提高回复数的影响，假设回复数对热度影响加倍
        long replyWeight = replyCount * 2;

        // 最终热度公式
        return likeCount + replyWeight + timeWeight;
    }
}
