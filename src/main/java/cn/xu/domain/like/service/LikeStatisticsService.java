package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 点赞统计服务
 * 提供点赞相关的统计功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeStatisticsService {
    
    private final ILikeRepository likeRepository;
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        // 这里应该从缓存或数据库获取点赞数
        // 暂时返回默认值，实际实现需要结合缓存服务
        return 0L;
    }
    
    /**
     * 获取用户点赞数统计
     * 
     * @param userId 用户ID
     * @return 用户点赞数统计
     */
    public UserLikeStatistics getUserLikeStatistics(Long userId) {
        // 获取用户的点赞统计信息
        return UserLikeStatistics.builder()
                .userId(userId)
                .articleLikeCount(0L)
                .commentLikeCount(0L)
                .essayLikeCount(0L)
                .totalLikeCount(0L)
                .build();
    }
    
    /**
     * 用户点赞统计信息
     */
    public static class UserLikeStatistics {
        private Long userId;
        private Long articleLikeCount;
        private Long commentLikeCount;
        private Long essayLikeCount;
        private Long totalLikeCount;
        
        // 构造函数
        public UserLikeStatistics() {}
        
        // Builder模式支持
        public static UserLikeStatisticsBuilder builder() {
            return new UserLikeStatisticsBuilder();
        }
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Long getArticleLikeCount() {
            return articleLikeCount;
        }
        
        public void setArticleLikeCount(Long articleLikeCount) {
            this.articleLikeCount = articleLikeCount;
        }
        
        public Long getCommentLikeCount() {
            return commentLikeCount;
        }
        
        public void setCommentLikeCount(Long commentLikeCount) {
            this.commentLikeCount = commentLikeCount;
        }
        
        public Long getEssayLikeCount() {
            return essayLikeCount;
        }
        
        public void setEssayLikeCount(Long essayLikeCount) {
            this.essayLikeCount = essayLikeCount;
        }
        
        public Long getTotalLikeCount() {
            return totalLikeCount;
        }
        
        public void setTotalLikeCount(Long totalLikeCount) {
            this.totalLikeCount = totalLikeCount;
        }
        
        /**
         * 用户点赞统计信息构建器
         */
        public static class UserLikeStatisticsBuilder {
            private Long userId;
            private Long articleLikeCount;
            private Long commentLikeCount;
            private Long essayLikeCount;
            private Long totalLikeCount;
            
            public UserLikeStatisticsBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }
            
            public UserLikeStatisticsBuilder articleLikeCount(Long articleLikeCount) {
                this.articleLikeCount = articleLikeCount;
                return this;
            }
            
            public UserLikeStatisticsBuilder commentLikeCount(Long commentLikeCount) {
                this.commentLikeCount = commentLikeCount;
                return this;
            }
            
            public UserLikeStatisticsBuilder essayLikeCount(Long essayLikeCount) {
                this.essayLikeCount = essayLikeCount;
                return this;
            }
            
            public UserLikeStatisticsBuilder totalLikeCount(Long totalLikeCount) {
                this.totalLikeCount = totalLikeCount;
                return this;
            }
            
            public UserLikeStatistics build() {
                UserLikeStatistics statistics = new UserLikeStatistics();
                statistics.setUserId(this.userId);
                statistics.setArticleLikeCount(this.articleLikeCount);
                statistics.setCommentLikeCount(this.commentLikeCount);
                statistics.setEssayLikeCount(this.essayLikeCount);
                statistics.setTotalLikeCount(this.totalLikeCount);
                return statistics;
            }
        }
    }
}