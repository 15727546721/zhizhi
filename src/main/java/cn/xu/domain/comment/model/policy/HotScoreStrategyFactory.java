package cn.xu.domain.comment.model.policy;

import java.time.LocalDateTime;

public class HotScoreStrategyFactory {

    public enum StrategyType {
        SIMPLE,
        REDDIT,
        TRENDING,
        LINEAR
    }

    // 这里可以改成配置注入，方便动态切换
    private static final StrategyType CURRENT_STRATEGY = StrategyType.SIMPLE;

    public static HotScoreStrategy getStrategy() {
        switch (CURRENT_STRATEGY) {
            case REDDIT:
                return new RedditLikeHotScoreStrategy();
            case TRENDING:
                return new TrendingCommentStrategy();
            case LINEAR:
                return new LinearTimeDecayStrategy();
            case SIMPLE:
            default:
                return new SimpleHotScoreStrategy();
        }
    }

    public static double calculateInitialScore(HotScoreStrategy strategy, LocalDateTime createTime) {
        double rawScore = strategy.calculate(0, 0, createTime);

        if (strategy instanceof SimpleHotScoreStrategy) {
            return rawScore;
        } else if (strategy instanceof LinearTimeDecayStrategy) {
            return Math.max(1.0, rawScore);
        } else if (strategy instanceof TrendingCommentStrategy) {
            return Math.max(1.0, rawScore);
        } else if (strategy instanceof RedditLikeHotScoreStrategy) {
            return Math.max(0.1, rawScore);
        }

        return Math.max(1.0, rawScore);
    }
}
