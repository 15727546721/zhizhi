package cn.xu.domain.comment.model.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HotScoreStrategyFactory {

    public enum StrategyType {
        SIMPLE,
        REDDIT,
        TRENDING,
        LINEAR
    }

    // 通过配置文件注入，方便动态切换
    @Value("${comment.hot.score.strategy:SIMPLE}")
    private StrategyType currentStrategy;

    public HotScoreStrategy getStrategy() {
        switch (currentStrategy) {
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

    public double calculateInitialScore(HotScoreStrategy strategy, LocalDateTime createTime) {
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
    
    // Getter and Setter for testing and dynamic configuration
    public StrategyType getCurrentStrategy() {
        return currentStrategy;
    }
    
    public void setCurrentStrategy(StrategyType currentStrategy) {
        this.currentStrategy = currentStrategy;
    }
}