package cn.xu.domain.comment.model.policy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 新鲜内容短期爆发加成
 */
public class TrendingCommentStrategy implements HotScoreStrategy {

    @Override
    public double calculate(long likeCount, long replyCount, LocalDateTime createTime) {
        long hoursSincePublish = Duration.between(createTime, LocalDateTime.now()).toHours();
        double boost;

        if (hoursSincePublish < 3) {
            boost = 1.5;
        } else if (hoursSincePublish < 6) {
            boost = 1.2;
        } else {
            boost = 1.0;
        }

        return (likeCount + replyCount * 2) * boost;
    }
}
