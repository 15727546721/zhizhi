package cn.xu.task;

import cn.xu.cache.UserRankingCacheRepository;
import cn.xu.model.entity.User;
import cn.xu.repository.IUserRepository;
import cn.xu.repository.impl.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户排行榜缓存更新任务
 * 定时更新Redis中的用户排行榜缓存
 * 
 * @author zhizhi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRankingCacheUpdateTask {

    private final IUserRepository userRepository;
    private final PostRepository postRepository;
    private final UserRankingCacheRepository userRankingCacheRepository;

    /**
     * 定时更新用户排行榜缓存
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void updateUserRankingCache() {
        log.info("开始更新用户排行榜缓存");
        
        try {
            // 更新粉丝数排行榜
            updateRankingByFans();
            
            // 更新获赞数排行榜
            updateRankingByLikes();
            
            // 更新帖子数排行榜
            updateRankingByPosts();
            
            // 更新综合排行榜
            updateRankingByComprehensive();
            
            log.info("用户排行榜缓存更新完成");
        } catch (Exception e) {
            log.error("用户排行榜缓存更新失败", e);
        }
    }

    /**
     * 更新粉丝数排行榜
     */
    private void updateRankingByFans() {
        try {
            log.debug("开始更新粉丝数排行榜");
            
            // 查询粉丝数前1000的用户
            List<User> users = userRepository.findUserRanking("fans", 0, 1000);
            
            // 转换为用户ID和分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0;
                    // 次级排序：获赞数
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 写入Redis缓存
            userRankingCacheRepository.cacheUserRanking("fans", userScores);
            
            log.info("粉丝数排行榜更新完成, 用户数: {}", userScores.size());
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
            
            // 查询获赞数前1000的用户
            List<User> users = userRepository.findUserRanking("likes", 0, 1000);
            
            // 转换为用户ID和分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0;
                    // 次级排序：粉丝数
                    score = score * 1000000 + (user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 写入Redis缓存
            userRankingCacheRepository.cacheUserRanking("likes", userScores);
            
            log.info("获赞数排行榜更新完成, 用户数: {}", userScores.size());
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
            
            // 查询帖子数前1000的用户
            List<User> users = userRepository.findUserRanking("posts", 0, 1000);
            
            // 转换为用户ID和分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    double score = user.getPostCount() != null ? user.getPostCount().doubleValue() : 0.0;
                    // 次级排序：获赞数
                    score = score * 1000000 + (user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0);
                    userScores.put(user.getId(), score);
                }
            }
            
            // 写入Redis缓存
            userRankingCacheRepository.cacheUserRanking("posts", userScores);
            
            log.info("帖子数排行榜更新完成, 用户数: {}", userScores.size());
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
            
            // 查询综合排行榜前1000的用户
            List<User> users = userRepository.findUserRanking("comprehensive", 0, 1000);
            
            // 转换为用户ID和分数的映射
            Map<Long, Double> userScores = new HashMap<>();
            for (User user : users) {
                if (user != null && user.getId() != null) {
                    long fansCount = user.getFansCount() != null ? user.getFansCount() : 0L;
                    long likeCount = user.getLikeCount() != null ? user.getLikeCount() : 0L;
                    long postCount = user.getPostCount() != null ? user.getPostCount() : 0L;
                    
                    // 计算综合分数
                    double score = fansCount * 0.4 + likeCount * 0.4 + postCount * 0.2;
                    userScores.put(user.getId(), score);
                }
            }
            
            // 写入Redis缓存
            userRankingCacheRepository.cacheUserRanking("comprehensive", userScores);
            
            log.info("综合排行榜更新完成, 用户数: {}", userScores.size());
        } catch (Exception e) {
            log.error("更新综合排行榜失败", e);
        }
    }
}
