package cn.xu.domain.comment.model.policy;

import java.time.LocalDateTime;

public interface HotScoreStrategy {
    /**
     * 计算评论热度分数
     * @param likeCount 点赞数
     * @param replyCount 回复数
     * @param createTime 创建时间
     * @return 热度分数
     */
    double calculate(long likeCount, long replyCount, LocalDateTime createTime);
}
