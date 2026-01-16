package cn.xu.service.like;

import cn.xu.model.entity.Like;
import cn.xu.repository.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 点赞统计服务
 * <p>提供点赞数统计、用户点赞统计功能</p>
 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeStatisticsService {
    
    private final LikeService likeDomainService;
    private final LikeMapper likeMapper;
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, Like.LikeType type) {
        // 从LikeService获取点赞数
        return likeDomainService.getLikeCount(targetId, type.getCode());
    }
    
    /**
     * 获取用户的点赞统计
     * 
     * @param userId 用户ID
     * @return 用户点赞统计
     */
    public UserLikeStatistics getUserLikeStatistics(Long userId) {
        if (userId == null) {
            log.warn("[点赞统计] 用户ID为空，返回空统计");
            return UserLikeStatistics.builder()
                    .userId(null)
                    .postLikeCount(0L)
                    .commentLikeCount(0L)
                    .essayLikeCount(0L)
                    .totalLikeCount(0L)
                    .build();
        }
        
        try {
            // 使用 COUNT 查询统计各类型的点赞数（优化：避免加载全部数据再计算 size）
            long postCount = likeMapper.countByUserIdAndType(userId, Like.LikeType.POST.getCode());
            long essayCount = likeMapper.countByUserIdAndType(userId, Like.LikeType.ESSAY.getCode());
            long commentCount = likeMapper.countByUserIdAndType(userId, Like.LikeType.COMMENT.getCode());
            
            long totalCount = postCount + essayCount + commentCount;
            
            log.info("[点赞统计] 用户统计完成 - userId: {}, 帖子: {}, 随笔: {}, 评论: {}, 总计: {}", 
                    userId, postCount, essayCount, commentCount, totalCount);
            
            return UserLikeStatistics.builder()
                    .userId(userId)
                    .postLikeCount(postCount)
                    .commentLikeCount(commentCount)
                    .essayLikeCount(essayCount)
                    .totalLikeCount(totalCount)
                    .build();
        } catch (Exception e) {
            log.error("[点赞统计] 获取用户点赞统计失败 - userId: {}", userId, e);
            // 异常时返回0统计
            return UserLikeStatistics.builder()
                    .userId(userId)
                    .postLikeCount(0L)
                    .commentLikeCount(0L)
                    .essayLikeCount(0L)
                    .totalLikeCount(0L)
                    .build();
        }
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
