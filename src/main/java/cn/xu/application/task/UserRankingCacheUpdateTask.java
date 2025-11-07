package cn.xu.application.task;

import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.cache.UserRankingCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户排行榜缓存更新定时任务
 * 定期从数据库同步用户排行榜数据到Redis缓存
 * 
 * @author zhizhi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRankingCacheUpdateTask {

    private final IUserRepository userRepository;
    private final IPostRepository postRepository;
    private final UserRankingCacheRepository userRankingCacheRepository;

    /**
     * 每5分钟更新一次用户排行榜缓存
     * 保持排行榜数据的新鲜度
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void updateUserRankingCache() {
        log.info("开始执行用户排行榜缓存更新任务");
        
        try {
            // 更新粉丝数排行榜
            updateRankingByFans();
            
            // 更新获赞数排行榜
            updateRankingByLikes();
            
            // 更新帖子数排行榜
            updateRankingByPosts();
            
            // 更新综合排行榜
            updateRankingByComprehensive();
            
            log.info("用户排行榜缓存更新任务执行完成");
        } catch (Exception e) {
            log.error("用户排行榜缓存更新任务执行失败", e);
        }
    }

    /**
     * 更新粉丝数排行榜
     */
    private void updateRankingByFans() {
        try {
            log.debug("开始更新粉丝数排行榜");
            
            // 从数据库查询所有正常状态的用户（限制前1000名，避免数据量过大）
            List<UserEntity> users = userRepository.findUserRanking("fans", 0, 1000);
            
            // 构建用户ID和粉丝数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (UserEntity user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0;
                    // 添加次要排序因子（获赞数），确保排序稳定
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("fans", userScores);
            
            log.info("粉丝数排行榜更新完成，用户数量: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新粉丝数排行榜失败", e);
        }
    }

    /**
     * 更新获赞数排行榜
     */
    private void updateRankingByLikes() {
        try {
            log.debug("开始更新获赞数排行榜");
            
            // 从数据库查询所有正常状态的用户
            List<UserEntity> users = userRepository.findUserRanking("likes", 0, 1000);
            
            // 构建用户ID和获赞数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (UserEntity user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0;
                    // 添加次要排序因子（粉丝数），确保排序稳定
                    score = score * 1000000 + (user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("likes", userScores);
            
            log.info("获赞数排行榜更新完成，用户数量: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新获赞数排行榜失败", e);
        }
    }

    /**
     * 更新帖子数排行榜
     */
    private void updateRankingByPosts() {
        try {
            log.debug("开始更新帖子数排行榜");
            
            // 从数据库查询所有正常状态的用户
            List<UserEntity> users = userRepository.findUserRanking("posts", 0, 1000);
            
            // 构建用户ID和帖子数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (UserEntity user : users) {
                if (user != null && user.getId() != null) {
                    // 从UserEntity的postCount字段获取帖子数
                    double score = user.getPostCount() != null ? user.getPostCount().doubleValue() : 0.0;
                    // 添加次要排序因子（获赞数），确保排序稳定
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("posts", userScores);
            
            log.info("帖子数排行榜更新完成，用户数量: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新帖子数排行榜失败", e);
        }
    }

    /**
     * 更新综合排行榜
     * 综合分数 = 粉丝数 * 0.4 + 获赞数 * 0.4 + 帖子数 * 0.2
     */
    private void updateRankingByComprehensive() {
        try {
            log.debug("开始更新综合排行榜");
            
            // 从数据库查询所有正常状态的用户
            List<UserEntity> users = userRepository.findUserRanking("comprehensive", 0, 1000);
            
            // 构建用户ID和综合分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (UserEntity user : users) {
                if (user != null && user.getId() != null) {
                    long fansCount = user.getFansCount() != null ? user.getFansCount() : 0L;
                    long likeCount = user.getLikeCount() != null ? user.getLikeCount() : 0L;
                    long postCount = user.getPostCount() != null ? user.getPostCount() : 0L;
                    
                    // 计算综合分数
                    double score = fansCount * 0.4 + likeCount * 0.4 + postCount * 0.2;
                    userScores.put(user.getId(), score);
                }
            }
            
            // 更新Redis缓存
            userRankingCacheRepository.cacheUserRanking("comprehensive", userScores);
            
            log.info("综合排行榜更新完成，用户数量: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新综合排行榜失败", e);
        }
    }
}

