package cn.xu.domain.post.model.policy;

import cn.xu.domain.post.model.valobj.PostHotScorePolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostHotScoreStrategyFactory {
    
    private static PostHotScorePolicy postHotScorePolicy;
    
    @Autowired(required = false)
    public PostHotScoreStrategyFactory(PostHotScorePolicy postHotScorePolicy) {
        PostHotScoreStrategyFactory.postHotScorePolicy = postHotScorePolicy;
    }
    
    public static PostHotScoreStrategy getStrategy() {
        // 使用可配置的热度计算策略
        // 如果没有配置，则使用默认策略
        if (postHotScorePolicy != null) {
            return (likeCount, commentCount, viewCount, publishTime) -> 
                postHotScorePolicy.calculateInstance(likeCount, commentCount, viewCount, publishTime);
        } else {
            // 使用默认策略
            return (likeCount, commentCount, viewCount, publishTime) -> 
                PostHotScorePolicy.calculate(likeCount, commentCount, viewCount, publishTime);
        }
    }
}