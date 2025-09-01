package cn.xu.domain.comment.model.policy;

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
}
