package cn.xu.service.post;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 帖子热度评分策略配置
 * <p>用于计算帖子热度分数</p>
 
 */
@Data
@Component
@ConfigurationProperties(prefix = "post.hot-score")
public class PostHotScorePolicy {

    /**
     * 点赞权重
     */
    private double likeWeight = 3.0;
    
    /**
     * 评论权重
     */
    private double commentWeight = 5.0;
    
    /**
     * 浏览权重
     */
    private double viewWeight = 0.5;
    
    /**
     * 收藏权重
     */
    private double favoriteWeight = 4.0;
    
    /**
     * 时间衰减指数
     */
    private double timeDecayExponent = 1.2;
    
    /**
     * 时间衰减基础值
     */
    private double timeDecayBase = 2.0;

    /**
     * 计算帖子热度值（实例方法，使用配置的权重）
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param favoriteCount  收藏数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    public double calculateInstance(long likeCount, long commentCount, long viewCount, long favoriteCount, LocalDateTime publishTime) {
        // 时间差（小时）
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        // 基础热度值（权重可配置）
        double baseScore = likeCount * likeWeight + commentCount * commentWeight + 
                          viewCount * viewWeight + favoriteCount * favoriteWeight;

        // 衰减系数（避免除以0，且调整衰减曲线）
        double decayFactor = Math.pow(hoursSincePublished + timeDecayBase, timeDecayExponent);

        // 带时间衰减的热度值
        return baseScore / decayFactor;
    }

    /**
     * 计算帖子热度值（实例方法，不包含收藏数）
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    public double calculateInstance(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        return calculateInstance(likeCount, commentCount, viewCount, 0L, publishTime);
    }

    /**
     * 计算帖子热度值（静态方法，使用默认权重）
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param favoriteCount  收藏数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    public static double calculate(long likeCount, long commentCount, long viewCount, long favoriteCount, LocalDateTime publishTime) {
        // 时间差（小时）
        long hoursSincePublished = Duration.between(publishTime, LocalDateTime.now()).toHours();

        // 基础热度值（使用默认权重）
        double baseScore = likeCount * 3.0 + commentCount * 5.0 + 
                          viewCount * 0.5 + favoriteCount * 4.0;

        // 衰减系数（避免除以0，且调整衰减曲线）
        double decayFactor = Math.pow(hoursSincePublished + 2.0, 1.2);

        // 带时间衰减的热度值
        return baseScore / decayFactor;
    }

    /**
     * 计算帖子热度值（静态方法，不包含收藏数）
     * @param likeCount      点赞数
     * @param commentCount   评论数
     * @param viewCount      浏览数
     * @param publishTime    发布时间
     * @return               热度分数（带时间衰减）
     */
    public static double calculate(long likeCount, long commentCount, long viewCount, LocalDateTime publishTime) {
        return calculate(likeCount, commentCount, viewCount, 0L, publishTime);
    }
}
