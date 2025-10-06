package cn.xu.domain.like.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
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
    
    private final ILikeAggregateRepository likeAggregateRepository;
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        // 从聚合根仓储获取点赞数
        return likeAggregateRepository.countByTarget(targetId, type);
    }
    
    /**
     * 获取用户点赞数统计
     * 
     * @param userId 用户ID
     * @return 用户点赞数统计
     */
    public UserLikeStatistics getUserLikeStatistics(Long userId) {
        // 获取用户的点赞统计信息
        // 这里需要实现具体的统计逻辑，暂时返回默认值
        return UserLikeStatistics.builder()
                .userId(userId)
                .postLikeCount(0L)
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
        private Long postLikeCount;
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
        
        public Long getPostLikeCount() {
            return postLikeCount;
        }
        
        public void setPostLikeCount(Long postLikeCount) {
            this.postLikeCount = postLikeCount;
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
            private Long postLikeCount;
            private Long commentLikeCount;
            private Long essayLikeCount;
            private Long totalLikeCount;
            
            public UserLikeStatisticsBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }
            
            public UserLikeStatisticsBuilder postLikeCount(Long postLikeCount) {
                this.postLikeCount = postLikeCount;
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
                statistics.setPostLikeCount(this.postLikeCount);
                statistics.setCommentLikeCount(this.commentLikeCount);
                statistics.setEssayLikeCount(this.essayLikeCount);
                statistics.setTotalLikeCount(this.totalLikeCount);
                return statistics;
            }
        }
    }
}