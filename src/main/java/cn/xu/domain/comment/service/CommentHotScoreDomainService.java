package cn.xu.domain.comment.service;

import cn.xu.domain.comment.model.entity.CommentEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 评论热度分数领域服务
 * 负责评论热度分数的计算和更新，遵循DDD原则
 */
@Service
public class CommentHotScoreDomainService {

    /**
     * 更新评论热度分数
     * @param comment 评论实体
     */
    public void updateHotScore(CommentEntity comment) {
        if (comment == null) {
            return;
        }
        
        long likes = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        long replies = comment.getReplyCount() != null ? comment.getReplyCount() : 0;
        
        // 时间衰减因子
        double timeDecay = calculateTimeDecay(comment.getCreateTime());
        
        // 热度计算公式：点赞数*2 + 回复数*3，乘以时间衰减因子
        double hotScore = (likes * 2 + replies * 3) * timeDecay;
        comment.setHotScore(hotScore);
        comment.setHot(hotScore >= 10); // 热度分数超过10认为是热门评论
    }

    /**
     * 计算时间衰减因子
     * @param createTime 创建时间
     * @return 时间衰减因子
     */
    private double calculateTimeDecay(LocalDateTime createTime) {
        if (createTime == null) return 1.0;
        
        long hoursSinceCreation = java.time.Duration.between(createTime, LocalDateTime.now()).toHours();
        // 每12小时热度衰减20%
        return Math.pow(0.8, hoursSinceCreation / 12.0);
    }
}