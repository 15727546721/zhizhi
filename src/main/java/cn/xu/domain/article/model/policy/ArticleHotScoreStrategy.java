package cn.xu.domain.article.model.policy;

import java.time.LocalDateTime;

public interface ArticleHotScoreStrategy {

    double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime);
}
