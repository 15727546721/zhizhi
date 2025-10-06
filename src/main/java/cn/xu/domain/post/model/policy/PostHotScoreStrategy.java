package cn.xu.domain.post.model.policy;

import java.time.LocalDateTime;

public interface PostHotScoreStrategy {
    /**
     * 计算帖子热度值
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime);
}