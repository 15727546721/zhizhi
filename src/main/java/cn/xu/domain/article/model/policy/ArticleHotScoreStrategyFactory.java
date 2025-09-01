package cn.xu.domain.article.model.policy;


/**
 * 热度策略工厂
 */
public class ArticleHotScoreStrategyFactory {

    public enum StrategyType {
        DEFAULT,
        REDDIT,
        TRENDING,
        LINEAR
    }

    // 可换成从配置文件读取
    private static final StrategyType CURRENT_STRATEGY = StrategyType.DEFAULT;

    public static ArticleHotScoreStrategy getStrategy() {
        switch (CURRENT_STRATEGY) {
            case REDDIT:
                return new RedditLikeArticleHotScoreStrategy();
            case TRENDING:
                return new TrendingBoostStrategy();
            case LINEAR:
                return new LinearDecayStrategy();
            case DEFAULT:
            default:
                return new DefaultArticleHotScoreStrategy();
        }
    }
}

